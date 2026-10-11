package com.parkarsite.g1.virtual.host;

import com.parkarsite.g1.virtual.VirtualClock;
import com.parkarsite.g1.virtual.VirtualGlassesCore;
import com.parkarsite.g1.virtual.VirtualLabPolicies;
import com.parkarsite.g6a.recovery.RecoveryImportStore;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * HOST JVM ONLY. Integrates the actual virtual BLE/peer state machine,
 * catalog/delta policy, a loopback synthetic HTTP server and the durable
 * RecoveryImportStore. Absolutely NOT an Android or glasses adapter.
 * The special synthetic-only media permit exists only inside this test.
 */
public final class HostCoreIntegrationSelfTest {
    private static final String BONDED = "AIMB-G1_TEST";
    private static final String RAW = String.join(":", "AA","BB","CC","DD","EE","FF");
    private static final String PEER = "TEST_AABBCCDDEEFF";
    private static final byte[] JPG = {(byte)255,(byte)216,(byte)255,1,2,(byte)255,(byte)217};
    private static final int MAX_CATALOG = 65_536;
    private static int passed;
    private HostCoreIntegrationSelfTest(){}

    private interface Trial { void run() throws Exception; }
    private static void check(String caseName, Trial trial) throws Exception {
        try {
            trial.run();
            passed++;
            System.out.println(caseName+" HOST_CORE_SYNTHETIC_PASS");
        } catch(Exception|AssertionError e) {
            System.err.println(caseName+" FAILED");
            throw e;
        }
    }
    private static void ok(boolean value, String invariant) {
        if(!value) throw new AssertionError(invariant);
    }

