package com.parkarsite.g1.workflow;

import java.util.*;

public final class ControllerSelfTest {
    private static int checks;
    private static void must(boolean pass, String why) {
        checks++;
        if(!pass) throw new AssertionError(why);
    }
    private static final class Fake implements SharedTransferContracts.BleSession,
        SharedTransferContracts.PeerObserver, SharedTransferContracts.Lifecycle,
        SharedTransferContracts.DeadlineScheduler {
        int begin, inventory, exits, teardown, sourceCalls, storeCalls, nextTimerId;
        boolean absent=true, failTeardown, failStart, failSchedule, failExit;
        List<SharedTransferContracts.PeerEvidence> peers=List.of();
        final Map<Integer,Runnable> callbacks=new HashMap<>();
        public void beginObservation() { begin++;if(failStart)throw new IllegalStateException("synthetic"); }
        public void requestInventory() { inventory++; }
        public void requestExit() { exits++;if(failExit)throw new IllegalStateException("synthetic"); }
        public List<SharedTransferContracts.PeerEvidence> snapshot() { return peers; }
        public void requestTeardown() { teardown++;if(failTeardown)throw new IllegalStateException("synthetic"); }
        public boolean groupAbsent() { return absent; }
        public SharedTransferContracts.Cancellation arm(long delay,Runnable callback) {
            must(delay>0,"timeout must be bounded");
            if(failSchedule)throw new IllegalStateException("synthetic");
            int id=++nextTimerId;
            callbacks.put(id,callback);
            return ()->callbacks.remove(id);
        }
        void fire(int id) { Runnable r=callbacks.remove(id);if(r!=null)r.run(); }
        SharedObservationController controller() { return new SharedObservationController(this,this,this,this); }
        SharedTransferContracts.OneShotByteSource source() { return (item,output)->sourceCalls++; }
        SharedTransferContracts.DurableStore store() { return new SharedTransferContracts.DurableStore() {
            public void prepare(SharedTransferContracts.MediaDescriptor item) {storeCalls++;}
            public void markFetchMayHaveStarted(SharedTransferContracts.MediaDescriptor item) {storeCalls++;}
            public void commitVerifiedBytes(SharedTransferContracts.MediaDescriptor item) {storeCalls++;}
            public boolean committedAndIntact(String opaqueId) {storeCalls++;return false;}
        }; }
    }
    private static SharedTransferContracts.MediaDescriptor media() {
        return new SharedTransferContracts.MediaDescriptor(
            "opaque",SharedTransferContracts.MediaKind.JPG,7L,0L);
    }
    public static void main(String[] args) {
        Fake a=new Fake();var c=a.controller();c.startObservation();
        must(c.stage()==SharedTransferContracts.Stage.OBSERVING,"active observation");
        must(a.begin==1&&a.inventory==1,"observation only");
        a.peers=List.of(new SharedTransferContracts.PeerEvidence("opaque",true,true,true,true));
        c.refreshPeerCandidates();
        must(c.candidateObserved(),"candidate classification");
        must(!c.physicalConnectAuthorized()&&!c.remoteMediaGetAuthorized(),"physical capabilities disabled");
        var denial=c.considerMediaImport(media(),
            SharedTransferContracts.PreservationVerdict.PHYSICALLY_PROVEN_SAFE,a.source(),a.store());
        must(denial==SharedObservationController.MediaDecision.BLOCKED_NO_PRESERVING_TRANSFER_PROOF,
            "caller-supplied physical verdict cannot authorize GET");
        must(a.sourceCalls==0&&a.storeCalls==0,"no read or storage side effects");
        c.onExitEvidence(true,true);
        must(c.stage()==SharedTransferContracts.Stage.OBSERVING,"pre-exit evidence ignored");
        c.finishObservation();c.finishObservation();
        must(a.exits==1&&a.teardown==1,"idempotent teardown");
        c.onExitEvidence(true,true);
        must(c.stage()==SharedTransferContracts.Stage.CLOSED
            &&c.result()==SharedObservationController.Result.CLOSED_OBSERVATION_ONLY,"closed observation only");
        c.refreshPeerCandidates();a.fire(1);a.fire(2);
        must(c.stage()==SharedTransferContracts.Stage.CLOSED,"late callbacks ignored");
        must(c.considerMediaImport(media(),SharedTransferContracts.PreservationVerdict.UNKNOWN,
            a.source(),a.store())==SharedObservationController.MediaDecision.BLOCKED_NOT_OBSERVING,
            "late source blocked");
        Fake ambiguous=new Fake();var d=ambiguous.controller();d.startObservation();
        ambiguous.peers=List.of(
            new SharedTransferContracts.PeerEvidence("a",true,true,true,true),
            new SharedTransferContracts.PeerEvidence("b",true,true,true,true));
        d.refreshPeerCandidates();must(!d.candidateObserved(),"ambiguous peers rejected");
        must(!d.remoteMediaGetAuthorized(),"ambiguous no GET");
        d.finishObservation();d.onExitEvidence(true,true);
        must(d.stage()==SharedTransferContracts.Stage.CLOSED,"closed without transfer");
        Fake timed=new Fake();var e=timed.controller();e.startObservation();timed.fire(1);
        must(timed.exits==1&&timed.teardown==1,"timeout initiates cleanup");
        e.onExitEvidence(true,true);
        must(e.stage()==SharedTransferContracts.Stage.FAILED
            &&e.result()==SharedObservationController.Result.OBSERVATION_TIMEOUT,
            "timed-out run never passes");
        Fake stuck=new Fake();var f=stuck.controller();f.startObservation();f.finishObservation();stuck.fire(2);
        must(f.stage()==SharedTransferContracts.Stage.FAILED
            &&f.result()==SharedObservationController.Result.EXIT_TIMEOUT,"independent exit deadline");
        f.onExitEvidence(true,true);
        must(f.stage()==SharedTransferContracts.Stage.FAILED,"late exit evidence ignored");
        Fake present=new Fake();present.absent=false;var g=present.controller();
        g.startObservation();g.finishObservation();g.onExitEvidence(true,true);
        must(g.stage()==SharedTransferContracts.Stage.FAILED
            &&g.result()==SharedObservationController.Result.EXIT_UNVERIFIED,"group must be absent");
        Fake missingAck=new Fake();var h=missingAck.controller();
        h.startObservation();h.finishObservation();h.onExitEvidence(true,false);
        must(h.stage()==SharedTransferContracts.Stage.FAILED,"missing post-exit frame");
        Fake teardown=new Fake();teardown.failTeardown=true;var i=teardown.controller();
        i.startObservation();i.finishObservation();
        must(i.stage()==SharedTransferContracts.Stage.FAILED
            &&i.result()==SharedObservationController.Result.TEARDOWN_FAILED,"teardown failed");
        Fake start=new Fake();start.failStart=true;var j=start.controller();j.startObservation();
        must(start.exits==1&&start.teardown==1,"partially started BLE triggers cleanup");
        j.onExitEvidence(true,true);
        must(j.stage()==SharedTransferContracts.Stage.FAILED
            &&j.result()==SharedObservationController.Result.START_FAILED,
            "start failure never passes");
        Fake schedule=new Fake();schedule.failSchedule=true;var k=schedule.controller();k.startObservation();
        must(k.stage()==SharedTransferContracts.Stage.FAILED
            &&schedule.teardown==1,"scheduler failure still tears down");
        Fake exitError=new Fake();exitError.failExit=true;var l=exitError.controller();
        l.startObservation();l.finishObservation();
        must(l.stage()==SharedTransferContracts.Stage.FAILED
            &&exitError.teardown==1,"BLE exit exception still tears down group");
        Fake shared=new Fake();var first=shared.controller();var second=shared.controller();
        first.startObservation();second.startObservation();first.finishObservation();
        must(shared.callbacks.containsKey(2)&&shared.callbacks.containsKey(3),
            "per-arm timers cannot cancel another controller");
        shared.fire(2);
        must(second.stage()==SharedTransferContracts.Stage.EXITING,
            "independent second controller deadline");
        first.onExitEvidence(true,true);second.onExitEvidence(true,true);
        must(first.stage()==SharedTransferContracts.Stage.CLOSED
            &&second.stage()==SharedTransferContracts.Stage.FAILED,"independent result retention");
        must(checks>=20,"sufficient assertions");
        System.out.println("V4-OBSERVATION-FAIL-CLOSED PASS ("+checks+" assertions)");
    }
}
