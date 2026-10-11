package com.parkarsite.g6a.recovery;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Offline-only, single-writer reference model for a possibly consumptive media read.
 * This class has no Android, BLE, Wifi, HTTP, or automatic retry functionality.
 */
public final class RecoveryImportStore {
    public enum State { PREPARED, FETCH_MAY_HAVE_STARTED, VERIFIED, COMMITTED }
    public enum Disposition { NOT_FETCHED, REMOTE_UNCERTAIN, LOCAL_RECOVERED, COMPLETE, MANUAL_REVIEW }
    public enum Barrier { AFTER_FETCH_MARK, AFTER_PART_SYNC, AFTER_VERIFIED_JOURNAL,
                          AFTER_FINAL_MOVE, AFTER_RECEIPT, AFTER_COMMITTED_JOURNAL }
    public interface Source { void streamTo(OutputStream output) throws Exception; }
    public interface FailureInjector { void reached(Barrier barrier) throws Exception; }
    public record Ticket(String opaqueId, String relativePath, State state, long bytes, String digest) {}
    public record RecoveryResult(String opaqueId, Disposition disposition, String detail) {}

    private static final long MAX_BYTES = 33_554_432L;
    private static final DateTimeFormatter DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private final Path root, txns, receipts, parts, lock;
    private final ZoneId zone;

    public RecoveryImportStore(Path root, ZoneId zone) throws IOException {
        this.root = root.toAbsolutePath().normalize();
        this.zone = Objects.requireNonNull(zone);
        Files.createDirectories(this.root);
        this.txns = this.root.resolve(".transactions");
        this.receipts = this.root.resolve(".receipts");
        this.parts = this.root.resolve(".quarantine");
        Files.createDirectories(txns); Files.createDirectories(receipts); Files.createDirectories(parts);
        this.lock = this.root.resolve(".single-writer.lock");
    }

