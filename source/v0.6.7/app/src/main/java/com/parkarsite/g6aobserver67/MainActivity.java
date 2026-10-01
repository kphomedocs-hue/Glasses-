package com.parkarsite.g6aobserver67;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.*;
import android.os.*;
import android.widget.*;

import com.parkarsite.g6a.OpaqueIdentity;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.*;
import java.text.SimpleDateFormat;
import java.util.*;

@SuppressLint("MissingPermission")
public final class MainActivity extends Activity {
    private static final int REQ_PERMISSION=6701;
    private static final String TARGET_NAME="AIMB-G1";
    private static final long CONNECT_TIMEOUT_MS=30000L;
    private static final long P2P_DISCOVERY_TIMEOUT_MS=20000L;
    private static final long P2P_CONNECTION_TIMEOUT_MS=15000L;
    private static final long CATALOG_WAIT_TIMEOUT_MS=8000L;
    private static final long CATALOG_READY_DELAY_MS=1000L;
    private static final long CAPTURE_WATCH_MS=60000L;
    private static final long RECONNECT_DELAY_MS=5000L;
    private static final int MEDIA_MAX_BYTES=33554432;
    private static final String MEDIA_PREFIX="/files/";
    private static final int CATALOG_MAX_BYTES=65536;
    private static final int CATALOG_MAX_ITEMS=256;
    private static final String CATALOG_PATH="/files/media.config";

    private static final UUID CYAN_SERVICE=UUID.fromString("de5bf728-d711-4e47-af26-65e3012a5dc7");
    private static final UUID CYAN_NOTIFY=UUID.fromString("de5bf729-d711-4e47-af26-65e3012a5dc7");
    private static final UUID CYAN_WRITE=UUID.fromString("de5bf72a-d711-4e47-af26-65e3012a5dc7");
    private static final UUID CLIENT_CONFIG=UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private static final byte[] MEDIA_COUNT=new byte[]{0x02,0x04};
    private static final byte[] ENTER_P2P=new byte[]{0x02,0x01,0x04,0x01};
    private static final byte[] EXIT_TRANSFER=new byte[]{0x02,0x01,0x09};

    private enum Stage { BASELINE, POST_CAPTURE, PRE_GET_RETENTION, POST_GET_RETENTION }
    private enum Phase { IDLE, CAPTURE_WATCH, COUNT_SENT, ENTER_SENT, DISCOVERING, CONNECTING, CONNECTED, CATALOG_DELAY, CATALOG_GET, MEDIA_GET, EXIT_SENT, CLEANUP, COMPLETE }

    private final Handler handler=new Handler(Looper.getMainLooper());
    private final StringBuilder report=new StringBuilder();
    private final LinkedHashSet<Integer> observed73Events=new LinkedHashSet<>();
    private final LinkedHashSet<String> baselineAllHashes=new LinkedHashSet<>();
    private final LinkedHashSet<String> baselineJpgHashes=new LinkedHashSet<>();
    private final LinkedHashSet<String> postCaptureAllHashes=new LinkedHashSet<>();
    private final LinkedHashSet<String> postCaptureJpgHashes=new LinkedHashSet<>();

    private BluetoothAdapter btAdapter;
    private BluetoothGatt gatt;
    private BluetoothGattCharacteristic writeChar;
    private WifiP2pManager p2pManager;
    private WifiP2pManager.Channel p2pChannel;
    private BroadcastReceiver p2pReceiver;
    private boolean receiverRegistered;

    private TextView statusView,reportView;
    private Button runButton,copyButton,shareButton;

    private Stage stage=Stage.BASELINE;
    private Phase phase=Phase.IDLE;
    private boolean reportFinished=true;
    private boolean runActive;
    private boolean countWriteAttempted,countWriteCallbackSucceeded,countResponseReceived;
    private boolean enterWriteAttempted,enterWriteStarted,enterWriteCallbackSucceeded,enterCredentialResponseReceived,exitWriteAttempted,exitWriteCallbackSucceeded,postExitInventoryConfirmed;
    private boolean p2pGroupFormed,phoneIsGroupOwner,catalogGetAttempted,catalogDelayScheduled;
    private boolean failurePending,comparisonCompleted,preGetRetentionExact,postGetRetentionExact,baselineFullMediaParity,postCaptureFullMediaParity,cleanupCompletionHandled,destroyRequested;
    private boolean captureWatchArmed,capturePassiveConfirmed,postCaptureCatalogExact,mediaValidated,tempCleanupPass;
    private String failureReason,glassesClientIp,expectedP2pName,credentialPassword,retentionClassification,newJpgHash;
    private Runnable timeout,catalogDelayTask,quietTask,cleanupFallbackTask;
    private long captureWatchStartElapsedMs=-1L;
    private int captureWatchInventoryEvents;

    private int currentImageCount=-1,currentVideoCount=-1,currentRecordCount=-1,currentConfigFileType=-1;
    private boolean currentOnlySupportApImport;
    private int baselineImageCount=-1,baselineVideoCount=-1,baselineRecordCount=-1;
    private int baselineTotalEntries=-1,baselineJpgEntries=-1,baselineVideoEntries=-1,baselineOpusEntries=-1;
    private int postCaptureImageCount=-1,postCaptureVideoCount=-1,postCaptureRecordCount=-1;
    private int postCaptureTotalEntries=-1,postCaptureJpgEntries=-1,postCaptureVideoEntries=-1,postCaptureOpusEntries=-1;
    private int currentCatalogEntries,currentJpgEntries,currentVideoEntries,currentOpusEntries;
    private int countWriteCount,enterWriteCount,exitWriteCount,catalogGetCount,httpRequestCount,mediaGetCount;
    private long downloadedBytes=-1L,declaredMediaBytes=-1L;
    private boolean postGetExitInventoryObserved=false,postGetExitInventoryMatchedPreGet=false;
    private int postGetExitImageCount=-1,postGetExitVideoCount=-1,postGetExitRecordCount=-1,postGetExitConfigType=-1;
    private boolean postGetExitOnlySupportApImport=false;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        buildUi();
        BluetoothManager bm=(BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter=bm==null?null:bm.getAdapter();
        p2pManager=(WifiP2pManager)getSystemService(Context.WIFI_P2P_SERVICE);
        if(p2pManager!=null)p2pChannel=p2pManager.initialize(this,Looper.getMainLooper(),()->appendThreadSafe("P2P channel: DISCONNECTED"));
        renderIdle();
    }

