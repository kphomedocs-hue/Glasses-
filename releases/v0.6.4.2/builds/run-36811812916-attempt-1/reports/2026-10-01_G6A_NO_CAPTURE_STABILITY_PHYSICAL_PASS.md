# G6A v0.6.4.2 No-Capture Catalog Stability — PHYSICAL PASS

Generated: 2026-10-01T21:51:54+0530

## Exact build provenance

- App version: `0.6.4.2`
- Build commit: `e97bfc4ac66d6de49007296fdfff311df129801d`
- Build run: `36811812916`
- Build attempt: `1`
- APK SHA-256: `550d4935f17ed6250feda603a9d2963e79aaa716dd20ed59be00124e97f4ae99`

Provenance matches the only authorized physical candidate exactly.

## Result

**PHYSICAL PASS — short-window no-capture inventory/catalog stability**

This is a diagnostic PASS only. It does not by itself close G6A.

### Snapshot A
- BLE inventory: images=10, videos=0, recordings=1
- configFileType=1
- onlySupportApImport=false
- P2P enter write callback: SUCCESS
- credential response: VALID
- enter write/credential handshake: COMPLETE
- exact BLE-reported P2P peer match: YES
- phone group owner: true
- passive glasses IPv4: 192.168.49.176
- catalog HTTP 200
- catalog bytes: 243
- safe entries: 11
- JPG=10, MP4=0, OPUS=1
- duplicate entries=0
- unsafe entries=0
- whitespace-altered lines rejected=0
- BLE/catalog full media parity=true
- media-file GETs=0

### Snapshot A exit/cleanup
- exit write start: SUCCESS
- generic exit-time 0x41: observed and correctly IGNORED for exit confirmation
- exit write callback: SUCCESS
- post-exit 0x73/0x01 inventory: VALID
- post-exit inventory matches current snapshot=true
- post-exit confirmation: COMPLETE
- removeGroup request: failed reason=2
- read-back group state: group absent
- P2P group absence verified before quiet interval=YES

### Quiet interval
- requested minimum: 30000 ms
- actual monotonic interval: 30030 ms
- no capture action issued by app
- user instruction: do not take a photo

### Snapshot B
- BLE inventory: images=10, videos=0, recordings=1
- configFileType=1
- onlySupportApImport=false
- P2P enter write callback: SUCCESS
- credential response: VALID
- enter write/credential handshake: COMPLETE
- exact BLE-reported P2P peer match: YES
- phone group owner: true
- group-owner address reported unavailable, but passive glasses IPv4 resolved to 192.168.49.176 and the bounded catalog GET succeeded
- catalog HTTP 200
- catalog bytes: 243
- safe entries: 11
- JPG=10, MP4=0, OPUS=1
- duplicate entries=0
- unsafe entries=0
- whitespace-altered lines rejected=0
- BLE/catalog full media parity=true
- media-file GETs=0

### Snapshot comparison
- inventory counts exactly equal=true
- total catalog entry count equal=true
- JPG count equal=true
- MP4 count equal=true
- OPUS count equal=true
- JPG identities retained=10/10
- JPG identities missing=0
- unexpected JPG identities=0
- all safe identities retained=11/11
- all safe identities missing=0
- unexpected safe identities=0
- JPG opaque identity sets exactly equal=true
- full safe catalog opaque identity sets exactly equal=true
- Snapshot A BLE/catalog full media parity=true
- Snapshot B BLE/catalog full media parity=true

### Snapshot B exit/cleanup
- exit write start: SUCCESS
- exit write callback: SUCCESS
- generic exit-time 0x41: observed and correctly IGNORED
- post-exit 0x73/0x01 inventory: VALID
- post-exit inventory matches current snapshot=true
- post-exit confirmation: COMPLETE
- removeGroup request: failed reason=2
- read-back group state: group absent
- P2P group absence verified after Snapshot B=YES

### Exact bounded totals
- media-count queries=2
- P2P enter writes=2
- transfer-exit writes=2
- catalog GET requests=2
- media-file GET requests=0
- total HTTP GET requests=2
- glasses mutation/deletion=0
- exact bounded operation totals=true

## Interpretation

This run proves that, for this exact red-team-corrected build and this bounded 30-second no-capture interval:

