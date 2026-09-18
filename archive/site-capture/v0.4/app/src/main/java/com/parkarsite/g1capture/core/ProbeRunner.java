package com.parkarsite.g1capture.core;

import com.parkarsite.g1capture.g1.G1Transport;
import com.parkarsite.g1capture.g1.RemoteMedia;
import com.parkarsite.g1capture.storage.FileImportLedger;
import com.parkarsite.g1capture.storage.FileMediaArchive;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ProbeRunner {
    private final G1Transport transport;
    private final FileMediaArchive archive;
    private final FileImportLedger ledger;

    public ProbeRunner(G1Transport transport, FileMediaArchive archive, FileImportLedger ledger) {
        this.transport = transport;
        this.archive = archive;
        this.ledger = ledger;
    }

    public List<File> run() throws Exception {
        transport.connect();
        try {
            transport.enterTransferMode();
            List<RemoteMedia> media = new ArrayList<>(transport.listMedia());
            media.sort(Comparator.comparingLong(RemoteMedia::captureEpochMs)
                    .thenComparing(m -> m.remoteId() == null ? "" : m.remoteId())
                    .thenComparing(m -> m.originalName() == null ? "" : m.originalName()));
            List<File> saved = new ArrayList<>();
            for (RemoteMedia item : media) {
                if (ledger.contains(item)) continue;
                File file = archive.save(item, transport);
                ledger.markImported(item);
                saved.add(file);
            }
            return saved;
        } finally {
            transport.disconnect();
        }
    }
}
