package com.parkarsite.g6a;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Locale;

public final class IdentityDiagnosticStore {
    public record Snapshot(String exactSha,String basenameSha,String lowercaseSha,int charLength,int componentCount){}

    private final File file;

    public IdentityDiagnosticStore(File root) throws IOException {
        if(root==null) throw new IOException("Missing diagnostic root");
        if(!root.exists()&&!root.mkdirs()) throw new IOException("Cannot create diagnostic root");
        file=new File(root,".g6a_identity_diag.tsv");
    }

    public synchronized void commit(String remoteIdentity) throws IOException {
        Snapshot s=derive(remoteIdentity);
        File part=new File(file.getParentFile(),file.getName()+".part");
        String line=s.exactSha()+"\t"+s.basenameSha()+"\t"+s.lowercaseSha()+"\t"+s.charLength()+"\t"+s.componentCount()+"\n";
        try(FileOutputStream out=new FileOutputStream(part)){
            out.write(line.getBytes(StandardCharsets.UTF_8));
            out.flush();
            out.getFD().sync();
        }
        try{
            Files.move(part.toPath(),file.toPath(),StandardCopyOption.REPLACE_EXISTING,StandardCopyOption.ATOMIC_MOVE);
        }catch(AtomicMoveNotSupportedException e){
            Files.move(part.toPath(),file.toPath(),StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public synchronized Snapshot load() throws IOException {
        if(!file.isFile()) return null;
        String raw=new String(Files.readAllBytes(file.toPath()),StandardCharsets.UTF_8).trim();
        if(raw.isEmpty()) return null;
        String[] p=raw.split("\t",-1);
        if(p.length!=5) throw new IOException("Invalid identity diagnostic capsule");
        if(!OpaqueIdentity.isOpaqueId(p[0])||!OpaqueIdentity.isOpaqueId(p[1])||!OpaqueIdentity.isOpaqueId(p[2]))
            throw new IOException("Invalid diagnostic opaque hash");
        int len=Integer.parseInt(p[3]);
        int components=Integer.parseInt(p[4]);
        if(len<1||components<1) throw new IOException("Invalid diagnostic shape");
        return new Snapshot(p[0],p[1],p[2],len,components);
    }

    public static Snapshot derive(String remoteIdentity){
        if(remoteIdentity==null||remoteIdentity.isEmpty()) throw new IllegalArgumentException("identity");
        String basename=basename(remoteIdentity);
        String lower=remoteIdentity.toLowerCase(Locale.ROOT);
        return new Snapshot(
                OpaqueIdentity.sha256(remoteIdentity),
                OpaqueIdentity.sha256(basename),
                OpaqueIdentity.sha256(lower),
                remoteIdentity.length(),
                componentCount(remoteIdentity));
    }

    public static String token(String sha){
        if(!OpaqueIdentity.isOpaqueId(sha)) return "<invalid>";
        return sha.substring(0,12);
    }

    private static String basename(String s){
        int slash=Math.max(s.lastIndexOf('/'),s.lastIndexOf('\\'));
        return slash>=0?s.substring(slash+1):s;
    }

    private static int componentCount(String s){
        String x=s.replace('\\','/');
        int count=0;
        for(String part:x.split("/",-1)) if(!part.isEmpty()) count++;
        return Math.max(1,count);
    }
}
