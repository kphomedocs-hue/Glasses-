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
        RecoveryImportStore recovered=store("verified");
        must(recovered.recover().stream().anyMatch(r->r.opaqueId().equals(verified)
            &&(r.disposition()==RecoveryImportStore.Disposition.COMPLETE
             ||r.disposition()==RecoveryImportStore.Disposition.LOCAL_RECOVERED)));
        must(recovered.isCommittedAndIntact(verified));
    }
    @Override public void onCreate(Bundle args){super.onCreate(args);start();}
    @Override public void onStart(){
        Bundle report=new Bundle();
        try{
            var a=AndroidHarnessController.run(getTargetContext());
            var b=AndroidHarnessController.run(getTargetContext());
            must(a.allPassed()&&b.allPassed());
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
            report.putString("hardware_gate","G6A OPEN; G6B BLOCKED");
            finish(1,report);
        }
    }
}
