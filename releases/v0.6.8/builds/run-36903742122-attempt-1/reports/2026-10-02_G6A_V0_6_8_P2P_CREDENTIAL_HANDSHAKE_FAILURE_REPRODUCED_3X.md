# G6A v0.6.8 — P2P credential-handshake blocker reproduced three times

Date: 2026-10-02

Exact build:
- App version: `0.6.8`
- Build commit: `cf92a44563352ed339d8dd74a825483c7dba39e4`
- Build run: `36903742122`
- Build attempt: `1`
- Package: `com.parkarsite.g6apersist68`
- APK SHA-256: `3f94a240030c10162c0025853846f6ea29860de2ed36874742cddbb2a61d4b27`

## Classification

Three physical attempts were supplied at:
- 2026-10-02T09:53:08+0530
- 2026-10-02T09:55:25+0530
- 2026-10-02T10:00:07+0530

All three stopped at the same baseline boundary:
- GATT connected;
- Cyan service/notify/write present;
- notification subscription succeeded;
- media-count query succeeded;
- baseline inventory was 11 images / 0 videos / 1 recording;
- P2P-enter write started successfully;
- P2P-enter write callback succeeded;
- required transfer-credential handshake was not accepted before timeout;
- abort issued the one allow-listed transfer-exit write;
- exit write callback succeeded;
- no matching post-exit `0x73/0x01` inventory was observed;
- Android local P2P group state was absent after cleanup;
- zero catalog GETs;
- zero media GETs;
- zero HTTP requests;
- no media validation;
- no persistent import;
- no durable receipt;
- no glasses explicit mutation/deletion;
- no capture-watch event and no photo required/taken by the app.

The first two runs occurred before the requested clean glasses power-cycle. The third run reproduced the same failure after the clean power-cycle procedure.

This does not prove whether no credential notification was emitted, a valid `0x41` frame was structurally rejected, or a valid credential arrived after the original 10-second window. That ambiguity is the purpose of the bounded v0.6.8.1 credential-handshake diagnostic.

G6A remains OPEN. G6B remains BLOCKED.

---

## Raw report 1 — 2026-10-02T09:53:08+0530

```text
K G1 G6A PERSISTENT IMPORT UNDER CONSUMPTIVE GET REPORT
Generated: 2026-10-02T09:53:08+0530
App version: 0.6.8
Build commit: cf92a44563352ed339d8dd74a825483c7dba39e4
Build run: 36903742122
Build attempt: 1
Mode: BASELINE -> ONE CAPTURE -> PRE-GET RETENTION -> ONE PERSISTENT JPG IMPORT -> OBSERVE REMOTE CONSUMPTION -> FRESH POST-GET CATALOG
Purpose: prove one consumed remote JPG is atomically persisted locally with a durable receipt
Instruction: DO NOT TAKE A PHOTO until the app explicitly reports ARMED
Capture action issued by app: NO
Intended physical captures: EXACTLY ONE
Persistent media GET/imports allowed: EXACTLY ONE
Inventory writes total allowed: EXACTLY FOUR
P2P enter writes total allowed: EXACTLY FOUR
Transfer exit writes total allowed: EXACTLY FOUR
Catalog GET requests total allowed: EXACTLY FOUR
Total HTTP GET requests allowed: EXACTLY FIVE (4 catalog + 1 JPG)
HTTP redirects: DISABLED
HTTP retry/resume/Range: DISABLED
Catalog response cap: 65536 bytes
Media response cap: 33554432 bytes
Peer selection: EXACT CASE-SENSITIVE BLE-REPORTED P2P NAME ONLY
Credential logging/persistence: DISABLED
Remote filename/path logging/persistence: DISABLED
Persistent archive root: app-private files / g6a_imports
Local write contract: .part + fsync -> JPEG validate -> synced opaque sidecar -> atomic rename -> synced ledger commit
Ledger committed entries at start: 0
Stale partial files removed at process startup: 0
Glasses explicit mutation/deletion command: NOT IMPLEMENTED

BASELINE SNAPSHOT
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
Purpose: exact Cyan media-count/config confirmation for current stage
No retry policy: TRUE
Media-count write start: SUCCESS
Media-count response: VALID
Image count: 11
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
G6A STABILITY ERROR: P2P enter write/credential handshake did not complete.
Abort recovery: attempting the single allow-listed transfer-exit write.

EXIT TRANSFER MODE
Command: 0x41 / 02 01 09
No retry policy: TRUE
EXIT write start: SUCCESS
BLE characteristic write status: SUCCESS
Exit write callback: SUCCESS
Post-exit confirmation required: valid 0x73/0x01 inventory matching current snapshot
Post-exit 0x41 frame observed: IGNORED for exit confirmation
Exit write callback observed: true
Post-exit matching 0x73/0x01 observed: false

LOCAL P2P CLEANUP
removeGroup request: FAILED reason=2
P2P group present after cleanup request: false

SUMMARY
Stage at failure: BASELINE
Capture-watch inventory events: 0
Media-count queries: 1
P2P enter writes: 1
Transfer-exit writes: 1
Catalog GET requests: 0
Media-file GET requests: 0
Total HTTP GET requests: 0
Media validated: false
Persistent import committed: false
Durable receipt committed: false
Remote filename/path values logged/persisted: NO
Glasses file mutation/deletion: 0
G6A SINGLE-JPG GET RETENTION RESULT: FAILED — P2P enter write/credential handshake did not complete.
END REPORT
```

