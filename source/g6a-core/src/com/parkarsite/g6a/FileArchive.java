package com.parkarsite.g6a;

import java.io.*;
import java.nio.file.*;
import java.time.ZoneId;
import java.util.*;

public final class FileArchive {
    public interface StreamWriter { void writeTo(OutputStream out) throws Exception; }
    public interface Validator { void validate(File part,long bytes) throws Exception; }

    private final File root;
    private final ZoneId zoneId;
    private final ImportLedger ledger;

    public FileArchive(File root,ZoneId zoneId,ImportLedger ledger){
        this.root=root; this.zoneId=zoneId; this.ledger=ledger;
    }

    public synchronized ImportRecord importOne(String opaqueId,MediaKind kind,long firstSeenEpochMs,
                                               StreamWriter writer,Validator validator) throws Exception {
        if(!OpaqueIdentity.isOpaqueId(opaqueId)) throw new IllegalArgumentException("opaqueId");
        ImportRecord existing=ledger.get(opaqueId);
        if(existing!=null) return existing;

        String day=NumberingPolicy.dailyFolder(firstSeenEpochMs,zoneId);
        File dir=new File(root,day);
        if(!dir.exists()&&!dir.mkdirs()) throw new IOException("Cannot create day directory");

        String[] names=dir.list((d,n)->n.matches("\\d{4,}\\.(?i:jpg|mp4|opus)"));
        List<String> existingNames=names==null?List.of():Arrays.asList(names);
        int next=NumberingPolicy.nextNumber(existingNames);
        String finalName=NumberingPolicy.fileName(next,kind);
        File finalFile=new File(dir,finalName);
        File part=new File(dir,finalName+".part");

        if(finalFile.exists()) throw new IOException("Final destination already exists");
        if(part.exists()&&!part.delete()) throw new IOException("Cannot clear stale part");

        long bytes=0;
        try {
            try(FileOutputStream raw=new FileOutputStream(part);
                CountingOutputStream out=new CountingOutputStream(raw)){
                writer.writeTo(out);
                out.flush(); raw.getFD().sync(); bytes=out.count;
            }
            if(bytes<=0) throw new IOException("Empty import");
            validator.validate(part,bytes);
            moveCommitted(part,finalFile);

            long completed=System.currentTimeMillis();
            String rel=day+"/"+finalName;
            ImportRecord record=new ImportRecord(opaqueId,kind,rel,bytes,firstSeenEpochMs,completed,ImportRecord.COMMITTED);
            ledger.commit(record);
            return record;
        } catch(Exception e){
            if(part.exists()) part.delete();
            throw e;
        }
    }

    public static int removeStaleParts(File root){
        if(root==null||!root.isDirectory()) return 0;
        int count=0;
        File[] dirs=root.listFiles(File::isDirectory);
        if(dirs==null) return 0;
        for(File dir:dirs){
            File[] parts=dir.listFiles((d,n)->n.endsWith(".part"));
            if(parts==null) continue;
            for(File p:parts) if(p.delete()) count++;
        }
        return count;
    }

    private static void moveCommitted(File part,File finalFile) throws IOException {
        try{ Files.move(part.toPath(),finalFile.toPath(),StandardCopyOption.ATOMIC_MOVE); }
        catch(AtomicMoveNotSupportedException e){ Files.move(part.toPath(),finalFile.toPath()); }
    }

    private static final class CountingOutputStream extends FilterOutputStream {
        long count;
        CountingOutputStream(OutputStream out){super(out);}
        @Override public void write(int b)throws IOException{out.write(b);count++;}
        @Override public void write(byte[] b,int off,int len)throws IOException{out.write(b,off,len);count+=len;}
    }
}
