package com.parkarsite.g1.ledger;

/** Central state-transition policy independent of Room/Android. */
public final class SyncTransitions {
    private SyncTransitions() {}

    public static boolean canTransition(SyncState from, SyncState to) {
        if (from == null || to == null) return false;
        return switch (from) {
            case PENDING -> to == SyncState.DOWNLOADING || to == SyncState.FAILED;
            case DOWNLOADING -> to == SyncState.COMPLETE || to == SyncState.FAILED;
            case FAILED -> to == SyncState.DOWNLOADING || to == SyncState.FAILED;
            case COMPLETE -> to == SyncState.COMPLETE;
        };
    }

    public static void requireTransition(SyncState from, SyncState to) {
        if (!canTransition(from, to)) {
            throw new IllegalStateException("Invalid sync transition: " + from + " -> " + to);
        }
    }
}
