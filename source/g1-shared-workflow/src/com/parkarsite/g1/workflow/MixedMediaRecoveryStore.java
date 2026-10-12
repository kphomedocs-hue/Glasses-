package com.parkarsite.g1.workflow;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.*;
import java.util.*;

/**
 * V4 Step 12: offline-only mixed JPG/MP4/OPUS durable transaction reference.
 * Never opens a network connection, accesses Android radios, or retries a Source.
 * Shapes of MP4/OPUS are NOT decoder/codec fidelity proof.
 * Existing G6A JPG-only source and frozen release folders are not modified.
 */
public final class MixedMediaRecoveryStore {
    public enum Kind { JPG, MP4, OPUS }
    public enum State { PREPARED, FETCH_MAY_HAVE_STARTED, VERIFIED, COMMITTED }
    public enum Recovery { NOT_FETCHED, REMOTE_UNCERTAIN, LOCAL_RECOVERED, COMPLETE, MANUAL_REVIEW }
    public enum Barrier { AFTER_FETCH_MARK, AFTER_PART_SYNC, AFTER_VERIFIED_JOURNAL,
        AFTER_FINAL_MOVE, AFTER_RECEIPT }
    public interface Source { void writeOnce(OutputStream out) throws Exception; }
    public interface Fault { void reached(Barrier at) throws Exception; }
    public record Ticket(String opaqueId,Kind kind,String relativePath,State state,
                         long firstSeenEpochMs,long captureEpochMs,long bytes,String digest) {}
    public record Result(String opaqueId, Recovery outcome) {}
    private static final long CAP=33_554_432L;
    private static final int METADATA_CAP=4096;
    private final Path root,journals,receipts,parts,lock,legacy,legacyReceipts;
    private final ZoneId zone;