    private static final class FakeBle implements VirtualGlassesCore.BleTransportPort {
        int connects, subscribes, enters, exits;
        public void connect(){connects++;}
        public void subscribe(){subscribes++;}
        public void enter(){enters++;}
        public void exit(){exits++;}
    }
    private static final class Session {
        final FakeBle ble = new FakeBle();
        final VirtualClock clock = new VirtualClock();
        final VirtualGlassesCore core = new VirtualGlassesCore(clock,ble,()->{});
        Session(List<String> names,boolean member, boolean bound,boolean trusted,boolean ip) {
            core.begin(1,BONDED,RAW);
            core.onGatt(true);
            core.onSubscribed(true);
            core.armObservation();
            core.onEnterWrite(true);
            core.onPeerSnapshot(names);
            core.onGroupEvidence(member,bound,trusted);
            core.onSyntheticIpEvent(ip);
        }
        void close(){core.finishObservation();core.onExitWrite(true,true);}
    }
    private static final class FakeGlassesServer implements AutoCloseable {
        final HttpServer server;
        final AtomicInteger catalogs = new AtomicInteger(), media = new AtomicInteger();
        String before = "media/old.jpg\n";
        String after = "media/old.jpg\nmedia/new.jpg\n";
        boolean consumed, wrongMethod, receivedRange;
        int mediaStatus=200;
        FakeGlassesServer() throws Exception {
            server=HttpServer.create(new InetSocketAddress(InetAddress.getByName("127.0.0.1"),0),0);
            server.createContext("/files/media.config",this::catalog);
            server.createContext("/files/media/new.jpg",this::media);
            server.start();
        }
        URI base(){return URI.create("http://127.0.0.1:"+server.getAddress().getPort()+"/");}
        private void catalog(HttpExchange e)throws IOException {
            wrongMethod |= !"GET".equals(e.getRequestMethod());
            String response=catalogs.getAndIncrement()==0?before:after;
            byte[] bytes=response.getBytes(java.nio.charset.StandardCharsets.UTF_8);
            try{e.sendResponseHeaders(200,bytes.length);
                try(OutputStream o=e.getResponseBody()){o.write(bytes);}}
            finally{e.close();}
        }
        private void media(HttpExchange e)throws IOException {
            wrongMethod |= !"GET".equals(e.getRequestMethod());
            receivedRange |= e.getRequestHeaders().containsKey("Range");
            media.incrementAndGet();
            consumed=true; // synthetic consumptive firmware fixture, NOT actual hardware
            try {
                if(mediaStatus!=200){e.sendResponseHeaders(mediaStatus,-1);}
                else {
                    e.getResponseHeaders().set("Content-Type","image/jpeg");
                    e.sendResponseHeaders(200,JPG.length);
                    try(OutputStream o=e.getResponseBody()){o.write(JPG);}
                }
            } finally {e.close();}
        }
        public void close(){server.stop(0);}
    }
    private static HttpURLConnection open(FakeGlassesServer src,String path)throws IOException {
        URI root=src.base();
        if(!"http".equals(root.getScheme())||!"127.0.0.1".equals(root.getHost())
           ||root.getPort()<=0||root.getRawUserInfo()!=null||root.getRawQuery()!=null)
            throw new IllegalArgumentException("Not a synthetic loopback");
        HttpURLConnection conn=(HttpURLConnection)root.resolve(path).toURL().openConnection();
        conn.setRequestMethod("GET");
        conn.setInstanceFollowRedirects(false);
        conn.setUseCaches(false);
        conn.setConnectTimeout(1200);
        conn.setReadTimeout(1200);
        return conn;
    }
    private static VirtualLabPolicies.Catalog.Result list(Session session,FakeGlassesServer src)
            throws Exception {
        if(!session.core.catalogEligible())throw new IllegalStateException("Catalog gate closed");
        HttpURLConnection c=open(src,"files/media.config");
        try {
            if(c.getResponseCode()!=200)throw new IOException("Bad synthetic catalog response");
            if(c.getContentLengthLong()>MAX_CATALOG)throw new IOException("Catalog too large");
            try(InputStream input=c.getInputStream()){
                byte[] bytes=input.readNBytes(MAX_CATALOG+1);
                if(bytes.length>MAX_CATALOG)throw new IOException("Catalog cap");
                return VirtualLabPolicies.Catalog.parse(1,bytes);
            }
        } finally {c.disconnect();}
    }
    /**
     * This special bypass exists exclusively in this host-only test package and
     * accepts only the synthetic fixture endpoint and id. It cannot be imported
     * by the no-permission Android app. Production core remains mediaGet=false.
     */
    private static void fakeOnlyDownload(Session s,FakeGlassesServer src,
            RecoveryImportStore store,RecoveryImportStore.Ticket ticket,
            VirtualLabPolicies.Decision delta,boolean explicitSyntheticPermit)throws Exception {
        if(!explicitSyntheticPermit || !s.core.catalogEligible()
            || s.core.mediaGetAuthorized() || delta!=VirtualLabPolicies.Decision.ALLOW)
            throw new IllegalStateException("Synthetic-only media gate refused");
        store.execute(ticket,JPG.length,out->{
            HttpURLConnection c=open(src,"files/media/new.jpg");
            try {
                if(c.getResponseCode()!=200)throw new IOException("Bad synthetic status");
                if(c.getContentLengthLong()!=JPG.length)throw new IOException("Bad synthetic length");
                if(!"image/jpeg".equalsIgnoreCase(c.getContentType()))
                    throw new IOException("Bad synthetic content type");
                try(InputStream input=c.getInputStream()){
                    byte[] bytes=input.readNBytes(JPG.length+1);
                    if(bytes.length!=JPG.length)throw new IOException("Synthetic truncation");
                    out.write(bytes);
                }
            } finally {c.disconnect();}
        },ignored->{});
    }
    private static RecoveryImportStore store()throws Exception {
        return new RecoveryImportStore(Files.createTempDirectory("vgl-host-core-only-"),
                                       ZoneId.of("Asia/Kolkata"));
    }
    private static RecoveryImportStore.Ticket ticket(RecoveryImportStore s,int id)throws Exception {
        return s.prepare(String.format("%064x",id),0L);
    }
    private static VirtualLabPolicies.Decision oneJpgDelta(Session session,FakeGlassesServer server)
            throws Exception {
        var baseline=list(session,server);
        var latest=list(session,server);
        return new VirtualLabPolicies.Capture(baseline).inspect(latest,2);
    }
    private static void denyCatalog(Session session,FakeGlassesServer server)throws Exception{
        try{list(session,server);throw new AssertionError("Expected blocked catalog");}
        catch(IllegalStateException expected){}
        ok(server.catalogs.get()==0&&server.media.get()==0,
           "No local synthetic HTTP before verified group and IP");
    }

