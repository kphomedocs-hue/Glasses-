# AIMB-G1 Gate Roadmap

Progress is evidence-gated, not version-number driven.

## G0 — software/recovery readiness

Status: **PASS**.

Required evidence:
- frozen Discovery source hash verified,
- read-only safety audit passes,
- clean Android compile/lint,
- APK signature verifies,
- legacy v0.4 pure-Java regression tests pass,
- project rebuilds from a clean repository checkout.

Evidence:
- Cold Recovery Gate run `35387241734`: PASS.
- Hardened Cold Recovery run `35387536088`: PASS.
- Core Module Regression run `35387408775`: PASS.
- Expanded Core Module Regression run `35387812463`: PASS.
- frozen reference branch: `frozen/discovery-v0.1-readonly`.

## G1 — physical GATT confirmation

Status: **PASS — 2026-09-19**.

Confirmed:
- physical AIMB-G1 LE GATT connection,
- complete service/characteristic/property enumeration,
- Cyan service `de5bf728-...`,
- Cyan notify `de5bf729-...`,
- Cyan write `de5bf72a-...`,
- standard Device Information hardware revision `AM01SPG1_V1.4`.

Evidence:
`docs/testing/results/2026-09-19_G1_PHYSICAL_DISCOVERY_PASS.md`

## G2 — response-channel confirmation

Status: **PASS — 2026-09-19**.

Confirmed:
- standard CCCD notification subscription succeeds on `de5bf729-...`,
- no proprietary write is required merely to receive spontaneous traffic,
- three valid spontaneous `0x73` frames were received,
- frames validate against the known little-endian length + CRC-16/MODBUS envelope.

Evidence:
`docs/testing/results/2026-09-19_G2_NOTIFICATION_ONLY_PASS.md`

## G2B — response-semantic correlation

Status: **PASS — exact Cyan static trace**.

Purpose:
- decode the meaning of observed `0x73` traffic,
- decide whether initialization/time behavior is required before control,
- avoid guessing and avoid active protocol writes.

Method:
1. targeted Cyan parser tracing;
2. passive event correlation using timestamped physical actions and notification frames;
3. repeated observations before assigning a semantic label.

Allowed:
- connect to exactly one bonded AIMB-G1-family device,
- discover the confirmed service,
- subscribe to the confirmed notify characteristic,
- standard CCCD enable-notification write,
- timestamp incoming frames,
- user-generated event markers,
- CRC/length validation,
- sanitized reporting.

Prohibited:
- proprietary characteristic write,
- `de5bf72a-...` control write,
- initialization/time command,
- media-mode command,
- Wi-Fi/P2P/AP,
- HTTP/media transfer,
- pairing/unpairing/reset,
- OTA/firmware operation,
- generic raw BLE command console.

Plan:
`docs/testing/G2B_PASSIVE_CORRELATION_PLAN.md`

Exit criterion: **met by exact Cyan trace**.

Evidence:
`docs/research/CYAN_EXACT_G2B_TRACE_2026-09-19.md`

Resolved:
- observed `0x01` event = media inventory/count/config report,
- observed `0x05` event = battery/charging-family report,
- Cyan's first queued post-discovery proprietary command = `0x40` syncTime,
- time-sync callback is not used as a handshake gate.

Passive event correlation remains available as a fallback for future unknown event types.

## G2C — one-command initialization parity

Status: **PASS — 2026-09-19**.

Candidate: K G1 Init Probe v0.2.1. GitHub Actions run `35416416013`: PASS.

Action:
- connect and subscribe to the confirmed response path,
- construct Cyan-equivalent dynamic `0x40` time-sync payload,
- send exactly one `0x40` frame,
- record any `0x40` response and spontaneous `0x73` reports,
- disconnect.

Prohibited:
- `0x41` glassesControl,
- P2P/AP media payload,
- Wi-Fi/P2P/AP activation,
- HTTP/media transfer,
- reset/restart/OTA/firmware action,
- generic write console.

Exit criterion:
- one benign Cyan-parity proprietary write succeeds or fails in a documented way and response behavior is understood.

## G3 — one allow-listed media-inventory query

Status: **PASS — 2026-09-19**.

Candidate: K G1 Media Count Probe v0.3. GitHub Actions run `35420261264`: PASS.

Use the exact Cyan `glassesControl` media-count request:

```text
outer command: 0x41
payload: 02 04
```

Purpose:
- validate the `0x41` request/response path,
- read media inventory/count information,
- avoid intentionally entering P2P/AP media mode.

