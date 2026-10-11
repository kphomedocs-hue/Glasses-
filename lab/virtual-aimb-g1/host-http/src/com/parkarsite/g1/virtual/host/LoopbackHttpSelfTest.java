package com.parkarsite.g1.virtual.host;

import com.parkarsite.g1.virtual.VirtualLabPolicies;
import com.parkarsite.g6a.recovery.RecoveryImportStore;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Host-JVM ONLY socket tests on loopback. NOT an Android adapter; no glasses,
 * vendor host/IP, credentials, external route or physically approved media GET.
 */
public final class LoopbackHttpSelfTest {
    private static final byte[] JPG={(byte)255,(byte)216,(byte)255,1,2,(byte)255,(byte)217};
    private static int passed;
    private LoopbackHttpSelfTest(){}
    private static void ok(boolean value,String invariant){if(!value)throw new AssertionError(invariant);}
    private interface Case {void run()throws Exception;}
    private static void check(String id,Case test)throws Exception{
        try{test.run();passed++;System.out.println(id+" LOOPBACK_SYNTHETIC_PASS");}
        catch(Exception|AssertionError error){System.err.println(id+" FAIL");throw error;}
    }
    private static final class Server implements AutoCloseable {
        final HttpServer server;
        final AtomicInteger getCount=new AtomicInteger(), redirectedHits=new AtomicInteger();
        volatile int status=200;
        volatile byte[] body=JPG;
        volatile int reportedLength=JPG.length;
        volatile boolean consumeAtGet, consumed;
        volatile boolean wrongMethod, rangeReceived;
        Server()throws Exception{
            server=HttpServer.create(new InetSocketAddress(InetAddress.getByName("127.0.0.1"),0),0);
            server.createContext("/files/media/a.jpg",this::respond);
            server.createContext("/redirected",ex->{
                redirectedHits.incrementAndGet();
                ex.sendResponseHeaders(500,-1);ex.close();
            });
            server.start();
        }
        URI base(){return URI.create("http://127.0.0.1:"+server.getAddress().getPort()+"/");}
        private void respond(HttpExchange exchange)throws IOException{
            getCount.incrementAndGet();
            wrongMethod |= !"GET".equals(exchange.getRequestMethod());
            rangeReceived |= exchange.getRequestHeaders().containsKey("Range");
            if(consumeAtGet)consumed=true;
            try{
                if(status==302){
                    exchange.getResponseHeaders().set("Location","/redirected");
                    exchange.sendResponseHeaders(302,-1);
                }else if(status!=200) {
                    exchange.sendResponseHeaders(status,-1);
                }else{
                    exchange.getResponseHeaders().set("Content-Type","image/jpeg");
                    // Java HttpServer automatically emits the actual transfer length.
                    exchange.sendResponseHeaders(200,body.length);
                    try(OutputStream os=exchange.getResponseBody()){os.write(body);}
                }
            }finally{exchange.close();}
        }
        @Override public void close(){server.stop(0);}
    }
    /** Restricted test adapter: the URL can only be 127.0.0.1 and the one fixed synthetic JPG. */
    private static RecoveryImportStore.Ticket localGetOnce(RecoveryImportStore store,
            RecoveryImportStore.Ticket ticket,URI root,long expectedBytes)throws Exception{
        if(root==null||!"http".equals(root.getScheme())||!"127.0.0.1".equals(root.getHost())
                ||root.getPort()<=0||root.getRawUserInfo()!=null||root.getRawQuery()!=null
                ||root.getRawFragment()!=null||!"/".equals(root.getPath()))
            throw new IllegalArgumentException("Not an approved synthetic loopback source");
        if(!VirtualLabPolicies.Release.allowHttp("GET",false,false,"media/a.jpg"))
            throw new IllegalStateException("HTTP safety policy rejected fixture");
        return store.execute(ticket,expectedBytes,output->{
            URI resolved=root.resolve("files/media/a.jpg");
            HttpURLConnection conn=(HttpURLConnection)resolved.toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setInstanceFollowRedirects(false);
            conn.setUseCaches(false);
            conn.setConnectTimeout(1000);
            conn.setReadTimeout(1000);
            try {
                if(conn.getResponseCode()!=200)throw new IOException("Synthetic HTTP status was not 200");
                if(conn.getContentLengthLong()!=expectedBytes)throw new IOException("Synthetic length conflict");
                if(!"image/jpeg".equalsIgnoreCase(conn.getContentType()))
                    throw new IOException("Synthetic media type mismatch");
                try(InputStream input=conn.getInputStream()){
                    byte[] chunk=new byte[4096];
                    int count;
                    long total=0;
                    while((count=input.read(chunk))!=-1) {
                        total+=count;
                        if(total>expectedBytes)throw new IOException("Too many bytes");
                        output.write(chunk,0,count);
                    }
                    if(total!=expectedBytes)throw new IOException("Partial body");
                }
            }finally{conn.disconnect();}
        },ignored->{});
    }
    private static RecoveryImportStore store()throws Exception{
        return new RecoveryImportStore(Files.createTempDirectory("vgl-host-http-"), ZoneId.of("Asia/Kolkata"));
    }
    private static RecoveryImportStore.Ticket prepared(RecoveryImportStore s,int id)throws Exception{
        return s.prepare(String.format("%064x",id),0L);
    }
    private static void expectUncertain(Case action,RecoveryImportStore s)throws Exception{
        try{action.run();throw new AssertionError("expected failure");}
        catch(IOException expected){}
        var recovered=s.recover();
        ok(recovered.size()==1&&recovered.get(0).disposition()
                ==RecoveryImportStore.Disposition.REMOTE_UNCERTAIN,"no retry on uncertain remote");
    }
    public static void main(String[] ignored)throws Exception{
        check("HTTP-01",()->{
            try(Server src=new Server()){
                var store=store();var ticket=prepared(store,7001);
                var finished=localGetOnce(store,ticket,src.base(),JPG.length);
                ok(finished.state()==RecoveryImportStore.State.COMMITTED,"durable completed");
                ok(store.isCommittedAndIntact(ticket.opaqueId()),"receipt and bytes");
                ok(src.getCount.get()==1&&!src.wrongMethod&&!src.rangeReceived,"only GET, no range");
                // Duplicate local GET is forbidden, even when catalog reappears.
                expectUncertainDuplicate(()->localGetOnce(store,ticket,src.base(),JPG.length));
                ok(src.getCount.get()==1,"duplicate did not open remote GET");
            }
        });
        check("HTTP-02",()->{
            try(Server src=new Server()){
                src.status=500;var store=store();var ticket=prepared(store,7002);
                expectUncertain(()->localGetOnce(store,ticket,src.base(),JPG.length),store);
                ok(src.getCount.get()==1,"HTTP 500 single GET");
                expectUncertainDuplicate(()->localGetOnce(store,ticket,src.base(),JPG.length));
                ok(src.getCount.get()==1,"HTTP 500 cannot retry");
            }
        });
        check("HTTP-03",()->{
            try(Server src=new Server()){
                src.status=302;var store=store();var ticket=prepared(store,7003);
                expectUncertain(()->localGetOnce(store,ticket,src.base(),JPG.length),store);
                ok(src.getCount.get()==1&&src.redirectedHits.get()==0,"redirect not followed");
            }
        });
        check("HTTP-04",()->{
            try(Server src=new Server()){
                src.body=new byte[]{(byte)255,(byte)216,1,2};
                var store=store();var ticket=prepared(store,7004);
                expectUncertain(()->localGetOnce(store,ticket,src.base(),JPG.length),store);
                ok(src.getCount.get()==1,"short media body rejected");
            }
        });
        check("HTTP-05",()->{
            try(Server src=new Server()){
                src.body=new byte[]{1,2,3,4,5,6,7};
                var store=store();var ticket=prepared(store,7005);
                expectUncertain(()->localGetOnce(store,ticket,src.base(),JPG.length),store);
                ok(src.getCount.get()==1,"wrong JPEG data cannot commit");
            }
        });
        check("HTTP-06",()->{
            try(Server src=new Server()){
                src.consumeAtGet=true;
                var store=store();var ticket=prepared(store,7006);
                localGetOnce(store,ticket,src.base(),JPG.length);
                ok(src.consumed&&store.isCommittedAndIntact(ticket.opaqueId()),"local receipt survives remote consumption");
                // Deliberately NOT a preservation success: consumed original is a HARD BLOCK.
            }
        });
        check("HTTP-07",()->{
            try(Server src=new Server()){
                var store=store();var ticket=prepared(store,7007);
                try{
                    localGetOnce(store,ticket,URI.create("http://invalid.example/"),JPG.length);
                    throw new AssertionError("non-loopback rejected");
                }catch(IllegalArgumentException expected){}
                ok(src.getCount.get()==0,"no external or unknown host");
                ok(store.recover().get(0).disposition()
                    ==RecoveryImportStore.Disposition.NOT_FETCHED,"no intent before validation");
            }
        });
        ok(passed==7,"7 loopback cases");
        System.out.println("HOST_HTTP 7/7 PASS: local-only synthetic GET and real recovery; zero device gates");
    }
    private static void expectUncertainDuplicate(Case action)throws Exception {
        try{action.run();throw new AssertionError("duplicate remote GET should be rejected");}
        catch(IOException expected){}
    }
}
