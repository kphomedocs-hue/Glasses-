package com.parkarsite.g6aimport60;

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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.time.ZoneId;
import com.parkarsite.g6a.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;


@SuppressLint("MissingPermission")
public final class MainActivity extends Activity {
    private static final int REQ_PERMISSION = 4101;
    private static final String TARGET_NAME = "AIMB-G1";
    private static final long CONNECT_TIMEOUT_MS = 30000L;
    private static final long P2P_DISCOVERY_TIMEOUT_MS = 20000L;
    private static final long P2P_CONNECTION_TIMEOUT_MS = 15000L;
    private static final long CATALOG_WAIT_TIMEOUT_MS = 8000L;
    private static final long CATALOG_READY_DELAY_MS = 1000L;
    private static final long VISIBILITY_TIMEOUT_MS = 60000L;
    private static final int CATALOG_MAX_BYTES = 65536;
    private static final int CATALOG_MAX_ITEMS = 256;
    private static final int MEDIA_MAX_BYTES = 33554432;
    private static final String CATALOG_PATH = "/files/media.config";
    private static final String MEDIA_PREFIX = "/files/";

    private static final UUID CYAN_SERVICE = UUID.fromString("de5bf728-d711-4e47-af26-65e3012a5dc7");
    private static final UUID CYAN_NOTIFY = UUID.fromString("de5bf729-d711-4e47-af26-65e3012a5dc7");
    private static final UUID CYAN_WRITE = UUID.fromString("de5bf72a-d711-4e47-af26-65e3012a5dc7");
    private static final UUID CLIENT_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private static final byte[] MEDIA_COUNT = new byte[]{0x02,0x04};
    private static final byte[] ENTER_P2P = new byte[]{0x02,0x01,0x04,0x01};
    private static final byte[] EXIT_TRANSFER = new byte[]{0x02,0x01,0x09};

    private enum Phase { IDLE, VISIBILITY_WATCH, COUNT_SENT, ENTER_SENT, DISCOVERING, CONNECTING, CONNECTED, CATALOG_DELAY, CATALOG_GET, MEDIA_GET, EXIT_SENT, CLEANUP, COMPLETE }
    private enum Stage { BASELINE, WAIT_FOR_CAPTURE, DOWNLOAD }

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final StringBuilder report = new StringBuilder();

    private BluetoothAdapter btAdapter;
    private BluetoothGatt gatt;
    private BluetoothGattCharacteristic writeChar;

    private WifiP2pManager p2pManager;
    private WifiP2pManager.Channel p2pChannel;
    private BroadcastReceiver p2pReceiver;
    private boolean receiverRegistered;

    private TextView statusView, reportView;
    private Button runButton, copyButton, shareButton;

    private Phase phase = Phase.IDLE;
    private Stage stage = Stage.BASELINE;
    private boolean reportFinished;
    private boolean countWriteAttempted;
    private boolean countWriteCallbackSucceeded;
    private boolean countResponseReceived;
    private boolean enterWriteAttempted;
    private boolean exitWriteAttempted;
    private boolean targetPeerMatched;
    private boolean failurePending;
    private boolean ipEventObserved;
    private String failureReason;
    private String glassesClientIp;
    private boolean p2pGroupFormed;
    private boolean phoneIsGroupOwner;
    private boolean catalogGetAttempted;
    private boolean catalogSuccess;
    private boolean baselineReady;
    private boolean mediaSuccess;
    private int httpRequestCount;
    private int catalogGetCount;
    private int mediaGetCount;
    private int countWriteCount;
    private int enterWriteCount;
    private int exitWriteCount;
    private int currentCatalogEntries;
    private int deltaCount;
    private int currentImageCount=-1,currentVideoCount=-1,currentRecordCount=-1,currentConfigFileType=-1;
    private boolean currentOnlySupportApImport;
    private int baselineImageCount=-1,baselineVideoCount=-1,baselineRecordCount=-1;
    private long downloadedBytes;
    private final LinkedHashSet<String> baselineCatalog = new LinkedHashSet<>();
    private final LinkedHashSet<Integer> observed73Events = new LinkedHashSet<>();
    private String expectedP2pName;
    private String credentialPassword;
    private Runnable timeout;
    private Runnable catalogDelayTask;
    private boolean catalogDelayScheduled;
    private Runnable visibilityTimeoutTask;
    private boolean passiveVisibilityObserved;
    private boolean physicalRunStarted;
    private long armElapsedStartMs=-1L;
    private long passiveVisibleAtMs=-1L;
    private long activeConfirmedAtMs=-1L;
    private long p2pReadyAtMs=-1L;
    private long catalogDoneAtMs=-1L;
    private long mediaDoneAtMs=-1L;
    private ImportLedger importLedger;
    private FileArchive fileArchive;
    private SingleItemImportCoordinator importCoordinator;
    private int ledgerCountAtStart=-1;
    private int ledgerCountAfter=-1;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        buildUi();
        try{
            File importRoot=new File(getFilesDir(),"g6a_imports");
            int stalePartsRemoved=FileArchive.removeStaleParts(importRoot);
            importLedger=new ImportLedger(importRoot);
            fileArchive=new FileArchive(importRoot,ZoneId.systemDefault(),importLedger);
            importCoordinator=new SingleItemImportCoordinator(fileArchive,importLedger);
            ledgerCountAtStart=importLedger.size();
            if(stalePartsRemoved>0) append("Recovered stale local partial files: "+stalePartsRemoved);
        }catch(Exception e){
            setStatus("Persistent import storage initialization failed.");
        }
        BluetoothManager bm=(BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter=bm==null?null:bm.getAdapter();
        p2pManager=(WifiP2pManager)getSystemService(Context.WIFI_P2P_SERVICE);
        if(p2pManager!=null) p2pChannel=p2pManager.initialize(this,Looper.getMainLooper(),()->appendThreadSafe("P2P channel: DISCONNECTED"));
        renderIdle();
    }

    private void buildUi(){
        int p=dp(20);
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(p,p,p,p);
        TextView t=new TextView(this); t.setText("K G1 G6A Persistent Import v0.6.0"); t.setTextSize(24); root.addView(t);
        TextView n=new TextView(this); n.setText("G6A: prove one newly captured JPG can be persistently imported once, validated, numbered locally, and recorded in an opaque restart-safe ledger."); n.setPadding(0,dp(8),0,dp(16)); root.addView(n);
        statusView=new TextView(this); statusView.setTextSize(16); root.addView(statusView);
        runButton=new Button(this); runButton.setText("Start G6A baseline"); runButton.setOnClickListener(v->begin()); root.addView(runButton);
        LinearLayout a=new LinearLayout(this); a.setOrientation(LinearLayout.HORIZONTAL);
        copyButton=new Button(this); copyButton.setText("Copy report"); copyButton.setEnabled(false); copyButton.setOnClickListener(v->copy()); a.addView(copyButton);
        shareButton=new Button(this); shareButton.setText("Share report"); shareButton.setEnabled(false); shareButton.setOnClickListener(v->share()); a.addView(shareButton); root.addView(a);
        ScrollView s=new ScrollView(this); reportView=new TextView(this); reportView.setTextSize(13); reportView.setTextIsSelectable(true); s.addView(reportView);
        root.addView(s,new LinearLayout.LayoutParams(-1,0,1)); setContentView(root);
    }

    private void renderIdle(){
        if(btAdapter==null||p2pManager==null){statusView.setText("Bluetooth LE or Wi-Fi Direct is unavailable.");runButton.setEnabled(false);return;}
        statusView.setText(importCoordinator==null?"Persistent storage unavailable.":"Ready. Force-stop Cyan Glasses, keep AIMB-G1 paired, then start the G6A baseline.");
        if(importCoordinator==null)runButton.setEnabled(false);
    }

    private void begin(){
        if(!hasPermissions()){ requestRequiredPermissions(); return; }
        try { if(!btAdapter.isEnabled()){setStatus("Bluetooth is off.");return;} } catch(SecurityException e){setStatus("Bluetooth permission required.");return;}
        if(stage==Stage.WAIT_FOR_CAPTURE){
            startTransportCycle(false);
        } else if(stage==Stage.BASELINE){
            startTransportCycle(true);
        }
    }

