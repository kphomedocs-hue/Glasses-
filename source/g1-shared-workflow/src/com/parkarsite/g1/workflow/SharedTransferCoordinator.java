package com.parkarsite.g1.workflow;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * V4 Step 07: one pure Java baseline -> exact JPG delta -> durable import -> teardown coordinator.
 * Production construction is READ ONLY and can never issue a media read.
 * Package-private fixture capability is supplied ONLY from host selftest source;
 * never package such an adapter in an Android application.
 * No BLE/P2P/HTTP/Android implementation is contained here.
 */
public final class SharedTransferCoordinator {
    public enum Phase { NEW, BASELINED, SELECTED, FIXTURE_STORED, DENIED }
    public enum Verdict {
        BASELINE_ACCEPTED, SINGLE_JPG_SELECTED, SYNTHETIC_RECEIPT_COMMITTED,
        SYNTHETIC_CLOSED, BLOCKED_PRESERVATION, BLOCKED_EVIDENCE,
        BLOCKED_CATALOG, BLOCKED_REPLAY, REMOTE_STATE_UNCERTAIN, EXIT_UNVERIFIED
    }
    /** A test-only callback, intentionally absent from the public read-only constructor. */
    interface HostFixturePermit { boolean syntheticHostOnly(); }
    /**
     * Durable adapter MUST mark FETCH_MAY_HAVE_STARTED on disk before calling Source,
     * must not retry, must validate and fsync the final file and receipt.
     */
    public interface DurableOneShotImport {
        void importOnce(SharedTransferContracts.MediaDescriptor item,
                        SharedTransferContracts.OneShotByteSource source) throws Exception;
        boolean committedAndIntact(String opaqueIdentity) throws Exception;
    }

    private final SharedObservationController observation;
    private final SharedTransferContracts.CatalogSource catalog;
    private final HostFixturePermit hostFixture;
    private Phase phase=Phase.NEW;
    private Verdict verdict=Verdict.BLOCKED_EVIDENCE;
    private Set<String> baseline;
    private SharedTransferContracts.MediaDescriptor selected;
    private boolean attempted;

    /** Shipping construction: no fixture capability, no transfer. */
    public SharedTransferCoordinator(SharedObservationController observation,
                                     SharedTransferContracts.CatalogSource catalog) {
        this(observation,catalog,null);
    }
    /** Host selftest injection only. Not a production API. */
    SharedTransferCoordinator(SharedObservationController observation,
                              SharedTransferContracts.CatalogSource catalog,
                              HostFixturePermit hostFixture) {
        this.observation=Objects.requireNonNull(observation);
        this.catalog=Objects.requireNonNull(catalog);
        this.hostFixture=hostFixture;
    }

    public synchronized Phase phase(){return phase;}
    public synchronized Verdict verdict(){return verdict;}
    public synchronized boolean remoteMediaGetAuthorized(){return false;}
    public synchronized boolean physicalConnectAuthorized(){return false;}

    private boolean observationReady() {
        return observation.stage()==SharedTransferContracts.Stage.OBSERVING
            && observation.candidateObserved();
    }
    private static Set<String> checked(SharedTransferContracts.CatalogSnapshot snapshot) {
        if(snapshot==null || snapshot.media()==null || snapshot.media().size()>1024)
            throw new IllegalArgumentException("Invalid synthetic catalog");
        Set<String> identities=new HashSet<>();
        for(var item:snapshot.media()) {
            if(item==null || !item.opaqueIdentity().matches("[0-9a-f]{64}")
                    || !identities.add(item.opaqueIdentity()))
                throw new IllegalArgumentException("Duplicate/unsafe opaque identity");
        }
        return identities;
    }
    private static int jpgs(List<SharedTransferContracts.MediaDescriptor> items) {
        int count=0;
        for(var item:items) if(item.kind()==SharedTransferContracts.MediaKind.JPG)count++;
        return count;
    }
    private Verdict deny(Verdict reason) {
        phase=Phase.DENIED;
        verdict=reason;
        observation.finishObservation();
        return reason;
    }

