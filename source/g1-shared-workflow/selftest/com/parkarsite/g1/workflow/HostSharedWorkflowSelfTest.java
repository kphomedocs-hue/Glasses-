package com.parkarsite.g1.workflow;

import com.parkarsite.g6a.recovery.RecoveryImportStore;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

/**
 * HOST SELFTEST ONLY: fake bytes with a tested RecoveryImportStore.
 * No network, real device, raw peer identity, test APK or transferable credential.
 */
public final class HostSharedWorkflowSelfTest {
    private static int assertions;
    private static void check(boolean ok,String message) {
        assertions++;
        if(!ok)throw new AssertionError(message);
    }
    private static String opaque(String text) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
            .digest(text.getBytes(StandardCharsets.US_ASCII)));
    }
    private static SharedTransferContracts.MediaDescriptor jpg(String label,long bytes) throws Exception {
        return new SharedTransferContracts.MediaDescriptor(opaque(label),
            SharedTransferContracts.MediaKind.JPG,bytes,1_760_200_000_000L);
    }
    private static final byte[] MARKER={(byte)255,(byte)216,(byte)255,1,2,(byte)255,(byte)217};

    private static final class Fake implements SharedTransferContracts.BleSession,
            SharedTransferContracts.PeerObserver, SharedTransferContracts.Lifecycle,
            SharedTransferContracts.DeadlineScheduler, SharedTransferContracts.CatalogSource {
        int bleStarts,inventory,exit,teardown,readCatalog,sourceCalls;
        boolean groupAbsent=true;
        final List<SharedTransferContracts.CatalogSnapshot> catalogs=new ArrayList<>();
        int index;
        final List<Runnable> deadlines=new ArrayList<>();
        public void beginObservation(){bleStarts++;}
        public void requestInventory(){inventory++;}
        public void requestExit(){exit++;}
        public void requestTeardown(){teardown++;}
        public boolean groupAbsent(){return groupAbsent;}
        public List<SharedTransferContracts.PeerEvidence> snapshot(){
            return List.of(new SharedTransferContracts.PeerEvidence(
                "opaque-test-peer",true,true,true,true));
        }
        public SharedTransferContracts.Cancellation arm(long ms,Runnable callback){
            check(ms>0 && ms<=16_000L,"deadline bounded");
            deadlines.add(callback);
            return ()->deadlines.remove(callback);
        }
        public SharedTransferContracts.CatalogSnapshot readCatalog(){
            readCatalog++;
            if(index>=catalogs.size())throw new IllegalArgumentException("No fake catalog");
            return catalogs.get(index++);
        }
        SharedObservationController observation(){
            var obs=new SharedObservationController(this,this,this,this);
            obs.startObservation();
            obs.refreshPeerCandidates();
            return obs;
        }
    }
    private static final class JournalAdapter implements SharedTransferCoordinator.DurableOneShotImport {
        final RecoveryImportStore store;
        JournalAdapter(Path root) throws Exception {
            this.store=new RecoveryImportStore(root,ZoneId.of("Asia/Kolkata"));
        }
        public boolean committedAndIntact(String id) throws Exception {
            return store.isCommittedAndIntact(id);
        }
        public void importOnce(SharedTransferContracts.MediaDescriptor item,
                               SharedTransferContracts.OneShotByteSource source) throws Exception {
            if(item.kind()!=SharedTransferContracts.MediaKind.JPG)
                throw new IOException("Only JPG currently supported");
            var ticket=store.prepare(item.opaqueIdentity(),item.captureEpochMs());
            if(ticket.state()!=RecoveryImportStore.State.PREPARED)
                throw new IOException("Non-prepared existing ticket; no retry");
            store.execute(ticket,item.expectedBytes(),
                out->source.streamOnce(item,out),barrier->{});
        }
    }
    private static Fake fixture(SharedTransferContracts.MediaDescriptor old,
                                SharedTransferContracts.MediaDescriptor added) {
        var fake=new Fake();
        fake.catalogs.add(new SharedTransferContracts.CatalogSnapshot(List.of(old)));
        fake.catalogs.add(new SharedTransferContracts.CatalogSnapshot(List.of(old,added)));
        return fake;
    }

    public static void main(String[] args) throws Exception {
        Path root=Files.createTempDirectory("v4-host-shared-");
        var old=jpg("V4_ONLY_SYNTHETIC_BASELINE",MARKER.length);
        var added=jpg("V4_ONLY_SYNTHETIC_NEW",MARKER.length);
        var a=fixture(old,added);
        var obs=a.observation();
        var workflow=new SharedTransferCoordinator(obs,a,()->true); // test-only package-private DI
        var journal=new JournalAdapter(root);
        check(workflow.observeBaseline(1)==SharedTransferCoordinator.Verdict.BASELINE_ACCEPTED,
            "baseline must be accepted");
        check(workflow.observeSingleNewJpg(2)==SharedTransferCoordinator.Verdict.SINGLE_JPG_SELECTED,
            "exact delta selected");
        var source=(SharedTransferContracts.OneShotByteSource)(item,out)->{
            a.sourceCalls++;
            check(item.opaqueIdentity().equals(added.opaqueIdentity()),"only new opaque item");
            Path tx=root.resolve(".transactions").resolve(item.opaqueIdentity()+".txn");
            check(Files.readString(tx).contains("FETCH_MAY_HAVE_STARTED"),
                "durable fetch marker predates source invocation");
            out.write(MARKER);
        };
        check(workflow.importSyntheticFixture(source,journal)
                ==SharedTransferCoordinator.Verdict.SYNTHETIC_RECEIPT_COMMITTED,
                "validated synthetic receipt");
        check(a.sourceCalls==1,"exactly one fixture byte read");
        check(a.exit==1 && a.teardown==1,"cleanup requested once");
        check(obs.stage()==SharedTransferContracts.Stage.EXITING,"verified exit not yet observed");
        obs.onExitEvidence(true,true);
        check(workflow.completeAfterExit()==SharedTransferCoordinator.Verdict.SYNTHETIC_CLOSED,
            "source to store to exit completed");
        check(journal.committedAndIntact(added.opaqueIdentity()),"disk file+receipt intact");
        var reopened=new JournalAdapter(root);
        check(reopened.committedAndIntact(added.opaqueIdentity()),"reopen checks durable receipt");
        check(reopened.store.recover().stream().anyMatch(x->
            x.opaqueId().equals(added.opaqueIdentity())
                &&x.disposition()==RecoveryImportStore.Disposition.COMPLETE),
            "restart local-only recovery");
        check(workflow.importSyntheticFixture(source,journal)
                ==SharedTransferCoordinator.Verdict.BLOCKED_REPLAY,"no second import");
        check(a.sourceCalls==1,"no duplicate source read");
        check(!workflow.remoteMediaGetAuthorized()&&!workflow.physicalConnectAuthorized(),
            "public real authorization remains false");

        // Same class via PUBLIC production constructor. Even with a fabricated trusted peer,
        // a transfer-source callback must NEVER be invoked.
        Path deniedRoot=Files.createTempDirectory("v4-readonly-");
        var b=fixture(old,added);
        var prodObs=b.observation();
        var production=new SharedTransferCoordinator(prodObs,b);
        var productionJournal=new JournalAdapter(deniedRoot);
        check(production.observeBaseline(1)==SharedTransferCoordinator.Verdict.BASELINE_ACCEPTED,
            "production can observe baseline");
        check(production.observeSingleNewJpg(2)==SharedTransferCoordinator.Verdict.SINGLE_JPG_SELECTED,
            "production can check synthetic delta");
        var deniedSource=(SharedTransferContracts.OneShotByteSource)(item,out)->{
            b.sourceCalls++;
            throw new AssertionError("production must not read");
        };
        check(production.importSyntheticFixture(deniedSource,productionJournal)
                ==SharedTransferCoordinator.Verdict.BLOCKED_PRESERVATION,
            "production constructor blocks even perfectly fabricated evidence");
        check(b.sourceCalls==0 && b.exit==1 && b.teardown==1,
            "denied transfer causes cleanup without any bytes");
        check(!Files.exists(deniedRoot.resolve(".transactions").resolve(added.opaqueIdentity()+".txn")),
            "denied production flow did not even prepare a ledger");

        // Source interrupted after fetch marker. Durable state stays uncertain and no retry.
        Path uncertainRoot=Files.createTempDirectory("v4-uncertain-");
        var c=fixture(old,added);
        var uncertain=new SharedTransferCoordinator(c.observation(),c,()->true);
        var uncertainJournal=new JournalAdapter(uncertainRoot);
        check(uncertain.observeBaseline(1)==SharedTransferCoordinator.Verdict.BASELINE_ACCEPTED,
            "failure scenario baseline");
        check(uncertain.observeSingleNewJpg(2)==SharedTransferCoordinator.Verdict.SINGLE_JPG_SELECTED,
            "failure scenario delta");
        check(uncertain.importSyntheticFixture((item,out)->{
            c.sourceCalls++;
            throw new IOException("synthetic incomplete read");
        },uncertainJournal)==SharedTransferCoordinator.Verdict.REMOTE_STATE_UNCERTAIN,
            "incomplete read stays uncertain");
        check(c.sourceCalls==1&&c.exit==1&&c.teardown==1,
            "failed read invokes cleanup but no retries");
        check(uncertainJournal.store.recover().stream().anyMatch(x->
            x.opaqueId().equals(added.opaqueIdentity())
                &&x.disposition()==RecoveryImportStore.Disposition.REMOTE_UNCERTAIN),
            "recovery forbids source retry");
        check(uncertain.importSyntheticFixture((item,out)->c.sourceCalls++,uncertainJournal)
                ==SharedTransferCoordinator.Verdict.BLOCKED_REPLAY,"second read blocked");
        check(c.sourceCalls==1,"no second source read");

        System.out.println("V4-07-SHARED-STATE-TO-STORE PASS ("+assertions+" assertions)");
        System.out.println("V4-07 boundaries: synthetic-only fake bytes, real disk/receipt, zero network");
    }
}
