package com.parkarsite.g1.ledger;

public final class SyncLedgerSelfTest {
    public static void main(String[] args) {
        truth(SyncTransitions.canTransition(SyncState.PENDING, SyncState.DOWNLOADING), "pending->downloading");
        truth(SyncTransitions.canTransition(SyncState.DOWNLOADING, SyncState.COMPLETE), "downloading->complete");
        truth(SyncTransitions.canTransition(SyncState.DOWNLOADING, SyncState.FAILED), "downloading->failed");
        truth(SyncTransitions.canTransition(SyncState.FAILED, SyncState.DOWNLOADING), "failed retry");
        truth(!SyncTransitions.canTransition(SyncState.COMPLETE, SyncState.DOWNLOADING), "complete terminal");
        truth(!SyncTransitions.canTransition(SyncState.PENDING, SyncState.COMPLETE), "no skip to complete");

        MediaIdentity a = new MediaIdentity("remote-1", "IMG_1.JPG", 1000L, 123L);
        MediaIdentity b = new MediaIdentity("remote-1", "IMG_1.JPG", 1000L, 123L);
        eq(a.stableKey(), b.stableKey(), "stable identity");

        MediaIdentity escaped = new MediaIdentity("x\ty", "a\nb.jpg", 1L, -1L);
        truth(!escaped.stableKey().contains("\na"), "line break escaped");

        System.out.println("PASS sync-ledger state model");
    }

    private static void truth(boolean value, String what) {
        if (!value) throw new AssertionError(what);
    }

    private static void eq(Object expected, Object actual, String what) {
        if (!expected.equals(actual)) {
            throw new AssertionError(what + ": expected=" + expected + " actual=" + actual);
        }
    }
}
