# G6A v0.6.5 Single-Capture Catalog Retention — PHYSICAL PASS

Generated: 2026-10-01T22:12:52+0530

## Exact build provenance

- App version: `0.6.5`
- Build commit: `598e9aed94546bff2c9b018114932902aca67e05`
- Build run: `36893102299`
- Build attempt: `1`
- APK SHA-256: `985edbcc5a8a7b035cd50c71771c7a51b9f166c9d6a827972b2f4b13423c477f`

Provenance matches the only promoted v0.6.5 physical candidate.

## Result

**PHYSICAL PASS — exactly one captured JPG persisted across a verified reconnect with zero media downloads.**

This is a diagnostic PASS. G6A overall remains open.

## Baseline

- BLE inventory: images=10, videos=0, recordings=1
- catalog: 11 safe entries
- catalog media: JPG=10, MP4=0, OPUS=1
- BLE/catalog full media parity=true
- baseline opaque JPG identities retained in memory
- baseline opaque full-safe identities retained in memory
- media-file GETs=0

### Baseline exit/cleanup
- exit write callback=SUCCESS
- generic exit-time 0x41 ignored
- post-exit 0x73/0x01 inventory=VALID
- post-exit inventory matched baseline
- P2P group absence verified

## Capture visibility

- app capture command: none
- user instruction: exactly one physical photo
- passive inventory event #1:
  - images=11
  - videos=0
  - recordings=1
- passive capture visibility: EXACT +1
- elapsed to visibility: 15091 ms
- active post-capture inventory independently confirmed images=11
- video/recording counts unchanged
- configFileType=1
- onlySupportApImport=false

## Post-capture catalog

- HTTP 200
- catalog bytes=265
- safe entries=12
- JPG=11, MP4=0, OPUS=1
- duplicate entries=0
- unsafe entries=0
- whitespace-altered lines=0
- full BLE/catalog parity=true

### Exact set delta
- baseline JPG identities retained=10/10
- baseline JPG identities missing=0
- new JPG identities=1
- baseline safe identities retained=11/11
- baseline safe identities missing=0
- unexpected safe identities=1
- exact +1 safe entry and +1 JPG only=true
- exact single new JPG catalog delta=true
- new JPG opaque identity retained in memory for reconnect check

### Post-capture exit/cleanup
- exit write callback=SUCCESS
- generic exit-time 0x41 ignored
- post-exit 0x73/0x01 inventory=VALID
- post-exit inventory matched current snapshot
- P2P group absence verified
- media-file GETs=0

## Retention reconnect

After 5000 ms:
- fresh BLE session
- inventory: images=11, videos=0, recordings=1
- inventory matched post-capture counts=true
- fresh P2P/catalog cycle
- HTTP 200
- catalog bytes=265
- safe entries=12
- JPG=11, MP4=0, OPUS=1
- BLE/catalog full parity=true

### Retention comparison
- inventory counts retained=true
- catalog media counts retained=true
- full safe identity set retained exactly=true
- JPG identity set retained exactly=true
- new JPG identity still present=true
- baseline JPG identities missing after reconnect=0
- retention BLE/catalog full media parity=true

Classification:
`PASS — EXACT +1 JPG PERSISTED ACROSS VERIFIED RECONNECT WITH ZERO MEDIA DOWNLOADS`

## Exact bounded totals

- capture action issued by app=NO
- physical capture requested=EXACTLY ONE
- passive exact +1 capture visibility observed=true
- capture-watch inventory events=1
- media-count queries=3
- P2P enter writes=3
- transfer-exit writes=3
- catalog GET requests=3
- media-file GET requests=0
- total HTTP GET requests=3
- exact bounded operation totals=true
- verified exit/group cleanup completed for baseline, post-capture and retention stages=YES
- remote filename/path values logged/persisted=NO
- glasses file mutation/deletion=0

## Interpretation

This run establishes that, under the tested conditions:

1. One intended physical capture produced exactly one visible BLE image-count increase.
2. The post-capture catalog gained exactly one JPG and one safe entry.
3. No baseline JPG or other safe catalog identity disappeared.
4. The one new JPG remained present across a verified transfer exit, verified P2P-group absence, 5-second delay, fresh BLE/P2P session, and fresh catalog GET.
5. No media file was downloaded before or during this retention proof.

Combined with the prior v0.6.4.2 physical PASS, the current evidence shows:
- short-window idle/no-capture catalog stability: proven;
- exact +1 single-capture catalog delta: proven in this run;
- capture-created JPG retention across a verified reconnect before download: proven.

The earlier v0.6.3 8→10 ambiguity remains historical and unexplained, but it was not reproduced here.

