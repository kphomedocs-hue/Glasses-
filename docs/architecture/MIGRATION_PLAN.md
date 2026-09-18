# Migration Plan: Legacy Site Capture → Modular Production App

Do not rewrite the proven storage logic while protocol work is still unresolved. Migrate by responsibility after the first physical gates.

## Phase A — preserve
Complete:
- Discovery v0.1 frozen and reproducible.
- Site Capture v0.4 legacy source preserved.
- protocol evidence documented separately.
- artifact hashes/provenance recorded.

## Phase B — protocol confirmation
- run read-only physical discovery,
- confirm service/characteristic profile,
- define response parser,
- decide whether initialization/time sync is required.

## Phase C — extract pure modules from v0.4

### media-storage
Migrate:
- `NumberingPolicy`
- `FileMediaArchive`
- extension allow-list
- partial-file transaction pattern
- provenance sidecars/integrity checks

Improve:
- replace hidden sidecar-only state with a formal storage metadata abstraction,
- preserve original capture timestamp explicitly,
- support user-selected SAF destination in production.

### sync-ledger
Migrate:
- dedup identity concept,
- crash-window recovery.

Improve:
- Room-backed state for production,
- explicit `PENDING / DOWNLOADING / COMPLETE / FAILED`,
- remote ID + size + capture time + optional content hash.

### media-transfer
Migrate:
- streaming OutputStream contract.

Improve:
- resumable download contract,
- declared-length verification,
- bounded retry/backoff,
- local-private-network enforcement,
- redirect rejection.

### device-session
Replace old `G1Transport` with smaller capabilities:

```text
GlassesDiscovery
GlassesControlSession
GlassesNetworkSession
RemoteMediaCatalog
RemoteMediaDownloader
```

This prevents BLE connection, Wi-Fi setup and file download concerns from collapsing into one class.

## Phase D — production coordinator

`SyncCoordinator` owns the workflow:

```text
detect device
→ establish control session
→ enter media mode
→ establish local network
→ list media
→ compare ledger
→ stream new media to storage
→ verify
→ mark complete
→ close sessions
```

## Phase E — minimal production UI

```text
AIMB-G1
Connected
Last sync 09:42
Today 27 files
Storage 18.4 GB free
[Open Folder]
```

Diagnostics remain accessible separately and are not mixed into the normal user experience.
