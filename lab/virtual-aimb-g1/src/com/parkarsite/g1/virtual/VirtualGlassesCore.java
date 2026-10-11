package com.parkarsite.g1.virtual;

import com.parkarsite.g6a.identity.PeerIdentityPolicy;
import java.util.Collection;

/**
 * Transport-free fail-closed control flow for the VGL packs 1-3.
 * This class is the candidate reusable control core; NO Android adapter is wired.
 * Candidate peer names never constitute authentication or authorize connect.
 */
public final class VirtualGlassesCore {
    public interface BleTransportPort {
        void connect(); void subscribe(); void enter(); void exit();
    }
    public interface PeerDiscoveryPort { void discover(); }
    public interface LocalCatalogPort { /* Reserved; intentionally no read operation in V1. */ }
    public interface OneShotMediaPort { /* Blocked by remote-original preservation gate. */ }
    public interface DurableStorePort { /* Future RecoveryImportStore adapter, not integrated. */ }

    public enum Stage { NEW, CONNECTING, SUBSCRIBING, READY, ENTER_PENDING,
                        OBSERVING, EXIT_PENDING, FINISHED, STOPPED }
    public enum Outcome { NONE, SAFE_FINISH, NO_BONDED_TARGET, AMBIGUOUS_BONDED_TARGET,
                          INVALID_BONDED_TARGET, GATT_TIMEOUT, SUBSCRIPTION_FAILED,
                          ENTER_FAILED, OBSERVATION_TIMEOUT, EXIT_UNVERIFIED }
    private final VirtualClock clock;
    private final BleTransportPort ble;
    private final PeerDiscoveryPort peer;
    private Stage stage = Stage.NEW;
    private Outcome outcome = Outcome.NONE;
    private String bondedName, bondedAddress;
    private PeerIdentityPolicy.Report peerReport;
    private int acceptedEvents, inventoryEvents, ipEvents;
    private boolean exitIssued, entered, groupVerified, ipObserved;
    private int images, videos, recordings;
    private static final int MAX_EVENTS = 256;
    private static final long GATT_MS=5000, SUB_MS=4000, ENTER_MS=5000,
                              DISCOVERY_MS=16000, EXIT_MS=5000;

    public VirtualGlassesCore(VirtualClock clock, BleTransportPort ble, PeerDiscoveryPort peer) {
        this.clock = java.util.Objects.requireNonNull(clock);
        this.ble = java.util.Objects.requireNonNull(ble);
        this.peer = java.util.Objects.requireNonNull(peer);
    }
    public Stage stage(){return stage;}
    public Outcome outcome(){return outcome;}
    public PeerIdentityPolicy.Report peerReport(){return peerReport;}
    public int eventCount(){return acceptedEvents;}
    public int inventoryEvents(){return inventoryEvents;}
    public int ipEvents(){return ipEvents;}
    public int images(){return images;}
    public boolean groupVerified(){return groupVerified;}
    public boolean exitIssued(){return exitIssued;}
    public boolean connectAuthorized(){return false;} // research candidates are never authenticated
    public boolean mediaGetAuthorized(){return false;} // preserve source originals
    public boolean catalogEligible(){return stage==Stage.OBSERVING && groupVerified && ipObserved;}

