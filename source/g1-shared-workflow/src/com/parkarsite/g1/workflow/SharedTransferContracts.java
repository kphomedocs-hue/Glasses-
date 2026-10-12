package com.parkarsite.g1.workflow;

import java.io.OutputStream;
import java.util.List;
import java.util.Objects;

/** V4 Step 05: pure Java contracts only. No implementation performs I/O. */
public final class SharedTransferContracts {
    private SharedTransferContracts() {}
    public enum MediaKind { JPG, MP4, OPUS }
    public enum PreservationVerdict { UNKNOWN, LOSS_OBSERVED, SUPPORTED_BY_SOURCE, PHYSICALLY_PROVEN_SAFE }
    public enum Stage { IDLE, OBSERVING, CATALOG_REVIEW, IMPORT_BLOCKED, EXITING, CLOSED, FAILED }
    public record ClockSnapshot(long monotonicMs, long wallEpochMs) {}
    public record PeerEvidence(String opaquePeerId, boolean selectedGroupMember, boolean boundNetwork,
                               boolean correlatedIp, boolean unambiguous) {
        public PeerEvidence { Objects.requireNonNull(opaquePeerId); }
        public boolean complete() { return !opaquePeerId.isBlank() && selectedGroupMember && boundNetwork && correlatedIp && unambiguous; }
    }
    public record MediaDescriptor(String opaqueIdentity, MediaKind kind, long expectedBytes, long captureEpochMs) {
        public MediaDescriptor {
            Objects.requireNonNull(kind); Objects.requireNonNull(opaqueIdentity);
            if (opaqueIdentity.isBlank() || expectedBytes <= 0 || expectedBytes > 33_554_432L)
                throw new IllegalArgumentException("Invalid media descriptor");
        }
    }
    public record CatalogSnapshot(List<MediaDescriptor> media) {
        public CatalogSnapshot { media=List.copyOf(media); }
    }
    public interface Clock { ClockSnapshot now(); }
    public interface BleSession {
        void beginObservation();
        void requestInventory();
        void requestExit();
    }
    public interface PeerObserver { List<PeerEvidence> snapshot(); }
    public interface CatalogSource { CatalogSnapshot readCatalog() throws Exception; }
    /** NEVER call Source without a separately approved transfer authorization boundary. */
    public interface OneShotByteSource { void streamOnce(MediaDescriptor item, OutputStream sink) throws Exception; }
    public interface DurableStore {
        void prepare(MediaDescriptor item) throws Exception;
        void markFetchMayHaveStarted(MediaDescriptor item) throws Exception;
        void commitVerifiedBytes(MediaDescriptor item) throws Exception;
        boolean committedAndIntact(String opaqueIdentity) throws Exception;
    }
    public interface Lifecycle { void requestTeardown(); boolean groupAbsent(); }
    /** No credentials, URLs, raw filenames or real peer addresses may be present in these values. */
    public record Observation(ClockSnapshot at, Stage stage, String safeCode) {
        public Observation { Objects.requireNonNull(at);Objects.requireNonNull(stage);Objects.requireNonNull(safeCode); }
    }
}
