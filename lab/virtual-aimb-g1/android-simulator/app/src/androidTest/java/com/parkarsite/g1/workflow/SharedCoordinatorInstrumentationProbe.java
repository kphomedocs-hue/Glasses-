package com.parkarsite.g1.workflow;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.parkarsite.g6a.recovery.RecoveryImportStore;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.ZoneId;
import java.util.HexFormat;
import java.util.List;
import java.util.ArrayList;

/** ANDROID-TEST SOURCE ONLY: never move to app/src/main or source/g1-shared-workflow/src. */
public final class SharedCoordinatorInstrumentationProbe {
    private SharedCoordinatorInstrumentationProbe(){}
    private static final String BASE="V4-API36-SYNTH-BASE-ONLY";
    private static final String NEW="V4-API36-SYNTH-JPEG-ONLY";
    private static final String CRASH="V4-API36-SYNTH-CRASH-ONLY";
    private static final long FIRST_SEEN=1760227200000L;
    private static void must(boolean ok,String reason){if(!ok)throw new AssertionError(reason);}
    private static String opaque(String value)throws Exception{
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
            .digest(value.getBytes(StandardCharsets.UTF_8)));
    }
    public static byte[] completeSyntheticJpeg() throws Exception {
        Bitmap bitmap=Bitmap.createBitmap(32,24,Bitmap.Config.ARGB_8888);
        try {
            for(int y=0;y<24;y++)for(int x=0;x<32;x++)
                bitmap.setPixel(x,y,0xff000000|((x*7)&255)<<16|((y*11)&255)<<8|((x+y*3)&255));
            var output=new ByteArrayOutputStream();
            must(bitmap.compress(Bitmap.CompressFormat.JPEG,90,output),"bitmap compression failed");
            var bytes=output.toByteArray();
            must(bytes.length>100 && bytes.length<65536,"unexpected synthetic JPEG length");
            Bitmap decoded=BitmapFactory.decodeByteArray(bytes,0,bytes.length);
            must(decoded!=null && decoded.getWidth()==32 && decoded.getHeight()==24,
                "JPEG must decode as the expected raster");
            decoded.recycle();
            return bytes;
        } finally {bitmap.recycle();}
    }
    private static void checkSavedJpeg(Path file) throws Exception {
        must(Files.isRegularFile(file),"JPEG missing");
        byte[] bytes=Files.readAllBytes(file);
        var decoded=BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        must(decoded!=null && decoded.getWidth()==32 && decoded.getHeight()==24,
            "stored JPEG does not decode");
        decoded.recycle();
    }
    private static final class Fake implements SharedTransferContracts.BleSession,
        SharedTransferContracts.PeerObserver, SharedTransferContracts.Lifecycle,
        SharedTransferContracts.DeadlineScheduler, SharedTransferContracts.CatalogSource {
        int begin, inventory, exit, teardown, reads, sourceReads;
        final List<SharedTransferContracts.CatalogSnapshot> catalogs=new ArrayList<>();
        public void beginObservation(){begin++;}
        public void requestInventory(){inventory++;}
        public void requestExit(){exit++;}
        public void requestTeardown(){teardown++;}
        public boolean groupAbsent(){return true;}
        public List<SharedTransferContracts.PeerEvidence> snapshot(){
            return List.of(new SharedTransferContracts.PeerEvidence("opaque-synthetic",
                true,true,true,true));
        }
        public SharedTransferContracts.Cancellation arm(long delay,Runnable deadline){
            must(delay>0 && delay<=16_000,"unbounded deadline");
            return ()->{};
        }
        public SharedTransferContracts.CatalogSnapshot readCatalog(){
            return catalogs.get(reads++);
        }
        SharedObservationController observation() {
            var result=new SharedObservationController(this,this,this,this);
            result.startObservation();
            result.refreshPeerCandidates();
            return result;
        }
    }
    private static SharedTransferContracts.MediaDescriptor jpg(String key,int len)throws Exception{
        return new SharedTransferContracts.MediaDescriptor(opaque(key),
            SharedTransferContracts.MediaKind.JPG,len,FIRST_SEEN);
    }
    private static Fake fake(String addedKey,int bytes)throws Exception{
        var baseline=jpg(BASE,bytes);
        var newest=jpg(addedKey,bytes);
        var f=new Fake();
        f.catalogs.add(new SharedTransferContracts.CatalogSnapshot(List.of(baseline)));
        f.catalogs.add(new SharedTransferContracts.CatalogSnapshot(List.of(baseline,newest)));
        return f;
    }
    private static final class StoreAdapter implements SharedTransferCoordinator.DurableOneShotImport {
        final RecoveryImportStore store;
        final RecoveryImportStore.Barrier fatalAt;
        StoreAdapter(Path root,RecoveryImportStore.Barrier fatalAt)throws Exception{
            this.store=new RecoveryImportStore(root,ZoneId.of("Asia/Kolkata"));
            this.fatalAt=fatalAt;
        }
        public boolean committedAndIntact(String id)throws Exception{
            return store.isCommittedAndIntact(id);
        }
        public void importOnce(SharedTransferContracts.MediaDescriptor item,
                               SharedTransferContracts.OneShotByteSource source)throws Exception{
            if(item.kind()!=SharedTransferContracts.MediaKind.JPG)throw new IOException("JPG only");
            var ticket=store.prepare(item.opaqueIdentity(),item.captureEpochMs());
            if(ticket.state()!=RecoveryImportStore.State.PREPARED)
                throw new IOException("No retry of pending/committed file");
            store.execute(ticket,item.expectedBytes(),
                out->source.streamOnce(item,out),
                barrier->{if(barrier==fatalAt)Runtime.getRuntime().halt(73);});
        }
    }
    private static Path directory(Context context,boolean crash) {
        return context.getApplicationContext().getNoBackupFilesDir().toPath()
            .resolve(crash?"v4-shared-crash-private":"v4-shared-real-jpeg-private");
    }
    private static Path completedFile(Path root) {
        return root.resolve("2025-10-12/0001.jpg");
    }
    /** Deliberate actual Android instrumentation process death at VERIFIED journal. */
    public static void crashAtVerified(Context context)throws Exception {
        var bytes=completeSyntheticJpeg();
        var f=fake(CRASH,bytes.length);
        var obs=f.observation();
        var workflow=new SharedTransferCoordinator(obs,f,()->true);
        must(workflow.observeBaseline(1)==SharedTransferCoordinator.Verdict.BASELINE_ACCEPTED,
            "crash baseline");
        must(workflow.observeSingleNewJpg(2)==SharedTransferCoordinator.Verdict.SINGLE_JPG_SELECTED,
            "crash delta");
        workflow.importSyntheticFixture((item,out)->{
            f.sourceReads++;
            out.write(bytes);
        },new StoreAdapter(directory(context,true),
            RecoveryImportStore.Barrier.AFTER_VERIFIED_JOURNAL));
        throw new AssertionError("Process survived configured verification barrier");
    }
    public static String run(Context context,String mode)throws Exception {
        byte[] bytes=completeSyntheticJpeg();
        Path root=directory(context,false);
        String newId=opaque(NEW);
        if("shared_first".equals(mode)){
            var f=fake(NEW,bytes.length);
            var obs=f.observation();
            var workflow=new SharedTransferCoordinator(obs,f,()->true);
            var store=new StoreAdapter(root,null);
            must(workflow.observeBaseline(1)==SharedTransferCoordinator.Verdict.BASELINE_ACCEPTED,
                "baseline must pass");
            must(workflow.observeSingleNewJpg(2)==SharedTransferCoordinator.Verdict.SINGLE_JPG_SELECTED,
                "exact +1 must pass");
            must(workflow.importSyntheticFixture((item,out)->{
                f.sourceReads++;
                var marker=root.resolve(".transactions").resolve(item.opaqueIdentity()+".txn");
                must(Files.readString(marker).contains("FETCH_MAY_HAVE_STARTED"),
                    "durable intent must precede fixture access");
                out.write(bytes);
            },store)==SharedTransferCoordinator.Verdict.SYNTHETIC_RECEIPT_COMMITTED,
                "synthetic receipt must be committed");
            must(f.sourceReads==1 && f.exit==1 && f.teardown==1,"one read, bounded cleanup");
            obs.onExitEvidence(true,true);
            must(workflow.completeAfterExit()==SharedTransferCoordinator.Verdict.SYNTHETIC_CLOSED,
                "must verify exit and group absence");
            must(store.committedAndIntact(newId),"receipt with valid local bytes");
            checkSavedJpeg(completedFile(root));
            return "V4_SHARED_FULL_JPEG_COMMITTED";
        }
        if("shared_reopen".equals(mode)){
            var store=new StoreAdapter(root,null);
            must(store.store.recover().stream().anyMatch(x->newId.equals(x.opaqueId())
                &&x.disposition()==RecoveryImportStore.Disposition.COMPLETE),
                "new instrumentation process must reopen durable committed receipt");
            must(store.committedAndIntact(newId),"file+receipt survive new Android process");
            checkSavedJpeg(completedFile(root));
            // public shipping coordinator still cannot read, even with injected proof.
            var fake=fake(NEW,bytes.length);
            var shipping=new SharedTransferCoordinator(fake.observation(),fake);
            must(shipping.observeBaseline(1)==SharedTransferCoordinator.Verdict.BASELINE_ACCEPTED,
                "read-only baseline");
            must(shipping.observeSingleNewJpg(2)==SharedTransferCoordinator.Verdict.SINGLE_JPG_SELECTED,
                "read-only catalog selection");
            must(shipping.importSyntheticFixture((item,out)->fake.sourceReads++,
                new StoreAdapter(context.getNoBackupFilesDir().toPath().resolve("v4-denied-shipping"),null))
                ==SharedTransferCoordinator.Verdict.BLOCKED_PRESERVATION,
                "public constructor must always block real media");
            must(fake.sourceReads==0 && !shipping.remoteMediaGetAuthorized(),
                "shipping path no source invocation");
            return "V4_SHARED_REOPEN_NO_DUPLICATE";
        }
        if("shared_recover_verified".equals(mode)){
            Path crashRoot=directory(context,true);
            var store=new StoreAdapter(crashRoot,null);
            String id=opaque(CRASH);
            must(store.store.recover().stream().anyMatch(x->id.equals(x.opaqueId())
                &&x.disposition()==RecoveryImportStore.Disposition.LOCAL_RECOVERED),
                "new Android process must promote verified local data only");
            must(store.committedAndIntact(id),"verified local receipt reconstructed");
            checkSavedJpeg(completedFile(crashRoot));
            return "V4_SHARED_HALT_RECOVERED_NO_REFETCH";
        }
        throw new IllegalArgumentException("Unknown isolated V4 mode");
    }
}