Allowed:
- existing confirmed LE connection and notify subscription,
- exactly one `0x41` write with payload `02 04`,
- capture and decode the matching response,
- passive `0x73` observation,
- disconnect.

Prohibited:
- `02 01 04 01` P2P media-mode command,
- `02 01 04 02` AP media-mode command,
- Wi-Fi/P2P/AP activation,
- HTTP/media transfer,
- delete/modify/reset/restart/OTA,
- arbitrary command input or retry loop.

Exit criterion:
- the single media-count query response is physically captured and parsed.

## G3B — bounded P2P transfer-mode lifecycle

Status: **PASS — 2026-09-19**.

Candidate: K G1 P2P Lifecycle Probe v0.3.1. GitHub Actions run `35421429624`: PASS.

Exact Cyan physical prerequisites now support the P2P route:
- configFileType = 1,
- onlySupportApImport = false.

Approved lifecycle:
1. connect + subscribe,
2. send `0x41` payload `02 01 04 01` exactly once,
3. observe BLE responses,
4. send `0x41` payload `02 01 09` exactly once as the exact Cyan exit-transfer command,
5. observe BLE responses,
6. disconnect.

G3B deliberately does **not** use Android Wi-Fi/P2P APIs or HTTP.

Exit criterion:
- enter-transfer and exit-transfer behavior are both physically captured and frame-validated.

## G4A — phone-side P2P discovery/association

Status: **PASS — 2026-09-19**.

Purpose:
- validate Android Wi-Fi Direct discovery/association against the physically confirmed glasses P2P mode,
- establish the local network path without issuing an HTTP request.

Allowed:
- enter P2P transfer mode with the confirmed `02 01 04 01` payload,
- parse transfer credentials in memory without logging or persisting them,
- use Android `WifiP2pManager` to discover/connect to the corresponding peer,
- inspect connection/group information and local/group-owner addresses,
- exit transfer mode with `02 01 09`,
- disconnect/remove temporary phone-side P2P state.

Prohibited:
- Internet access,
- HTTP/socket requests,
- `media.config` access,
- file/media listing,
- downloads,
- file modification/deletion,
- AP-mode command,
- arbitrary BLE/network command input.

Exit criterion:
- glasses P2P network is discovered/associated from the phone and local connection metadata is captured without accessing media.

## G4A2 — passive P2P-IP notification capture

Status: **PASS — 2026-09-19**.

Purpose:
- resolve the glasses-side P2P client IP without adding any new proprietary command.

Method:
- repeat the physically proven G4A association path,
- expose only sanitized `0x73` event identifiers,
- if event `0x08` appears, parse IPv4 bytes `[7..10]` in memory,
- do not log credentials, peer addresses, or unrelated raw payloads,
- exit transfer mode and clean up exactly as G4A.

Allowed proprietary writes:
1. P2P enter `0x41 / 02 01 04 01`, once;
2. transfer exit `0x41 / 02 01 09`, once.

No additional query is allowed in G4A2.

Physical result:
- valid `0x73 / 0x08` observed during the proven P2P association lifecycle,
- phone group owner: `192.168.49.1`,
- glasses client IP: `192.168.49.176`,
- observed event IDs: `0x0B`, `0x08`, `0x01`,
- HTTP/socket/media operations: 0,
- `0x41 / 02 03` query: not used,
- transfer exit: successful,
- cleanup anomaly `removeGroup` reason 2 remained non-blocking.

Evidence:
`docs/testing/results/2026-09-19_G4A2_P2P_IP_NOTIFY_PASS.md`

Verified candidate: K G1 P2P IP Notify Probe v0.4.1. APK SHA-256 `3c9104c34fbf06fb06631a09c67cac9eab3e2a626078abcc14401cf09a2cddf3`; build run `35429585616` PASS.

## G4B — read-only local media listing

Status: **READ PATH CONFIRMED; INITIAL DIAGNOSTIC PARSER WAS WRONG**.

v0.4.2 successfully reached the exact local endpoint and read the bounded body, but our diagnostic incorrectly assumed JSON and threw `JSONException`.

Evidence:
`docs/testing/results/2026-09-19_G4B_CATALOG_PARSE_FAIL.md`

## G4B2 — response-shape characterization

Status: **PASS — 2026-09-19**.

Physical v0.4.4 established:
- one GET to `/files/media.config`,
- HTTP 200,
- `text/plain`,
- 67 bytes,
- strict UTF-8 valid,
- no BOM,
- diagnostic line count 4,
- no JSON structure,
- zero media-file GETs,
- successful transfer exit.

