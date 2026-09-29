package com.parkarsite.g6a;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public final class RetentionDiagnosticStore {
    public record Snapshot(List<String> baselineExactHashes,String newItemExactHash){}

    private final File file;

    public RetentionDiagnosticStore(File root) throws IOException {
        if(root==null) throw new IOException("Missing retention diagnostic root");
        if(!root.exists()&&!root.mkdirs()) throw new IOException("Cannot create retention diagnostic root");
        file=new File(root,".g6a_retention_diag.tsv");
    }

    public synchronized Snapshot commit(Collection<String> baselineRemoteEntries,String newRemoteIdentity) throws IOException {
        if(baselineRemoteEntries==null||newRemoteIdentity==null||newRemoteIdentity.isEmpty()) throw new IOException("Missing retention inputs");
        TreeSet<String> baselineHashes=new TreeSet<>();
        for(String entry:baselineRemoteEntries){
            if(entry==null)continue;
            String x=entry.toLowerCase(Locale.ROOT);
            if(x.endsWith(".jpg")) baselineHashes.add(OpaqueIdentity.sha256(entry));
        }
        if(baselineHashes.isEmpty()) throw new IOException("No baseline JPG identities");
        String newHash=OpaqueIdentity.sha256(newRemoteIdentity);
        if(baselineHashes.contains(newHash)) throw new IOException("New identity already exists in baseline");
        File part=new File(file.getParentFile(),file.getName()+".part");
        StringBuilder b=new StringBuilder();
        b.append("V1\t").append(newHash).append('\n');
        for(String h:baselineHashes)b.append("B\t").append(h).append('\n');
        try(FileOutputStream out=new FileOutputStream(part)){
            out.write(b.toString().getBytes(StandardCharsets.UTF_8));
            out.flush();
            out.getFD().sync();
        }
        try{
            Files.move(part.toPath(),file.toPath(),StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);
        }catch(AtomicMoveNotSupportedException e){
            Files.move(part.toPath(),file.toPath(),StandardCopyOption.REPLACE_EXISTING);
        }
        return new Snapshot(List.copyOf(baselineHashes),newHash);
    }

    public synchronized Snapshot load() throws IOException {
        if(!file.isFile()) return null;
        List<String> lines=Files.readAllLines(file.toPath(),StandardCharsets.UTF_8);
        if(lines.isEmpty()) return null;
        String[] head=lines.get(0).split("\t",-1);
        if(head.length!=2||!"V1".equals(head[0])||!OpaqueIdentity.isOpaqueId(head[1])) throw new IOException("Invalid retention header");
        TreeSet<String> baseline=new TreeSet<>();
        for(int i=1;i<lines.size();i++){
            String line=lines.get(i);
            if(line.isBlank())continue;
            String[] p=line.split("\t",-1);
            if(p.length!=2||!"B".equals(p[0])||!OpaqueIdentity.isOpaqueId(p[1])) throw new IOException("Invalid retention baseline row");
            baseline.add(p[1]);
        }
        if(baseline.isEmpty()) throw new IOException("Empty retention baseline");
        return new Snapshot(List.copyOf(baseline),head[1]);
    }

    public static String token(String sha){
        if(!OpaqueIdentity.isOpaqueId(sha)) return "<invalid>";
        return sha.substring(0,12);
    }
}
