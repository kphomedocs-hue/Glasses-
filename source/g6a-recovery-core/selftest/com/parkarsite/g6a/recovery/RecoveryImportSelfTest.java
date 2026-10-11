package com.parkarsite.g6a.recovery;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.ZoneId;
import java.util.*;

public final class RecoveryImportSelfTest {
    private static final ZoneId ZONE=ZoneId.of("Asia/Kolkata");
    private static final long TIME=1791705600000L;
    private static final byte[] JPEG={ (byte)0xff,(byte)0xd8,(byte)0xff,1,2,3,(byte)0xff,(byte)0xd9};
    private static int assertions=0;

    public static void main(String[] args)throws Exception{
        testSuccessAndRepeatedOpen();
        testPrepareThenReopen();
        testErrorBeforeFetchDoesNotConsume();
        for(RecoveryImportStore.Barrier b:RecoveryImportStore.Barrier.values())testCrashAt(b);
        testLengthMismatchCannotRetry();
        testRejectedJpegCannotRetry();
        testRemoteReadFailureCannotRetry();
        testCorruptionNeverPromotes();
        testReceiptCorruptionFailsClosed();
        testMultiEntryReservesUniqueOrdinals();
        testPartDoesNotGoAwayOnRestart();
        testNoRawIdentityInPersistence();
        System.out.println("PASS G6A CONSUMPTIVE RECOVERY SELFTEST assertions="+assertions);
    }
    private static void testSuccessAndRepeatedOpen()throws Exception{
        withStore("success",(s,p)->{
            String id=id("success");
            var t=s.prepare(id,TIME);
            var done=s.execute(t,JPEG.length,out->out.write(JPEG),ignored->{});
            check(done.state()==RecoveryImportStore.State.COMMITTED,"success state");
            check(s.isCommittedAndIntact(id),"success integrity");
            var r=new RecoveryImportStore(p,ZONE);
            check(r.isCommittedAndIntact(id),"restart integrity");
            check(r.recover().get(0).disposition()==RecoveryImportStore.Disposition.COMPLETE,"restart verified");
            throwsFailure(()->r.execute(t,JPEG.length,out->{throw new AssertionError("duplicate byte read");},ignored->{}),"duplicate blocked");
        });
    }
    private static void testPrepareThenReopen()throws Exception{
        withStore("prepared",(s,p)->{
            String id=id("prepared");
            var t=s.prepare(id,TIME);
            var r=new RecoveryImportStore(p,ZONE);
            check(r.recover().get(0).disposition()==RecoveryImportStore.Disposition.NOT_FETCHED,"prepared no read");
            check(r.prepare(id,TIME).relativePath().equals(t.relativePath()),"prepare stable");
        });
    }
    private static void testErrorBeforeFetchDoesNotConsume()throws Exception{
        withStore("precheck",(s,p)->{
            String id=id("precheck");var t=s.prepare(id,TIME);
            throwsFailure(()->s.execute(t,0,out->{throw new AssertionError("invalid length must not invoke source");},ignored->{}),"invalid length blocks");
            check(s.recover().get(0).disposition()==RecoveryImportStore.Disposition.NOT_FETCHED,"precheck no GET marker");
        });
    }
    private static void testCrashAt(RecoveryImportStore.Barrier barrier)throws Exception{
        withStore("crash-"+barrier,(s,p)->{
            String id=id(barrier.name());var t=s.prepare(id,TIME);
            throwsFailure(()->s.execute(t,JPEG.length,out->out.write(JPEG),at->{if(at==barrier)throw new IOException("simulated process crash at "+barrier);}),"fault injected "+barrier);
            var r=new RecoveryImportStore(p,ZONE);
            var states=r.recover();check(states.size()==1,"one entry "+barrier);
            var actual=states.get(0).disposition();
            if(barrier==RecoveryImportStore.Barrier.AFTER_FETCH_MARK || barrier==RecoveryImportStore.Barrier.AFTER_PART_SYNC) {
                check(actual==RecoveryImportStore.Disposition.REMOTE_UNCERTAIN,"uncertain after "+barrier);
                check(!r.isCommittedAndIntact(id),"no phantom complete "+barrier);
                throwsFailure(()->r.execute(t,JPEG.length,out->{throw new AssertionError("must not reread");},ignored->{}),"no automatic retrigger "+barrier);
            }else{
                check(actual==RecoveryImportStore.Disposition.LOCAL_RECOVERED||actual==RecoveryImportStore.Disposition.COMPLETE,"local recovery "+barrier);
                check(r.isCommittedAndIntact(id),"recovered bytes and receipt "+barrier);
                throwsFailure(()->r.execute(t,JPEG.length,out->{throw new AssertionError("must not reread");},ignored->{}),"no retrigger recovered "+barrier);
            }
        });
    }
    private static void testLengthMismatchCannotRetry()throws Exception{
        withStore("short",(s,p)->{
            String id=id("short");var t=s.prepare(id,TIME);
            throwsFailure(()->s.execute(t,JPEG.length+1,out->out.write(JPEG),ignored->{}),"short read failure");
            var r=new RecoveryImportStore(p,ZONE);
            check(r.recover().get(0).disposition()==RecoveryImportStore.Disposition.REMOTE_UNCERTAIN,"short remains uncertain");
            check(Files.exists(p.resolve(".quarantine").resolve(id+".part")),"partial preserved");
        });
    }
    private static void testRejectedJpegCannotRetry()throws Exception{
        withStore("badjpg",(s,p)->{
            String id=id("badjpg");var t=s.prepare(id,TIME);
            byte[] bad={1,2,3,4,5,6,7,8};
            throwsFailure(()->s.execute(t,bad.length,out->out.write(bad),ignored->{}),"JPG validation blocks");
            check(s.recover().get(0).disposition()==RecoveryImportStore.Disposition.REMOTE_UNCERTAIN,"bad JPG uncertain");
        });
    }
    private static void testRemoteReadFailureCannotRetry()throws Exception{
        withStore("io",(s,p)->{
            String id=id("io");var t=s.prepare(id,TIME);
            throwsFailure(()->s.execute(t,JPEG.length,out->{out.write(JPEG,0,3);throw new IOException("network died");},ignored->{}),"mid-stream error");
            var r=new RecoveryImportStore(p,ZONE);
            check(r.recover().get(0).disposition()==RecoveryImportStore.Disposition.REMOTE_UNCERTAIN,"read error uncertain");
            throwsFailure(()->r.execute(t,JPEG.length,out->out.write(JPEG),ignored->{}),"no second GET");
        });
    }
    private static void testCorruptionNeverPromotes()throws Exception{
        withStore("corrupt",(s,p)->{
            String id=id("corrupt");var t=s.prepare(id,TIME);
            throwsFailure(()->s.execute(t,JPEG.length,out->out.write(JPEG),at->{if(at==RecoveryImportStore.Barrier.AFTER_VERIFIED_JOURNAL)throw new IOException("crash");}),"verified crash");
            Files.write(p.resolve(".quarantine").resolve(id+".part"),new byte[]{1,2,3},StandardOpenOption.TRUNCATE_EXISTING);
            var r=new RecoveryImportStore(p,ZONE);
            check(r.recover().get(0).disposition()==RecoveryImportStore.Disposition.MANUAL_REVIEW,"corrupt local not committed");
            check(!r.isCommittedAndIntact(id),"corrupt not complete");
        });
    }
    private static void testReceiptCorruptionFailsClosed()throws Exception{
        withStore("receipt-corruption",(s,p)->{
            String id=id("receipt-corruption");var t=s.prepare(id,TIME);
            s.execute(t,JPEG.length,out->out.write(JPEG),ignored->{});
            Files.writeString(p.resolve(".receipts").resolve(id+".receipt"),"corrupt",StandardOpenOption.TRUNCATE_EXISTING);
            check(!s.isCommittedAndIntact(id),"corrupt receipt fails check");
            throwsFailure(()->s.recover(),"corrupt existing receipt refuses overwrite");
        });
    }
    private static void testMultiEntryReservesUniqueOrdinals()throws Exception{
        withStore("numbering",(s,p)->{
            var a=s.prepare(id("a"),TIME);var b=s.prepare(id("b"),TIME);
            check(!a.relativePath().equals(b.relativePath()),"reserved filenames unique");
            check(a.relativePath().endsWith("0001.jpg"),"first number");
            check(b.relativePath().endsWith("0002.jpg"),"second number");
            check(s.prepare(id("a"),TIME).relativePath().equals(a.relativePath()),"idempotent reserve");
        });
    }
    private static void testPartDoesNotGoAwayOnRestart()throws Exception{
        withStore("quarantine",(s,p)->{
            String id=id("quarantine");var t=s.prepare(id,TIME);
            throwsFailure(()->s.execute(t,JPEG.length,out->{out.write(JPEG,0,2);throw new IOException("drop");},ignored->{}),"drop");
            Path part=p.resolve(".quarantine").resolve(id+".part");
            check(Files.exists(part),"part exists");
            new RecoveryImportStore(p,ZONE).recover();
            check(Files.exists(part),"recovery never deletes uncertain part");
        });
    }
    private static void testNoRawIdentityInPersistence()throws Exception{
        withStore("privacy",(s,p)->{
            String id=id("private/real/path.jpg");var t=s.prepare(id,TIME);
            s.execute(t,JPEG.length,out->out.write(JPEG),ignored->{});
            try(var files=Files.walk(p)){
                for(Path file:(Iterable<Path>)files.filter(Files::isRegularFile)::iterator){
                    String name=file.getFileName().toString();
                    if(name.equals(".single-writer.lock")||name.endsWith(".jpg"))continue;
                    String contents=Files.readString(file);
                    check(!contents.contains("private/real/path.jpg"),"private remote path not persisted");
                }
            }
        });
    }
    private static String id(String s)throws Exception {
        MessageDigest m=MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(m.digest(s.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    }
    private interface Experiment {void run(RecoveryImportStore s,Path p)throws Exception;}
    private static void withStore(String scenario,Experiment run)throws Exception{
        Path root=Files.createTempDirectory("g6a-recovery-"+scenario+"-");
        try{run.run(new RecoveryImportStore(root,ZONE),root);}finally{try(var paths=Files.walk(root)){
            paths.sorted(Comparator.reverseOrder()).forEach(p->{try{Files.deleteIfExists(p);}catch(IOException e){throw new UncheckedIOException(e);}});
        }}
    }
    private interface Throwing {void run()throws Exception;}
    private static void throwsFailure(Throwing f,String note)throws Exception{boolean failed=false;try{f.run();}catch(Exception expected){failed=true;}check(failed,note);}
    private static void check(boolean yes,String note){assertions++;if(!yes)throw new AssertionError(note);}
}
