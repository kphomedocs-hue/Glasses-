package com.parkarsite.g1capturevisibilityprobe54;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@SuppressLint("MissingPermission")
public final class MainActivity extends Activity {
    private static final int REQ_PERMISSION = 5301;
    private static final long CONNECT_TIMEOUT_MS = 30000L;
    private static final long QUERY_TIMEOUT_MS = 10000L;
    private static final long POST_CAPTURE_OBSERVE_MS = 60000L;
    private static final String TARGET_NAME = "AIMB-G1";

    private static final UUID CYAN_SERVICE = UUID.fromString("de5bf728-d711-4e47-af26-65e3012a5dc7");
    private static final UUID CYAN_NOTIFY = UUID.fromString("de5bf729-d711-4e47-af26-65e3012a5dc7");
    private static final UUID CYAN_WRITE = UUID.fromString("de5bf72a-d711-4e47-af26-65e3012a5dc7");
    private static final UUID CLIENT_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private static final byte[] MEDIA_COUNT_PAYLOAD = new byte[]{0x02,0x04};

    private enum Stage { IDLE, BASELINE_QUERY, WAIT_CAPTURE, POST_CAPTURE_OBSERVE, FINAL_QUERY, COMPLETE }

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final StringBuilder report = new StringBuilder();
    private final LinkedHashSet<Integer> observed73Ids = new LinkedHashSet<>();

    private BluetoothAdapter adapter;
    private BluetoothGatt gatt;
    private BluetoothGattCharacteristic writer;

    private TextView statusView;
    private TextView reportView;
    private Button runButton;
    private Button copyButton;
    private Button shareButton;

    private Stage stage = Stage.IDLE;
    private Runnable timeoutTask;
    private Runnable observeTask;
    private boolean reportFinished;
    private int queryWriteCount;
    private boolean queryWriteCallbackSucceeded;
    private boolean queryResponseReceived;
    private Inventory pendingInventory;
    private Inventory baselineInventory;
    private Inventory finalInventory;
    private int passive73Count;
    private int passive01Count;
    private int passive01Logged;
    private int watch73Count;
    private int watch01Count;
    private int watch01Logged;
    private int maxPassiveImage = -1;
    private boolean passiveVisibilitySeen;

    @Override protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        buildUi();
        BluetoothManager manager=(BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        adapter=manager==null?null:manager.getAdapter();
        renderIdle();
    }

    private void buildUi(){
        int pad=dp(18);
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad,pad,pad,pad);

        TextView title=new TextView(this);
        title.setText("K G1 Capture Visibility Probe v0.5.4");
        title.setTextSize(22f);
        root.addView(title);

        TextView note=new TextView(this);
        note.setText("BLE-only G5 visibility diagnostic. Baseline inventory once, keep notifications connected while you take ONE photo, observe passive 0x73 events, then one final inventory recheck. No P2P, Wi-Fi or HTTP.");
        note.setPadding(0,dp(8),0,dp(12));
        root.addView(note);

        runButton=new Button(this);
        runButton.setText("Start BLE baseline");
        runButton.setOnClickListener(v->onRunButton());
        root.addView(runButton);

        copyButton=new Button(this);
        copyButton.setText("Copy report");
        copyButton.setEnabled(false);
        copyButton.setOnClickListener(v->copyReport());
        root.addView(copyButton);

        shareButton=new Button(this);
        shareButton.setText("Share report");
        shareButton.setEnabled(false);
        shareButton.setOnClickListener(v->shareReport());
        root.addView(shareButton);

        statusView=new TextView(this);
        statusView.setPadding(0,dp(10),0,dp(10));
        root.addView(statusView);