    private interface Locked<T> { T run() throws Exception; }
    private <T> T exclusive(Locked<T> body) throws Exception {
        try (FileChannel channel = FileChannel.open(lock, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
             FileLock ignored = channel.lock()) {
            return body.run();
        }
    }

    public Ticket prepare(String opaqueId, long firstSeenEpochMs) throws Exception {
        return exclusive(() -> {
            requireId(opaqueId);
            Path existing = txn(opaqueId);
            if (Files.exists(existing)) return load(existing);
            String day = DATE.format(Instant.ofEpochMilli(firstSeenEpochMs).atZone(zone));
            int max = 0;
            try (DirectoryStream<Path> entries = Files.newDirectoryStream(txns, "*.txn")) {
                for (Path e : entries) {
                    Ticket t = load(e);
                    if (t.relativePath.startsWith(day + "/")) max = Math.max(max, ordinal(t.relativePath));
                }
            }
            Path directory = root.resolve(day);
            if (Files.exists(directory)) {
                try (DirectoryStream<Path> entries = Files.newDirectoryStream(directory, "*.jpg")) {
                    for (Path e : entries) {
                        String n=e.getFileName().toString();
                        if (n.matches("[0-9]{4,}\\.jpg")) max = Math.max(max,Integer.parseInt(n.substring(0,n.length()-4)));
                    }
                }
            }
            if (max == Integer.MAX_VALUE) throw new IOException("No numbering slots left");
            String rel = day + "/" + String.format(Locale.ROOT,"%04d.jpg", max+1);
            Ticket t = new Ticket(opaqueId,rel,State.PREPARED,0,"-");
            save(t);
            return t;
        });
    }

    /** Begin exactly once: durable MAY_HAVE_STARTED must precede the caller's remote byte read. */
    public Ticket execute(Ticket ticket, long expectedBytes, Source source, FailureInjector fault) throws Exception {
        return exclusive(() -> {
            Objects.requireNonNull(source);
            Objects.requireNonNull(fault);
            Objects.requireNonNull(ticket);
            requireId(ticket.opaqueId);
            if(expectedBytes<=0 || expectedBytes>MAX_BYTES) throw new IOException("Missing or invalid expected length; no remote fetch");
            Ticket current=load(txn(ticket.opaqueId));
            if (!current.relativePath.equals(ticket.relativePath) || current.state != State.PREPARED)
                throw new IOException("Fetch not allowed for nonprepared/changed ticket");
            Path part=part(current.opaqueId);
            if (Files.exists(part)) throw new IOException("Unexpected partial file; fail closed");
            Ticket started=new Ticket(current.opaqueId,current.relativePath,State.FETCH_MAY_HAVE_STARTED,0,"-");
            save(started); // must be on stable storage before touching the remote source
            fault.reached(Barrier.AFTER_FETCH_MARK);
            try(FileOutputStream file=new FileOutputStream(part.toFile());
                OutputStream capped=new BoundedOutputStream(file,MAX_BYTES)) {
                source.streamTo(capped);
                capped.flush(); file.getFD().sync();
            }
            forceDirectory(parts);
            fault.reached(Barrier.AFTER_PART_SYNC);
            long size=Files.size(part);
            if(size!=expectedBytes) throw new IOException("Downloaded byte length incomplete/incorrect: manual review, no retry");
            validateJpg(part);
            String digest=sha256(part);
            Ticket verified=new Ticket(current.opaqueId,current.relativePath,State.VERIFIED,size,digest);
            save(verified);
            fault.reached(Barrier.AFTER_VERIFIED_JOURNAL);
            Path destination=finalPath(verified.relativePath);
            Files.createDirectories(destination.getParent());
            forceDirectory(root);
            if(Files.exists(destination)) throw new IOException("Final file unexpectedly exists; no overwrite");
            Files.move(part,destination,StandardCopyOption.ATOMIC_MOVE);
            forceDirectory(destination.getParent());
            forceDirectory(parts);
            fault.reached(Barrier.AFTER_FINAL_MOVE);
            receipt(verified);
            fault.reached(Barrier.AFTER_RECEIPT);
            Ticket complete=new Ticket(current.opaqueId,current.relativePath,State.COMMITTED,size,digest);
            save(complete);
            fault.reached(Barrier.AFTER_COMMITTED_JOURNAL);
            return complete;
        });
    }

    /** Recovery can promote a validated local file; it NEVER invokes Source or retries a remote fetch. */
    public List<RecoveryResult> recover() throws Exception {
        return exclusive(() -> {
            List<RecoveryResult> results=new ArrayList<>();
            try(DirectoryStream<Path> files=Files.newDirectoryStream(txns,"*.txn")) {
                for(Path path:files) {
                    Ticket t=load(path);
                    Path part=part(t.opaqueId);
                    Path fin=finalPath(t.relativePath);
                    switch(t.state) {
                        case PREPARED -> {
                            if(Files.exists(part)||Files.exists(fin)||Files.exists(receiptFile(t.opaqueId)))
                                results.add(new RecoveryResult(t.opaqueId,Disposition.MANUAL_REVIEW,"unexpected files before fetch"));
                            else results.add(new RecoveryResult(t.opaqueId,Disposition.NOT_FETCHED,"no fetch marker"));
                        }
                        case FETCH_MAY_HAVE_STARTED ->
                            results.add(new RecoveryResult(t.opaqueId,Disposition.REMOTE_UNCERTAIN,
                               "partial or remote state unknown; no automatic retry or cleanup"));
                        case VERIFIED -> {
                            if(Files.exists(part)==Files.exists(fin)) {
                                results.add(new RecoveryResult(t.opaqueId,Disposition.MANUAL_REVIEW,"zero or two local candidates"));
                            } else {
                                Path candidate=Files.exists(fin)?fin:part;
                                if(!matches(candidate,t)) {
                                    results.add(new RecoveryResult(t.opaqueId,Disposition.MANUAL_REVIEW,"verified candidate integrity mismatch"));
                                    break;
                                }
                                if(Files.exists(part)) {
                                    Files.createDirectories(fin.getParent());
                                    forceDirectory(root);
                                    Files.move(part,fin,StandardCopyOption.ATOMIC_MOVE);
                                    forceDirectory(fin.getParent()); forceDirectory(parts);
                                }
                                receipt(t);
                                save(new Ticket(t.opaqueId,t.relativePath,State.COMMITTED,t.bytes,t.digest));
                                results.add(new RecoveryResult(t.opaqueId,Disposition.LOCAL_RECOVERED,"validated local file promoted; no remote read"));
                            }
                        }
                        case COMMITTED -> {
                            if(!Files.exists(fin)||Files.exists(part)||!matches(fin,t)) {
                                results.add(new RecoveryResult(t.opaqueId,Disposition.MANUAL_REVIEW,"committed file missing, duplicate or corrupted"));
                            } else {
                                receipt(t);
                                results.add(new RecoveryResult(t.opaqueId,Disposition.COMPLETE,"file and receipt validated"));
                            }
                        }
                    }
                }
            }
            return List.copyOf(results);
        });
    }

    /** Do not treat a journal/receipt row as a valid import until the file's bytes are checked. */
    public boolean isCommittedAndIntact(String id) throws Exception {
        return exclusive(() -> {
            requireId(id);
            if(!Files.exists(txn(id)))return false;
            Ticket t=load(txn(id));
            return t.state==State.COMMITTED && Files.exists(receiptFile(id))
                    && Files.readString(receiptFile(id)).equals(serializeReceipt(t))
                    && matches(finalPath(t.relativePath),t);
        });
    }

    private void receipt(Ticket t) throws IOException {
        Path r=receiptFile(t.opaqueId);
        String expected=serializeReceipt(t);
        if(Files.exists(r)) {
            if(!Files.readString(r).equals(expected)) throw new IOException("Receipt conflict; manual review");
            return;
        }
        atomicWrite(r,expected);
    }
    private String serializeReceipt(Ticket t) {return "RECEIPT1\t"+t.opaqueId+"\t"+t.relativePath+"\t"+t.bytes+"\t"+t.digest+"\n";}
    private Path txn(String id){return txns.resolve(id+".txn");}
    private Path part(String id){return parts.resolve(id+".part");}
    private Path receiptFile(String id){return receipts.resolve(id+".receipt");}
    private Path finalPath(String rel){return root.resolve(rel);}
    private void save(Ticket t) throws IOException {atomicWrite(txn(t.opaqueId),"TXN1\t"+t.opaqueId+"\t"+t.relativePath+"\t"+t.state+"\t"+t.bytes+"\t"+t.digest+"\n");}
    private Ticket load(Path path) throws IOException {
        if(!Files.isRegularFile(path))throw new IOException("Missing transaction");
        String[] p=Files.readString(path).stripTrailing().split("\\t",-1);
        if(p.length!=6||!p[0].equals("TXN1"))throw new IOException("Invalid transaction format");
        requireId(p[1]);
        if(!path.getFileName().toString().equals(p[1]+".txn"))throw new IOException("Identity/file mismatch");
        if(!p[2].matches("[0-9]{4}-[0-9]{2}-[0-9]{2}/[0-9]{4,}\\.jpg"))throw new IOException("Unsafe archive path");
        try{
            State state=State.valueOf(p[3]); long bytes=Long.parseLong(p[4]);
            if(bytes<0||bytes>MAX_BYTES|| !(p[5].equals("-")||p[5].matches("[0-9a-f]{64}")))throw new IOException("Unsafe state data");
            if((state==State.PREPARED||state==State.FETCH_MAY_HAVE_STARTED) && !(bytes==0&&p[5].equals("-")))throw new IOException("Invalid pending state");
            if((state==State.VERIFIED||state==State.COMMITTED) && !(bytes>0&&p[5].length()==64))throw new IOException("Invalid verified state");
            return new Ticket(p[1],p[2],state,bytes,p[5]);
        }catch(IllegalArgumentException e){throw new IOException("Invalid transaction values",e);}
    }
    private static int ordinal(String relativePath){String n=relativePath.substring(relativePath.indexOf('/')+1,relativePath.length()-4);return Integer.parseInt(n);}
    private static void requireId(String id){if(id==null||!id.matches("[0-9a-f]{64}"))throw new IllegalArgumentException("Only opaque SHA256 identities allowed");}
    private static void atomicWrite(Path target,String text)throws IOException{
        Path next=target.resolveSibling(target.getFileName()+".next");
        byte[] bytes=text.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        try(FileChannel channel=FileChannel.open(next,StandardOpenOption.CREATE,StandardOpenOption.TRUNCATE_EXISTING,StandardOpenOption.WRITE)) {
            ByteBuffer buffer=ByteBuffer.wrap(bytes);while(buffer.hasRemaining())channel.write(buffer);
            channel.force(true);
        }
        Files.move(next,target,StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);
        forceDirectory(target.getParent());
    }
    private static void forceDirectory(Path directory)throws IOException{
        try(FileChannel channel=FileChannel.open(directory,StandardOpenOption.READ)){channel.force(true);}
    }
    private static boolean matches(Path file,Ticket t)throws IOException{
        return Files.isRegularFile(file)&&Files.size(file)==t.bytes&&sha256(file).equals(t.digest)&&jpegShape(file);
    }
    private static void validateJpg(Path file)throws IOException{if(!jpegShape(file))throw new IOException("JPEG marker validation failed");}
    private static boolean jpegShape(Path file)throws IOException{
        if(!Files.isRegularFile(file)||Files.size(file)<5)return false;
        try(RandomAccessFile raf=new RandomAccessFile(file.toFile(),"r")){
            if(raf.readUnsignedByte()!=255||raf.readUnsignedByte()!=216||raf.readUnsignedByte()!=255)return false;
            raf.seek(raf.length()-2);
            return raf.readUnsignedByte()==255&&raf.readUnsignedByte()==217;
        }
    }
    private static String sha256(Path file)throws IOException{
        try{
            MessageDigest d=MessageDigest.getInstance("SHA-256");
            try(InputStream input=Files.newInputStream(file)){byte[] b=new byte[16384];int n;while((n=input.read(b))!=-1)d.update(b,0,n);}
            return HexFormat.of().formatHex(d.digest());
        }catch(java.security.NoSuchAlgorithmException e){throw new IOException(e);}
    }
    private static class BoundedOutputStream extends FilterOutputStream {
        private final long cap;private long count;
        BoundedOutputStream(OutputStream out,long cap){super(out);this.cap=cap;}
        private void check(long n)throws IOException{if(n<0||count>cap-n)throw new IOException("Media cap exceeded; no retry");}
        @Override public void write(int value)throws IOException{check(1);out.write(value);count++;}
        @Override public void write(byte[] b,int off,int len)throws IOException{Objects.checkFromIndexSize(off,len,b.length);check(len);out.write(b,off,len);count+=len;}
    }
}
