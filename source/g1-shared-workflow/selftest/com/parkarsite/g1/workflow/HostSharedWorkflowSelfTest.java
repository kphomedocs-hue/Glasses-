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
        List<SharedTransferContracts.PeerEvidence> peerEvidence=List.of(
            new SharedTransferContracts.PeerEvidence("opaque-test-peer",true,true,true,true));
        final List<SharedTransferContracts.CatalogSnapshot> catalogs=new ArrayList<>();
        int index;
        final List<Runnable> deadlines=new ArrayList<>();
        public void beginObservation(){bleStarts++;}
        public void requestInventory(){inventory++;}
        public void requestExit(){exit++;}
        public void requestTeardown(){teardown++;}
        public boolean groupAbsent(){return groupAbsent;}
        public List<SharedTransferContracts.PeerEvidence> snapshot(){
            return peerEvidence;
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
        // V4-08: adversarial checks against the exact shared state-to-store coordinator,
        // rather than repeating the disconnected virtual-lab policy tests.
        int after07=assertions;
        var third=jpg("V4_ONLY_SYNTHETIC_EXTRA",MARKER.length);

        Fake wrongPeer=fixture(old,added);
        wrongPeer.peerEvidence=List.of(new SharedTransferContracts.PeerEvidence(
            "opaque-not-selected",false,true,true,true));
        var wrongFlow=new SharedTransferCoordinator(wrongPeer.observation(),wrongPeer,()->true);
        check(wrongFlow.observeBaseline(1)==SharedTransferCoordinator.Verdict.BLOCKED_EVIDENCE,
            "unselected peer evidence blocks before catalog");
        check(wrongPeer.readCatalog==0&&wrongPeer.exit==1,
            "wrong peer cannot reach catalog; cleanup requested");

        Fake noIp=fixture(old,added);
        noIp.peerEvidence=List.of(new SharedTransferContracts.PeerEvidence(
            "opaque-no-correlated-ip",true,true,false,true));
        var noIpFlow=new SharedTransferCoordinator(noIp.observation(),noIp,()->true);
        check(noIpFlow.observeBaseline(1)==SharedTransferCoordinator.Verdict.BLOCKED_EVIDENCE,
            "no correlated IP blocks");

        Fake ambiguous=fixture(old,added);
        ambiguous.peerEvidence=List.of(
            new SharedTransferContracts.PeerEvidence("opaque-a",true,true,true,true),
            new SharedTransferContracts.PeerEvidence("opaque-b",true,true,true,true));
        var ambiguousFlow=new SharedTransferCoordinator(ambiguous.observation(),ambiguous,()->true);
        check(ambiguousFlow.observeBaseline(1)==SharedTransferCoordinator.Verdict.BLOCKED_EVIDENCE,
            "multiple candidates blocked without fetch");

        Fake plusTwo=new Fake();
        plusTwo.catalogs.add(new SharedTransferContracts.CatalogSnapshot(List.of(old)));
        plusTwo.catalogs.add(new SharedTransferContracts.CatalogSnapshot(List.of(old,added,third)));
        var plusTwoFlow=new SharedTransferCoordinator(plusTwo.observation(),plusTwo,()->true);
        check(plusTwoFlow.observeBaseline(1)==SharedTransferCoordinator.Verdict.BASELINE_ACCEPTED,
            "plus-two scenario baseline");
        check(plusTwoFlow.observeSingleNewJpg(3)==SharedTransferCoordinator.Verdict.BLOCKED_CATALOG,
            "two fresh JPGs rejected");

        Fake duplicate=new Fake();
        duplicate.catalogs.add(new SharedTransferContracts.CatalogSnapshot(List.of(old)));
        duplicate.catalogs.add(new SharedTransferContracts.CatalogSnapshot(List.of(old,added,added)));
        var dupFlow=new SharedTransferCoordinator(duplicate.observation(),duplicate,()->true);
        check(dupFlow.observeBaseline(1)==SharedTransferCoordinator.Verdict.BASELINE_ACCEPTED,
            "duplicate scenario baseline");
        check(dupFlow.observeSingleNewJpg(3)==SharedTransferCoordinator.Verdict.BLOCKED_CATALOG,
            "duplicate opaque catalog identity rejected");

        Fake changedKind=new Fake();
        changedKind.catalogs.add(new SharedTransferContracts.CatalogSnapshot(List.of(old)));
        var mutatedOld=new SharedTransferContracts.MediaDescriptor(
            old.opaqueIdentity(),SharedTransferContracts.MediaKind.OPUS,7L,old.captureEpochMs());
        changedKind.catalogs.add(new SharedTransferContracts.CatalogSnapshot(List.of(mutatedOld,added)));
        var changedKindFlow=new SharedTransferCoordinator(changedKind.observation(),changedKind,()->true);
        check(changedKindFlow.observeBaseline(1)==SharedTransferCoordinator.Verdict.BASELINE_ACCEPTED,
            "changed kind scenario baseline");
        check(changedKindFlow.observeSingleNewJpg(1)==SharedTransferCoordinator.Verdict.BLOCKED_CATALOG,
            "unchanged image count with identity mutation is rejected");

        Fake revoked=fixture(old,added);
        var revokedFlow=new SharedTransferCoordinator(revoked.observation(),revoked,()->true);
        check(revokedFlow.observeBaseline(1)==SharedTransferCoordinator.Verdict.BASELINE_ACCEPTED,
            "revocation scenario baseline");
        revoked.peerEvidence=List.of();
        check(revokedFlow.observeSingleNewJpg(2)==SharedTransferCoordinator.Verdict.BLOCKED_EVIDENCE,
            "peer lost between catalog reads blocks");
        check(revoked.readCatalog==1,"no second catalog if peer evidence lost");

        Fake beforeBytes=fixture(old,added);
        var beforeBytesFlow=new SharedTransferCoordinator(beforeBytes.observation(),beforeBytes,()->true);
        check(beforeBytesFlow.observeBaseline(1)==SharedTransferCoordinator.Verdict.BASELINE_ACCEPTED,
            "peer withdrawal before bytes baseline");
        check(beforeBytesFlow.observeSingleNewJpg(2)==SharedTransferCoordinator.Verdict.SINGLE_JPG_SELECTED,
            "peer withdrawal before bytes delta");
        beforeBytes.peerEvidence=List.of();
        Path blockedRoot=Files.createTempDirectory("v4-no-peer-before-fetch-");
        check(beforeBytesFlow.importSyntheticFixture((item,out)->beforeBytes.sourceCalls++,
            new JournalAdapter(blockedRoot))==SharedTransferCoordinator.Verdict.BLOCKED_EVIDENCE,
            "peer evidence must still exist at fetch boundary");
        check(beforeBytes.sourceCalls==0&&beforeBytes.exit==1,"no byte source and exit requested");

        Fake expired=fixture(old,added);
        var expiredObs=expired.observation();
        expired.deadlines.get(0).run(); // injected stale timeout rather than real clock
        var expiredFlow=new SharedTransferCoordinator(expiredObs,expired,()->true);
        check(expiredFlow.observeBaseline(1)==SharedTransferCoordinator.Verdict.BLOCKED_EVIDENCE,
            "expired observation cannot observe catalog");
        check(expired.readCatalog==0&&expired.exit==1,"timeout blocks and initiates cleanup");

        Fake throwingPermit=fixture(old,added);
        var throwingFlow=new SharedTransferCoordinator(throwingPermit.observation(),throwingPermit,
            ()->{throw new IllegalStateException("test permit failed");});
        check(throwingFlow.observeBaseline(1)==SharedTransferCoordinator.Verdict.BASELINE_ACCEPTED,
            "throwing permit baseline");
        check(throwingFlow.observeSingleNewJpg(2)==SharedTransferCoordinator.Verdict.SINGLE_JPG_SELECTED,
            "throwing permit delta");
        check(throwingFlow.importSyntheticFixture((item,out)->throwingPermit.sourceCalls++,
            new JournalAdapter(Files.createTempDirectory("v4-permit-error-")))
            ==SharedTransferCoordinator.Verdict.BLOCKED_PRESERVATION,
            "fixture permit failure denies without reading");
        check(throwingPermit.sourceCalls==0&&throwingPermit.exit==1,"permit error cleanup");

        System.out.println("V4-08-SHARED-ADVERSARIAL PASS ("+(assertions-after07)+" assertions)");
        System.out.println("V4-07 boundaries: synthetic-only fake bytes, real disk/receipt, zero network");
    }
}
