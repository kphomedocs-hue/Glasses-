package com.parkarsite.g1.preview;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.parkarsite.g1.sitecapture.SiteCaptureOfflineCore;
import java.time.ZoneId;

/**
 * No connected-device controls, no vendor media downloads and no background sync.
 * Source-only preview; NOT an authorized physical-test or final transfer APK.
 */
public final class OfflinePreviewActivity extends Activity {
    private static final int INK=Color.rgb(31,43,49);
    private static final int MUTED=Color.rgb(86,103,110);
    private static final int BACKGROUND=Color.rgb(247,249,250);
    private static final int WARNING=Color.rgb(133,58,37);
    private LinearLayout content;

    private TextView line(String text,int sp,int color,boolean bold) {
        TextView view=new TextView(this);
        view.setText(text);
        view.setTextSize(sp);
        view.setTextColor(color);
        view.setPadding(0,0,0,dp(12));
        if(bold)view.setTypeface(null,android.graphics.Typeface.BOLD);
        content.addView(view,new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        return view;
    }
    private int dp(int size) {
        return (int)(size*getResources().getDisplayMetrics().density+0.5f);
    }
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BACKGROUND);
        content=new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.TOP);
        content.setPadding(dp(24),dp(48),dp(24),dp(32));
        scroll.addView(content);
        setContentView(scroll);
    }
    @Override protected void onResume() {
        super.onResume();
        content.removeAllViews();
        line("K SITE CAPTURE",16,MUTED,true);
        line("Local Archive",28,INK,true);
        line("Read-only development preview",14,MUTED,false);
        line("Media transfer disabled",20,WARNING,true);
        line("Original photographs have not been proven to remain on the glasses after import. No Bluetooth, Wi-Fi Direct or remote media request is made.",15,INK,false);
        try {
            SiteCaptureOfflineCore core=new SiteCaptureOfflineCore(
                getNoBackupFilesDir().toPath(),ZoneId.systemDefault());
            var status=core.inspectAndRecoverLocalOnly();
            line("Locally verified files: "+status.verifiedFiles(),18,INK,true);
            line("Uncertain imports: "+status.remotelyUncertain(),16,INK,false);
            line("Manual reviews: "+status.manualReview(),16,INK,false);
            line("Prepared without fetch: "+status.preparedWithoutFetch(),16,INK,false);
            if(status.locallyRecovered()>0) {
                line("Locally recovered: "+status.locallyRecovered(),16,INK,false);
            }
            line("Archive status: "+status.safeStatusCode(),13,MUTED,false);
            if(status.mode()==SiteCaptureOfflineCore.Mode.REVIEW_REQUIRED)
                line("Review required. This preview never attempts a remote retry.",14,WARNING,true);
            if(core.canReadRemoteMedia() || core.canConnectToGlasses() || core.canSyncFromGlasses())
                throw new IllegalStateException("Transfer safety contract violated");
        } catch(Exception unsafeState) {
            line("Local archive unavailable — review required. No transfer attempted.",14,WARNING,true);
        }
        line("Not a syncing APK. Physical compatibility and original retention remain unverified.",13,MUTED,false);
    }
}
