package com.parkarsite.g6a;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public final class ImportLedger {
    private final File file;
    private final LinkedHashMap<String,ImportRecord> committed=new LinkedHashMap<>();

    public ImportLedger(File root) throws IOException {
        if(!root.exists()&&!root.mkdirs()) throw new IOException("Cannot create root");
        file=new File(root,".g6a_imported.tsv");
        load();
    }

    private void load() throws IOException {
        committed.clear();
        if(!file.isFile()) return;
        for(String line:Files.readAllLines(file.toPath(),StandardCharsets.UTF_8)){
            if(line.isBlank()) continue;
            String[] p=line.split("\\t",-1);
            if(p.length!=7) throw new IOException("Invalid ledger row");
            if(!OpaqueIdentity.isOpaqueId(p[0])) throw new IOException("Non-opaque ledger identity");
            MediaKind kind=MediaKind.valueOf(p[1]);
            long bytes=Long.parseLong(p[3]);
            long first=Long.parseLong(p[4]);
            long complete=Long.parseLong(p[5]);
            if(!ImportRecord.COMMITTED.equals(p[6])) throw new IOException("Unexpected ledger status");
            committed.put(p[0],new ImportRecord(p[0],kind,p[2],bytes,first,complete,p[6]));
        }
    }

    public synchronized boolean contains(String opaqueId){
        return committed.containsKey(opaqueId);
    }

    public synchronized ImportRecord get(String opaqueId){
        return committed.get(opaqueId);
    }

    public synchronized int size(){ return committed.size(); }

    public synchronized void commit(ImportRecord r) throws IOException {
        if(!OpaqueIdentity.isOpaqueId(r.opaqueId())) throw new IOException("Identity must be opaque SHA-256");
        if(!ImportRecord.COMMITTED.equals(r.status())) throw new IOException("Only COMMITTED records may be persisted");
        if(r.localRelativePath()==null||r.localRelativePath().isBlank()||r.localRelativePath().contains("..")) throw new IOException("Unsafe relative path");
        if(r.byteCount()<=0) throw new IOException("Invalid byte count");
        if(committed.containsKey(r.opaqueId())) return;
        String row=String.join("\t",
                r.opaqueId(),r.kind().name(),r.localRelativePath(),Long.toString(r.byteCount()),
                Long.toString(r.firstSeenEpochMs()),Long.toString(r.completedEpochMs()),r.status());
        try(FileOutputStream raw=new FileOutputStream(file,true);
            OutputStreamWriter writer=new OutputStreamWriter(raw,StandardCharsets.UTF_8);
            BufferedWriter out=new BufferedWriter(writer)){
            out.write(row); out.newLine(); out.flush(); raw.getFD().sync();
        }
        committed.put(r.opaqueId(),r);
    }
}
