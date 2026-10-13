package com.parkarsite.g1.preview;

import android.app.Instrumentation;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import com.parkarsite.g1.sitecapture.SiteCaptureOfflineCore;
import com.parkarsite.g1.workflow.MixedMediaRecoveryStore;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.ZoneId;
import java.util.HexFormat;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ANDROIDTEST ONLY. Populates synthetic media directly into app-private storage.
 * No radios, Internet or vendor glass access. No test API in the main APK.
 */
public final class OfflineArchiveInstrumentation extends Instrumentation {
    private static final String COMPLETE_JPG="V4_PREVIEW_SYNTH_ONLY_JPG";
    private static final String COMPLETE_MP4="V4_PREVIEW_SYNTH_ONLY_MP4";
    private static final String COMPLETE_OPUS="V4_PREVIEW_SYNTH_ONLY_OPUS";
    private static final String UNCERTAIN="V4_PREVIEW_SYNTH_ONLY_UNCERTAIN";
    private Bundle options;

    @Override public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        options=arguments;
        start();
    }
    private static void require(boolean condition,String explanation) {
        if(!condition)throw new AssertionError(explanation);
    }
    private static String opaque(String tag)throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
            .digest(tag.getBytes(StandardCharsets.UTF_8)));
    }
    private Path root(){
        return getTargetContext().getApplicationContext()
            .getNoBackupFilesDir().toPath().resolve("KSiteCapture");
    }
    private MixedMediaRecoveryStore store()throws Exception {
        return new MixedMediaRecoveryStore(root(),ZoneId.systemDefault());
    }
    private static byte[] jpg()throws Exception{
        Bitmap bitmap=Bitmap.createBitmap(24,16,Bitmap.Config.ARGB_8888);
        try {
            for(int y=0;y<16;y++)for(int x=0;x<24;x++)
                bitmap.setPixel(x,y,0xff000000|((x*10)&255)<<16|((y*15)&255)<<8|(x+y));
            var output=new ByteArrayOutputStream();
            require(bitmap.compress(Bitmap.CompressFormat.JPEG,90,output),
                "synthetic JPEG compression failed");
            byte[] bytes=output.toByteArray();
            Bitmap decoded=BitmapFactory.decodeByteArray(bytes,0,bytes.length);
            require(decoded!=null&&decoded.getWidth()==24&&decoded.getHeight()==16,
                "synthetic JPEG not decoder-valid");
            decoded.recycle();
            return bytes;
        }finally{bitmap.recycle();}
    }
    private static byte[] mp4(){
        // Tiny ISO BMFF ftyp+mdat shape, deliberately NOT a playable video.
        return new byte[]{0,0,0,8,'f','t','y','p',0,0,0,8,'m','d','a','t'};
    }
    private static byte[] opus(){
        // Deliberately NOT a decoded/playable vendor recording.
        byte[] bytes=new byte[36];
        byte[] ogg="OggS".getBytes(StandardCharsets.US_ASCII);
        byte[] head="OpusHead".getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(ogg,0,bytes,0,ogg.length);
        System.arraycopy(head,0,bytes,28,head.length);
        return bytes;
    }
    private void writeSynthetic(String tag,MixedMediaRecoveryStore.Kind kind,
                                byte[] bytes)throws Exception {
        var storage=store();
        String id=opaque(tag);
        var ticket=storage.prepare(id,kind,1_760_400_000_000L,1_760_399_000_000L);
        require(ticket.state()==MixedMediaRecoveryStore.State.PREPARED,
            "fixture was already staged");
        AtomicInteger reads=new AtomicInteger();
        storage.execute(ticket,bytes.length,out->{
            reads.incrementAndGet();
            out.write(bytes);
        },at->{});
        require(reads.get()==1&&storage.committedAndIntact(id),
            "synthetic fixture did not commit exactly once");
    }
    private void seedComplete()throws Exception {
        writeSynthetic(COMPLETE_JPG,MixedMediaRecoveryStore.Kind.JPG,jpg());
        writeSynthetic(COMPLETE_MP4,MixedMediaRecoveryStore.Kind.MP4,mp4());
        writeSynthetic(COMPLETE_OPUS,MixedMediaRecoveryStore.Kind.OPUS,opus());
        var overview=new SiteCaptureOfflineCore(
            getTargetContext().getNoBackupFilesDir().toPath(),ZoneId.systemDefault())
            .inspectAndRecoverLocalOnly();
        require(overview.verifiedFiles()==3&&overview.remotelyUncertain()==0
                &&overview.manualReview()==0,"three synthetic receipts not verified");
    }
    private void seedUncertain()throws Exception {
        var storage=store();
        String id=opaque(UNCERTAIN);
        var ticket=storage.prepare(id,MixedMediaRecoveryStore.Kind.OPUS,
            1_760_400_000_000L,1_760_399_000_000L);
        AtomicInteger callbacks=new AtomicInteger();
        boolean stopped=false;
        try{
            storage.execute(ticket,opus().length,out->{
                callbacks.incrementAndGet();
                out.write(opus());
            },barrier->{
                if(barrier==MixedMediaRecoveryStore.Barrier.AFTER_FETCH_MARK)
                    throw new IOException("synthetic remote state intentionally uncertain");
            });
        }catch(IOException expected){stopped=true;}
        require(stopped&&callbacks.get()==0,"interruption must precede Source");
        var overview=new SiteCaptureOfflineCore(
            getTargetContext().getNoBackupFilesDir().toPath(),ZoneId.systemDefault())
            .inspectAndRecoverLocalOnly();
        require(overview.verifiedFiles()==3&&overview.remotelyUncertain()==1
                &&overview.mode()==SiteCaptureOfflineCore.Mode.REVIEW_REQUIRED,
            "uncertain read must request review, never auto retry");
    }
    private void corruptJournal()throws Exception {
        Path journal=root().resolve(".mixed-journals")
            .resolve(opaque(COMPLETE_JPG)+".txn");
        require(Files.isRegularFile(journal),"expected synthetic journal");
        Files.write(journal,"BAD_SYNTHETIC_JOURNAL".getBytes(StandardCharsets.UTF_8));
        var overview=new SiteCaptureOfflineCore(
            getTargetContext().getNoBackupFilesDir().toPath(),ZoneId.systemDefault())
            .inspectAndRecoverLocalOnly();
        require(overview.mode()==SiteCaptureOfflineCore.Mode.REVIEW_REQUIRED
                &&overview.safeStatusCode().equals("LOCAL_LEDGER_UNVERIFIED"),
            "corrupted journal must never display a normal state");
    }
    @Override public void onStart(){
        var result=new Bundle();
        try{
            String mode=options==null?"":options.getString("preview_mode","");
            switch(mode){
                case "seed_complete" -> {seedComplete();result.putString("fixture","COMPLETE_3");}
                case "seed_uncertain" -> {seedUncertain();result.putString("fixture","UNCERTAIN_1");}
                case "corrupt_journal" -> {corruptJournal();result.putString("fixture","JOURNAL_CORRUPT");}
                default -> throw new IllegalArgumentException("Unknown synthetic fixture mode");
            }
            result.putString("kind","ANDROIDTEST_SYNTHETIC_PRIVATE_ONLY");
            result.putString("status","PASS");
            finish(0,result);
        }catch(Throwable failure){
            result.putString("status","FAIL");
            result.putString("error_kind",failure.getClass().getSimpleName());
            finish(1,result);
        }
    }
}
