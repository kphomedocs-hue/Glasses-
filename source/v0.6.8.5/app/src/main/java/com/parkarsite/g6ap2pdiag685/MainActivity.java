package com.parkarsite.g6ap2pdiag685;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.*;
import android.os.*;
import android.widget.*;

import java.text.SimpleDateFormat;
import java.util.*;

/** Diagnostic only. No credential parser, remote file access, HTTP or proprietary reset. */
@SuppressLint("MissingPermission")
public final class MainActivity extends Activity {
    private static final int REQ_PERMISSION = 4101;
    private static final String TARGET_NAME = "AIMB-G1";
    private static final long CONNECT_TIMEOUT_MS = 30000L;
    private static final long P2P_DISCOVERY_TIMEOUT_MS = 16000L;
    private static final long P2P_CONNECTION_TIMEOUT_MS = 15000L;
    private static final long POST_CONNECT_OBSERVE_MS = 5000L;
    private static final long PREFLIGHT_TIMEOUT_MS = 5000L;
    private static final long EXIT_CALLBACK_TIMEOUT_MS = 3500L;
    private static final long CLEANUP_TOTAL_TIMEOUT_MS = 16000L;
    private static final long CLEANUP_STEP_TIMEOUT_MS = 3000L;

    private static final UUID CYAN_SERVICE = UUID.fromString("de5bf728-d711-4e47-af26-65e3012a5dc7");
    private static final UUID CYAN_NOTIFY = UUID.fromString("de5bf729-d711-4e47-af26-65e3012a5dc7");
    private static final UUID CYAN_WRITE = UUID.fromString("de5bf72a-d711-4e47-af26-65e3012a5dc7");
    private static final UUID CLIENT_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private static final byte[] ENTER_P2P = new byte[]{0x02,0x01,0x04,0x01};
    private static final byte[] EXIT_TRANSFER = new byte[]{0x02,0x01,0x09};

    private enum Phase { IDLE, PREFLIGHT, DISCOVERING, CONNECTING, CONNECTED, EXIT_SENT, CLEANUP, COMPLETE }
    private enum BleWrite { ENTER, EXIT }
    private enum CleanupStep { NONE, STOP_DISCOVERY, CANCEL_CONNECT, INSPECT_GROUP, REMOVE_GROUP, VERIFY_GROUP_FIRST, VERIFY_GROUP_SECOND, DONE }

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final StringBuilder report = new StringBuilder();
    private final Map<String,Runnable> timers = new HashMap<>();
    private final Map<String,Long> deadlines = new HashMap<>();
    private final ArrayDeque<BleWrite> pendingWrites = new ArrayDeque<>();
    private final LinkedHashSet<Integer> observed73Events = new LinkedHashSet<>();

    private BluetoothAdapter btAdapter;
    private BluetoothGatt gatt;
    private BluetoothGattCharacteristic writeChar;
    private WifiP2pManager p2pManager;
    private WifiP2pManager.Channel p2pChannel;
    private BroadcastReceiver p2pReceiver;
    private TextView statusView, reportView;
    private Button runButton, copyButton, shareButton;

