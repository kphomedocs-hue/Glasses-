package com.parkarsite.g1capture.selftest;

import com.parkarsite.g1capture.core.ProbeRunner;
import com.parkarsite.g1capture.g1.G1Transport;
import com.parkarsite.g1capture.g1.RemoteMedia;
import com.parkarsite.g1capture.research.HeyCyanCandidateProtocol;
import com.parkarsite.g1capture.storage.FileImportLedger;
import com.parkarsite.g1capture.storage.FileMediaArchive;
import com.parkarsite.g1capture.storage.NumberingPolicy;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

public final class CoreSelfTest {
    public static void main(String[] args) throws Exception {
        testNumbering();
        testMixedChronologyAndDedup();
        testSameTimestampDeterminism();
        testDuplicateEntrySameListing();
        testInterruptedTransferCleanup();
        testSizeMismatchCleanup();
        testLargeStreamingTransfer();
        testExtensionSanitizing();
        testOpusSupport();
        testMidnightRollover();
        testCrashWindowRecovery();
        testCandidateCrcAndFraming();
        testCandidateFragmentation();
        System.out.println("ALL CORE SELF-TESTS PASSED");
    }

    static void testNumbering() {
        eq(4, NumberingPolicy.nextNumber(List.of("0001.jpg", "0002.mp4", "0003.m4a", ".0003.m4a.source", "0004.mp4.part")), "mixed counter");
        eq("0100.jpg", NumberingPolicy.fileName(100, ".JPG"), "filename");
    }

    static void testMixedChronologyAndDedup() throws Exception {
        File root = Files.createTempDirectory("ksite-mixed").toFile();
        List<RemoteMedia> items = List.of(
                new RemoteMedia("v", "VID.MP4", 3000, 3),
                new RemoteMedia("p", "IMG.JPG", 1000, 1),
                new RemoteMedia("a", "AUD.M4A", 2000, 2));
        G1Transport t = bytesTransport(items, false, -1, false);
        ProbeRunner r = new ProbeRunner(t, new FileMediaArchive(root, ZoneId.of("UTC")), new FileImportLedger(root));
        List<File> first = r.run();
        eq(3, first.size(), "first import count");
        eq("0001.jpg", first.get(0).getName(), "chronology photo");
        eq("0002.m4a", first.get(1).getName(), "chronology audio");
        eq("0003.mp4", first.get(2).getName(), "chronology video");
        List<File> second = new ProbeRunner(bytesTransport(items, false, -1, false), new FileMediaArchive(root, ZoneId.of("UTC")), new FileImportLedger(root)).run();
        eq(0, second.size(), "dedup after restart");
    }

    static void testSameTimestampDeterminism() throws Exception {
        File root = Files.createTempDirectory("ksite-tie").toFile();
        long t = 1000;
        List<RemoteMedia> items = List.of(
                new RemoteMedia("z", "Z.MP4", t, 1),
                new RemoteMedia("a", "A.JPG", t, 1));
        List<File> files = new ProbeRunner(bytesTransport(items, false, -1, false), new FileMediaArchive(root, ZoneId.of("UTC")), new FileImportLedger(root)).run();
        eq("0001.jpg", files.get(0).getName(), "tie breaker remote id");
        eq("0002.mp4", files.get(1).getName(), "tie breaker remote id 2");
    }

    static void testDuplicateEntrySameListing() throws Exception {
        File root = Files.createTempDirectory("ksite-dupe").toFile();
        RemoteMedia m = new RemoteMedia("same", "X.JPG", 1000, 1);
        List<File> files = new ProbeRunner(bytesTransport(List.of(m, m), false, -1, false), new FileMediaArchive(root, ZoneId.of("UTC")), new FileImportLedger(root)).run();
        eq(1, files.size(), "duplicate listing imported once");
    }

    static void testInterruptedTransferCleanup() throws Exception {
        File root = Files.createTempDirectory("ksite-fail").toFile();
        RemoteMedia item = new RemoteMedia("bad", "BAD.MP4", 1000, 100);
        G1Transport t = bytesTransport(List.of(item), true, 10, false);
        expectFailure(() -> new FileMediaArchive(root, ZoneId.of("UTC")).save(item, t), "Expected transfer failure");
        assertNoPublishedFiles(root, "no partial/final after failure");
    }

    static void testSizeMismatchCleanup() throws Exception {
        File root = Files.createTempDirectory("ksite-size").toFile();
        RemoteMedia item = new RemoteMedia("short", "BAD.JPG", 1000, 20);
        G1Transport t = bytesTransport(List.of(item), false, -1, true);
        expectFailure(() -> new FileMediaArchive(root, ZoneId.of("UTC")).save(item, t), "Expected size mismatch");
        assertNoPublishedFiles(root, "no publish after size mismatch");
    }