    public static void main(String[] unused)throws Exception{
        check("CORE-HTTP-01",()->{
            try(FakeGlassesServer src=new FakeGlassesServer()){
                Session s=new Session(List.of(PEER),true,true,true,true);
                ok(s.core.catalogEligible(),"trusted synthetic group with correlated IP");
                ok(!s.core.mediaGetAuthorized()&&!s.core.connectAuthorized(),
                    "real media and peer connection remain forbidden");
                var delta=oneJpgDelta(s,src);
                ok(delta==VirtualLabPolicies.Decision.ALLOW,"single new JPG set delta");
                var db=store();var t=ticket(db,111);
                fakeOnlyDownload(s,src,db,t,delta,true);
                ok(db.isCommittedAndIntact(t.opaqueId()),"durable local synthetic media receipt");
                ok(src.catalogs.get()==2&&src.media.get()==1&&src.consumed,
                    "two catalogs, one synthetic media GET, simulated remote consumption");
                try{fakeOnlyDownload(s,src,db,t,delta,true);throw new AssertionError("second GET allowed");}
                catch(IOException expected){}
                ok(src.media.get()==1,"no double GET");
                s.close();
                ok(s.core.stage()==VirtualGlassesCore.Stage.FINISHED&&s.ble.exits==1,
                    "one final EXIT and verified cleanup");
                ok(!src.wrongMethod&&!src.receivedRange,"no HEAD, Range or alternate method");
            }
        });
        check("CORE-HTTP-02",()->{
            try(FakeGlassesServer src=new FakeGlassesServer()){
                Session s=new Session(List.of("OTHER"),false,false,false,false);
                ok(s.core.peerReport().verdict()
                    ==com.parkarsite.g6a.identity.PeerIdentityPolicy.Verdict.NO_CANDIDATE,
                    "anonymous peer not a candidate");
                denyCatalog(s,src);s.close();
            }
        });
        check("CORE-HTTP-03",()->{
            try(FakeGlassesServer src=new FakeGlassesServer()){
                Session s=new Session(List.of("AIMB-G1_OTHER"),true,true,true,true);
                denyCatalog(s,src);s.close();
                ok(s.core.outcome()==VirtualGlassesCore.Outcome.SAFE_FINISH,"clean aborted visit");
            }
        });
        check("CORE-HTTP-04",()->{
            try(FakeGlassesServer src=new FakeGlassesServer()){
                Session s=new Session(List.of(PEER),true,true,false,true);
                denyCatalog(s,src);s.close();
            }
        });
        check("CORE-HTTP-05",()->{
            try(FakeGlassesServer src=new FakeGlassesServer()){
                Session s=new Session(List.of(PEER),true,true,true,false);
                denyCatalog(s,src);s.close();
            }
        });
        check("CORE-HTTP-06",()->{
            try(FakeGlassesServer src=new FakeGlassesServer()){
                src.after="media/new.jpg\n"; // baseline photo vanished before download
                Session s=new Session(List.of(PEER),true,true,true,true);
                var result=oneJpgDelta(s,src);
                ok(result==VirtualLabPolicies.Decision.BLOCK,"baseline loss blocks import");
                var db=store();var t=ticket(db,112);
                try{fakeOnlyDownload(s,src,db,t,result,true);
                    throw new AssertionError("lost baseline not blocked");}
                catch(IllegalStateException expected){}
                ok(src.media.get()==0,"no consumptive read on invalid set delta");
                s.close();
            }
        });
        check("CORE-HTTP-07",()->{
            try(FakeGlassesServer src=new FakeGlassesServer()){
                src.after="media/old.jpg\nmedia/new.jpg\nmedia/other.jpg\n";
                Session s=new Session(List.of(PEER),true,true,true,true);
                var result=oneJpgDelta(s,src);
                ok(result==VirtualLabPolicies.Decision.AMBIGUOUS,"+2 is ambiguous");
                var db=store();var t=ticket(db,113);
                try{fakeOnlyDownload(s,src,db,t,result,true);
                    throw new AssertionError("+2 not blocked");}
                catch(IllegalStateException expected){}
                ok(src.media.get()==0,"no arbitrary +2 download");
                s.close();
            }
        });
        check("CORE-HTTP-08",()->{
            try(FakeGlassesServer src=new FakeGlassesServer()){
                Session s=new Session(List.of(PEER),true,true,true,true);
                var result=oneJpgDelta(s,src);
                var db=store();var t=ticket(db,114);
                try{fakeOnlyDownload(s,src,db,t,result,false);
                    throw new AssertionError("Missing explicit synthetic permit");}
                catch(IllegalStateException expected){}
                ok(src.media.get()==0,"no test GET unless simulation explicitly authorized");
                s.close();
                try{fakeOnlyDownload(s,src,db,t,result,true);
                    throw new AssertionError("Ended session still permitted download");}
                catch(IllegalStateException expected){}
                ok(src.media.get()==0,"completed session cannot fetch");
            }
        });
        check("CORE-HTTP-09",()->{
            try(FakeGlassesServer src=new FakeGlassesServer()){
                src.mediaStatus=500;
                Session s=new Session(List.of(PEER),true,true,true,true);
                var result=oneJpgDelta(s,src);
                var db=store();var t=ticket(db,115);
                try{fakeOnlyDownload(s,src,db,t,result,true);
                    throw new AssertionError("HTTP error accepted");}
                catch(IOException expected){}
                ok(db.recover().get(0).disposition()
                    ==RecoveryImportStore.Disposition.REMOTE_UNCERTAIN,
                    "one-time source failure quarantines receipt");
                ok(src.media.get()==1,"GET was issued exactly once");
                try{fakeOnlyDownload(s,src,db,t,result,true);
                    throw new AssertionError("unsafe retry");}
                catch(IOException expected){}
                ok(src.media.get()==1,"no second request following 500");
                s.close();
            }
        });
        check("CORE-HTTP-10",()->{
            try(FakeGlassesServer src=new FakeGlassesServer()){
                Session s=new Session(List.of(PEER,"DIFFERENT_AABBCCDDEEFF"),true,true,true,true);
                ok(!s.core.catalogEligible(),"two candidate peer names cannot verify group");
                denyCatalog(s,src);
                s.close();
            }
        });
        ok(passed==10,"ten integrated host-only scenarios executed");
        System.out.println("HOST_CORE 10/10 PASS: BLE/peer/delta/localhost-only/receipt; no Android or glasses");
    }
}
