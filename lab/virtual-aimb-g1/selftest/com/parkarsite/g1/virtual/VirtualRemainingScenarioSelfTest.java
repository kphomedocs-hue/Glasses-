package com.parkarsite.g1.virtual;

import com.parkarsite.g6a.recovery.RecoveryImportStore;
import java.nio.file.*;
import java.time.*;
import java.util.*;

/**
 * 72 synthetic decision-model assertions VGL-04..12. No Android emulator and no
 * glasses; real RecoveryImportStore is invoked in VGL-09. Neither these fixtures
 * nor permissive synthetic HTTP results authorize any real media GET.
 */
public final class VirtualRemainingScenarioSelfTest {
  interface Test { void run() throws Exception; }
  static int passed;
  static void ok(boolean result){if(!result)throw new AssertionError("offline policy invariant");}
  static void run(String id,Test test) throws Exception {
    try {test.run();passed++;System.out.println(id+" OFFLINE_ASSERTIONS_PASS");}
    catch(Exception|AssertionError ex){System.err.println(id+" FAIL");throw ex;}
  }
  static VirtualLabPolicies.Catalog.Result cat(String text){
    return VirtualLabPolicies.Catalog.parse(1,VirtualLabPolicies.Catalog.bytes(text));
  }
  static byte[] image(){return new byte[]{(byte)255,(byte)216,(byte)255,1,2,(byte)255,(byte)217};}
  static String id(int n){return String.format("%064x",n);}
  static RecoveryImportStore store(Path root)throws Exception{return new RecoveryImportStore(root,ZoneId.of("Asia/Kolkata"));}
  static Path tmp() throws Exception{return Files.createTempDirectory("vgl-recovery-");}
  static RecoveryImportStore.Source jpegSource(){return out->out.write(image());}
  static RecoveryImportStore.FailureInjector fail(RecoveryImportStore.Barrier at){
    return current->{if(current==at)throw new java.io.IOException("synthetic crash");};
  }
  static RecoveryImportStore.FailureInjector never(){return current->{};}
  static RecoveryImportStore.Disposition recovered(RecoveryImportStore s)throws Exception {
    List<RecoveryImportStore.RecoveryResult> list=s.recover();
    ok(list.size()==1);return list.get(0).disposition();
  }
  static void expectBad(Test t){
    try{t.run();throw new AssertionError("expected rejection");}
    catch(IllegalArgumentException expected){}catch(Exception e){throw new AssertionError(e);}
  }
  static byte[] atom(String type,byte[] body){
    byte[] o=new byte[8+body.length];int l=o.length;
    o[0]=(byte)(l>>>24);o[1]=(byte)(l>>>16);o[2]=(byte)(l>>>8);o[3]=(byte)l;
    for(int i=0;i<4;i++)o[4+i]=(byte)type.charAt(i);
    System.arraycopy(body,0,o,8,body.length);return o;
  }
  static byte[] concat(byte[] a,byte[] b){byte[] o=Arrays.copyOf(a,a.length+b.length);System.arraycopy(b,0,o,a.length,b.length);return o;}
  static byte[] opus(){byte[] o=new byte[40];System.arraycopy("OggS".getBytes(),0,o,0,4);System.arraycopy("OpusHead".getBytes(),0,o,28,8);return o;}
  static VirtualLabPolicies.OneShot begun(){
    var o=new VirtualLabPolicies.OneShot();
    ok(o.start(true,true,"GET","media/a.jpg",false)==VirtualLabPolicies.Decision.ALLOW);return o;
  }
  public static void main(String[] args)throws Exception {
    // Group teardown/virtual deadlines; no WifiP2pManager or actual connect path.
    run("VGL-04-01",()->{var g=new VirtualLabPolicies.Group(new VirtualClock());g.preflight(true,true);ok(g.state()==VirtualLabPolicies.Group.State.ABORTED&&g.removals()==0);});
    run("VGL-04-02",()->{var g=new VirtualLabPolicies.Group(new VirtualClock());g.preflight(false,true);g.peers(1);g.formed(true,true);ok(g.mayCatalog()&&!g.mayConnectRealPeer());});
    run("VGL-04-03",()->{var c=new VirtualClock();var g=new VirtualLabPolicies.Group(c);g.preflight(false,true);g.peers(0);c.advance(16_001);ok(g.state()==VirtualLabPolicies.Group.State.CLEANUP_PENDING);});
    run("VGL-04-04",()->{var g=new VirtualLabPolicies.Group(new VirtualClock());g.preflight(false,true);g.peers(1);g.formed(false,true);ok(!g.mayCatalog()&&g.removals()==1);});
    run("VGL-04-05",()->{var g=new VirtualLabPolicies.Group(new VirtualClock());g.preflight(false,true);g.cleanup();g.cleanupCallback(VirtualLabPolicies.Group.CleanupStatus.BUSY,false);ok(g.state()==VirtualLabPolicies.Group.State.UNCERTAIN);});
    run("VGL-04-06",()->{var c=new VirtualClock();final int[] ble={0};c.deadline("ble",5_000,()->ble[0]++);var g=new VirtualLabPolicies.Group(c);g.preflight(false,true);c.advance(5_001);ok(ble[0]==1&&c.scheduled("group-discovery"));});
    run("VGL-04-07",()->{var g=new VirtualLabPolicies.Group(new VirtualClock());g.preflight(false,true);g.peers(1);g.activityDestroyed();ok(g.state()==VirtualLabPolicies.Group.State.CLEANUP_PENDING&&g.removals()==1);});
    run("VGL-04-08",()->{var g=new VirtualLabPolicies.Group(new VirtualClock());g.preflight(false,true);g.cleanup();g.cleanupCallback(VirtualLabPolicies.Group.CleanupStatus.SUCCESS,true);g.cleanupCallback(VirtualLabPolicies.Group.CleanupStatus.FAILURE,false);g.cleanup();ok(g.state()==VirtualLabPolicies.Group.State.CLEAN&&g.removals()==1);});

    // Only synthetic catalog shape is documented. Real type2 firmware schema is UNKNOWN.
    run("VGL-05-01",()->{var r=cat("media/a.jpg\nmedia/b.opus\nmedia/c.mp4\n");ok(r.entries().size()==3&&r.count(VirtualLabPolicies.MediaKind.JPG)==1&&r.count(VirtualLabPolicies.MediaKind.OPUS)==1);});
    run("VGL-05-02",()->{String raw="{\"files\":[{\"path\":\"media/a.jpg\"}]}";ok(VirtualLabPolicies.Catalog.parse(2,VirtualLabPolicies.Catalog.bytes(raw)).entries().size()==1);expectBad(()->VirtualLabPolicies.Catalog.parse(1,VirtualLabPolicies.Catalog.bytes(raw)));});
    run("VGL-05-03",()->{expectBad(()->VirtualLabPolicies.Catalog.parse(1,new byte[]{(byte)0xc3,(byte)0x28}));});
    run("VGL-05-04",()->{var r=cat("\nmedia/a.jpg\n\n");ok(r.entries().size()==1&&r.blanks()==3);});
    run("VGL-05-05",()->{for(String s:List.of("../a.jpg","/media/a.jpg","https://bad/a.jpg","media//a.jpg"))expectBad(()->cat(s));});
    run("VGL-05-06",()->{var r=cat("media/a.jpg\nmedia/a.jpg");ok(r.entries().size()==1&&r.duplicates()==1);ok(new VirtualLabPolicies.Capture(cat("")).inspect(r,null)==VirtualLabPolicies.Decision.AMBIGUOUS);});
    run("VGL-05-07",()->{byte[] oversized=new byte[VirtualLabPolicies.CATALOG_CAP+1];expectBad(()->VirtualLabPolicies.Catalog.parse(1,oversized));});
    run("VGL-05-08",()->{var r=cat("private_project_site/client_x.jpg");String summary="jpg="+r.count(VirtualLabPolicies.MediaKind.JPG);ok(!summary.contains("client_x")&&summary.equals("jpg=1"));});

    // Set-delta-only capture decisions, with bounded deadline handled by shared clock.
    run("VGL-06-01",()->{var c=new VirtualLabPolicies.Capture(cat("media/old.jpg"));ok(c.inspect(cat("media/old.jpg\nmedia/new.jpg"),2)==VirtualLabPolicies.Decision.ALLOW);});
    run("VGL-06-02",()->{var v=new VirtualClock();var c=new VirtualLabPolicies.Capture(cat("media/old.jpg"));final boolean[] timeout={false};v.deadline("capture",10_000,()->timeout[0]=true);ok(c.inspect(cat("media/old.jpg"),null)==VirtualLabPolicies.Decision.EXPIRED);v.advance(1000);ok(c.inspect(cat("media/old.jpg\nmedia/later.jpg"),2)==VirtualLabPolicies.Decision.ALLOW&&!timeout[0]);v.cancel("capture");});
    run("VGL-06-03",()->{var c=new VirtualLabPolicies.Capture(cat(""));var t=new VirtualClock();t.deadline("capture",5,()->{});t.advance(6);ok(c.inspect(cat(""),null)==VirtualLabPolicies.Decision.EXPIRED);});
    run("VGL-06-04",()->{var c=new VirtualLabPolicies.Capture(cat(""));ok(c.inspect(cat("media/a.jpg\nmedia/b.jpg"),null)==VirtualLabPolicies.Decision.AMBIGUOUS);});
    run("VGL-06-05",()->{var c=new VirtualLabPolicies.Capture(cat(""));ok(c.inspect(cat("media/n.opus"),null)==VirtualLabPolicies.Decision.BLOCK);});
    run("VGL-06-06",()->{var c=new VirtualLabPolicies.Capture(cat("media/z.jpg\nmedia/a.jpg"));ok(c.inspect(cat("media/a.jpg\nmedia/new.jpg\nmedia/z.jpg"),3)==VirtualLabPolicies.Decision.ALLOW&&c.selected().equals("media/new.jpg"));});
    run("VGL-06-07",()->{var c=new VirtualLabPolicies.Capture(cat("media/old.jpg"));var after=cat("media/old.jpg\nmedia/new.jpg");ok(c.inspect(after,2)==VirtualLabPolicies.Decision.ALLOW&&c.inspect(after,2)==VirtualLabPolicies.Decision.ALLOW);});
    run("VGL-06-08",()->{var c=new VirtualLabPolicies.Capture(cat(""));ok(c.inspect(cat("media/only.jpg"),2)==VirtualLabPolicies.Decision.BLOCK);});

    // These are scripted response observations only; no socket and no production GET.
    run("VGL-07-01",()->{var o=begun();ok(o.observe(200,image().length,image(),false,true,true)==VirtualLabPolicies.Decision.ALLOW&&o.committed());});
    run("VGL-07-02",()->{var o=begun();ok(o.observe(500,7,image(),false,true,false)==VirtualLabPolicies.Decision.REMOTE_UNCERTAIN&&!o.committed());});
    run("VGL-07-03",()->{var o=begun();ok(o.observe(200,6,image(),false,true,true)==VirtualLabPolicies.Decision.REMOTE_UNCERTAIN);});
    run("VGL-07-04",()->{var o=begun();ok(o.observe(200,VirtualLabPolicies.MEDIA_CAP+1,image(),false,true,true)==VirtualLabPolicies.Decision.REMOTE_UNCERTAIN);});
    run("VGL-07-05",()->{var o=begun();ok(o.observe(200,7,Arrays.copyOf(image(),4),false,false,false)==VirtualLabPolicies.Decision.REMOTE_UNCERTAIN&&o.retry()==VirtualLabPolicies.Decision.BLOCK);});
    run("VGL-07-06",()->{var t=new VirtualClock();var o=begun();final boolean[] fired={false};t.deadline("http",3000,()->fired[0]=true);t.advance(3001);ok(fired[0]&&o.restart()==VirtualLabPolicies.Decision.REMOTE_UNCERTAIN&&!o.committed());});
    run("VGL-07-07",()->{var o=begun();o.observe(200,7,image(),false,true,true);ok(o.restart()==VirtualLabPolicies.Decision.ALLOW&&o.retry()==VirtualLabPolicies.Decision.BLOCK);});
    run("VGL-07-08",()->{var o=begun();o.observe(200,7,image(),false,true,true);ok(o.committed());var noReceipt=begun();noReceipt.observe(200,7,image(),false,true,false);ok(noReceipt.restart()==VirtualLabPolicies.Decision.REMOTE_UNCERTAIN);});

    // Media shape checks are NOT codec decoding/fidelity attestations.
    run("VGL-08-01",()->{ok(VirtualLabPolicies.Media.validate(VirtualLabPolicies.MediaKind.JPG,image())==VirtualLabPolicies.Media.Fidelity.SHAPE_ONLY);});
    run("VGL-08-02",()->{ok(VirtualLabPolicies.Media.validate(VirtualLabPolicies.MediaKind.JPG,new byte[]{(byte)255,(byte)216,(byte)255,0,(byte)255,(byte)217})!=VirtualLabPolicies.Media.Fidelity.DECODER_VERIFIED);});
    run("VGL-08-03",()->{var mp4=concat(atom("ftyp",new byte[]{0,0,0,0}),atom("mdat",new byte[]{1}));ok(VirtualLabPolicies.Media.validate(VirtualLabPolicies.MediaKind.MP4,mp4)==VirtualLabPolicies.Media.Fidelity.CONTAINER_ONLY);});
    run("VGL-08-04",()->{var bad=atom("ftyp",new byte[0]);bad[0]=127;ok(VirtualLabPolicies.Media.validate(VirtualLabPolicies.MediaKind.MP4,bad)==VirtualLabPolicies.Media.Fidelity.INVALID);});
    run("VGL-08-05",()->{ok(VirtualLabPolicies.Media.validate(VirtualLabPolicies.MediaKind.OPUS,opus())==VirtualLabPolicies.Media.Fidelity.CONTAINER_ONLY);});
    run("VGL-08-06",()->{ok(VirtualLabPolicies.Media.validate(VirtualLabPolicies.MediaKind.JPG,opus())==VirtualLabPolicies.Media.Fidelity.INVALID);});
    run("VGL-08-07",()->{ok(VirtualLabPolicies.MEDIA_CAP==33_554_432L&&VirtualLabPolicies.Media.validate(VirtualLabPolicies.MediaKind.MP4,new byte[1])==VirtualLabPolicies.Media.Fidelity.INVALID);});
    run("VGL-08-08",()->{ok(VirtualLabPolicies.Media.validate(VirtualLabPolicies.MediaKind.JPG,new byte[0])==VirtualLabPolicies.Media.Fidelity.INVALID&&begun().retry()==VirtualLabPolicies.Decision.BLOCK);});

    // Calls the real recovery source module rather than a copied mock.
    run("VGL-09-01",()->{var s=store(tmp());s.prepare(id(901),0);ok(recovered(s)==RecoveryImportStore.Disposition.NOT_FETCHED);});
    run("VGL-09-02",()->{var s=store(tmp());var t=s.prepare(id(902),0);final int[] calls={0};try{s.execute(t,7,out->{calls[0]++;jpegSource().streamTo(out);},fail(RecoveryImportStore.Barrier.AFTER_FETCH_MARK));}catch(java.io.IOException expected){}ok(calls[0]==0&&recovered(s)==RecoveryImportStore.Disposition.REMOTE_UNCERTAIN);});
    run("VGL-09-03",()->{var s=store(tmp());var t=s.prepare(id(903),0);try{s.execute(t,7,jpegSource(),fail(RecoveryImportStore.Barrier.AFTER_PART_SYNC));}catch(java.io.IOException expected){}ok(recovered(s)==RecoveryImportStore.Disposition.REMOTE_UNCERTAIN);});
    run("VGL-09-04",()->{var s=store(tmp());var t=s.prepare(id(904),0);try{s.execute(t,8,jpegSource(),never());}catch(java.io.IOException expected){}ok(recovered(s)==RecoveryImportStore.Disposition.REMOTE_UNCERTAIN);});
    run("VGL-09-05",()->{var s=store(tmp());var t=s.prepare(id(905),0);try{s.execute(t,7,jpegSource(),fail(RecoveryImportStore.Barrier.AFTER_VERIFIED_JOURNAL));}catch(java.io.IOException expected){}ok(recovered(s)==RecoveryImportStore.Disposition.LOCAL_RECOVERED&&s.isCommittedAndIntact(id(905)));});
    run("VGL-09-06",()->{var s=store(tmp());var t=s.prepare(id(906),0);try{s.execute(t,7,jpegSource(),fail(RecoveryImportStore.Barrier.AFTER_FINAL_MOVE));}catch(java.io.IOException expected){}ok(recovered(s)==RecoveryImportStore.Disposition.LOCAL_RECOVERED&&s.isCommittedAndIntact(id(906)));});
    run("VGL-09-07",()->{var s=store(tmp());var t=s.prepare(id(907),0);try{s.execute(t,7,jpegSource(),fail(RecoveryImportStore.Barrier.AFTER_RECEIPT));}catch(java.io.IOException expected){}ok(recovered(s)==RecoveryImportStore.Disposition.LOCAL_RECOVERED&&s.isCommittedAndIntact(id(907)));});
    run("VGL-09-08",()->{var dir=tmp();var s=store(dir);var t=s.prepare(id(908),0);s.execute(t,7,jpegSource(),never());Files.write(dir.resolve(t.relativePath()),new byte[]{2,3},StandardOpenOption.TRUNCATE_EXISTING);ok(!s.isCommittedAndIntact(id(908))&&recovered(s)==RecoveryImportStore.Disposition.MANUAL_REVIEW);});

    // Mixed-kind naming is candidate policy; real recovery store is currently JPG-only.
    run("VGL-10-01",()->{var n=new VirtualLabPolicies.Names(ZoneId.of("UTC"));ok(!n.reserve(id(1),0,VirtualLabPolicies.MediaKind.JPG).equals(n.reserve(id(2),0,VirtualLabPolicies.MediaKind.JPG)));});
    run("VGL-10-02",()->{var n=new VirtualLabPolicies.Names(ZoneId.of("UTC"));ok(n.reserve(id(1),0,VirtualLabPolicies.MediaKind.JPG).endsWith("0001.jpg")&&n.reserve(id(2),0,VirtualLabPolicies.MediaKind.OPUS).endsWith("0002.opus"));});
    run("VGL-10-03",()->{var n=new VirtualLabPolicies.Names(ZoneId.of("UTC"));ok(n.reserve(id(1),86_399_999,VirtualLabPolicies.MediaKind.JPG).startsWith("1970-01-01/")&&n.reserve(id(2),86_400_000,VirtualLabPolicies.MediaKind.JPG).startsWith("1970-01-02/"));});
    run("VGL-10-04",()->{var n=new VirtualLabPolicies.Names(ZoneId.of("UTC"));n.reserve(id(1),1,VirtualLabPolicies.MediaKind.JPG);n.reserve(id(2),1,VirtualLabPolicies.MediaKind.JPG);ok(n.reservations()==2);});
    run("VGL-10-05",()->{var n=new VirtualLabPolicies.Names(ZoneId.of("UTC"));n.reserve(id(1),0,VirtualLabPolicies.MediaKind.JPG);ok(n.reserve(id(2),0,VirtualLabPolicies.MediaKind.JPG).endsWith("0002.jpg"));});
    run("VGL-10-06",()->{var n=new VirtualLabPolicies.Names(ZoneId.of("UTC"));String path=n.reserve(id(1),0,VirtualLabPolicies.MediaKind.JPG);ok(n.knownReceipt(id(1))&&n.reserve(id(1),0,VirtualLabPolicies.MediaKind.JPG).equals(path));});
    run("VGL-10-07",()->{var n=new VirtualLabPolicies.Names(ZoneId.of("UTC"));n.reserve(id(1),0,VirtualLabPolicies.MediaKind.JPG);ok(n.verifyIdentity(id(1),"a","b")==VirtualLabPolicies.Decision.MANUAL_REVIEW);});
    run("VGL-10-08",()->{var n=new VirtualLabPolicies.Names(ZoneId.of("Asia/Kolkata"));ok(n.reserve(id(1),0,VirtualLabPolicies.MediaKind.JPG).startsWith("1970-01-01/")&&n.reserve(id(1),86_400_000,VirtualLabPolicies.MediaKind.JPG).endsWith("0001.jpg"));});

    // Injections test pure decisions, not real Android permissions or reboot.
    run("VGL-11-01",()->{var p=new VirtualLabPolicies.Platform();p.begin(true,false,true);ok(p.state()==VirtualLabPolicies.Platform.State.PLATFORM_BLOCKED);});
    run("VGL-11-02",()->{var p=new VirtualLabPolicies.Platform();p.begin(true,true,true);p.permissionLost();ok(p.state()==VirtualLabPolicies.Platform.State.PLATFORM_BLOCKED);});
    run("VGL-11-03",()->{var p=new VirtualLabPolicies.Platform();p.begin(true,true,false);ok(p.state()==VirtualLabPolicies.Platform.State.PLATFORM_BLOCKED);});
    run("VGL-11-04",()->{var p=new VirtualLabPolicies.Platform();p.begin(true,true,true);p.background();ok(p.state()==VirtualLabPolicies.Platform.State.BACKGROUND&&!p.canFetch());});
    run("VGL-11-05",()->{var p=new VirtualLabPolicies.Platform();p.begin(true,true,true);p.fetchIntent();p.restart();ok(p.state()==VirtualLabPolicies.Platform.State.REMOTE_UNCERTAIN&&!p.canFetch());});
    run("VGL-11-06",()->{var p=new VirtualLabPolicies.Platform();p.begin(true,true,true);p.fetchIntent();p.restart();ok(p.state()==VirtualLabPolicies.Platform.State.REMOTE_UNCERTAIN);});
    run("VGL-11-07",()->{var p=new VirtualLabPolicies.Platform();p.diskFull();ok(p.state()==VirtualLabPolicies.Platform.State.DISK_BLOCKED);});
    run("VGL-11-08",()->{var p=new VirtualLabPolicies.Platform();p.migrate(99);ok(p.state()==VirtualLabPolicies.Platform.State.SCHEMA_BLOCKED);});

    // Release security remains blocked because G6A physical evidence is absent.
    run("VGL-12-01",()->{var synthetic=image();ok(synthetic.length==7&&!new String(synthetic).contains("client"));});
    run("VGL-12-02",()->{ok(!VirtualLabPolicies.Release.cleanProduction(true,false,false,true));});
    run("VGL-12-03",()->{String mac=String.join(":","02","11","22","33","44","55");String clean=VirtualLabPolicies.Release.redact(mac);ok(!clean.contains(mac)&&VirtualLabPolicies.Release.safeReport(clean));});
    run("VGL-12-04",()->{String clean=VirtualLabPolicies.Release.redact("error: media/private_project.jpg");ok(!clean.contains("private_project.jpg"));});
    run("VGL-12-05",()->{var g=new VirtualLabPolicies.Group(new VirtualClock());g.preflight(false,true);g.peers(0);ok(!g.mayConnectRealPeer()&&!g.mayCatalog());});
    run("VGL-12-06",()->{ok(!VirtualLabPolicies.Release.allowHttp("GET",false,true,"media/a.jpg"));});
    run("VGL-12-07",()->{ok(!VirtualLabPolicies.Release.allowHttp("HEAD",false,false,"media/a.jpg")&&!VirtualLabPolicies.Release.allowHttp("GET",true,false,"media/a.jpg"));});
    run("VGL-12-08",()->{ok(!VirtualLabPolicies.Release.cleanProduction(false,false,false,false)&&!new VirtualLabPolicies.Platform().canFetch());});
    ok(passed==72);
    System.out.println("72/72 ADDITIONAL OFFLINE SYNTHETIC ASSERTION SETS PASS; ZERO ANDROID OR PHYSICAL GATES");
  }
}