    public synchronized Verdict observeBaseline(int inventoryJpg) {
        if(phase!=Phase.NEW)return deny(Verdict.BLOCKED_REPLAY);
        if(!observationReady())return deny(Verdict.BLOCKED_EVIDENCE);
        try {
            var snapshot=catalog.readCatalog();
            var identities=checked(snapshot);
            if(inventoryJpg<0 || jpgs(snapshot.media())!=inventoryJpg)
                return deny(Verdict.BLOCKED_CATALOG);
            baseline=Set.copyOf(identities);
            phase=Phase.BASELINED;
            verdict=Verdict.BASELINE_ACCEPTED;
            return verdict;
        } catch(Exception failure) {
            return deny(Verdict.BLOCKED_CATALOG);
        }
    }

    public synchronized Verdict observeSingleNewJpg(int inventoryJpg) {
        if(phase!=Phase.BASELINED)return deny(Verdict.BLOCKED_REPLAY);
        if(!observationReady())return deny(Verdict.BLOCKED_EVIDENCE);
        try {
            var snapshot=catalog.readCatalog();
            var identities=checked(snapshot);
            if(inventoryJpg<0 || jpgs(snapshot.media())!=inventoryJpg
                    || !identities.containsAll(baseline)
                    || identities.size()!=baseline.size()+1)
                return deny(Verdict.BLOCKED_CATALOG);
            SharedTransferContracts.MediaDescriptor found=null;
            for(var item:snapshot.media())if(!baseline.contains(item.opaqueIdentity())) {
                if(found!=null || item.kind()!=SharedTransferContracts.MediaKind.JPG)
                    return deny(Verdict.BLOCKED_CATALOG);
                found=item;
            }
            if(found==null)return deny(Verdict.BLOCKED_CATALOG);
            selected=found;
            phase=Phase.SELECTED;
            verdict=Verdict.SINGLE_JPG_SELECTED;
            return verdict;
        } catch(Exception failure) {
            return deny(Verdict.BLOCKED_CATALOG);
        }
    }

    /**
     * The only executable read branch is gated by an injected host-selftest permit.
     * The public shipping constructor always denies, including for trusted-looking peers.
     * A failed source/store never causes an automatic second invocation.
     */
    public synchronized Verdict importSyntheticFixture(
        SharedTransferContracts.OneShotByteSource source, DurableOneShotImport store) {
        Objects.requireNonNull(source);
        Objects.requireNonNull(store);
        if(attempted || phase!=Phase.SELECTED) return deny(Verdict.BLOCKED_REPLAY);
        attempted=true;
        if(!observationReady()) return deny(Verdict.BLOCKED_EVIDENCE);
        if(hostFixture==null || !hostFixture.syntheticHostOnly()
                || observation.remoteMediaGetAuthorized()
                || observation.physicalConnectAuthorized())
            return deny(Verdict.BLOCKED_PRESERVATION);
        try {
            if(store.committedAndIntact(selected.opaqueIdentity()))
                return deny(Verdict.BLOCKED_REPLAY);
            store.importOnce(selected,source);
            if(!store.committedAndIntact(selected.opaqueIdentity()))
                return deny(Verdict.REMOTE_STATE_UNCERTAIN);
            phase=Phase.FIXTURE_STORED;
            verdict=Verdict.SYNTHETIC_RECEIPT_COMMITTED;
            return verdict;
        } catch(Exception failure) {
            return deny(Verdict.REMOTE_STATE_UNCERTAIN);
        } finally {
            observation.finishObservation();
        }
    }

    /** A durable receipt never substitutes for verified EXIT and group absence. */
    public synchronized Verdict completeAfterExit() {
        if(phase!=Phase.FIXTURE_STORED)return verdict;
        if(observation.stage()==SharedTransferContracts.Stage.CLOSED
                && observation.result()==SharedObservationController.Result.CLOSED_OBSERVATION_ONLY) {
            verdict=Verdict.SYNTHETIC_CLOSED;
            return verdict;
        }
        verdict=Verdict.EXIT_UNVERIFIED;
        return verdict;
    }
}
