package com.parkarsite.g1.virtual;
import java.time.ZoneId;
import java.util.*;

/** Independently targeted negative properties for the VGL-04..12 synthetic policy layer. */
public final class VirtualPhase2RedTeamSelfTest {
  static void check(boolean b,String label){if(!b)throw new AssertionError(label);}
  static VirtualLabPolicies.Catalog.Result cat(String s){
    return VirtualLabPolicies.Catalog.parse(1,VirtualLabPolicies.Catalog.bytes(s));
  }
  public static void main(String[] args){
    var clock=new VirtualClock();
    var early=new VirtualLabPolicies.Group(clock);
    early.cleanup();
    check(early.removals()==0&&early.state()==VirtualLabPolicies.Group.State.ABORTED,"no cleanup before preflight");
    var formed=new VirtualLabPolicies.Group(clock);
    formed.preflight(false,true);
    formed.peers(1);formed.formed(true,true);
    check(formed.mayCatalog()&&!formed.mayConnectRealPeer(),"no real peer connection");
    formed.groupChanged();
    check(!formed.mayCatalog()&&formed.removals()==1,"group invalidation");
    formed.cleanupCallback(VirtualLabPolicies.Group.CleanupStatus.SUCCESS,false);
    check(formed.state()==VirtualLabPolicies.Group.State.UNCERTAIN,"callback alone not absence proof");
    formed.cleanup();
    check(formed.removals()==1,"cleanup is one shot");
    var sample=new VirtualLabPolicies.OneShot();
    check(sample.start(true,true,"GET","media/probe.jpg",false)==VirtualLabPolicies.Decision.ALLOW,"synthetic start");
    byte[] wrong=new byte[]{1,2,3,4,5,6,7};
    check(sample.observe(200,7,wrong,false,true,true)==VirtualLabPolicies.Decision.REMOTE_UNCERTAIN,
       "malformed media cannot commit even with fake receipt flag");
    check(!sample.committed()&&sample.retry()==VirtualLabPolicies.Decision.BLOCK,"no retry");
    var once=new VirtualLabPolicies.OneShot();
    check(once.start(true,true,"HEAD","media/probe.jpg",false)==VirtualLabPolicies.Decision.BLOCK,"HEAD forbidden");
    check(once.start(true,true,"GET","media/probe.jpg",true)==VirtualLabPolicies.Decision.BLOCK,"Range forbidden");
    check(once.start(true,false,"GET","media/probe.jpg",false)==VirtualLabPolicies.Decision.BLOCK,"preservation blocks");
    var changed=new VirtualLabPolicies.Capture(cat("media/old.jpg"));
    check(changed.inspect(cat("media/new.jpg"),1)==VirtualLabPolicies.Decision.BLOCK,"missing original is not a good new delta");
    var names=new VirtualLabPolicies.Names(ZoneId.of("UTC"));
    String opaque=String.format("%064x",123);
    names.reserve(opaque,0,VirtualLabPolicies.MediaKind.JPG);
    check(!names.knownReceipt(opaque),"reservation must never count as a receipt");
    check(!names.receiptVerified(opaque,false)&&!names.knownReceipt(opaque),"bad receipt blocked");
    check(names.receiptVerified(opaque,true)&&names.knownReceipt(opaque),"verified synthetic receipt");
    String fabricatedMac=String.join(":","02","44","00","00","12","99");
    String raw="bluetooth "+fabricatedMac+" email admin@example.invalid ip 10.0.0.12 password="+
       "fixture-secrets"+" file media/private_capture.jpg";
    String safe=VirtualLabPolicies.Release.redact(raw);
    check(!safe.contains(fabricatedMac)&&!safe.contains("admin@example.invalid")
          &&!safe.contains("10.0.0.12")&&!safe.contains("fixture-secrets")
          &&!safe.contains("private_capture.jpg"),"redaction");
    check(VirtualLabPolicies.Release.safeReport(safe),"sanitised report safe");
    check(!VirtualLabPolicies.Release.safeReport(raw),"raw secrets blocked");
    check(!VirtualLabPolicies.Release.cleanProduction(false,false,false,false),"physical release gate remains blocked");
    check(!new VirtualLabPolicies.Platform().canFetch(),"no live reads");
    System.out.println("PHASE2_RED_TEAM PASS: malformed bytes, uncertain cleanup, retained originals, receipt integrity, secrets, no GET");
  }
}
