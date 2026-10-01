# G6A v0.6.7 Post-GET Transition Observer — PHYSICAL CLASSIFICATION

Generated: 2026-10-01T22:59:39+0530

## Exact build attribution

Physical report fields:
- report title: `K G1 G6A POST-GET INVENTORY TRANSITION OBSERVER REPORT`
- reported App version line: `0.6.6` **(known source-header defect)**
- Build commit: `cb90f25b0bea9cfe308a1d79d9f4235c7f2b21fe`
- Build run: `36898159938`
- Build attempt: `1`

Promoted exact v0.6.7 build:
- Package ID: `com.parkarsite.g6aobserver67`
- APK SHA-256: `b152a20f049d8d33e514b177d3abcb8a01dad00677a046ea58a4a8e5de9dd7ab`
- Exact source ZIP SHA-256: `f3ab19b2bdcd390319f3ee7e4659742cc3406417304fb7f29dbc2166ea164092`

### Header defect

Exact source at build commit `cb90f25b0bea9cfe308a1d79d9f4235c7f2b21fe` contains:
`append("App version: 0.6.6");`

Therefore the physical report's `App version: 0.6.6` line is a known hard-coded report-header bug in the exact v0.6.7 source. It is **not** evidence that the wrong APK was run.

The build commit/run/attempt and package/build archive uniquely identify the physical run as the promoted v0.6.7 candidate.

## Physical result

**COMPLETE — PERSISTENT CHANGE: DOWNLOADED NEW JPG ABSENT AFTER FRESH POST-GET RECONNECT.**

### Baseline
- inventory: 11 images / 0 videos / 1 recording
- catalog: 12 safe entries = 11 JPG + 1 OPUS
- BLE/catalog parity: true
- baseline exit confirmation: complete
- P2P group absence: verified

### One physical capture
- passive capture event: 12 images / 0 videos / 1 recording
- exact +1: true
- active inventory confirmation: 12 / 0 / 1
- post-capture catalog: 13 safe entries = 12 JPG + 1 OPUS
- all 11 baseline JPG identities retained
- exactly one new JPG identity
- no baseline safe identity lost
- post-capture exit + P2P cleanup verified

### Pre-GET retention
- fresh inventory: 12 / 0 / 1
- catalog: 12 JPG + 1 OPUS
- full safe identity set retained exactly
- JPG identity set retained exactly
- new JPG still present
- baseline JPG missing: 0
- exact new JPG resolved from current catalog by opaque identity

### Exactly one media GET
- HTTP 200
- declared Content-Length: 599645
- downloaded bytes: 599645
- Content-Length consistency: PASS
- 32 MiB cap: PASS
- JPEG SOI: PASS
- JPEG EOI: PASS
- temporary app-cache file created and deleted
- persistent import: none
- persistent ledger: none
- media GET count: exactly 1

### Immediate post-GET exit inventory
- valid post-exit `0x73/0x01`
- pre-GET current snapshot: 12 / 0 / 1
- observed post-GET exit inventory: **11 / 0 / 1**
- image delta: **-1**
- configFileType=1
- onlySupportApImport=false
- P2P group absence: verified

### Fresh post-GET reconnect
- active inventory: **11 / 0 / 1**
- fresh catalog: **11 JPG + 1 OPUS**
- catalog bytes: 265
- BLE/catalog full parity: true
- inventory equals pre-GET state: false
- catalog counts equal pre-GET state: false
- full safe identity set equals pre-GET state: false
- JPG identity set equals pre-GET state: false
- **downloaded new JPG identity still present: false**
- **baseline JPG identities missing: 0**

Therefore the only JPG removed from the remote catalog was the exact one that had just been downloaded.

### Final exit/cleanup
- exit write callback: success
- valid post-exit `0x73/0x01`
- post-exit inventory matched the 11-image post-GET snapshot
- P2P group absence verified

## Exact bounded totals

- media-count queries: 4
- P2P enter writes: 4
- transfer-exit writes: 4
- catalog GET requests: 4
- media-file GET requests: 1
- total HTTP GET requests: 5
- exact bounded operation totals: true
- persistent import/ledger: none
- glasses explicit mutation/deletion commands: 0

## Interpretation

This physically establishes the transfer semantics for the tested AIMB-G1 path:

1. A newly captured JPG remains remotely present through a verified reconnect **before** media download.
2. Exactly one successful GET of that JPG is followed by an immediate inventory decrement of exactly one image.
3. After a fresh reconnect, the inventory remains decremented by one.
4. The remote catalog contains one fewer JPG.
5. All baseline JPG identities remain.
6. The exact downloaded JPG identity is absent.

So, under the tested protocol path, **a successful media GET consumes/removes the downloaded JPG from the glasses-side inventory/catalog** even though the app sends no explicit delete/mutation command.

This explains the earlier v0.6.2 restart mismatch: an imported JPG should not be expected to remain available under the same remote identity after a successful transfer.

The previous dedup design assumption—"restart should find the same imported JPG still in the remote catalog"—was invalid for this transfer behavior.

## G6A consequence

The remaining G6A design should treat a successful validated import as a transfer/consumption event:
- persist the local import atomically;
- persist an opaque local receipt/ledger atomically after the local file is durable;
- do **not** require the consumed remote identity to reappear on restart;
- on restart, dedup must prevent re-import based on durable local transfer state, while the remote catalog should naturally no longer contain successfully consumed items.

## Gate status

- no-capture stability: PASS
- exact +1 capture: PASS
- pre-download remote retention: PASS
- one media GET/JPEG validation: PASS
- post-GET remote consumption semantics: **PHYSICALLY PROVEN**
- persistent local import + atomic receipt: still to prove
- restart-safe no-redownload with consumed remote item absent: still to prove
- G6A overall: OPEN
- G6B: BLOCKED
