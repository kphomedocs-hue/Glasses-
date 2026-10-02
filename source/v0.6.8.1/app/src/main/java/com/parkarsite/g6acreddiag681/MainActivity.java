package com.parkarsite.g6acreddiag681;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.*;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

@SuppressLint("MissingPermission")
public final class MainActivity extends Activity {
  static final int REQ=6811, MAX_EVENTS=24;
  static final String TARGET="AIMB-G1";
  static final long CONNECT_MS=30000L, COUNT_MS=10000L, NORMAL_CREDENTIAL_WINDOW_MS=10000L,
      LATE_OBSERVE_WINDOW_MS=10000L, EXIT_MS=10000L;
  static final UUID SERVICE=UUID.fromString("de5bf728-d711-4e47-af26-65e3012a5dc7");
  static final UUID NOTIFY=UUID.fromString("de5bf729-d711-4e47-af26-65e3012a5dc7");
  static final UUID WRITE=UUID.fromString("de5bf72a-d711-4e47-af26-65e3012a5dc7");
  static final UUID CCCD=UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
  static final byte[] MEDIA_COUNT=new byte[]{0x02,0x04};
  static final byte[] ENTER_P2P=new byte[]{0x02,0x01,0x04,0x01};
  static final byte[] EXIT_TRANSFER=new byte[]{0x02,0x01,0x09};

  private enum Phase { IDLE, COUNT_SENT, ENTER_SENT, LATE_OBSERVE, EXIT_SENT, COMPLETE }
  private enum ShapeCode { VALID, TOO_SHORT, PAYLOAD_LEN_TOO_SHORT, PREFIX_MISMATCH, NONPOSITIVE_LENGTH, BOUNDS_OVERFLOW }
  private static final class Inv {
    int images,videos,records,config; boolean apOnly;
  }
  private static final class CredentialShape {
    final ShapeCode code; final int ssidLength,passwordLength;
    CredentialShape(ShapeCode c,int s,int p){code=c;ssidLength=s;passwordLength=p;}
    static CredentialShape of(ShapeCode c){return new CredentialShape(c,-1,-1);}
  }

  final Handler h=new Handler(Looper.getMainLooper());
  final StringBuilder report=new StringBuilder();
  BluetoothAdapter adapter; BluetoothGatt gatt; BluetoothGattCharacteristic writeChar;
  Runnable timeout; TextView status,reportView; Button run,copy,share;
  Phase phase=Phase.IDLE;
  boolean finished=false,active=false,destroying=false;
  boolean countWriteAttempted=false,countWriteCb=false,countResponse=false;
  boolean enterWriteAttempted=false,enterWriteCb=false;
  boolean exitWriteAttempted=false,exitWriteCb=false,exitInventoryMatched=false;
  int countWrites=0,enterWrites=0,exitWrites=0;
  int baseImages=-1,baseVideos=-1,baseRecords=-1,baseConfig=-1; boolean baseApOnly=false;
  long enterStart=-1,enterCbAt=-1,validAt=-1;
  boolean normalValidCredential=false,lateValidCredential=false;
  int credentialSsidLength=-1,credentialPasswordLength=-1;
  int validFrames=0,invalidFrames=0,cmd41=0,cmd73=0,cmdOther=0;
  int rTooShort=0,rPayload=0,rPrefix=0,rNonpositive=0,rBounds=0,eventLines=0,suppressed=0;