1. BLE media counts were stable.
2. Catalog membership and opaque identities were stable.
3. BLE counts and catalog media counts agreed in both snapshots.
4. The same 10-JPG state persisted across a complete exit, verified P2P-group absence, 30.03-second quiet period, reconnect, and second catalog read.
5. No media file was downloaded and no glasses file was mutated/deleted.

This materially strengthens the earlier v0.6.4 10→10 observation because the corrected build additionally proves:
- exact build provenance;
- enter write/credential handshake completion;
- post-exit inventory evidence;
- P2P group absence;
- full media-class parity;
- exact operation totals.

## What remains unresolved

This PASS does **not** explain the earlier transition from 8 images to 10 images seen around the v0.6.3 capture experiment.

It also does not prove:
- long-duration idle stability;
- stability across glasses power-cycle/reboot;
- which item disappeared in the earlier v0.6.2 post-transfer/restart reversion;
- restart-safe remote-identity dedup after a newly imported JPG.

Therefore:
- no-capture short-window stability: **PASS**
- remote spontaneous churn during the tested short idle window: **not observed**
- G6A overall: **OPEN**
- G6B: **BLOCKED**

## Raw report

```text
K G1 G6A NO-CAPTURE CATALOG STABILITY REPORT
Generated: 2026-10-01T21:51:54+0530
App version: 0.6.4.2
Build commit: e97bfc4ac66d6de49007296fdfff311df129801d
Build run: 36811812916
Build attempt: 1
Mode: READ-ONLY TWO-SNAPSHOT NO-CAPTURE DIAGNOSTIC
Purpose: test whether AIMB-G1 inventory/catalog changes without a photo or media transfer
Instruction: DO NOT TAKE ANY PHOTO during the entire run
Snapshot count: EXACTLY TWO
Quiet interval between snapshots: 30000 ms
BLE writes allowed per snapshot: media-count 02 04 once + P2P enter once + transfer exit once
Catalog HTTP allowed per snapshot: EXACTLY ONE GET /files/media.config
Media-file GET allowed: 0
HTTP redirects: DISABLED
HTTP retry/resume/Range: DISABLED
Catalog response cap: 65536 bytes
Peer selection: EXACT BLE-REPORTED P2P NAME ONLY
Credential logging/persistence: DISABLED
Remote filename/path logging/persistence: DISABLED
Catalog identity comparison: OPAQUE SHA-256 IN MEMORY ONLY
Per-file identity/hash tokens in report: DISABLED
Glasses file mutation/deletion: NOT IMPLEMENTED

SNAPSHOT A
BONDED TARGET CHECK
Bonded devices total: 7
AIMB-G1-family bonded matches: 1
Bonded target name: AIMB-G1_<suffix>
Bonded target address: not logged

GATT CONNECTION
Target selection: exactly one bonded AIMB-G1-family device
Target name: AIMB-G1_<suffix>
Bluetooth address: not logged
GATT: connected
CYAN SERVICE/NOTIFY/WRITE: PRESENT
Notification subscription start: SUCCESS
Notification subscription: SUCCESS

CYAN MEDIA INVENTORY REFRESH
Command: 0x41 / 02 04
Purpose: exact Cyan album-screen media-count/config parity
No retry policy: TRUE
Media-count write start: SUCCESS
Media-count response: VALID
Image count: 10
Video count: 0
Recording count: 1
Config file type: 1
Only-support-AP-import: false
BLE characteristic write status: SUCCESS
Media-count write/response handshake: COMPLETE

ENTER P2P MODE
Command: 0x41 / 02 01 04 01
No retry policy: TRUE
ENTER write start: SUCCESS
BLE characteristic write status: SUCCESS
P2P enter write callback: SUCCESS
Transfer credential response: VALID
SSID length: 20
Password length: 9
SSID/password values: not logged
P2P enter write/credential handshake: COMPLETE

WI-FI DIRECT DISCOVERY
Peer-name match: exact BLE-reported name, in memory only
Nearby peer names/addresses: not logged
Async 0x73 event ID: 0x0B
discoverPeers start: SUCCESS
Peers observed: 1 (names/addresses not logged)
Exact glasses P2P peer match: YES

WI-FI DIRECT ASSOCIATION
Target: exact BLE-reported peer
WPS: PBC
groupOwnerIntent: 0
Peer address: not logged
connect request: SUCCESS
Async 0x73 event ID: 0x0B
P2P group formed: TRUE
Phone is group owner: true
Group-owner address: 192.168.49.1
Async 0x73 event ID: 0x08
0x73/0x08 glasses IPv4: 192.168.49.176

CYAN CATALOG READINESS
Delay before catalog GET: 1000 ms
Source parity: PictureFragment.downloadMediaConfig() / ktxRunOnUiDelay(0x03e8)
Async 0x73 event ID: 0x0B

SNAPSHOT A CATALOG
Method: GET
Path: /files/media.config
Target: passive 0x73/0x08 glasses IPv4 only
Redirects: DISABLED
Max response bytes: 65536
Media-file GET requests so far: 0
HTTP status: 200
Catalog bytes received: 243
Content-Type: text/plain
Content-Encoding: <none>
UTF-8 valid: true
BOM: NONE
Cyan parser branch: configFileType=1 / readLines()
Line-list entries: 11
Non-empty entries: 11
Relative safe entries: 11
Duplicate entries: 0
Unsafe entry count: 0
Whitespace-altered lines rejected: 0
Extension summary: .jpg=10, .opus=1
Filename/path values logged: NO
Catalog raw body logged: NO
Opaque all-entry identities in memory: 11
Opaque JPG identities in memory: 10
Per-file identity/hash tokens reported: NO
Remote filename/path values persisted/logged: NO
Media-file GET requests: 0
Snapshot A BLE/catalog media-count parity: true
Snapshot A opaque identity sets retained in memory only: YES

EXIT TRANSFER MODE
Command: 0x41 / 02 01 09
No retry policy: TRUE
EXIT write start: SUCCESS
Post-exit 0x41 frame observed: IGNORED for exit confirmation
BLE characteristic write status: SUCCESS
Exit write callback: SUCCESS
Post-exit confirmation required: valid 0x73/0x01 inventory matching current snapshot
Async 0x73 event ID: 0x01
Post-exit 0x73/0x01 inventory: VALID
Post-exit inventory matches current snapshot: true
Post-exit 0x73/0x01 confirmation: COMPLETE

LOCAL P2P CLEANUP
removeGroup request: FAILED reason=2
P2P group present after cleanup request: false

SNAPSHOT A COMPLETE
Snapshot A inventory: images=10, videos=0, recordings=1
Snapshot A total safe catalog entries: 11
Snapshot A JPG/MP4/OPUS entries: 10/0/1
Snapshot A media-file GET requests: 0
Post-exit 0x73/0x01 confirmation: YES
P2P group absence verified before quiet interval: YES

QUIET INTERVAL
Minimum requested duration: 30000 ms
Instruction: KEEP GLASSES UNTOUCHED — DO NOT TAKE ANY PHOTO

QUIET INTERVAL COMPLETE
Actual monotonic quiet interval: 30030 ms

SNAPSHOT B
BONDED TARGET CHECK
Bonded devices total: 7
AIMB-G1-family bonded matches: 1
Bonded target name: AIMB-G1_<suffix>
Bonded target address: not logged

GATT CONNECTION
Target selection: exactly one bonded AIMB-G1-family device
Target name: AIMB-G1_<suffix>
Bluetooth address: not logged
GATT: connected
CYAN SERVICE/NOTIFY/WRITE: PRESENT
Notification subscription start: SUCCESS
Notification subscription: SUCCESS

CYAN MEDIA INVENTORY REFRESH
Command: 0x41 / 02 04
Purpose: exact Cyan album-screen media-count/config parity
No retry policy: TRUE
Media-count write start: SUCCESS
Media-count response: VALID
Image count: 10
Video count: 0
Recording count: 1
Config file type: 1
Only-support-AP-import: false
BLE characteristic write status: SUCCESS
Media-count write/response handshake: COMPLETE

ENTER P2P MODE
Command: 0x41 / 02 01 04 01
No retry policy: TRUE
ENTER write start: SUCCESS
BLE characteristic write status: SUCCESS
P2P enter write callback: SUCCESS
Transfer credential response: VALID
SSID length: 20
Password length: 9
SSID/password values: not logged
P2P enter write/credential handshake: COMPLETE

WI-FI DIRECT DISCOVERY
Peer-name match: exact BLE-reported name, in memory only
Nearby peer names/addresses: not logged
Async 0x73 event ID: 0x0B
discoverPeers start: SUCCESS
Peers observed: 1 (names/addresses not logged)
Exact glasses P2P peer match: YES

WI-FI DIRECT ASSOCIATION
Target: exact BLE-reported peer
WPS: PBC
groupOwnerIntent: 0
Peer address: not logged
connect request: SUCCESS
P2P group formed: TRUE
Phone is group owner: true
Group-owner address: unavailable
Async 0x73 event ID: 0x0B
Async 0x73 event ID: 0x08
0x73/0x08 glasses IPv4: 192.168.49.176

CYAN CATALOG READINESS
Delay before catalog GET: 1000 ms
Source parity: PictureFragment.downloadMediaConfig() / ktxRunOnUiDelay(0x03e8)
Async 0x73 event ID: 0x0B

SNAPSHOT B CATALOG
Method: GET
Path: /files/media.config
Target: passive 0x73/0x08 glasses IPv4 only
Redirects: DISABLED
Max response bytes: 65536
Media-file GET requests so far: 0
HTTP status: 200
Catalog bytes received: 243
Content-Type: text/plain
Content-Encoding: <none>
UTF-8 valid: true
BOM: NONE
Cyan parser branch: configFileType=1 / readLines()
Line-list entries: 11
Non-empty entries: 11
Relative safe entries: 11
Duplicate entries: 0
Unsafe entry count: 0
Whitespace-altered lines rejected: 0
Extension summary: .jpg=10, .opus=1
Filename/path values logged: NO
Catalog raw body logged: NO
Opaque all-entry identities in memory: 11
Opaque JPG identities in memory: 10
Per-file identity/hash tokens reported: NO
Remote filename/path values persisted/logged: NO
Media-file GET requests: 0

NO-CAPTURE COMPARISON
Snapshot A inventory: images=10, videos=0, recordings=1
Snapshot B inventory: images=10, videos=0, recordings=1
Inventory counts exactly equal: true
Snapshot A total safe catalog entries: 11
Snapshot B total safe catalog entries: 11
Total catalog entry count equal: true
Snapshot A JPG identities: 10
Snapshot B JPG identities: 10
JPG identity count equal: true
MP4 entry count equal: true
OPUS entry count equal: true
JPG identities retained from Snapshot A: 10
JPG identities missing from Snapshot A: 0
Unexpected JPG identities in Snapshot B: 0
All safe identities retained from Snapshot A: 11
All safe identities missing from Snapshot A: 0
Unexpected safe identities in Snapshot B: 0
JPG opaque identity sets exactly equal: true
Full safe catalog opaque identity sets exactly equal: true
Snapshot A BLE/catalog full media parity: true
Snapshot B BLE/catalog full media parity: true
G6A NO-CAPTURE STABILITY DIAGNOSTIC: STABLE — INVENTORY AND CATALOG IDENTITIES UNCHANGED WITHOUT CAPTURE

EXIT TRANSFER MODE
Command: 0x41 / 02 01 09
No retry policy: TRUE
EXIT write start: SUCCESS
BLE characteristic write status: SUCCESS
Exit write callback: SUCCESS
Post-exit confirmation required: valid 0x73/0x01 inventory matching current snapshot
Post-exit 0x41 frame observed: IGNORED for exit confirmation
Async 0x73 event ID: 0x01
Post-exit 0x73/0x01 inventory: VALID
Post-exit inventory matches current snapshot: true
Post-exit 0x73/0x01 confirmation: COMPLETE

LOCAL P2P CLEANUP
removeGroup request: FAILED reason=2
P2P group present after cleanup request: false

SUMMARY
Capture action issued by app: NO
User instruction throughout: DO NOT TAKE ANY PHOTO
Media-count queries: 2
P2P enter writes: 2
Transfer-exit writes: 2
Catalog GET requests: 2
Media-file GET requests: 0
Total HTTP GET requests: 2
Exact bounded operation totals: true
Post-exit 0x73/0x01 confirmation completed in both snapshots: YES
P2P group absence verified after both snapshots: YES
Remote filename/path values logged/persisted: NO
Glasses file mutation/deletion: 0
G6A NO-CAPTURE STABILITY RESULT: PASS — STABLE — INVENTORY AND CATALOG IDENTITIES UNCHANGED WITHOUT CAPTURE
END REPORT
```
