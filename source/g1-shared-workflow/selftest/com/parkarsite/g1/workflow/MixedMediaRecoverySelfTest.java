package com.parkarsite.g1.workflow;

import java.io.IOException;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/** V4-12 offline-only coverage; no HTTP/media/glasses device access. */
public final class MixedMediaRecoverySelfTest {
    private static int checks;
    private static void check(boolean good,String why){
        checks++;if(!good)throw new AssertionError(why);
    }
    private static String id(String label)throws Exception{
        return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
            .digest(label.getBytes(StandardCharsets.UTF_8)));
    }
    private static byte[] jpg(){return new byte[]{(byte)255,(byte)216,(byte)255,1,2,(byte)255,(byte)217};}
    private static byte[] mp4(){return new byte[]{0,0,0,8,'f','t','y','p',0,0,0,8,'m','d','a','t'};}
    private static byte[] opus(){
        byte[] b=new byte[36];
        byte[] o="OggS".getBytes(StandardCharsets.US_ASCII),
            p="OpusHead".getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(o,0,b,0,o.length);
        System.arraycopy(p,0,b,28,p.length);
        return b;
    }
    private static long time(int day,int hour,int minute){
        return ZonedDateTime.of(2026,10,day,hour,minute,0,0,ZoneId.of("Asia/Kolkata"))
            .toInstant().toEpochMilli();
    }
    private static MixedMediaRecoveryStore.Ticket commit(MixedMediaRecoveryStore store,
        String opaque,MixedMediaRecoveryStore.Kind kind,long seen,long capture,byte[] bytes)
            throws Exception{
        var ticket=store.prepare(opaque,kind,seen,capture);
        check(ticket.state()==MixedMediaRecoveryStore.State.PREPARED,"new reservation prepared");
        AtomicInteger reads=new AtomicInteger();
        var committed=store.execute(ticket,bytes.length,out->{
            reads.incrementAndGet();
            var journal=Path.of(storeRoot.toString(),".mixed-journals",opaque+".txn");
            check(Files.size(journal)<=4096
                &&new String(Files.readAllBytes(journal),StandardCharsets.UTF_8)
                    .contains("FETCH_MAY_HAVE_STARTED"),"durable marker before source");
            out.write(bytes);
        },barrier->{});
        check(reads.get()==1,"exact one source callback");
        check(store.committedAndIntact(opaque),"committed bytes and receipt checked");
        return committed;
    }
    private static Path storeRoot;
    public static void main(String[] args)throws Exception{
        storeRoot=Files.createTempDirectory("vgl-mixed-offline-");
        var zone=ZoneId.of("Asia/Kolkata");
        var store=new MixedMediaRecoveryStore(storeRoot,zone);
        long before=time(12,23,59),after=time(13,0,1),olderCapture=time(11,9,0);
        String a=id("JPG-A"),b=id("MP4-B"),c=id("OPUS-C"),d=id("JPG-D");
        var t1=commit(store,a,MixedMediaRecoveryStore.Kind.JPG,before,olderCapture,jpg());
        check(t1.relativePath().equals("2026-10-12/0001.jpg"),"first date JPG");
        check(t1.captureEpochMs()==olderCapture&&t1.firstSeenEpochMs()==before,
            "capture and first seen timestamps distinct");
        var t2=commit(store,b,MixedMediaRecoveryStore.Kind.MP4,before,olderCapture,mp4());
        check(t2.relativePath().equals("2026-10-12/0002.mp4"),"shared sequential video slot");
        var t3=commit(store,c,MixedMediaRecoveryStore.Kind.OPUS,before,olderCapture,opus());
        check(t3.relativePath().equals("2026-10-12/0003.opus"),"shared sequential audio slot");

        var reopened=new MixedMediaRecoveryStore(storeRoot,zone);
        check(reopened.recover().stream().filter(x->
            x.outcome()==MixedMediaRecoveryStore.Recovery.COMPLETE).count()==3L,
            "three committed file receipts survive restart");
        check(reopened.prepare(b,MixedMediaRecoveryStore.Kind.MP4,before,olderCapture)
            .equals(t2),"repeated identity points at original immutable ticket");
        final AtomicInteger replayReads=new AtomicInteger();
        try{
            reopened.execute(t2,mp4().length,out->replayReads.incrementAndGet(),barrier->{});
            throw new AssertionError("replay unexpectedly allowed");
        }catch(IOException expected){}
        check(replayReads.get()==0,"no repeated Source on committed identity");
        var t4=commit(reopened,d,MixedMediaRecoveryStore.Kind.JPG,after,olderCapture,jpg());
        check(t4.relativePath().equals("2026-10-13/0001.jpg"),"Asia/Kolkata midnight rollover");

        var retry=new MixedMediaRecoveryStore(storeRoot,zone);
        check(retry.committedAndIntact(a)&&retry.committedAndIntact(b)
            &&retry.committedAndIntact(c)&&retry.committedAndIntact(d),"all three kinds and rollover intact");

        String uncertain=id("INTERRUPTED-OPUS");
        var future=retry.prepare(uncertain,MixedMediaRecoveryStore.Kind.OPUS,after,olderCapture);
        AtomicInteger sources=new AtomicInteger();
        try{
            retry.execute(future,opus().length,out->{
                sources.incrementAndGet();out.write(opus());
            },barrier->{if(barrier==MixedMediaRecoveryStore.Barrier.AFTER_FETCH_MARK)
                throw new IOException("synthetic interruption before bytes");});
            throw new AssertionError("expected interruption");
        }catch(IOException expected){}
        check(sources.get()==0,"before-fetch exception no Source");
        check(retry.recover().stream().anyMatch(x->x.opaqueId().equals(uncertain)
            &&x.outcome()==MixedMediaRecoveryStore.Recovery.REMOTE_UNCERTAIN),
            "uncertain source never retried");
        try{
            retry.execute(future,opus().length,out->sources.incrementAndGet(),barrier->{});
            throw new AssertionError("unsafe retry");
        }catch(IOException expected){}
        check(sources.get()==0,"no repeated source after fetch marker");

        String verified=id("VERIFIED-MP4");
        var v=retry.prepare(verified,MixedMediaRecoveryStore.Kind.MP4,after,olderCapture);
        AtomicInteger verifiedReads=new AtomicInteger();
        try{
            retry.execute(v,mp4().length,out->{verifiedReads.incrementAndGet();out.write(mp4());},
                barrier->{if(barrier==MixedMediaRecoveryStore.Barrier.AFTER_VERIFIED_JOURNAL)
                    throw new IOException("synthetic verified barrier");});
            throw new AssertionError("expected verified fault");
        }catch(IOException expected){}
        check(verifiedReads.get()==1,"one verified source");
        var recovered=new MixedMediaRecoveryStore(storeRoot,zone);
        check(recovered.recover().stream().anyMatch(x->x.opaqueId().equals(verified)
            &&x.outcome()==MixedMediaRecoveryStore.Recovery.LOCAL_RECOVERED),
            "verified MP4 finalized strictly from local bytes");
        check(recovered.committedAndIntact(verified),"recovered MP4 receipt intact");

        // Conservative, additive legacy migration: old numbered files reserve slots;
        // the old transactions are not rewritten or automatically downloaded again.
        Path legacyDay=storeRoot.resolve("2026-10-14");
        Files.createDirectories(legacyDay);
        Files.write(legacyDay.resolve("0009.jpg"),jpg());
        String legacyId=id("OLD-LEDGER-NO-REREAD");
        Path legacy=storeRoot.resolve(".transactions");
        Files.createDirectories(legacy);
        Files.writeString(legacy.resolve(legacyId+".txn"),
            "TXN1\\t"+legacyId+"\\t2026-10-14/0008.jpg\\tPREPARED\\t0\\t-\\n");
        var migration=new MixedMediaRecoveryStore(storeRoot,zone);
        check(migration.legacyIdentityPresent(legacyId),"old ledger recognized without parsing/modifying");
        try{
            migration.prepare(legacyId,MixedMediaRecoveryStore.Kind.JPG,
                time(14,8,0),olderCapture);
            throw new AssertionError("legacy duplicate allowed");
        }catch(IOException expected){}
        var migrated=migration.prepare(id("NEW-AFTER-LEGACY"),
            MixedMediaRecoveryStore.Kind.OPUS,time(14,8,0),olderCapture);
        check(migrated.relativePath().equals("2026-10-14/0010.opus"),
            "legacy daily JPG slot preserved in shared new mixed numbering");
        check(Files.isRegularFile(legacyDay.resolve("0009.jpg"))
            &&Files.isRegularFile(legacy.resolve(legacyId+".txn")),
            "old source and ledger unmodified");
        // An uncommitted legacy numbering reservation WITHOUT a final file
        // must still reserve its daily ordinal in the new mixed namespace.
        String pendingId=id("PENDING-LEGACY-JPG");
        Files.writeString(legacy.resolve(pendingId+".txn"),
            "TXN1\\t"+pendingId+"\\t2026-10-15/0007.jpg\\tPREPARED\\t0\\t-\\n");
        var afterPending=migration.prepare(id("MIXED-AFTER-PENDING-LEGACY"),
            MixedMediaRecoveryStore.Kind.MP4,time(15,8,0),olderCapture);
        check(afterPending.relativePath().equals("2026-10-15/0008.mp4"),
            "legacy uncommitted JPG reservation protects sequence even without final file");
        // Orphan legacy receipt is not permission to re-download its opaque ID.
        String orphan=id("LEGACY-RECEIPT-ONLY");
        Path legacyReceipts=storeRoot.resolve(".receipts");
        Files.createDirectories(legacyReceipts);
        Files.writeString(legacyReceipts.resolve(orphan+".receipt"),"LEGACY_RECEIPT");
        check(migration.legacyIdentityPresent(orphan),"orphan legacy receipt blocks duplicate fetch");
        try{
            migration.prepare(orphan,MixedMediaRecoveryStore.Kind.OPUS,
                time(15,8,0),olderCapture);
            throw new AssertionError("orphan old receipt reimport allowed");
        }catch(IOException expected){}


        // Explicit no-overwrite and corrupt local receipt classification.
        Path receipt=storeRoot.resolve(".mixed-receipts").resolve(a+".receipt");
        Files.writeString(receipt,"CORRUPT_RECEIPT",StandardCharsets.UTF_8);
        check(!migration.committedAndIntact(a),"damaged receipt cannot justify dedup complete");
        check(migration.recover().stream().anyMatch(x->x.opaqueId().equals(a)
            &&x.outcome()==MixedMediaRecoveryStore.Recovery.MANUAL_REVIEW),
            "corrupt committed receipt is manual review, no automatic GET");

        System.out.println("V4-12-MIXED-DURABLE PASS ("+checks+" assertions)");
    }
}
