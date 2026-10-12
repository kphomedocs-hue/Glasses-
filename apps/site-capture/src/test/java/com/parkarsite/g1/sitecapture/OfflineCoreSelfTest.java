package com.parkarsite.g1.sitecapture;

import com.parkarsite.g1.workflow.MixedMediaRecoveryStore;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.ZoneId;
import java.util.HexFormat;
import java.util.concurrent.atomic.AtomicInteger;

/** Offline site-capture facade acceptance: no network, no glasses or APK. */
public final class OfflineCoreSelfTest {
    private static int checks;
    private static void must(boolean value,String reason) {
        checks++; if(!value)throw new AssertionError(reason);
    }
    private static final ZoneId ZONE=ZoneId.of("Asia/Kolkata");
    private static String identity(String name) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
            .digest(name.getBytes(StandardCharsets.US_ASCII)));
    }
    private static byte[] jpg() {
        return new byte[]{(byte)255,(byte)216,(byte)255,12,34,(byte)255,(byte)217};
    }
    private static byte[] mp4() {
        return new byte[]{0,0,0,8,'f','t','y','p',0,0,0,8,'m','d','a','t'};
    }
    public static void main(String[] args) throws Exception {
        var root=Files.createTempDirectory("v4-production-offline");
        var facade=new SiteCaptureOfflineCore(root,ZONE);
        must(!facade.canReadRemoteMedia()&&!facade.canSyncFromGlasses()
                &&!facade.canConnectToGlasses(),"no capability to touch device");
        must(facade.whyCannotSync()==SiteCaptureOfflineCore.TransferBlock
            .ORIGINALS_NOT_PROVEN_TO_REMAIN_ON_GLASSES,"original-preservation veto immutable");
        var empty=facade.inspectAndRecoverLocalOnly();
        must(empty.verifiedFiles()==0&&empty.manualReview()==0,
            "empty local store is not fabricated media");
        must(empty.safeStatusCode().equals("LOCAL_ONLY_SAFE"),"empty local status");
        var storage=new MixedMediaRecoveryStore(root.resolve("KSiteCapture"),ZONE);
        var ticket=storage.prepare(identity("safe-jpg"),MixedMediaRecoveryStore.Kind.JPG,
            1760227200000L,1760140800000L);
        var completed=storage.execute(ticket,jpg().length,out->out.write(jpg()),at->{});
        must(storage.committedAndIntact(completed.opaqueId()),"source injected only by offline test");
        var pending=storage.prepare(identity("uncertain-mp4"),MixedMediaRecoveryStore.Kind.MP4,
            1760227200000L,1760140800000L);
        var reads=new AtomicInteger();
        try {
            storage.execute(pending,mp4().length,out->{reads.incrementAndGet();out.write(mp4());},
                barrier->{if(barrier==MixedMediaRecoveryStore.Barrier.AFTER_FETCH_MARK)
                    throw new IOException("synthetic stop before source");});
            throw new AssertionError("expected synthetic stop");
        }catch(IOException expected){}
        must(reads.get()==0,"no bytes read after early interruption");
        var beforeRestart=facade.inspectAndRecoverLocalOnly();
        must(beforeRestart.verifiedFiles()==1
            && beforeRestart.remotelyUncertain()==1
            && beforeRestart.mode()==SiteCaptureOfflineCore.Mode.REVIEW_REQUIRED,
            "remote uncertain file must remain in review");
        var reopened=new SiteCaptureOfflineCore(root,ZONE);
        var afterRestart=reopened.inspectAndRecoverLocalOnly();
        must(afterRestart.verifiedFiles()==1&&afterRestart.remotelyUncertain()==1,
            "fresh facade does not automatically retry remote transfer");
        must(reads.get()==0,"restart must not call byte source");

        // Invalid disk metadata is NOT silently called empty or complete.
        Files.writeString(root.resolve("KSiteCapture").resolve(".mixed-journals")
            .resolve(completed.opaqueId()+".txn"),"CORRUPTED",StandardCharsets.UTF_8);
        var corrupt=new SiteCaptureOfflineCore(root,ZONE).inspectAndRecoverLocalOnly();
        must(corrupt.safeStatusCode().equals("LOCAL_LEDGER_UNVERIFIED")
            &&corrupt.mode()==SiteCaptureOfflineCore.Mode.REVIEW_REQUIRED,
            "corrupt ledger fail closed");
        must(!facade.canReadRemoteMedia(),"no remote read after corrupted journal");
        // The shipping facade intentionally exposes no source or test permit objects.
        for(var method:SiteCaptureOfflineCore.class.getDeclaredMethods()) {
            var text=method.toGenericString();
            must(!text.contains("OneShotByteSource")
                    &&!text.contains("HostFixturePermit")
                    &&!text.contains("importSyntheticFixture"),
                "shipping-facing API must not expose byte source or fixture permit");
        }
        System.out.println("V4-SITE-CAPTURE-OFFLINE PASS ("+checks+" assertions)");
    }
}
