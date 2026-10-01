# G6A v0.6.6 Single-JPG GET — REPRODUCED POST-GET INVENTORY TRANSITION

Generated report supplied: 2026-10-01T22:36:19+0530

## Exact build provenance

- App version: `0.6.6`
- Build commit: `1ea64b26f311e6b2f18be536e1548766dabe8aa9`
- Build run: `36895589975`
- Build attempt: `1`
- APK SHA-256: `29920782f16e5ef5785ef62d835622b3dbff0cb1b53f0f5848306f13cf76343d`

Provenance matches the sole promoted v0.6.6 physical candidate exactly.

## Operator repeat note

The operator states that this v0.6.6 physical test was performed **two times** and the same stopping condition occurred.

Only one complete raw report was supplied in the current chat, so only that report is treated as exact raw evidence. The second run is recorded as an operator-reported reproduction, not as a second independently parsed raw report.

## Physical sequence observed

### Baseline
- BLE inventory: 11 images / 0 videos / 1 recording.
- Catalog: 12 safe entries = 11 JPG + 1 OPUS.
- BLE/catalog parity: true.
- Verified exit confirmation.
- P2P group absence verified.

### One physical capture
- first relevant passive capture inventory: 12 images / 0 videos / 1 recording.
- exact +1 visibility: true.
- active inventory independently confirmed 12 images.
- post-capture catalog: 13 safe entries = 12 JPG + 1 OPUS.
- all 11 baseline JPG identities retained.
- exactly one new JPG identity.
- all 12 baseline safe identities retained.
- exactly one unexpected/new safe identity.
- verified exit + group absence.

### Pre-GET retention
- fresh reconnect inventory: 12 / 0 / 1.
- catalog remained 12 JPG + 1 OPUS.
- full safe identity set equal to post-capture set.
- JPG identity set equal.
- new JPG still present.
- baseline JPG missing: 0.
- exact new JPG resolved from CURRENT catalog by opaque identity.

### Exactly one disposable media GET
- HTTP status: 200.
- declared Content-Length: 797965.
- downloaded bytes: 797965.
- Content-Length consistency: PASS.
- media cap: PASS.
- JPEG SOI: PASS.
- JPEG EOI: PASS.
- app-private temporary file created.
- temporary file deleted successfully.
- persistent import: none.
- ledger update: none.
- media GET count: exactly 1.

### Immediate post-GET exit observation
- exit write callback: SUCCESS.
- generic post-exit 0x41 ignored.
- valid post-exit 0x73/0x01 inventory observed.
- **post-exit inventory did NOT match the pre-GET/current snapshot.**
- P2P group absence after cleanup: verified.
- diagnostic then stopped before the planned post-GET fresh reconnect/catalog read.

## Exact bounded totals at stop

- media-count queries: 3
- P2P enters: 3
- transfer exits: 3
- catalog GETs: 3
- media-file GETs: 1
- total HTTP GETs: 4
- media validated: true
- temporary file cleanup: true
- glasses mutation/deletion: 0

## Interpretation

This is **not** evidence yet that the remote JPG was deleted or that the catalog lost an entry.

It is evidence that, in the supplied run, a valid `0x73/0x01` inventory event observed after the one validated JPG GET did not equal the 12-image pre-GET snapshot.

That mismatch did not occur during the equivalent verified exits in v0.6.5, where there was no media GET.

Because the operator reports the same v0.6.6 outcome twice, the post-GET inventory transition is currently a **reproduced physical signal**.

The current v0.6.6 diagnostic stops too early to classify the transition because it treats post-GET count equality as part of exit-separation proof. After GET, count equality is itself an experimental variable.

## Required next diagnostic correction

Do not proceed to persistent import/ledger yet.

The next diagnostic must:
1. preserve the proven baseline → +1 capture → pre-GET retention chain;
2. perform exactly one GET of the exact new JPG;
3. after the exit-write callback, accept a valid post-GET `0x73/0x01` as an observation even if counts changed;
4. report the exact post-GET image/video/recording/config counts;
5. verify P2P group absence;
6. perform a fresh BLE inventory query after cleanup/reconnect boundary;
7. perform a fresh P2P catalog GET;
8. compare the resulting inventory/catalog identities to the pre-GET snapshot.

Only that follow-up can distinguish:
- transient exit-time inventory event;
- permanent image-count decrement;
- catalog removal of downloaded JPG;
- identity rewrite;
- some other inventory/catalog transition.

## Gate status

- no-capture stability: PASS
- exact +1 capture/catalog delta: PASS
- pre-download retention: PASS
- one media GET transport/JPEG validation: PASS
- post-GET inventory unchanged: **NOT TRUE in observed run**
- post-GET remote catalog retention: **NOT YET OBSERVED**
- G6A overall: OPEN
- G6B: BLOCKED
