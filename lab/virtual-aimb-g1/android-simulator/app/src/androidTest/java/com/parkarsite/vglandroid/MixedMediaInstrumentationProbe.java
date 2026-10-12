package com.parkarsite.vglandroid;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.parkarsite.g1.workflow.MixedMediaRecoveryStore;
import com.parkarsite.g1.workflow.SharedCoordinatorInstrumentationProbe;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HexFormat;
import java.util.concurrent.atomic.AtomicInteger;

/** API36 instrumentation-only mixed-media stress probe; no network or vendor media. */
public final class MixedMediaInstrumentationProbe {
    private MixedMediaInstrumentationProbe(){}
    private static final ZoneId ZONE=ZoneId.of("Asia/Kolkata");
    private static void must(boolean ok,String msg){if(!ok)throw new AssertionError(msg);}
    private static String id(String label)throws Exception{
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
            .digest(label.getBytes(StandardCharsets.UTF_8)));
    }
    private static long epoch(int day,int hour,int minute){
        return ZonedDateTime.of(2026,10,day,hour,minute,0,0,ZONE).toInstant().toEpochMilli();
    }
    private static byte[] mp4(){return new byte[]{0,0,0,8,'f','t','y','p',0,0,0,8,'m','d','a','t'};}
    private static byte[] opus(){
        byte[] b=new byte[36];
        System.arraycopy("OggS".getBytes(StandardCharsets.US_ASCII),0,b,0,4);
        System.arraycopy("OpusHead".getBytes(StandardCharsets.US_ASCII),0,b,28,8);
        return b;
    }
    private static Path root(Context context){
        return context.getApplicationContext().getNoBackupFilesDir().toPath()
            .resolve("vgl-v4-mixed-files");
    }
    private static Path crashRoot(Context context){
        return context.getApplicationContext().getNoBackupFilesDir().toPath()
            .resolve("vgl-v4-mixed-crash");
    }
    private static MixedMediaRecoveryStore store(Context context){
        try{return new MixedMediaRecoveryStore(root(context),ZONE);}
        catch(Exception error){throw new IllegalStateException(error);}
    }
    private static void decode(Path file)throws Exception{
        byte[] bytes=Files.readAllBytes(file);
        Bitmap bitmap=BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        must(bitmap!=null&&bitmap.getWidth()==32&&bitmap.getHeight()==24,
            "complete persisted synthetic JPEG decode failed");
        bitmap.recycle();
    }
    private static void save(MixedMediaRecoveryStore data,String label,
        MixedMediaRecoveryStore.Kind kind,long firstSeen,long capture,byte[] bytes,
        String path)throws Exception {
        var ticket=data.prepare(id(label),kind,firstSeen,capture);
        must(ticket.relativePath().equals(path),"shared daily numbering incorrect");
        must(ticket.firstSeenEpochMs()==firstSeen &&ticket.captureEpochMs()==capture,
            "first-seen and capture timestamps must remain distinct");
        AtomicInteger sourceCalls=new AtomicInteger();
        data.execute(ticket,bytes.length,out->{
            sourceCalls.incrementAndGet();
            out.write(bytes);
        },barrier->{});
        must(sourceCalls.get()==1 && data.committedAndIntact(id(label)),
            "synthetic durable import or receipt incomplete");
    }
    public static String run(Context context,String mode)throws Exception {
        String jpgId=id("V4-MM-JPG"),mp4Id=id("V4-MM-MP4"),opusId=id("V4-MM-OPUS"),
            nextId=id("V4-MM-ROLLOVER");
        var data=store(context);
        if("mixed_first".equals(mode)){
            byte[] jpeg=SharedCoordinatorInstrumentationProbe.completeSyntheticJpeg();
            long seen=epoch(12,23,58),captured=epoch(11,14,7);
            save(data,"V4-MM-JPG",MixedMediaRecoveryStore.Kind.JPG,seen,captured,jpeg,
                "2026-10-12/0001.jpg");
            save(data,"V4-MM-MP4",MixedMediaRecoveryStore.Kind.MP4,seen,captured,mp4(),
                "2026-10-12/0002.mp4");
            save(data,"V4-MM-OPUS",MixedMediaRecoveryStore.Kind.OPUS,seen,captured,opus(),
                "2026-10-12/0003.opus");
            save(data,"V4-MM-ROLLOVER",MixedMediaRecoveryStore.Kind.JPG,epoch(13,0,1),
                captured,jpeg,"2026-10-13/0001.jpg");
            decode(root(context).resolve("2026-10-12/0001.jpg"));
            decode(root(context).resolve("2026-10-13/0001.jpg"));
            return "V4_MIXED_JPG_MP4_OPUS_NUMBERED";
        }
        if("mixed_reopen".equals(mode)){
            var events=data.recover();
            must(events.size()==4,"expected four committed synthetic objects");
            must(events.stream().allMatch(e->e.outcome()==MixedMediaRecoveryStore.Recovery.COMPLETE),
                "every receipt must survive new instrumentation process");
            must(data.committedAndIntact(jpgId)&&data.committedAndIntact(mp4Id)
                &&data.committedAndIntact(opusId)&&data.committedAndIntact(nextId),
                "mixed receipts not intact after force-stop");
            decode(root(context).resolve("2026-10-12/0001.jpg"));
            AtomicInteger sourceCalls=new AtomicInteger();
            var previous=data.prepare(mp4Id,MixedMediaRecoveryStore.Kind.MP4,
                epoch(12,23,58),epoch(11,14,7));
            try {
                data.execute(previous,mp4().length,out->sourceCalls.incrementAndGet(),
                    barrier->{});
                throw new AssertionError("Committed MP4 could be fetched again");
            }catch(java.io.IOException expected){}
            must(sourceCalls.get()==0,"mixed media was re-fetched");
            return "V4_MIXED_REOPEN_NO_DUPLICATES";
        }
        if("mixed_recover_verified".equals(mode)){
            var crashed=new MixedMediaRecoveryStore(crashRoot(context),ZONE);
            String opaque=id("V4-MM-CRASH");
            must(crashed.recover().stream().anyMatch(e->e.opaqueId().equals(opaque)
                    &&e.outcome()==MixedMediaRecoveryStore.Recovery.LOCAL_RECOVERED),
                "verified synthetic MP4 should recover entirely locally");
            must(crashed.committedAndIntact(opaque),"recovered MP4 receipt invalid");
            return "V4_MIXED_MP4_RECOVERED_NO_RETRY";
        }
        throw new IllegalArgumentException("Unknown mixed probe mode");
    }
    public static void crashAtVerified(Context context)throws Exception{
        var data=new MixedMediaRecoveryStore(crashRoot(context),ZONE);
        var ticket=data.prepare(id("V4-MM-CRASH"),MixedMediaRecoveryStore.Kind.MP4,
            epoch(14,9,0),epoch(12,9,0));
        data.execute(ticket,mp4().length,out->out.write(mp4()),barrier->{
            if(barrier==MixedMediaRecoveryStore.Barrier.AFTER_VERIFIED_JOURNAL)
                Runtime.getRuntime().halt(73);
        });
        throw new AssertionError("Unexpectedly survived mixed recovery barrier");
    }
}
