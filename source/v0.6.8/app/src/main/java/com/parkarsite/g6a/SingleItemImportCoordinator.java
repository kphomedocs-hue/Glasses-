package com.parkarsite.g6a;

import java.io.*;
import java.nio.file.Files;

/**
 * Transport-neutral one-item import coordinator.
 * The caller supplies only a private remote identity and a bounded byte stream writer.
 * No remote filename/path is persisted; only its opaque SHA-256 identity is stored.
 */
public final class SingleItemImportCoordinator {
    public interface Source {
        void streamTo(OutputStream out) throws Exception;
    }

    private final FileArchive archive;
    private final ImportLedger ledger;

    public SingleItemImportCoordinator(FileArchive archive, ImportLedger ledger){
        this.archive=archive;
        this.ledger=ledger;
    }

    public synchronized ImportRecord importNewJpg(String privateRemoteIdentity,
                                                  long firstSeenEpochMs,
                                                  Source source) throws Exception {
        String opaqueId=OpaqueIdentity.sha256(privateRemoteIdentity);
        ImportRecord existing=ledger.get(opaqueId);
        if(existing!=null)return existing;

        return archive.importOne(
                opaqueId,
                MediaKind.JPG,
                firstSeenEpochMs,
                source::streamTo,
                SingleItemImportCoordinator::validateJpeg);
    }

    private static void validateJpeg(File part,long bytes) throws Exception {
        if(bytes<5) throw new IOException("JPEG too small");
        try(InputStream in=new BufferedInputStream(new FileInputStream(part))){
            int a=in.read(),b=in.read(),c=in.read();
            if(a!=0xFF||b!=0xD8||c!=0xFF) throw new IOException("JPEG SOI missing");
        }
        try(RandomAccessFile raf=new RandomAccessFile(part,"r")){
            if(raf.length()!=bytes) throw new IOException("Byte count mismatch");
            raf.seek(raf.length()-2);
            if(raf.readUnsignedByte()!=0xFF||raf.readUnsignedByte()!=0xD9) throw new IOException("JPEG EOI missing");
        }
        if(Files.size(part.toPath())!=bytes) throw new IOException("Filesystem size mismatch");
    }
}
