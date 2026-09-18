package com.parkarsite.g1capture.storage;

import com.parkarsite.g1capture.g1.RemoteMedia;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

/** Tiny persistent dedup ledger plus recovery from per-file provenance sidecars. */
public final class FileImportLedger {
    private final File file;
    private final Set<String> imported = new HashSet<>();

    public FileImportLedger(File root) throws Exception {
        if (!root.exists() && !root.mkdirs()) throw new IllegalStateException("Cannot create " + root);
        this.file = new File(root, ".imported.tsv");
        if (file.exists()) imported.addAll(Files.readAllLines(file.toPath(), StandardCharsets.UTF_8));
        recoverCompletedFiles(root);
    }

    public synchronized boolean contains(RemoteMedia media) {
        return imported.contains(media.identityKey());
    }

    public synchronized void markImported(RemoteMedia media) throws Exception {
        persistKey(media.identityKey());
    }

    private void recoverCompletedFiles(File root) throws Exception {
        File[] dayDirs = root.listFiles(File::isDirectory);
        if (dayDirs == null) return;
        for (File day : dayDirs) {
            File[] sidecars = day.listFiles((d, n) -> n.startsWith(".") && n.endsWith(".source"));
            if (sidecars == null) continue;
            for (File sidecar : sidecars) {
                String n = sidecar.getName();
                String finalName = n.substring(1, n.length() - ".source".length());
                File finalFile = new File(day, finalName);
                if (!finalFile.isFile() || finalFile.length() <= 0) {
                    sidecar.delete();
                    continue;
                }
                String key = Files.readString(sidecar.toPath(), StandardCharsets.UTF_8).trim();
                if (!key.isEmpty()) persistKey(key);
            }
        }
    }

    private synchronized void persistKey(String key) throws Exception {
        if (!imported.add(key)) return;
        try (FileWriter out = new FileWriter(file, StandardCharsets.UTF_8, true)) {
            out.write(key);
            out.write(System.lineSeparator());
            out.flush();
        }
    }
}
