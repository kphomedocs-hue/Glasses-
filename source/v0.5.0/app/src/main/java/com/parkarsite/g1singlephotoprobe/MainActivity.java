package com.parkarsite.g1singlephotoprobe;

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
    private static final int CATALOG_MAX_BYTES = 65536;
    private static final int CATALOG_MAX_ITEMS = 256;
    private static final int MEDIA_MAX_BYTES = 33554432;
    private static final String CATALOG_PATH = "/files/media.config";
    private static final String MEDIA_PREFIX = "/files/";

    private static final UUID CYAN_SERVICE = UUID.fromString("de5bf728-d711-4e47-af26-65e3012a5dc7");
    private static final UUID CYAN_NOTIFY = UUID.fromString("de5bf729-d711-4e47-af26-65e3012a5dc7");
    private static final UUID CYAN_WRITE = UUID.fromString("de5bf72a-d711-4e47-af26-65e3012a5dc7");
    private static final UUID CLIENT_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private static final byte[] ENTER_P2P = new byte[]{0x02,0x01,0x04,0x01};
    private static final byte[] EXIT_TRANSFER = new byte[]{0x02,0x01,0x09};

    private enum Phase { IDLE, ENTER_SENT, DISCOVERING, CONNECTING, CONNECTED, CATALOG_GET, MEDIA_GET, EXIT_SENT, CLEANUP, COMPLETE }
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
    private int enterWriteCount;
    private int exitWriteCount;
    private int currentCatalogEntries;
    private int deltaCount;
    private long downloadedBytes;
    private final LinkedHashSet<String> baselineCatalog = new LinkedHashSet<>();
    private final LinkedHashSet<Integer> observed73Events = new LinkedHashSet<>();
    private String expectedP2pName;
    private String credentialPassword;
    private Runnable timeout;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        buildUi();
        BluetoothManager bm=(BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter=bm==null?null:bm.getAdapter();
        p2pManager=(WifiP2pManager)getSystemService(Context.WIFI_P2P_SERVICE);
        if(p2pManager!=null) p2pChannel=p2pManager.initialize(this,Looper.getMainLooper(),()->appendThreadSafe("P2P channel: DISCONNECTED"));
        renderIdle();
    }

    private void buildUi(){
        int p=dp(20);
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(p,p,p,p);
        TextView t=new TextView(this); t.setText("K G1 Disposable Photo Probe v0.5.0"); t.setTextSize(24); root.addView(t);
        TextView n=new TextView(this); n.setText("G5: baseline the safe catalog, exit transfer, then after you capture exactly one disposable photo, reconnect and download only the single new JPG. No old-file guessing, no retry, no multi-file sync."); n.setPadding(0,dp(8),0,dp(16)); root.addView(n);
        statusView=new TextView(this); statusView.setTextSize(16); root.addView(statusView);
        runButton=new Button(this); runButton.setText("Start G5 baseline"); runButton.setOnClickListener(v->begin()); root.addView(runButton);
        LinearLayout a=new LinearLayout(this); a.setOrientation(LinearLayout.HORIZONTAL);
        copyButton=new Button(this); copyButton.setText("Copy report"); copyButton.setEnabled(false); copyButton.setOnClickListener(v->copy()); a.addView(copyButton);
        shareButton=new Button(this); shareButton.setText("Share report"); shareButton.setEnabled(false); shareButton.setOnClickListener(v->share()); a.addView(shareButton); root.addView(a);
        ScrollView s=new ScrollView(this); reportView=new TextView(this); reportView.setTextSize(13); reportView.setTextIsSelectable(true); s.addView(reportView);
        root.addView(s,new LinearLayout.LayoutParams(-1,0,1)); setContentView(root);
    }

    private void renderIdle(){
        if(btAdapter==null||p2pManager==null){statusView.setText("Bluetooth LE or Wi-Fi Direct is unavailable.");runButton.setEnabled(false);return;}
        statusView.setText("Ready. Force-stop Cyan Glasses, keep AIMB-G1 paired, then start the G5 baseline.");
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
            enterWriteCount=0;
            exitWriteCount=0;
            currentCatalogEntries=0;
            deltaCount=0;
            downloadedBytes=0;

            append("K G1 DISPOSABLE PHOTO PROBE REPORT");
            append("Generated: "+isoNow());
            append("App version: 0.5.0");
            append("Gate: G5 one disposable JPG download by two-phase catalog delta");
            append("Selection: BASELINE -> EXIT -> USER CAPTURES ONE JPG -> RECONNECT -> EXACTLY ONE NEW SAFE JPG");
            append("BLE writes allowed: P2P enter once + transfer exit once PER PHASE");
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
            append("");
            append("PHASE A — BASELINE");
        }else{
            if(!baselineReady||baselineCatalog.isEmpty()){
                fail("Baseline is unavailable. Restart G5 from Phase A.");
                return;
            }
            stage=Stage.DOWNLOAD;
            reportFinished=false;
            append("");
            append("PHASE B — POST-CAPTURE");
            append("User action confirmation: exactly one disposable JPG was captured before continuing.");
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
            handler.post(()->sendEnter(x));
        }
        @Override public void onCharacteristicWrite(BluetoothGatt x,BluetoothGattCharacteristic c,int status){
            handler.post(()->{
                if(reportFinished)return;
                append("BLE characteristic write status: "+(status==BluetoothGatt.GATT_SUCCESS?"SUCCESS":status));
                if(status!=BluetoothGatt.GATT_SUCCESS) fail("BLE write callback failed.");
            });
        }
        @Override public void onCharacteristicChanged(BluetoothGatt x,BluetoothGattCharacteristic c,byte[] value){handleNotify(value);}
        @SuppressWarnings("deprecation") @Override public void onCharacteristicChanged(BluetoothGatt x,BluetoothGattCharacteristic c){handleNotify(c.getValue());}
    };

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
            if(cmd==0x41 && phase==Phase.ENTER_SENT && parseTransferCredentials(data)){
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
                append("P2P group formed: TRUE");
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
        if(reportFinished||phase!=Phase.CONNECTED||!p2pGroupFormed||!phoneIsGroupOwner||catalogGetAttempted||glassesClientIp==null)return;
        if(!isConfirmedP2pIp(glassesClientIp)){failWithExit("Resolved IPv4 is outside the confirmed 192.168.49.0/24 P2P subnet.");return;}
        cancelTimeout(); catalogGetAttempted=true; catalogGetCount++; httpRequestCount++; phase=Phase.CATALOG_GET;
        append(""); append(stage==Stage.BASELINE?"PHASE A CATALOG":"PHASE B CATALOG");
        append("Method: GET");
        append("Path: "+CATALOG_PATH);
        append("Target: passive 0x73/0x08 glasses IPv4 only");
        append("Redirects: DISABLED");
        append("Max response bytes: "+CATALOG_MAX_BYTES);
        append("Media-file GET requests so far: "+mediaGetCount);
        setStatus(stage==Stage.BASELINE?"Phase A: reading baseline media.config once…":"Phase B: reading post-capture media.config once…");
        final String ip=glassesClientIp;
        new Thread(()->fetchCatalogOnce(ip),"g4b-catalog-get").start();
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
                currentCatalogEntries=summary.safeEntries.size();

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
        if(!isConfirmedP2pIp(ip)||!isStrictMediaRelativePath(candidate)||!".jpg".equals(extensionOnly(candidate))){
            failWithExit("Media candidate failed final pre-request guard.");
            return;
        }
        mediaGetCount++;
        httpRequestCount++;
        phase=Phase.MEDIA_GET;
        append("");
        append("ONE DISPOSABLE JPG DOWNLOAD");
        append("Method: GET");
        append("Target: passive glasses IPv4 + /files/ + exact new catalog entry");
        append("Redirects: DISABLED");
        append("Retry/resume/Range: DISABLED");
        append("Max media bytes: "+MEDIA_MAX_BYTES);
        append("Remote filename/path value logged: NO");
        final String safeIp=ip;
        final String safeCandidate=candidate;
        new Thread(()->downloadMediaOnce(safeIp,safeCandidate),"g5-one-media-get").start();
    }

    private void downloadMediaOnce(String ip,String candidate){
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
            int status=c.getResponseCode();
            long declared=c.getContentLengthLong();
            String contentType=safeHeader(c.getContentType());
            if(status!=HttpURLConnection.HTTP_OK){
                postMediaFailure("Media HTTP status "+status);
                return;
            }
            if(declared>MEDIA_MAX_BYTES){
                postMediaFailure("Media Content-Length exceeds safety cap.");
                return;
            }

            File localFile=new File(getCacheDir(),"g5_disposable_"+System.currentTimeMillis()+".jpg");
            MediaSummary m=streamMediaBounded(c.getInputStream(),localFile,MEDIA_MAX_BYTES);
            m.declaredLength=declared;
            m.contentType=contentType;
            if(m.bytes<=0){
                postMediaFailure("Downloaded media is empty.");
                return;
            }
            if(declared>=0&&m.bytes!=declared){
                postMediaFailure("Downloaded byte count does not match Content-Length.");
                return;
            }
            if(!m.jpegStart||!m.jpegEnd){
                postMediaFailure("Downloaded media failed JPEG signature validation.");
                return;
            }

            handler.post(()->{
                if(reportFinished)return;
                downloadedBytes=m.bytes;
                mediaSuccess=true;
                append("Media HTTP status: 200");
                append("Media Content-Type: "+m.contentType);
                append("Declared Content-Length: "+(m.declaredLength<0?"<none>":Long.toString(m.declaredLength)));
                append("Downloaded bytes: "+m.bytes);
                append("JPEG SOI signature: PASS");
                append("JPEG EOI signature: PASS");
                append("App-private temporary file created: YES");
                append("Local temporary path logged: NO");
                append("Remote filename/path value logged: NO");
                append("Media-file GET requests: "+mediaGetCount);
                setStatus("One disposable JPG downloaded and validated. Exiting transfer mode…");
                sendExit();
            });
        }catch(Exception e){
            postMediaFailure("Media GET/stream failed: "+e.getClass().getSimpleName());
        }finally{
            if(c!=null)c.disconnect();
        }
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
        append("G5 ERROR: "+msg);
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
            String reason=failureReason==null?"G5 did not complete.":failureReason;
            cleanupRuntime(false);
            append("");
            append("SUMMARY");
            append("Stage at failure: "+stage);
            append("P2P enter writes: "+enterWriteCount);
            append("Transfer-exit writes: "+exitWriteCount);
            append("Catalog GET requests: "+catalogGetCount);
            append("Media-file GET requests: "+mediaGetCount);
            append("Total HTTP GET requests: "+httpRequestCount);
            append("Credentials logged/persisted: NO");
            append("Remote filename/path values logged: NO");
            append("G5 RESULT: FAILED — "+reason);
            append("END REPORT");
            baselineCatalog.clear();
            baselineReady=false;
            reportFinished=true;
            phase=Phase.COMPLETE;
            setStatus(reason);
            enableActions();
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
        append("Baseline entries retained in memory only: "+baselineCatalog.size());
        append("Phase A media-file GET requests: 0");
        append("Transfer mode exited before user capture: YES");
        append("Baseline filename/path values logged: NO");
        stage=Stage.WAIT_FOR_CAPTURE;
        phase=Phase.IDLE;
        setStatus("Phase A complete. Now use the glasses to capture EXACTLY ONE disposable photo. Then tap Continue G5.");
        runButton.setText("I captured ONE test photo — Continue G5");
        runButton.setEnabled(true);
        copyButton.setEnabled(false);
        shareButton.setEnabled(false);
    }

    private void finishSuccess(){
        cleanupRuntime(false);
        append("");
        append("SUMMARY");
        append("Baseline catalog entries: "+baselineCatalog.size());
        append("Post-capture catalog entries: "+currentCatalogEntries);
        append("New catalog entries: "+deltaCount);
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
        append("G5 RESULT: ONE DISPOSABLE JPG DOWNLOADED — REVIEW PHYSICAL REPORT");
        append("END REPORT");
        baselineCatalog.clear();
        baselineReady=false;
        reportFinished=true;
        phase=Phase.COMPLETE;
        stage=Stage.BASELINE;
        setStatus("G5 complete. Review the report before any further media work.");
        runButton.setText("Start G5 baseline");
        enableActions();
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

    private void schedule(Runnable r,long ms){cancelTimeout();timeout=r;handler.postDelayed(r,ms);}
    private void cancelTimeout(){if(timeout!=null){handler.removeCallbacks(timeout);timeout=null;}}

    private void fail(String m){if(reportFinished)return;cancelTimeout();append("G5 RESULT: FAILED — "+m);cleanupRuntime(false);baselineCatalog.clear();baselineReady=false;append("END REPORT");reportFinished=true;phase=Phase.COMPLETE;stage=Stage.BASELINE;setStatus(m);runButton.setText("Start G5 baseline");enableActions();}
    private void closeAndFail(String m){handler.post(()->fail(m));}

    private void cleanupRuntime(boolean destroy){
        cancelTimeout();
        expectedP2pName=null; credentialPassword=null;
        if(receiverRegistered&&p2pReceiver!=null){try{unregisterReceiver(p2pReceiver);}catch(Exception ignored){} receiverRegistered=false;p2pReceiver=null;}
        BluetoothGatt x=gatt;gatt=null;writeChar=null;if(x!=null){try{x.disconnect();}catch(Exception ignored){}try{x.close();}catch(Exception ignored){}}
        if(destroy&&p2pChannel!=null&&Build.VERSION.SDK_INT>=27){try{p2pChannel.close();}catch(Exception ignored){}}
    }

    private void copy(){ClipboardManager c=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);if(c!=null){c.setPrimaryClip(ClipData.newPlainText("G5 Disposable Photo Report",report.toString()));Toast.makeText(this,"Report copied",Toast.LENGTH_SHORT).show();}}
    private void share(){Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_SUBJECT,"AIMB-G1 G5 Disposable Photo Report");i.putExtra(Intent.EXTRA_TEXT,report.toString());startActivity(Intent.createChooser(i,"Share G5 report"));}
    private void enableActions(){copyButton.setEnabled(report.length()>0);shareButton.setEnabled(report.length()>0);runButton.setEnabled(true);reportView.setText(report.toString());}
    private void append(String s){report.append(s).append('\n');reportView.setText(report.toString());}
    private void appendThreadSafe(String s){handler.post(()->{if(!reportFinished)append(s);});}
    private void setStatus(String s){statusView.setText(s);}
    private static boolean matchesTarget(String n){if(n==null)return false;String x=n.trim().toUpperCase(Locale.US);return x.equals(TARGET_NAME)||x.startsWith(TARGET_NAME+"_");}
    private static String safeGetName(BluetoothDevice d){try{return d==null?null:d.getName();}catch(SecurityException e){return null;}}
    private static String safeName(String n){if(n==null)return"<none>";String x=n.trim().toUpperCase(Locale.US);return x.equals(TARGET_NAME)?TARGET_NAME:x.startsWith(TARGET_NAME+"_")?TARGET_NAME+"_<suffix>":"<other>";}
    private static String isoNow(){return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ",Locale.US).format(new Date());}
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}

    @Override protected void onDestroy(){baselineCatalog.clear();cleanupRuntime(true);super.onDestroy();}
}
