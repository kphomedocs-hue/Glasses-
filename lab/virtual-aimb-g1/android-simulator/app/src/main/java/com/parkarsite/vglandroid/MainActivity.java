package com.parkarsite.vglandroid;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Explicitly non-production, offline simulator only. No permission request. */
public final class MainActivity extends Activity {
    private TextView status;
    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);
        var root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int px=(int)(24*getResources().getDisplayMetrics().density);
        root.setPadding(px,px,px,px);
        var title=new TextView(this);
        title.setText("K G1 / ANDROID SYNTHETIC LAB");
        title.setTextSize(22);
        root.addView(title);
        var note=new TextView(this);
        note.setText("Local generated fixture only. No Bluetooth, Wi-Fi, Internet, "
                +"remote GET or glasses access. G6A OPEN; G6B BLOCKED.");
        root.addView(note);
        status=new TextView(this);
        status.setText("Local test not executed.");
        root.addView(status);
        var run=new Button(this);
        run.setText("Run synthetic private-storage self-check");
        run.setOnClickListener(v->{
            run.setEnabled(false);
            status.setText("Local policy and private storage check");
            new Thread(()->{
                String text;
                try{
                    var result=AndroidHarnessController.run(getApplicationContext());
                    text=(result.allPassed()?"SYNTHETIC PASS: ":"SYNTHETIC FAIL: ")
                            +result.summary();
                }catch(Exception error){text="SYNTHETIC FAIL: local test could not complete";}
                final String output=text;
                runOnUiThread(()->{
                    if(!isFinishing()&&!isDestroyed()){
                        status.setText(output);
                        run.setEnabled(true);
                    }
                });
            },"vgl-local-offline-check").start();
        });
        root.addView(run);
        setContentView(root);
    }
}