A deeper exact Cyan bytecode trace corrected the branch semantics:
- `configFileType == 2` → `vf_list.txt`, Kotlin `readText()`, Moshi `PtPFileModel` JSON;
- `configFileType != 2` → `media.config`, Kotlin `readLines()`;
- physical `configFileType=1`, therefore the line-oriented branch applies;
- each line is used to construct `http://<glassDeviceWifiIP>/files/<line>` and is paired with that URL in `PictureDownloadBean`.

Evidence:
- `docs/testing/results/2026-09-19_G4B2_RESPONSE_SHAPE_PASS.md`
- `docs/research/CYAN_EXACT_G4B_TRACE_2026-09-19.md`

## G4B3 — exact line-list parser parity

Status: **PASS — 2026-09-19**.

Physical v0.4.5:
- one GET to `/files/media.config`;
- 67-byte UTF-8 `text/plain`;
- exact line-list entries: 3;
- 3 non-empty, 3 relative-safe;
- 0 blank, scheme/absolute-URL, leading-slash, traversal or control-character entries;
- extensions: `.jpg=2, .opus=1`;
- zero media-file GETs;
- transfer exit success.

Evidence:
`docs/testing/results/2026-09-19_G4B3_CATALOG_LINE_PASS.md`

Note: G4B2's reported line count 4 included the terminal newline; G4B3's `readLine()` parity establishes the real entry count as 3.

## G5 — one disposable media download

Status: **v0.5.0 SAFE NO-DELTA STOP; v0.5.2 SAFE NO-DELTA STOP; BLE-ONLY CAPTURE-VISIBILITY v0.5.4 VERIFIED BUILD READY**.

Exact static evidence:
`docs/research/CYAN_EXACT_G5_TRACE_2026-09-19.md`

Cyan's physical branch:
- each line becomes `http://<glassDeviceWifiIP>/files/<line>`;
- `downloadGlassFile()` passes that URL into `AndroidNetworking.download(url, albumDir, filename)`;
- medium priority + progress listener + download listener;
- queue completion reaches the known transfer-exit flow.

Safety decision: catalog order is not proven chronological, so G5 must not select first/last/current JPG by position.

Approved G5 diagnostic design:
1. Phase A baseline:
   - P2P enter / exact peer / passive IP;
   - one GET of `/files/media.config`;
   - hold validated line values in memory only;
   - exit transfer and clean P2P group.
2. User physically captures exactly one disposable JPG test photo.
3. Phase B:
   - re-enter P2P / exact peer / passive IP;
   - one second GET of `/files/media.config`;
   - compute set difference;
   - require exactly one new safe relative `.jpg`;
   - construct only `http://<passive-IP>/files/<new-entry>`;
   - perform exactly one media GET;
   - no redirect, no retry, no Range/resume;
   - stream to one app-private temporary file with a hard size cap;
   - validate HTTP 200, non-empty, Content-Length when supplied, and JPEG signature;
   - never log the remote catalog line or filename/path;
   - exit transfer and stop.

Abort before media GET if delta count is not exactly one, the entry is unsafe, or extension is not JPG.

Verified candidate: **K G1 Disposable Photo Probe v0.5.0**.
- APK SHA-256: `7966cb3f5ecb1be89f876a6686d34b74dbf693c9231603cd8a92dc2b8d361712`
- hardened build commit: `b9152f13ed7bacbd2d895a161da3dafbb9461406`
- build run: `35435019643` — PASS
- archive commit: `3749b2398c300f8b8677496157fd5d45ad51bd9b`
- hardened source Repository Hygiene: `35435019654` — PASS
- safety audit / compile / lint / signature: PASS

The hardened v0.5.0 physical run stopped safely before media transfer because Phase B returned the unchanged 3-entry / 67-byte catalog after one user-captured JPG.

Post-result static parity recheck:
- Cyan calls `readAlbumCounts()` / `0x41 02 04` before import from `loadDataData()`;
- Cyan delays catalog retrieval by exactly 1000 ms via `ktxRunOnUiDelay(..., 0x03e8, ...)`.

G5.1 may add only those two already-supported Cyan behaviors. The exact-one-new-safe-JPG selection rule remains mandatory.

G5 does not implement multi-file sync, OPUS/video import, production naming, ledger, deletion, or glasses mutation.


## G6 — automatic sync