    static void testLargeStreamingTransfer() throws Exception {
        final long size = 32L * 1024 * 1024;
        File root = Files.createTempDirectory("ksite-large").toFile();
        RemoteMedia item = new RemoteMedia("large", "BIG.MP4", 1000, size);
        G1Transport t = new G1Transport() {
            public void connect() {}
            public void enterTransferMode() {}
            public List<RemoteMedia> listMedia() { return List.of(item); }
            public void download(RemoteMedia m, OutputStream out) throws Exception {
                byte[] chunk = new byte[64 * 1024];
                long remaining = size;
                while (remaining > 0) {
                    int n = (int)Math.min(chunk.length, remaining);
                    out.write(chunk, 0, n);
                    remaining -= n;
                }
            }
            public void disconnect() {}
        };
        File f = new FileMediaArchive(root, ZoneId.of("UTC")).save(item, t);
        eq(size, f.length(), "large stream size");
    }

    static void testExtensionSanitizing() {
        eq(".jpg", new RemoteMedia("1", "X.JPG", 1, 1).extension(), "jpg ext");
        eq(".bin", new RemoteMedia("2", "X.apk", 1, 1).extension(), "unsafe ext");
        eq(".bin", new RemoteMedia("3", "NOEXT", 1, 1).extension(), "missing ext");
    }

    static void testOpusSupport() {
        eq(".opus", new RemoteMedia("4", "REC.OPUS", 1, 1).extension(), "opus ext");
    }

    static void testMidnightRollover() throws Exception {
        ZoneId z = ZoneId.of("UTC");
        long before = Instant.parse("2026-09-18T23:59:59Z").toEpochMilli();
        long after = Instant.parse("2026-09-19T00:00:01Z").toEpochMilli();
        eq("2026-09-18", NumberingPolicy.dailyFolder(before, z), "day before midnight");
        eq("2026-09-19", NumberingPolicy.dailyFolder(after, z), "day after midnight");
    }

    static void testCrashWindowRecovery() throws Exception {
        File root = Files.createTempDirectory("ksite-crash").toFile();
        RemoteMedia item = new RemoteMedia("crash", "C.JPG", 1000, 4);
        FileMediaArchive archive = new FileMediaArchive(root, ZoneId.of("UTC"));
        File saved = archive.save(item, bytesTransport(List.of(item), false, -1, false));
        isTrue(saved.isFile(), "media published before simulated crash");
        FileImportLedger recovered = new FileImportLedger(root);
        isTrue(recovered.contains(item), "ledger recovered from sidecar");
        List<File> again = new ProbeRunner(bytesTransport(List.of(item), false, -1, false), new FileMediaArchive(root, ZoneId.of("UTC")), recovered).run();
        eq(0, again.size(), "no duplicate after crash window");
    }

    static void testCandidateCrcAndFraming() {
        byte[] photo = new byte[]{0x02,0x01,0x01};
        eq(0x5010, HeyCyanCandidateProtocol.crc16Modbus(photo), "CRC independently calculated");
        byte[] framed = HeyCyanCandidateProtocol.frame(HeyCyanCandidateProtocol.CMD_GLASSES_CONTROL, photo);
        byte[] expected = new byte[]{(byte)0xBC,0x41,0x03,0x00,0x10,0x50,0x02,0x01,0x01};
        isTrue(Arrays.equals(expected, framed), "candidate frame layout");
        eq(0x53D0, HeyCyanCandidateProtocol.crc16Modbus(HeyCyanCandidateProtocol.candidateTransferPayload()), "transfer payload CRC");
    }

    static void testCandidateFragmentation() {
        byte[] data = new byte[600];
        List<byte[]> chunks = HeyCyanCandidateProtocol.fragment(data);
        eq(3, chunks.size(), "fragment count");
        eq(244, chunks.get(0).length, "fragment 1");
        eq(244, chunks.get(1).length, "fragment 2");
        eq(112, chunks.get(2).length, "fragment 3");
    }

    static G1Transport bytesTransport(List<RemoteMedia> items, boolean fail, int failAfter, boolean shortWrite) {
        return new G1Transport() {
            public void connect() {}
            public void enterTransferMode() {}
            public List<RemoteMedia> listMedia() { return items; }
            public void download(RemoteMedia m, OutputStream out) throws Exception {
                long count = Math.max(0, m.sizeBytes() - (shortWrite ? 1 : 0));
                for (long i = 0; i < count; i++) {
                    if (fail && i == failAfter) throw new java.io.IOException("simulated disconnect");
                    out.write((int)i & 0xff);
                }
            }
            public void disconnect() {}
        };
    }

    static void assertNoPublishedFiles(File root, String what) {
        File[] dirs = root.listFiles(File::isDirectory);
        if (dirs == null) return;
        int count = 0;
        for (File d : dirs) {
            File[] fs = d.listFiles((x,n) -> n.endsWith(".part") || n.matches("\\d+\\..+") || n.endsWith(".source"));
            count += fs == null ? 0 : fs.length;
        }
        eq(0, count, what);
    }

    interface Throwing { void run() throws Exception; }
    static void expectFailure(Throwing x, String message) throws Exception {
        try { x.run(); } catch (Exception expected) { return; }
        throw new AssertionError(message);
    }
    static void isTrue(boolean actual, String what) { if (!actual) throw new AssertionError(what); }
    static void eq(Object expected, Object actual, String what) {
        if (!expected.equals(actual)) throw new AssertionError(what + ": expected=" + expected + " actual=" + actual);
    }
}