The earlier v0.6.2 post-transfer/restart reversion therefore cannot be attributed to ordinary idle churn or to simple capture+reconnect behavior based on the current evidence. The next unresolved boundary is whether **the media-file GET itself, or later import/restart handling, changes remote catalog retention semantics**.

## Gate status

- G6A no-capture stability sub-boundary: PASS
- G6A single-capture pre-download retention sub-boundary: PASS
- G6A overall: OPEN
- G6B: BLOCKED

## Raw report

```text
K G1 G6A SINGLE-CAPTURE CATALOG RETENTION REPORT
Generated: 2026-10-01T22:12:52+0530
App version: 0.6.5
Build commit: 598e9aed94546bff2c9b018114932902aca67e05
Build run: 36893102299
Build attempt: 1
Mode: BASELINE -> ONE PHYSICAL CAPTURE -> POST-CAPTURE CATALOG -> VERIFIED RECONNECT RETENTION
Purpose: isolate capture/catalog retention semantics before any media download
Instruction: DO NOT TAKE A PHOTO until the app explicitly reports ARMED
Capture action issued by app: NO
Intended physical captures: EXACTLY ONE
Capture watch window: 60000 ms
Retention reconnect delay: 5000 ms
Inventory writes total allowed: EXACTLY THREE (baseline + post-capture confirmation + retention)
P2P enter writes total allowed: EXACTLY THREE
Transfer exit writes total allowed: EXACTLY THREE
Catalog HTTP allowed: EXACTLY THREE GET /files/media.config
Media-file GET allowed: 0
HTTP redirects: DISABLED
HTTP retry/resume/Range: DISABLED
Catalog response cap: 65536 bytes
Peer selection: EXACT CASE-SENSITIVE BLE-REPORTED P2P NAME ONLY
Credential logging/persistence: DISABLED
Remote filename/path logging/persistence: DISABLED
Catalog identity comparison: OPAQUE SHA-256 IN MEMORY ONLY
Per-file identity/hash tokens in report: DISABLED
Glasses file mutation/deletion: NOT IMPLEMENTED

BASELINE SNAPSHOT
Image count: 10
Video count: 0
Recording count: 1
Catalog: 11 safe entries; .jpg=10, .opus=1
BLE/catalog full media-count parity: true
Baseline exit confirmation: COMPLETE
P2P group absence verified before capture: YES

CAPTURE WATCH ARMED
Instruction: TAKE EXACTLY ONE PHOTO NOW
Capture-watch inventory event #1: images=11, videos=0, recordings=1
Passive capture visibility: EXACT +1
Capture visibility elapsed: 15091 ms
Active post-capture inventory confirms exact +1 image: true

POST-CAPTURE CATALOG
Catalog: 12 safe entries; .jpg=11, .opus=1
Exact +1 image with other BLE counts unchanged: true
Exact +1 safe entry and +1 JPG only: true
Baseline JPG identities retained: 10/10
Baseline JPG identities missing: 0
New JPG identities: 1
Baseline safe identities retained: 11/11
Baseline safe identities missing: 0
Unexpected safe identities: 1
Post-capture BLE/catalog full media parity: true
Exact single new JPG catalog delta: true
Post-capture exit confirmation: COMPLETE
P2P group absence verified before retention reconnect: YES
Media-file GET requests: 0

RETENTION SNAPSHOT
Image count: 11
Video count: 0
Recording count: 1
Retention inventory matches post-capture counts: true
Catalog: 12 safe entries; .jpg=11, .opus=1
Inventory counts retained: true
Catalog media counts retained: true
Full safe identity set retained exactly: true
JPG identity set retained exactly: true
New JPG identity still present: true
Baseline JPG identities missing after reconnect: 0
Retention BLE/catalog full media parity: true
G6A SINGLE-CAPTURE RETENTION DIAGNOSTIC: PASS — EXACT +1 JPG PERSISTED ACROSS VERIFIED RECONNECT WITH ZERO MEDIA DOWNLOADS

SUMMARY
Capture action issued by app: NO
Physical capture requested by app: EXACTLY ONE
Passive exact +1 capture visibility observed: true
Capture-watch inventory events: 1
Media-count queries: 3
P2P enter writes: 3
Transfer-exit writes: 3
Catalog GET requests: 3
Media-file GET requests: 0
Total HTTP GET requests: 3
Exact bounded operation totals: true
Verified exit/group cleanup completed for baseline, post-capture, and retention snapshots: YES
Remote filename/path values logged/persisted: NO
Glasses file mutation/deletion: 0
G6A SINGLE-CAPTURE RETENTION RESULT: PASS — EXACT +1 JPG PERSISTED ACROSS VERIFIED RECONNECT WITH ZERO MEDIA DOWNLOADS
END REPORT
```
