# G6A v0.6.8.1 Credential Handshake Diagnostic — PHYSICAL RESULT

Generated: 2026-10-02T10:29:19+0530

## Exact build attribution

- App version: `0.6.8.1`
- Build commit: `beceb27754f491fcae6a53433ad4dd4fcfadc22b`
- Build run: `36965814011`
- Build attempt: `1`
- Package: `com.parkarsite.g6acreddiag681`
- APK SHA-256: `01ba35d297c6d48dce31242185032d8d37ced39856419e827cc9c9013e4246dd`

## Physical result

Baseline transport prerequisites passed:
- one bonded AIMB-G1-family target;
- GATT connected;
- Cyan service/notify/write present;
- notification subscription succeeded;
- media-count query succeeded;
- baseline inventory = 11 images / 0 videos / 1 recording;
- configFileType=1;
- onlySupportApImport=false.

P2P-enter observation:
- enter write start: SUCCESS;
- one valid framed `0x41` notification arrived at +72 ms;
- that `0x41` frame classified `TOO_SHORT` for the credential structure;
- enter write callback arrived at +88 ms;
- no structurally valid credential frame appeared within the original 10-second window;
- no structurally valid credential frame appeared during the additional 10-second passive late window;
- no invalid frames were observed;
- no `0x73` or other valid command frames were observed during credential observation.

Exit:
- one allow-listed transfer-exit write issued;
- exit write callback succeeded;
- post-exit `0x41` observed and ignored for exit confirmation;
- no matching post-exit `0x73/0x01` baseline inventory was observed.

Negative scope remained intact:
- physical captures requested: 0;
- Android Wi-Fi Direct API operations: 0;
- HTTP requests: 0;
- catalog GETs: 0;
- media GETs: 0;
- credential values decoded/logged/persisted: NO;
- raw payload logged: NO;
- remote filename/path logged/persisted: NO;
- glasses explicit mutation/deletion command: NOT IMPLEMENTED.

## Interpretation

The v0.6.8 blocker is now narrower than the original three-run report.

The glasses did produce a valid Cyan `0x41` notification after the P2P-enter command, but the only observed `0x41` frame was too short to satisfy the credential-frame minimum structure. No normal credential-bearing frame was observed for a full 20 seconds.

Therefore this run does **not** support the hypothesis that a normal credential frame arrived and was rejected because of prefix mismatch, non-positive credential lengths, or bounds overflow.

The remaining ambiguity is the exact meaning of the short `0x41` frame. It may be a command echo/ack/status response, but v0.6.8.1 deliberately did not expose its payload bytes. A further bounded diagnostic should classify the safe short response by total length, declared payload length, and non-secret short payload bytes while continuing to avoid SSID/password/raw credential exposure.

## Gate state

- v0.6.8 Phase 1: BLOCKED before capture/catalog/import.
- v0.6.8.1 diagnostic: COMPLETE — short 0x41 response observed; no credential-bearing response within 20 s.
- G6A: OPEN.
- G6B: BLOCKED.

---

## Raw report

```text
K G1 G6A P2P CREDENTIAL HANDSHAKE DIAGNOSTIC REPORT
Generated: 2026-10-02T10:29:19+0530
App version: 0.6.8.1
Build commit: beceb27754f491fcae6a53433ad4dd4fcfadc22b
Build run: 36965814011
Build attempt: 1
Purpose: classify no credential candidate vs rejected 0x41 candidate vs normal valid vs late valid
Physical captures requested by app: 0
Instruction: DO NOT TAKE A PHOTO during this diagnostic
Media-count writes allowed: EXACTLY ONE
P2P enter writes allowed: EXACTLY ONE
Transfer-exit writes allowed: EXACTLY ONE
Normal credential window: 10000 ms
Late passive observation window: 10000 ms
Android Wi-Fi Direct API operations: 0
HTTP requests: 0
Catalog GET requests: 0
Media-file GET requests: 0
Credential values decoded/logged/persisted: NO
Bluetooth address logging: NO
Raw notification payload logging: NO
Remote filename/path logging/persistence: NO
Glasses explicit mutation/deletion command: NOT IMPLEMENTED

BONDED TARGET CHECK
Bonded devices total: 7
AIMB-G1-family bonded matches: 1
Target name: AIMB-G1_<suffix>
Bluetooth address: not logged

GATT CONNECTION
GATT: connected
CYAN SERVICE/NOTIFY/WRITE: PRESENT
Notification subscription start: SUCCESS
Notification subscription: SUCCESS

CYAN MEDIA INVENTORY REFRESH
Command: 0x41 / 02 04
No retry policy: TRUE
Media-count write start: SUCCESS
BLE characteristic write status: SUCCESS
Media-count response: VALID
Image count: 11
Video count: 0
Recording count: 1
Config file type: 1
Only-support-AP-import: false
Media-count write/response handshake: COMPLETE

ENTER P2P MODE — CREDENTIAL OBSERVATION ONLY
Command: 0x41 / 02 01 04 01
No retry policy: TRUE
ENTER write start: SUCCESS
Normal credential observation window started: 10000 ms
Enter-observation 0x41 classification: TOO_SHORT at +72 ms
BLE characteristic write status: SUCCESS
P2P enter write callback: SUCCESS
Normal 10-second credential window ended without an accepted credential frame.
Normal-window interim counters: validFrames=1, invalidFrames=0, cmd41=1, cmd73=0, other=0
Late passive observation window started: 10000 ms; no second ENTER write will be sent.
Late passive observation window ended without an accepted credential frame.

EXIT TRANSFER MODE
Command: 0x41 / 02 01 09
No retry policy: TRUE
EXIT write start: SUCCESS
BLE characteristic write status: SUCCESS
Exit write callback: SUCCESS
Post-exit confirmation target: valid 0x73/0x01 inventory matching baseline
Post-exit 0x41 frame observed: IGNORED for exit confirmation
Post-exit matching 0x73/0x01 observed: false

DIAGNOSTIC SUMMARY
Media-count writes: 1
P2P enter writes: 1
Transfer-exit writes: 1
Enter write callback observed: true
Valid framed notifications during credential observation: 1
Invalid framed notifications during credential observation: 0
Valid 0x41 frames during credential observation: 1
Valid 0x73 frames during credential observation: 0
Other valid command frames during credential observation: 0
Rejected credential shapes — TOO_SHORT: 1
Rejected credential shapes — PAYLOAD_LEN_TOO_SHORT: 0
Rejected credential shapes — PREFIX_MISMATCH: 0
Rejected credential shapes — NONPOSITIVE_LENGTH: 0
Rejected credential shapes — BOUNDS_OVERFLOW: 0
Suppressed per-event lines after report cap: 0
Normal-window valid credential: false
Late-window valid credential: false
Valid credential elapsed from ENTER write start: -1 ms
ENTER write callback elapsed from ENTER write start: 88 ms
Credential values decoded/logged/persisted: NO
Raw notification payload logged: NO
Android Wi-Fi Direct API operations: 0
HTTP requests: 0
Catalog GET requests: 0
Media-file GET requests: 0
Exit write callback observed: true
Post-exit matching baseline inventory observed: false
G6A CREDENTIAL HANDSHAKE DIAGNOSTIC CLASSIFICATION: REJECTED_0x41_CANDIDATE_ACTIVITY — valid 0x41 frame activity occurred but none matched the credential structure
Gate effect: DIAGNOSTIC ONLY — DOES NOT CLOSE G6A OR UNBLOCK G6B
END REPORT
```
