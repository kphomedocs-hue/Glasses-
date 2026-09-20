package com.parkarsite.g6a;

import java.io.*;
import java.nio.file.*;
import java.time.ZoneId;
import java.util.*;

public final class CoreSelfTest {
    public static void main(String[] args) throws Exception {
        Path tmp=Files.createTempDirectory("g6a-core-");
        File root=tmp.toFile();
        try{
            testOpaqueIdentity();
            testNumbering();
            testCommitAndRestartDedup(root);
            testFailedValidationLeavesNoFinal(root);
            testStalePartRecovery(root);
            testCrashWindowRecovery(root);
            testCoordinatorOpaqueDedup(root);
            testCoordinatorRejectsBadJpeg(root);
            System.out.println("G6A CORE SELFTEST: PASS");
        }finally{
            deleteTree(tmp);
        }
    }

    static void testOpaqueIdentity(){
        String a=OpaqueIdentity.sha256("private/path/a.jpg");
        String b=OpaqueIdentity.sha256("private/path/a.jpg");
        if(!a.equals(b)||!OpaqueIdentity.isOpaqueId(a)||a.contains("private")) fail("opaque identity");
    }

    static void testNumbering(){
        int n=NumberingPolicy.nextNumber(List.of("0001.jpg","0002.opus","0004.mp4","note.txt"));
        if(n!=5) fail("shared daily counter");
        if(!"0005.jpg".equals(NumberingPolicy.fileName(5,MediaKind.JPG))) fail("filename");
    }

    static void testCommitAndRestartDedup(File root) throws Exception {
        ImportLedger ledger=new ImportLedger(root);
        FileArchive archive=new FileArchive(root,ZoneId.of("UTC"),ledger);
        String id=OpaqueIdentity.sha256("secret/new-photo.jpg");
        long t=1767225600000L;
        byte[] jpeg=new byte[]{(byte)0xff,(byte)0xd8,(byte)0xff,1,2,3,(byte)0xff,(byte)0xd9};
        ImportRecord r=archive.importOne(id,MediaKind.JPG,t,
                out->out.write(jpeg),
                (file,bytes)->{byte[] x=Files.readAllBytes(file.toPath());if(x.length!=bytes||x[0]!=(byte)0xff||x[x.length-2]!=(byte)0xff||x[x.length-1]!=(byte)0xd9)throw new IOException("bad jpeg");});
        if(ledger.size()!=1||!new File(root,r.localRelativePath()).isFile()) fail("commit");

        ImportLedger restarted=new ImportLedger(root);
        FileArchive archive2=new FileArchive(root,ZoneId.of("UTC"),restarted);
        ImportRecord again=archive2.importOne(id,MediaKind.JPG,t,
                out->{throw new IOException("must not redownload duplicate");},
                (f,b)->{throw new IOException("must not revalidate duplicate");});
        if(!again.localRelativePath().equals(r.localRelativePath())||restarted.size()!=1) fail("restart dedup");
    }

    static void testFailedValidationLeavesNoFinal(File root) throws Exception {
        ImportLedger ledger=new ImportLedger(root);
        FileArchive archive=new FileArchive(root,ZoneId.of("UTC"),ledger);
        String id=OpaqueIdentity.sha256("secret/bad.jpg");
        int before=ledger.size();
        try{
            archive.importOne(id,MediaKind.JPG,1767225600000L,out->out.write(new byte[]{1,2,3}),
                    (f,b)->{throw new IOException("reject");});
            fail("validation should fail");
        }catch(IOException expected){}
        if(ledger.size()!=before) fail("failed item entered ledger");
        if(findBySuffix(root,".part")>0) fail("part remains after failure");
    }