        reportView=new TextView(this);
        reportView.setTextIsSelectable(true);
        ScrollView scroll=new ScrollView(this);
        scroll.addView(reportView);
        root.addView(scroll,new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1f));
        setContentView(root);
    }

    private void renderIdle(){
        statusView.setText("Ready. Force-stop Cyan Glasses, keep AIMB-G1 paired, then start the BLE baseline.");
    }

    private void onRunButton(){
        if(!hasPermission()){
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT},REQ_PERMISSION);
            return;
        }
        if(stage==Stage.WAIT_CAPTURE){
            armCaptureWatch();
            return;
        }
        if(stage==Stage.IDLE||stage==Stage.COMPLETE){
            startBaseline();
        }
    }

    @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if(requestCode==REQ_PERMISSION){
            if(hasPermission())onRunButton();
            else statusView.setText("Bluetooth Connect permission is required.");
        }
    }

    private boolean hasPermission(){
        return android.os.Build.VERSION.SDK_INT<31 || checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)==PackageManager.PERMISSION_GRANTED;
    }

    private void startBaseline(){
        resetAll();
        if(adapter==null){finishFailure("Bluetooth adapter unavailable.");return;}
        try{
            if(!adapter.isEnabled()){finishFailure("Bluetooth is off.");return;}
        }catch(SecurityException e){finishFailure("Bluetooth permission error.");return;}

        BluetoothDevice target=findUniqueTarget();
        if(target==null)return;

        append("K G1 CAPTURE VISIBILITY PROBE REPORT");
        append("Generated: "+isoNow());
        append("App version: 0.5.3");
        append("Gate: G5 capture visibility / BLE only");
        append("Proprietary writes allowed: EXACTLY TWO total, both 0x41 / 02 04");
        append("BLE connection: ONE continuous connection through user capture");
        append("Passive notifications: sanitized 0x73 event IDs only; event 0x01 media counts/config only");
        append("Post-capture observation window: 60 seconds");
        append("P2P/Wi-Fi/HTTP/media GET: NOT IMPLEMENTED");
        append("Credential/filename/path/raw-frame logging: NOT IMPLEMENTED");
        append("");
        append("BONDED TARGET CHECK");
        append("Bonded AIMB-G1-family matches: 1");
        append("Target name: "+safeName(safeGetName(target)));
        append("Bluetooth address: not logged");

        stage=Stage.BASELINE_QUERY;
        runButton.setEnabled(false);
        copyButton.setEnabled(false);
        shareButton.setEnabled(false);
        setStatus("Connecting BLE and subscribing…");
        scheduleTimeout(CONNECT_TIMEOUT_MS,()->finishFailure("GATT/subscription timeout."));
        try{
            gatt=target.connectGatt(this,false,gattCallback,BluetoothDevice.TRANSPORT_LE);
        }catch(Exception e){
            finishFailure("BLE connect request failed.");
        }
    }

    private BluetoothDevice findUniqueTarget(){
        Set<BluetoothDevice> bonded;
        try{bonded=adapter.getBondedDevices();}catch(SecurityException e){finishFailure("Cannot read bonded devices.");return null;}
        BluetoothDevice found=null;
        int matches=0;
        for(BluetoothDevice d:bonded){
            if(matchesName(safeGetName(d))){
                matches++;
                found=d;
            }
        }
        if(matches!=1){
            finishFailure("Expected exactly one bonded AIMB-G1-family device; found "+matches+".");
            return null;
        }
        return found;
    }

    private final BluetoothGattCallback gattCallback=new BluetoothGattCallback(){
        @Override public void onConnectionStateChange(BluetoothGatt callbackGatt,int status,int newState){
            if(newState==BluetoothProfile.STATE_CONNECTED){
                appendThreadSafe("GATT: connected");
                try{callbackGatt.discoverServices();}catch(Exception e){handler.post(()->finishFailure("Service discovery start failed."));}
            }else if(newState==BluetoothProfile.STATE_DISCONNECTED){
                handler.post(()->{
                    if(!reportFinished&&stage!=Stage.COMPLETE)finishFailure("GATT disconnected before diagnostic completion.");
                });
            }
        }

        @Override public void onServicesDiscovered(BluetoothGatt callbackGatt,int status){
            handler.post(()->{
                if(reportFinished)return;
                if(status!=BluetoothGatt.GATT_SUCCESS){finishFailure("GATT service discovery failed.");return;}
                BluetoothGattService svc=callbackGatt.getService(CYAN_SERVICE);
                BluetoothGattCharacteristic notify=svc==null?null:svc.getCharacteristic(CYAN_NOTIFY);
                writer=svc==null?null:svc.getCharacteristic(CYAN_WRITE);
                if(svc==null||notify==null||writer==null){finishFailure("Cyan service/notify/write not fully present.");return;}
                append("CYAN SERVICE/NOTIFY/WRITE: PRESENT");
                try{
                    boolean local=callbackGatt.setCharacteristicNotification(notify,true);
                    append("Notification subscription start: "+(local?"SUCCESS":"FAILED"));
                    if(!local){finishFailure("Local notification enable failed.");return;}
                    BluetoothGattDescriptor d=notify.getDescriptor(CLIENT_CONFIG);
                    if(d==null){finishFailure("CCCD missing.");return;}
                    d.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    if(!callbackGatt.writeDescriptor(d)){finishFailure("CCCD write did not start.");}
                }catch(Exception e){finishFailure("Notification subscription failed.");}
            });
        }

        @Override public void onDescriptorWrite(BluetoothGatt callbackGatt,BluetoothGattDescriptor descriptor,int status){
            handler.post(()->{
                if(reportFinished)return;
                if(status!=BluetoothGatt.GATT_SUCCESS){finishFailure("CCCD write failed.");return;}
                cancelTimeout();
                append("Notification subscription: SUCCESS");
                sendInventoryQuery(Stage.BASELINE_QUERY);
            });
        }

        @Override public void onCharacteristicWrite(BluetoothGatt callbackGatt,BluetoothGattCharacteristic characteristic,int status){
            handler.post(()->{
                if(reportFinished)return;
                if(!CYAN_WRITE.equals(characteristic.getUuid()))return;
                append("Inventory BLE write callback: "+(status==BluetoothGatt.GATT_SUCCESS?"SUCCESS":Integer.toString(status)));
                if(status!=BluetoothGatt.GATT_SUCCESS){finishFailure("Inventory write callback failed.");return;}
                queryWriteCallbackSucceeded=true;
                maybeCompleteQuery();
            });
        }

        @Override public void onCharacteristicChanged(BluetoothGatt callbackGatt,BluetoothGattCharacteristic characteristic){
            handleNotify(characteristic,characteristic.getValue());
        }

        @Override public void onCharacteristicChanged(BluetoothGatt callbackGatt,BluetoothGattCharacteristic characteristic,byte[] value){
            handleNotify(characteristic,value);
        }
    };

    private void sendInventoryQuery(Stage queryStage){
        if(reportFinished)return;
        if(queryWriteCount>=2){finishFailure("Inventory write limit exceeded.");return;}
        if(queryStage!=Stage.BASELINE_QUERY&&queryStage!=Stage.FINAL_QUERY){finishFailure("Invalid inventory-query stage.");return;}

        stage=queryStage;
        queryWriteCallbackSucceeded=false;
        queryResponseReceived=false;
        pendingInventory=null;
        queryWriteCount++;

        append("");
        append(queryStage==Stage.BASELINE_QUERY?"BASELINE INVENTORY QUERY":"FINAL INVENTORY RECHECK");
        append("Command: 0x41 / 02 04");
        append("Query number: "+queryWriteCount+" of 2");
        append("No retry policy: TRUE");

        byte[] frame=frame41(MEDIA_COUNT_PAYLOAD);
        BluetoothGatt localGatt=gatt;
        BluetoothGattCharacteristic localWriter=writer;
        if(localGatt==null||localWriter==null){finishFailure("GATT writer unavailable.");return;}
        localWriter.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        localWriter.setValue(frame);
        boolean started;
        try{started=localGatt.writeCharacteristic(localWriter);}catch(Exception e){started=false;}
        append("Inventory write start: "+(started?"SUCCESS":"FAILED"));
        if(!started){finishFailure("Inventory write did not start.");return;}
        scheduleTimeout(QUERY_TIMEOUT_MS,()->finishFailure("No complete inventory write/response handshake."));
    }

    private void handleNotify(BluetoothGattCharacteristic characteristic,byte[] value){
        if(characteristic==null||!CYAN_NOTIFY.equals(characteristic.getUuid())||reportFinished)return;
        final byte[] frame=value==null?new byte[0]:value.clone();
        handler.post(()->{
            if(reportFinished||!validateFrame(frame))return;
            int cmd=frame[1]&255;
            if(cmd==0x41&&(stage==Stage.BASELINE_QUERY||stage==Stage.FINAL_QUERY)){
                Inventory inv=parse41Inventory(frame);
                if(inv!=null){
                    pendingInventory=inv;
                    queryResponseReceived=true;
                    append("Inventory response: VALID");
                    maybeCompleteQuery();
                }
                return;
            }
            if(cmd==0x73){
                passive73Count++;
                if(stage==Stage.POST_CAPTURE_OBSERVE)watch73Count++;
                int eventId=frame.length>6?(frame[6]&255):-1;
                if(eventId>=0)observed73Ids.add(eventId);
                if(eventId==0x01){
                    Inventory inv=parse73Inventory(frame);
                    if(inv!=null){
                        passive01Count++;
                        if(stage==Stage.POST_CAPTURE_OBSERVE){
                            watch01Count++;
                            if(maxPassiveImage<inv.imageCount)maxPassiveImage=inv.imageCount;
                            if(baselineInventory!=null&&inv.imageCount>baselineInventory.imageCount)passiveVisibilitySeen=true;
                            if(watch01Logged<10){
                                watch01Logged++;
                                append("Watch-window 0x73/0x01 inventory: images="+inv.imageCount+", videos="+inv.videoCount+", recordings="+inv.recordCount+", configFileType="+inv.configFileType+", AP-only="+tri(inv.onlySupportApKnown,inv.onlySupportApImport));
                            }
                        }
                    }
                }
            }
        });
    }

    private void maybeCompleteQuery(){
        if(reportFinished||!queryWriteCallbackSucceeded||!queryResponseReceived||pendingInventory==null)return;
        cancelTimeout();
        Inventory done=pendingInventory;
        pendingInventory=null;
        append("Inventory write/response handshake: COMPLETE");
        append("Image count: "+done.imageCount);
        append("Video count: "+done.videoCount);
        append("Recording count: "+done.recordCount);
        append("Config file type: "+done.configFileType);
        append("Only-support-AP-import: "+tri(done.onlySupportApKnown,done.onlySupportApImport));

        if(stage==Stage.BASELINE_QUERY){
            baselineInventory=done;
            append("");
            append("BASELINE COMPLETE");
            append("Keep this app open and BLE connected.");
            append("Next, arm the 60-second observation window BEFORE taking the photo.");
            stage=Stage.WAIT_CAPTURE;
            runButton.setText("Arm 60s watch — then capture ONE photo");
            runButton.setEnabled(true);
            setStatus("Baseline complete. Tap Arm first; then capture exactly ONE photo during the 60-second window.");
        }else if(stage==Stage.FINAL_QUERY){
            finalInventory=done;
            finishVisibilityReport();
        }
    }

    private void armCaptureWatch(){
        if(stage!=Stage.WAIT_CAPTURE||baselineInventory==null||reportFinished)return;
        stage=Stage.POST_CAPTURE_OBSERVE;
        watch73Count=0;
        watch01Count=0;
        watch01Logged=0;
        passiveVisibilitySeen=false;
        maxPassiveImage=baselineInventory.imageCount;
        runButton.setEnabled(false);
        append("");
        append("ARMED CAPTURE VISIBILITY WATCH");
        append("BLE remained connected: YES");
        append("Transfer mode: OFF");
        append("Observation window: 60 seconds");
        append("Instruction: capture EXACTLY ONE disposable photo NOW, during this armed window");
        append("Additional proprietary writes during watch: 0");
        setStatus("ARMED for 60 seconds. Capture exactly ONE photo NOW; do not take a second photo.");
        observeTask=()->{
            observeTask=null;
            if(reportFinished||stage!=Stage.POST_CAPTURE_OBSERVE)return;
            sendInventoryQuery(Stage.FINAL_QUERY);
        };
        handler.postDelayed(observeTask,POST_CAPTURE_OBSERVE_MS);
    }

    private void finishVisibilityReport(){
        if(reportFinished||baselineInventory==null||finalInventory==null)return;
        int imageDelta=finalInventory.imageCount-baselineInventory.imageCount;
        int videoDelta=finalInventory.videoCount-baselineInventory.videoCount;
        int recordDelta=finalInventory.recordCount-baselineInventory.recordCount;

        append("");
        append("VISIBILITY SUMMARY");
        append("Baseline counts: images="+baselineInventory.imageCount+", videos="+baselineInventory.videoCount+", recordings="+baselineInventory.recordCount);
        append("Final counts: images="+finalInventory.imageCount+", videos="+finalInventory.videoCount+", recordings="+finalInventory.recordCount);
        append("Final image delta: "+imageDelta);
        append("Final video delta: "+videoDelta);
        append("Final recording delta: "+recordDelta);
        append("All-session 0x73 notifications observed: "+passive73Count);
        append("All-session 0x73/0x01 inventory events observed: "+passive01Count);
        append("Armed-window 0x73 notifications observed: "+watch73Count);
        append("Armed-window 0x73/0x01 inventory events observed: "+watch01Count);
        append("Observed 0x73 event IDs: "+eventIdSummary());
        append("Maximum image count observed during armed window: "+maxPassiveImage);
        append("Armed-window image increase observed: "+(passiveVisibilitySeen?"YES":"NO"));
        append("Proprietary writes total: "+queryWriteCount);
        append("P2P/Wi-Fi/HTTP/media operations: 0");
        append("Raw notification frames logged: NO");
        append("Bluetooth address logged: NO");

        String result;
        if(imageDelta==1){
            result="CAPTURE VISIBLE — final inventory increased by exactly one image";
        }else if(imageDelta>1){
            result="AMBIGUOUS — final inventory increased by more than one image";
        }else if(imageDelta==0&&passiveVisibilitySeen){
            result="PASSIVE VISIBILITY OBSERVED — final query did not retain the increase";
        }else if(imageDelta==0){
            result="NO CAPTURE VISIBILITY within bounded BLE watch";
        }else{
            result="UNEXPECTED — final image count decreased";
        }
        append("G5 VISIBILITY RESULT: "+result);
        append("END REPORT");

        reportFinished=true;
        stage=Stage.COMPLETE;
        disconnectGatt();
        setStatus("Capture-visibility diagnostic complete. Review report before any further G5 test.");
        runButton.setText("Start BLE baseline");
        runButton.setEnabled(true);
        copyButton.setEnabled(true);
        shareButton.setEnabled(true);
    }

    private Inventory parse41Inventory(byte[] frame){
        if(frame==null||frame.length<16)return null;
        int len=(frame[2]&255)|((frame[3]&255)<<8);
        if(len<10)return null;
        int p=6;
        if((frame[p]&255)!=0x02||(frame[p+1]&255)!=0x04)return null;
        Inventory out=new Inventory();
        out.imageCount=le16(frame,p+2);
        out.videoCount=le16(frame,p+4);
        out.recordCount=le16(frame,p+6);
        out.configFileType=frame[p+8]&255;
        out.onlySupportApKnown=true;
        out.onlySupportApImport=(frame[p+9]&255)!=0;
        return out;
    }

    private Inventory parse73Inventory(byte[] frame){
        if(frame==null||frame.length<14||(frame[1]&255)!=0x73||(frame[6]&255)!=0x01)return null;
        Inventory out=new Inventory();
        out.imageCount=le16(frame,7);
        out.videoCount=le16(frame,9);
        out.recordCount=le16(frame,11);
        out.configFileType=frame[13]&255;
        if(frame.length>=15){
            out.onlySupportApKnown=true;
            out.onlySupportApImport=(frame[14]&255)!=0;
        }
        return out;
    }

    private static int le16(byte[] b,int i){return (b[i]&255)|((b[i+1]&255)<<8);}

    private static final class Inventory{
        int imageCount,videoCount,recordCount,configFileType;
        boolean onlySupportApKnown,onlySupportApImport;
    }

    private static String tri(boolean known,boolean value){return known?(value?"true":"false"):"<not present>";}

    private byte[] frame41(byte[] payload){
        int crc=crc16Modbus(payload);
        byte[] frame=new byte[payload.length+6];
        frame[0]=(byte)0xBC;
        frame[1]=0x41;
        frame[2]=(byte)(payload.length&255);
        frame[3]=(byte)((payload.length>>>8)&255);
        frame[4]=(byte)(crc&255);
        frame[5]=(byte)((crc>>>8)&255);
        System.arraycopy(payload,0,frame,6,payload.length);
        if(!validateFrame(frame))throw new IllegalStateException("Internal frame validation failed");
        return frame;
    }

    private static boolean validateFrame(byte[] frame){
        if(frame==null||frame.length<6||(frame[0]&255)!=0xBC)return false;
        int len=(frame[2]&255)|((frame[3]&255)<<8);
        if(frame.length!=len+6)return false;
        byte[] payload=new byte[len];
        System.arraycopy(frame,6,payload,0,len);
        int expected=(frame[4]&255)|((frame[5]&255)<<8);
        return crc16Modbus(payload)==expected;
    }

    private static int crc16Modbus(byte[] data){
        int crc=0xFFFF;
        for(byte value:data){
            crc^=value&255;
            for(int bit=0;bit<8;bit++)crc=(crc&1)!=0?(crc>>>1)^0xA001:crc>>>1;
        }
        return crc&0xFFFF;
    }

    private String eventIdSummary(){
        if(observed73Ids.isEmpty())return "none";
        StringBuilder b=new StringBuilder();
        for(int id:observed73Ids){
            if(b.length()>0)b.append(", ");
            b.append(String.format(Locale.US,"0x%02X",id));
        }
        return b.toString();
    }

    private void resetAll(){
        cancelTasks();
        disconnectGatt();
        report.setLength(0);
        reportView.setText("");
        reportFinished=false;
        stage=Stage.IDLE;
        queryWriteCount=0;
        queryWriteCallbackSucceeded=false;
        queryResponseReceived=false;
        pendingInventory=null;
        baselineInventory=null;
        finalInventory=null;
        passive73Count=0;
        passive01Count=0;
        passive01Logged=0;
        watch73Count=0;
        watch01Count=0;
        watch01Logged=0;
        maxPassiveImage=-1;
        passiveVisibilitySeen=false;
        observed73Ids.clear();
    }

    private void finishFailure(String reason){
        if(reportFinished)return;
        cancelTasks();
        append("G5 VISIBILITY RESULT: FAILED — "+reason);
        append("Proprietary writes total: "+queryWriteCount);
        append("P2P/Wi-Fi/HTTP/media operations: 0");
        append("END REPORT");
        reportFinished=true;
        stage=Stage.COMPLETE;
        disconnectGatt();
        setStatus(reason);
        runButton.setText("Start BLE baseline");
        runButton.setEnabled(true);
        copyButton.setEnabled(report.length()>0);
        shareButton.setEnabled(report.length()>0);
    }

    private void scheduleTimeout(long ms,Runnable action){
        cancelTimeout();
        timeoutTask=action;
        handler.postDelayed(timeoutTask,ms);
    }

    private void cancelTimeout(){
        if(timeoutTask!=null){handler.removeCallbacks(timeoutTask);timeoutTask=null;}
    }

    private void cancelTasks(){
        cancelTimeout();
        if(observeTask!=null){handler.removeCallbacks(observeTask);observeTask=null;}
    }

    private void disconnectGatt(){
        BluetoothGatt local=gatt;
        gatt=null;
        writer=null;
        if(local!=null){
            try{local.disconnect();}catch(Exception ignored){}
            try{local.close();}catch(Exception ignored){}
        }
    }

    private void copyReport(){
        ClipboardManager cb=(ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
        if(cb!=null){
            cb.setPrimaryClip(ClipData.newPlainText("G5 Capture Visibility Report",report.toString()));
            Toast.makeText(this,"Report copied",Toast.LENGTH_SHORT).show();
        }
    }

    private void shareReport(){
        Intent i=new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT,"AIMB-G1 G5 Capture Visibility Report");
        i.putExtra(Intent.EXTRA_TEXT,report.toString());
        startActivity(Intent.createChooser(i,"Share capture visibility report"));
    }

    @Override protected void onDestroy(){
        cancelTasks();
        disconnectGatt();
        super.onDestroy();
    }

    private void append(String line){report.append(line).append('\n');reportView.setText(report.toString());}
    private void appendThreadSafe(String line){handler.post(()->{if(!reportFinished)append(line);});}
    private void setStatus(String text){statusView.setText(text);}

    private static boolean matchesName(String name){
        if(name==null)return false;
        String n=name.trim().toUpperCase(Locale.US);
        return n.equals(TARGET_NAME)||n.startsWith(TARGET_NAME+"_");
    }

    private static String safeGetName(BluetoothDevice d){
        try{return d==null?null:d.getName();}catch(SecurityException e){return null;}
    }

    private static String safeName(String name){
        if(name==null)return "<none>";
        String n=name.trim().toUpperCase(Locale.US);
        if(n.equals(TARGET_NAME))return TARGET_NAME;
        if(n.startsWith(TARGET_NAME+"_"))return TARGET_NAME+"_<suffix>";
        return "<non-AIMB name not logged>";
    }

    private static String isoNow(){return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ",Locale.US).format(new Date());}
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
}