    private void buildUi(){
        int p=dp(20);
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(p,p,p,p);
        TextView title=new TextView(this); title.setText("K G1 Post-GET Transition Observer v0.6.7"); title.setTextSize(23); root.addView(title);
        TextView note=new TextView(this); note.setText("G6A diagnostic: reproduce one exact JPG GET, observe the immediate post-GET inventory even if counts change, then reconnect and classify the stable remote catalog."); note.setPadding(0,dp(8),0,dp(16)); root.addView(note);
        statusView=new TextView(this); statusView.setTextSize(16); root.addView(statusView);
        runButton=new Button(this); runButton.setText("Start post-GET transition observer"); runButton.setOnClickListener(v->begin()); root.addView(runButton);
        LinearLayout row=new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL);
        copyButton=new Button(this); copyButton.setText("Copy report"); copyButton.setEnabled(false); copyButton.setOnClickListener(v->copy()); row.addView(copyButton);
        shareButton=new Button(this); shareButton.setText("Share report"); shareButton.setEnabled(false); shareButton.setOnClickListener(v->share()); row.addView(shareButton); root.addView(row);
        ScrollView scroll=new ScrollView(this); reportView=new TextView(this); reportView.setTextSize(13); reportView.setTextIsSelectable(true); scroll.addView(reportView);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);
    }

    private void renderIdle(){
        if(btAdapter==null||p2pManager==null){
            statusView.setText("Bluetooth LE or Wi-Fi Direct is unavailable.");
            runButton.setEnabled(false);
        }else{
            statusView.setText("Ready. Force-stop Cyan Glasses. Do NOT take a photo until the app explicitly says ARMED.");
        }
    }

    private void begin(){
        if(!hasPermissions()){requestRequiredPermissions();return;}
        try{if(!btAdapter.isEnabled()){setStatus("Bluetooth is off.");return;}}
        catch(SecurityException e){setStatus("Bluetooth permission required.");return;}

        cleanupRuntime(false);
        cancelQuiet();
        resetTotals();
        report.setLength(0);
        reportFinished=false;
        runActive=true;
        stage=Stage.BASELINE;

        append("K G1 G6A POST-GET INVENTORY TRANSITION OBSERVER REPORT");
        append("Generated: "+isoNow());
        append("App version: 0.6.6");
        append("Build commit: "+BuildConfig.BUILD_COMMIT);
        append("Build run: "+BuildConfig.BUILD_RUN);
        append("Build attempt: "+BuildConfig.BUILD_ATTEMPT);
        append("Mode: BASELINE -> ONE CAPTURE -> PRE-GET RETENTION -> ONE JPG GET -> OBSERVE POST-GET EXIT INVENTORY -> FRESH POST-GET INVENTORY/CATALOG");
        append("Purpose: classify the reproduced post-GET inventory transition and the stable remote catalog state after exactly one media GET");
        append("Instruction: DO NOT TAKE A PHOTO until the app explicitly reports ARMED");
        append("Capture action issued by app: NO");
        append("Intended physical captures: EXACTLY ONE");
        append("Media GETs allowed: EXACTLY ONE, only after pre-GET retention is proven");
        append("Inventory writes total allowed: EXACTLY FOUR");
        append("P2P enter writes total allowed: EXACTLY FOUR");
        append("Transfer exit writes total allowed: EXACTLY FOUR");
        append("Post-GET exit inventory equality: NOT REQUIRED; exact counts are observational data");
        append("Catalog GET requests total allowed: EXACTLY FOUR");
        append("Total HTTP GET requests allowed: EXACTLY FIVE (4 catalog + 1 JPG)");
        append("HTTP redirects: DISABLED");
        append("HTTP retry/resume/Range: DISABLED");
        append("Catalog response cap: "+CATALOG_MAX_BYTES+" bytes");
        append("Media response cap: "+MEDIA_MAX_BYTES+" bytes");
        append("Peer selection: EXACT CASE-SENSITIVE BLE-REPORTED P2P NAME ONLY");
        append("Credential logging/persistence: DISABLED");
        append("Remote filename/path logging/persistence: DISABLED");
        append("Persistent import/ledger: DISABLED");
        append("Temporary JPG: app-private cache only; deleted after validation");
        append("Glasses file mutation/deletion: NOT IMPLEMENTED");
        append("");
        append("BASELINE SNAPSHOT");

        runButton.setEnabled(false);copyButton.setEnabled(false);shareButton.setEnabled(false);
        startSnapshotConnection();
    }

    private boolean hasPermissions(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.S&&checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!=PackageManager.PERMISSION_GRANTED)return false;
        if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.NEARBY_WIFI_DEVICES)!=PackageManager.PERMISSION_GRANTED)return false;
        if(Build.VERSION.SDK_INT<33&&checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)return false;
        return true;
    }

    private void requestRequiredPermissions(){
        ArrayList<String> ps=new ArrayList<>();
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.S)ps.add(Manifest.permission.BLUETOOTH_CONNECT);
        if(Build.VERSION.SDK_INT>=33)ps.add(Manifest.permission.NEARBY_WIFI_DEVICES);
        else{ps.add(Manifest.permission.ACCESS_COARSE_LOCATION);ps.add(Manifest.permission.ACCESS_FINE_LOCATION);}
        requestPermissions(ps.toArray(new String[0]),REQ_PERMISSION);
    }

    @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){
        super.onRequestPermissionsResult(r,p,g);
        if(r==REQ_PERMISSION){
            if(hasPermissions())begin();
            else setStatus("Bluetooth and Nearby Wi-Fi permission are required.");
        }
    }

    private void resetTotals(){
        countWriteCount=enterWriteCount=exitWriteCount=catalogGetCount=httpRequestCount=mediaGetCount=0;
        baselineImageCount=baselineVideoCount=baselineRecordCount=-1;
        baselineTotalEntries=baselineJpgEntries=baselineVideoEntries=baselineOpusEntries=-1;
        postCaptureImageCount=postCaptureVideoCount=postCaptureRecordCount=-1;
        postCaptureTotalEntries=postCaptureJpgEntries=postCaptureVideoEntries=postCaptureOpusEntries=-1;
        baselineAllHashes.clear();baselineJpgHashes.clear();postCaptureAllHashes.clear();postCaptureJpgHashes.clear();
        comparisonCompleted=false;preGetRetentionExact=false;postGetRetentionExact=false;
        baselineFullMediaParity=false;postCaptureFullMediaParity=false;
        captureWatchArmed=false;capturePassiveConfirmed=false;postCaptureCatalogExact=false;
        mediaValidated=false;tempCleanupPass=false;downloadedBytes=-1L;declaredMediaBytes=-1L;
        postGetExitInventoryObserved=false;postGetExitInventoryMatchedPreGet=false;
        postGetExitImageCount=postGetExitVideoCount=postGetExitRecordCount=postGetExitConfigType=-1;postGetExitOnlySupportApImport=false;
        retentionClassification="<not classified>";newJpgHash=null;captureWatchStartElapsedMs=-1L;captureWatchInventoryEvents=0;destroyRequested=false;
    }

    private void resetCycleState(){
        countWriteAttempted=false;countWriteCallbackSucceeded=false;countResponseReceived=false;
        enterWriteAttempted=false;enterWriteStarted=false;enterWriteCallbackSucceeded=false;enterCredentialResponseReceived=false;exitWriteAttempted=false;exitWriteCallbackSucceeded=false;postExitInventoryConfirmed=false;cleanupCompletionHandled=false;
        p2pGroupFormed=false;phoneIsGroupOwner=false;catalogGetAttempted=false;catalogDelayScheduled=false;
        failurePending=false;failureReason=null;glassesClientIp=null;expectedP2pName=null;credentialPassword=null;
        currentImageCount=currentVideoCount=currentRecordCount=currentConfigFileType=-1;
        currentOnlySupportApImport=false;currentCatalogEntries=0;currentJpgEntries=0;currentVideoEntries=0;currentOpusEntries=0;
        observed73Events.clear();phase=Phase.IDLE;
        if(catalogDelayTask!=null){handler.removeCallbacks(catalogDelayTask);catalogDelayTask=null;}
    }

    private void startSnapshotConnection(){
        cleanupRuntime(false);
        resetCycleState();
        BluetoothDevice target=findUniqueBondedTarget();
        if(target==null)return;
        registerP2pReceiver();
        append("GATT CONNECTION");
        append("Target selection: exactly one bonded AIMB-G1-family device");
        append("Target name: "+safeName(safeGetName(target)));
        append("Bluetooth address: not logged");
        setStatus((stage==Stage.BASELINE?"Baseline":stage==Stage.PRE_GET_RETENTION?"Pre-GET retention":"Post-GET retention")+": connecting BLE. DO NOT TAKE A PHOTO.");
        schedule(()->abortRun("BLE/P2P setup timeout."),CONNECT_TIMEOUT_MS);
        try{gatt=target.connectGatt(this,false,gattCb,BluetoothDevice.TRANSPORT_LE);}
        catch(Exception e){abortRun("BLE connect request failed.");}
    }

    private void startCaptureWatchConnection(){
        cleanupRuntime(false);
        resetCycleState();
        stage=Stage.POST_CAPTURE;
        captureWatchArmed=false;
        capturePassiveConfirmed=false;
        captureWatchInventoryEvents=0;
        BluetoothDevice target=findUniqueBondedTarget();
        if(target==null)return;
        registerP2pReceiver();
        append("");append("CAPTURE VISIBILITY WATCH");
        append("Purpose: observe exactly one physical photo through passive 0x73/0x01 before any post-capture P2P/catalog read");
        append("P2P/HTTP operations before +1 confirmation: 0");
        append("User instruction: WAIT — DO NOT TAKE A PHOTO YET");
        setStatus("Preparing capture watch. DO NOT TAKE A PHOTO YET.");
        schedule(()->abortRun("BLE setup timeout before capture watch."),CONNECT_TIMEOUT_MS);
        try{gatt=target.connectGatt(this,false,gattCb,BluetoothDevice.TRANSPORT_LE);}
        catch(Exception e){abortRun("BLE connect request failed before capture watch.");}
    }

    private void armCaptureWatch(){
        if(reportFinished||stage!=Stage.POST_CAPTURE||captureWatchArmed)return;
        cancelTimeout();
        captureWatchArmed=true;
        captureWatchStartElapsedMs=SystemClock.elapsedRealtime();
        phase=Phase.CAPTURE_WATCH;
        append("");append("CAPTURE WATCH ARMED");
        append("Baseline inventory: images="+baselineImageCount+", videos="+baselineVideoCount+", recordings="+baselineRecordCount);
        append("Instruction: TAKE EXACTLY ONE PHOTO NOW");
        append("Do not take a second photo.");
        setStatus("ARMED — take exactly ONE photo now, then do not take another.");
        schedule(()->abortRun("No exact +1 capture became visible within the bounded capture-watch window."),CAPTURE_WATCH_MS);
    }

    private BluetoothDevice findUniqueBondedTarget(){
        append("BONDED TARGET CHECK");
        int total=0,matches=0; BluetoothDevice unique=null;
        try{
            Set<BluetoothDevice> bonded=btAdapter.getBondedDevices(); total=bonded==null?0:bonded.size();
            if(bonded!=null)for(BluetoothDevice d:bonded){if(matchesTarget(safeGetName(d))){matches++;unique=d;}}
        }catch(SecurityException e){abortRun("Cannot inspect bonded devices.");return null;}
        append("Bonded devices total: "+total);append("AIMB-G1-family bonded matches: "+matches);
        if(matches!=1){abortRun("Exactly one AIMB-G1-family bonded device is required.");return null;}
        append("Bonded target name: "+safeName(safeGetName(unique)));append("Bonded target address: not logged");append("");
        return unique;
    }

    private final BluetoothGattCallback gattCb=new BluetoothGattCallback(){
        @Override public void onConnectionStateChange(BluetoothGatt x,int status,int state){
            if(status!=BluetoothGatt.GATT_SUCCESS){closeAndAbort("GATT status "+status);return;}
            if(state==BluetoothProfile.STATE_CONNECTED){
                appendThreadSafe("GATT: connected");
                try{if(!x.discoverServices())closeAndAbort("Service discovery did not start.");}
                catch(SecurityException e){closeAndAbort("Bluetooth permission error.");}
            }else if(state==BluetoothProfile.STATE_DISCONNECTED&&!reportFinished&&phase!=Phase.CLEANUP&&phase!=Phase.COMPLETE){
                closeAndAbort("BLE disconnected early.");
            }
        }
        @Override public void onServicesDiscovered(BluetoothGatt x,int status){
            if(status!=BluetoothGatt.GATT_SUCCESS){closeAndAbort("Service discovery failed.");return;}
            BluetoothGattService s=x.getService(CYAN_SERVICE);if(s==null){closeAndAbort("Cyan service not found.");return;}
            BluetoothGattCharacteristic n=s.getCharacteristic(CYAN_NOTIFY),w=s.getCharacteristic(CYAN_WRITE);
            if(n==null||w==null){closeAndAbort("Cyan notify/write characteristic missing.");return;}
            writeChar=w;appendThreadSafe("CYAN SERVICE/NOTIFY/WRITE: PRESENT");
            try{
                if(!x.setCharacteristicNotification(n,true)){closeAndAbort("Local notification registration failed.");return;}
                BluetoothGattDescriptor d=n.getDescriptor(CLIENT_CONFIG);if(d==null){closeAndAbort("CCCD not found.");return;}
                d.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                if(!x.writeDescriptor(d)){closeAndAbort("CCCD write did not start.");return;}
                appendThreadSafe("Notification subscription start: SUCCESS");
            }catch(SecurityException e){closeAndAbort("Bluetooth permission error.");}
        }
        @Override public void onDescriptorWrite(BluetoothGatt x,BluetoothGattDescriptor d,int status){
            if(status!=BluetoothGatt.GATT_SUCCESS){closeAndAbort("CCCD write failed: "+status);return;}
            appendThreadSafe("Notification subscription: SUCCESS");
            handler.post(()->{
                if(stage==Stage.POST_CAPTURE&&!capturePassiveConfirmed)armCaptureWatch();
                else sendMediaCountQuery(x);
            });
        }
        @Override public void onCharacteristicWrite(BluetoothGatt x,BluetoothGattCharacteristic c,int status){
            handler.post(()->{
                if(reportFinished)return;
                append("BLE characteristic write status: "+(status==BluetoothGatt.GATT_SUCCESS?"SUCCESS":status));
                if(status!=BluetoothGatt.GATT_SUCCESS){
                    if(phase==Phase.EXIT_SENT){
                        if(!failurePending){failurePending=true;failureReason="Exit-transfer BLE write callback failed; snapshot separation not proven.";}
                        startLocalCleanup();
                    }else abortRun("BLE write callback failed.");
                    return;
                }
                if(phase==Phase.COUNT_SENT){countWriteCallbackSucceeded=true;maybeAdvanceAfterMediaCount();}
                else if(phase==Phase.ENTER_SENT){
                    enterWriteCallbackSucceeded=true;
                    append("P2P enter write callback: SUCCESS");
                    maybeAdvanceAfterEnter();
                }
                else if(phase==Phase.EXIT_SENT){
                    exitWriteCallbackSucceeded=true;
                    append("Exit write callback: SUCCESS");
                    append("Post-exit confirmation required: valid 0x73/0x01 inventory matching current snapshot");
                }
            });
        }
        @Override public void onCharacteristicChanged(BluetoothGatt x,BluetoothGattCharacteristic c,byte[] value){handleNotify(value);}
        @SuppressWarnings("deprecation") @Override public void onCharacteristicChanged(BluetoothGatt x,BluetoothGattCharacteristic c){handleNotify(c.getValue());}
    };

    private void sendMediaCountQuery(BluetoothGatt x){
        cancelTimeout();if(reportFinished||countWriteAttempted)return;
        append("");append("CYAN MEDIA INVENTORY REFRESH");
        append("Command: 0x41 / 02 04");
        append("Purpose: exact Cyan media-count/config confirmation for current stage");
        append("No retry policy: TRUE");
        countWriteAttempted=true;countWriteCount++;phase=Phase.COUNT_SENT;
        if(!writeFrame(x,frame41(MEDIA_COUNT))){abortRun("Media-count write did not start.");return;}
        append("Media-count write start: SUCCESS");
        setStatus((stage==Stage.BASELINE?"Baseline":stage==Stage.POST_CAPTURE?"Post-capture confirmation":stage==Stage.PRE_GET_RETENTION?"Pre-GET retention":"Post-GET retention")+": waiting for inventory response.");
        schedule(()->abortRun("No valid media-count response received."),10000);
    }

    private void maybeAdvanceAfterMediaCount(){
        if(reportFinished||phase!=Phase.COUNT_SENT||!countWriteCallbackSucceeded||!countResponseReceived)return;
        BluetoothGatt x=gatt;if(x==null){abortRun("GATT unavailable after media-count handshake.");return;}
        cancelTimeout();append("Media-count write/response handshake: COMPLETE");

        if(stage==Stage.POST_CAPTURE){
            boolean exactPlusOne=currentImageCount==baselineImageCount+1
                    &&currentVideoCount==baselineVideoCount
                    &&currentRecordCount==baselineRecordCount
                    &&currentConfigFileType==1&&!currentOnlySupportApImport;
            append("Active post-capture inventory confirms exact +1 image: "+exactPlusOne);
            if(!exactPlusOne){abortRun("Active post-capture inventory did not confirm the exact +1 image boundary.");return;}
        }else if(stage==Stage.PRE_GET_RETENTION||stage==Stage.POST_GET_RETENTION){
            boolean retainedCounts=currentImageCount==postCaptureImageCount
                    &&currentVideoCount==postCaptureVideoCount
                    &&currentRecordCount==postCaptureRecordCount
                    &&currentConfigFileType==1&&!currentOnlySupportApImport;
            append((stage==Stage.PRE_GET_RETENTION?"Pre-GET":"Post-GET")+" retention inventory matches post-capture counts: "+retainedCounts);
        }

        sendEnter(x);
    }

    private void sendEnter(BluetoothGatt x){
        cancelTimeout();if(reportFinished||enterWriteAttempted)return;
        append("");append("ENTER P2P MODE");append("Command: 0x41 / 02 01 04 01");append("No retry policy: TRUE");
        enterWriteAttempted=true;enterWriteStarted=false;enterWriteCallbackSucceeded=false;enterCredentialResponseReceived=false;enterWriteCount++;phase=Phase.ENTER_SENT;
        if(!writeFrame(x,frame41(ENTER_P2P))){abortRun("P2P enter write did not start.");return;}
        enterWriteStarted=true;
        append("ENTER write start: SUCCESS");setStatus("Waiting for enter write callback + transfer credentials. DO NOT TAKE ANOTHER PHOTO.");
        schedule(()->abortRun("P2P enter write/credential handshake did not complete."),10000);
    }

    private void maybeAdvanceAfterEnter(){
        if(reportFinished||phase!=Phase.ENTER_SENT||!enterWriteCallbackSucceeded||!enterCredentialResponseReceived)return;
        if(expectedP2pName==null||credentialPassword==null){
            abortRun("P2P enter handshake completed without usable in-memory credentials.");
            return;
        }
        cancelTimeout();
        append("P2P enter write/credential handshake: COMPLETE");
        startP2pDiscovery();
    }

    private void handleNotify(byte[] v){
        if(v==null||reportFinished)return;final byte[] data=v.clone();
        handler.post(()->{
            if(reportFinished)return;
            if(!validFrame(data)){append("Notification: invalid frame ignored");return;}
            int cmd=data[1]&255;
            if(cmd==0x41&&phase==Phase.COUNT_SENT){
                InventorySummary inv=parseMediaCountResponse(data);
                if(inv!=null){
                    currentImageCount=inv.imageCount;currentVideoCount=inv.videoCount;currentRecordCount=inv.recordCount;
                    currentConfigFileType=inv.configFileType;currentOnlySupportApImport=inv.onlySupportApImport;
                    append("Media-count response: VALID");
                    append("Image count: "+currentImageCount);append("Video count: "+currentVideoCount);append("Recording count: "+currentRecordCount);
                    append("Config file type: "+currentConfigFileType);append("Only-support-AP-import: "+currentOnlySupportApImport);
                    if(currentConfigFileType!=1||currentOnlySupportApImport){abortRun("Inventory response no longer selects confirmed configFileType=1 P2P branch.");return;}
                    countResponseReceived=true;maybeAdvanceAfterMediaCount();
                }
            }else if(cmd==0x41&&phase==Phase.ENTER_SENT&&parseTransferCredentials(data)){
                enterCredentialResponseReceived=true;
                append("Transfer credential response: VALID");
                append("SSID length: "+expectedP2pName.getBytes(StandardCharsets.UTF_8).length);
                append("Password length: "+credentialPassword.getBytes(StandardCharsets.UTF_8).length);
                append("SSID/password values: not logged");
                maybeAdvanceAfterEnter();
            }else if(cmd==0x73){
                handle73Event(data);
            }else if(cmd==0x41&&phase==Phase.EXIT_SENT){
                append("Post-exit 0x41 frame observed: IGNORED for exit confirmation");
            }
        });
    }

    private InventorySummary parseMediaCountResponse(byte[] f){
        if(f==null||f.length<16)return null;
        int len=(f[2]&255)|((f[3]&255)<<8);if(len<10)return null;
        int p=6;if((f[p]&255)!=0x02||(f[p+1]&255)!=0x04)return null;
        InventorySummary out=new InventorySummary();
        out.imageCount=(f[p+2]&255)|((f[p+3]&255)<<8);
        out.videoCount=(f[p+4]&255)|((f[p+5]&255)<<8);
        out.recordCount=(f[p+6]&255)|((f[p+7]&255)<<8);
        out.configFileType=f[p+8]&255;out.onlySupportApImport=(f[p+9]&255)!=0;return out;
    }

    private static final class InventorySummary{int imageCount,videoCount,recordCount,configFileType;boolean onlySupportApImport;}

    private boolean parseTransferCredentials(byte[] f){
        if(f.length<14)return false;int len=(f[2]&255)|((f[3]&255)<<8);if(len<8)return false;
        int p=6;if((f[p]&255)!=2||(f[p+1]&255)!=1||(f[p+2]&255)!=4||(f[p+3]&255)!=1)return false;
        int sl=(f[p+4]&255)|((f[p+5]&255)<<8),pl=(f[p+6]&255)|((f[p+7]&255)<<8),off=p+8;
        if(sl<=0||pl<=0||off+sl+pl>f.length)return false;
        expectedP2pName=new String(f,off,sl,StandardCharsets.UTF_8);
        credentialPassword=new String(f,off+sl,pl,StandardCharsets.UTF_8);
        return !expectedP2pName.isEmpty()&&!credentialPassword.isEmpty();
    }

    private void handle73Event(byte[] data){
        if(data.length<7){append("Async 0x73 frame observed without event ID");return;}
        int eventId=data[6]&255;observed73Events.add(eventId);
        append(String.format(Locale.US,"Async 0x73 event ID: 0x%02X",eventId));

        if(eventId==0x01&&phase==Phase.CAPTURE_WATCH&&stage==Stage.POST_CAPTURE&&captureWatchArmed&&!capturePassiveConfirmed){
            InventorySummary inv=parse73Inventory(data);
            if(inv==null){
                append("Capture-watch 0x73/0x01 inventory: INVALID / IGNORED");
                return;
            }
            captureWatchInventoryEvents++;
            append("Capture-watch inventory event #"+captureWatchInventoryEvents+": images="+inv.imageCount+", videos="+inv.videoCount+", recordings="+inv.recordCount);
            boolean otherCountsStable=inv.videoCount==baselineVideoCount&&inv.recordCount==baselineRecordCount&&inv.configFileType==1&&!inv.onlySupportApImport;
            if(!otherCountsStable){
                abortRun("Capture-watch non-image/config counts changed; capture attribution is ambiguous.");
                return;
            }
            if(inv.imageCount==baselineImageCount){
                append("Capture-watch image count unchanged; continuing to wait.");
                return;
            }
            if(inv.imageCount!=baselineImageCount+1){
                abortRun("Ambiguous capture visibility: expected exactly +1 image but observed delta "+(inv.imageCount-baselineImageCount)+".");
                return;
            }
            capturePassiveConfirmed=true;
            currentImageCount=inv.imageCount;currentVideoCount=inv.videoCount;currentRecordCount=inv.recordCount;
            currentConfigFileType=inv.configFileType;currentOnlySupportApImport=inv.onlySupportApImport;
            long elapsed=SystemClock.elapsedRealtime()-captureWatchStartElapsedMs;
            cancelTimeout();
            append("Passive capture visibility: EXACT +1");
            append("Capture visibility elapsed: "+elapsed+" ms");
            append("Instruction: DO NOT TAKE ANY MORE PHOTOS");
            setStatus("Exact +1 observed. Do not take any more photos. Confirming inventory.");
            sendMediaCountQuery(gatt);
            return;
        }

        if(eventId==0x01&&phase==Phase.EXIT_SENT&&exitWriteCallbackSucceeded&&!postExitInventoryConfirmed){
            InventorySummary inv=parse73Inventory(data);
            if(inv==null){
                append("Post-exit 0x73/0x01 inventory: INVALID / IGNORED");
                return;
            }
            boolean matches=inv.imageCount==currentImageCount
                    &&inv.videoCount==currentVideoCount
                    &&inv.recordCount==currentRecordCount
                    &&inv.configFileType==currentConfigFileType;
            append("Post-exit 0x73/0x01 inventory: VALID");
            append("Post-exit inventory matches current snapshot: "+matches);

            boolean observationalPostGetExit=stage==Stage.PRE_GET_RETENTION&&mediaValidated&&mediaGetCount==1;
            if(observationalPostGetExit){
                postGetExitInventoryObserved=true;
                postGetExitInventoryMatchedPreGet=matches;
                postGetExitImageCount=inv.imageCount;
                postGetExitVideoCount=inv.videoCount;
                postGetExitRecordCount=inv.recordCount;
                postGetExitConfigType=inv.configFileType;
                postGetExitOnlySupportApImport=inv.onlySupportApImport;
                append("Post-GET exit inventory observation mode: TRUE");
                append("Post-GET exit inventory images: "+inv.imageCount);
                append("Post-GET exit inventory videos: "+inv.videoCount);
                append("Post-GET exit inventory recordings: "+inv.recordCount);
                append("Post-GET exit config file type: "+inv.configFileType);
                append("Post-GET exit only-support-AP-import: "+inv.onlySupportApImport);
                append("Post-GET exit image delta vs pre-GET snapshot: "+(inv.imageCount-currentImageCount));
                append("Post-GET exit inventory mismatch is observational, not a failure.");
                postExitInventoryConfirmed=true;
                cancelTimeout();
                append("Post-GET exit 0x73/0x01 observation: COMPLETE");
                startLocalCleanup();
                return;
            }

            if(!matches){
                if(!failurePending){failurePending=true;failureReason="Post-exit inventory did not match current snapshot outside the post-GET observation stage; exit separation not proven.";}
                startLocalCleanup();
                return;
            }
            postExitInventoryConfirmed=true;
            cancelTimeout();
            append("Post-exit 0x73/0x01 confirmation: COMPLETE");
            startLocalCleanup();
            return;
        }

        if(eventId!=0x08)return;
        String ip=parseIpv4(data,7);
        if(ip==null){append("0x73/0x08 IPv4: unresolved");return;}
        glassesClientIp=ip;append("0x73/0x08 glasses IPv4: "+ip);maybeStartCatalogGet();
    }

    private InventorySummary parse73Inventory(byte[] frame){
        if(frame==null||frame.length<14||(frame[1]&255)!=0x73||(frame[6]&255)!=0x01)return null;
        InventorySummary out=new InventorySummary();
        out.imageCount=le16(frame,7);
        out.videoCount=le16(frame,9);
        out.recordCount=le16(frame,11);
        out.configFileType=frame[13]&255;
        out.onlySupportApImport=frame.length>=15&&(frame[14]&255)!=0;
        return out;
    }

    private static int le16(byte[] b,int i){return (b[i]&255)|((b[i+1]&255)<<8);}

    private static String parseIpv4(byte[] data,int offset){
        if(data==null||offset<0||offset+4>data.length)return null;
        int a=data[offset]&255,b=data[offset+1]&255,c=data[offset+2]&255,d=data[offset+3]&255;
        if(a==0&&b==0&&c==0&&d==0)return null;return a+"."+b+"."+c+"."+d;
    }

    @SuppressWarnings("deprecation")
    private void startP2pDiscovery(){
        if(expectedP2pName==null||p2pManager==null||p2pChannel==null){abortRun("P2P manager/target name unavailable.");return;}
        phase=Phase.DISCOVERING;append("");append("WI-FI DIRECT DISCOVERY");
        append("Peer-name match: exact BLE-reported name, in memory only");append("Nearby peer names/addresses: not logged");
        try{
            p2pManager.discoverPeers(p2pChannel,new WifiP2pManager.ActionListener(){
                public void onSuccess(){append("discoverPeers start: SUCCESS");setStatus("Discovering exact P2P peer. DO NOT TAKE ANOTHER PHOTO.");schedule(()->abortRun("Exact glasses P2P peer was not discovered."),P2P_DISCOVERY_TIMEOUT_MS);}
                public void onFailure(int reason){append("discoverPeers start: FAILED reason="+reason);abortRun("P2P discovery failed.");}
            });
        }catch(SecurityException e){abortRun("Nearby Wi-Fi permission error.");}
    }

    private void registerP2pReceiver(){
        if(receiverRegistered)return;
        IntentFilter f=new IntentFilter();f.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);f.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);f.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        p2pReceiver=new BroadcastReceiver(){@Override public void onReceive(Context c,Intent i){
            String a=i.getAction();
            if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(a))requestPeers();
            else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(a)){
                if(Build.VERSION.SDK_INT<29){NetworkInfo ni=i.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);if(ni!=null&&ni.isConnected())requestConnectionInfo();}
                else requestConnectionInfo();
            }
        }};
        if(Build.VERSION.SDK_INT>=33)registerReceiver(p2pReceiver,f,Context.RECEIVER_NOT_EXPORTED);else registerReceiver(p2pReceiver,f);
        receiverRegistered=true;
    }

    @SuppressWarnings("deprecation")
    private void requestPeers(){
        if(reportFinished||phase!=Phase.DISCOVERING)return;
        try{
            p2pManager.requestPeers(p2pChannel,peers->{
                if(reportFinished||phase!=Phase.DISCOVERING||expectedP2pName==null)return;
                WifiP2pDevice match=null;int count=0;
                for(WifiP2pDevice d:peers.getDeviceList()){
                    count++;
                    if(d.deviceName!=null&&d.deviceName.equals(expectedP2pName)){
                        if(match!=null){abortRun("Multiple exact peer-name matches; refusing connection.");return;}
                        match=d;
                    }
                }
                append("Peers observed: "+count+" (names/addresses not logged)");
                if(match!=null){cancelTimeout();append("Exact glasses P2P peer match: YES");connectPeer(match);}
            });
        }catch(SecurityException e){abortRun("Permission error while requesting peers.");}
    }

    @SuppressWarnings("deprecation")
    private void connectPeer(WifiP2pDevice d){
        if(phase!=Phase.DISCOVERING)return;phase=Phase.CONNECTING;
        WifiP2pConfig cfg=new WifiP2pConfig();cfg.deviceAddress=d.deviceAddress;cfg.groupOwnerIntent=0;cfg.wps.setup=WpsInfo.PBC;
        append("");append("WI-FI DIRECT ASSOCIATION");append("Target: exact BLE-reported peer");append("WPS: PBC");append("groupOwnerIntent: 0");append("Peer address: not logged");
        try{
            p2pManager.connect(p2pChannel,cfg,new WifiP2pManager.ActionListener(){
                public void onSuccess(){append("connect request: SUCCESS");setStatus("Waiting for P2P group. DO NOT TAKE ANOTHER PHOTO.");schedule(()->abortRun("P2P group did not form in time."),P2P_CONNECTION_TIMEOUT_MS);}
                public void onFailure(int reason){append("connect request: FAILED reason="+reason);abortRun("P2P connect request failed.");}
            });
        }catch(SecurityException e){abortRun("Permission error while connecting P2P.");}
    }

    @SuppressWarnings("deprecation")
    private void requestConnectionInfo(){
        if(reportFinished||phase!=Phase.CONNECTING)return;
        try{
            p2pManager.requestConnectionInfo(p2pChannel,info->{
                if(reportFinished||phase!=Phase.CONNECTING||info==null||!info.groupFormed)return;
                cancelTimeout();phase=Phase.CONNECTED;p2pGroupFormed=true;phoneIsGroupOwner=info.isGroupOwner;
                append("P2P group formed: TRUE");append("Phone is group owner: "+info.isGroupOwner);
                append("Group-owner address: "+(info.groupOwnerAddress==null?"unavailable":info.groupOwnerAddress.getHostAddress()));
                if(!info.isGroupOwner){abortRun("G4B requires confirmed phone-group-owner topology.");return;}
                setStatus("P2P formed. Waiting for passive glasses IP. DO NOT TAKE ANOTHER PHOTO.");
                schedule(()->abortRun("No passive glasses IP available for catalog GET."),CATALOG_WAIT_TIMEOUT_MS);
                maybeStartCatalogGet();
            });
        }catch(SecurityException e){abortRun("Permission error requesting P2P connection info.");}
    }

    private void maybeStartCatalogGet(){
        if(reportFinished||phase!=Phase.CONNECTED||!p2pGroupFormed||!phoneIsGroupOwner||catalogGetAttempted||catalogDelayScheduled||glassesClientIp==null)return;
        if(!isConfirmedP2pIp(glassesClientIp)){abortRun("Resolved IPv4 is outside confirmed 192.168.49.0/24 P2P subnet.");return;}
        cancelTimeout();catalogDelayScheduled=true;phase=Phase.CATALOG_DELAY;
        append("");append("CYAN CATALOG READINESS");append("Delay before catalog GET: "+CATALOG_READY_DELAY_MS+" ms");
        append("Source parity: PictureFragment.downloadMediaConfig() / ktxRunOnUiDelay(0x03e8)");
        setStatus("Waiting exact Cyan 1000 ms before catalog GET. DO NOT TAKE ANOTHER PHOTO.");
        catalogDelayTask=()->{
            catalogDelayTask=null;catalogDelayScheduled=false;
            if(reportFinished||phase!=Phase.CATALOG_DELAY||glassesClientIp==null)return;
            startCatalogGetNow(glassesClientIp);
        };
        handler.postDelayed(catalogDelayTask,CATALOG_READY_DELAY_MS);
    }

    private void startCatalogGetNow(String ip){
        if(reportFinished||catalogGetAttempted||phase!=Phase.CATALOG_DELAY)return;
        catalogGetAttempted=true;catalogGetCount++;httpRequestCount++;phase=Phase.CATALOG_GET;
        append("");append(stage==Stage.BASELINE?"BASELINE CATALOG":stage==Stage.POST_CAPTURE?"POST-CAPTURE CATALOG":stage==Stage.PRE_GET_RETENTION?"PRE-GET RETENTION CATALOG":"POST-GET RETENTION CATALOG");
        append("Method: GET");append("Path: "+CATALOG_PATH);append("Target: passive 0x73/0x08 glasses IPv4 only");
        append("Redirects: DISABLED");append("Max response bytes: "+CATALOG_MAX_BYTES);append("Media-file GET requests so far: "+mediaGetCount);
        setStatus((stage==Stage.BASELINE?"Baseline":stage==Stage.POST_CAPTURE?"Post-capture":stage==Stage.PRE_GET_RETENTION?"Pre-GET retention":"Post-GET retention")+": reading media.config once. DO NOT TAKE A PHOTO.");
        new Thread(()->fetchCatalogOnce(ip),"g6a67-catalog-get").start();
    }

    private void fetchCatalogOnce(String ip){
        HttpURLConnection c=null;
        try{
            URL u=new URL("http://"+ip+CATALOG_PATH);
            c=(HttpURLConnection)u.openConnection();c.setRequestMethod("GET");c.setInstanceFollowRedirects(false);
            c.setConnectTimeout(4000);c.setReadTimeout(4000);c.setUseCaches(false);c.setDoInput(true);c.setDoOutput(false);
            int status=c.getResponseCode();long declared=c.getContentLengthLong();
            if(status!=HttpURLConnection.HTTP_OK){postCatalogFailure("Catalog HTTP status "+status);return;}
            if(declared>CATALOG_MAX_BYTES){postCatalogFailure("Catalog Content-Length exceeds safety cap.");return;}
            byte[] body=readBounded(c.getInputStream(),CATALOG_MAX_BYTES);
            CatalogSummary summary=classifyCatalog(body,c.getContentType(),c.getContentEncoding());
            handler.post(()->processCatalog(summary));
        }catch(Exception e){postCatalogFailure("Catalog GET/parse failed: "+e.getClass().getSimpleName());}
        finally{if(c!=null)c.disconnect();}
    }

    private void processCatalog(CatalogSummary summary){
        if(reportFinished)return;
        append("HTTP status: 200");append("Catalog bytes received: "+summary.bodyBytes);append("Content-Type: "+summary.contentType);
        append("Content-Encoding: "+summary.contentEncoding);append("UTF-8 valid: "+summary.utf8Valid);append("BOM: "+summary.bom);
        append("Cyan parser branch: configFileType=1 / readLines()");append("Line-list entries: "+summary.totalEntries);
        append("Non-empty entries: "+summary.nonEmptyEntries);append("Relative safe entries: "+summary.relativeSafeEntries);
        append("Duplicate entries: "+summary.duplicateEntries);append("Unsafe entry count: "+summary.unsafeEntries);append("Whitespace-altered lines rejected: "+summary.whitespaceAlteredEntries);
        append("Extension summary: "+summary.extensionSummary);append("Filename/path values logged: NO");append("Catalog raw body logged: NO");
        if(!summary.catalogUsable){abortRun("Catalog failed strict safety validation.");return;}

        currentCatalogEntries=summary.safeEntries.size();
        LinkedHashSet<String> allHashes=new LinkedHashSet<>();
        LinkedHashSet<String> jpgHashes=new LinkedHashSet<>();
        for(String entry:summary.safeEntries){
            String h=OpaqueIdentity.sha256(entry);allHashes.add(h);
            if(".jpg".equals(extensionOnly(entry)))jpgHashes.add(h);
        }
        currentJpgEntries=summary.jpgEntries;currentVideoEntries=summary.mp4Entries;currentOpusEntries=summary.opusEntries;
        boolean fullMediaParity=currentImageCount==currentJpgEntries&&currentVideoCount==currentVideoEntries&&currentRecordCount==currentOpusEntries;
        append("Opaque all-entry identities in memory: "+allHashes.size());
        append("Opaque JPG identities in memory: "+jpgHashes.size());
        append("Per-file identity/hash tokens reported: NO");
        append("Remote filename/path values persisted/logged: NO");
        append("Media-file GET requests: "+mediaGetCount);
        append("BLE/catalog full media-count parity: "+fullMediaParity);

        if(stage==Stage.BASELINE){
            if(!fullMediaParity){abortRun("Baseline BLE/catalog media-count parity mismatch.");return;}
            baselineImageCount=currentImageCount;baselineVideoCount=currentVideoCount;baselineRecordCount=currentRecordCount;
            baselineTotalEntries=currentCatalogEntries;baselineJpgEntries=currentJpgEntries;baselineVideoEntries=currentVideoEntries;baselineOpusEntries=currentOpusEntries;
            baselineFullMediaParity=true;
            baselineAllHashes.clear();baselineAllHashes.addAll(allHashes);
            baselineJpgHashes.clear();baselineJpgHashes.addAll(jpgHashes);
            append("Baseline opaque identity sets retained in memory only: YES");
            setStatus("Baseline complete. Exiting transfer before capture.");
            sendExit();return;
        }

        if(stage==Stage.POST_CAPTURE){
            int jpgOverlap=0;for(String h:jpgHashes)if(baselineJpgHashes.contains(h))jpgOverlap++;
            int jpgMissing=baselineJpgHashes.size()-jpgOverlap;
            LinkedHashSet<String> newJpgs=new LinkedHashSet<>();for(String h:jpgHashes)if(!baselineJpgHashes.contains(h))newJpgs.add(h);
            int allOverlap=0;for(String h:allHashes)if(baselineAllHashes.contains(h))allOverlap++;
            int allMissing=baselineAllHashes.size()-allOverlap;
            int allUnexpected=0;for(String h:allHashes)if(!baselineAllHashes.contains(h))allUnexpected++;

            boolean exactCounts=currentImageCount==baselineImageCount+1&&currentVideoCount==baselineVideoCount&&currentRecordCount==baselineRecordCount;
            boolean exactCatalogCounts=currentCatalogEntries==baselineTotalEntries+1&&currentJpgEntries==baselineJpgEntries+1&&currentVideoEntries==baselineVideoEntries&&currentOpusEntries==baselineOpusEntries;
            boolean exactMembership=jpgMissing==0&&newJpgs.size()==1&&allMissing==0&&allUnexpected==1;
            postCaptureFullMediaParity=fullMediaParity;
            postCaptureCatalogExact=exactCounts&&exactCatalogCounts&&exactMembership&&postCaptureFullMediaParity;

            append("");append("POST-CAPTURE CATALOG COMPARISON");
            append("Exact +1 image with other BLE counts unchanged: "+exactCounts);
            append("Exact +1 safe entry and +1 JPG only: "+exactCatalogCounts);
            append("Baseline JPG identities retained: "+jpgOverlap+"/"+baselineJpgHashes.size());
            append("Baseline JPG identities missing: "+jpgMissing);
            append("New JPG identities: "+newJpgs.size());
            append("Baseline safe identities retained: "+allOverlap+"/"+baselineAllHashes.size());
            append("Baseline safe identities missing: "+allMissing);
            append("Unexpected safe identities: "+allUnexpected);
            append("Post-capture BLE/catalog full media parity: "+postCaptureFullMediaParity);
            append("Exact single new JPG catalog delta: "+postCaptureCatalogExact);
            if(!postCaptureCatalogExact){abortRun("Post-capture catalog did not contain exactly one new retained JPG with the baseline intact.");return;}

            newJpgHash=newJpgs.iterator().next();
            postCaptureImageCount=currentImageCount;postCaptureVideoCount=currentVideoCount;postCaptureRecordCount=currentRecordCount;
            postCaptureTotalEntries=currentCatalogEntries;postCaptureJpgEntries=currentJpgEntries;postCaptureVideoEntries=currentVideoEntries;postCaptureOpusEntries=currentOpusEntries;
            postCaptureAllHashes.clear();postCaptureAllHashes.addAll(allHashes);
            postCaptureJpgHashes.clear();postCaptureJpgHashes.addAll(jpgHashes);
            append("New JPG opaque identity retained in memory for later exact GET selection: YES");
            setStatus("Exact +1 catalog confirmed. Exiting before pre-GET retention reconnect.");
            sendExit();return;
        }

        boolean countsEqual=currentImageCount==postCaptureImageCount&&currentVideoCount==postCaptureVideoCount&&currentRecordCount==postCaptureRecordCount;
        boolean catalogCountsEqual=currentCatalogEntries==postCaptureTotalEntries&&currentJpgEntries==postCaptureJpgEntries&&currentVideoEntries==postCaptureVideoEntries&&currentOpusEntries==postCaptureOpusEntries;
        boolean allSetEqual=allHashes.equals(postCaptureAllHashes);
        boolean jpgSetEqual=jpgHashes.equals(postCaptureJpgHashes);
        boolean newJpgPresent=newJpgHash!=null&&jpgHashes.contains(newJpgHash);
        int baselineMissing=0;for(String h:baselineJpgHashes)if(!jpgHashes.contains(h))baselineMissing++;
        boolean exactRetention=countsEqual&&catalogCountsEqual&&allSetEqual&&jpgSetEqual&&newJpgPresent&&baselineMissing==0&&fullMediaParity;

        if(stage==Stage.PRE_GET_RETENTION){
            preGetRetentionExact=exactRetention;
            append("");append("PRE-GET RETENTION COMPARISON");
            append("Inventory counts retained: "+countsEqual);
            append("Catalog media counts retained: "+catalogCountsEqual);
            append("Full safe identity set retained exactly: "+allSetEqual);
            append("JPG identity set retained exactly: "+jpgSetEqual);
            append("New JPG identity still present before GET: "+newJpgPresent);
            append("Baseline JPG identities missing before GET: "+baselineMissing);
            append("Pre-GET BLE/catalog full media parity: "+fullMediaParity);
            if(!preGetRetentionExact){abortRun("New JPG/catalog did not survive the pre-GET verified reconnect.");return;}

            String candidate=null;
            for(String entry:summary.safeEntries){
                if(".jpg".equals(extensionOnly(entry))&&OpaqueIdentity.sha256(entry).equals(newJpgHash)){candidate=entry;break;}
            }
            if(candidate==null||!isStrictMediaRelativePath(candidate)){abortRun("Exact new JPG could not be safely resolved from the current pre-GET catalog.");return;}
            append("Exact new JPG resolved from current catalog by opaque identity: YES");
            append("Remote filename/path value logged: NO");
            setStatus("Pre-GET retention proven. Downloading the exact new JPG once.");
            startMediaGet(glassesClientIp,candidate);
            return;
        }

        postGetRetentionExact=exactRetention;
        append("");append("STABLE POST-GET RECONNECT COMPARISON");
        append("Fresh reconnect inventory equals pre-GET/post-capture counts: "+countsEqual);
        append("Fresh reconnect catalog media counts equal pre-GET state: "+catalogCountsEqual);
        append("Fresh reconnect full safe identity set equals pre-GET state: "+allSetEqual);
        append("Fresh reconnect JPG identity set equals pre-GET state: "+jpgSetEqual);
        append("Downloaded new JPG identity still present after fresh reconnect: "+newJpgPresent);
        append("Baseline JPG identities missing after media GET: "+baselineMissing);
        append("Post-GET BLE/catalog full media parity: "+fullMediaParity);

        if(postGetRetentionExact){
            if(postGetExitInventoryObserved&&!postGetExitInventoryMatchedPreGet)
                retentionClassification="TRANSIENT EXIT-TIME INVENTORY TRANSITION — STABLE REMOTE CATALOG/INVENTORY RECOVERED AFTER FRESH RECONNECT";
            else
                retentionClassification="NO REMOTE RETENTION CHANGE — EXACT NEW JPG REMAINED PRESENT AFTER ONE MEDIA GET";
        }else if(!newJpgPresent)retentionClassification="PERSISTENT CHANGE — DOWNLOADED NEW JPG ABSENT AFTER FRESH POST-GET RECONNECT";
        else if(baselineMissing>0)retentionClassification="PERSISTENT CHANGE — BASELINE JPG MEMBERSHIP CHANGED AFTER FRESH POST-GET RECONNECT";
        else if(!countsEqual||!catalogCountsEqual)retentionClassification="PERSISTENT CHANGE — INVENTORY/CATALOG COUNTS CHANGED AFTER FRESH POST-GET RECONNECT";
        else retentionClassification="PERSISTENT CHANGE — CATALOG IDENTITIES CHANGED AFTER FRESH POST-GET RECONNECT";

        append("G6A POST-GET TRANSITION CLASSIFICATION: "+retentionClassification);
        comparisonCompleted=true;
        setStatus("Post-GET retention comparison complete. Exiting transfer mode.");
        sendExit();
    }

    private void startMediaGet(String ip,String candidate){
        if(reportFinished||stage!=Stage.PRE_GET_RETENTION||phase!=Phase.CATALOG_GET)return;
        if(mediaGetCount!=0){abortRun("Media GET already attempted.");return;}
        if(!preGetRetentionExact){abortRun("Media GET blocked because pre-GET retention was not proven.");return;}
        if(!isConfirmedP2pIp(ip)||!isStrictMediaRelativePath(candidate)||!".jpg".equals(extensionOnly(candidate))||!OpaqueIdentity.sha256(candidate).equals(newJpgHash)){
            abortRun("Exact new JPG failed final pre-request guard.");return;
        }
        mediaGetCount++;httpRequestCount++;phase=Phase.MEDIA_GET;
        append("");append("ONE DISPOSABLE JPG GET");
        append("Method: GET");
        append("Target: passive glasses IPv4 + /files/ + exact new JPG from current catalog");
        append("Redirects: DISABLED");
        append("Retry/resume/Range: DISABLED");
        append("Max media bytes: "+MEDIA_MAX_BYTES);
        append("Persistent import/ledger: DISABLED");
        append("Remote filename/path value logged: NO");
        new Thread(()->downloadMediaOnce(ip,candidate),"g6a67-one-media-get").start();
    }

    private void downloadMediaOnce(String ip,String candidate){
        HttpURLConnection conn=null;File localFile=null;
        try{
            URL u=new URL("http://"+ip+MEDIA_PREFIX+candidate);
            conn=(HttpURLConnection)u.openConnection();
            conn.setRequestMethod("GET");conn.setInstanceFollowRedirects(false);
            conn.setConnectTimeout(4000);conn.setReadTimeout(15000);conn.setUseCaches(false);conn.setDoInput(true);conn.setDoOutput(false);
            int status=conn.getResponseCode();long declared=conn.getContentLengthLong();
            if(status!=HttpURLConnection.HTTP_OK){postMediaFailure("Media HTTP status "+status);return;}
            if(declared>MEDIA_MAX_BYTES){postMediaFailure("Media Content-Length exceeds safety cap.");return;}

            localFile=new File(getCacheDir(),"g6a66_disposable.jpg");
            MediaSummary m=streamMediaBounded(conn.getInputStream(),localFile,MEDIA_MAX_BYTES);
            m.declaredLength=declared;m.contentType=safeHeader(conn.getContentType());
            if(m.bytes<=0){postMediaFailure("Downloaded media is empty.");return;}
            if(declared>=0&&m.bytes!=declared){postMediaFailure("Downloaded byte count does not match Content-Length.");return;}
            if(!m.jpegStart||!m.jpegEnd){postMediaFailure("Downloaded media failed JPEG signature validation.");return;}
            tempCleanupPass=!localFile.exists()||localFile.delete();
            if(!tempCleanupPass){postMediaFailure("Validated temporary file could not be removed.");return;}

            handler.post(()->{
                if(reportFinished)return;
                downloadedBytes=m.bytes;declaredMediaBytes=m.declaredLength;mediaValidated=true;
                append("Media HTTP status: 200");
                append("Media Content-Type: "+m.contentType);
                append("Declared Content-Length: "+(m.declaredLength<0?"<none>":Long.toString(m.declaredLength)));
                append("Downloaded bytes: "+m.bytes);
                append("Content-Length consistency: "+(m.declaredLength<0?"NOT PROVIDED":"PASS"));
                append("Media size cap: PASS");
                append("JPEG SOI signature: PASS");
                append("JPEG EOI signature: PASS");
                append("App-private temporary file created: YES");
                append("Temporary file cleanup after validation: PASS");
                append("Persistent import created: NO");
                append("Persistent ledger updated: NO");
                append("Local temporary path logged: NO");
                append("Remote filename/path value logged: NO");
                append("Media-file GET requests: "+mediaGetCount);
                setStatus("One JPG GET validated and temp file deleted. Exiting before post-GET reconnect.");
                sendExit();
            });
        }catch(Exception e){postMediaFailure("Media GET/stream failed: "+e.getClass().getSimpleName());}
        finally{
            if(localFile!=null&&localFile.exists())localFile.delete();
            if(conn!=null)conn.disconnect();
        }
    }

    private MediaSummary streamMediaBounded(InputStream input,File file,int maxBytes)throws IOException{
        MediaSummary m=new MediaSummary();int firstCount=0,prev=-1,last=-1;
        try(InputStream in=input;FileOutputStream out=new FileOutputStream(file)){
            byte[] buf=new byte[8192];long total=0;
            while(true){
                int n=in.read(buf);if(n<0)break;
                if(total+n>maxBytes)throw new IOException("media too large");
                for(int i=0;i<n;i++){int v=buf[i]&255;if(firstCount<3)m.first[firstCount++]=v;prev=last;last=v;}
                out.write(buf,0,n);total+=n;
            }
            out.flush();out.getFD().sync();m.bytes=total;
        }
        m.jpegStart=firstCount>=3&&m.first[0]==0xFF&&m.first[1]==0xD8&&m.first[2]==0xFF;
        m.jpegEnd=prev==0xFF&&last==0xD9;return m;
    }

    private void postMediaFailure(String reason){handler.post(()->{if(!reportFinished)abortRun(reason);});}
    private static final class MediaSummary{
        long bytes,declaredLength=-1;String contentType="<none>";boolean jpegStart,jpegEnd;final int[] first=new int[3];
    }

    private void postCatalogFailure(String reason){handler.post(()->{if(!reportFinished)abortRun(reason);});}

    private static byte[] readBounded(InputStream in,int maxBytes)throws IOException{
        try(InputStream input=in;ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] buf=new byte[4096];int total=0;
            while(true){int n=input.read(buf);if(n<0)break;total+=n;if(total>maxBytes)throw new IOException("catalog too large");out.write(buf,0,n);}
            return out.toByteArray();
        }
    }

    private static CatalogSummary classifyCatalog(byte[] body,String contentType,String contentEncoding)throws IOException{
        CatalogSummary out=new CatalogSummary();out.bodyBytes=body==null?0:body.length;out.contentType=safeHeader(contentType);out.contentEncoding=safeHeader(contentEncoding);
        out.bom=detectBom(body);out.utf8Valid=isStrictUtf8(body);if(!out.utf8Valid)return out;
        if(!"NONE".equals(out.bom))return out;
        String raw=new String(body==null?new byte[0]:body,StandardCharsets.UTF_8);
        TreeMap<String,Integer> extensions=new TreeMap<>();
        try(BufferedReader reader=new BufferedReader(new StringReader(raw))){
            String line;
            while((line=reader.readLine())!=null){
                out.totalEntries++;if(out.totalEntries>CATALOG_MAX_ITEMS){out.overItemCap=true;break;}
                if(line.isEmpty()){out.blankEntries++;out.unsafeEntries++;continue;}
                out.nonEmptyEntries++;
                if(!line.equals(line.trim())){out.whitespaceAlteredEntries++;out.unsafeEntries++;continue;}
                boolean strictSafe=isStrictMediaRelativePath(line);
                if(strictSafe){
                    out.relativeSafeEntries++;
                    if(!out.safeEntries.add(line))out.duplicateEntries++;
                    String ext=extensionOnly(line);
                    extensions.put(ext,extensions.getOrDefault(ext,0)+1);
                    if(".jpg".equals(ext))out.jpgEntries++;
                    else if(".mp4".equals(ext))out.mp4Entries++;
                    else if(".opus".equals(ext))out.opusEntries++;
                }else out.unsafeEntries++;
            }
        }
        out.extensionSummary=summarizeExtensions(extensions);
        out.catalogUsable=!out.overItemCap&&out.totalEntries>0&&out.nonEmptyEntries>0&&out.blankEntries==0&&out.unsafeEntries==0&&out.duplicateEntries==0&&out.relativeSafeEntries==out.nonEmptyEntries&&out.safeEntries.size()==out.nonEmptyEntries;
        return out;
    }

    private static final class CatalogSummary{
        int bodyBytes,totalEntries,nonEmptyEntries,relativeSafeEntries,duplicateEntries,unsafeEntries,blankEntries,whitespaceAlteredEntries,jpgEntries,mp4Entries,opusEntries;boolean overItemCap,utf8Valid,catalogUsable;
        String contentType="<none>",contentEncoding="<none>",bom="NONE",extensionSummary="<none>";
        final LinkedHashSet<String> safeEntries=new LinkedHashSet<>();
    }

    private static String summarizeExtensions(TreeMap<String,Integer> m){
        if(m.isEmpty())return "<none>";StringBuilder b=new StringBuilder();
        for(Map.Entry<String,Integer> e:m.entrySet()){if(b.length()>0)b.append(", ");b.append(e.getKey()).append("=").append(e.getValue());}
        return b.toString();
    }

    private static boolean hasScheme(String s){
        if(s==null||s.length()<2)return false;int colon=s.indexOf(':');if(colon<=0||colon>16)return false;
        if(!Character.isLetter(s.charAt(0)))return false;
        for(int i=1;i<colon;i++){char ch=s.charAt(i);if(!(Character.isLetterOrDigit(ch)||ch=='+'||ch=='-'||ch=='.'))return false;}return true;
    }
    private static boolean startsWithSlash(String s){return s!=null&&!s.isEmpty()&&(s.charAt(0)=='/'||s.charAt(0)=='\\');}
    private static boolean hasTraversalSegment(String s){if(s==null)return false;for(String p:s.replace('\\','/').split("/",-1))if("..".equals(p))return true;return false;}
    private static boolean hasControlCharacter(String s){if(s==null)return false;for(int i=0;i<s.length();i++){char ch=s.charAt(i);if(ch<0x20||ch==0x7F)return true;}return false;}
    private static boolean isStrictMediaRelativePath(String s){
        if(s==null||s.isEmpty()||s.length()>240)return false;
        if(hasScheme(s)||startsWithSlash(s)||hasTraversalSegment(s)||hasControlCharacter(s))return false;
        if(s.indexOf('?')>=0||s.indexOf('#')>=0||s.indexOf('%')>=0||s.indexOf(':')>=0||s.indexOf('\\')>=0)return false;
        if(s.startsWith(".")||s.endsWith(".")||s.contains("//"))return false;
        for(String part:s.split("/",-1)){
            if(part.isEmpty()||".".equals(part)||"..".equals(part))return false;
            for(int i=0;i<part.length();i++){char ch=part.charAt(i);if(!(Character.isLetterOrDigit(ch)||ch=='_'||ch=='-'||ch=='.'))return false;}
        }
        return true;
    }

    private static String extensionOnly(String path){
        int slash=Math.max(path.lastIndexOf('/'),path.lastIndexOf('\\'));String name=slash>=0?path.substring(slash+1):path;
        int dot=name.lastIndexOf('.');if(dot<0||dot==name.length()-1)return "<none>";
        String ext=name.substring(dot).toLowerCase(Locale.US);return ext.matches("\\.[a-z0-9]{1,8}")?ext:"<other>";
    }

    private static String detectBom(byte[] b){
        if(b==null||b.length<2)return "NONE";
        if(b.length>=3&&(b[0]&255)==0xEF&&(b[1]&255)==0xBB&&(b[2]&255)==0xBF)return "UTF-8";
        if((b[0]&255)==0xFF&&(b[1]&255)==0xFE)return "UTF-16LE";
        if((b[0]&255)==0xFE&&(b[1]&255)==0xFF)return "UTF-16BE";return "NONE";
    }
    private static boolean isStrictUtf8(byte[] b){
        if(b==null)return true;
        try{StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(b));return true;}
        catch(CharacterCodingException e){return false;}
    }
    private static String safeHeader(String s){
        if(s==null||s.isEmpty())return "<none>";StringBuilder b=new StringBuilder();
        for(int i=0;i<s.length()&&i<96;i++){char ch=s.charAt(i);b.append((ch>=32&&ch<=126)?ch:'?');}return b.toString();
    }

    private static boolean isConfirmedP2pIp(String ip){
        if(ip==null)return false;String[] p=ip.split("\\.");if(p.length!=4)return false;
        try{int a=Integer.parseInt(p[0]),b=Integer.parseInt(p[1]),c=Integer.parseInt(p[2]),d=Integer.parseInt(p[3]);return a==192&&b==168&&c==49&&d>=2&&d<=254;}
        catch(NumberFormatException e){return false;}
    }

    private void sendExit(){
        cancelTimeout();
        if(reportFinished||exitWriteAttempted){
            if(failurePending)startLocalCleanup();
            return;
        }
        append("");append("EXIT TRANSFER MODE");append("Command: 0x41 / 02 01 09");append("No retry policy: TRUE");
        exitWriteAttempted=true;exitWriteCallbackSucceeded=false;postExitInventoryConfirmed=false;exitWriteCount++;phase=Phase.EXIT_SENT;
        if(gatt==null||!writeFrame(gatt,frame41(EXIT_TRANSFER))){
            if(!failurePending){failurePending=true;failureReason="Exit-transfer write did not start; snapshot separation not proven.";}
            startLocalCleanup();
            return;
        }
        append("EXIT write start: SUCCESS");
        schedule(()->{
            if(reportFinished||phase!=Phase.EXIT_SENT)return;
            append("Exit write callback observed: "+exitWriteCallbackSucceeded);
            append("Post-exit matching 0x73/0x01 observed: "+postExitInventoryConfirmed);
            if(!failurePending){failurePending=true;failureReason="Post-exit inventory confirmation timed out; snapshot separation not proven.";}
            startLocalCleanup();
        },5000);
    }

    private void abortRun(String reason){
        if(reportFinished)return;
        cancelTimeout();cancelQuiet();
        if(!failurePending){
            failurePending=true;
            failureReason=reason;
            append("G6A STABILITY ERROR: "+reason);
        }
        if(phase==Phase.CLEANUP)return;
        if(enterWriteStarted&&!exitWriteAttempted&&gatt!=null&&writeChar!=null){
            append("Abort recovery: attempting the single allow-listed transfer-exit write.");
            sendExit();
            return;
        }
        startLocalCleanup();
    }

    private void startLocalCleanup(){
        if(reportFinished||phase==Phase.CLEANUP)return;
        phase=Phase.CLEANUP;cancelTimeout();append("");append("LOCAL P2P CLEANUP");
        cleanupCompletionHandled=false;
        cleanupFallbackTask=()->{
            cleanupFallbackTask=null;
            if(reportFinished||cleanupCompletionHandled)return;
            cleanupCompletionHandled=true;
            append("removeGroup/group-state callback: TIMEOUT");
            if(!failurePending){failurePending=true;failureReason="Wi-Fi Direct cleanup could not be proven.";}
            finishAfterCleanup();
        };
        handler.postDelayed(cleanupFallbackTask,4000);
        try{
            if(p2pManager!=null&&p2pChannel!=null){
                p2pManager.removeGroup(p2pChannel,new WifiP2pManager.ActionListener(){
                    public void onSuccess(){append("removeGroup request: SUCCESS");verifyGroupAbsent();}
                    public void onFailure(int r){append("removeGroup request: FAILED reason="+r);verifyGroupAbsent();}
                });
            }else{
                if(!failurePending){failurePending=true;failureReason="Wi-Fi Direct manager/channel unavailable during cleanup.";}
                completeCleanupOnce();
            }
        }catch(SecurityException e){
            append("removeGroup request: permission error");
            if(!failurePending){failurePending=true;failureReason="Wi-Fi Direct cleanup permission error.";}
            completeCleanupOnce();
        }
    }

    private void verifyGroupAbsent(){
        if(reportFinished||cleanupCompletionHandled)return;
        try{
            p2pManager.requestGroupInfo(p2pChannel,group->{
                if(reportFinished||cleanupCompletionHandled)return;
                boolean present=group!=null;
                append("P2P group present after cleanup request: "+present);
                if(present&&!failurePending){failurePending=true;failureReason="P2P group remained after cleanup; snapshot separation not proven.";}
                completeCleanupOnce();
            });
        }catch(SecurityException e){
            append("P2P group absence verification: permission error");
            if(!failurePending){failurePending=true;failureReason="Could not verify P2P group absence after cleanup.";}
            completeCleanupOnce();
        }
    }

    private void completeCleanupOnce(){
        if(cleanupCompletionHandled)return;
        cleanupCompletionHandled=true;
        if(cleanupFallbackTask!=null){handler.removeCallbacks(cleanupFallbackTask);cleanupFallbackTask=null;}
        finishAfterCleanup();
    }

    private void finishAfterCleanup(){
        if(failurePending){
            String reason=failureReason==null?"Single-JPG GET retention diagnostic did not complete.":failureReason;
            cleanupRuntime(destroyRequested);append("");append("SUMMARY");
            append("Stage at failure: "+stage);append("Capture-watch inventory events: "+captureWatchInventoryEvents);
            append("Media-count queries: "+countWriteCount);append("P2P enter writes: "+enterWriteCount);
            append("Transfer-exit writes: "+exitWriteCount);append("Catalog GET requests: "+catalogGetCount);
            append("Media-file GET requests: "+mediaGetCount);append("Total HTTP GET requests: "+httpRequestCount);
            append("Media validated: "+mediaValidated);append("Temporary file cleanup: "+tempCleanupPass);
            append("Remote filename/path values logged/persisted: NO");append("Glasses file mutation/deletion: 0");
            append("G6A SINGLE-JPG GET RETENTION RESULT: FAILED — "+reason);append("END REPORT");
            reportFinished=true;runActive=false;phase=Phase.COMPLETE;
            if(!destroyRequested){setStatus(reason);enableActions();}
            return;
        }

        if(stage==Stage.BASELINE){
            cleanupRuntime(false);phase=Phase.COMPLETE;
            append("");append("BASELINE COMPLETE");
            append("Post-exit 0x73/0x01 confirmation: YES");
            append("P2P group absence verified before capture: YES");
            startCaptureWatchConnection();return;
        }

        if(stage==Stage.POST_CAPTURE){
            cleanupRuntime(false);phase=Phase.COMPLETE;
            append("");append("POST-CAPTURE SNAPSHOT COMPLETE");
            append("Exact single new JPG catalog delta: YES");
            append("Post-exit 0x73/0x01 confirmation: YES");
            append("P2P group absence verified before pre-GET reconnect: YES");
            append("Media-file GET requests: 0");
            append("");append("PRE-GET RETENTION RECONNECT DELAY");append("Duration: "+RECONNECT_DELAY_MS+" ms");
            setStatus("Waiting before pre-GET retention reconnect. Do not take any more photos.");
            quietTask=()->{
                quietTask=null;if(reportFinished||!runActive)return;
                stage=Stage.PRE_GET_RETENTION;append("");append("PRE-GET RETENTION SNAPSHOT");startSnapshotConnection();
            };
            handler.postDelayed(quietTask,RECONNECT_DELAY_MS);return;
        }

        if(stage==Stage.PRE_GET_RETENTION){
            if(!preGetRetentionExact||!mediaValidated||mediaGetCount!=1||!postGetExitInventoryObserved){
                failurePending=true;
                failureReason="Pre-GET/GET stage ended without proven retention, one validated media GET, and a valid post-GET exit inventory observation.";
                finishAfterCleanup();
                return;
            }
            cleanupRuntime(false);phase=Phase.COMPLETE;
            append("");append("ONE JPG GET + EXIT OBSERVATION COMPLETE");
            append("Pre-GET retention proven: YES");
            append("Media validated: YES");
            append("Downloaded bytes: "+downloadedBytes);
            append("Temporary file cleanup: PASS");
            append("Valid post-GET exit 0x73/0x01 observed: YES");
            append("Post-GET exit inventory matched pre-GET snapshot: "+postGetExitInventoryMatchedPreGet);
            append("Post-GET exit inventory: images="+postGetExitImageCount+", videos="+postGetExitVideoCount+", recordings="+postGetExitRecordCount+", configType="+postGetExitConfigType+", AP-only="+postGetExitOnlySupportApImport);
            append("P2P group absence verified after media GET: YES");
            append("");append("POST-GET RETENTION RECONNECT DELAY");append("Duration: "+RECONNECT_DELAY_MS+" ms");
            setStatus("Waiting before post-GET retention reconnect. Do not take any more photos.");
            quietTask=()->{
                quietTask=null;if(reportFinished||!runActive)return;
                stage=Stage.POST_GET_RETENTION;append("");append("POST-GET RETENTION SNAPSHOT");startSnapshotConnection();
            };
            handler.postDelayed(quietTask,RECONNECT_DELAY_MS);return;
        }

        if(stage==Stage.POST_GET_RETENTION&&comparisonCompleted){
            cleanupRuntime(false);append("");append("SUMMARY");
            append("Capture action issued by app: NO");
            append("Physical capture requested by app: EXACTLY ONE");
            append("Passive exact +1 capture visibility observed: "+capturePassiveConfirmed);
            append("Pre-GET retention exact: "+preGetRetentionExact);
            append("One media GET validated: "+mediaValidated);
            append("Temporary file cleanup: "+tempCleanupPass);
            append("Post-GET exit inventory observed: "+postGetExitInventoryObserved);
            append("Post-GET exit inventory matched pre-GET snapshot: "+postGetExitInventoryMatchedPreGet);
            append("Post-GET exit observed counts: images="+postGetExitImageCount+", videos="+postGetExitVideoCount+", recordings="+postGetExitRecordCount+", configType="+postGetExitConfigType);
            append("Fresh post-GET retention exact: "+postGetRetentionExact);
            append("Media-count queries: "+countWriteCount);append("P2P enter writes: "+enterWriteCount);
            append("Transfer-exit writes: "+exitWriteCount);append("Catalog GET requests: "+catalogGetCount);
            append("Media-file GET requests: "+mediaGetCount);append("Total HTTP GET requests: "+httpRequestCount);
            boolean exactOperationTotals=countWriteCount==4&&enterWriteCount==4&&exitWriteCount==4&&catalogGetCount==4&&mediaGetCount==1&&httpRequestCount==5;
            append("Exact bounded operation totals: "+exactOperationTotals);
            append("Verified exit/group cleanup completed for all four transport snapshots: YES");
            append("Persistent import/ledger: NONE");
            append("Remote filename/path values logged/persisted: NO");append("Glasses file mutation/deletion: 0");
            boolean diagnosticComplete=postCaptureCatalogExact&&preGetRetentionExact&&mediaValidated&&tempCleanupPass&&postGetExitInventoryObserved&&exactOperationTotals&&capturePassiveConfirmed;
            if(!exactOperationTotals)append("G6A POST-GET TRANSITION OBSERVER RESULT: FAILED — OPERATION TOTALS DEVIATED FROM BOUNDED CONTRACT");
            else append("G6A POST-GET TRANSITION OBSERVER RESULT: COMPLETE — "+retentionClassification);
            append("END REPORT");
            reportFinished=true;runActive=false;phase=Phase.COMPLETE;
            if(!destroyRequested){setStatus(diagnosticComplete?"Post-GET transition classified. Copy the report.":"Post-GET observer incomplete. Copy the report.");enableActions();}
            return;
        }

        abortRun("Unexpected cleanup state.");
    }

    private boolean writeFrame(BluetoothGatt x,byte[] frame){
        if(writeChar==null||!validFrame(frame))return false;
        writeChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);writeChar.setValue(frame);
        try{return x.writeCharacteristic(writeChar);}catch(Exception e){return false;}
    }
    private static byte[] frame41(byte[] payload){
        int crc=crc16(payload);byte[] f=new byte[payload.length+6];f[0]=(byte)0xBC;f[1]=0x41;f[2]=(byte)payload.length;f[3]=0;f[4]=(byte)(crc&255);f[5]=(byte)((crc>>>8)&255);System.arraycopy(payload,0,f,6,payload.length);return f;
    }
    private static int crc16(byte[] d){int c=0xffff;for(byte b:d){c^=b&255;for(int i=0;i<8;i++)c=(c&1)!=0?(c>>>1)^0xA001:c>>>1;}return c&0xffff;}
    private static boolean validFrame(byte[] f){
        if(f==null||f.length<6||(f[0]&255)!=0xBC)return false;int l=(f[2]&255)|((f[3]&255)<<8);if(f.length!=l+6)return false;
        byte[] p=Arrays.copyOfRange(f,6,f.length);int e=(f[4]&255)|((f[5]&255)<<8);return crc16(p)==e;
    }

    private void schedule(Runnable r,long ms){cancelTimeout();timeout=r;handler.postDelayed(r,ms);}
    private void cancelTimeout(){if(timeout!=null){handler.removeCallbacks(timeout);timeout=null;}}
    private void cancelQuiet(){if(quietTask!=null){handler.removeCallbacks(quietTask);quietTask=null;}}
    private void closeAndAbort(String m){handler.post(()->abortRun(m));}

    private void cleanupRuntime(boolean destroy){
        cancelTimeout();
        if(catalogDelayTask!=null){handler.removeCallbacks(catalogDelayTask);catalogDelayTask=null;}catalogDelayScheduled=false;
        if(cleanupFallbackTask!=null){handler.removeCallbacks(cleanupFallbackTask);cleanupFallbackTask=null;}
        expectedP2pName=null;credentialPassword=null;
        if(receiverRegistered&&p2pReceiver!=null){try{unregisterReceiver(p2pReceiver);}catch(Exception ignored){}receiverRegistered=false;p2pReceiver=null;}
        BluetoothGatt x=gatt;gatt=null;writeChar=null;if(x!=null){try{x.disconnect();}catch(Exception ignored){}try{x.close();}catch(Exception ignored){}}
        if(destroy&&p2pChannel!=null&&Build.VERSION.SDK_INT>=27){try{p2pChannel.close();}catch(Exception ignored){}}
    }

    private void copy(){
        ClipboardManager c=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        if(c!=null){c.setPrimaryClip(ClipData.newPlainText("G6A Post-GET Transition Observer Report",report.toString()));Toast.makeText(this,"Report copied",Toast.LENGTH_SHORT).show();}
    }
    private void share(){
        Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_SUBJECT,"AIMB-G1 G6A Post-GET Transition Observer Report");i.putExtra(Intent.EXTRA_TEXT,report.toString());startActivity(Intent.createChooser(i,"Share report"));
    }
    private void enableActions(){copyButton.setEnabled(report.length()>0);shareButton.setEnabled(report.length()>0);runButton.setEnabled(true);reportView.setText(report.toString());}
    private void append(String s){report.append(s).append('\n');reportView.setText(report.toString());}
    private void appendThreadSafe(String s){handler.post(()->{if(!reportFinished)append(s);});}
    private void setStatus(String s){statusView.setText(s);}
    private static boolean matchesTarget(String n){if(n==null)return false;String x=n.trim().toUpperCase(Locale.US);return x.equals(TARGET_NAME)||x.startsWith(TARGET_NAME+"_");}
    private static String safeGetName(BluetoothDevice d){try{return d==null?null:d.getName();}catch(SecurityException e){return null;}}
    private static String safeName(String n){if(n==null)return"<none>";String x=n.trim().toUpperCase(Locale.US);return x.equals(TARGET_NAME)?TARGET_NAME:x.startsWith(TARGET_NAME+"_")?TARGET_NAME+"_<suffix>":"<other>";}
    private static String isoNow(){return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ",Locale.US).format(new Date());}
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}

    @Override protected void onStop(){
        super.onStop();
        if(runActive&&!reportFinished&&!isChangingConfigurations())abortRun("App left foreground during controlled post-GET transition observer.");
    }
    @Override protected void onDestroy(){
        destroyRequested=true;
        cancelQuiet();
        if(runActive&&!reportFinished)abortRun("Activity destroyed during controlled post-GET transition observer.");
        else cleanupRuntime(true);
        super.onDestroy();
    }
}