    static void testCoordinatorOpaqueDedup(File root) throws Exception {
        ImportLedger ledger=new ImportLedger(root);
        FileArchive archive=new FileArchive(root,ZoneId.of("UTC"),ledger);
        SingleItemImportCoordinator c=new SingleItemImportCoordinator(archive,ledger);
        String privateIdentity="secret/catalog/path/new-photo.jpg";
        byte[] jpeg=new byte[]{(byte)0xff,(byte)0xd8,(byte)0xff,1,2,3,(byte)0xff,(byte)0xd9};
        int before=ledger.size();
        ImportRecord first=c.importNewJpg(privateIdentity,1767225600000L,out->out.write(jpeg));
        if(ledger.size()!=before+1)fail("coordinator commit");
        String ledgerText=Files.readString(new File(root,".g6a_imported.tsv").toPath());
        if(ledgerText.contains(privateIdentity)||ledgerText.contains("new-photo.jpg"))fail("private identity leaked to ledger");

        ImportRecord second=c.importNewJpg(privateIdentity,1767225600000L,
                out->{throw new IOException("duplicate should not stream");});
        if(!second.opaqueId().equals(first.opaqueId())||!second.localRelativePath().equals(first.localRelativePath()))fail("coordinator dedup");
    }

    static void testCoordinatorRejectsBadJpeg(File root) throws Exception {
        ImportLedger ledger=new ImportLedger(root);
        FileArchive archive=new FileArchive(root,ZoneId.of("UTC"),ledger);
        SingleItemImportCoordinator c=new SingleItemImportCoordinator(archive,ledger);
        int before=ledger.size();
        try{
            c.importNewJpg("secret/catalog/path/bad.jpg",1767225600000L,out->out.write(new byte[]{1,2,3,4,5,6}));
            fail("bad jpeg accepted");
        }catch(IOException expected){}
        if(ledger.size()!=before)fail("bad jpeg ledger entry");
        if(findBySuffix(root,".part")>0)fail("bad jpeg part remains");
    }

    static void testCrashWindowRecovery(File root) throws Exception {
        File day=new File(root,"2099-02-02"); if(!day.mkdirs()&&!day.isDirectory())fail("mkdir recovery");
        String opaque=OpaqueIdentity.sha256("private/recovery.jpg");
        File finalFile=new File(day,"0001.jpg");
        Files.write(finalFile.toPath(),new byte[]{(byte)0xff,(byte)0xd8,(byte)0xff,1,(byte)0xff,(byte)0xd9});
        File sidecar=new File(day,".0001.jpg.source");
        Files.writeString(sidecar.toPath(),opaque+"\n");
        ImportLedger recovered=new ImportLedger(root);
        if(!recovered.contains(opaque))fail("crash-window sidecar recovery");
        int before=recovered.size();
        FileArchive archive=new FileArchive(root,ZoneId.of("UTC"),recovered);
        ImportRecord again=archive.importOne(opaque,MediaKind.JPG,1767225600000L,
                out->{throw new IOException("must not redownload recovered duplicate");},
                (f,b)->{throw new IOException("must not validate recovered duplicate");});
        if(recovered.size()!=before||!again.localRelativePath().equals("2099-02-02/0001.jpg")) fail("recovered dedup");
    }

    static void testStalePartRecovery(File root) throws Exception {
        File d=new File(root,"2099-01-01"); if(!d.mkdirs()&&!d.isDirectory())fail("mkdir");
        File p=new File(d,"9999.jpg.part"); Files.write(p.toPath(),new byte[]{1});
        if(FileArchive.removeStaleParts(root)!=1||p.exists()) fail("stale part recovery");
    }

    static int findBySuffix(File root,String suffix){
        int n=0; File[] files=root.listFiles(); if(files==null)return 0;
        for(File f:files){if(f.isDirectory())n+=findBySuffix(f,suffix);else if(f.getName().endsWith(suffix))n++;}
        return n;
    }

    static void deleteTree(Path p)throws IOException{
        if(!Files.exists(p))return;
        try(var walk=Files.walk(p)){walk.sorted(Comparator.reverseOrder()).forEach(x->{try{Files.deleteIfExists(x);}catch(IOException ignored){}});}
    }

    static void fail(String m){throw new AssertionError(m);}
}
