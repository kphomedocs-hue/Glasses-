package com.parkarsite.g1.sitecapture;

import com.parkarsite.g1.workflow.MixedMediaRecoveryStore;
import java.nio.file.Path;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

/**
 * Production-oriented, OFFLINE/READ-ONLY K Site Capture backend.
 *
 * Its public API intentionally has no BLE, Wi-Fi, HTTP, Source, media GET,
 * capture, automatic retry, or test-fixture authorization method.
 * This is not yet a complete Android application.
 */
public final class SiteCaptureOfflineCore {
    public enum Mode { LOCAL_ARCHIVE_ONLY, REVIEW_REQUIRED }
    public enum TransferBlock {
        ORIGINALS_NOT_PROVEN_TO_REMAIN_ON_GLASSES,
        PEER_AND_BOUND_NETWORK_NOT_AUTHENTICATED,
        LIVE_ADAPTER_NOT_RELEASE_QUALIFIED
    }
    public record Overview(
        Mode mode, int verifiedFiles, int remotelyUncertain,
        int manualReview, int preparedWithoutFetch, int locallyRecovered,
        String safeStatusCode, TransferBlock transferBlock
    ) {
        public Overview { Objects.requireNonNull(mode); Objects.requireNonNull(safeStatusCode);
            Objects.requireNonNull(transferBlock); }
    }
    private final MixedMediaRecoveryStore archive;

    /** Caller supplies Android app-private root; no system/network capability is accepted. */
    public SiteCaptureOfflineCore(Path appPrivateRoot, ZoneId localZone) throws Exception {
        Objects.requireNonNull(appPrivateRoot);
        this.archive = new MixedMediaRecoveryStore(appPrivateRoot.resolve("KSiteCapture"),localZone);
    }
    public boolean canSyncFromGlasses() { return false; }
    public boolean canConnectToGlasses() { return false; }
    public boolean canReadRemoteMedia() { return false; }
    public TransferBlock whyCannotSync() {
        return TransferBlock.ORIGINALS_NOT_PROVEN_TO_REMAIN_ON_GLASSES;
    }

    /**
     * Recover only existing local journal/files/receipts. Never calls a media
     * Source. Even an incomplete local import remains remote-uncertain.
     * This operation does not clear, discard, retry or overwrite local media.
     */
    public synchronized Overview inspectAndRecoverLocalOnly() {
        try {
            List<MixedMediaRecoveryStore.Result> events=archive.recover();
            int complete=0,uncertain=0,review=0,prepared=0,recovered=0;
            for(MixedMediaRecoveryStore.Result event:events) {
                switch(event.outcome()) {
                    case COMPLETE -> complete++;
                    case REMOTE_UNCERTAIN -> uncertain++;
                    case MANUAL_REVIEW -> review++;
                    case NOT_FETCHED -> prepared++;
                    case LOCAL_RECOVERED -> { complete++;recovered++; }
                }
            }
            Mode mode=review>0||uncertain>0 ? Mode.REVIEW_REQUIRED:Mode.LOCAL_ARCHIVE_ONLY;
            return new Overview(mode,complete,uncertain,review,prepared,recovered,
                mode==Mode.REVIEW_REQUIRED ? "LOCAL_REVIEW_NO_REMOTE_RETRY":"LOCAL_ONLY_SAFE",
                whyCannotSync());
        } catch(Exception cannotSafelyClassify) {
            // Do not silently call a corrupted ledger empty or dedup complete.
            return new Overview(Mode.REVIEW_REQUIRED,0,0,1,0,0,
                "LOCAL_LEDGER_UNVERIFIED",whyCannotSync());
        }
    }
}