- sync new JPG/MP4/OPUS,
- one daily chronological counter,
- persistent ledger,
- retry/recovery,
- integrity checks,
- minimal production UI.

## G7 — hardening

- long videos,
- weak/failed network transitions,
- process kill/reboot recovery,
- day rollover,
- storage-full handling,
- duplicate catalog entries,
- repeated captures with the same timestamp,
- permission revocation/regrant,
- battery/background behavior.

No destructive maintenance commands are part of this roadmap.


### G5.1 verified candidate

K G1 Disposable Photo Probe v0.5.1:
- APK SHA-256 `20cab114c980e10c2e277cb82960b761975aac8dad467c36d7c88023dd7a403f`
- source/build commit `c8e54b68ed817f899aef57030d9e9bf4a442ea2e`
- build run `35436132719` — PASS
- archive commit `353bbd72bbf9d72a81b3ff82f6664a58aa864807`

Only two behavioral deltas from v0.5.0 are authorized:
1. one exact `0x41 / 02 04` media-count/config query before P2P enter in each phase;
2. exact 1000 ms wait after P2P + passive-IP readiness before each `/files/media.config` GET.

The media-download boundary is unchanged. G6 remains blocked until the G5.1 physical report is reviewed.


### G5.2 pre-physical hardening

v0.5.2 adds no new protocol/network behavior. It only:
- requires the `02 04` BLE write callback and valid response before P2P enter;
- resets failed two-phase runs back to BASELINE for a clean retry.

Verified APK SHA-256: `14fa9447ee938d20c35c935f5665bbcd72f0edb61b33f1e266d96aacb18bfe7a`.


### G5 capture-visibility diagnostic

Purpose: isolate when a physical photo becomes visible to the glasses inventory before another download attempt.

Allowed:
- BLE connect + notification subscription;
- one `0x41 / 02 04` baseline query;
- keep BLE connected while transfer mode is OFF;
- user captures exactly one photo;
- observe only sanitized `0x73` event IDs and media-count/config fields for event `0x01`;
- after a bounded observation window, one `02 04` recheck;
- disconnect.

Forbidden:
- P2P enter;
- Wi-Fi Direct;
- HTTP/catalog/media GET;
- AP mode;
- `02 03`;
- file mutation/deletion;
- reset/restart/OTA.


### G5 capture-visibility verified candidate — v0.5.3

- APK SHA-256: `36c508e98cfb2ba26d54ad54b0b7716fcbdbede0ed6a724ed0a4964a9bbacdc1`
- build commit: `245c9d944e1b2eed10494ad2e8d5fe4d7810c5f9`
- build run: `35438040193` — PASS
- archive commit: `45892085a8024aec032131c7c093e1c7e2b75b0d`

Only `02 04` is implemented, with a maximum of two proprietary writes total. No P2P/Wi-Fi/HTTP/media path exists.


### G5 capture-visibility v0.5.4

v0.5.4 supersedes v0.5.3 before physical use.

Diagnostic-quality correction:
- arm the 60-second window before the physical photo;
- reset separate armed-window event counters at arm time;
- count passive `0x73` and `0x73/0x01` events inside that window separately;
- final `02 04` remains the authoritative inventory recheck.

Verified:
- APK SHA-256 `5f508ba014db4f8cfd803e2573419b95672d95d58fc6f4f7319c729c8013426a`
- build run `35440385274` — PASS
- build commit `86b2ac3d642947f1d02b5b1e91e0488a8ec9c687`
- archive commit `4881adff06b31e134ace69b515cabca9a1ce8468`

No network/protocol scope was added.


### G5 capture-visibility v0.5.5

v0.5.5 supersedes v0.5.4 before physical use.

Reason:
- v0.5.4 retained a stale exported-report version literal (`App version: 0.5.3`).
- v0.5.5 corrects report provenance only; diagnostic behavior is unchanged.

Verified:
- APK SHA-256 `d6ee69fe4929719de31410f35a2e5f27c281598eec75163a10142c4367a8f55a`
- build commit `2aec1fc118291506fb4a0da338f82f88f51f103b`
- build run `35450379889` — PASS
- archive commit `da9600289372f8d2385e8f3629f28ade7415f64d`
- source Repository Hygiene `35450379856` — PASS
- safety audit / compile / lint / APK signature: PASS

Physical boundary remains BLE-only with at most two `0x41 / 02 04` writes and no network/media path.

Next action: run v0.5.5 once and review the sanitized capture-visibility report before any further G5 download attempt.
