package com.parkarsite.vglandroid;

import android.app.Instrumentation;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import com.parkarsite.g6a.recovery.RecoveryImportStore;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.time.ZoneId;
import java.util.HexFormat;

/**
 * THREE independent emulator-only hard process termination checks.
 * The fixtures are tiny generated marker bytes; no URL, radio, Bluetooth,
 * real media, external storage, background automation or Android permissions.
 *
 * This class is never packaged into the final application source; it lives
 * under app/src/androidTest, installed only to a single approved AVD.
 */
public final class KillProofProbe {
    private static final String LOG_TAG = "VGL_KILL_PROBE";
    private static final byte[] SYNTHETIC = {
        (byte)0xff,(byte)0xd8,(byte)0xff,1,2,(byte)0xff,(byte)0xd9
    };
    private KillProofProbe() {}

    private static void requireEmulator() {
        String hardware = Build.HARDWARE == null ? "" : Build.HARDWARE.toLowerCase(java.util.Locale.ROOT);
        if (!("ranchu".equals(hardware) || "goldfish".equals(hardware)))
            throw new IllegalStateException("Test forbidden outside emulator");
        if (Build.VERSION.SDK_INT != 36)
            throw new IllegalStateException("API 36 emulator required");
    }
    private static String opaque(String mode) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(
            ("VGL-EMU-KILL-PROBE-SYNTHETIC-ONLY-"+mode).getBytes(StandardCharsets.UTF_8)));
    }
    private static RecoveryImportStore store(Instrumentation inst,String mode) throws Exception {
        Path root=inst.getTargetContext().getApplicationContext()
            .getNoBackupFilesDir().toPath().resolve("vgl-kill-"+mode);
        return new RecoveryImportStore(root,ZoneId.of("Asia/Kolkata"));
    }
    private static void require(boolean yes) {
        if (!yes) throw new AssertionError("Synthetic emulator crash invariant");
    }
    private static void hardStop(String mode) {
        // Only an enum-like fixed string, no device identifiers/paths/secrets.
        Log.i(LOG_TAG,"SIMULATED_ABRUPT_TERMINATION_"+mode);
        Runtime.getRuntime().halt(37);
        throw new AssertionError("Should never return from process termination");
    }
    public static void run(Instrumentation instrument, String action) {
        Bundle result=new Bundle();
        result.putString("kind","API36_EMULATOR_HARD_PROCESS_STOP_ONLY");
        try {
            requireEmulator();
            if (action==null) throw new IllegalArgumentException("Missing test action");
            boolean recover=action.startsWith("recover-");
            String mode=recover?action.substring(8):action.startsWith("crash-")?action.substring(6):"";
            if (!("fetch".equals(mode)||"partial".equals(mode)||"verified".equals(mode)))
                throw new IllegalArgumentException("Invalid synthetic fixture");
            RecoveryImportStore instance=store(instrument,mode);
            String id=opaque(mode);
            if(!recover) {
                RecoveryImportStore.Ticket ticket=instance.prepare(id,0L);
                require(ticket.state()==RecoveryImportStore.State.PREPARED);
                instance.execute(ticket,SYNTHETIC.length,output->{
                    if("partial".equals(mode)){
                        // Kill the PROCESS after actual bytes have been written into
                        // the .part file, without normal flush/finally.
                        output.write(SYNTHETIC,0,3);
                        hardStop(mode);
                    }
                    output.write(SYNTHETIC);
                },barrier->{
                    if("fetch".equals(mode)
                           &&barrier==RecoveryImportStore.Barrier.AFTER_FETCH_MARK)
                        hardStop(mode);
                    if("verified".equals(mode)
                           &&barrier==RecoveryImportStore.Barrier.AFTER_VERIFIED_JOURNAL)
                        hardStop(mode);
                });
                throw new AssertionError("Hard stop did not terminate process");
            }

            var cases=instance.recover();
            require(cases.size()==1);
            var actual=cases.get(0);
            require(actual.opaqueId().equals(id));
            if("verified".equals(mode)){
                require(actual.disposition()==RecoveryImportStore.Disposition.LOCAL_RECOVERED);
                require(instance.isCommittedAndIntact(id));
                require(instance.recover().get(0).disposition()
                    ==RecoveryImportStore.Disposition.COMPLETE);
            }else{
                require(actual.disposition()==RecoveryImportStore.Disposition.REMOTE_UNCERTAIN);
                require(!instance.isCommittedAndIntact(id));
                if("partial".equals(mode)){
                    Path quarantine=instrument.getTargetContext().getApplicationContext()
                        .getNoBackupFilesDir().toPath().resolve("vgl-kill-"+mode)
                        .resolve(".quarantine").resolve(id+".part");
                    require(Files.isRegularFile(quarantine));
                    require(Files.size(quarantine)>0);
                }
                final int[] sourceAttempts={0};
                try {
                    instance.execute(instance.prepare(id,0L),SYNTHETIC.length,
                      output->{sourceAttempts[0]++;output.write(SYNTHETIC);},
                      ignored->{});
                    throw new AssertionError("Second GET would have been permitted");
                }catch(IOException expected) {}
                require(sourceAttempts[0]==0);
            }
            result.putString("status","PASS");
            result.putString("mode","RECOVERED_"+mode.toUpperCase(java.util.Locale.ROOT));
            result.putString("hardware_gate","G6A OPEN; G6B BLOCKED");
            instrument.finish(0,result);
        } catch(Throwable failure) {
            result.putString("status","FAIL");
            result.putString("error_kind",failure.getClass().getSimpleName());
            result.putString("hardware_gate","G6A OPEN; G6B BLOCKED");
            instrument.finish(1,result);
        }
    }
}
