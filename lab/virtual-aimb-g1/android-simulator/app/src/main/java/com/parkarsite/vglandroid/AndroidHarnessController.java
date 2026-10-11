package com.parkarsite.vglandroid;

import android.content.Context;
import com.parkarsite.g1.virtual.*;
import com.parkarsite.g6a.recovery.RecoveryImportStore;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.ZoneId;
import java.util.HexFormat;
import java.util.List;

/**
 * Android private-storage integration with the actual existing Java VGL code.
 * Uses ONLY an in-memory synthetic JPG marker fixture. NO network, no radio,
 * and no physical glasses Source can be injected by this surface.
 */
public final class AndroidHarnessController {
    public record Result(boolean policyPassed, boolean durablePassed, boolean recoveryPassed,
                         boolean remoteBlocked, String summary) {
        public boolean allPassed(){
            return policyPassed && durablePassed && recoveryPassed && remoteBlocked;
        }
    }
    private static final byte[] FIXTURE=new byte[]{
        (byte)0xff,(byte)0xd8,(byte)0xff,0x01,0x02,(byte)0xff,(byte)0xd9
    };
    private AndroidHarnessController(){}

    private static String syntheticOpaqueId() throws Exception {
        var bytes=MessageDigest.getInstance("SHA-256").digest(
                "VGL-ANDROID-SYNTHETIC-LOCAL-ONLY-01".getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(bytes);
    }

    public static Result run(Context context) throws Exception {
        if(context==null||context.getApplicationContext()==null)
            throw new IllegalArgumentException("Application context required");

        var baseline=VirtualLabPolicies.Catalog.parse(1,
                VirtualLabPolicies.Catalog.bytes("media/old.jpg\n"));
        var present=VirtualLabPolicies.Catalog.parse(1,
                VirtualLabPolicies.Catalog.bytes("media/new.jpg\nmedia/old.jpg\n"));
        boolean exactDelta=new VirtualLabPolicies.Capture(baseline).inspect(present,2)
                    ==VirtualLabPolicies.Decision.ALLOW;
        boolean missingOriginalRejected=new VirtualLabPolicies.Capture(baseline).inspect(
                VirtualLabPolicies.Catalog.parse(1,
                    VirtualLabPolicies.Catalog.bytes("media/new.jpg\n")),1)
                    ==VirtualLabPolicies.Decision.BLOCK;

        var clock=new VirtualClock();
        final int[] callbacks=new int[4];
        var ble=new VirtualGlassesCore.BleTransportPort(){
            public void connect(){callbacks[0]++;}
            public void subscribe(){callbacks[1]++;}
            public void enter(){callbacks[2]++;}
            public void exit(){callbacks[3]++;}
        };
        VirtualGlassesCore.PeerDiscoveryPort discovery=()->{};
        var core=new VirtualGlassesCore(clock,ble,discovery);
        core.begin(1,"AIMB-G1_TEST",String.join(":","02","00","00","00","00","08"));
        core.onGatt(true);
        core.onSubscribed(true);
        core.armObservation();
        core.onEnterWrite(true);
        core.onPeerSnapshot(List.of("TEST_020000000008"));
        core.finishObservation();
        core.onExitWrite(true,true);
        boolean boundedLifecycle=core.outcome()==VirtualGlassesCore.Outcome.SAFE_FINISH
                && callbacks[0]==1 && callbacks[1]==1
                && callbacks[2]==1 && callbacks[3]==1;

        Path path=context.getApplicationContext().getNoBackupFilesDir().toPath()
                .resolve("vgl-private-synthetic-fixture");
        var store=new RecoveryImportStore(path,ZoneId.of("Asia/Kolkata"));
        String id=syntheticOpaqueId();
        var ticket=store.prepare(id,0L);
        if(ticket.state()==RecoveryImportStore.State.PREPARED){
            // The only permitted Source is this inline constant; no HTTP object exists.
            store.execute(ticket,FIXTURE.length,out->out.write(FIXTURE),b->{});
        }
        boolean durable=store.isCommittedAndIntact(id);
        var reopened=new RecoveryImportStore(path,ZoneId.of("Asia/Kolkata"));
        boolean recovered=reopened.recover().stream().anyMatch(item->
            item.opaqueId().equals(id)
                && item.disposition()==RecoveryImportStore.Disposition.COMPLETE);
        boolean remoteBlocked=!core.connectAuthorized()&&!core.mediaGetAuthorized()
            &&!new VirtualLabPolicies.Platform().canFetch()
            &&!new VirtualLabPolicies.Group(new VirtualClock()).mayConnectRealPeer();
        boolean policy=exactDelta && missingOriginalRejected && boundedLifecycle;
        return new Result(policy,durable,recovered,remoteBlocked,
           "policy="+policy+" durable="+durable+" reopen="+recovered
           +" remote_access_blocked="+remoteBlocked);
    }
}
