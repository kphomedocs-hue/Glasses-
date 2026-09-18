package com.parkarsite.g1capture.storage;

import com.parkarsite.g1capture.g1.G1Transport;
import com.parkarsite.g1capture.g1.RemoteMedia;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Pure-Java archive engine, testable without Android. */
public final class FileMediaArchive {
    private final File root;
    private final ZoneId zoneId;

    public FileMediaArchive(File root, ZoneId zoneId) {
        this.root = root;
        this.zoneId = zoneId;
    }

    public synchronized File save(RemoteMedia media, G1Transport transport) throws Exception {
        String day = NumberingPolicy.dailyFolder(media.captureEpochMs(), zoneId);
        File dir = new File(root, day);
        if (!dir.exists() && !dir.mkdirs()) throw new IllegalStateException("Cannot create " + dir);

        String[] names = dir.list();
        List<String> existing = names == null ? new ArrayList<>() : Arrays.asList(names);
        int next = NumberingPolicy.nextNumber(existing);
        File finalFile = new File(dir, NumberingPolicy.fileName(next, media.extension()));
        File part = new File(dir, finalFile.getName() + ".part");
        File source = sourceSidecar(finalFile);
        if (part.exists() && !part.delete()) throw new IllegalStateException("Cannot clear stale part " + part);
        if (source.exists() && !finalFile.exists() && !source.delete()) {
            throw new IllegalStateException("Cannot clear stale source sidecar " + source);
        }

        long written;
        try {
            try (FileOutputStream raw = new FileOutputStream(part);
                 CountingOutputStream out = new CountingOutputStream(raw)) {
                transport.download(media, out);
                out.flush();
                raw.getFD().sync();
                written = out.count();
            }
            if (written <= 0) throw new IllegalStateException("Downloaded file is empty");
            if (media.sizeBytes() >= 0 && written != media.sizeBytes()) {
                throw new IllegalStateException("Size mismatch. Expected " + media.sizeBytes() + " bytes, got " + written);
            }

            writeSynced(source, media.identityKey());
            moveCompleted(part, finalFile);
            return finalFile;
        } catch (Exception e) {
            if (part.exists()) part.delete();
            if (!finalFile.exists() && source.exists()) source.delete();
            throw e;
        }
    }

    public static File sourceSidecar(File finalFile) {
        return new File(finalFile.getParentFile(), "." + finalFile.getName() + ".source");
    }

    private static void writeSynced(File file, String value) throws Exception {
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(value.getBytes(StandardCharsets.UTF_8));
            out.write('\n');
            out.flush();
            out.getFD().sync();
        }
    }

    private static void moveCompleted(File part, File finalFile) throws Exception {
        try {
            Files.move(part.toPath(), finalFile.toPath(), StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(part.toPath(), finalFile.toPath());
        }
    }

    private static final class CountingOutputStream extends FilterOutputStream {
        private long count;
        CountingOutputStream(OutputStream out) { super(out); }
        @Override public void write(int b) throws java.io.IOException { out.write(b); count++; }
        @Override public void write(byte[] b, int off, int len) throws java.io.IOException { out.write(b, off, len); count += len; }
        long count() { return count; }
    }
}