  @Override public void onCreate(Bundle b){
    super.onCreate(b); buildUi();
    BluetoothManager bm=(BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
    adapter=bm==null?null:bm.getAdapter(); renderIdle();
  }

  void buildUi(){
    int p=dp(20);
    LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(p,p,p,p);
    TextView title=new TextView(this); title.setText("K G1 Credential Handshake Diagnostic v0.6.8.1"); title.setTextSize(23); root.addView(title);
    TextView note=new TextView(this); note.setText("Diagnostic only. NO PHOTO. One inventory query + one P2P-enter command; no Android Wi-Fi Direct discovery/connection, HTTP, catalog, media download, import, or receipt."); note.setPadding(0,dp(8),0,dp(16)); root.addView(note);
    status=new TextView(this); status.setTextSize(16); root.addView(status);
    run=new Button(this); run.setText("Start credential diagnostic"); run.setOnClickListener(v->begin()); root.addView(run);
    LinearLayout row=new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL);
    copy=new Button(this); copy.setText("Copy report"); copy.setEnabled(false); copy.setOnClickListener(v->copyReport()); row.addView(copy);
    share=new Button(this); share.setText("Share report"); share.setEnabled(false); share.setOnClickListener(v->shareReport()); row.addView(share); root.addView(row);
    ScrollView scroll=new ScrollView(this); reportView=new TextView(this); reportView.setTextSize(13); reportView.setTextIsSelectable(true); scroll.addView(reportView);
    root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1)); setContentView(root);
  }

  void renderIdle(){
    if(adapter==null){status.setText("Bluetooth LE unavailable.");run.setEnabled(false);return;}
    status.setText("Ready. Force-stop Cyan Glasses. Do NOT take a photo."); run.setEnabled(true);
  }

  void begin(){
    if(!hasPermission()){requestPermission();return;}
    try{if(!adapter.isEnabled()){status.setText("Bluetooth is off.");return;}}catch(SecurityException e){status.setText("Bluetooth permission required.");return;}
    cleanup(); reset();
    report.setLength(0); finished=false; active=true; phase=Phase.IDLE;
    run.setEnabled(false);copy.setEnabled(false);share.setEnabled(false);
    add("K G1 G6A P2P CREDENTIAL HANDSHAKE DIAGNOSTIC REPORT");
    add("Generated: "+now()); add("App version: 0.6.8.1");
    add("Build commit: "+BuildConfig.BUILD_COMMIT); add("Build run: "+BuildConfig.BUILD_RUN); add("Build attempt: "+BuildConfig.BUILD_ATTEMPT);
    add("Purpose: classify no credential candidate vs rejected 0x41 candidate vs normal valid vs late valid");
    add("Physical captures requested by app: 0"); add("Instruction: DO NOT TAKE A PHOTO during this diagnostic");
    add("Media-count writes allowed: EXACTLY ONE"); add("P2P enter writes allowed: EXACTLY ONE"); add("Transfer-exit writes allowed: EXACTLY ONE");
    add("Normal credential window: "+NORMAL_CREDENTIAL_WINDOW_MS+" ms"); add("Late passive observation window: "+LATE_OBSERVE_WINDOW_MS+" ms");
    add("Android Wi-Fi Direct API operations: 0"); add("HTTP requests: 0"); add("Catalog GET requests: 0"); add("Media-file GET requests: 0");
    add("Credential values decoded/logged/persisted: NO"); add("Bluetooth address logging: NO"); add("Raw notification payload logging: NO");
    add("Remote filename/path logging/persistence: NO"); add("Glasses explicit mutation/deletion command: NOT IMPLEMENTED");
    add(""); add("BONDED TARGET CHECK");
    BluetoothDevice d=target();
    if(d==null)return;
    add("Target name: "+safeName(name(d))); add("Bluetooth address: not logged");
    status.setText("Connecting. Do NOT take a photo.");
    try{gatt=d.connectGatt(this,false,cb,BluetoothDevice.TRANSPORT_LE);}catch(Exception e){fail("GATT connection start failed.");return;}
    schedule(()->fail("GATT connection timeout."),CONNECT_MS);
  }

  BluetoothDevice target(){
    try{
      Set<BluetoothDevice> all=adapter.getBondedDevices(); add("Bonded devices total: "+all.size());
      ArrayList<BluetoothDevice> m=new ArrayList<>(); for(BluetoothDevice d:all)if(matches(name(d)))m.add(d);
      add("AIMB-G1-family bonded matches: "+m.size());
      if(m.size()!=1){fail("Expected exactly one bonded AIMB-G1-family device.");return null;} return m.get(0);
    }catch(SecurityException e){fail("Bluetooth permission error while reading bonded devices.");return null;}
  }

  final BluetoothGattCallback cb=new BluetoothGattCallback(){
    @Override public void onConnectionStateChange(BluetoothGatt x,int s,int state){
      h.post(()->{
        if(finished)return;
        if(s==BluetoothGatt.GATT_SUCCESS&&state==BluetoothProfile.STATE_CONNECTED){
          cancel(); add("");add("GATT CONNECTION");add("GATT: connected");
          try{if(!x.discoverServices())fail("Service discovery did not start.");else schedule(()->fail("Service discovery timeout."),10000L);}catch(Exception e){fail("Service discovery start failed.");}
        }else if(state==BluetoothProfile.STATE_DISCONNECTED){
          if(phase==Phase.EXIT_SENT){add("BLE disconnected during exit confirmation.");finishDiagnostic();}
          else if(phase!=Phase.COMPLETE)fail("BLE disconnected before diagnostic completion.");
        }
      });
    }
    @Override public void onServicesDiscovered(BluetoothGatt x,int s){
      h.post(()->{
        if(finished)return; cancel();
        if(s!=BluetoothGatt.GATT_SUCCESS){fail("Service discovery failed.");return;}
        BluetoothGattService svc=x.getService(SERVICE); if(svc==null){fail("Cyan service not found.");return;}
        BluetoothGattCharacteristic n=svc.getCharacteristic(NOTIFY),w=svc.getCharacteristic(WRITE);
        if(n==null||w==null){fail("Cyan notify/write characteristic missing.");return;} writeChar=w; add("CYAN SERVICE/NOTIFY/WRITE: PRESENT");
        try{
          if(!x.setCharacteristicNotification(n,true)){fail("Local notification registration failed.");return;}
          BluetoothGattDescriptor d=n.getDescriptor(CCCD); if(d==null){fail("CCCD not found.");return;}
          d.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
          if(!x.writeDescriptor(d)){fail("CCCD write did not start.");return;} add("Notification subscription start: SUCCESS");
        }catch(Exception e){fail("Notification subscription error.");}
      });
    }
    @Override public void onDescriptorWrite(BluetoothGatt x,BluetoothGattDescriptor d,int s){
      h.post(()->{if(finished)return;if(s!=BluetoothGatt.GATT_SUCCESS){fail("CCCD write failed: "+s);return;}add("Notification subscription: SUCCESS");sendCount(x);});
    }
    @Override public void onCharacteristicWrite(BluetoothGatt x,BluetoothGattCharacteristic c,int s){
      h.post(()->{
        if(finished)return; add("BLE characteristic write status: "+(s==BluetoothGatt.GATT_SUCCESS?"SUCCESS":s));
        if(s!=BluetoothGatt.GATT_SUCCESS){if(phase==Phase.EXIT_SENT){add("Exit write callback: FAILED");finishDiagnostic();}else fail("BLE write callback failed.");return;}
        if(phase==Phase.COUNT_SENT){countWriteCb=true;advanceCount();}
        else if((phase==Phase.ENTER_SENT||phase==Phase.LATE_OBSERVE)&&enterWriteAttempted&&!enterWriteCb){enterWriteCb=true;enterCbAt=SystemClock.elapsedRealtime();add("P2P enter write callback: SUCCESS");if(phase==Phase.ENTER_SENT)advanceEnter();else if(normalValidCredential){cancel();sendExit();}}
        else if(phase==Phase.EXIT_SENT){exitWriteCb=true;add("Exit write callback: SUCCESS");add("Post-exit confirmation target: valid 0x73/0x01 inventory matching baseline");}
      });
    }
    @Override public void onCharacteristicChanged(BluetoothGatt x,BluetoothGattCharacteristic c,byte[] v){notifyFrame(v);}
    @SuppressWarnings("deprecation") @Override public void onCharacteristicChanged(BluetoothGatt x,BluetoothGattCharacteristic c){notifyFrame(c.getValue());}
  };

  void sendCount(BluetoothGatt x){
    cancel(); if(countWriteAttempted)return;
    add("");add("CYAN MEDIA INVENTORY REFRESH");add("Command: 0x41 / 02 04");add("No retry policy: TRUE");
    countWriteAttempted=true;countWrites++;phase=Phase.COUNT_SENT;
    if(!write(x,frame41(MEDIA_COUNT))){fail("Media-count write did not start.");return;} add("Media-count write start: SUCCESS");
    schedule(()->fail("No valid media-count response received."),COUNT_MS);
  }

  void advanceCount(){
    if(finished||phase!=Phase.COUNT_SENT||!countWriteCb||!countResponse)return;
    BluetoothGatt x=gatt;if(x==null){fail("GATT unavailable after media-count handshake.");return;}
    cancel();add("Media-count write/response handshake: COMPLETE");sendEnter(x);
  }

  void sendEnter(BluetoothGatt x){
    cancel(); if(enterWriteAttempted)return;
    add("");add("ENTER P2P MODE — CREDENTIAL OBSERVATION ONLY");add("Command: 0x41 / 02 01 04 01");add("No retry policy: TRUE");
    enterWriteAttempted=true;enterWrites++;phase=Phase.ENTER_SENT;enterStart=SystemClock.elapsedRealtime();
    if(!write(x,frame41(ENTER_P2P))){fail("P2P enter write did not start.");return;} add("ENTER write start: SUCCESS");add("Normal credential observation window started: 10000 ms");
    status.setText("Observing credential notifications. DO NOT take a photo."); scheduleNormalCredentialTimeout();
  }

  void scheduleNormalCredentialTimeout(){
    schedule(()->{
      if(finished||phase!=Phase.ENTER_SENT)return;
      add("Normal 10-second credential window ended without an accepted credential frame.");
      add("Normal-window interim counters: validFrames="+validFrames+", invalidFrames="+invalidFrames+", cmd41="+cmd41+", cmd73="+cmd73+", other="+cmdOther);
      phase=Phase.LATE_OBSERVE;add("Late passive observation window started: 10000 ms; no second ENTER write will be sent.");
      status.setText("Normal window expired. Passively observing for a late credential frame."); scheduleLateCredentialTimeout();
    },NORMAL_CREDENTIAL_WINDOW_MS);
  }

  void scheduleLateCredentialTimeout(){
    schedule(()->{if(finished||phase!=Phase.LATE_OBSERVE)return;add("Late passive observation window ended without an accepted credential frame.");sendExit();},LATE_OBSERVE_WINDOW_MS);
  }

  void advanceEnter(){
    if(finished||phase!=Phase.ENTER_SENT||!enterWriteCb||!normalValidCredential)return;
    cancel();add("P2P enter write + credential handshake: COMPLETE WITHIN NORMAL WINDOW");sendExit();
  }

  void notifyFrame(byte[] v){
    if(v==null||finished)return; final byte[] data=v.clone();
    h.post(()->{
      if(finished)return; boolean observing=phase==Phase.ENTER_SENT||phase==Phase.LATE_OBSERVE;
      if(!validFrame(data)){if(observing){invalidFrames++;event("Enter-observation notification classification: INVALID_FRAME");}return;}
      int cmd=data[1]&255;
      if(phase==Phase.COUNT_SENT&&cmd==0x41){
        Inv i=parseCount(data); if(i!=null){
          baseImages=i.images;baseVideos=i.videos;baseRecords=i.records;baseConfig=i.config;baseApOnly=i.apOnly;
          add("Media-count response: VALID");add("Image count: "+baseImages);add("Video count: "+baseVideos);add("Recording count: "+baseRecords);
          add("Config file type: "+baseConfig);add("Only-support-AP-import: "+baseApOnly);
          if(baseConfig!=1||baseApOnly){fail("Inventory no longer selects confirmed configFileType=1 P2P branch.");return;}
          countResponse=true;advanceCount();
        } return;
      }
      if(observing){
        validFrames++;
        if(cmd==0x41){
          cmd41++; CredentialShape sh=classifyCredentialFrame(data); record(sh);
          long e=enterStart<0?-1:SystemClock.elapsedRealtime()-enterStart; event("Enter-observation 0x41 classification: "+sh.code+" at +"+e+" ms");
          if(sh.code==ShapeCode.VALID){
            credentialSsidLength=sh.ssidLength;credentialPasswordLength=sh.passwordLength;validAt=e;
            add("Structurally valid credential frame observed.");add("SSID length: "+credentialSsidLength);add("Password length: "+credentialPasswordLength);add("SSID/password values: not decoded or logged");
            if(phase==Phase.ENTER_SENT){normalValidCredential=true;advanceEnter();}
            else{lateValidCredential=true;cancel();add("Credential classification timing: VALID ONLY IN LATE WINDOW");sendExit();}
          }
        }else if(cmd==0x73){cmd73++;event("Enter-observation valid frame command: 0x73");}
        else{cmdOther++;event(String.format(Locale.US,"Enter-observation valid frame command: 0x%02X",cmd));}
        return;
      }
      if(phase==Phase.EXIT_SENT){
        if(cmd==0x41){add("Post-exit 0x41 frame observed: IGNORED for exit confirmation");return;}
        if(cmd==0x73&&data.length>=7&&(data[6]&255)==0x01){
          Inv i=parse73(data); if(i!=null){boolean m=matchesBase(i);add("Post-exit 0x73/0x01 inventory observed: images="+i.images+", videos="+i.videos+", recordings="+i.records);add("Post-exit inventory matches baseline: "+m);if(m){exitInventoryMatched=true;cancel();finishDiagnostic();}}
        }
      }
    });
  }

  CredentialShape classifyCredentialFrame(byte[] f){
    if(f==null||f.length<14)return CredentialShape.of(ShapeCode.TOO_SHORT);
    int len=(f[2]&255)|((f[3]&255)<<8);if(len<8)return CredentialShape.of(ShapeCode.PAYLOAD_LEN_TOO_SHORT);
    int p=6;if((f[p]&255)!=2||(f[p+1]&255)!=1||(f[p+2]&255)!=4||(f[p+3]&255)!=1)return CredentialShape.of(ShapeCode.PREFIX_MISMATCH);
    int sl=(f[p+4]&255)|((f[p+5]&255)<<8),pl=(f[p+6]&255)|((f[p+7]&255)<<8),off=p+8;
    if(sl<=0||pl<=0)return CredentialShape.of(ShapeCode.NONPOSITIVE_LENGTH);
    if(off+sl+pl>f.length)return CredentialShape.of(ShapeCode.BOUNDS_OVERFLOW);
    return new CredentialShape(ShapeCode.VALID,sl,pl);
  }

  void record(CredentialShape s){
    if(s.code==ShapeCode.TOO_SHORT)rTooShort++;
    else if(s.code==ShapeCode.PAYLOAD_LEN_TOO_SHORT)rPayload++;
    else if(s.code==ShapeCode.PREFIX_MISMATCH)rPrefix++;
    else if(s.code==ShapeCode.NONPOSITIVE_LENGTH)rNonpositive++;
    else if(s.code==ShapeCode.BOUNDS_OVERFLOW)rBounds++;
  }

  Inv parseCount(byte[] f){
    if(f==null||f.length<16)return null;int len=(f[2]&255)|((f[3]&255)<<8);if(len<10)return null;
    int p=6;if((f[p]&255)!=2||(f[p+1]&255)!=4)return null;Inv i=new Inv();
    i.images=le16(f,p+2);i.videos=le16(f,p+4);i.records=le16(f,p+6);i.config=f[p+8]&255;i.apOnly=(f[p+9]&255)!=0;return i;
  }
  Inv parse73(byte[] f){
    if(f==null||f.length<14||(f[1]&255)!=0x73||(f[6]&255)!=1)return null;Inv i=new Inv();
    i.images=le16(f,7);i.videos=le16(f,9);i.records=le16(f,11);i.config=f[13]&255;i.apOnly=f.length>=15&&(f[14]&255)!=0;return i;
  }
  static int le16(byte[] b,int i){return (b[i]&255)|((b[i+1]&255)<<8);}
  boolean matchesBase(Inv i){return i.images==baseImages&&i.videos==baseVideos&&i.records==baseRecords&&i.config==baseConfig&&i.apOnly==baseApOnly;}

  void sendExit(){
    cancel();if(finished||exitWriteAttempted)return;
    add("");add("EXIT TRANSFER MODE");add("Command: 0x41 / 02 01 09");add("No retry policy: TRUE");
    exitWriteAttempted=true;exitWrites++;phase=Phase.EXIT_SENT;BluetoothGatt x=gatt;
    if(x==null||!write(x,frame41(EXIT_TRANSFER))){add("EXIT write start: FAILED");finishDiagnostic();return;}
    add("EXIT write start: SUCCESS");schedule(()->{add("Post-exit matching 0x73/0x01 observed: false");finishDiagnostic();},EXIT_MS);
  }

  void finishDiagnostic(){
    if(finished)return;cancel();add("");add("DIAGNOSTIC SUMMARY");
    add("Media-count writes: "+countWrites);add("P2P enter writes: "+enterWrites);add("Transfer-exit writes: "+exitWrites);
    add("Enter write callback observed: "+enterWriteCb);add("Valid framed notifications during credential observation: "+validFrames);add("Invalid framed notifications during credential observation: "+invalidFrames);
    add("Valid 0x41 frames during credential observation: "+cmd41);add("Valid 0x73 frames during credential observation: "+cmd73);add("Other valid command frames during credential observation: "+cmdOther);
    add("Rejected credential shapes — TOO_SHORT: "+rTooShort);add("Rejected credential shapes — PAYLOAD_LEN_TOO_SHORT: "+rPayload);add("Rejected credential shapes — PREFIX_MISMATCH: "+rPrefix);
    add("Rejected credential shapes — NONPOSITIVE_LENGTH: "+rNonpositive);add("Rejected credential shapes — BOUNDS_OVERFLOW: "+rBounds);add("Suppressed per-event lines after report cap: "+suppressed);
    add("Normal-window valid credential: "+normalValidCredential);add("Late-window valid credential: "+lateValidCredential);add("Valid credential elapsed from ENTER write start: "+validAt+" ms");
    add("ENTER write callback elapsed from ENTER write start: "+(enterStart>=0&&enterCbAt>=0?enterCbAt-enterStart:-1)+" ms");
    add("Credential values decoded/logged/persisted: NO");add("Raw notification payload logged: NO");
    add("Android Wi-Fi Direct API operations: 0");add("HTTP requests: 0");add("Catalog GET requests: 0");add("Media-file GET requests: 0");
    add("Exit write callback observed: "+exitWriteCb);add("Post-exit matching baseline inventory observed: "+exitInventoryMatched);
    String c=classification();add("G6A CREDENTIAL HANDSHAKE DIAGNOSTIC CLASSIFICATION: "+c);add("Gate effect: DIAGNOSTIC ONLY — DOES NOT CLOSE G6A OR UNBLOCK G6B");add("END REPORT");
    finished=true;active=false;phase=Phase.COMPLETE;cleanup();
    if(!destroying){status.setText("Diagnostic complete: "+c+". Copy the full report.");copy.setEnabled(true);share.setEnabled(true);run.setEnabled(false);}
  }

  String classification(){
    if(normalValidCredential)return "NORMAL_VALID — structurally valid credential frame accepted within original 10-second window";
    if(lateValidCredential)return "LATE_VALID — no accepted credential by 10 seconds; structurally valid credential frame arrived during bounded late window";
    int r=rTooShort+rPayload+rPrefix+rNonpositive+rBounds;
    if(cmd41>0&&r>0)return "REJECTED_0x41_CANDIDATE_ACTIVITY — valid 0x41 frame activity occurred but none matched the credential structure";
    if(invalidFrames>0&&cmd41==0)return "INVALID_FRAME_ACTIVITY_ONLY — notification activity occurred but no valid 0x41 credential candidate was observed";
    if(cmd41==0)return "NO_0x41_CREDENTIAL_CANDIDATE — no valid 0x41 frame was observed across the normal + late windows";
    return "UNCLASSIFIED_NO_VALID_CREDENTIAL";
  }

  void fail(String why){
    if(finished)return;cancel();add("");add("DIAGNOSTIC ERROR: "+why);
    if((phase==Phase.ENTER_SENT||phase==Phase.LATE_OBSERVE)&&!exitWriteAttempted){sendExit();return;}
    add("G6A CREDENTIAL HANDSHAKE DIAGNOSTIC CLASSIFICATION: PRECONDITION_OR_TRANSPORT_FAILURE");add("END REPORT");
    finished=true;active=false;phase=Phase.COMPLETE;cleanup();
    if(!destroying){status.setText("Diagnostic failed before classification. Copy the report.");copy.setEnabled(true);share.setEnabled(true);run.setEnabled(false);}
  }

  boolean hasPermission(){return Build.VERSION.SDK_INT<Build.VERSION_CODES.S||checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)==PackageManager.PERMISSION_GRANTED;}
  void requestPermission(){if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.S)requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT},REQ);}
  @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){super.onRequestPermissionsResult(r,p,g);if(r==REQ)renderIdle();}

  boolean write(BluetoothGatt x,byte[] f){if(writeChar==null||!validFrame(f))return false;writeChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);writeChar.setValue(f);try{return x.writeCharacteristic(writeChar);}catch(Exception e){return false;}}
  static byte[] frame41(byte[] p){int crc=crc16(p);byte[] f=new byte[p.length+6];f[0]=(byte)0xBC;f[1]=0x41;f[2]=(byte)p.length;f[3]=0;f[4]=(byte)(crc&255);f[5]=(byte)((crc>>>8)&255);System.arraycopy(p,0,f,6,p.length);return f;}
  static int crc16(byte[] d){int c=0xffff;for(byte b:d){c^=b&255;for(int i=0;i<8;i++)c=(c&1)!=0?(c>>>1)^0xA001:c>>>1;}return c&0xffff;}
  static boolean validFrame(byte[] f){if(f==null||f.length<6||(f[0]&255)!=0xBC)return false;int l=(f[2]&255)|((f[3]&255)<<8);if(f.length!=l+6)return false;byte[] p=Arrays.copyOfRange(f,6,f.length);int e=(f[4]&255)|((f[5]&255)<<8);return crc16(p)==e;}

  void schedule(Runnable r,long ms){cancel();timeout=r;h.postDelayed(r,ms);} void cancel(){if(timeout!=null){h.removeCallbacks(timeout);timeout=null;}}
  void event(String s){if(eventLines<MAX_EVENTS){add(s);eventLines++;}else suppressed++;}
  void reset(){
    phase=Phase.IDLE;countWriteAttempted=countWriteCb=countResponse=enterWriteAttempted=enterWriteCb=exitWriteAttempted=exitWriteCb=exitInventoryMatched=false;
    countWrites=enterWrites=exitWrites=0;baseImages=baseVideos=baseRecords=baseConfig=-1;baseApOnly=false;enterStart=enterCbAt=validAt=-1;normalValidCredential=lateValidCredential=false;
    credentialSsidLength=credentialPasswordLength=-1;validFrames=invalidFrames=cmd41=cmd73=cmdOther=rTooShort=rPayload=rPrefix=rNonpositive=rBounds=eventLines=suppressed=0;
  }
  void cleanup(){cancel();BluetoothGatt x=gatt;gatt=null;writeChar=null;if(x!=null){try{x.disconnect();}catch(Exception ignored){}try{x.close();}catch(Exception ignored){}}}
  void copyReport(){ClipboardManager c=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);if(c!=null){c.setPrimaryClip(ClipData.newPlainText("G6A credential handshake diagnostic",report.toString()));Toast.makeText(this,"Report copied",Toast.LENGTH_SHORT).show();}}
  void shareReport(){Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_SUBJECT,"AIMB-G1 G6A credential handshake diagnostic");i.putExtra(Intent.EXTRA_TEXT,report.toString());startActivity(Intent.createChooser(i,"Share report"));}
  void add(String s){report.append(s).append('\n');reportView.setText(report.toString());}
  static boolean matches(String n){if(n==null)return false;String x=n.trim().toUpperCase(Locale.US);return x.equals(TARGET)||x.startsWith(TARGET+"_");}
  static String name(BluetoothDevice d){try{return d==null?null:d.getName();}catch(SecurityException e){return null;}}
  static String safeName(String n){if(n==null)return "<none>";String x=n.trim().toUpperCase(Locale.US);return x.equals(TARGET)?TARGET:x.startsWith(TARGET+"_")?TARGET+"_<suffix>":"<other>";}
  static String now(){return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ",Locale.US).format(new Date());}
  int dp(int v){return Math.round(v*getResources().getDisplayMetrics().density);}

  @Override protected void onStop(){super.onStop();if(active&&!finished&&!isChangingConfigurations())fail("App left foreground during controlled credential diagnostic.");}
  @Override protected void onDestroy(){destroying=true;if(active&&!finished)fail("Activity destroyed during controlled credential diagnostic.");else cleanup();super.onDestroy();}
}
