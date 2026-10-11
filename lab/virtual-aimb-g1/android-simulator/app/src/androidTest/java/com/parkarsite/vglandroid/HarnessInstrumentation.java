package com.parkarsite.vglandroid;

import android.app.Instrumentation;
import android.os.Bundle;

/**
 * Source-only Android test hook. No instrumentation APK assembled by CI.
 * Real Android storage tests require future explicit emulator-only authorization.
 */
public final class HarnessInstrumentation extends Instrumentation {
    @Override public void onCreate(Bundle arguments){super.onCreate(arguments);start();}
    @Override public void onStart(){
        var result=new Bundle();
        try {
            var a=AndroidHarnessController.run(getTargetContext());
            var b=AndroidHarnessController.run(getTargetContext());
            boolean passed=a.allPassed()&&b.allPassed();
            result.putString("kind","SYNTHETIC_ONLY");
            result.putString("status",passed?"PASS":"FAIL");
            result.putBoolean("local_second_run_idempotent",passed);
            result.putString("hardware_gate","G6A OPEN; G6B BLOCKED");
            finish(passed?0:1,result);
        }catch(Exception failure){
            result.putString("status","FAIL: PRIVATE_STORAGE_OR_POLICY");
            finish(1,result);
        }
    }
}
