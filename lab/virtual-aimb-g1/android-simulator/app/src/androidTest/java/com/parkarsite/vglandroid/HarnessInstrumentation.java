package com.parkarsite.vglandroid;

import android.app.Instrumentation;
import android.os.Bundle;
import com.parkarsite.g6a.recovery.RecoveryImportStore;
import java.io.IOException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.HexFormat;

/**
 * Single API-36 emulator ONLY. Never interacts with Bluetooth, Wi-Fi Direct,
 * vendor media, owner phone or glasses. Private synthetic fixtures persist
 * between independently invoked instrumentations and are read on restart.
 */
public final class HarnessInstrumentation extends Instrumentation {
    private static String stage="INITIAL";
    private Bundle startupArguments;
    private static final String MODE_KEY="vgl_mode";
    private static final String HARD_FETCH="crash_after_fetch_mark";
    private static final String HARD_VERIFIED="crash_after_verified_journal";
    private static final String HARD_PARTIAL="crash_mid_part_write";
    private static final String HARD_SYNC="crash_after_part_sync";
    private static final String HARD_MOVE="crash_after_final_move";
    private static final String HARD_RECEIPT="crash_after_receipt";
    private static final String RECOVER_SYNC="recover_after_part_sync";
    private static final String RECOVER_MOVE="recover_after_final_move";
    private static final String RECOVER_RECEIPT="recover_after_receipt";
    private static final String RECOVER_FETCH="recover_after_fetch_mark";
    private static final String RECOVER_VERIFIED="recover_after_verified_journal";
    private static final String RECOVER_PARTIAL="recover_mid_part_write";
    private static final byte[] SYNTHETIC_JPG={
        (byte)255,(byte)216,(byte)255,1,2,(byte)255,(byte)217
    };
    private static void must(boolean ok){if(!ok)throw new AssertionError("offline storage invariant");}
    private static String id(String fixture)throws Exception{
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
            .digest(fixture.getBytes(StandardCharsets.UTF_8)));
    }
    private RecoveryImportStore store(String name)throws Exception{
        Path root=getTargetContext().getApplicationContext().getNoBackupFilesDir()
            .toPath().resolve("vgl-instrument-"+name);
        return new RecoveryImportStore(root,ZoneId.of("Asia/Kolkata"));
    }
    private void faultChecks()throws Exception{
        // Mark a possible remote GET before a synthetic interrupted source.
        // Confirm no source is called at this barrier, and no automatic retry.
        stage="INTERRUPT_PREPARE";
        RecoveryImportStore first=store("uncertain");
        String uncertain=id("VGL-EMU-UNCERTAIN-01");
        var ticket=first.prepare(uncertain,0L);
        if(ticket.state()==RecoveryImportStore.State.PREPARED){
            final int[] reads={0};
            try{
                first.execute(ticket,SYNTHETIC_JPG.length,
                    out->{reads[0]++;out.write(SYNTHETIC_JPG);},
                    barrier->{if(barrier==RecoveryImportStore.Barrier.AFTER_FETCH_MARK)
                        throw new IOException("synthetic interrupted source");});
                throw new AssertionError("expected injected fault");
            }catch(IOException expected){}
            must(reads[0]==0);
        }
        stage="INTERRUPT_REOPEN";
        RecoveryImportStore reopened=store("uncertain");
        must(reopened.recover().stream().anyMatch(r->r.opaqueId().equals(uncertain)
            &&r.disposition()==RecoveryImportStore.Disposition.REMOTE_UNCERTAIN));
        final int[] duplicateReads={0};
        try{
            reopened.execute(ticket,SYNTHETIC_JPG.length,
                out->{duplicateReads[0]++;out.write(SYNTHETIC_JPG);},barrier->{});
            throw new AssertionError("unsafe second source read");
        }catch(IOException expected){}
        must(duplicateReads[0]==0);

        stage="VERIFIED_PREPARE";
        RecoveryImportStore second=store("verified");
        String verified=id("VGL-EMU-VERIFIED-01");
        var prepared=second.prepare(verified,0L);
        if(prepared.state()==RecoveryImportStore.State.PREPARED){
            try {
                second.execute(prepared,SYNTHETIC_JPG.length,
                    out->out.write(SYNTHETIC_JPG),
                    barrier->{if(barrier==RecoveryImportStore.Barrier.AFTER_VERIFIED_JOURNAL)
                        throw new IOException("synthetic after verification");});
                throw new AssertionError("expected verification barrier");
            }catch(IOException expected){}
        }
        stage="VERIFIED_REOPEN";
        RecoveryImportStore recovered=store("verified");
        must(recovered.recover().stream().anyMatch(r->r.opaqueId().equals(verified)
            &&(r.disposition()==RecoveryImportStore.Disposition.COMPLETE
             ||r.disposition()==RecoveryImportStore.Disposition.LOCAL_RECOVERED)));
        must(recovered.isCommittedAndIntact(verified));
    }

    /**
     * Synthetic crash-only fixture. These modes are allowed solely in the
     * separate, no-permissions emulator instrumentation APK. The first mode
     * kills its own JVM after the synced possible-fetch marker and BEFORE
     * any Source invocation; the second kills after VERIFIED journal fsync.
     *
     * This is a real test-process death (Runtime.halt), NOT an Android or
     * hardware power loss, and it never opens Bluetooth/HTTP/network.
     */
    private void hardStopAt(RecoveryImportStore.Barrier selected,String name,String fixture) throws Exception {
        stage="HARD_STOP_PREPARE";
        var store=store(name);
        var ticket=store.prepare(id(fixture),0L);
        must(ticket.state()==RecoveryImportStore.State.PREPARED);
        stage="HARD_STOP_EXECUTE";
        store.execute(ticket,SYNTHETIC_JPG.length,
            out->out.write(SYNTHETIC_JPG),
            barrier->{
                if(barrier==selected){
                    // No normal exception handler, finally block or shutdown hook.
                    Runtime.getRuntime().halt(73);
                }
            });
        throw new AssertionError("Unexpectedly survived configured hard-stop barrier");
    }

    /** Real abrupt test-process halt with three synthetic JPG bytes already written. */
    private void hardStopMidWrite() throws Exception {
        stage="HARD_STOP_PARTIAL_PREPARE";
        var data=store("hard-stop-partial");
        var ticket=data.prepare(id("VGL-EMU-HARD-PARTIAL-01"),0L);
        must(ticket.state()==RecoveryImportStore.State.PREPARED);
        stage="HARD_STOP_PARTIAL_SOURCE";
        data.execute(ticket,SYNTHETIC_JPG.length,out->{
            out.write(SYNTHETIC_JPG,0,3);
            // No close, flush, finalization or shutdown hook; kernel closes file on process death.
            Runtime.getRuntime().halt(73);
        },ignored->{});
        throw new AssertionError("Unexpectedly survived mid-part halt");
    }

    private void recoverHardStop(String name,String fixture,boolean fullBytesExpected)
            throws Exception {
        stage="HARD_STOP_RECOVER";
        var store=store(name);
        String opaque=id(fixture);
        var events=store.recover();
        if(fullBytesExpected){
            must(events.stream().anyMatch(r->r.opaqueId().equals(opaque)
                &&(r.disposition()==RecoveryImportStore.Disposition.COMPLETE
                ||r.disposition()==RecoveryImportStore.Disposition.LOCAL_RECOVERED)));
            must(store.isCommittedAndIntact(opaque));
        }else{
            must(events.stream().anyMatch(r->r.opaqueId().equals(opaque)
                &&r.disposition()==RecoveryImportStore.Disposition.REMOTE_UNCERTAIN));
            // No fresh Source is invoked during recovery.
            final int[] sourceInvocations={0};
            var stale=store.prepare(opaque,0L);
            try {
                store.execute(stale,SYNTHETIC_JPG.length,
                    out->{sourceInvocations[0]++;out.write(SYNTHETIC_JPG);},barrier->{});
                throw new AssertionError("Remote-uncertain media re-opened");
            }catch(IOException expected){}
            must(sourceInvocations[0]==0);
            if ("hard-stop-partial".equals(name)) {
                // The interrupted source wrote exactly three synthetic bytes
                // before its JVM exited. Recovery must preserve those bytes
                // for review, not delete them or declare a complete JPG.
                var quarantine=getTargetContext().getApplicationContext()
                    .getNoBackupFilesDir().toPath().resolve("vgl-instrument-"+name)
                    .resolve(".quarantine").resolve(opaque+".part");
                must(java.nio.file.Files.isRegularFile(quarantine));
                must(java.nio.file.Files.size(quarantine)==3L);
            }
        }
    }

    @Override public void onCreate(Bundle args){
        super.onCreate(args);
        startupArguments=args;
        start();
    }
    @Override public void onStart(){
        Bundle report=new Bundle();
        try{
            String mode=startupArguments==null?"":startupArguments.getString(MODE_KEY,"");
            if(HARD_FETCH.equals(mode)){
                hardStopAt(RecoveryImportStore.Barrier.AFTER_FETCH_MARK,
                    "hard-stop-fetch","VGL-EMU-HARD-FETCH-01");
            }else if(HARD_VERIFIED.equals(mode)){
                hardStopAt(RecoveryImportStore.Barrier.AFTER_VERIFIED_JOURNAL,
                    "hard-stop-verified","VGL-EMU-HARD-VERIFIED-01");
            }else if(HARD_PARTIAL.equals(mode)){
                hardStopMidWrite();
            }else if(HARD_SYNC.equals(mode)){
                hardStopAt(RecoveryImportStore.Barrier.AFTER_PART_SYNC,
                    "hard-stop-sync","VGL-EMU-HARD-SYNC-01");
            }else if(HARD_MOVE.equals(mode)){
                hardStopAt(RecoveryImportStore.Barrier.AFTER_FINAL_MOVE,
                    "hard-stop-move","VGL-EMU-HARD-MOVE-01");
            }else if(HARD_RECEIPT.equals(mode)){
                hardStopAt(RecoveryImportStore.Barrier.AFTER_RECEIPT,
                    "hard-stop-receipt","VGL-EMU-HARD-RECEIPT-01");
            }else if(RECOVER_SYNC.equals(mode)){
                recoverHardStop("hard-stop-sync","VGL-EMU-HARD-SYNC-01",false);
                report.putString("status","PASS");
                report.putString("kind","EMULATOR_ONLY_SYNTHETIC");
                report.putString("hard_stop","SYNCED_PART_UNCERTAIN_NO_REFETCH");
                finish(0,report);return;
            }else if(RECOVER_MOVE.equals(mode)){
                recoverHardStop("hard-stop-move","VGL-EMU-HARD-MOVE-01",true);
                report.putString("status","PASS");
                report.putString("kind","EMULATOR_ONLY_SYNTHETIC");
                report.putString("hard_stop","MOVED_FINAL_LOCAL_RECOVERY");
                finish(0,report);return;
            }else if(RECOVER_RECEIPT.equals(mode)){
                recoverHardStop("hard-stop-receipt","VGL-EMU-HARD-RECEIPT-01",true);
                report.putString("status","PASS");
                report.putString("kind","EMULATOR_ONLY_SYNTHETIC");
                report.putString("hard_stop","RECEIPT_LOCAL_RECOVERY");
                finish(0,report);return;
            }else if(RECOVER_PARTIAL.equals(mode)){
                recoverHardStop("hard-stop-partial","VGL-EMU-HARD-PARTIAL-01",false);
                report.putString("status","PASS");
                report.putString("kind","EMULATOR_ONLY_SYNTHETIC");
                report.putString("hard_stop","PARTIAL_BYTES_UNCERTAIN_NO_REFETCH");
                report.putString("hardware_gate","G6A OPEN; G6B BLOCKED");
                finish(0,report);
                return;
            }else if(RECOVER_FETCH.equals(mode)){
                recoverHardStop("hard-stop-fetch","VGL-EMU-HARD-FETCH-01",false);
                report.putString("status","PASS");
                report.putString("kind","EMULATOR_ONLY_SYNTHETIC");
                report.putString("hard_stop","FETCH_MARK_UNCERTAIN_NO_REFETCH");
                report.putString("hardware_gate","G6A OPEN; G6B BLOCKED");
                finish(0,report);
                return;
            }else if(RECOVER_VERIFIED.equals(mode)){
                recoverHardStop("hard-stop-verified","VGL-EMU-HARD-VERIFIED-01",true);
                report.putString("status","PASS");
                report.putString("kind","EMULATOR_ONLY_SYNTHETIC");
                report.putString("hard_stop","VERIFIED_LOCAL_RECOVERY_NO_REFETCH");
                report.putString("hardware_gate","G6A OPEN; G6B BLOCKED");
                finish(0,report);
                return;
            }else if(!mode.isEmpty()){
                throw new IllegalArgumentException("Unrecognized synthetic mode");
            }
            stage="FIRST_APP_PRIVATE_IMPORT";
            var a=AndroidHarnessController.run(getTargetContext());
            stage="SECOND_APP_PRIVATE_REOPEN";
            var b=AndroidHarnessController.run(getTargetContext());
            must(a.allPassed()&&b.allPassed());
            stage="CRASH_RECOVERY_PROBES";
            faultChecks();
            report.putString("kind","EMULATOR_ONLY_SYNTHETIC");
            report.putString("status","PASS");
            report.putString("storage","PRIVATE_RECEIPT_REOPEN_OK");
            report.putString("interrupt","REMOTE_UNCERTAIN_AND_LOCAL_RECOVERY_OK");
            report.putString("hardware_gate","G6A OPEN; G6B BLOCKED");
            finish(0,report);
        }catch(Throwable failure){
            report.putString("status","FAIL");
            report.putString("kind","EMULATOR_ONLY_SYNTHETIC");
            report.putString("error_kind",failure.getClass().getSimpleName());
            report.putString("stage",stage);
            report.putString("hardware_gate","G6A OPEN; G6B BLOCKED");
            finish(1,report);
        }
    }
}
