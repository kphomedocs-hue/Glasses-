package com.parkarsite.g1.workflow;

import java.util.List;
import java.util.Objects;

/**
 * V4 pure-Java observation and teardown coordinator.
 * NOT an Android shipping adapter; cannot read remote media.
 * Synthetic peer/group/IP or preservation assertions cannot enable GET.
 */
public final class SharedObservationController {
    public enum Result { NONE, CLOSED_OBSERVATION_ONLY, START_FAILED, OBSERVATION_TIMEOUT,
        EXIT_TIMEOUT, EXIT_UNVERIFIED, TEARDOWN_FAILED }
    public enum MediaDecision { BLOCKED_NO_PRESERVING_TRANSFER_PROOF, BLOCKED_NOT_OBSERVING }
    private static final long OBSERVATION_LIMIT_MS = 16_000L;
    private static final long EXIT_LIMIT_MS = 5_000L;

    private final SharedTransferContracts.BleSession ble;
    private final SharedTransferContracts.PeerObserver peer;
    private final SharedTransferContracts.Lifecycle lifecycle;
    private final SharedTransferContracts.DeadlineScheduler deadlines;
    private SharedTransferContracts.Stage stage=SharedTransferContracts.Stage.IDLE;
    private Result result=Result.NONE;
    private boolean lifecycleStarted, exitRequested, candidateObserved;
    private SharedTransferContracts.Cancellation observeDeadline, exitDeadline;

    public SharedObservationController(SharedTransferContracts.BleSession ble,
        SharedTransferContracts.PeerObserver peer,
        SharedTransferContracts.Lifecycle lifecycle,
        SharedTransferContracts.DeadlineScheduler deadlines) {
        this.ble=Objects.requireNonNull(ble);
        this.peer=Objects.requireNonNull(peer);
        this.lifecycle=Objects.requireNonNull(lifecycle);
        this.deadlines=Objects.requireNonNull(deadlines);
    }

    public synchronized SharedTransferContracts.Stage stage() { return stage; }
    public synchronized Result result() { return result; }
    /** Observation is NOT identity authentication. Never use as permission to connect. */
    public synchronized boolean candidateObserved() { return candidateObserved; }
    public boolean physicalConnectAuthorized() { return false; }
    public boolean remoteMediaGetAuthorized() { return false; }

    public synchronized void startObservation() {
        if(stage!=SharedTransferContracts.Stage.IDLE)
            throw new IllegalStateException("Observation already started");
        stage=SharedTransferContracts.Stage.OBSERVING;
        try {
            observeDeadline=Objects.requireNonNull(deadlines.arm(OBSERVATION_LIMIT_MS,this::observationTimedOut));
            lifecycleStarted=true; // may have issued part of operation before throwing
            ble.beginObservation();
            ble.requestInventory();
        } catch (RuntimeException failure) {
            result=Result.START_FAILED;
            requestExitSafely();
        }
    }

    public synchronized void refreshPeerCandidates() {
        if(stage!=SharedTransferContracts.Stage.OBSERVING) return;
        try {
            List<SharedTransferContracts.PeerEvidence> snapshot=peer.snapshot();
            candidateObserved=snapshot!=null && snapshot.size()==1
                && snapshot.get(0)!=null && snapshot.get(0).complete();
        } catch (RuntimeException failure) {
            candidateObserved=false;
        }
    }

    /** Every call is blocked even with fabricated PHYSICALLY_PROVEN_SAFE evidence. */
    public synchronized MediaDecision considerMediaImport(
        SharedTransferContracts.MediaDescriptor item,
        SharedTransferContracts.PreservationVerdict preservation,
        SharedTransferContracts.OneShotByteSource source,
        SharedTransferContracts.DurableStore store) {
        Objects.requireNonNull(item); Objects.requireNonNull(preservation);
        Objects.requireNonNull(source); Objects.requireNonNull(store);
        if(stage!=SharedTransferContracts.Stage.OBSERVING)
            return MediaDecision.BLOCKED_NOT_OBSERVING;
        return MediaDecision.BLOCKED_NO_PRESERVING_TRANSFER_PROOF;
    }

    public synchronized void finishObservation() {
        if(stage==SharedTransferContracts.Stage.OBSERVING) requestExitSafely();
    }

    private synchronized void observationTimedOut() {
        if(stage!=SharedTransferContracts.Stage.OBSERVING) return;
        result=Result.OBSERVATION_TIMEOUT;
        requestExitSafely();
    }
    private synchronized void exitTimedOut() {
        if(stage!=SharedTransferContracts.Stage.EXITING) return;
        result=Result.EXIT_TIMEOUT;
        stage=SharedTransferContracts.Stage.FAILED;
        cancelExit();
    }
    private void cancelObservation() {
        var cancellation=observeDeadline;
        observeDeadline=null;
        if(cancellation!=null) try { cancellation.cancel(); }
        catch(RuntimeException ignored) { /* terminal state ignores late callbacks */ }
    }
    private void cancelExit() {
        var cancellation=exitDeadline;
        exitDeadline=null;
        if(cancellation!=null) try { cancellation.cancel(); }
        catch(RuntimeException ignored) { /* terminal state ignores late callbacks */ }
    }
    private void requestExitSafely() {
        if(stage!=SharedTransferContracts.Stage.OBSERVING || exitRequested) return;
        exitRequested=true;
        stage=SharedTransferContracts.Stage.EXITING;
        cancelObservation();
        boolean cleanupFailed=false;
        try { exitDeadline=Objects.requireNonNull(deadlines.arm(EXIT_LIMIT_MS,this::exitTimedOut)); }
        catch (RuntimeException failure) { cleanupFailed=true; }
        // Both cleanup paths are attempted regardless of any preceding failure.
        try { if(lifecycleStarted) ble.requestExit(); }
        catch (RuntimeException failure) { cleanupFailed=true; }
        try { lifecycle.requestTeardown(); }
        catch (RuntimeException failure) { cleanupFailed=true; }
        if(cleanupFailed) {
            cancelExit();
            result=Result.TEARDOWN_FAILED;
            stage=SharedTransferContracts.Stage.FAILED;
        }
    }

    /** BLE write, valid protocol event and actual group absence are distinct evidence. */
    public synchronized void onExitEvidence(boolean exitWriteSucceeded, boolean validPostExitInventory) {
        if(stage!=SharedTransferContracts.Stage.EXITING) return;
        cancelExit();
        try {
            if(!exitWriteSucceeded || !validPostExitInventory || !lifecycle.groupAbsent()) {
                result=Result.EXIT_UNVERIFIED;
                stage=SharedTransferContracts.Stage.FAILED;
            } else if(result!=Result.NONE) {
                stage=SharedTransferContracts.Stage.FAILED;
            } else {
                result=Result.CLOSED_OBSERVATION_ONLY;
                stage=SharedTransferContracts.Stage.CLOSED;
            }
        } catch (RuntimeException failure) {
            result=Result.EXIT_UNVERIFIED;
            stage=SharedTransferContracts.Stage.FAILED;
        }
    }
}
