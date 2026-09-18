package com.parkarsite.g1capture;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.parkarsite.g1capture.core.ProbeRunner;
import com.parkarsite.g1capture.g1.FakeAimbG1Transport;
import com.parkarsite.g1capture.storage.FileImportLedger;
import com.parkarsite.g1capture.storage.FileMediaArchive;

import java.io.File;
import java.time.ZoneId;
import java.util.List;

public final class MainActivity extends Activity {
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView status = findViewById(R.id.status);
        TextView output = findViewById(R.id.output);
        Button run = findViewById(R.id.runFakeProbe);

        run.setOnClickListener(v -> {
            status.setText("Running fake import…");
            run.setEnabled(false);
            new Thread(() -> {
                try {
                    File base = getExternalFilesDir(null);
                    if (base == null) throw new IllegalStateException("Android external-files directory unavailable");
                    File root = new File(base, "AIMB-G1");
                    ProbeRunner runner = new ProbeRunner(
                            new FakeAimbG1Transport(),
                            new FileMediaArchive(root, ZoneId.systemDefault()),
                            new FileImportLedger(root));
                    List<File> files = runner.run();
                    StringBuilder b = new StringBuilder();
                    if (files.isEmpty()) b.append("No new files (dedup working).\n");
                    for (File f : files) b.append(f.getAbsolutePath()).append('\n');
                    runOnUiThread(() -> {
                        status.setText("Fake import complete");
                        output.setText(b.toString());
                        run.setEnabled(true);
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        status.setText("Probe failed");
                        output.setText(e.toString());
                        run.setEnabled(true);
                    });
                }
            }).start();
        });
    }
}