    public void begin(int bondedMatches, String name, String bleAddress) {
        require(Stage.NEW);
        if (bondedMatches == 0) { stop(Outcome.NO_BONDED_TARGET); return; }
        if (bondedMatches != 1) { stop(Outcome.AMBIGUOUS_BONDED_TARGET); return; }
        if (!PeerIdentityPolicy.validBondedTargetName(name) ||
                PeerIdentityPolicy.assess(name,bleAddress,java.util.List.of()).verdict()
                    == PeerIdentityPolicy.Verdict.INVALID_BONDED_INPUT) {
            stop(Outcome.INVALID_BONDED_TARGET); return;
        }
        bondedName=name; bondedAddress=bleAddress;
        stage=Stage.CONNECTING;
        ble.connect();
        clock.deadline("gatt",GATT_MS,()->{if(stage==Stage.CONNECTING)stop(Outcome.GATT_TIMEOUT);});
    }
    public void onGatt(boolean success) {
        if(stage!=Stage.CONNECTING)return;
        clock.cancel("gatt");
        if(!success){stop(Outcome.GATT_TIMEOUT);return;}
        stage=Stage.SUBSCRIBING;
        ble.subscribe();
        clock.deadline("subscribe",SUB_MS,()->{if(stage==Stage.SUBSCRIBING)stop(Outcome.SUBSCRIPTION_FAILED);});
    }
    public void onSubscribed(boolean success) {
        if(stage!=Stage.SUBSCRIBING)return;
        clock.cancel("subscribe");
        if(!success){stop(Outcome.SUBSCRIPTION_FAILED);return;}
        stage=Stage.READY;
    }
    /** Explicit user/test action only; subscription alone performs no write. */
    public void armObservation() {
        require(Stage.READY);
        stage=Stage.ENTER_PENDING; entered=true;
        ble.enter();
        clock.deadline("enter",ENTER_MS,()->{
            if(stage==Stage.ENTER_PENDING){outcome=Outcome.ENTER_FAILED;startExit();}
        });
    }
    public void onEnterWrite(boolean success) {
        if(stage!=Stage.ENTER_PENDING)return;
        clock.cancel("enter");
        if(!success){outcome=Outcome.ENTER_FAILED;startExit();return;}
        stage=Stage.OBSERVING;
        peer.discover();
        clock.deadline("discovery",DISCOVERY_MS,()->{
            if(stage==Stage.OBSERVING){outcome=Outcome.OBSERVATION_TIMEOUT;startExit();}
        });
    }
    public void onPeerSnapshot(Collection<String> names) {
        // Snapshot before ENTER callback is not readiness.
        if(stage!=Stage.OBSERVING)return;
        peerReport=PeerIdentityPolicy.assess(bondedName,bondedAddress,names);
        groupVerified=false; ipObserved=false; // never carry attestation across changed peer snapshots
        // Intentionally NO call to connect, even when EXACT or sole candidate.
    }
    /** Called only by a future platform adapter after peer/group/network proof. */
    public void onGroupEvidence(boolean selectedPeerIsMember, boolean networkBound, boolean trustedSource) {
        if(stage==Stage.OBSERVING) {
            groupVerified = peerReport != null
                && peerReport.verdict() == PeerIdentityPolicy.Verdict.SINGLE_UNVERIFIED_CANDIDATE
                && selectedPeerIsMember && networkBound && trustedSource;
            ipObserved = false; // require a freshly correlated IP after each attestation
        }
    }
    public FrameEvidence.Parsed onFrame(byte[] raw) {
        FrameEvidence.Parsed evidence=FrameEvidence.parse(raw);
        if(!evidence.valid() || stage==Stage.STOPPED || stage==Stage.FINISHED)return evidence;
        if(evidence.kind()==FrameEvidence.Kind.EVENT_73) {
            acceptedEvents=Math.min(MAX_EVENTS,acceptedEvents+1);
            if(evidence.eventId()==1)inventoryEvents=Math.min(MAX_EVENTS,inventoryEvents+1);
            if(evidence.eventId()==8)ipEvents=Math.min(MAX_EVENTS,ipEvents+1);
        }
        // No state transition from a short 0x41 or unsolicited 0x73 alone.
        return evidence;
    }
    /** Typed synthetic event payload, not a claim about actual firmware byte layout. */
    public void onSyntheticInventory(int img,int vid,int rec) {
        if(img<0||vid<0||rec<0||img>65535||vid>65535||rec>65535)return;
        if(stage==Stage.OBSERVING){images=img;videos=vid;recordings=rec;}
    }
    public void onSyntheticIpEvent(boolean hasCorrelatedIp) {
        if(stage==Stage.OBSERVING && groupVerified && hasCorrelatedIp)ipObserved=true;
    }
    public void finishObservation() {
        if(stage==Stage.OBSERVING||stage==Stage.ENTER_PENDING)startExit();
    }
    private void startExit() {
        clock.cancel("enter");clock.cancel("discovery");
        if(!entered){stop(Outcome.SAFE_FINISH);return;}
        if(exitIssued)return;
        exitIssued=true;
        stage=Stage.EXIT_PENDING;
        ble.exit();
        clock.deadline("exit",EXIT_MS,()->{
            if(stage==Stage.EXIT_PENDING)stop(Outcome.EXIT_UNVERIFIED);
        });
    }
    public void onExitWrite(boolean success, boolean groupAbsent) {
        if(stage!=Stage.EXIT_PENDING)return;
        clock.cancel("exit");
        if(!success||!groupAbsent){stop(Outcome.EXIT_UNVERIFIED);return;}
        stage=Stage.FINISHED;
        if(outcome==Outcome.NONE)outcome=Outcome.SAFE_FINISH;
    }
    private void stop(Outcome why) {
        outcome=why;stage=Stage.STOPPED;
        clock.cancel("gatt");clock.cancel("subscribe");clock.cancel("enter");
        clock.cancel("discovery");clock.cancel("exit");
    }
    private void require(Stage expected) {
        if(stage!=expected)throw new IllegalStateException("Invalid state transition");
    }
}
