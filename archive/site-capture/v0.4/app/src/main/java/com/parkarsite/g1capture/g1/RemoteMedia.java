package com.parkarsite.g1capture.g1;

import java.util.Locale;

/** Metadata only. Real photo/video bytes must never be held in this record. */
public record RemoteMedia(String remoteId, String originalName, long captureEpochMs, long sizeBytes) {
    public String extension() {
        if (originalName == null) return ".bin";
        int dot = originalName.lastIndexOf('.');
        if (dot < 0 || dot == originalName.length() - 1) return ".bin";
        String ext = originalName.substring(dot).toLowerCase(Locale.ROOT);
        return switch (ext) {
            case ".jpg", ".jpeg", ".mp4", ".m4a", ".aac", ".wav", ".opus" -> ext;
            default -> ".bin";
        };
    }

    public String identityKey() {
        return safe(remoteId) + "\t" + safe(originalName) + "\t" + captureEpochMs + "\t" + sizeBytes;
    }

    private static String safe(String s) {
        return s == null ? "" : s.replace("\t", "_").replace("\r", "_").replace("\n", "_");
    }
}
