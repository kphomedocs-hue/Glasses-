package com.parkarsite.g1.ledger;

import java.util.Objects;

/**
 * Stable remote identity used for deduplication.
 * No local filename is part of identity.
 */
public record MediaIdentity(
        String remoteId,
        String originalName,
        long captureEpochMs,
        long declaredSizeBytes
) {
    public MediaIdentity {
        remoteId = safe(remoteId);
        originalName = safe(originalName);
        if (captureEpochMs < 0) throw new IllegalArgumentException("captureEpochMs < 0");
        if (declaredSizeBytes < -1) throw new IllegalArgumentException("declaredSizeBytes < -1");
    }

    public String stableKey() {
        return escape(remoteId) + "\t"
                + escape(originalName) + "\t"
                + captureEpochMs + "\t"
                + declaredSizeBytes;
    }

    private static String safe(String value) {
        return Objects.requireNonNullElse(value, "");
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\")
                .replace("\t", "\\t")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}