## Raw report 2 — 2026-10-02T09:55:25+0530

```text
K G1 G6A PERSISTENT IMPORT UNDER CONSUMPTIVE GET REPORT
Generated: 2026-10-02T09:55:25+0530
App version: 0.6.8
Build commit: cf92a44563352ed339d8dd74a825483c7dba39e4
Build run: 36903742122
Build attempt: 1
Mode: BASELINE -> ONE CAPTURE -> PRE-GET RETENTION -> ONE PERSISTENT JPG IMPORT -> OBSERVE REMOTE CONSUMPTION -> FRESH POST-GET CATALOG
Purpose: prove one consumed remote JPG is atomically persisted locally with a durable receipt
Instruction: DO NOT TAKE A PHOTO until the app explicitly reports ARMED
Capture action issued by app: NO
Intended physical captures: EXACTLY ONE
Persistent media GET/imports allowed: EXACTLY ONE
Inventory writes total allowed: EXACTLY FOUR
P2P enter writes total allowed: EXACTLY FOUR
Transfer exit writes total allowed: EXACTLY FOUR
Catalog GET requests total allowed: EXACTLY FOUR
Total HTTP GET requests allowed: EXACTLY FIVE (4 catalog + 1 JPG)
HTTP redirects: DISABLED
HTTP retry/resume/Range: DISABLED
Catalog response cap: 65536 bytes
Media response cap: 33554432 bytes
Peer selection: EXACT CASE-SENSITIVE BLE-REPORTED P2P NAME ONLY
Credential logging/persistence: DISABLED
Remote filename/path logging/persistence: DISABLED
Persistent archive root: app-private files / g6a_imports
Local write contract: .part + fsync -> JPEG validate -> synced opaque sidecar -> atomic rename -> synced ledger commit
Ledger committed entries at start: 0
Stale partial files removed at process startup: 0
Glasses explicit mutation/deletion command: NOT IMPLEMENTED

BASELINE SNAPSHOT
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
Purpose: exact Cyan media-count/config confirmation for current stage
No retry policy: TRUE
Media-count write start: SUCCESS
Media-count response: VALID
Image count: 11
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
G6A STABILITY ERROR: P2P enter write/credential handshake did not complete.
Abort recovery: attempting the single allow-listed transfer-exit write.

EXIT TRANSFER MODE
Command: 0x41 / 02 01 09
No retry policy: TRUE
EXIT write start: SUCCESS
BLE characteristic write status: SUCCESS
Exit write callback: SUCCESS
Post-exit confirmation required: valid 0x73/0x01 inventory matching current snapshot
Post-exit 0x41 frame observed: IGNORED for exit confirmation
Exit write callback observed: true
Post-exit matching 0x73/0x01 observed: false

LOCAL P2P CLEANUP
removeGroup request: FAILED reason=2
P2P group present after cleanup request: false

SUMMARY
Stage at failure: BASELINE
Capture-watch inventory events: 0
Media-count queries: 1
P2P enter writes: 1
Transfer-exit writes: 1
Catalog GET requests: 0
Media-file GET requests: 0
Total HTTP GET requests: 0
Media validated: false
Persistent import committed: false
Durable receipt committed: false
Remote filename/path values logged/persisted: NO
Glasses file mutation/deletion: 0
G6A SINGLE-JPG GET RETENTION RESULT: FAILED — P2P enter write/credential handshake did not complete.
END REPORT
```

