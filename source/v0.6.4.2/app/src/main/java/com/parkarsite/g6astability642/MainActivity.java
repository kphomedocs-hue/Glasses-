package com.parkarsite.g6astability642;

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
    private static final int REQ_PERMISSION=6401;
    private static final String TARGET_NAME="AIMB-G1";
    private static final long CONNECT_TIMEOUT_MS=30000L;
    private static final long P2P_DISCOVERY_TIMEOUT_MS=20000L;
    private static final long P2P_CONNECTION_TIMEOUT_MS=15000L;
    private static final long CATALOG_WAIT_TIMEOUT_MS=8000L;
    private static final long CATALOG_READY_DELAY_MS=1000L;
    private static final long STABILITY_QUIET_MS=30000L;
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

    private enum Stage { SNAPSHOT_A, SNAPSHOT_B }
    private enum Phase { IDLE, COUNT_SENT, ENTER_SENT, DISCOVERING, CONNECTING, CONNECTED, CATALOG_DELAY, CATALOG_GET, EXIT_SENT, CLEANUP, COMPLETE }

    private final Handler handler=new Handler(Looper.getMainLooper());
    private final StringBuilder report=new StringBuilder();
    private final LinkedHashSet<Integer> observed73Events=new LinkedHashSet<>();
    private final LinkedHashSet<String> snapshotAAllHashes=new LinkedHashSet<>();
    private final LinkedHashSet<String> snapshotAJpgHashes=new LinkedHashSet<>();

    private BluetoothAdapter btAdapter;
    private BluetoothGatt gatt;
    private BluetoothGattCharacteristic writeChar;
    private WifiP2pManager p2pManager;
    private WifiP2pManager.Channel p2pChannel;
    private BroadcastReceiver p2pReceiver;
    private boolean receiverRegistered;

    private TextView statusView,reportView;
    private Button runButton,copyButton,shareButton;

    private Stage stage=Stage.SNAPSHOT_A;
    private Phase phase=Phase.IDLE;
    private boolean reportFinished=true;
    private boolean runActive;
    private boolean countWriteAttempted,countWriteCallbackSucceeded,countResponseReceived;
    private boolean enterWriteAttempted,enterWriteStarted,enterWriteCallbackSucceeded,enterCredentialResponseReceived,exitWriteAttempted,exitWriteCallbackSucceeded,postExitInventoryConfirmed;
    private boolean p2pGroupFormed,phoneIsGroupOwner,catalogGetAttempted,catalogDelayScheduled;
    private boolean failurePending,comparisonCompleted,stabilityExact,snapshotAFullMediaParity,cleanupCompletionHandled,destroyRequested;
    private String failureReason,glassesClientIp,expectedP2pName,credentialPassword,stabilityClassification;
    private Runnable timeout,catalogDelayTask,quietTask,cleanupFallbackTask;
    private long quietStartElapsedMs=-1L;

    private int currentImageCount=-1,currentVideoCount=-1,currentRecordCount=-1,currentConfigFileType=-1;
    private boolean currentOnlySupportApImport;
    private int snapshotAImageCount=-1,snapshotAVideoCount=-1,snapshotARecordCount=-1;
    private int snapshotATotalEntries=-1,snapshotAJpgEntries=-1,snapshotAVideoEntries=-1,snapshotAOpusEntries=-1;
    private int currentCatalogEntries,currentJpgEntries,currentVideoEntries,currentOpusEntries;
    private int countWriteCount,enterWriteCount,exitWriteCount,catalogGetCount,httpRequestCount,mediaGetCount;

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
        TextView title=new TextView(this); title.setText("K G1 No-Capture Catalog Stability v0.6.4.2"); title.setTextSize(23); root.addView(title);
        TextView note=new TextView(this); note.setText("G6A diagnostic: two read-only inventory/catalog snapshots with NO PHOTO and ZERO media downloads."); note.setPadding(0,dp(8),0,dp(16)); root.addView(note);
        statusView=new TextView(this); statusView.setTextSize(16); root.addView(statusView);
        runButton=new Button(this); runButton.setText("Start no-capture stability test"); runButton.setOnClickListener(v->begin()); root.addView(runButton);
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
            statusView.setText("Ready. Force-stop Cyan Glasses. Do NOT take any photo during this test.");
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
        stage=Stage.SNAPSHOT_A;

        append("K G1 G6A NO-CAPTURE CATALOG STABILITY REPORT");
        append("Generated: "+isoNow());
        append("App version: 0.6.4.2");
        append("Build commit: "+BuildConfig.BUILD_COMMIT);
        append("Build run: "+BuildConfig.BUILD_RUN);
        append("Build attempt: "+BuildConfig.BUILD_ATTEMPT);
        append("Mode: READ-ONLY TWO-SNAPSHOT NO-CAPTURE DIAGNOSTIC");
        append("Purpose: test whether AIMB-G1 inventory/catalog changes without a photo or media transfer");
        append("Instruction: DO NOT TAKE ANY PHOTO during the entire run");
        append("Snapshot count: EXACTLY TWO");
        append("Quiet interval between snapshots: "+STABILITY_QUIET_MS+" ms");
        append("BLE writes allowed per snapshot: media-count 02 04 once + P2P enter once + transfer exit once");
        append("Catalog HTTP allowed per snapshot: EXACTLY ONE GET /files/media.config");
        append("Media-file GET allowed: 0");
        append("HTTP redirects: DISABLED");
        append("HTTP retry/resume/Range: DISABLED");
        append("Catalog response cap: "+CATALOG_MAX_BYTES+" bytes");
        append("Peer selection: EXACT BLE-REPORTED P2P NAME ONLY");
        append("Credential logging/persistence: DISABLED");
        append("Remote filename/path logging/persistence: DISABLED");
        append("Catalog identity comparison: OPAQUE SHA-256 IN MEMORY ONLY");
        append("Per-file identity/hash tokens in report: DISABLED");
        append("Glasses file mutation/deletion: NOT IMPLEMENTED");
        append("");
        append("SNAPSHOT A");

        runButton.setEnabled(false); copyButton.setEnabled(false); shareButton.setEnabled(false);
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
        snapshotAImageCount=snapshotAVideoCount=snapshotARecordCount=-1;
        snapshotATotalEntries=snapshotAJpgEntries=snapshotAVideoEntries=snapshotAOpusEntries=-1;
        snapshotAAllHashes.clear();snapshotAJpgHashes.clear();
        comparisonCompleted=false;stabilityExact=false;snapshotAFullMediaParity=false;stabilityClassification="<not classified>";quietStartElapsedMs=-1L;destroyRequested=false;
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
        setStatus((stage==Stage.SNAPSHOT_A?"Snapshot A":"Snapshot B")+": connecting BLE. DO NOT TAKE A PHOTO.");
        schedule(()->abortRun("BLE/P2P setup timeout."),CONNECT_TIMEOUT_MS);
        try{gatt=target.connectGatt(this,false,gattCb,BluetoothDevice.TRANSPORT_LE);}
        catch(Exception e){abortRun("BLE connect request failed.");}
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
            handler.post(()->sendMediaCountQuery(x));
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
        append("Purpose: exact Cyan album-screen media-count/config parity");
        append("No retry policy: TRUE");
        countWriteAttempted=true;countWriteCount++;phase=Phase.COUNT_SENT;
        if(!writeFrame(x,frame41(MEDIA_COUNT))){abortRun("Media-count write did not start.");return;}
        append("Media-count write start: SUCCESS");
        setStatus((stage==Stage.SNAPSHOT_A?"Snapshot A":"Snapshot B")+": waiting for inventory response.");
        schedule(()->abortRun("No valid media-count response received."),10000);
    }

    private void maybeAdvanceAfterMediaCount(){
        if(reportFinished||phase!=Phase.COUNT_SENT||!countWriteCallbackSucceeded||!countResponseReceived)return;
        BluetoothGatt x=gatt;if(x==null){abortRun("GATT unavailable after media-count handshake.");return;}
        cancelTimeout();append("Media-count write/response handshake: COMPLETE");sendEnter(x);
    }

    private void sendEnter(BluetoothGatt x){
        cancelTimeout();if(reportFinished||enterWriteAttempted)return;
        append("");append("ENTER P2P MODE");append("Command: 0x41 / 02 01 04 01");append("No retry policy: TRUE");
        enterWriteAttempted=true;enterWriteStarted=false;enterWriteCallbackSucceeded=false;enterCredentialResponseReceived=false;enterWriteCount++;phase=Phase.ENTER_SENT;
        if(!writeFrame(x,frame41(ENTER_P2P))){abortRun("P2P enter write did not start.");return;}
        enterWriteStarted=true;
        append("ENTER write start: SUCCESS");setStatus("Waiting for enter write callback + transfer credentials. NO PHOTO.");
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
            if(!matches){
                if(!failurePending){failurePending=true;failureReason="Post-exit inventory did not match current snapshot; exit separation not proven.";}
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
                public void onSuccess(){append("discoverPeers start: SUCCESS");setStatus("Discovering exact P2P peer. NO PHOTO.");schedule(()->abortRun("Exact glasses P2P peer was not discovered."),P2P_DISCOVERY_TIMEOUT_MS);}
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
                public void onSuccess(){append("connect request: SUCCESS");setStatus("Waiting for P2P group. NO PHOTO.");schedule(()->abortRun("P2P group did not form in time."),P2P_CONNECTION_TIMEOUT_MS);}
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
                setStatus("P2P formed. Waiting for passive glasses IP. NO PHOTO.");
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
        setStatus("Waiting exact Cyan 1000 ms before catalog GET. NO PHOTO.");
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
        append("");append(stage==Stage.SNAPSHOT_A?"SNAPSHOT A CATALOG":"SNAPSHOT B CATALOG");
        append("Method: GET");append("Path: "+CATALOG_PATH);append("Target: passive 0x73/0x08 glasses IPv4 only");
        append("Redirects: DISABLED");append("Max response bytes: "+CATALOG_MAX_BYTES);append("Media-file GET requests so far: "+mediaGetCount);
        setStatus((stage==Stage.SNAPSHOT_A?"Snapshot A":"Snapshot B")+": reading media.config once. NO PHOTO.");
        new Thread(()->fetchCatalogOnce(ip),"g6a64-catalog-get").start();
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
        currentJpgEntries=summary.jpgEntries;
        currentVideoEntries=summary.mp4Entries;
        currentOpusEntries=summary.opusEntries;
        append("Opaque all-entry identities in memory: "+allHashes.size());
        append("Opaque JPG identities in memory: "+jpgHashes.size());
        append("Per-file identity/hash tokens reported: NO");
        append("Remote filename/path values persisted/logged: NO");
        append("Media-file GET requests: "+mediaGetCount);

        if(stage==Stage.SNAPSHOT_A){
            snapshotAImageCount=currentImageCount;snapshotAVideoCount=currentVideoCount;snapshotARecordCount=currentRecordCount;
            snapshotATotalEntries=currentCatalogEntries;snapshotAJpgEntries=currentJpgEntries;
            snapshotAVideoEntries=currentVideoEntries;snapshotAOpusEntries=currentOpusEntries;
            snapshotAFullMediaParity=(snapshotAImageCount==snapshotAJpgEntries)
                    &&(snapshotAVideoCount==snapshotAVideoEntries)
                    &&(snapshotARecordCount==snapshotAOpusEntries);
            snapshotAAllHashes.clear();snapshotAAllHashes.addAll(allHashes);
            snapshotAJpgHashes.clear();snapshotAJpgHashes.addAll(jpgHashes);
            append("Snapshot A BLE/catalog media-count parity: "+snapshotAFullMediaParity);
            append("Snapshot A opaque identity sets retained in memory only: YES");
            setStatus("Snapshot A captured. Exiting transfer mode. DO NOT TAKE A PHOTO.");
            sendExit();
            return;
        }

        int jpgOverlap=0;for(String h:jpgHashes)if(snapshotAJpgHashes.contains(h))jpgOverlap++;
        int jpgMissing=snapshotAJpgHashes.size()-jpgOverlap;
        int jpgUnexpected=0;for(String h:jpgHashes)if(!snapshotAJpgHashes.contains(h))jpgUnexpected++;
        int allOverlap=0;for(String h:allHashes)if(snapshotAAllHashes.contains(h))allOverlap++;
        int allMissing=snapshotAAllHashes.size()-allOverlap;
        int allUnexpected=0;for(String h:allHashes)if(!snapshotAAllHashes.contains(h))allUnexpected++;

        boolean inventoryEqual=currentImageCount==snapshotAImageCount&&currentVideoCount==snapshotAVideoCount&&currentRecordCount==snapshotARecordCount;
        boolean totalCountEqual=currentCatalogEntries==snapshotATotalEntries;
        boolean jpgCountEqual=currentJpgEntries==snapshotAJpgEntries;
        boolean videoCountEqual=currentVideoEntries==snapshotAVideoEntries;
        boolean opusCountEqual=currentOpusEntries==snapshotAOpusEntries;
        boolean allSetEqual=allHashes.equals(snapshotAAllHashes);
        boolean jpgSetEqual=jpgHashes.equals(snapshotAJpgHashes);
        boolean snapshotBFullMediaParity=(currentImageCount==currentJpgEntries)
                &&(currentVideoCount==currentVideoEntries)
                &&(currentRecordCount==currentOpusEntries);
        stabilityExact=inventoryEqual&&totalCountEqual&&jpgCountEqual&&videoCountEqual&&opusCountEqual
                &&allSetEqual&&jpgSetEqual&&snapshotAFullMediaParity&&snapshotBFullMediaParity;

        append("");append("NO-CAPTURE COMPARISON");
        append("Snapshot A inventory: images="+snapshotAImageCount+", videos="+snapshotAVideoCount+", recordings="+snapshotARecordCount);
        append("Snapshot B inventory: images="+currentImageCount+", videos="+currentVideoCount+", recordings="+currentRecordCount);
        append("Inventory counts exactly equal: "+inventoryEqual);
        append("Snapshot A total safe catalog entries: "+snapshotATotalEntries);
        append("Snapshot B total safe catalog entries: "+currentCatalogEntries);
        append("Total catalog entry count equal: "+totalCountEqual);
        append("Snapshot A JPG identities: "+snapshotAJpgEntries);
        append("Snapshot B JPG identities: "+currentJpgEntries);
        append("JPG identity count equal: "+jpgCountEqual);
        append("MP4 entry count equal: "+videoCountEqual);
        append("OPUS entry count equal: "+opusCountEqual);
        append("JPG identities retained from Snapshot A: "+jpgOverlap);
        append("JPG identities missing from Snapshot A: "+jpgMissing);
        append("Unexpected JPG identities in Snapshot B: "+jpgUnexpected);
        append("All safe identities retained from Snapshot A: "+allOverlap);
        append("All safe identities missing from Snapshot A: "+allMissing);
        append("Unexpected safe identities in Snapshot B: "+allUnexpected);
        append("JPG opaque identity sets exactly equal: "+jpgSetEqual);
        append("Full safe catalog opaque identity sets exactly equal: "+allSetEqual);
        append("Snapshot A BLE/catalog full media parity: "+snapshotAFullMediaParity);
        append("Snapshot B BLE/catalog full media parity: "+snapshotBFullMediaParity);

        if(!snapshotAFullMediaParity||!snapshotBFullMediaParity)stabilityClassification="CROSS-CHANNEL PARITY MISMATCH — BLE COUNTS DO NOT MATCH CATALOG MEDIA COUNTS";
        else if(stabilityExact)stabilityClassification="STABLE — INVENTORY AND CATALOG IDENTITIES UNCHANGED WITHOUT CAPTURE";
        else if(inventoryEqual&&totalCountEqual&&jpgCountEqual)stabilityClassification="COUNTS STABLE BUT ONE OR MORE CATALOG IDENTITIES CHANGED WITHOUT CAPTURE";
        else stabilityClassification="INVENTORY AND/OR CATALOG MEMBERSHIP CHANGED WITHOUT CAPTURE";
        append("G6A NO-CAPTURE STABILITY DIAGNOSTIC: "+stabilityClassification);
        comparisonCompleted=true;
        setStatus("Comparison complete. Exiting transfer mode.");
        sendExit();
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
            String reason=failureReason==null?"Stability diagnostic did not complete.":failureReason;
            cleanupRuntime(destroyRequested);append("");append("SUMMARY");
            append("Stage at failure: "+stage);append("Media-count queries: "+countWriteCount);append("P2P enter writes: "+enterWriteCount);
            append("Transfer-exit writes: "+exitWriteCount);append("Catalog GET requests: "+catalogGetCount);
            append("Media-file GET requests: "+mediaGetCount);append("Total HTTP GET requests: "+httpRequestCount);
            append("Remote filename/path values logged/persisted: NO");append("Glasses file mutation/deletion: 0");
            append("G6A NO-CAPTURE STABILITY RESULT: FAILED — "+reason);append("END REPORT");
            reportFinished=true;runActive=false;phase=Phase.COMPLETE;
            if(!destroyRequested){setStatus(reason);enableActions();}
            return;
        }

        if(stage==Stage.SNAPSHOT_A){
            cleanupRuntime(false);phase=Phase.COMPLETE;stage=Stage.SNAPSHOT_B;
            append("");append("SNAPSHOT A COMPLETE");
            append("Snapshot A inventory: images="+snapshotAImageCount+", videos="+snapshotAVideoCount+", recordings="+snapshotARecordCount);
            append("Snapshot A total safe catalog entries: "+snapshotATotalEntries);
            append("Snapshot A JPG/MP4/OPUS entries: "+snapshotAJpgEntries+"/"+snapshotAVideoEntries+"/"+snapshotAOpusEntries);
            append("Snapshot A media-file GET requests: 0");
            append("Post-exit 0x73/0x01 confirmation: YES");
            append("P2P group absence verified before quiet interval: YES");
            append("");append("QUIET INTERVAL");
            append("Minimum requested duration: "+STABILITY_QUIET_MS+" ms");
            append("Instruction: KEEP GLASSES UNTOUCHED — DO NOT TAKE ANY PHOTO");
            quietStartElapsedMs=SystemClock.elapsedRealtime();
            setStatus("Quiet interval. Keep glasses untouched. DO NOT TAKE A PHOTO.");
            quietTask=()->{
                quietTask=null;
                if(reportFinished||!runActive)return;
                long quietElapsed=SystemClock.elapsedRealtime()-quietStartElapsedMs;
                append("");append("QUIET INTERVAL COMPLETE");
                append("Actual monotonic quiet interval: "+quietElapsed+" ms");
                if(quietElapsed<STABILITY_QUIET_MS){
                    abortRun("Quiet interval completed earlier than the required minimum.");
                    return;
                }
                append("");append("SNAPSHOT B");
                startSnapshotConnection();
            };
            handler.postDelayed(quietTask,STABILITY_QUIET_MS);
            return;
        }

        if(stage==Stage.SNAPSHOT_B&&comparisonCompleted){
            cleanupRuntime(false);append("");append("SUMMARY");
            append("Capture action issued by app: NO");
            append("User instruction throughout: DO NOT TAKE ANY PHOTO");
            append("Media-count queries: "+countWriteCount);append("P2P enter writes: "+enterWriteCount);
            append("Transfer-exit writes: "+exitWriteCount);append("Catalog GET requests: "+catalogGetCount);
            append("Media-file GET requests: "+mediaGetCount);append("Total HTTP GET requests: "+httpRequestCount);
            boolean exactOperationTotals=countWriteCount==2&&enterWriteCount==2&&exitWriteCount==2&&catalogGetCount==2&&mediaGetCount==0&&httpRequestCount==2;
            append("Exact bounded operation totals: "+exactOperationTotals);
            append("Post-exit 0x73/0x01 confirmation completed in both snapshots: YES");
            append("P2P group absence verified after both snapshots: YES");
            append("Remote filename/path values logged/persisted: NO");append("Glasses file mutation/deletion: 0");
            boolean finalPass=stabilityExact&&exactOperationTotals;
            if(!exactOperationTotals)append("G6A NO-CAPTURE STABILITY RESULT: FAILED — OPERATION TOTALS DEVIATED FROM BOUNDED CONTRACT");
            else append("G6A NO-CAPTURE STABILITY RESULT: "+(finalPass?"PASS — "+stabilityClassification:"OBSERVED CHANGE — "+stabilityClassification));
            append("END REPORT");
            reportFinished=true;runActive=false;phase=Phase.COMPLETE;
            if(!destroyRequested){
                setStatus(finalPass?"No-capture stability PASS. Copy the report.":"No-capture diagnostic completed without a stability PASS. Copy the report.");
                enableActions();
            }
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
        if(c!=null){c.setPrimaryClip(ClipData.newPlainText("G6A No-Capture Stability Report",report.toString()));Toast.makeText(this,"Report copied",Toast.LENGTH_SHORT).show();}
    }
    private void share(){
        Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_SUBJECT,"AIMB-G1 G6A No-Capture Stability Report");i.putExtra(Intent.EXTRA_TEXT,report.toString());startActivity(Intent.createChooser(i,"Share report"));
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
        if(runActive&&!reportFinished&&!isChangingConfigurations())abortRun("App left foreground during controlled no-capture test.");
    }
    @Override protected void onDestroy(){
        destroyRequested=true;
        cancelQuiet();
        if(runActive&&!reportFinished)abortRun("Activity destroyed during controlled no-capture test.");
        else cleanupRuntime(true);
        super.onDestroy();
    }
}
