package com.parkarsite.g1.virtual;
import java.util.*;
/** Separate adversarial timing/identity test; no device use. */
public final class VirtualRedTeamSelfTest {
  static void ok(boolean b){if(!b)throw new AssertionError("red-team invariant");}
  static final class Fake implements VirtualGlassesCore.BleTransportPort,VirtualGlassesCore.PeerDiscoveryPort{
    int enter,exit,connect,discover,subscribe;
    public void enter(){enter++;}public void exit(){exit++;}
    public void connect(){connect++;}public void discover(){discover++;}
    public void subscribe(){subscribe++;}
  }
  public static void main(String[] ignored){
    Fake f=new Fake();VirtualClock t=new VirtualClock();
    VirtualGlassesCore c=new VirtualGlassesCore(t,f,f);
    c.begin(1,"AIMB-G1_TEST",String.join(":", "02","00","00","00","00","03"));c.onGatt(true);c.onSubscribed(true);c.armObservation();
    t.advance(4999);ok(c.stage()==VirtualGlassesCore.Stage.ENTER_PENDING&&f.exit==0);
    c.onEnterWrite(true);ok(f.discover==1&&!t.scheduled("enter")&&t.scheduled("discovery"));
    c.onPeerSnapshot(List.of("TEST_020000000003"));ok(!c.connectAuthorized()&&!c.mediaGetAuthorized()&&!c.catalogEligible());
    t.advance(21000);ok(c.stage()==VirtualGlassesCore.Stage.EXIT_PENDING&&f.exit==1);
    t.advance(26010);ok(c.outcome()==VirtualGlassesCore.Outcome.EXIT_UNVERIFIED);
    c.onExitWrite(true,true);c.finishObservation();ok(f.exit==1&&c.stage()==VirtualGlassesCore.Stage.STOPPED);
    byte[] b=FrameEvidence.synthetic(0x41,new byte[5]);b[3]=127;
    ok(FrameEvidence.parse(b).kind()==FrameEvidence.Kind.INVALID);
    Fake f2=new Fake();VirtualClock t2=new VirtualClock();VirtualGlassesCore c2=new VirtualGlassesCore(t2,f2,f2);
    c2.begin(1,"AIMB-G1_TEST",String.join(":", "02","00","00","00","00","03"));c2.onGatt(true);c2.onSubscribed(true);
    t2.advance(1_000_000);ok(c2.stage()==VirtualGlassesCore.Stage.READY&&f2.enter==0);
    System.out.println("RED_TEAM PASS: separate timers, no retry, no auth, no GET, invalid framing");
  }
}
