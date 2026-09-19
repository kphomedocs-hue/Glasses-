package com.parkarsite.g6a;

public record ImportRecord(
        String opaqueId,
        MediaKind kind,
        String localRelativePath,
        long byteCount,
        long firstSeenEpochMs,
        long completedEpochMs,
        String status) {
    public static final String COMMITTED = "COMMITTED";
}