## Raw report 3 — 2026-10-02T10:00:07+0530

```text
K G1 G6A PERSISTENT IMPORT UNDER CONSUMPTIVE GET REPORT
Generated: 2026-10-02T10:00:07+0530
App version: 0.6.8
Build commit: cf92a44563352ed339d8dd74a825483c7dba39e4
Build run: 36903742122
Build attempt: 1
Mode: BASELINE -> ONE CAPTURE -> PRE-GET RETENTION -> ONE PERSISTENT JPG IMPORT -> OBSERVE REMOTE CONSUMPTION -> FRESH POST-GET CATALOG
Purpose: prove one consumed remote JPG is atomically persisted locally with a durable receipt
Instruction: DO NOT TAKE A PHOTO until the app explicitly reports ARMED
Capture action issued by app: NO
Intended physical captures: EXACTLY ONE
Persistent media GET/imports allowed: EXACTLY ONE
Inventory writes total allowed: EXACTLY FOUR
P2P enter writes total allowed: EXACTLY FOUR
Transfer exit writes total allowed: EXACTLY FOUR
Catalog GET requests total allowed: EXACTLY FOUR
Total HTTP GET requests allowed: EXACTLY FIVE (4 catalog + 1 JPG)
HTTP redirects: DISABLED
HTTP retry/resume/Range: DISABLED
Catalog response cap: 65536 bytes
Media response cap: 33554432 bytes
Peer selection: EXACT CASE-SENSITIVE BLE-REPORTED P2P NAME ONLY
Credential logging/persistence: DISABLED
Remote filename/path logging/persistence: DISABLED
Persistent archive root: app-private files / g6a_imports
Local write contract: .part + fsync -> JPEG validate -> synced opaque sidecar -> atomic rename -> synced ledger commit
Ledger committed entries at start: 0
Stale partial files removed at process startup: 0
Glasses explicit mutation/deletion command: NOT IMPLEMENTED

BASELINE SNAPSHOT
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
Purpose: exact Cyan media-count/config confirmation for current stage
No retry policy: TRUE
Media-count write start: SUCCESS
Media-count response: VALID
Image count: 11
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
G6A STABILITY ERROR: P2P enter write/credential handshake did not complete.
Abort recovery: attempting the single allow-listed transfer-exit write.

EXIT TRANSFER MODE
Command: 0x41 / 02 01 09
No retry policy: TRUE
EXIT write start: SUCCESS
BLE characteristic write status: SUCCESS
Exit write callback: SUCCESS
Post-exit confirmation required: valid 0x73/0x01 inventory matching current snapshot
Post-exit 0x41 frame observed: IGNORED for exit confirmation
Exit write callback observed: true
Post-exit matching 0x73/0x01 observed: false

LOCAL P2P CLEANUP
removeGroup request: FAILED reason=2
P2P group present after cleanup request: false

SUMMARY
Stage at failure: BASELINE
Capture-watch inventory events: 0
Media-count queries: 1
P2P enter writes: 1
Transfer-exit writes: 1
Catalog GET requests: 0
Media-file GET requests: 0
Total HTTP GET requests: 0
Media validated: false
Persistent import committed: false
Durable receipt committed: false
Remote filename/path values logged/persisted: NO
Glasses file mutation/deletion: 0
G6A SINGLE-JPG GET RETENTION RESULT: FAILED — P2P enter write/credential handshake did not complete.
END REPORT
```