    private Phase phase = Phase.IDLE;
    private CleanupStep cleanupStep = CleanupStep.NONE;
    private boolean reportFinished, terminalStarted, destroyed;
    private boolean bleConnected, enterWriteAttempted, enterCallbackSucceeded;
    private boolean exitWriteAttempted, exitWriteStarted, exitCallbackSucceeded;
    private boolean discoveryRequested, discoveryStarted, connectRequested, targetPeerMatched, groupCreatedByTest;
    private boolean preflightConfirmedAbsent, preexistingGroup, cleanupIssue, cleanupConfirmedAbsent;
    private boolean peersRequestPending, connectionInfoRequestPending;
    private int preEnter73Count, pendingEnter73Count;
    private String failureReason, pairedAddressSuffix, expectedP2pAddress;
    private BluetoothDevice pendingTarget;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        buildUi();
        BluetoothManager bm = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = bm==null?null:bm.getAdapter();
        p2pManager = (WifiP2pManager)getSystemService(Context.WIFI_P2P_SERVICE);
        if(p2pManager!=null) p2pChannel=p2pManager.initialize(this,Looper.getMainLooper(),()->{
            if(!reportFinished && phase!=Phase.IDLE && phase!=Phase.COMPLETE) {
                append("P2P channel disconnected");
                if(phase==Phase.CLEANUP) cleanupIssue=true;
                else requestTerminal("P2P channel disconnected.");
            }
        });
        renderIdle();
    }

    private void buildUi() {
        int p=dp(20);
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(p,p,p,p);
        TextView t=new TextView(this); t.setText("K G1 G6A Cyan P2P State Diagnostic"); t.setTextSize(24); root.addView(t);
        TextView n=new TextView(this);
        n.setText("Bounded Wi-Fi Direct discriminator: discovery before ENTER, single exact peer association, one EXIT attempt, verified cleanup. No HTTP, media, capture, reset, retry or credential logging.");
        n.setPadding(0,dp(8),0,dp(16)); root.addView(n);
        statusView=new TextView(this); statusView.setTextSize(16); root.addView(statusView);
        runButton=new Button(this); runButton.setText("Run bounded P2P discriminator"); runButton.setOnClickListener(v->begin()); root.addView(runButton);
        LinearLayout a=new LinearLayout(this); a.setOrientation(LinearLayout.HORIZONTAL);
        copyButton=new Button(this); copyButton.setText("Copy report"); copyButton.setEnabled(false); copyButton.setOnClickListener(v->copy()); a.addView(copyButton);
        shareButton=new Button(this); shareButton.setText("Share report"); shareButton.setEnabled(false); shareButton.setOnClickListener(v->share()); a.addView(shareButton); root.addView(a);
        ScrollView s=new ScrollView(this); reportView=new TextView(this); reportView.setTextSize(13); reportView.setTextIsSelectable(true); s.addView(reportView);
        root.addView(s,new LinearLayout.LayoutParams(-1,0,1)); setContentView(root);
    }

    private void renderIdle(){
        if(btAdapter==null||p2pManager==null||p2pChannel==null){statusView.setText("Bluetooth LE or Wi-Fi Direct is unavailable.");runButton.setEnabled(false);return;}
        statusView.setText("Ready. Force-stop Cyan Glasses, keep AIMB-G1 paired, then run this one bounded diagnostic.");
    }
    private void begin(){
        if(!hasPermissions()){requestRequiredPermissions();return;}
        try{if(!btAdapter.isEnabled()){abortBeforeRun("Bluetooth is off.");return;}}
        catch(SecurityException e){abortBeforeRun("Bluetooth permission required.");return;}
        runProbe();
    }
    private boolean hasPermissions(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.S && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!=PackageManager.PERMISSION_GRANTED)return false;
        if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.NEARBY_WIFI_DEVICES)!=PackageManager.PERMISSION_GRANTED)return false;
        if(Build.VERSION.SDK_INT<33 && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED)return false;
        return true;
    }
    private void requestRequiredPermissions(){
        ArrayList<String> ps=new ArrayList<>();
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.S)ps.add(Manifest.permission.BLUETOOTH_CONNECT);
        if(Build.VERSION.SDK_INT>=33)ps.add(Manifest.permission.NEARBY_WIFI_DEVICES);
        else {ps.add(Manifest.permission.ACCESS_COARSE_LOCATION);ps.add(Manifest.permission.ACCESS_FINE_LOCATION);}
        requestPermissions(ps.toArray(new String[0]),REQ_PERMISSION);
    }
    @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){
        super.onRequestPermissionsResult(r,p,g);
        if(r==REQ_PERMISSION){if(hasPermissions())begin();else abortBeforeRun("Required permissions declined.");}
    }
    /** Report and immutable build archive must identify the same exact CI artifact. */
    private void appendBuildIdentity(){
        append("App version: "+BuildConfig.VERSION_NAME);
        append("Build commit: "+BuildConfig.BUILD_COMMIT);
        append("Build run: "+BuildConfig.BUILD_RUN);
        append("Build attempt: "+BuildConfig.BUILD_ATTEMPT);
    }
    private void abortBeforeRun(String reason){
        report.setLength(0);
        append("K G1 G6A CYAN P2P STATE DISCRIMINATOR REPORT");
        append("Generated: "+isoNow());appendBuildIdentity();
        append("PRECONDITION FAILED: "+reason);
        append("BLE writes / Wi-Fi Direct operations: 0");
        append("G6A RESULT: NOT STARTED; G6A OPEN; G6B BLOCKED");append("END REPORT");
        reportFinished=true;phase=Phase.COMPLETE;
        setStatus(reason);enableActions();
    }

    private void runProbe(){
        if(phase!=Phase.IDLE&&phase!=Phase.COMPLETE)return;
        releaseRuntime(false);
        clearTimers(); report.setLength(0); pendingWrites.clear(); observed73Events.clear();
        reportFinished=false;terminalStarted=false;destroyed=false;
        bleConnected=false;enterWriteAttempted=false;enterCallbackSucceeded=false;
        exitWriteAttempted=false;exitWriteStarted=false;exitCallbackSucceeded=false;
        discoveryRequested=false;discoveryStarted=false;connectRequested=false;targetPeerMatched=false;groupCreatedByTest=false;
        preflightConfirmedAbsent=false;preexistingGroup=false;cleanupIssue=false;cleanupConfirmedAbsent=false;
        peersRequestPending=false;connectionInfoRequestPending=false;preEnter73Count=0;pendingEnter73Count=0;
        failureReason=null;pairedAddressSuffix=null;expectedP2pAddress=null;pendingTarget=null;
        cleanupStep=CleanupStep.NONE; phase=Phase.IDLE;
        runButton.setEnabled(false);copyButton.setEnabled(false);shareButton.setEnabled(false);
        append("K G1 G6A CYAN P2P STATE DISCRIMINATOR REPORT");
        append("Generated: "+isoNow());appendBuildIdentity();
        append("Scope: research-only; G6A stays OPEN and G6B BLOCKED");
        append("BLE writes allowed: at most one ENTER and one EXIT attempt");
        append("0x73: CRC-valid post-ENTER callback IDs only; no payloads");
        append("Peer matching: Cyan name-or-paired-BLE-suffix predicate, in memory only");
        append("Credential parsing and storage: NONE; INTERNET permission: ABSENT");
        append("HTTP/socket/media/capture/reset/retry operations: 0 by design");append("");
        BluetoothDevice target=findUniqueBondedTarget();
        if(target==null)return;
        pendingTarget=target; pairedAddressSuffix=addressSuffix(target);
        if(pairedAddressSuffix==null){requestTerminal("Cannot derive paired BLE suffix.");return;}
        registerP2pReceiver();
        startGroupPreflight();
    }
    private BluetoothDevice findUniqueBondedTarget(){
        append("BONDED TARGET CHECK");int total=0,matches=0;BluetoothDevice unique=null;
        try{
            Set<BluetoothDevice> bonded=btAdapter.getBondedDevices();total=bonded==null?0:bonded.size();
            if(bonded!=null)for(BluetoothDevice d:bonded){if(matchesTarget(safeGetName(d))){matches++;unique=d;}}
        }catch(SecurityException e){requestTerminal("Cannot inspect bonded devices.");return null;}
        append("Bonded devices total: "+total);append("AIMB-G1-family matches: "+matches);
        if(matches!=1){requestTerminal("Exactly one bonded AIMB-G1-family target required.");return null;}
        append("Bonded target: "+safeName(safeGetName(unique)));append("Addresses: NOT LOGGED");append("");return unique;
    }

    private final BluetoothGattCallback gattCb=new BluetoothGattCallback(){
        @Override public void onConnectionStateChange(BluetoothGatt x,int status,int state){handler.post(()->{
            if(reportFinished||phase==Phase.COMPLETE)return;
            if(phase==Phase.CLEANUP)return;
            if(status!=BluetoothGatt.GATT_SUCCESS || state==BluetoothProfile.STATE_DISCONNECTED){
                bleConnected=false;
                if(phase==Phase.EXIT_SENT){
                    noteCleanupIssue("BLE disconnected while awaiting EXIT acknowledgment");
                    startCleanup();return;
                }
                requestTerminal(status!=BluetoothGatt.GATT_SUCCESS?"GATT status "+status:"BLE disconnected early.");return;
            }
            if(state==BluetoothProfile.STATE_CONNECTED){
                bleConnected=true;append("GATT: connected");
                try{if(!x.discoverServices())requestTerminal("Service discovery did not start.");}
                catch(SecurityException e){requestTerminal("Bluetooth permission error.");}
            }
        });}
        @Override public void onServicesDiscovered(BluetoothGatt x,int status){handler.post(()->{
            if(terminalStarted||reportFinished)return;
            if(status!=BluetoothGatt.GATT_SUCCESS){requestTerminal("Service discovery failed.");return;}
            BluetoothGattService s=x.getService(CYAN_SERVICE);
            if(s==null){requestTerminal("Cyan service absent.");return;}
            BluetoothGattCharacteristic n=s.getCharacteristic(CYAN_NOTIFY),w=s.getCharacteristic(CYAN_WRITE);
            if(n==null||w==null){requestTerminal("Cyan notify/write characteristic absent.");return;}
            writeChar=w;append("CYAN SERVICE/NOTIFY/WRITE: PRESENT");
            try{
                if(!x.setCharacteristicNotification(n,true)){requestTerminal("Local notification registration failed.");return;}
                BluetoothGattDescriptor d=n.getDescriptor(CLIENT_CONFIG);
                if(d==null){requestTerminal("CCCD absent.");return;}
                d.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                if(!x.writeDescriptor(d)){requestTerminal("CCCD write did not start.");return;}
                append("Notification subscription write: STARTED");
            }catch(SecurityException e){requestTerminal("BLE permission error.");}
        });}
        @Override public void onDescriptorWrite(BluetoothGatt x,BluetoothGattDescriptor d,int status){handler.post(()->{
            if(terminalStarted||reportFinished)return;
            if(status!=BluetoothGatt.GATT_SUCCESS){requestTerminal("CCCD callback failed: "+status);return;}
            append("Notification subscription: SUCCESS");cancelTimer("BLE");startP2pDiscovery();
        });}
        @Override public void onCharacteristicWrite(BluetoothGatt x,BluetoothGattCharacteristic c,int status){handler.post(()->{
            if(reportFinished||phase==Phase.CLEANUP)return;
            if(!CYAN_WRITE.equals(c.getUuid()))return;
            BleWrite op=pendingWrites.pollFirst();
            if(op==null){append("Unmatched BLE write callback; ignored");return;}
            if(op==BleWrite.ENTER){
                append("ENTER write callback: "+(status==BluetoothGatt.GATT_SUCCESS?"SUCCESS":"FAILED "+status));
                if(status==BluetoothGatt.GATT_SUCCESS && !terminalStarted){enterCallbackSucceeded=true;requestPeers();}
                else if(status!=BluetoothGatt.GATT_SUCCESS)requestTerminal("ENTER write callback failed.");
            }else{
                cancelTimer("EXIT");exitCallbackSucceeded=status==BluetoothGatt.GATT_SUCCESS;
                append("EXIT callback: "+(exitCallbackSucceeded?"SUCCESS":"FAILED "+status));
                if(!exitCallbackSucceeded)noteCleanupIssue("EXIT callback failure");
                startCleanup();
            }
        });}
        @Override public void onCharacteristicChanged(BluetoothGatt x,BluetoothGattCharacteristic c,byte[] value){handleNotify(value);}
        @SuppressWarnings("deprecation") @Override public void onCharacteristicChanged(BluetoothGatt x,BluetoothGattCharacteristic c){handleNotify(c.getValue());}
    };

    private void startGroupPreflight(){
        if(terminalStarted)return;
        phase=Phase.PREFLIGHT;append("WI-FI DIRECT GROUP PREFLIGHT");
        armTimer("PREFLIGHT",PREFLIGHT_TIMEOUT_MS,()->requestTerminal("Preflight group query timed out."));
        try{p2pManager.requestGroupInfo(p2pChannel,group->{
            if(terminalStarted||phase!=Phase.PREFLIGHT)return;
            if(expired("PREFLIGHT")){requestTerminal("Preflight query arrived after deadline.");return;}
            cancelTimer("PREFLIGHT");
            if(group!=null){preexistingGroup=true;append("Pre-existing Wi-Fi Direct group: PRESENT (will not modify)");requestTerminal("Pre-existing group; abort without creating new group.");return;}
            preflightConfirmedAbsent=true;append("Pre-existing group: ABSENT");startBleConnection(pendingTarget);
        });}catch(Exception e){requestTerminal("Preflight group API unavailable.");}
    }
    private void startP2pDiscovery(){
        if(terminalStarted||!preflightConfirmedAbsent||!bleConnected||gatt==null||writeChar==null){requestTerminal("BLE/discovery prerequisites unavailable.");return;}
        phase=Phase.DISCOVERING;append("");append("WI-FI DIRECT DISCOVERY");append("Discover before ENTER: TRUE");
        // This independently clocks wall time from the request, not from BLE or Wi-Fi callbacks.
        armTimer("DISCOVERY",P2P_DISCOVERY_TIMEOUT_MS,()->requestTerminal("Cyan-matching P2P peer not found within 16 seconds."));
        discoveryRequested=true;
        try{p2pManager.discoverPeers(p2pChannel,new WifiP2pManager.ActionListener(){
            public void onSuccess(){
                if(terminalStarted||phase!=Phase.DISCOVERING)return;
                if(expired("DISCOVERY")){requestTerminal("Discovery start returned past deadline.");return;}
                discoveryStarted=true;append("discoverPeers: SUCCESS");sendEnter(gatt);
            }
            public void onFailure(int reason){if(!terminalStarted){append("discoverPeers failed reason="+reason);requestTerminal("P2P discovery start failed.");}}
        });}catch(Exception e){requestTerminal("P2P discovery API/permission error.");}
    }
    private void startBleConnection(BluetoothDevice target){
        if(terminalStarted||phase!=Phase.PREFLIGHT)return;
        append("GATT CONNECTION (before 16s Wi-Fi discovery window)");append("Target name: "+safeName(safeGetName(target)));
        armTimer("BLE",CONNECT_TIMEOUT_MS,()->requestTerminal("GATT/notification connection timeout."));
        try{gatt=target.connectGatt(this,false,gattCb,BluetoothDevice.TRANSPORT_LE);
            if(gatt==null)requestTerminal("GATT connect returned no client.");
        }catch(Exception e){requestTerminal("BLE connect request failed.");}
    }
    private void sendEnter(BluetoothGatt x){
        if(terminalStarted||reportFinished||enterWriteAttempted||phase!=Phase.DISCOVERING)return;
        if(expired("DISCOVERY")){requestTerminal("Discovery deadline passed before ENTER.");return;}
        enterWriteAttempted=true;
        append("");append("ENTER P2P MODE");append("Command: 0x41 / 02 01 04 01");append("No retry: TRUE");
        if(x==null||!writeFrame(x,frame41(ENTER_P2P))){requestTerminal("ENTER write could not start.");return;}
        pendingWrites.addLast(BleWrite.ENTER);append("ENTER write: STARTED");setStatus("Discovering the single Cyan-matching P2P peer…");
        requestPeers();
    }
    private void registerP2pReceiver(){
        if(p2pReceiver!=null)return;
        IntentFilter f=new IntentFilter();
        f.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        f.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        p2pReceiver=new BroadcastReceiver(){@Override public void onReceive(Context c,Intent i){
            if(reportFinished||terminalStarted)return;
            String a=i.getAction();
            if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(a))requestPeers();
            else if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(a))requestConnectionInfo();
        }};
        try{
            if(Build.VERSION.SDK_INT>=33)registerReceiver(p2pReceiver,f,Context.RECEIVER_NOT_EXPORTED);
            else registerReceiver(p2pReceiver,f);
        }catch(Exception e){p2pReceiver=null;requestTerminal("Cannot register P2P broadcast receiver.");}
    }
    private void requestPeers(){
        if(reportFinished||terminalStarted||phase!=Phase.DISCOVERING||!enterCallbackSucceeded||peersRequestPending)return;
        if(expired("DISCOVERY")){requestTerminal("Peer query beyond discovery deadline.");return;}
        peersRequestPending=true;
        try{p2pManager.requestPeers(p2pChannel,peers->{
            peersRequestPending=false;
            if(reportFinished||terminalStarted||phase!=Phase.DISCOVERING)return;
            if(expired("DISCOVERY")){requestTerminal("Peer result beyond discovery deadline.");return;}
            WifiP2pDevice match=null;int matches=0,count=0;
            try{
                if(peers!=null)for(WifiP2pDevice d:peers.getDeviceList()){count++;if(matchesCyanPeer(d)){match=d;matches++;}}
            }catch(Exception e){requestTerminal("Peer-list inspection failed.");return;}
            append("Peers seen: "+count+"; Cyan matches: "+matches+" (identities hidden)");
            if(matches>1){requestTerminal("Multiple Cyan-matching peers; connection refused.");return;}
            if(matches==1){targetPeerMatched=true;cancelTimer("DISCOVERY");connectPeer(match);}
        });}catch(Exception e){peersRequestPending=false;requestTerminal("P2P peer query error.");}
    }
    private void connectPeer(WifiP2pDevice d){
        if(terminalStarted||phase!=Phase.DISCOVERING)return;
        phase=Phase.CONNECTING;connectRequested=true;expectedP2pAddress=d.deviceAddress;
        WifiP2pConfig cfg=new WifiP2pConfig();cfg.deviceAddress=d.deviceAddress;cfg.wps.setup=WpsInfo.PBC;
        append("");append("WI-FI DIRECT ASSOCIATION");append("Matched peer only: TRUE");append("WPS: PBC");append("Forced group-owner role: NO");
        armTimer("ASSOCIATION",P2P_CONNECTION_TIMEOUT_MS,()->requestTerminal("P2P group did not form within connection deadline."));
        try{p2pManager.connect(p2pChannel,cfg,new WifiP2pManager.ActionListener(){
            public void onSuccess(){if(!terminalStarted&&phase==Phase.CONNECTING){append("connect request: SUCCESS");requestConnectionInfo();}}
            public void onFailure(int reason){if(!terminalStarted){append("connect request failure reason="+reason);requestTerminal("P2P connect request failed.");}}
        });}catch(Exception e){requestTerminal("P2P connection API/permission error.");}
    }
    private void requestConnectionInfo(){
        if(reportFinished||terminalStarted||phase!=Phase.CONNECTING||connectionInfoRequestPending)return;
        if(expired("ASSOCIATION")){requestTerminal("Association deadline passed.");return;}
        connectionInfoRequestPending=true;
        try{p2pManager.requestConnectionInfo(p2pChannel,info->{
            connectionInfoRequestPending=false;
            if(reportFinished||terminalStarted||phase!=Phase.CONNECTING)return;
            if(expired("ASSOCIATION")){requestTerminal("Connection info beyond deadline.");return;}
            if(info==null||!info.groupFormed)return;
            cancelTimer("ASSOCIATION");phase=Phase.CONNECTED;groupCreatedByTest=true;
            append("P2P group formed: TRUE");append("Phone is group owner: "+info.isGroupOwner);
            append("HTTP/socket/media operations: 0");setStatus("Association observed; awaiting bounded passive 0x73 events…");
            armTimer("OBSERVE",POST_CONNECT_OBSERVE_MS,()->requestTerminal(null));
        });}catch(Exception e){connectionInfoRequestPending=false;requestTerminal("Connection-info API error.");}
    }
    private void handleNotify(byte[] v){
        if(v==null||reportFinished)return;
        final byte[] data=v.clone();
        handler.post(()->{
            if(reportFinished||phase==Phase.CLEANUP||phase==Phase.COMPLETE)return;
            if(!validFrame(data)){append("Invalid BLE notification frame ignored");return;}
            int cmd=data[1]&255;
            if(cmd==0x73)handle73Event(data);
            else if(cmd==0x41 && phase==Phase.EXIT_SENT)append("EXIT response: CRC-valid 0x41 (not used as proof)");
        });
    }
    private void handle73Event(byte[] data){
        if(data.length<7)return;
        if(!enterWriteAttempted){preEnter73Count++;return;}
        if(!enterCallbackSucceeded){pendingEnter73Count++;return;}
        if(terminalStarted||exitWriteAttempted)return;
        int eventId=data[6]&255;observed73Events.add(eventId);
        append(String.format(Locale.US,"Post-ENTER callback 0x73 event ID: 0x%02X",eventId));
    }

    /** Single terminal path: all attempted ENTER operations route through at most one EXIT. */
    private void requestTerminal(String reason){
        if(reportFinished||terminalStarted)return;
        terminalStarted=true;
        if(reason!=null){failureReason=reason;append("DIAGNOSTIC STOP: "+reason);}
        cancelTimer("PREFLIGHT");cancelTimer("DISCOVERY");cancelTimer("BLE");
        cancelTimer("ASSOCIATION");cancelTimer("OBSERVE");
        if(enterWriteAttempted)sendExitOnce();else startCleanup();
    }
    private void sendExitOnce(){
        if(exitWriteAttempted){startCleanup();return;}
        exitWriteAttempted=true;phase=Phase.EXIT_SENT;
        append("");append("EXIT TRANSFER MODE");append("Command: 0x41 / 02 01 09; at most one attempt");
        if(!bleConnected||gatt==null||writeChar==null){
            append("EXIT: IMPOSSIBLE — BLE transport unavailable");noteCleanupIssue("EXIT impossible after ENTER");startCleanup();return;
        }
        if(!writeFrame(gatt,frame41(EXIT_TRANSFER))){
            append("EXIT write: FAILED TO START; no retry");noteCleanupIssue("EXIT write could not start");startCleanup();return;
        }
        exitWriteStarted=true;pendingWrites.addLast(BleWrite.EXIT);append("EXIT write: STARTED");
        armTimer("EXIT",EXIT_CALLBACK_TIMEOUT_MS,()->{
            append("EXIT callback: TIMED OUT");noteCleanupIssue("EXIT callback timeout");startCleanup();
        });
    }
    private void startCleanup(){
        if(phase==Phase.CLEANUP||phase==Phase.COMPLETE)return;
        phase=Phase.CLEANUP;cleanupStep=CleanupStep.NONE;
        cancelTimer("EXIT");cancelTimer("OBSERVE");cancelTimer("ASSOCIATION");
        cancelTimer("DISCOVERY");cancelTimer("BLE");cancelTimer("PREFLIGHT");
        append("");append("BOUNDED TERMINAL CLEANUP");
        armTimer("CLEANUP_TOTAL",CLEANUP_TOTAL_TIMEOUT_MS,()->{
            noteCleanupIssue("Overall cleanup deadline exceeded");finishReport();
        });
        stopDiscoveryCleanup();
    }
    private boolean atStep(CleanupStep step){return !reportFinished&&phase==Phase.CLEANUP&&cleanupStep==step;}
    private void cleanupStepTimeout(CleanupStep s,String msg,Runnable next){
        armTimer("CLEANUP_STEP",CLEANUP_STEP_TIMEOUT_MS,()->{
            if(!atStep(s))return;
            noteCleanupIssue(msg+" callback timed out");next.run();
        });
    }
    private void stopDiscoveryCleanup(){
        cleanupStep=CleanupStep.STOP_DISCOVERY;cancelTimer("CLEANUP_STEP");
        if(!discoveryRequested||p2pManager==null||p2pChannel==null){append("stopPeerDiscovery: NOT NEEDED");cancelConnectCleanup();return;}
        cleanupStepTimeout(CleanupStep.STOP_DISCOVERY,"stopPeerDiscovery",this::cancelConnectCleanup);
        try{p2pManager.stopPeerDiscovery(p2pChannel,new WifiP2pManager.ActionListener(){
            public void onSuccess(){if(atStep(CleanupStep.STOP_DISCOVERY)){cancelTimer("CLEANUP_STEP");append("stopPeerDiscovery: SUCCESS");cancelConnectCleanup();}}
            public void onFailure(int r){if(atStep(CleanupStep.STOP_DISCOVERY)){cancelTimer("CLEANUP_STEP");append("stopPeerDiscovery: FAILED "+r);noteCleanupIssue("stopPeerDiscovery failed");cancelConnectCleanup();}}
        });}catch(Exception e){cancelTimer("CLEANUP_STEP");noteCleanupIssue("stopPeerDiscovery API error");cancelConnectCleanup();}
    }
    private void cancelConnectCleanup(){
        cleanupStep=CleanupStep.CANCEL_CONNECT;cancelTimer("CLEANUP_STEP");
        if(!connectRequested||p2pManager==null||p2pChannel==null){append("cancelConnect: NOT NEEDED");inspectGroupCleanup();return;}
        cleanupStepTimeout(CleanupStep.CANCEL_CONNECT,"cancelConnect",this::inspectGroupCleanup);
        try{p2pManager.cancelConnect(p2pChannel,new WifiP2pManager.ActionListener(){
            public void onSuccess(){if(atStep(CleanupStep.CANCEL_CONNECT)){cancelTimer("CLEANUP_STEP");append("cancelConnect: SUCCESS");inspectGroupCleanup();}}
            public void onFailure(int r){if(atStep(CleanupStep.CANCEL_CONNECT)){cancelTimer("CLEANUP_STEP");append("cancelConnect: FAILED "+r+" (verify group independently)");inspectGroupCleanup();}}
        });}catch(Exception e){cancelTimer("CLEANUP_STEP");noteCleanupIssue("cancelConnect API error");inspectGroupCleanup();}
    }
    private void inspectGroupCleanup(){
        cleanupStep=CleanupStep.INSPECT_GROUP;cancelTimer("CLEANUP_STEP");
        if(p2pManager==null||p2pChannel==null){noteCleanupIssue("P2P channel unavailable during cleanup");finishReport();return;}
        cleanupStepTimeout(CleanupStep.INSPECT_GROUP,"Cleanup group inspection",this::verifyGroupFirst);
        try{p2pManager.requestGroupInfo(p2pChannel,group->{
            if(!atStep(CleanupStep.INSPECT_GROUP))return;
            cancelTimer("CLEANUP_STEP");
            if(group==null){append("Cleanup group inspection: ABSENT");verifyGroupFirst();return;}
            if(preexistingGroup){append("Pre-existing group preserved (not ours)");verifyGroupFirst();return;}
            if(DiagnosticSafetyRules.mayRemoveGroup(preflightConfirmedAbsent,preexistingGroup,groupCreatedByTest,connectRequested,groupContainsTarget(group))){
                append("Group attributable to test: YES (identity not logged)");removeOwnGroupCleanup();
            }else{
                append("Unexpected group; ownership unverified — NOT REMOVED");noteCleanupIssue("Unexpected group ownership unverified");verifyGroupFirst();
            }
        });}catch(Exception e){cancelTimer("CLEANUP_STEP");noteCleanupIssue("Cleanup group inspection API error");verifyGroupFirst();}
    }
    private boolean groupContainsTarget(WifiP2pGroup group){
        if(group==null||expectedP2pAddress==null)return false;
        try{
            WifiP2pDevice owner=group.getOwner();
            if(owner!=null&&expectedP2pAddress.equalsIgnoreCase(owner.deviceAddress))return true;
            Collection<WifiP2pDevice> clients=group.getClientList();
            if(clients!=null)for(WifiP2pDevice d:clients)if(expectedP2pAddress.equalsIgnoreCase(d.deviceAddress))return true;
        }catch(Exception ignored){ /* ownership remains unproven; never remove */ }
        return false;
    }
    private void removeOwnGroupCleanup(){
        cleanupStep=CleanupStep.REMOVE_GROUP;cancelTimer("CLEANUP_STEP");
        cleanupStepTimeout(CleanupStep.REMOVE_GROUP,"removeGroup",this::verifyGroupFirst);
        try{p2pManager.removeGroup(p2pChannel,new WifiP2pManager.ActionListener(){
            public void onSuccess(){if(atStep(CleanupStep.REMOVE_GROUP)){cancelTimer("CLEANUP_STEP");append("removeGroup: SUCCESS");verifyGroupFirst();}}
            public void onFailure(int r){if(atStep(CleanupStep.REMOVE_GROUP)){cancelTimer("CLEANUP_STEP");append("removeGroup: FAILED "+r);noteCleanupIssue("removeGroup failed");verifyGroupFirst();}}
        });}catch(Exception e){cancelTimer("CLEANUP_STEP");noteCleanupIssue("removeGroup API error");verifyGroupFirst();}
    }
    private void verifyGroupFirst(){
        cleanupStep=CleanupStep.VERIFY_GROUP_FIRST;cancelTimer("CLEANUP_STEP");
        // Allow pending Android group transitions to settle; only one removal attempt is ever made.
        handler.postDelayed(()->{
            if(atStep(CleanupStep.VERIFY_GROUP_FIRST))queryGroupVerification(false);
        },500);
    }
    private void queryGroupVerification(boolean second){
        CleanupStep s=second?CleanupStep.VERIFY_GROUP_SECOND:CleanupStep.VERIFY_GROUP_FIRST;
        if(!atStep(s))return;
        cleanupStepTimeout(s,second?"Final group verification":"First group verification",()->{
            if(second)finishReport();else verifyGroupSecond();
        });
        try{p2pManager.requestGroupInfo(p2pChannel,group->{
            if(!atStep(s))return;
            cancelTimer("CLEANUP_STEP");
            boolean absent=group==null;
            append((second?"Final":"First")+" post-cleanup P2P group absent: "+absent);
            if(!absent)noteCleanupIssue("P2P group present after cleanup");
            if(second){cleanupConfirmedAbsent=absent && !preexistingGroup;finishReport();}
            else verifyGroupSecond();
        });}catch(Exception e){cancelTimer("CLEANUP_STEP");noteCleanupIssue("Group verification API error");if(second)finishReport();else verifyGroupSecond();}
    }
    private void verifyGroupSecond(){
        cleanupStep=CleanupStep.VERIFY_GROUP_SECOND;cancelTimer("CLEANUP_STEP");
        handler.postDelayed(()->{if(atStep(CleanupStep.VERIFY_GROUP_SECOND))queryGroupVerification(true);},500);
    }
    private void noteCleanupIssue(String s){cleanupIssue=true;append("CLEANUP WARNING: "+s);}
    private void finishReport(){
        if(reportFinished)return;
        cancelTimer("CLEANUP_STEP");cancelTimer("CLEANUP_TOTAL");clearTimers();
        cleanupStep=CleanupStep.DONE;phase=Phase.COMPLETE;
        append("");append("SUMMARY");
        append("Peer matched: "+targetPeerMatched);
        append("P2P group formed: "+groupCreatedByTest);
        append("ENTER attempt: "+enterWriteAttempted+"; callback success: "+enterCallbackSucceeded);
        append("EXIT attempted: "+exitWriteAttempted+"; started: "+exitWriteStarted+"; callback success: "+exitCallbackSucceeded);
        append("Pre-ENTER 0x73 events ignored: "+preEnter73Count);
        append("Pending-ENTER-callback 0x73 events ignored: "+pendingEnter73Count);
        append("Post-ENTER callback 0x73 event IDs: "+eventIdSummary());
        append("HTTP/socket/media/capture/reset operations: 0");append("Credential values logged/persisted: NO");
        append("Confirmed group absent after cleanup: "+cleanupConfirmedAbsent);
        append("Cleanup issue: "+cleanupIssue);
        boolean safe=DiagnosticSafetyRules.cleanTerminalClassification(enterWriteAttempted,exitWriteStarted,exitCallbackSucceeded,cleanupConfirmedAbsent,cleanupIssue);
        if(!safe){
            append("CLEANUP RESULT: FAILED OR INDETERMINATE — NO POSITIVE P2P CLASSIFICATION");
            if(failureReason!=null)append("Primary failure: "+failureReason);
            append("G6A RESULT: INCONCLUSIVE / FAIL-CLOSED");
        }else if(failureReason!=null){
            append("CLEANUP RESULT: VERIFIED");append("G6A RESULT: INCONCLUSIVE — "+failureReason);
        }else if(groupCreatedByTest){
            append("CLEANUP RESULT: VERIFIED");
            append(observed73Events.contains(0x08)
                ?"G6A RESULT: P2P FORMED + POST-ENTER 0x73/08 — REVIEW PHYSICAL REPORT"
                :"G6A RESULT: P2P FORMED, NO POST-ENTER 0x73/08 — REVIEW PHYSICAL REPORT");
        }else{
            append("CLEANUP RESULT: VERIFIED");append("G6A RESULT: INCONCLUSIVE — GROUP NOT FORMED");
        }
        append("G6A GATE: OPEN; G6B GATE: BLOCKED");append("END REPORT");
        reportFinished=true;
        releaseRuntime(destroyed);
        setStatus("Diagnostic ended; no rerun in this app session. Review full report.");enableActions();
    }

    private boolean writeFrame(BluetoothGatt x,byte[] frame){
        if(writeChar==null||x==null||!validFrame(frame))return false;
        try{
            writeChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            writeChar.setValue(frame);
            return x.writeCharacteristic(writeChar);
        }catch(Exception e){return false;}
    }
    private static byte[] frame41(byte[] payload){
        int crc=crc16(payload);byte[] f=new byte[payload.length+6];f[0]=(byte)0xBC;f[1]=0x41;f[2]=(byte)payload.length;f[3]=0;
        f[4]=(byte)(crc&255);f[5]=(byte)((crc>>>8)&255);System.arraycopy(payload,0,f,6,payload.length);return f;
    }
    private static int crc16(byte[] d){int c=0xffff;for(byte b:d){c^=b&255;for(int i=0;i<8;i++)c=(c&1)!=0?(c>>>1)^0xA001:c>>>1;}return c&0xffff;}
    private static boolean validFrame(byte[] f){
        if(f==null||f.length<6||(f[0]&255)!=0xBC)return false;
        int l=(f[2]&255)|((f[3]&255)<<8);if(f.length!=l+6)return false;
        byte[] p=Arrays.copyOfRange(f,6,f.length);int e=(f[4]&255)|((f[5]&255)<<8);return crc16(p)==e;
    }
    private void armTimer(String name,long ms,Runnable action){
        cancelTimer(name);long due=SystemClock.elapsedRealtime()+ms;deadlines.put(name,due);
        final Runnable[] holder=new Runnable[1];
        holder[0]=()->{if(timers.get(name)!=holder[0])return;timers.remove(name);deadlines.remove(name);action.run();};
        timers.put(name,holder[0]);handler.postDelayed(holder[0],ms);
    }
    private boolean expired(String name){Long deadline=deadlines.get(name);return deadline!=null&&SystemClock.elapsedRealtime()>=deadline;}
    private void cancelTimer(String name){Runnable r=timers.remove(name);deadlines.remove(name);if(r!=null)handler.removeCallbacks(r);}
    private void clearTimers(){for(Runnable r:timers.values())handler.removeCallbacks(r);timers.clear();deadlines.clear();}
    private String eventIdSummary(){
        if(observed73Events.isEmpty())return "none";
        StringBuilder b=new StringBuilder();for(Integer id:observed73Events){if(b.length()>0)b.append(", ");b.append(String.format(Locale.US,"0x%02X",id));}return b.toString();
    }
    private void releaseRuntime(boolean closeChannel){
        pendingTarget=null;pairedAddressSuffix=null;expectedP2pAddress=null;
        if(p2pReceiver!=null){try{unregisterReceiver(p2pReceiver);}catch(Exception ignored){}p2pReceiver=null;}
        BluetoothGatt x=gatt;gatt=null;writeChar=null;bleConnected=false;
        if(x!=null){try{x.disconnect();}catch(Exception ignored){}try{x.close();}catch(Exception ignored){}}
        if(closeChannel && p2pChannel!=null && Build.VERSION.SDK_INT>=27){try{p2pChannel.close();}catch(Exception ignored){}p2pChannel=null;}
    }
    private void copy(){ClipboardManager c=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);if(c!=null){c.setPrimaryClip(ClipData.newPlainText("G6A P2P Diagnostic Report",report.toString()));Toast.makeText(this,"Report copied",Toast.LENGTH_SHORT).show();}}
    private void share(){Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_SUBJECT,"AIMB-G1 G6A P2P Diagnostic Report");i.putExtra(Intent.EXTRA_TEXT,report.toString());startActivity(Intent.createChooser(i,"Share diagnostic report"));}
    private void enableActions(){copyButton.setEnabled(report.length()>0);shareButton.setEnabled(report.length()>0);runButton.setEnabled(false);reportView.setText(report.toString());}
    private void append(String s){report.append(s).append('\n');reportView.setText(report.toString());}
    private void setStatus(String s){statusView.setText(s);}
    private static boolean matchesTarget(String n){if(n==null)return false;String x=n.trim().toUpperCase(Locale.US);return x.equals(TARGET_NAME)||x.startsWith(TARGET_NAME+"_");}
    private static String safeGetName(BluetoothDevice d){try{return d==null?null:d.getName();}catch(SecurityException e){return null;}}
    private static String addressSuffix(BluetoothDevice d){try{return d==null?null:d.getAddress().replace(":","").toUpperCase(Locale.US);}catch(SecurityException e){return null;}}
    private boolean matchesCyanPeer(WifiP2pDevice d){
        if(d==null||d.deviceName==null||pairedAddressSuffix==null)return false;
        String name=d.deviceName.trim();
        return name.equalsIgnoreCase(TARGET_NAME)||name.toUpperCase(Locale.US).endsWith(pairedAddressSuffix);
    }
    private static String safeName(String n){if(n==null)return "<none>";String x=n.trim().toUpperCase(Locale.US);return x.equals(TARGET_NAME)?TARGET_NAME:x.startsWith(TARGET_NAME+"_")?TARGET_NAME+"_<suffix>":"<other>";}
    private static String isoNow(){return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ",Locale.US).format(new Date());}
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}
    @Override protected void onStop(){
        if(!reportFinished&&phase!=Phase.IDLE&&phase!=Phase.COMPLETE)requestTerminal("Activity left foreground; terminating diagnostic.");
        super.onStop();
    }
    @Override protected void onDestroy(){
        destroyed=true;
        if(!reportFinished&&phase!=Phase.IDLE&&phase!=Phase.COMPLETE)requestTerminal("Activity destroyed during diagnostic.");
        else releaseRuntime(true);
        super.onDestroy();
    }
}