    public MixedMediaRecoveryStore(Path root,ZoneId zone)throws IOException {
        this.root=Objects.requireNonNull(root).toAbsolutePath().normalize();
        this.zone=Objects.requireNonNull(zone);
        Files.createDirectories(this.root);
        journals=this.root.resolve(".mixed-journals");
        receipts=this.root.resolve(".mixed-receipts");
        parts=this.root.resolve(".mixed-parts");
        legacy=this.root.resolve(".transactions");
        legacyReceipts=this.root.resolve(".receipts");
        Files.createDirectories(journals);
        Files.createDirectories(receipts);
        Files.createDirectories(parts);
        lock=this.root.resolve(".mixed-single-writer.lock");
    }
    private interface Locked<T>{T run()throws Exception;}
    private <T> T exclusive(Locked<T> block)throws Exception{
        try(FileChannel channel=FileChannel.open(lock,StandardOpenOption.CREATE,StandardOpenOption.WRITE);
            FileLock ignored=channel.lock()){
            return block.run();
        }
    }
    private static void requireId(String id){
        if(id==null||!id.matches("[0-9a-f]{64}"))
            throw new IllegalArgumentException("Only opaque SHA-256 file identities");
    }
    private static String extension(Kind kind){
        return switch(kind){case JPG->"jpg";case MP4->"mp4";case OPUS->"opus";};
    }
    private static Path journalPath(Path directory,String id){return directory.resolve(id+".txn");}
    private Path journal(String id){return journalPath(journals,id);}
    private Path receiptPath(String id){return receipts.resolve(id+".receipt");}
    private Path part(String id){return parts.resolve(id+".part");}
    private Path finalFile(Ticket t){return root.resolve(t.relativePath()).normalize();}
    private static boolean validRelative(String path,Kind kind){
        return path!=null&&path.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}/[0-9]{4,}\\."
            +extension(kind));
    }
    private static long seq(String relative){
        String n=relative.substring(relative.indexOf('/')+1,relative.lastIndexOf('.'));
        return Long.parseLong(n);
    }
    private static String day(long epoch,ZoneId zone){
        return Instant.ofEpochMilli(epoch).atZone(zone).toLocalDate().toString();
    }
    private static String utf8(Path p)throws IOException {
        if(!Files.isRegularFile(p)||Files.size(p)>METADATA_CAP)
            throw new IOException("Invalid transaction file size/type");
        try {
            return StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(Files.readAllBytes(p))).toString();
        }catch(CharacterCodingException failure){
            throw new IOException("Malformed transaction encoding",failure);
        }
    }
    private static void fsyncDirectory(Path directory)throws IOException {
        try(FileChannel channel=FileChannel.open(directory,StandardOpenOption.READ)){
            channel.force(true);
        }
    }
    private static void atomicWrite(Path path,String text)throws IOException {
        byte[] raw=text.getBytes(StandardCharsets.UTF_8);
        if(raw.length>METADATA_CAP)throw new IOException("Metadata cap exceeded");
        Path next=path.resolveSibling(path.getFileName()+".next");
        if(Files.exists(next))throw new IOException("Unresolved metadata pending write");
        try(FileChannel c=FileChannel.open(next,StandardOpenOption.CREATE_NEW,StandardOpenOption.WRITE)){
            ByteBuffer buf=ByteBuffer.wrap(raw);
            while(buf.hasRemaining())c.write(buf);
            c.force(true);
        }
        Files.move(next,path,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);
        fsyncDirectory(path.getParent());
    }
    private static String serialize(Ticket t){
        return "MM2\t"+t.opaqueId()+"\t"+t.kind()+"\t"+t.relativePath()+"\t"+
            t.state()+"\t"+t.firstSeenEpochMs()+"\t"+t.captureEpochMs()+"\t"+
            t.bytes()+"\t"+t.digest()+"\n";
    }
    private static String receiptText(Ticket t){
        return "MMRECEIPT2\t"+t.opaqueId()+"\t"+t.kind()+"\t"+t.relativePath()+
            "\t"+t.firstSeenEpochMs()+"\t"+t.captureEpochMs()+"\t"+
            t.bytes()+"\t"+t.digest()+"\n";
    }
    private Ticket read(Path path)throws IOException {
        String[] v=utf8(path).stripTrailing().split("\t",-1);
        if(v.length!=9 || !"MM2".equals(v[0]))throw new IOException("Invalid mixed journal");
        requireId(v[1]);
        if(!path.getFileName().toString().equals(v[1]+".txn"))
            throw new IOException("Journal identity mismatch");
        try {
            Kind kind=Kind.valueOf(v[2]);
            State state=State.valueOf(v[4]);
            long firstSeen=Long.parseLong(v[5]),captured=Long.parseLong(v[6]),
                bytes=Long.parseLong(v[7]);
            if(firstSeen<0||captured<0||!validRelative(v[3],kind)
                ||!v[3].startsWith(day(firstSeen,zone)+"/")
                ||bytes<0||bytes>CAP || !(v[8].equals("-")||v[8].matches("[0-9a-f]{64}")))
                throw new IOException("Invalid mixed journal values");
            if((state==State.PREPARED||state==State.FETCH_MAY_HAVE_STARTED)
                &&(bytes!=0||!v[8].equals("-")))throw new IOException("Invalid pending state");
            if((state==State.VERIFIED||state==State.COMMITTED)
                &&(bytes<=0||v[8].length()!=64))throw new IOException("Invalid complete state");
            return new Ticket(v[1],kind,v[3],state,firstSeen,captured,bytes,v[8]);
        }catch(IllegalArgumentException failure){
            throw new IOException("Invalid mixed journal fields",failure);
        }
    }
    private void save(Ticket ticket)throws IOException{atomicWrite(journal(ticket.opaqueId()),serialize(ticket));}
    private void writeReceipt(Ticket ticket)throws IOException {
        Path file=receiptPath(ticket.opaqueId());
        String expected=receiptText(ticket);
        if(Files.exists(file)){
            if(!utf8(file).equals(expected))throw new IOException("Receipt conflict");
            return;
        }
        atomicWrite(file,expected);
    }
    private boolean hasLegacyId(String id)throws IOException{
        if(Files.exists(legacy)&&!Files.isDirectory(legacy))
            throw new IOException("Unexpected legacy journal directory");
        if(Files.exists(legacyReceipts)&&!Files.isDirectory(legacyReceipts))
            throw new IOException("Unexpected legacy receipt directory");
        // A legacy receipt with a missing journal is still a NO-GET collision.
        return Files.exists(journalPath(legacy,id))
            || Files.exists(legacyReceipts.resolve(id+".receipt"));
    }
    private long legacyMaxOrdinal(String date)throws IOException {
        if(!Files.exists(legacy))return 0;
        if(!Files.isDirectory(legacy))throw new IOException("Unexpected legacy journals");
        long max=0;
        try(DirectoryStream<Path> entries=Files.newDirectoryStream(legacy,"*.txn")){
            for(Path entry:entries){
                String[] v=utf8(entry).stripTrailing().split("\\t",-1);
                if(v.length!=6 || !v[0].equals("TXN1")
                    || !v[1].matches("[0-9a-f]{64}")
                    || !entry.getFileName().toString().equals(v[1]+".txn")
                    || !v[2].matches("[0-9]{4}-[0-9]{2}-[0-9]{2}/[0-9]{4,}\\\\.jpg"))
                    throw new IOException("Unrecognized legacy ledger; migration blocked");
                if(v[2].startsWith(date+"/"))
                    max=Math.max(max,seq(v[2]));
            }
        }
        return max;
    }
    /** Conservative migration: legacy identities are never reimported or overwritten. */
    public boolean legacyIdentityPresent(String opaqueId)throws Exception{
        requireId(opaqueId);
        return exclusive(()->hasLegacyId(opaqueId));
    }
    /**
     * Reserve one shared daily ordinal across all three media kinds.
     * Daily partition comes from local first-seen, not vendor capture time;
     * both timestamps remain independently and durably recorded.
     */
    public Ticket prepare(String opaqueId,Kind kind,long firstSeenEpochMs,
                          long captureEpochMs)throws Exception{
        requireId(opaqueId);Objects.requireNonNull(kind);
        if(firstSeenEpochMs<0 || captureEpochMs<0)
            throw new IllegalArgumentException("Negative timestamp");
        return exclusive(()->{
            if(hasLegacyId(opaqueId))
                throw new IOException("Legacy identity exists; manual migration review, no re-fetch");
            if(Files.exists(journal(opaqueId))){
                Ticket previous=read(journal(opaqueId));
                if(previous.kind()!=kind || previous.firstSeenEpochMs()!=firstSeenEpochMs
                    || previous.captureEpochMs()!=captureEpochMs)
                    throw new IOException("Opaque identity metadata conflict");
                return previous;
            }
            String date=day(firstSeenEpochMs,zone);
            long max=legacyMaxOrdinal(date);
            try(DirectoryStream<Path> entries=Files.newDirectoryStream(journals,"*.txn")){
                for(Path file:entries){
                    var previous=read(file);
                    if(previous.relativePath().startsWith(date+"/"))
                        max=Math.max(max,seq(previous.relativePath()));
                }
            }
            Path dir=root.resolve(date);
            if(Files.exists(dir)){
                if(!Files.isDirectory(dir))throw new IOException("Invalid date directory");
                try(DirectoryStream<Path> entries=Files.newDirectoryStream(dir)){
                    for(Path file:entries){
                        String name=file.getFileName().toString();
                        if(name.matches("[0-9]{4,}\\.(jpg|mp4|opus)"))
                            max=Math.max(max,Long.parseLong(name.substring(0,name.indexOf('.'))));
                    }
                }
            }
            if(max>=Integer.MAX_VALUE)throw new IOException("Daily numbering exhausted");
            String rel=date+"/"+String.format(Locale.ROOT,"%04d",max+1)+"."+extension(kind);
            if(Files.exists(root.resolve(rel)))throw new IOException("Archive collision");
            var ticket=new Ticket(opaqueId,kind,rel,State.PREPARED,firstSeenEpochMs,
                captureEpochMs,0,"-");
            save(ticket);
            return ticket;
        });
    }
    private static String checksum(Path file)throws IOException{
        try{
            MessageDigest d=MessageDigest.getInstance("SHA-256");
            try(InputStream in=Files.newInputStream(file)){
                byte[] buf=new byte[16384];int n;
                while((n=in.read(buf))!=-1)d.update(buf,0,n);
            }
            return HexFormat.of().formatHex(d.digest());
        }catch(java.security.NoSuchAlgorithmException impossible){throw new IOException(impossible);}
    }
    private static final class Capped extends FilterOutputStream{
        private long count;
        Capped(OutputStream out){super(out);}
        private void check(int increment)throws IOException{
            if(increment<0||count>CAP-increment)throw new IOException("Media length cap");
        }
        @Override public void write(int value)throws IOException{check(1);out.write(value);count++;}
        @Override public void write(byte[] b,int off,int len)throws IOException{
            Objects.checkFromIndexSize(off,len,b.length);check(len);out.write(b,off,len);count+=len;
        }
    }
    private static boolean shape(Path file,Kind kind)throws IOException{
        if(!Files.isRegularFile(file)||Files.size(file)<5 || Files.size(file)>CAP)return false;
        if(kind==Kind.JPG){
            try(RandomAccessFile raf=new RandomAccessFile(file.toFile(),"r")){
                if(raf.readUnsignedByte()!=255||raf.readUnsignedByte()!=216
                    ||raf.readUnsignedByte()!=255)return false;
                raf.seek(raf.length()-2);
                return raf.readUnsignedByte()==255&&raf.readUnsignedByte()==217;
            }
        }
        if(kind==Kind.OPUS){
            if(Files.size(file)<36)return false;
            try(RandomAccessFile raf=new RandomAccessFile(file.toFile(),"r")){
                byte[] header=new byte[36];raf.readFully(header);
                return new String(header,0,4,StandardCharsets.US_ASCII).equals("OggS")
                    && new String(header,28,8,StandardCharsets.US_ASCII).equals("OpusHead");
            }
        }
        // MP4 synthetic ISO-BMFF shape: one finite ftyp and mdat box;
        // no claim that media payload is playable or vendor container format.
        if(Files.size(file)<16)return false;
        try(RandomAccessFile raf=new RandomAccessFile(file.toFile(),"r")){
            long limit=raf.length(),pos=0;boolean ftyp=false,mdat=false;
            while(pos<limit){
                if(limit-pos<8)return false;
                raf.seek(pos);
                long size=Integer.toUnsignedLong(raf.readInt());
                byte[] type=new byte[4];raf.readFully(type);
                if(size<8||size>limit-pos)return false;
                String label=new String(type,StandardCharsets.US_ASCII);
                if("ftyp".equals(label))ftyp=true;
                if("mdat".equals(label))mdat=true;
                pos+=size;
            }
            return ftyp&&mdat&&pos==limit;
        }
    }
    private static boolean matches(Path file,Ticket ticket)throws IOException{
        return Files.isRegularFile(file)&&Files.size(file)==ticket.bytes()
            &&checksum(file).equals(ticket.digest()) &&shape(file,ticket.kind());
    }
    /** Journal FETCH marker is fsynced BEFORE Source; after any ambiguity there is no retry. */
    public Ticket execute(Ticket reservation,long expectedBytes,Source source,Fault fault)
            throws Exception{
        Objects.requireNonNull(reservation);Objects.requireNonNull(source);Objects.requireNonNull(fault);
        requireId(reservation.opaqueId());
        return exclusive(()->{
            if(hasLegacyId(reservation.opaqueId()))
                throw new IOException("Legacy identity cannot be reread");
            if(expectedBytes<=0||expectedBytes>CAP)throw new IOException("Invalid expected bytes");
            Ticket current=read(journal(reservation.opaqueId()));
            if(!current.equals(reservation)||current.state()!=State.PREPARED)
                throw new IOException("One-shot reservation changed, no repeat");
            Path quarantine=part(current.opaqueId());
            if(Files.exists(quarantine)||Files.exists(finalFile(current)))
                throw new IOException("Unresolved prefetch or archive collision");
            var fetching=new Ticket(current.opaqueId(),current.kind(),current.relativePath(),
                State.FETCH_MAY_HAVE_STARTED,current.firstSeenEpochMs(),
                current.captureEpochMs(),0,"-");
            save(fetching);
            fault.reached(Barrier.AFTER_FETCH_MARK);
            try(FileOutputStream output=new FileOutputStream(quarantine.toFile());
                OutputStream capped=new Capped(output)){
                source.writeOnce(capped);
                capped.flush();
                output.getFD().sync();
            }
            fsyncDirectory(parts);
            fault.reached(Barrier.AFTER_PART_SYNC);
            if(Files.size(quarantine)!=expectedBytes || !shape(quarantine,current.kind()))
                throw new IOException("Incomplete/invalid synthetic media; remote uncertain");
            String digest=checksum(quarantine);
            var verified=new Ticket(current.opaqueId(),current.kind(),current.relativePath(),
                State.VERIFIED,current.firstSeenEpochMs(),current.captureEpochMs(),
                expectedBytes,digest);
            save(verified);
            fault.reached(Barrier.AFTER_VERIFIED_JOURNAL);
            Path dest=finalFile(verified);
            Files.createDirectories(dest.getParent());
            fsyncDirectory(root);
            if(Files.exists(dest))throw new IOException("Refuse overwrite");
            Files.move(quarantine,dest,StandardCopyOption.ATOMIC_MOVE);
            fsyncDirectory(dest.getParent());fsyncDirectory(parts);
            fault.reached(Barrier.AFTER_FINAL_MOVE);
            writeReceipt(verified);
            fault.reached(Barrier.AFTER_RECEIPT);
            var completed=new Ticket(verified.opaqueId(),verified.kind(),verified.relativePath(),
                State.COMMITTED,verified.firstSeenEpochMs(),verified.captureEpochMs(),
                verified.bytes(),verified.digest());
            save(completed);
            return completed;
        });
    }
    /** Reopens and locally promotes bytes only. Does NOT invoke Source. */
    public List<Result> recover()throws Exception{
        return exclusive(()->{
            List<Result> results=new ArrayList<>();
            try(DirectoryStream<Path> entries=Files.newDirectoryStream(journals,"*.txn")){
                for(Path path:entries){
                    Ticket t=read(path);
                    Path quarantined=part(t.opaqueId()),dest=finalFile(t);
                    Recovery outcome;
                    if(t.state()==State.PREPARED){
                        outcome=Files.exists(quarantined)||Files.exists(dest)
                            ||Files.exists(receiptPath(t.opaqueId()))
                            ?Recovery.MANUAL_REVIEW:Recovery.NOT_FETCHED;
                    }else if(t.state()==State.FETCH_MAY_HAVE_STARTED){
                        outcome=Recovery.REMOTE_UNCERTAIN;
                    }else if(t.state()==State.VERIFIED){
                        boolean hasPart=Files.exists(quarantined),hasDest=Files.exists(dest);
                        if(hasPart==hasDest)outcome=Recovery.MANUAL_REVIEW;
                        else {
                            Path candidate=hasPart?quarantined:dest;
                            if(!matches(candidate,t))outcome=Recovery.MANUAL_REVIEW;
                            else {
                                if(hasPart){
                                    Files.createDirectories(dest.getParent());
                                    fsyncDirectory(root);
                                    Files.move(quarantined,dest,StandardCopyOption.ATOMIC_MOVE);
                                    fsyncDirectory(dest.getParent());fsyncDirectory(parts);
                                }
                                writeReceipt(t);
                                save(new Ticket(t.opaqueId(),t.kind(),t.relativePath(),
                                    State.COMMITTED,t.firstSeenEpochMs(),t.captureEpochMs(),
                                    t.bytes(),t.digest()));
                                outcome=Recovery.LOCAL_RECOVERED;
                            }
                        }
                    }else {
                        if(Files.exists(quarantined) || !matches(dest,t)
                            || !Files.exists(receiptPath(t.opaqueId()))
                            || !utf8(receiptPath(t.opaqueId())).equals(receiptText(t)))
                            outcome=Recovery.MANUAL_REVIEW;
                        else outcome=Recovery.COMPLETE;
                    }
                    results.add(new Result(t.opaqueId(),outcome));
                }
            }
            return List.copyOf(results);
        });
    }
    public boolean committedAndIntact(String opaqueId)throws Exception{
        requireId(opaqueId);
        return exclusive(()->{
            if(!Files.exists(journal(opaqueId)))return false;
            Ticket t=read(journal(opaqueId));
            return t.state()==State.COMMITTED&&!Files.exists(part(opaqueId))
                &&Files.exists(receiptPath(opaqueId))
                &&utf8(receiptPath(opaqueId)).equals(receiptText(t))
                &&matches(finalFile(t),t);
        });
    }
}