    private boolean hasPermissions(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.S && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!=PackageManager.PERMISSION_GRANTED) return false;
        if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.NEARBY_WIFI_DEVICES)!=PackageManager.PERMISSION_GRANTED) return false;
        if(Build.VERSION.SDK_INT<33 && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED) return false;
        return true;
    }

    private void requestRequiredPermissions(){
        ArrayList<String> ps=new ArrayList<>();
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.S) ps.add(Manifest.permission.BLUETOOTH_CONNECT);
        if(Build.VERSION.SDK_INT>=33) ps.add(Manifest.permission.NEARBY_WIFI_DEVICES); else { ps.add(Manifest.permission.ACCESS_COARSE_LOCATION); ps.add(Manifest.permission.ACCESS_FINE_LOCATION); }
        requestPermissions(ps.toArray(new String[0]),REQ_PERMISSION);
    }

    @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){
        super.onRequestPermissionsResult(r,p,g);
        if(r==REQ_PERMISSION){if(hasPermissions()) begin(); else setStatus("Bluetooth and Nearby Wi-Fi permission are required.");}
    }

    private void startTransportCycle(boolean fresh){
        cleanupRuntime(false);
        resetCycleState();

        if(fresh){
            stage=Stage.BASELINE;
            report.setLength(0);
            reportFinished=false;
            baselineCatalog.clear();
            baselineReady=false;
            mediaSuccess=false;
            httpRequestCount=0;
            catalogGetCount=0;
            mediaGetCount=0;
            countWriteCount=0;
            enterWriteCount=0;
            exitWriteCount=0;
            currentCatalogEntries=0;
            deltaCount=0;
            currentImageCount=currentVideoCount=currentRecordCount=currentConfigFileType=-1;
            currentOnlySupportApImport=false;
            baselineImageCount=baselineVideoCount=baselineRecordCount=-1;
            downloadedBytes=0;
            physicalRunStarted=false;
            armElapsedStartMs=passiveVisibleAtMs=activeConfirmedAtMs=p2pReadyAtMs=catalogDoneAtMs=mediaDoneAtMs=-1L;

            append("K G1 DISPOSABLE PHOTO PROBE REPORT");
            append("Generated: "+isoNow());
            append("App version: 0.6.0");
            append("Gate: G6A persistent single-item JPG import");
            append("Selection: G5.7 transport -> EXACT ONE NEW SAFE JPG -> PERSISTENT IMPORT -> OPAQUE LEDGER");
            append("BLE writes allowed: media-count 02 04 once + P2P enter once + transfer exit once PER PHASE");
            append("Inventory parity: EXACTLY ONE 0x41 / 02 04 query PER PHASE");
            append("Inventory handoff: wait for BOTH BLE write callback and valid 02 04 response before P2P enter");
            append("Catalog readiness delay: EXACT CYAN 1000 ms PER PHASE");
            append("Catalog HTTP allowed: EXACTLY ONE GET /files/media.config PER PHASE");
            append("Media HTTP allowed: EXACTLY ONE GET in Phase B only after exact single-JPG delta");
            append("HTTP redirects: DISABLED");
            append("HTTP retry/resume/Range: DISABLED");
            append("Catalog response cap: "+CATALOG_MAX_BYTES+" bytes");
            append("Media response cap: "+MEDIA_MAX_BYTES+" bytes");
            append("Peer selection: EXACT BLE-REPORTED P2P NAME ONLY");
            append("Credential logging/persistence: DISABLED");
            append("Catalog/filename/path logging: DISABLED");
            append("Catalog/media fingerprint/hash logging: DISABLED");
            append("Glasses file mutation/deletion: NOT IMPLEMENTED");
            append("Persistent import root: APP-PRIVATE FILES");
            append("Persistent ledger identity: OPAQUE SHA-256 ONLY");
            append("Ledger committed entries at run start: "+(importLedger==null?"<unavailable>":Integer.toString(importLedger.size())));
            append("");
            append("PHASE A — BASELINE");
        }else{
            if(!baselineReady||baselineCatalog.isEmpty()){
                fail("Baseline is unavailable. Restart G6A from Phase A.");
                return;
            }
            stage=Stage.DOWNLOAD;
            reportFinished=false;
            append("");
            append("PHASE B — VISIBILITY-GATED CAPTURE");
            append("Diagnostic timing: monotonic elapsed-time markers enabled");
            append("Capture timing: DO NOT take the photo until BLE subscription completes and the app reports ARMED.");
            append("Visibility gate: P2P enter is blocked until image inventory increases by exactly one.");
        }

        runButton.setEnabled(false);
        copyButton.setEnabled(false);
        shareButton.setEnabled(false);

        BluetoothDevice target=findUniqueBondedTarget();
        if(target==null)return;
        registerP2pReceiver();
        append("GATT CONNECTION");
        append("Target selection: exactly one bonded AIMB-G1-family device");
        append("Target name: "+safeName(safeGetName(target)));
        append("Bluetooth address: not logged");
        setStatus(stage==Stage.BASELINE?"Phase A: connecting BLE…":"Phase B: reconnecting BLE…");
        schedule(()->fail("BLE/P2P setup timeout."),CONNECT_TIMEOUT_MS);
        try{gatt=target.connectGatt(this,false,gattCb,BluetoothDevice.TRANSPORT_LE);}catch(Exception e){fail("BLE connect request failed.");}
    }

    private void resetCycleState(){
        countWriteAttempted=false;
        countWriteCallbackSucceeded=false;
        countResponseReceived=false;
        enterWriteAttempted=false;
        exitWriteAttempted=false;
        targetPeerMatched=false;
        expectedP2pName=null;
        credentialPassword=null;
        failurePending=false;
        ipEventObserved=false;
        failureReason=null;
        glassesClientIp=null;
        p2pGroupFormed=false;
        phoneIsGroupOwner=false;
        catalogGetAttempted=false;
        catalogSuccess=false;
        catalogDelayScheduled=false;
        passiveVisibilityObserved=false;
        if(catalogDelayTask!=null){handler.removeCallbacks(catalogDelayTask);catalogDelayTask=null;}
        if(visibilityTimeoutTask!=null){handler.removeCallbacks(visibilityTimeoutTask);visibilityTimeoutTask=null;}
        currentImageCount=currentVideoCount=currentRecordCount=currentConfigFileType=-1;
        currentOnlySupportApImport=false;
        observed73Events.clear();
        phase=Phase.IDLE;
    }

    private BluetoothDevice findUniqueBondedTarget(){
        append("BONDED TARGET CHECK"); int total=0,matches=0; BluetoothDevice unique=null;
        try{
            Set<BluetoothDevice> bonded=btAdapter.getBondedDevices(); total=bonded==null?0:bonded.size();
            if(bonded!=null) for(BluetoothDevice d:bonded){if(matchesTarget(safeGetName(d))){matches++;unique=d;}}
        }catch(SecurityException e){fail("Cannot inspect bonded devices.");return null;}
        append("Bonded devices total: "+total); append("AIMB-G1-family bonded matches: "+matches);
        if(matches!=1){fail("Exactly one AIMB-G1-family bonded device is required.");return null;}
        append("Bonded target name: "+safeName(safeGetName(unique))); append("Bonded target address: not logged"); append(""); return unique;
    }

    private final BluetoothGattCallback gattCb=new BluetoothGattCallback(){
        @Override public void onConnectionStateChange(BluetoothGatt x,int status,int state){
            if(status!=BluetoothGatt.GATT_SUCCESS){closeAndFail("GATT status "+status);return;}
            if(state==BluetoothProfile.STATE_CONNECTED){appendThreadSafe("GATT: connected");try{if(!x.discoverServices())closeAndFail("Service discovery did not start.");}catch(SecurityException e){closeAndFail("Bluetooth permission error.");}}
            else if(state==BluetoothProfile.STATE_DISCONNECTED && !reportFinished && phase!=Phase.CLEANUP && phase!=Phase.COMPLETE) closeAndFail("BLE disconnected early.");
        }
        @Override public void onServicesDiscovered(BluetoothGatt x,int status){
            if(status!=BluetoothGatt.GATT_SUCCESS){closeAndFail("Service discovery failed.");return;}
            BluetoothGattService s=x.getService(CYAN_SERVICE); if(s==null){closeAndFail("Cyan service not found.");return;}
            BluetoothGattCharacteristic n=s.getCharacteristic(CYAN_NOTIFY), w=s.getCharacteristic(CYAN_WRITE);
            if(n==null||w==null){closeAndFail("Cyan notify/write characteristic missing.");return;}
            writeChar=w; appendThreadSafe("CYAN SERVICE/NOTIFY/WRITE: PRESENT");
            try{
                if(!x.setCharacteristicNotification(n,true)){closeAndFail("Local notification registration failed.");return;}
                BluetoothGattDescriptor d=n.getDescriptor(CLIENT_CONFIG); if(d==null){closeAndFail("CCCD not found.");return;}
                d.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                if(!x.writeDescriptor(d)){closeAndFail("CCCD write did not start.");return;}
                appendThreadSafe("Notification subscription start: SUCCESS");
            }catch(SecurityException e){closeAndFail("Bluetooth permission error.");}
        }
        @Override public void onDescriptorWrite(BluetoothGatt x,BluetoothGattDescriptor d,int status){
            if(status!=BluetoothGatt.GATT_SUCCESS){closeAndFail("CCCD write failed: "+status);return;}
            appendThreadSafe("Notification subscription: SUCCESS");
            handler.post(()->{ if(stage==Stage.DOWNLOAD) armVisibilityWatch(x); else sendMediaCountQuery(x); });
        }
        @Override public void onCharacteristicWrite(BluetoothGatt x,BluetoothGattCharacteristic c,int status){
            handler.post(()->{
                if(reportFinished)return;
                append("BLE characteristic write status: "+(status==BluetoothGatt.GATT_SUCCESS?"SUCCESS":status));
                if(status!=BluetoothGatt.GATT_SUCCESS){
                    fail("BLE write callback failed.");
                    return;
                }
                if(phase==Phase.COUNT_SENT){
                    countWriteCallbackSucceeded=true;
                    maybeAdvanceAfterMediaCount();
                }
            });
        }
        @Override public void onCharacteristicChanged(BluetoothGatt x,BluetoothGattCharacteristic c,byte[] value){handleNotify(value);}
        @SuppressWarnings("deprecation") @Override public void onCharacteristicChanged(BluetoothGatt x,BluetoothGattCharacteristic c){handleNotify(c.getValue());}
    };

    private void armVisibilityWatch(BluetoothGatt x){
        cancelTimeout();
        if(reportFinished||stage!=Stage.DOWNLOAD)return;
        phase=Phase.VISIBILITY_WATCH;
        passiveVisibilityObserved=false;
        physicalRunStarted=true;
        armElapsedStartMs=SystemClock.elapsedRealtime();
        append("STATE: ARMED — TAKE ONE PHOTO NOW");
        append("");
        append("ARMED CAPTURE VISIBILITY WATCH");
        append("BLE remained connected: YES");
        append("Transfer mode: OFF");
        append("Observation window: 60 seconds maximum");
        append("Instruction: capture EXACTLY ONE disposable photo NOW");
        append("P2P/HTTP/media operations before +1 visibility: 0");
        setStatus("ARMED for up to 60 seconds. Capture exactly ONE photo NOW.");
        visibilityTimeoutTask=()->{
            visibilityTimeoutTask=null;
            if(reportFinished||phase!=Phase.VISIBILITY_WATCH)return;
            append("Passive +1 inventory event before timeout: NO");
            append("Bounded active visibility confirmation: 0x41 / 02 04");
            sendMediaCountQuery(x);
        };
        handler.postDelayed(visibilityTimeoutTask,VISIBILITY_TIMEOUT_MS);
    }

    private void cancelVisibilityTimeout(){
        if(visibilityTimeoutTask!=null){handler.removeCallbacks(visibilityTimeoutTask);visibilityTimeoutTask=null;}
    }

    private void sendMediaCountQuery(BluetoothGatt x){
        cancelTimeout();
        if(reportFinished||countWriteAttempted)return;
        append("");
        append("CYAN MEDIA INVENTORY REFRESH");
        append("Command: 0x41 / 02 04");
        append("Purpose: exact Cyan album-screen media-count/config parity");
        append("No retry policy: TRUE");
        countWriteAttempted=true;
        countWriteCount++;
        phase=Phase.COUNT_SENT;
        if(!writeFrame(x,frame41(MEDIA_COUNT))){fail("Media-count write did not start.");return;}
        append("Media-count write start: SUCCESS");
        setStatus(stage==Stage.BASELINE?"Phase A: waiting for media-count response…":"Phase B: waiting for post-capture media-count response…");
        schedule(()->fail("No valid media-count response received."),10000);
    }

    private void maybeAdvanceAfterMediaCount(){
        if(reportFinished||phase!=Phase.COUNT_SENT)return;
        if(!countWriteCallbackSucceeded||!countResponseReceived)return;
        BluetoothGatt x=gatt;
        if(x==null){
            fail("GATT unavailable after media-count handshake.");
            return;
        }
        cancelTimeout();
        append("Media-count write/response handshake: COMPLETE");
        if(stage==Stage.DOWNLOAD){
            int imageDelta=currentImageCount-baselineImageCount;
            int videoDelta=currentVideoCount-baselineVideoCount;
            int recordDelta=currentRecordCount-baselineRecordCount;
            if(imageDelta!=1||videoDelta!=0||recordDelta!=0){
                append("Visibility gate: FAILED");
                fail("Post-capture inventory must be exactly +1 image with video/recording unchanged; P2P remains blocked.");
                return;
            }
            activeConfirmedAtMs=SystemClock.elapsedRealtime();
            append("Visibility gate: PASS — exactly +1 image confirmed");
            append("ARMED -> active 02 04 confirmation: "+elapsedSinceArm(activeConfirmedAtMs)+" ms");
            if(passiveVisibleAtMs>=0)append("Passive +1 -> active confirmation: "+(activeConfirmedAtMs-passiveVisibleAtMs)+" ms");
            append("Passive +1 event observed before confirmation: "+passiveVisibilityObserved);
            setStatus("PHOTO VISIBLE — inventory +1 confirmed. Entering P2P…");
        }
        sendEnter(x);
    }

    private void sendEnter(BluetoothGatt x){
        cancelTimeout(); if(reportFinished||enterWriteAttempted)return;
        append("");append("ENTER P2P MODE");append("Command: 0x41 / 02 01 04 01");append("No retry policy: TRUE");
        enterWriteAttempted=true;enterWriteCount++;phase=Phase.ENTER_SENT;
        if(!writeFrame(x,frame41(ENTER_P2P))){fail("P2P enter write did not start.");return;}
        append("ENTER write start: SUCCESS"); setStatus("Waiting for transfer credentials…");
        schedule(()->fail("No valid transfer-credential response received."),10000);
    }

    private void handleNotify(byte[] v){
        if(v==null||reportFinished)return; final byte[] data=v.clone();
        handler.post(()->{
            if(reportFinished)return;
            if(!validFrame(data)){append("Notification: invalid frame ignored");return;}
            int cmd=data[1]&255;
            if(cmd==0x41 && phase==Phase.COUNT_SENT){
                InventorySummary inv=parseMediaCountResponse(data);
                if(inv!=null){
                    currentImageCount=inv.imageCount;
                    currentVideoCount=inv.videoCount;
                    currentRecordCount=inv.recordCount;
                    currentConfigFileType=inv.configFileType;
                    currentOnlySupportApImport=inv.onlySupportApImport;
                    append("Media-count response: VALID");
                    append("Image count: "+currentImageCount);
                    append("Video count: "+currentVideoCount);
                    append("Recording count: "+currentRecordCount);
                    append("Config file type: "+currentConfigFileType);
                    append("Only-support-AP-import: "+currentOnlySupportApImport);
                    if(currentConfigFileType!=1||currentOnlySupportApImport){
                        fail("Inventory response no longer selects the confirmed configFileType=1 P2P branch.");
                        return;
                    }
                    if(stage==Stage.BASELINE){
                        baselineImageCount=currentImageCount;
                        baselineVideoCount=currentVideoCount;
                        baselineRecordCount=currentRecordCount;
                    }else{
                        append("Inventory image delta from Phase A: "+(currentImageCount-baselineImageCount));
                        append("Inventory video delta from Phase A: "+(currentVideoCount-baselineVideoCount));
                        append("Inventory recording delta from Phase A: "+(currentRecordCount-baselineRecordCount));
                    }
                    countResponseReceived=true;
                    maybeAdvanceAfterMediaCount();
                }
            } else if(cmd==0x41 && phase==Phase.ENTER_SENT && parseTransferCredentials(data)){
                cancelTimeout();
                append("Transfer credential response: VALID");
                append("SSID length: "+expectedP2pName.getBytes(StandardCharsets.UTF_8).length);
                append("Password length: "+credentialPassword.getBytes(StandardCharsets.UTF_8).length);
                append("SSID/password values: not logged");
                startP2pDiscovery();
            } else if(cmd==0x41 && phase==Phase.EXIT_SENT){
                append("Exit response: valid 0x41 frame");
            } else if(cmd==0x73){
                handle73Event(data);
            }
        });
    }

    private InventorySummary parseMediaCountResponse(byte[] f){
        if(f==null||f.length<16)return null;
        int len=(f[2]&255)|((f[3]&255)<<8);
        if(len<10)return null;
        int p=6;
        if((f[p]&255)!=0x02||(f[p+1]&255)!=0x04)return null;
        InventorySummary out=new InventorySummary();
        out.imageCount=(f[p+2]&255)|((f[p+3]&255)<<8);
        out.videoCount=(f[p+4]&255)|((f[p+5]&255)<<8);
        out.recordCount=(f[p+6]&255)|((f[p+7]&255)<<8);
        out.configFileType=f[p+8]&255;
        out.onlySupportApImport=(f[p+9]&255)!=0;
        return out;
    }

    private static final class InventorySummary{
        int imageCount,videoCount,recordCount,configFileType;
        boolean onlySupportApImport;
    }

    private boolean parseTransferCredentials(byte[] f){
        if(f.length<14)return false; int len=(f[2]&255)|((f[3]&255)<<8); if(len<8)return false;
        int p=6; if((f[p]&255)!=2||(f[p+1]&255)!=1||(f[p+2]&255)!=4||(f[p+3]&255)!=1)return false;
        int sl=(f[p+4]&255)|((f[p+5]&255)<<8); int pl=(f[p+6]&255)|((f[p+7]&255)<<8);
        int off=p+8; if(sl<=0||pl<=0||off+sl+pl>f.length)return false;
        expectedP2pName=new String(f,off,sl,StandardCharsets.UTF_8);
        credentialPassword=new String(f,off+sl,pl,StandardCharsets.UTF_8);
        return !expectedP2pName.isEmpty()&&!credentialPassword.isEmpty();
    }

    private void handle73Event(byte[] data){
        if(data.length<7){append("Async 0x73 frame observed without event ID");return;}
        int eventId=data[6]&255;
        observed73Events.add(eventId);
        append(String.format(Locale.US,"Async 0x73 event ID: 0x%02X",eventId));
        if(phase==Phase.VISIBILITY_WATCH){
            if(eventId==0x01){
                InventorySummary inv=parse73Inventory(data);
                if(inv!=null){
                    append("Visibility-watch 0x73/0x01: images="+inv.imageCount+", videos="+inv.videoCount+", recordings="+inv.recordCount+", configFileType="+inv.configFileType);
                    int di=inv.imageCount-baselineImageCount, dv=inv.videoCount-baselineVideoCount, dr=inv.recordCount-baselineRecordCount;
                    if(di==1&&dv==0&&dr==0&&inv.configFileType==1){
                        passiveVisibilityObserved=true;
                        passiveVisibleAtMs=SystemClock.elapsedRealtime();
                        append("ARMED -> passive +1 visibility: "+elapsedSinceArm(passiveVisibleAtMs)+" ms");
                        cancelVisibilityTimeout();
                        append("Passive visibility gate candidate: EXACT +1 IMAGE");
                        BluetoothGatt x=gatt;
                        if(x==null){fail("GATT unavailable for visibility confirmation.");return;}
                        append("Confirming visibility with the single Phase-B 0x41 / 02 04 query before P2P.");
                        sendMediaCountQuery(x);
                    }else if(di>1||dv!=0||dr!=0){
                        cancelVisibilityTimeout();
                        fail("Ambiguous inventory change during visibility watch; P2P remains blocked.");
                    }
                }
            }
            return;
        }
        if(eventId!=0x08)return;
        ipEventObserved=true;
        String ip=parseIpv4(data,7);
        if(ip==null){
            append("0x73/0x08 IPv4: unresolved");
            return;
        }
        glassesClientIp=ip;
        append("0x73/0x08 glasses IPv4: "+ip);
        maybeStartCatalogGet();
    }

    private InventorySummary parse73Inventory(byte[] f){
        if(f==null||f.length<14||(f[1]&255)!=0x73||(f[6]&255)!=0x01)return null;
        InventorySummary out=new InventorySummary();
        out.imageCount=(f[7]&255)|((f[8]&255)<<8);
        out.videoCount=(f[9]&255)|((f[10]&255)<<8);
        out.recordCount=(f[11]&255)|((f[12]&255)<<8);
        out.configFileType=f[13]&255;
        out.onlySupportApImport=f.length>=15 && (f[14]&255)!=0;
        return out;
    }

    private static String parseIpv4(byte[] data,int offset){
        if(data==null||offset<0||offset+4>data.length)return null;
        int a=data[offset]&255,b=data[offset+1]&255,c=data[offset+2]&255,d=data[offset+3]&255;
        if(a==0&&b==0&&c==0&&d==0)return null;
        return a+"."+b+"."+c+"."+d;
    }

    private String eventIdSummary(){
        if(observed73Events.isEmpty())return "none";
        StringBuilder b=new StringBuilder();
        for(Integer id:observed73Events){
            if(b.length()>0)b.append(", ");
            b.append(String.format(Locale.US,"0x%02X",id));
        }
        return b.toString();
    }

    private void appendIpSummary(){
        append("Observed 0x73 event IDs: "+eventIdSummary());
        append("Event 0x08 observed: "+ipEventObserved);
        append("Glasses client IP resolved from 0x73/0x08: "+(glassesClientIp==null?"NO":"YES"));
        if(glassesClientIp!=null)append("Glasses client IP: "+glassesClientIp);
    }

    @SuppressWarnings("deprecation")
    private void startP2pDiscovery(){
        if(expectedP2pName==null||p2pManager==null||p2pChannel==null){fail("P2P manager/target name unavailable.");return;}
        phase=Phase.DISCOVERING; append("");append("WI-FI DIRECT DISCOVERY");append("Peer-name match: exact BLE-reported name, in memory only"); append("Nearby peer names/addresses: not logged");
        try{
            p2pManager.discoverPeers(p2pChannel,new WifiP2pManager.ActionListener(){
                public void onSuccess(){append("discoverPeers start: SUCCESS");setStatus("Discovering exact P2P peer…");schedule(()->failWithExit("Exact glasses P2P peer was not discovered."),P2P_DISCOVERY_TIMEOUT_MS);}
                public void onFailure(int reason){append("discoverPeers start: FAILED reason="+reason);failWithExit("P2P discovery failed.");}
            });
        }catch(SecurityException e){failWithExit("Nearby Wi-Fi permission error.");}
    }

    private void registerP2pReceiver(){
        if(receiverRegistered)return;
        IntentFilter f=new IntentFilter(); f.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION); f.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION); f.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        p2pReceiver=new BroadcastReceiver(){@Override public void onReceive(Context c,Intent i){
            String a=i.getAction();
            if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(a)) requestPeers();
            else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(a)){
                if(Build.VERSION.SDK_INT<29){NetworkInfo ni=i.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO); if(ni!=null&&ni.isConnected()) requestConnectionInfo();}
                else requestConnectionInfo();
            }
        }};
        if(Build.VERSION.SDK_INT>=33) registerReceiver(p2pReceiver,f,Context.RECEIVER_NOT_EXPORTED); else registerReceiver(p2pReceiver,f);
        receiverRegistered=true;
    }

    @SuppressWarnings("deprecation")
    private void requestPeers(){
        if(reportFinished||phase!=Phase.DISCOVERING)return;
        try{
            p2pManager.requestPeers(p2pChannel,peers->{
                if(reportFinished||phase!=Phase.DISCOVERING||expectedP2pName==null)return;
                WifiP2pDevice match=null; int count=0;
                for(WifiP2pDevice d:peers.getDeviceList()){count++; if(d.deviceName!=null&&d.deviceName.equalsIgnoreCase(expectedP2pName)){if(match!=null){failWithExit("Multiple exact peer-name matches; refusing connection.");return;} match=d;}}
                append("Peers observed: "+count+" (names/addresses not logged)");
                if(match!=null){targetPeerMatched=true;cancelTimeout();append("Exact glasses P2P peer match: YES");connectPeer(match);}
            });
        }catch(SecurityException e){failWithExit("Permission error while requesting peers.");}
    }

    @SuppressWarnings("deprecation")
    private void connectPeer(WifiP2pDevice d){
        if(phase!=Phase.DISCOVERING)return; phase=Phase.CONNECTING;
        WifiP2pConfig cfg=new WifiP2pConfig(); cfg.deviceAddress=d.deviceAddress; cfg.groupOwnerIntent=0; cfg.wps.setup=WpsInfo.PBC;
        append("");append("WI-FI DIRECT ASSOCIATION");append("Target: exact BLE-reported peer");append("WPS: PBC");append("groupOwnerIntent: 0");append("Peer address: not logged");
        try{
            p2pManager.connect(p2pChannel,cfg,new WifiP2pManager.ActionListener(){
                public void onSuccess(){append("connect request: SUCCESS");setStatus("Waiting for P2P group formation…");schedule(()->failWithExit("P2P group did not form in time."),P2P_CONNECTION_TIMEOUT_MS);}
                public void onFailure(int reason){append("connect request: FAILED reason="+reason);failWithExit("P2P connect request failed.");}
            });
        }catch(SecurityException e){failWithExit("Permission error while connecting P2P.");}
    }

    @SuppressWarnings("deprecation")
    private void requestConnectionInfo(){
        if(reportFinished||phase!=Phase.CONNECTING)return;
        try{
            p2pManager.requestConnectionInfo(p2pChannel,info->{
                if(reportFinished||phase!=Phase.CONNECTING||info==null||!info.groupFormed)return;
                cancelTimeout(); phase=Phase.CONNECTED; p2pGroupFormed=true; phoneIsGroupOwner=info.isGroupOwner;
                p2pReadyAtMs=SystemClock.elapsedRealtime();
                append("P2P group formed: TRUE");
                if(activeConfirmedAtMs>=0)append("Active confirmation -> P2P group ready: "+(p2pReadyAtMs-activeConfirmedAtMs)+" ms");
                append("Phone is group owner: "+info.isGroupOwner);
                append("Group-owner address: "+(info.groupOwnerAddress==null?"unavailable":info.groupOwnerAddress.getHostAddress()));
                if(!info.isGroupOwner){failWithExit("G4B requires the physically confirmed phone-group-owner topology.");return;}
                setStatus("P2P formed. Waiting for passive glasses IP before one catalog GET…");
                schedule(()->failWithExit("No passive glasses IP available for catalog GET."),CATALOG_WAIT_TIMEOUT_MS);
                maybeStartCatalogGet();
            });
        }catch(SecurityException e){failWithExit("Permission error requesting P2P connection info.");}
    }

    private void maybeStartCatalogGet(){
        if(reportFinished||phase!=Phase.CONNECTED||!p2pGroupFormed||!phoneIsGroupOwner||catalogGetAttempted||catalogDelayScheduled||glassesClientIp==null)return;
        if(!isConfirmedP2pIp(glassesClientIp)){failWithExit("Resolved IPv4 is outside the confirmed 192.168.49.0/24 P2P subnet.");return;}
        cancelTimeout();
        catalogDelayScheduled=true;
        phase=Phase.CATALOG_DELAY;
        append("");
        append("CYAN CATALOG READINESS");
        append("Delay before catalog GET: "+CATALOG_READY_DELAY_MS+" ms");
        append("Source parity: PictureFragment.downloadMediaConfig() / ktxRunOnUiDelay(0x03e8)");
        setStatus("Waiting exact Cyan 1000 ms before catalog GET…");
        catalogDelayTask=()->{
            catalogDelayTask=null;
            catalogDelayScheduled=false;
            if(reportFinished||phase!=Phase.CATALOG_DELAY||glassesClientIp==null)return;
            startCatalogGetNow(glassesClientIp);
        };
        handler.postDelayed(catalogDelayTask,CATALOG_READY_DELAY_MS);
    }

    private void startCatalogGetNow(String ip){
        if(reportFinished||catalogGetAttempted||phase!=Phase.CATALOG_DELAY)return;
        catalogGetAttempted=true;
        catalogGetCount++;
        httpRequestCount++;
        phase=Phase.CATALOG_GET;
        append("");
        append(stage==Stage.BASELINE?"PHASE A CATALOG":"PHASE B CATALOG");
        append("Method: GET");
        append("Path: "+CATALOG_PATH);
        append("Target: passive 0x73/0x08 glasses IPv4 only");
        append("Redirects: DISABLED");
        append("Max response bytes: "+CATALOG_MAX_BYTES);
        append("Media-file GET requests so far: "+mediaGetCount);
        setStatus(stage==Stage.BASELINE?"Phase A: reading refreshed media.config once…":"Phase B: reading refreshed post-capture media.config once…");
        final String safeIp=ip;
        new Thread(()->fetchCatalogOnce(safeIp),"g5-1-catalog-get").start();
    }

    private void fetchCatalogOnce(String ip){
        HttpURLConnection c=null;
        try{
            URL u=new URL("http://"+ip+CATALOG_PATH);
            c=(HttpURLConnection)u.openConnection();
            c.setRequestMethod("GET");
            c.setInstanceFollowRedirects(false);
            c.setConnectTimeout(4000);
            c.setReadTimeout(4000);
            c.setUseCaches(false);
            c.setDoInput(true);
            c.setDoOutput(false);
            int status=c.getResponseCode();
            final long declared=c.getContentLengthLong();
            if(status!=HttpURLConnection.HTTP_OK){postCatalogFailure("Catalog HTTP status "+status);return;}
            if(declared>CATALOG_MAX_BYTES){postCatalogFailure("Catalog Content-Length exceeds safety cap.");return;}
            byte[] body=readBounded(c.getInputStream(),CATALOG_MAX_BYTES);
            CatalogSummary summary=classifyCatalog(body,c.getContentType(),c.getContentEncoding());
            handler.post(()->{
                if(reportFinished)return;
                append("HTTP status: 200");
                append("Catalog bytes received: "+summary.bodyBytes);
                append("Content-Type: "+summary.contentType);
                append("Content-Encoding: "+summary.contentEncoding);
                append("UTF-8 valid: "+summary.utf8Valid);
                append("BOM: "+summary.bom);
                append("Cyan parser branch: configFileType=1 / readLines()");
                append("Line-list entries: "+summary.totalEntries);
                append("Non-empty entries: "+summary.nonEmptyEntries);
                append("Relative safe entries: "+summary.relativeSafeEntries);
                append("Duplicate entries: "+summary.duplicateEntries);
                append("Unsafe entry count: "+summary.unsafeEntries);
                append("Extension summary: "+summary.extensionSummary);
                append("Filename/path values logged: NO");
                append("Catalog raw body logged: NO");
                if(!summary.catalogUsable){
                    failWithExit("Catalog failed strict G5 safety validation.");
                    return;
                }
                catalogSuccess=true;
                catalogDoneAtMs=SystemClock.elapsedRealtime();
                currentCatalogEntries=summary.safeEntries.size();
                if(stage==Stage.DOWNLOAD&&p2pReadyAtMs>=0)append("P2P ready -> catalog parsed: "+(catalogDoneAtMs-p2pReadyAtMs)+" ms");

                if(stage==Stage.BASELINE){
                    baselineCatalog.clear();
                    baselineCatalog.addAll(summary.safeEntries);
                    baselineReady=true;
                    append("Baseline entries retained in memory only: "+baselineCatalog.size());
                    append("Media-file GET requests: 0");
                    setStatus("Phase A baseline captured. Exiting transfer mode before test photo…");
                    sendExit();
                    return;
                }

                if(stage!=Stage.DOWNLOAD){
                    failWithExit("Unexpected G5 stage.");
                    return;
                }

                if(!summary.safeEntries.containsAll(baselineCatalog)){
                    append("Baseline entries still present: NO");
                    failWithExit("Post-capture catalog does not contain the full baseline.");
                    return;
                }
                append("Baseline entries still present: YES");
                LinkedHashSet<String> delta=new LinkedHashSet<>(summary.safeEntries);
                delta.removeAll(baselineCatalog);
                deltaCount=delta.size();
                append("New catalog entries: "+deltaCount);
                if(deltaCount!=1){
                    failWithExit("Expected exactly one new catalog entry.");
                    return;
                }
                String candidate=delta.iterator().next();
                if(!isStrictMediaRelativePath(candidate)){
                    failWithExit("The single new catalog entry failed strict path validation.");
                    return;
                }
                if(!".jpg".equals(extensionOnly(candidate))){
                    append("New entry type: NOT JPG");
                    failWithExit("The single new catalog entry is not a JPG.");
                    return;
                }
                append("New entry strict relative-path validation: PASS");
                append("New entry type: JPG");
                append("Remote filename/path value logged: NO");
                setStatus("Exactly one new safe JPG identified. Downloading it once…");
                startMediaGet(ip,candidate);
            });
        }catch(Exception e){
            postCatalogFailure("Catalog GET/parse failed: "+e.getClass().getSimpleName());
        }finally{
            if(c!=null)c.disconnect();
        }
    }

    private void postCatalogFailure(String reason){
        handler.post(()->{if(!reportFinished)failWithExit(reason);});
    }

    private static byte[] readBounded(InputStream in,int maxBytes) throws IOException{
        try(InputStream input=in; ByteArrayOutputStream out=new ByteArrayOutputStream()){
            byte[] buf=new byte[4096]; int total=0;
            while(true){
                int n=input.read(buf);
                if(n<0)break;
                total+=n;
                if(total>maxBytes)throw new IOException("catalog too large");
                out.write(buf,0,n);
            }
            return out.toByteArray();
        }
    }

    private static CatalogSummary classifyCatalog(byte[] body,String contentType,String contentEncoding) throws IOException{
        CatalogSummary out=new CatalogSummary();
        out.bodyBytes=body==null?0:body.length;
        out.contentType=safeHeader(contentType);
        out.contentEncoding=safeHeader(contentEncoding);
        out.bom=detectBom(body);
        out.utf8Valid=isStrictUtf8(body);
        if(!out.utf8Valid)return out;

        String raw=new String(body==null?new byte[0]:body,StandardCharsets.UTF_8);
        if(raw.startsWith("\uFEFF"))raw=raw.substring(1);

        TreeMap<String,Integer> extensions=new TreeMap<>();
        try(BufferedReader reader=new BufferedReader(new StringReader(raw))){
            String line;
            while((line=reader.readLine())!=null){
                out.totalEntries++;
                if(out.totalEntries>CATALOG_MAX_ITEMS){
                    out.overItemCap=true;
                    break;
                }
                String trimmed=line.trim();
                if(trimmed.isEmpty()){
                    out.blankEntries++;
                    out.unsafeEntries++;
                    continue;
                }
                out.nonEmptyEntries++;
                boolean strictSafe=isStrictMediaRelativePath(trimmed);
                if(strictSafe){
                    out.relativeSafeEntries++;
                    if(!out.safeEntries.add(trimmed))out.duplicateEntries++;
                }else{
                    out.unsafeEntries++;
                }

                String ext=extensionOnly(trimmed);
                extensions.put(ext,extensions.getOrDefault(ext,0)+1);
            }
        }
        out.extensionSummary=summarizeExtensions(extensions);
        out.catalogUsable=!out.overItemCap
                && out.totalEntries>0
                && out.nonEmptyEntries>0
                && out.blankEntries==0
                && out.unsafeEntries==0
                && out.duplicateEntries==0
                && out.relativeSafeEntries==out.nonEmptyEntries
                && out.safeEntries.size()==out.nonEmptyEntries;
        return out;
    }

    private static boolean hasScheme(String s){
        if(s==null||s.length()<2)return false;
        int colon=s.indexOf(':');
        if(colon<=0||colon>16)return false;
        char first=s.charAt(0);
        if(!Character.isLetter(first))return false;
        for(int i=1;i<colon;i++){
            char ch=s.charAt(i);
            if(!(Character.isLetterOrDigit(ch)||ch=='+'||ch=='-'||ch=='.'))return false;
        }
        return true;
    }

    private static boolean startsWithSlash(String s){
        return s!=null&&!s.isEmpty()&&(s.charAt(0)=='/'||s.charAt(0)=='\\');
    }

    private static boolean hasTraversalSegment(String s){
        if(s==null)return false;
        String x=s.replace('\\','/');
        String[] parts=x.split("/",-1);
        for(String p:parts)if("..".equals(p))return true;
        return false;
    }

    private static boolean hasControlCharacter(String s){
        if(s==null)return false;
        for(int i=0;i<s.length();i++){
            char ch=s.charAt(i);
            if(ch<0x20||ch==0x7F)return true;
        }
        return false;
    }

    private static boolean isStrictMediaRelativePath(String s){
        if(s==null||s.isEmpty()||s.length()>240)return false;
        if(hasScheme(s)||startsWithSlash(s)||hasTraversalSegment(s)||hasControlCharacter(s))return false;
        if(s.indexOf('?')>=0||s.indexOf('#')>=0||s.indexOf('%')>=0||s.indexOf(':')>=0||s.indexOf('\\')>=0)return false;
        if(s.startsWith(".")||s.endsWith(".")||s.contains("//"))return false;
        String[] parts=s.split("/",-1);
        for(String part:parts){
            if(part.isEmpty()||".".equals(part)||"..".equals(part))return false;
            for(int i=0;i<part.length();i++){
                char ch=part.charAt(i);
                if(!(Character.isLetterOrDigit(ch)||ch=='_'||ch=='-'||ch=='.'))return false;
            }
        }
        return true;
    }

    private static String detectBom(byte[] b){
        if(b==null||b.length<2)return "NONE";
        if(b.length>=3&&(b[0]&255)==0xEF&&(b[1]&255)==0xBB&&(b[2]&255)==0xBF)return "UTF-8";
        if((b[0]&255)==0xFF&&(b[1]&255)==0xFE)return "UTF-16LE";
        if((b[0]&255)==0xFE&&(b[1]&255)==0xFF)return "UTF-16BE";
        return "NONE";
    }

    private static boolean isStrictUtf8(byte[] b){
        if(b==null)return true;
        try{
            StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(b));
            return true;
        }catch(CharacterCodingException e){
            return false;
        }
    }

    private static String safeHeader(String s){
        if(s==null||s.isEmpty())return "<none>";
        StringBuilder b=new StringBuilder();
        for(int i=0;i<s.length()&&i<96;i++){
            char ch=s.charAt(i);
            b.append((ch>=32&&ch<=126)?ch:'?');
        }
        return b.toString();
    }

    private static String extensionOnly(String path){
        int slash=Math.max(path.lastIndexOf('/'),path.lastIndexOf('\\'));
        String name=slash>=0?path.substring(slash+1):path;
        int dot=name.lastIndexOf('.');
        if(dot<0||dot==name.length()-1)return "<none>";
        String ext=name.substring(dot).toLowerCase(Locale.US);
        return ext.matches("\\.[a-z0-9]{1,8}")?ext:"<other>";
    }

    private static String summarizeExtensions(SortedMap<String,Integer> m){
        if(m.isEmpty())return "none";
        StringBuilder b=new StringBuilder();
        for(Map.Entry<String,Integer> e:m.entrySet()){
            if(b.length()>0)b.append(", ");
            b.append(e.getKey()).append("=").append(e.getValue());
        }
        return b.toString();
    }

    private static final class CatalogSummary{
        int bodyBytes,totalEntries,nonEmptyEntries,blankEntries,relativeSafeEntries;
        int duplicateEntries,unsafeEntries;
        String contentType="<none>",contentEncoding="<none>",bom="NONE",extensionSummary="none";
        boolean utf8Valid,overItemCap,catalogUsable;
        final LinkedHashSet<String> safeEntries=new LinkedHashSet<>();
    }

    private void startMediaGet(String ip,String candidate){
        if(reportFinished||stage!=Stage.DOWNLOAD||phase!=Phase.CATALOG_GET)return;
        if(mediaGetCount!=0){
            failWithExit("Media GET already attempted.");
            return;
        }
        if(importCoordinator==null||importLedger==null){
            failWithExit("Persistent import storage unavailable.");
            return;
        }
        if(!isConfirmedP2pIp(ip)||!isStrictMediaRelativePath(candidate)||!".jpg".equals(extensionOnly(candidate))){
            failWithExit("Media candidate failed final pre-request guard.");
            return;
        }
        String opaqueId=OpaqueIdentity.sha256(candidate);
        if(importLedger.contains(opaqueId)){
            append("Persistent dedup precheck: ALREADY COMMITTED");
            append("Media-file GET requests: 0");
            failWithExit("The selected new catalog item is already present in the persistent ledger.");
            return;
        }
        append("Persistent dedup precheck: NEW OPAQUE ID");
        append("Remote filename/path persisted: NO");
        mediaGetCount++;
        httpRequestCount++;
        phase=Phase.MEDIA_GET;
        append("");
        append("ONE PERSISTENT JPG IMPORT");
        append("Method: GET");
        append("Target: passive glasses IPv4 + /files/ + exact new catalog entry");
        append("Redirects: DISABLED");
        append("Retry/resume/Range: DISABLED");
        append("Max media bytes: "+MEDIA_MAX_BYTES);
        append("Remote filename/path value logged: NO");
        final String safeIp=ip;
        final String safeCandidate=candidate;
        final long firstSeenEpochMs=System.currentTimeMillis();
        new Thread(()->importMediaOnce(safeIp,safeCandidate,firstSeenEpochMs),"g6a-one-persistent-import").start();
    }

    private void importMediaOnce(String ip,String candidate,long firstSeenEpochMs){
        final NetworkImportSummary net=new NetworkImportSummary();
        try{
            int before=importLedger.size();
            ImportRecord record=importCoordinator.importNewJpg(candidate,firstSeenEpochMs,out->{
                HttpURLConnection c=null;
                try{
                    URL u=new URL("http://"+ip+MEDIA_PREFIX+candidate);
                    c=(HttpURLConnection)u.openConnection();
                    c.setRequestMethod("GET");
                    c.setInstanceFollowRedirects(false);
                    c.setConnectTimeout(4000);
                    c.setReadTimeout(15000);
                    c.setUseCaches(false);
                    c.setDoInput(true);
                    c.setDoOutput(false);
                    net.status=c.getResponseCode();
                    net.declaredLength=c.getContentLengthLong();
                    net.contentType=safeHeader(c.getContentType());
                    if(net.status!=HttpURLConnection.HTTP_OK)throw new IOException("Media HTTP status "+net.status);
                    if(net.declaredLength>MEDIA_MAX_BYTES)throw new IOException("Media Content-Length exceeds safety cap.");
                    try(InputStream in=c.getInputStream()){
                        byte[] buf=new byte[8192];
                        long total=0;
                        while(true){
                            int n=in.read(buf);
                            if(n<0)break;
                            total+=n;
                            if(total>MEDIA_MAX_BYTES)throw new IOException("media too large");
                            out.write(buf,0,n);
                        }
                        net.downloadedBytes=total;
                    }
                    if(net.downloadedBytes<=0)throw new IOException("Downloaded media is empty.");
                    if(net.declaredLength>=0&&net.downloadedBytes!=net.declaredLength)throw new IOException("Downloaded byte count does not match Content-Length.");
                }finally{
                    if(c!=null)c.disconnect();
                }
            });
            ledgerCountAfter=importLedger.size();
            if(ledgerCountAfter!=before+1)throw new IOException("Persistent ledger did not gain exactly one committed item.");
            if(record.byteCount()!=net.downloadedBytes)throw new IOException("Committed byte count mismatch.");
            mediaDoneAtMs=SystemClock.elapsedRealtime();
            handler.post(()->{
                if(reportFinished)return;
                downloadedBytes=record.byteCount();
                mediaSuccess=true;
                append("Media HTTP status: "+net.status);
                append("Media Content-Type: "+net.contentType);
                append("Declared Content-Length: "+(net.declaredLength<0?"<none>":Long.toString(net.declaredLength)));
                append("Downloaded bytes: "+net.downloadedBytes);
                append("HTTP status validation: PASS (200)");
                append("Content-Length consistency: "+(net.declaredLength<0?"NOT PROVIDED":"PASS"));
                append("Media size cap: PASS");
                append("JPEG SOI/EOI validation: PASS");
                append("Persistent final file committed: YES");
                append("Persistent ledger commit: PASS");
                append("Opaque ledger identity only: YES");
                append("Remote filename/path persisted: NO");
                append("Ledger entry delta: +1");
                if(catalogDoneAtMs>=0)append("Catalog parsed -> persistent media commit: "+(mediaDoneAtMs-catalogDoneAtMs)+" ms");
                append("Media-file GET requests: "+mediaGetCount);
                setStatus("G6A persistent import committed. Exiting transfer mode…");
                sendExit();
            });
        }catch(Exception e){
            postMediaFailure("Persistent import failed: "+e.getClass().getSimpleName());
        }
    }

    private static final class NetworkImportSummary{
        int status=-1;
        long declaredLength=-1,downloadedBytes;
        String contentType="<none>";
    }

    private MediaSummary streamMediaBounded(InputStream input,File file,int maxBytes) throws IOException{
        MediaSummary m=new MediaSummary();
        int firstCount=0;
        int prev=-1,last=-1;
        try(InputStream in=input; FileOutputStream out=new FileOutputStream(file)){
            byte[] buf=new byte[8192];
            long total=0;
            while(true){
                int n=in.read(buf);
                if(n<0)break;
                if(total+n>maxBytes)throw new IOException("media too large");
                for(int i=0;i<n;i++){
                    int v=buf[i]&255;
                    if(firstCount<3)m.first[firstCount++]=v;
                    prev=last;
                    last=v;
                }
                out.write(buf,0,n);
                total+=n;
            }
            out.flush();
            out.getFD().sync();
            m.bytes=total;
        }
        m.jpegStart=firstCount>=3&&m.first[0]==0xFF&&m.first[1]==0xD8&&m.first[2]==0xFF;
        m.jpegEnd=prev==0xFF&&last==0xD9;
        return m;
    }

    private void postMediaFailure(String reason){
        handler.post(()->{if(!reportFinished)failWithExit(reason);});
    }

    private static final class MediaSummary{
        long bytes,declaredLength=-1;
        String contentType="<none>";
        boolean jpegStart,jpegEnd;
        final int[] first=new int[3];
    }

    private static boolean isConfirmedP2pIp(String ip){
        if(ip==null)return false;
        String[] p=ip.split("\\.");
        if(p.length!=4)return false;
        try{
            int a=Integer.parseInt(p[0]),b=Integer.parseInt(p[1]),c=Integer.parseInt(p[2]),d=Integer.parseInt(p[3]);
            return a==192&&b==168&&c==49&&d>=2&&d<=254;
        }catch(NumberFormatException e){return false;}
    }

    private void sendExit(){
        cancelTimeout(); if(reportFinished||exitWriteAttempted)return;
        append("");append("EXIT TRANSFER MODE");append("Command: 0x41 / 02 01 09");append("No retry policy: TRUE");
        exitWriteAttempted=true;exitWriteCount++;phase=Phase.EXIT_SENT;
        if(gatt==null||!writeFrame(gatt,frame41(EXIT_TRANSFER))){fail("Exit-transfer write did not start.");return;}
        append("EXIT write start: SUCCESS"); schedule(this::cleanupSuccess,3000);
    }

    private void failWithExit(String msg){
        cancelTimeout();
        append("G6A ERROR: "+msg);
        failurePending=true;
        failureReason=msg;
        if(enterWriteAttempted&&!exitWriteAttempted&&gatt!=null){
            sendExit();
        } else {
            fail(msg);
        }
    }

    private void cleanupSuccess(){
        phase=Phase.CLEANUP; cancelTimeout(); append("");append("CLEANUP");
        try{
            if(p2pManager!=null&&p2pChannel!=null){
                p2pManager.removeGroup(p2pChannel,new WifiP2pManager.ActionListener(){
                    public void onSuccess(){append("removeGroup: SUCCESS");finishAfterCleanup();}
                    public void onFailure(int r){append("removeGroup: nonfatal reason="+r);finishAfterCleanup();}
                });
            } else finishAfterCleanup();
        } catch(SecurityException e){
            append("removeGroup: permission error (nonfatal)");
            finishAfterCleanup();
        }
    }

    private void finishAfterCleanup(){
        if(failurePending){
            String reason=failureReason==null?"G6A did not complete.":failureReason;
            cleanupRuntime(false);
            append("");
            append("SUMMARY");
            append("Stage at failure: "+stage);
            append("Media-count queries: "+countWriteCount);
            append("P2P enter writes: "+enterWriteCount);
            append("Transfer-exit writes: "+exitWriteCount);
            append("Catalog GET requests: "+catalogGetCount);
            append("Media-file GET requests: "+mediaGetCount);
            append("Total HTTP GET requests: "+httpRequestCount);
            append("Credentials logged/persisted: NO");
            append("Remote filename/path values logged: NO");
            append("G6A RESULT: FAILED — "+reason);
            append("END REPORT");
            baselineCatalog.clear();
            baselineReady=false;
            reportFinished=true;
            phase=Phase.COMPLETE;
            stage=Stage.BASELINE;
            setStatus(reason);
            runButton.setText(physicalRunStarted?"Run ended — restart app":"Start G6A baseline");
            enableActions();
            if(physicalRunStarted)runButton.setEnabled(false);
            return;
        }

        if(stage==Stage.BASELINE&&baselineReady){
            finishBaselinePhase();
            return;
        }

        if(stage==Stage.DOWNLOAD&&mediaSuccess){
            finishSuccess();
            return;
        }

        fail("Unexpected cleanup state.");
    }

    private void finishBaselinePhase(){
        cleanupRuntime(false);
        append("");
        append("PHASE A COMPLETE");
        append("Baseline inventory counts: images="+baselineImageCount+", videos="+baselineVideoCount+", recordings="+baselineRecordCount);
        append("Baseline entries retained in memory only: "+baselineCatalog.size());
        append("Phase A media-file GET requests: 0");
        append("Transfer mode exited before user capture: YES");
        append("Baseline filename/path values logged: NO");
        stage=Stage.WAIT_FOR_CAPTURE;
        phase=Phase.COMPLETE;
        setStatus("Phase A complete. Do NOT capture yet. Tap Arm; wait for ARMED; then take exactly ONE photo.");
        runButton.setText("Arm visibility watch — then capture ONE photo");
        runButton.setEnabled(true);
        copyButton.setEnabled(false);
        shareButton.setEnabled(false);
    }

    private void finishSuccess(){
        cleanupRuntime(false);
        append("");
        append("SUMMARY");
        append("Baseline inventory counts: images="+baselineImageCount+", videos="+baselineVideoCount+", recordings="+baselineRecordCount);
        append("Post-capture inventory counts: images="+currentImageCount+", videos="+currentVideoCount+", recordings="+currentRecordCount);
        append("Visibility gate confirmed exactly +1 image: YES");
        append("Passive +1 event observed: "+passiveVisibilityObserved);
        append("Timing ARMED -> passive +1 ms: "+(passiveVisibleAtMs<0?"<not observed>":Long.toString(elapsedSinceArm(passiveVisibleAtMs))));
        append("Timing ARMED -> active confirmation ms: "+(activeConfirmedAtMs<0?"<not available>":Long.toString(elapsedSinceArm(activeConfirmedAtMs))));
        append("Timing active confirmation -> P2P ready ms: "+(activeConfirmedAtMs<0||p2pReadyAtMs<0?"<not available>":Long.toString(p2pReadyAtMs-activeConfirmedAtMs)));
        append("Timing P2P ready -> catalog parsed ms: "+(p2pReadyAtMs<0||catalogDoneAtMs<0?"<not available>":Long.toString(catalogDoneAtMs-p2pReadyAtMs)));
        append("Timing catalog parsed -> media validated ms: "+(catalogDoneAtMs<0||mediaDoneAtMs<0?"<not available>":Long.toString(mediaDoneAtMs-catalogDoneAtMs)));
        append("Baseline catalog entries: "+baselineCatalog.size());
        append("Post-capture catalog entries: "+currentCatalogEntries);
        append("New catalog entries: "+deltaCount);
        append("Media-count queries: "+countWriteCount);
        append("P2P enter writes: "+enterWriteCount);
        append("Transfer-exit writes: "+exitWriteCount);
        append("Catalog GET requests: "+catalogGetCount);
        append("Media-file GET requests: "+mediaGetCount);
        append("Total HTTP GET requests: "+httpRequestCount);
        append("Downloaded bytes: "+downloadedBytes);
        append("Credentials logged/persisted: NO");
        append("Catalog/raw filename/path values logged: NO");
        append("Remote media filename/path logged: NO");
        append("Glasses file mutation/deletion: 0");
        append("Ledger committed entries at run end: "+(importLedger==null?"<unavailable>":Integer.toString(importLedger.size())));
        append("G6A RESULT: ONE NEW JPG PERSISTENTLY IMPORTED — REVIEW PHYSICAL REPORT");
        append("END REPORT");
        baselineCatalog.clear();
        baselineReady=false;
        reportFinished=true;
        phase=Phase.COMPLETE;
        stage=Stage.BASELINE;
        setStatus("G6A import committed. Restart app before dedup/recovery verification.");
        runButton.setText("Run complete — restart app");
        enableActions();
        runButton.setEnabled(false);
    }

    private boolean writeFrame(BluetoothGatt x,byte[] frame){
        if(writeChar==null||!validFrame(frame))return false;
        writeChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT); writeChar.setValue(frame);
        try{return x.writeCharacteristic(writeChar);}catch(Exception e){return false;}
    }

    private static byte[] frame41(byte[] payload){
        int crc=crc16(payload); byte[] f=new byte[payload.length+6]; f[0]=(byte)0xBC;f[1]=0x41;f[2]=(byte)payload.length;f[3]=0;f[4]=(byte)(crc&255);f[5]=(byte)((crc>>>8)&255);System.arraycopy(payload,0,f,6,payload.length);return f;
    }
    private static int crc16(byte[] d){int c=0xffff;for(byte b:d){c^=b&255;for(int i=0;i<8;i++)c=(c&1)!=0?(c>>>1)^0xA001:c>>>1;}return c&0xffff;}
    private static boolean validFrame(byte[] f){if(f==null||f.length<6||(f[0]&255)!=0xBC)return false;int l=(f[2]&255)|((f[3]&255)<<8);if(f.length!=l+6)return false;byte[] p=Arrays.copyOfRange(f,6,f.length);int e=(f[4]&255)|((f[5]&255)<<8);return crc16(p)==e;}

    private long elapsedSinceArm(long t){return armElapsedStartMs>=0&&t>=armElapsedStartMs?t-armElapsedStartMs:-1L;}

    private void schedule(Runnable r,long ms){cancelTimeout();timeout=r;handler.postDelayed(r,ms);}
    private void cancelTimeout(){if(timeout!=null){handler.removeCallbacks(timeout);timeout=null;}}

    private void fail(String m){if(reportFinished)return;cancelTimeout();reportFinished=true;phase=Phase.COMPLETE;append("G6A RESULT: FAILED — "+m);cleanupRuntime(false);baselineCatalog.clear();baselineReady=false;append("END REPORT");stage=Stage.BASELINE;setStatus(m);runButton.setText("Start G6A baseline");enableActions();}
    private void closeAndFail(String m){handler.post(()->fail(m));}

    private void cleanupRuntime(boolean destroy){
        cancelTimeout();
        cancelVisibilityTimeout();
        if(catalogDelayTask!=null){handler.removeCallbacks(catalogDelayTask);catalogDelayTask=null;}
        catalogDelayScheduled=false;
        expectedP2pName=null; credentialPassword=null;
        if(receiverRegistered&&p2pReceiver!=null){try{unregisterReceiver(p2pReceiver);}catch(Exception ignored){} receiverRegistered=false;p2pReceiver=null;}
        BluetoothGatt x=gatt;gatt=null;writeChar=null;if(x!=null){try{x.disconnect();}catch(Exception ignored){}try{x.close();}catch(Exception ignored){}}
        if(destroy&&p2pChannel!=null&&Build.VERSION.SDK_INT>=27){try{p2pChannel.close();}catch(Exception ignored){}}
    }

    private void copy(){ClipboardManager c=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);if(c!=null){c.setPrimaryClip(ClipData.newPlainText("G6A G6A Persistent Import Report",report.toString()));Toast.makeText(this,"Report copied",Toast.LENGTH_SHORT).show();}}
    private void share(){Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_SUBJECT,"AIMB-G1 G6A G6A Persistent Import Report");i.putExtra(Intent.EXTRA_TEXT,report.toString());startActivity(Intent.createChooser(i,"Share G6A report"));}
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
        if(!isChangingConfigurations()&&!reportFinished&&(stage==Stage.WAIT_FOR_CAPTURE||stage==Stage.DOWNLOAD)){
            fail("App left foreground during controlled test; baseline invalidated.");
        }
    }

    @Override protected void onDestroy(){baselineCatalog.clear();cleanupRuntime(true);super.onDestroy();}
}
