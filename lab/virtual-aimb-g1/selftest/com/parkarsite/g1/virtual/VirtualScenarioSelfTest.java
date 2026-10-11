package com.parkarsite.g1.virtual;
import com.parkarsite.g6a.identity.PeerIdentityPolicy;
import java.util.*;

/** 24 executable synthetic safety scenarios, not physical firmware tests. */
public final class VirtualScenarioSelfTest {
  static final String NAME="AIMB-G1_42",ADDR=String.join(":", "02","00","00","00","00","01"),EXACT="42_020000000001";
  static int passed;
  static void ok(boolean b){if(!b)throw new AssertionError("synthetic scenario invariant");}
  static void run(String id,Runnable r){try{r.run();passed++;System.out.println(id+" OFFLINE_ASSERTIONS_PASS");}catch(Throwable e){System.err.println(id+" FAIL");throw e;}}
  static final class Fake implements VirtualGlassesCore.BleTransportPort,VirtualGlassesCore.PeerDiscoveryPort {
    int connects,subscriptions,enters,exits,discovers;
    public void connect(){connects++;}public void subscribe(){subscriptions++;}
    public void enter(){enters++;}public void exit(){exits++;}
    public void discover(){discovers++;}
  }
  static final class H {
    final Fake f=new Fake();final VirtualClock t=new VirtualClock();
    final VirtualGlassesCore c=new VirtualGlassesCore(t,f,f);
    H ready(){c.begin(1,NAME,ADDR);c.onGatt(true);c.onSubscribed(true);ok(c.stage()==VirtualGlassesCore.Stage.READY);return this;}
    H observing(){ready();c.armObservation();c.onEnterWrite(true);ok(c.stage()==VirtualGlassesCore.Stage.OBSERVING);return this;}
    void close(){c.finishObservation();c.onExitWrite(true,true);}
    H peers(String... names){c.onPeerSnapshot(Arrays.asList(names));return this;}
  }
  static byte[] frame41(int n){byte[] p=new byte[n];Arrays.fill(p,(byte)42);return FrameEvidence.synthetic(0x41,p);}
  static byte[] event(int id){return FrameEvidence.synthetic(0x73,new byte[]{(byte)id});}
  public static void main(String[] ignored) {
    run("VGL-01-01",()->{H h=new H().ready();ok(h.f.connects==1&&h.f.subscriptions==1&&h.f.enters==0&&h.f.exits==0);});
    run("VGL-01-02",()->{H h=new H();h.c.begin(0,NAME,ADDR);ok(h.c.outcome()==VirtualGlassesCore.Outcome.NO_BONDED_TARGET&&h.f.connects==0);});
    run("VGL-01-03",()->{H h=new H();h.c.begin(2,NAME,ADDR);ok(h.c.outcome()==VirtualGlassesCore.Outcome.AMBIGUOUS_BONDED_TARGET&&h.f.connects==0);});
    run("VGL-01-04",()->{H h=new H();h.c.begin(1,NAME,ADDR);h.t.advance(5001);ok(h.c.outcome()==VirtualGlassesCore.Outcome.GATT_TIMEOUT&&h.f.enters==0);});
    run("VGL-01-05",()->{H h=new H();h.c.begin(1,NAME,ADDR);h.c.onGatt(true);h.c.onSubscribed(false);ok(h.c.outcome()==VirtualGlassesCore.Outcome.SUBSCRIPTION_FAILED&&h.f.enters==0);});
    run("VGL-01-06",()->{H h=new H().ready();h.c.armObservation();h.c.onEnterWrite(false);ok(h.f.enters==1&&h.f.exits==1&&h.c.stage()==VirtualGlassesCore.Stage.EXIT_PENDING);h.c.onExitWrite(true,true);ok(h.c.outcome()==VirtualGlassesCore.Outcome.ENTER_FAILED);});
    run("VGL-01-07",()->{H h=new H().ready();h.c.armObservation();h.c.onPeerSnapshot(List.of(EXACT));ok(h.f.discovers==0&&h.c.peerReport()==null);h.c.onEnterWrite(true);ok(h.f.discovers==1);h.close();});
    run("VGL-01-08",()->{H h=new H().observing();h.c.finishObservation();h.t.advance(5001);h.c.onExitWrite(true,true);ok(h.f.exits==1&&h.c.outcome()==VirtualGlassesCore.Outcome.EXIT_UNVERIFIED);});
    run("VGL-02-01",()->{H h=new H().observing();ok(h.c.onFrame(frame41(20)).kind()==FrameEvidence.Kind.OPAQUE_LONG_41&&!h.c.connectAuthorized());h.close();});
    run("VGL-02-02",()->{H h=new H().observing();ok(h.c.onFrame(frame41(5)).kind()==FrameEvidence.Kind.SHORT_41_UNKNOWN&&h.c.stage()==VirtualGlassesCore.Stage.OBSERVING);h.close();});
    run("VGL-02-03",()->{byte[] b=frame41(5);b[4]^=1;H h=new H().observing();ok(h.c.onFrame(b).kind()==FrameEvidence.Kind.INVALID&&h.c.stage()==VirtualGlassesCore.Stage.OBSERVING);h.close();});
    run("VGL-02-04",()->{byte[] b=frame41(5);b[2]=(byte)255;b[3]=(byte)255;ok(FrameEvidence.parse(b).kind()==FrameEvidence.Kind.INVALID);});
    run("VGL-02-05",()->{H h=new H().observing();ok(h.c.onFrame(event(1)).eventId()==1);h.c.onSyntheticInventory(4,2,1);ok(h.c.inventoryEvents()==1&&h.c.images()==4);h.close();});
    run("VGL-02-06",()->{H h=new H().observing();h.c.onFrame(event(8));h.c.onSyntheticIpEvent(true);ok(h.c.ipEvents()==1&&!h.c.catalogEligible());h.close();});
    run("VGL-02-07",()->{H h=new H().observing();h.c.onGroupEvidence(true,false,true);h.c.onSyntheticIpEvent(true);ok(!h.c.catalogEligible());h.c.onGroupEvidence(true,true,true);h.c.onSyntheticIpEvent(true);ok(h.c.catalogEligible()&&!h.c.mediaGetAuthorized());h.close();});
    run("VGL-02-08",()->{H h=new H().observing();for(int i=0;i<600;i++){h.c.onFrame(event(1));h.c.onFrame(event(8));}ok(h.c.eventCount()==256&&h.c.inventoryEvents()==256&&h.c.ipEvents()==256&&h.f.enters==1&&!h.c.mediaGetAuthorized());h.close();});
    run("VGL-03-01",()->{H h=new H().observing().peers(EXACT);ok(h.c.peerReport().exact()==1&&h.c.peerReport().verdict()==PeerIdentityPolicy.Verdict.SINGLE_UNVERIFIED_CANDIDATE&&!h.c.connectAuthorized());h.close();});
    run("VGL-03-02",()->{H h=new H().observing().peers("P2P_020000000001");ok(h.c.peerReport().normalizedSuffix()==1&&h.c.peerReport().exact()==0&&!h.c.connectAuthorized());h.close();});
    run("VGL-03-03",()->{H h=new H().observing().peers("P2P_"+ADDR);ok(h.c.peerReport().rawSuffixResearch()==1&&h.c.peerReport().normalizedSuffix()==0&&!h.c.connectAuthorized());h.close();});
    run("VGL-03-04",()->{H h=new H().observing().peers("AIMB-G1_MOCK");ok(h.c.peerReport().familyOnly()==1&&h.c.peerReport().verdict()==PeerIdentityPolicy.Verdict.NO_CANDIDATE);h.close();});
    run("VGL-03-05",()->{H h=new H().observing().peers("OTHER_TEST");ok(h.c.peerReport().other()==1&&h.c.peerReport().verdict()==PeerIdentityPolicy.Verdict.NO_CANDIDATE);h.close();});
    run("VGL-03-06",()->{H h=new H().observing().peers(EXACT,"P2P_020000000001");ok(h.c.peerReport().verdict()==PeerIdentityPolicy.Verdict.AMBIGUOUS_CANDIDATES&&!h.c.connectAuthorized());h.close();});
    run("VGL-03-07",()->{H h=new H().observing().peers(EXACT);h.c.onGroupEvidence(false,true,true);h.c.onSyntheticIpEvent(true);ok(!h.c.catalogEligible()&&!h.c.connectAuthorized());h.close();});
    run("VGL-03-08",()->{H h=new H().observing().peers(EXACT);String p2pInterface=String.join(":", "06","00","00","00","00","02");ok(!ADDR.equals(p2pInterface));h.c.onGroupEvidence(true,false,true);ok(!h.c.catalogEligible()&&!h.c.connectAuthorized());h.close();});
    ok(passed==24);System.out.println("24/24 OFFLINE SYNTHETIC ASSERTION SETS PASS; ZERO PHYSICAL GATES");
  }
}
