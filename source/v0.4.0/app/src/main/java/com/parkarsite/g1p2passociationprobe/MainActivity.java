package com.parkarsite.g1p2passociationprobe;

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
    private static final long POST_CONNECT_OBSERVE_MS = 5000L;

    private static final UUID CYAN_SERVICE = UUID.fromString("de5bf728-d711-4e47-af26-65e3012a5dc7");
    private static final UUID CYAN_NOTIFY = UUID.fromString("de5bf729-d711-4e47-af26-65e3012a5dc7");
    private static final UUID CYAN_WRITE = UUID.fromString("de5bf72a-d711-4e47-af26-65e3012a5dc7");
    private static final UUID CLIENT_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private static final byte[] ENTER_P2P = new byte[]{0x02,0x01,0x04,0x01};
    private static final byte[] EXIT_TRANSFER = new byte[]{0x02,0x01,0x09};

    private enum Phase { IDLE, ENTER_SENT, DISCOVERING, CONNECTING, CONNECTED, EXIT_SENT, CLEANUP, COMPLETE }

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
    private boolean reportFinished;
    private boolean enterWriteAttempted;
    private boolean exitWriteAttempted;
    private boolean targetPeerMatched;
    private boolean failurePending;
    private String failureReason;
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
        TextView t=new TextView(this); t.setText("K G1 P2P Association Probe"); t.setTextSize(24); root.addView(t);
        TextView n=new TextView(this); n.setText("G4A: enters glasses P2P mode, exactly matches the BLE-reported peer name in memory, associates via Wi-Fi Direct, records sanitized connection metadata, exits and cleans up. No HTTP or file access."); n.setPadding(0,dp(8),0,dp(16)); root.addView(n);
        statusView=new TextView(this); statusView.setTextSize(16); root.addView(statusView);
        runButton=new Button(this); runButton.setText("Run G4A association probe"); runButton.setOnClickListener(v->begin()); root.addView(runButton);
        LinearLayout a=new LinearLayout(this); a.setOrientation(LinearLayout.HORIZONTAL);
        copyButton=new Button(this); copyButton.setText("Copy report"); copyButton.setEnabled(false); copyButton.setOnClickListener(v->copy()); a.addView(copyButton);
        shareButton=new Button(this); shareButton.setText("Share report"); shareButton.setEnabled(false); shareButton.setOnClickListener(v->share()); a.addView(shareButton); root.addView(a);
        ScrollView s=new ScrollView(this); reportView=new TextView(this); reportView.setTextSize(13); reportView.setTextIsSelectable(true); s.addView(reportView);
        root.addView(s,new LinearLayout.LayoutParams(-1,0,1)); setContentView(root);
    }

    private void renderIdle(){
        if(btAdapter==null||p2pManager==null){statusView.setText("Bluetooth LE or Wi-Fi Direct is unavailable.");runButton.setEnabled(false);return;}
        statusView.setText("Ready. Force-stop Cyan Glasses, keep AIMB-G1 paired, then run G4A.");
    }

    private void begin(){
        if(!hasPermissions()){ requestRequiredPermissions(); return; }
        try { if(!btAdapter.isEnabled()){setStatus("Bluetooth is off.");return;} } catch(SecurityException e){setStatus("Bluetooth permission required.");return;}
        runProbe();
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
        if(Build.VERSION.SDK_INT>=33) ps.add(Manifest.permission.NEARBY_WIFI_DEVICES); else ps.add(Manifest.permission.ACCESS_FINE_LOCATION);
        requestPermissions(ps.toArray(new String[0]),REQ_PERMISSION);
    }

    @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){
        super.onRequestPermissionsResult(r,p,g);
        if(r==REQ_PERMISSION){if(hasPermissions()) runProbe(); else setStatus("Bluetooth and Nearby Wi-Fi permission are required.");}
    }

    private void runProbe(){
        cleanupRuntime(false);
        report.setLength(0); reportFinished=false; enterWriteAttempted=false; exitWriteAttempted=false; targetPeerMatched=false;
        expectedP2pName=null; credentialPassword=null; failurePending=false; failureReason=null; phase=Phase.IDLE;
        runButton.setEnabled(false);copyButton.setEnabled(false);shareButton.setEnabled(false);
        append("K G1 P2P ASSOCIATION PROBE REPORT");
        append("Generated: "+isoNow());
        append("App version: 0.4.0");
        append("Gate: G4A phone-side Wi-Fi Direct discovery/association only");
        append("BLE writes allowed: P2P enter once + transfer exit once");
        append("Peer selection: EXACT BLE-REPORTED P2P NAME ONLY");
        append("Credential logging/persistence: DISABLED");
        append("Internet permission: ABSENT");
        append("HTTP/socket/media access: NOT IMPLEMENTED");
        append("");
        BluetoothDevice target=findUniqueBondedTarget();
        if(target==null)return;
        registerP2pReceiver();
        append("GATT CONNECTION");
        append("Target selection: exactly one bonded AIMB-G1-family device");
        append("Target name: "+safeName(safeGetName(target)));
        append("Bluetooth address: not logged");
        setStatus("Connecting BLE…"); schedule(()->fail("BLE/P2P setup timeout."),CONNECT_TIMEOUT_MS);
        try{gatt=target.connectGatt(this,false,gattCb,BluetoothDevice.TRANSPORT_LE);}catch(Exception e){fail("BLE connect request failed.");}
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
        enterWriteAttempted=true;phase=Phase.ENTER_SENT;
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
                append("Async 0x73 frame observed (payload not logged in G4A)");
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
                cancelTimeout(); phase=Phase.CONNECTED;
                append("P2P group formed: TRUE");
                append("Phone is group owner: "+info.isGroupOwner);
                append("Group-owner address: "+(info.groupOwnerAddress==null?"unavailable":info.groupOwnerAddress.getHostAddress()));
                append("HTTP/socket requests performed: 0");
                setStatus("P2P association confirmed. No HTTP. Preparing clean exit…");
                schedule(()->sendExit(),POST_CONNECT_OBSERVE_MS);
            });
        }catch(SecurityException e){failWithExit("Permission error requesting P2P connection info.");}
    }

    private void sendExit(){
        cancelTimeout(); if(reportFinished||exitWriteAttempted)return;
        append("");append("EXIT TRANSFER MODE");append("Command: 0x41 / 02 01 09");append("No retry policy: TRUE");
        exitWriteAttempted=true;phase=Phase.EXIT_SENT;
        if(gatt==null||!writeFrame(gatt,frame41(EXIT_TRANSFER))){fail("Exit-transfer write did not start.");return;}
        append("EXIT write start: SUCCESS"); schedule(this::cleanupSuccess,3000);
    }

    private void failWithExit(String msg){
        cancelTimeout();
        append("G4A ERROR: "+msg);
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
            String reason=failureReason==null?"G4A did not complete.":failureReason;
            cleanupRuntime(false);
            append("");append("SUMMARY");append("Exact target peer matched: "+targetPeerMatched);append("HTTP/socket/media operations: 0");append("Credentials logged/persisted: NO");append("RESULT: FAILED — "+reason);append("END REPORT");
            reportFinished=true;phase=Phase.COMPLETE;setStatus(reason);enableActions();
            return;
        }
        finishSuccess();
    }

    private void finishSuccess(){
        cleanupRuntime(false); append("");append("SUMMARY");append("Exact target peer matched: "+targetPeerMatched);append("P2P group formed: TRUE");append("HTTP/socket/media operations: 0");append("Credentials logged/persisted: NO");append("RESULT: PASS");append("END REPORT");
        reportFinished=true;phase=Phase.COMPLETE;setStatus("G4A association probe complete.");enableActions();
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

    private void fail(String m){if(reportFinished)return;cancelTimeout();append("RESULT: "+m);cleanupRuntime(false);append("END REPORT");reportFinished=true;phase=Phase.COMPLETE;setStatus(m);enableActions();}
    private void closeAndFail(String m){handler.post(()->fail(m));}

    private void cleanupRuntime(boolean destroy){
        cancelTimeout();
        expectedP2pName=null; credentialPassword=null;
        if(receiverRegistered&&p2pReceiver!=null){try{unregisterReceiver(p2pReceiver);}catch(Exception ignored){} receiverRegistered=false;p2pReceiver=null;}
        BluetoothGatt x=gatt;gatt=null;writeChar=null;if(x!=null){try{x.disconnect();}catch(Exception ignored){}try{x.close();}catch(Exception ignored){}}
        if(destroy&&p2pChannel!=null){try{p2pChannel.close();}catch(Exception ignored){}}
    }

    private void copy(){ClipboardManager c=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);if(c!=null){c.setPrimaryClip(ClipData.newPlainText("G4A P2P Association Report",report.toString()));Toast.makeText(this,"Report copied",Toast.LENGTH_SHORT).show();}}
    private void share(){Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_SUBJECT,"AIMB-G1 G4A Association Report");i.putExtra(Intent.EXTRA_TEXT,report.toString());startActivity(Intent.createChooser(i,"Share G4A report"));}
    private void enableActions(){copyButton.setEnabled(report.length()>0);shareButton.setEnabled(report.length()>0);runButton.setEnabled(true);reportView.setText(report.toString());}
    private void append(String s){report.append(s).append('\n');reportView.setText(report.toString());}
    private void appendThreadSafe(String s){handler.post(()->{if(!reportFinished)append(s);});}
    private void setStatus(String s){statusView.setText(s);}
    private static boolean matchesTarget(String n){if(n==null)return false;String x=n.trim().toUpperCase(Locale.US);return x.equals(TARGET_NAME)||x.startsWith(TARGET_NAME+"_");}
    private static String safeGetName(BluetoothDevice d){try{return d==null?null:d.getName();}catch(SecurityException e){return null;}}
    private static String safeName(String n){if(n==null)return"<none>";String x=n.trim().toUpperCase(Locale.US);return x.equals(TARGET_NAME)?TARGET_NAME:x.startsWith(TARGET_NAME+"_")?TARGET_NAME+"_<suffix>":"<other>";}
    private static String isoNow(){return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ",Locale.US).format(new Date());}
    private int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}

    @Override protected void onDestroy(){cleanupRuntime(true);super.onDestroy();}
}
