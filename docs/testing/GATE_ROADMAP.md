Warning: truncated output (original token count: 21361)
Total output lines: 1708

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


### G5 capture visibility physical result — PASS

Physical v0.5.5:
- baseline images: 5;
- exactly one user capture during the armed 60-second window;
- passive `0x73 / 0x01` observed images=6;
- final `0x41 / 02 04` confirmed images=6;
- final image delta: +1;
- videos/recordings unchanged;
- observed event IDs: `0x01`, `0x05`;
- proprietary writes total: 2;
- P2P/Wi-Fi/HTTP/media operations: 0.

Evidence:
`docs/testing/results/2026-09-19_G5_CAPTURE_VISIBILITY_V0_5_5_PASS.md`

Interpretation:
- the prior no-delta G5.0/G5.2 results were timing/visibility related rather than evidence of a broken capture path;
- the new image becomes visible asynchronously to BLE inventory.

Next G5 design:
- baseline catalog first;
- capture one disposable JPG with transfer mode off;
- wait for +1 inventory visibility;
- only then re-enter P2P and fetch one new catalog;
- require exactly one new safe relative JPG by set difference;
- allow one media GET maximum;
- stop after validation.

G6 remains blocked.


### G5 visibility-gated v0.5.6

Verified candidate.

- APK SHA-256: `cc997c3d196cb59875707aeaadfa2c96c0be1d0cd9e7951d0703155f93566c9c`
- source/build commit: `cee088d1f7eae87ea3ab125c330414a3af33ca14`
- build run: `35461427615` — PASS
- archive commit: `0dcdaa3e11f9bf8fcdeea021fc8ca452253a4323`
- safety audit / compile / lint / APK signature: PASS

The app will not enter Phase-B P2P until the capture is physically confirmed as exactly +1 image inventory. One media GET maximum remains the hard boundary.


### G5 diagnostic-hardened v0.5.7

Verified candidate; supersedes v0.5.6 before physical use.

- APK SHA-256: `63a43f06175f9a591db72570a78a0f977841c836b77fb834648546b52a8092b3`
- source/build commit: `594dbd906c34778c0276d5d4385431f0a94d2d6b`
- build run: `35462487457` — PASS
- archive commit: `20a7dfc0f6baa8447360c0946d61da5dc0e0cf96`

Protocol/network boundary is unchanged from v0.5.6. Added only timing/state diagnostics, temporary-cache cleanup, single-run lock, and foreground integrity protection.


## G5 — PASS

Physical v0.5.7 completed the bounded single-disposable-JPG transfer successfully.

- baseline inventory/catalog established;
- one physical capture observed as passive +1 image;
- active `02 04` independently confirmed +1;
- P2P entered only after visibility confirmation;
- one refreshed catalog contained exactly one new safe JPG;
- one media GET only;
- HTTP 200;
- Content-Length matched;
- JPEG SOI/EOI passed;
- temporary local file cleanup passed;
- no glasses mutation/deletion.

Evidence:
`docs/testing/results/2026-09-20_G5_7_SINGLE_JPG_DOWNLOAD_PASS.md`

G6 is now unblocked.


### G6A — persistent single-item import

Design freeze:
`docs/design/G6_AUTOMATIC_SYNC_ARCHITECTURE.md`

Purpose: validate the production ledger/archive path with exactly one newly discovered safe JPG per run.

Required:
- G5.7 proven transport only;
- persistent opaque dedup ledger;
- deterministic daily shared counter;
- part-file then validated final commit;
- restart-safe duplicate prevention;
- one item maximum;
- no background service;
- no repeated polling;
- no MP4/OPUS yet;
- no glasses mutation.

G6B and later remain blocked until G6A physical PASS.


### G6A v0.6.1 verified candidate

Preferred physical candidate for G6A.

- APK SHA-256: `ed0d36f32cb1c547a67a45f3dd5f7ea918c62d9ba5ea42310b2fc7cdfe34d94a`
- source/build commit: `20833db86c4a627786fc4b6fb611f215036d7309`
- build run: `36461373394` — PASS
- archive commit: `a44d94aee0f05c42b57e0f51fbfd2bcbb0a36e85`

Part 1 proves one persistent JPG import. Part 2 proves restart persistence/dedup by matching the current catalog against the opaque ledger with zero media redownloads.

G6B remains blocked pending physical PASS.


## G6A v0.6.1 physical restart result — IDENTITY MISMATCH

Physical restart verification reached the intended read-only boundary but did not find the committed opaque identity in the current catalog.

Observed:
- ledger committed entries at verification start: 1;
- inventory: images=8, videos=0, recordings=1;
- catalog: 9 safe entries (.jpg=8, .opus=1);
- current JPG entries checked: 8;
- exact opaque ledger matches: 0;
- inventory queries: 1;
- P2P enter writes: 1;
- catalog GET requests: 1;
- media-file GET requests: 0;
- transfer exit: 1;
- no glasses mutation/deletion.

Evidence:
`docs/testing/results/2026-09-29_G6A_V0_6_1_RESTART_DEDUP_MISMATCH.md`

Source inspection confirms Part 1 and restart verification both hash the exact catalog entry using the same SHA-256 implementation. The mismatch therefore requires a bounded identity-stability diagnostic rather than loosening dedup safety.

**G6A remains NOT PASSED. G6B remains blocked.**


## G6A v0.6.2 identity-stability diagnostic — VERIFIED BUILD / PHYSICAL PENDING

v0.6.2 preserves the G5.7/G6A transport boundary and adds privacy-safe diagnostics to distinguish exact identity stability from directory/path, case-only, or larger remote-identity changes.

Diagnostic capsule stores only:
- SHA-256 of exact remote identity;
- SHA-256 of basename only;
- SHA-256 of lowercase identity;
- character length;
- path-component count.

No raw remote filename/path text is stored. Restart verification still permits **zero media-file GETs**.

Verified:
- APK: `releases/v0.6.2/K_G1_G6A_Identity_Diagnostic_v0_6_2.apk`
- APK SHA-256: `da04ca568d6260eb2f168a3ca6222788beb769217dad24cd63114c0e75f4a27e`
- source/build commit: `2107c445c85b71cff82c0e57a8eecc39277a5d33`
- build run: `36466571422` — PASS
- archive commit: `d84028deed2ceb22d101ab0b13b5d2e192926e9d`
- bounded-scope safety audit / Android compile / lint / APK signature / artifact upload: PASS

Physical procedure:
1. fresh v0.6.2 install;
2. Part 1: baseline → arm → exactly one photo → one persistent JPG import;
3. copy Part-1 report;
4. force-stop/reopen without clearing data or uninstalling;
5. take no new photo;
6. run **Verify restart dedup — NO DOWNLOAD**;
7. return the full diagnostic report.

G6B remains blocked until the v0.6.2 physical result is understood and G6A restart dedup passes safely.


## G6A v0.6.2 physical Part 2 — RESTART CATALOG REVERSION / DEDUP MISMATCH

Part 2 was run on the same v0.6.2 installation after the successful Part-1 import.

Observed after force-stop/reopen:
- ledger committed entries at verification start: 1;
- inventory: images=8, videos=0, recordings=1;
- catalog: 9 safe entries (.jpg=8, .opus=1);
- exact opaque ledger matches: 0;
- committed exact-ID token: `4c28b1090be7`;
- basename-derived matches: 0;
- lowercase-exact matches: 0;
- identity-shape matches: 8;
- media-file GET requests: 0;
- catalog GET requests: 1;
- no glasses mutation/deletion command.

Critical cross-session fact:
- Part-1 baseline: 8 JPG / 9 total catalog entries;
- Part-1 post-capture/import: 9 JPG / 10 total entries;
- Part-2 restart: 8 JPG / 9 total entries.

The remote catalog therefore reverted exactly to the pre-capture count. The committed item's exact/basename/lowercase identity is absent. v0.6.2 cannot distinguish whether the new transferred JPG disappeared, or another JPG disappeared while the new identity changed. The `Identity-shape matches: 8` result is non-discriminating and must not be treated as proof of a rename.

Evidence:
`docs/testing/results/2026-09-29_G6A_V0_6_2_PART2_CATALOG_REVERSION.md`

Gate status:
- persistent local import: physically proven;
- restart-safe remote-identity dedup: NOT proven;
- remote post-transfer retention semantics: unresolved;
- **G6A remains open; G6B remains blocked.**

Next diagnostic must persist an opaque hash set of the entire pre-capture baseline catalog plus the new-item opaque identity, then compare the restart catalog against both with zero media GETs. No raw remote filename/path logging and no glasses deletion/mutation.


## G6A v0.6.3 remote-retention diagnostic — VERIFIED PHYSIC…9361 tokens truncated…26b8e518deb152c`.
- Full independent decision and limitations: `releases/v0.6.8.5/builds/run-38116572973-attempt-1/reviews/MANUAL_RED_TEAM_PREPROMOTION.md`.
- The build is safety-audit, host-JVM regression, compile, lint, signature, and immutable-archive verified. The lint report contains 12 non-blocking warnings.
- One run ONLY. Force-stop Cyan; keep AIMB-G1 paired; Bluetooth/Wi-Fi on; no capture, no reset, no repeat, no network transfer.
- Important limitations: suffix-only Wi-Fi peer matching; largely broadcast-driven connection-state observation; 5s post-group BLE event window. A negative/inconclusive outcome is NOT proof the glasses cannot enter P2P.
- Stop if cleanup reports failed/indeterminate, preserve report, and inspect Wi-Fi Direct group state before any further attempt.
- Do not resume persistent import until this diagnostic's full physical report is reviewed. Never treat CI success as physical PASS.

**G6A OPEN. G6B BLOCKED.**

## CURRENT OVERRIDE — 2026-10-11 G6A v0.6.8.5 physical INCONCLUSIVE

Physical one-time diagnostic completed at 2026-10-11T11:45:57+0530:
- Exact build: run `38116572973`, attempt `1`, source commit `c38203f6e96044b7d36b6b6c12d1775079d750e9`.
- Complete operator report: `releases/v0.6.8.5/builds/run-38116572973-attempt-1/reports/2026-10-11_G6A_V0_6_8_5_PHYSICAL_INCONCLUSIVE.md`.
- New root-cause assessment: `docs/research/G6A_V0_6_8_5_INCONCLUSIVE_PEER_MATCH_ANALYSIS_2026-10-11.md`.
- BLE GATT, notifications, discovery-before-ENTER, one ENTER write/callback all succeeded. Peer count rose from 0 to 1 but zero matched; **no P2P connect was attempted**, so association/readiness remains untested.
- Proven earlier G4A used exact BLE-reported P2P name. Tested v0.6.8.5 uses only a bonded BLE-address suffix match; its header's "name-or-suffix" claim is inaccurate.
- One 0x73 event was counted during pending ENTER callback but event ID was not retained. Meaning unknown.
- One EXIT write/callback succeeded, discovery stopped, and dual post-cleanup group-absence checks passed; no media/HTTP operations.
- **INCONCLUSIVE. Do not repeat unchanged.** Next: exact Cyan name-matching recheck and, if necessary, sanitized no-connect peer classification + early 0x73 event-ID timing. No new physical APK authorized.

**G6A OPEN. G6B BLOCKED.**

## CURRENT OVERRIDE — 2026-10-11 v0.6.8.6 passive diagnostic exact-build prepromotion

**Reason for v0.6.8.6:** The one-time v0.6.8.5 physical run on 2026-10-11 showed one Wi-Fi Direct peer but no suffix match; it never attempted association. Historical G4A/G4A2 used an exact credential-derived P2P name. Current short 0x41 response does not provide that name. v0.6.8.5 also discarded the CRC-valid early 0x73 event ID while ENTER write callback was pending.

**Frozen passive diagnostic source/build**:
- Specification: `docs/research/G6A_V0_6_8_6_PASSIVE_PEER_IDENTITY_SPEC_2026-10-11.md`.
- Source: `source/v0.6.8.6/`.
- Exact source commit: `90c2acb4f8736acf6ff024bfa3ba660ead09dad9`.
- GitHub Actions: run `38118060511`, attempt `1`, **SUCCESS**.
- Exact immutable archive: `releases/v0.6.8.6/builds/run-38118060511-attempt-1/`.
- APK SHA-256: `d05ce2a23d25d409fc583c3830b9e574577586f2eb2f21e830895d9eece099d0`.
- Source ZIP SHA-256: `9202118df6eb15a5628fdf0d199c909bc0b8ca47b01e02b93396bdc02422473b`.
- Independent exact-build manual review: `releases/v0.6.8.6/builds/run-38118060511-attempt-1/reviews/MANUAL_RED_TEAM_PREPROMOTION.md`.
- Safety/red-team/host-JVM, compile/lint, signature, archive and artifact checks: PASS. Lint has 12 nonblocking warnings. Two harmless build-info wording defects recorded in manual review.

**Physical decision:** Conditional PASS for **ONE supervised passive diagnostic run only** of the exact APK above. No Wi-Fi Direct `connect`, `cancelConnect` or `removeGroup`, and no media, HTTP, capture, reset or retry. It performs one bounded discoverPeers + ENTER, reports only safe peer-name categories and CRC-valid 0x73 event ID timing buckets, then one EXIT and verified read-only cleanup.

The user must not repeat any failed/indeterminate run, must preserve the full terminal report, and must not interpret a single peer or model prefix as proof of glasses identity. Do not perform any production import until the new diagnostic result is reviewed.

**G6A remains OPEN. G6B remains BLOCKED.**



### 2026-10-11 G6A Step 3 offline crash-safe reference (NOT a device gate)
Source: `source/g6a-recovery-core/` and `docs/research/G6A_STEP3_OFFLINE_CONSUMPTIVE_RECOVERY_RED_TEAM_2026-10-11.md`. Pure Java 17, no Android/radio/network. Test 85 assertions including six abrupt JVM process stops; GitHub run 38125976170 PASS; hygiene 38125976168 PASS. Locally validated bytes can be recovered with no second remote GET after verified crash; partial/uncertain transfers are quarantined, not retried. Not production exactly-once or on-device power-fail proof. Does not advance G6A physical gate; G6B BLOCKED.


### 2026-10-11 Step 4 source-only peer-identity reference (not gate pass)
Identity research: source/g6a-identity-core/ and docs/research/G6A_STEP4_PEER_IDENTITY_RED_TEAM_AND_TEST_SPEC_2026-10-11.md. Hardened Java 17 offline CI 38126649073 PASS (54 assertions), hygiene 38126649087 PASS. A derived candidate, full BLE suffix, or model prefix is not sufficient to authenticate the current P2P peer. Previously observed single OTHER peer still unidentified and no current group formed/tested. No diagnostic APK or physical run authorized. G6A OPEN; G6B BLOCKED.


### 2026-10-11 G6A Step 4 retry (source-only, no physical gate promotion)
Original Cyan DEX recheck and direct frozen-v0.6.8.5/v0.6.8.6 matcher tests expose a v0.6.8.6 OTHER-category blind spot for hypothetical peer names ending with **colon-formatted** bonded Bluetooth address. See docs/research/G6A_STEP4_RETRY_ORIGINAL_DEX_RAW_ADDRESS_BLIND_SPOT_2026-10-11.md. CI run 38127615703 PASS (77 assertions), hygiene 38127615701 PASS. Actual anonymous peer was not identified; do not connect unknown peer or repeat prior passive scan unchanged. A discriminating observer would need raw-address suffix aggregate and temporal phase, and separate authorization. G6A OPEN; G6B BLOCKED.


### 2026-10-11 Virtual Lab phase 1 — OFFLINE SUBSETS ONLY
Additive shared-core candidate and test harness: lab/virtual-aimb-g1/. Source commit 1c195bd6fb6254365994520cabc48a493e6ed95c, CI run 38130089425 PASS with 24/24 Java 17 synthetic assertion sets, and hygiene run 38130089461 PASS. Details and initial hygiene-fail correction: docs/testing/results/2026-10-11_VGL_PHASE1_24_OFFLINE_TEST_RESULT.md. These are **not production Android, emulator, radio, or physical proofs**; scenario matrix 01–03 marked OFFLINE SUBSET RUN / INTEGRATION PENDING; 72 remain unrun. P2P peer identity and preservation-safe GET still unproven. No experimental APK or real-glasses testing authorized. **G6A OPEN; G6B BLOCKED.**


## LATEST OVERRIDE — 2026-10-11: ALL 96 VGL LANE A OFFLINE SUBSETS VERIFIED
Latest code verified at source commit 34a218ccec0116909cc07fbc75c2a729b63a791a. GitHub offline workflow 38131350988 SUCCESS, hygiene 38131350927 SUCCESS. All 96 distinct planned scenario IDs (12 packs x 8) executed synthetic safety-subset assertions; two red-team suites and an exact-ID manifest guard PASS. Pack 09 executes ACTUAL offline RecoveryImportStore filesystem/crash-barrier paths; the other new policy models are candidate-only and NOT wired to the shipping Android app. New audit fixes reject wrong-shaped HTTP bodies, incomplete reserved receipts, missing baseline photos, unsafe preflight cleanup and tested raw identifier/secret leaks. Initial runner-shell quotation failure in 38131306328 was fixed before final green CI. Detailed result: docs/testing/results/2026-10-11_VGL_LANE_A_96_OFFLINE_TEST_RESULT.md. Review: docs/testing/VGL_LANE_A_96_RED_TEAM_REVIEW_2026-10-11.md. This supersedes earlier statements that 72 are unimplemented, but DOES NOT constitute 96 full production-integrated PASS or any Android/emulator, peer/firmware, originals-retention or physical PASS. No APK, glasses commands or media GET authorized. Existing releases unchanged. Next priorities: single adopted core integrated into Android, safe one-emulator fake-local-server test, perhaps feasibility-gated dual AVD, and separate real identity + originals-preservation research. **G6A OPEN; G6B BLOCKED.**


## 2026-10-11 V2 VERIFIED OVERRIDE — ANDROID SOURCE COMPILES, HOST-LOCAL HTTP 7/7 PASS
Latest V2 verified source: c16b5898fd3d7caf8b2261ddec2c7ed80f5a2c91. Read docs/testing/results/2026-10-11_VGL_V2_ANDROID_COMPILE_HOST_HTTP_RESULT.md and docs/testing/VGL_V2_ANDROID_SOURCE_RED_TEAM_2026-10-11.md. Additive Android simulator-only Gradle project lab/virtual-aimb-g1/android-simulator/ compiles the ACTUAL shared VirtualGlassesCore, policies, protocol, identity and RecoveryImportStore into a no-permissions, source-only Android Activity plus compiled but UNEXECUTED instrumentation hook. API 36, JDK17, Gradle 9.6. Android CI 38132519731 SUCCESS: compile-only; verified merged manifest and NO APK built or installed, NO emulator. Recovery core Files.readString ported to strict 4 KiB bounded UTF-8 read to permit Android Java compilation; previous recovery CI 38132290876 PASS. A separate HOST JVM loopback HTTP simulator in lab/virtual-aimb-g1/host-http/ runs 7/7 synthetic fault cases (GET200, 500, 302, truncation, corrupt JPEG, consumptive-on-GET, forbidden other host) against ACTUAL RecoveryImportStore; together with 96/96 offline synthetic IDs and both red teams CI 38132519735 PASS, hygiene 38132519714 PASS. These are NOT Android emulator runtime tests, real phone tests, production adapter integration, physical identity proof or original-preserving sync. Historical remote JPG GET consumes original; OWNER REQUIREMENT remains unsatisfied. DO NOT BUILD/INSTALL EXPERIMENTAL APK OR RUN PHYSICAL MEDIA GET without separately scoped authorization. Next: one-AVD emulator-only instrumentation after specific approval, full shipping-path adapter integration, then independently verify actual peer and originals-preserving method. G6A OPEN; G6B BLOCKED.


## LATEST OVERRIDE — 2026-10-11 V3: ANDROID EMULATOR RUNTIME PRIVATE STORAGE PASS
Actual single Google APIs Android 16/API36 emulator runtime verified on source revision 7495e9bba9a0bcf0323e859fcd19f10902f251c9. GitHub workflow https://github.com/kphomedocs-hue/Glasses-/actions/runs/38141611598 SUCCESS with exact VGL-EMU-01 PASS, VGL-EMU-02 PASS, VGL_EMULATOR 2/2 PASS. App-private no-backup RecoveryImportStore commit/receipt/reopen, injected AFTER_FETCH_MARK -> REMOTE_UNCERTAIN with no retry, and AFTER_VERIFIED_JOURNAL -> local-only recovery verified on Android. Runner force-stops app/test before independent second instrumentation session; this is NOT kill during a write or power-loss testing. Source-only Android emulator package and instrumentation were assembled as ephemeral APKs, installed and uninstalled **ONLY ON THE GitHub-hosted virtual device** with zero requested app/test permissions. No upload or physical phone APK. The initial runtime run 38141069637 failed; red-team sanitized trace in 38141319056 isolated ExceptionInInitializerError during first import, traced to Android ICU pattern differences for unescaped JSON closing braces; corrected shared Catalog regex, then verified success. Evidence: docs/testing/results/2026-10-11_VGL_V3_EMULATOR_ANDROID_RUNTIME_RESULT.md and docs/testing/VGL_V3_EMULATOR_RED_TEAM_REVIEW_2026-10-11.md. No Android HTTP yet, no production app adapter, no Bluetooth/P2P, no firmware peer identity, no actual decodable photo, NO remote original preservation. The previous 96 offline scenario IDs remain OFFLINE SUBSET RUN; INTEGRATION PENDING rather than upgraded to end-to-end. Remote JPG GET+EXIT previously consumed original; do not issue real GET or install a physical APK without separately scoped authorization. Existing frozen releases unchanged. **G6A OPEN; G6B BLOCKED.**


### 2026-10-11 V3B Android emulator actual process-death recovery (NOT physical gate)
Pure synthetic API36 Android emulator app-private storage using shared RecoveryImportStore, one virtual phone, zero manifest permissions. Source commit 6bde5ef0451984e5dd812d0159ecc277bc216aad. GitHub 38163205078 SUCCESS: real JVM `Runtime.halt(73)` after fetch marker, after verified journal, and during a three-byte partial write, followed by independent fresh-process recoveries; no second byte Source; incomplete bytes still quarantined. Companion CI compile/96/offline/hygiene SUCCESS. See docs/testing/results/2026-10-11_VGL_V3B_ANDROID_ABRUPT_PROCESS_RECOVERY_RESULT.md and docs/testing/VGL_V3B_ANDROID_HARD_STOP_RED_TEAM_REVIEW_2026-10-11.md. Not full phone power cut, actual firmware/media, original photo retention, or production Android sync; G6A OPEN/G6B BLOCKED.


### 2026-10-12 V3C six-boundary Android emulator hard-stop result — software-only PASS
Actual API36 emulator run 38169936791 SUCCESS: 6/6 abrupt JVM process terminations after transaction barriers with fresh-process local recovery; normal 2/2 storage instrumentation PASS. Detailed report docs/testing/results/2026-10-12_VGL_V3C_ANDROID_SIX_BARRIER_IMPLEMENTATION.md. This upgrades simulated Android recovery evidence only; not hardware power-loss, shipping Android integration, actual consumptive-transfer protection, or original-preservation proof. G6A OPEN; G6B BLOCKED.


### 2026-10-12 V4 RISK-FIRST DEVELOPMENT OVERRIDE — STEPS 07–09 SOFTWARE-SCOPE PASS
The current authority is `docs/testing/V4_RISK_FIRST_PLAN_2026-10-12.md`, not the superseded HTTP-first discussion. Step 01 source audit and Steps 02–04 preservation/identity static review are committed; Step 05 pure-Java interfaces and early Step 06 shipping-default-deny guard verified. The **same new** `SharedTransferCoordinator` completes one host-only synthetic catalog -> exact +1 opaque JPG -> actual `RecoveryImportStore` local receipt -> synthetic EXIT sequence, plus 43 adversarial assertions. Java host CI run `38277694218` SUCCESS; hygiene `38277694277` SUCCESS; report `docs/testing/results/2026-10-12_V4_STEPS07_08_SHARED_HOST_INTEGRATION.md`. Step 09 source-only **compile** of unchanged shared controller into current Android API36 zero-permission simulator PASS, run `38278073484`, after correcting a separate CI preflight relative path. No APK was built or installed; Android emulator run of the *new* coordinator (Step 10) not done. Report `docs/testing/results/2026-10-12_V4_STEP09_API36_SHARED_CONTROLLER_COMPILE.md`. All new host/Android inputs remain injected, not live phone/glasses evidence. JPG is a shape-only marker fixture, not a decoded photograph. No original-preserving real media method verified; actual P2P peer identity remains unknown. **Physical G6A OPEN; G6B BLOCKED.** Real media GET and physical-test APK remain prohibited without independent evidence and separate authorization.


### 2026-10-12 V4 ANDROID SAME-COORDINATOR RUNTIME — STEPS 10–11 BOUNDED PASS
The same `SharedTransferCoordinator` class as the verified JVM host path now runs in one real API36 Android emulator under a separate AndroidTest-only synthetic fixture permit. GitHub run [38280843057](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38280843057) SUCCESS, exact source SHA `46c242cd183a116521a3d81477903dddcbef3695`; matched Android compile 38280843127, offline 38280843183 and hygiene 38280843017 all SUCCESS. New integration commits an Android-generated **decodable 32x24 JPG** with durable local receipt, independently reopens after force stop, and survives real instrumentation process `Runtime.halt(73)` **at new-coordinator VERIFIED journal**, recovering entirely from local disk with no second synthetic Source. This is not real phone power loss, actual AIMB-G1 JPEG/EXIF fidelity, MP4/OPUS, or proof of glasses original retention. Full report: `docs/testing/results/2026-10-12_V4_STEP10_11_ANDROID_SHARED_RUNTIME_AND_FULL_JPEG.md`. Step 10 PASS in emulator synthetic scope. Step 11 PASS for generated JPEG encode/decode dimensions and stored/recovered decoding, but not vendor metadata parity. Steps 12–14 remain open; final release and physical evidence still blocked. **G6A OPEN; G6B BLOCKED.**


### 2026-10-12 V4 FINAL SOFTWARE-LAB CHECKPOINT — NO PHYSICAL GATE PROMOTION
New same `SharedTransferCoordinator` API36 synthetic run 38280843057 PASS (one real Android Bitmap-generated/decoded 32x24 JPG, persistent file/receipt, independent restart, fresh-process VERIFIED-journal `Runtime.halt(73)` recovery). Source/host Step 07–08 integration evidence retained unchanged. New offline `MixedMediaRecoveryStore` adds durably journalled, shared daily JPG/MP4/OPUS numbering and conservative old-JPG reservation migration. Final host 38 assertions in run 38282545306 PASS and latest same-source API36 mixed runtime 38282672292 PASS (mixed receipt reopen, interrupted MP4 local recovery, AndroidTest-only fixture location preflight). Details: `docs/testing/results/2026-10-12_V4_STEP10_11_ANDROID_SHARED_RUNTIME_AND_FULL_JPEG.md`, `docs/testing/results/2026-10-12_V4_STEP12_MIXED_DURABLE_HOST_ANDROID.md`. Step 13 release isolation audit recorded but no real signed production APK to certify; Step 14 software/hardware convergence decision is HOLD in `docs/testing/V4_STEP14_SOFTWARE_HARDWARE_CONVERGENCE_DECISION_2026-10-12.md`. Production BLE/P2P/HTTP adapters NOT complete; vendor MP4/OPUS/JPG metadata parity NOT proven; actual P2P peer UNKNOWN; physical GET+EXIT original JPG disappearance still blocks the preserve-originals objective. **C1 HOLD, C2 BLOCKED, C3 NO-GO, C4 BLOCKED. G6A OPEN, G6B BLOCKED.** No physical glasses or authorized real media GET.


### 2026-10-12 — REAL K SITE CAPTURE OFFLINE READ-ONLY ANDROID PREVIEW: SOFTWARE-SCOPE PASS
A minimal **local-only** Site Capture facade and read-only Android UI source now live under `apps/site-capture/`. Host offline facade test 38284068924 PASS (16 assertions). Hardened zero-permission Android API36 compile [38285067603](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38285067603) SUCCESS with class-absence guard: shared remote-transfer controller / host test-grant not compiled into preview, no APK produced in that compile lane. API36 virtual UI run [38285067667](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38285067667) SUCCESS: temporary virtual-only no-permissions preview displayed mandatory `Media transfer disabled` plus local archive. Initial Gradle Kotlin sourceSet exclusion broke two earlier compile runs; fixed to JavaCompile exclusion and all final gates passed. Details `docs/testing/results/2026-10-12_V4_SITE_CAPTURE_PRODUCTION_OFFLINE_UI_SCAFFOLD.md`. This is not a live BLE/P2P/HTTP integration or authorized physical-test APK and does not establish original preservation. Public user manual says `Import` saves phone copy but does not document original retention. Evidence gaps for vendor documented preservation listed in `docs/research/G6A_ORIGINAL_PRESERVATION_VENDOR_EVIDENCE_REQUEST_2026-10-12.md`. **C1 production HOLD; C2 BLOCKED; C3 NO-GO; C4 BLOCKED. G6A OPEN; G6B BLOCKED.**


### 2026-10-13 — READ-ONLY SITE CAPTURE UI WITH NONEMPTY DURABLE STORE + ANDROID15 RUNTIME: SOFTWARE PASS
`apps/site-capture/android-preview/` now tests the real read-only Activity against 3 synthetic app-private JPG/MP4/OPUS receipts and a separate process restart, then uncertain remote state and intentionally corrupt local journal. API36 [38340827600](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38340827600) **4/4 UI PASS**. Separate Android15/API35 [38341334991](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38341334991) **4/4 UI PASS** with preview `minSdk=35`, Android16/API36 same-source regression [38341308157](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38341308157) **PASS**, compile-only [38341320200](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38341320200) PASS. Documents: `docs/testing/results/2026-10-13_V4_SITE_CAPTURE_POPULATED_ARCHIVE_ANDROID_RUNTIME.md` and `docs/testing/results/2026-10-13_V4_SITE_CAPTURE_API35_API36_COMPATIBILITY.md`. Emulator ephemeral APKs only; preview still omits coordinator/test-grant, uses zero permissions and cannot GET/associate with real glasses. Vendor MP4/OPUS validity, real peer identity, original-on-glasses persistence and production release are still unverified; **C1 HOLD, C2 BLOCKED, C3 NO-GO, C4 BLOCKED. G6A OPEN; G6B BLOCKED.**


### V5.1 2026-10-13 — CYAN ORIGINAL-DEX DOWNLOAD/EXIT STATIC AUDIT: 27/27 PASS, PRESERVATION NO-GO
The original user-supplied Cyan APK `base.apk` SHA256 `1e700628d76fa4fa84047632e2ccce3673f1a01966fbf8c583985568f3aaaf64` and all six extracted embedded DEX files were privately hash/byte-compared; method-table/DEX invoke offsets independently rechecked `AlbumDepository.downloadGlassFile`, success/error callbacks, phone-local `processDownloadedFile`, empty-phone-queue `fileDownloadComplete`, BLE EXIT `02 01 09`, Wi-Fi Direct teardown and local `GFileUtilKt.deleteFile`. 27/27 offline assertions PASSED. Local `BlockingDeque.removeFirst` and `File.delete` **cannot** be treated as glasses-side deletion commands. **But** v0.6.7 physical one-JPG GET+EXIT still removed the exact remote catalog identity; v0.6.5 no-GET EXIT-only control retained it. Cyan's normal error path retries media request, and its exact EXIT callback does not wait for acknowledgement before scheduled cleanup; copying those assumptions would be unsafe. READ `docs/research/V5_1_CYAN_MEDIA_DOWNLOAD_EXIT_FORENSICS_2026-10-13.md`, `docs/research/V5_1_CYAN_MEDIA_EXIT_RED_TEAM_2026-10-13.md`, `docs/testing/results/2026-10-13_V5_1_CYAN_MEDIA_EXIT_STATIC_AUDIT.md`. Full remote firmware causality and non-consumptive transfer remain **OPEN**. No media GET, radio, physical APK, release, or glasses operation in V5.1. **G6A OPEN; G6B BLOCKED; C1 HOLD; C2 BLOCKED; C3 NO-GO; C4 BLOCKED.** Next V5.2 research-only cause matrix and vendor evidence.


## 2026-10-31 — V5.2 OFFLINE CAUSAL / MANUFACTURER EVIDENCE SLICE COMPLETE
No V5.1 APK/DEX work repeated. V5.2 reports:
- `docs/research/V5_2_GET_EXIT_CAUSAL_EVIDENCE_MATRIX_2026-10-31.md`
- `docs/research/V5_2_MANUFACTURER_TRANSFER_PRESERVATION_EVIDENCE_2026-10-31.md`
- `docs/research/V5_2_RED_TEAM_AND_DECISION_2026-10-31.md`

Evidence boundary: the existing successful JPG GET followed by normal EXIT/teardown/reconnect is associated with disappearance of that identity from the remote catalog; no-media-GET EXIT control retained its JPG. Neither isolates the causal event. Catalog absence is not proof of physical erasure. Public product/manual material documents Cyan/CyanGlasses app import over Wi-Fi, but no reviewed source guarantees preservation or documents an original-preserving alternative. Manufacturer request is drafted, not sent. No device access, media GET, BLE command, APK build/install, or physical test occurred. This is research only; **C3 NO-GO, G6A OPEN, G6B BLOCKED, C1 HOLD, C2 BLOCKED, C4 BLOCKED.** Keep real media GET disabled. Next: obtain exact model/firmware-scoped authoritative support evidence; do not infer permission for a hardware test from these reports.

## 2026-10-31 — V5.4 PUBLIC PROTOCOL IMPLEMENTATION AUDIT
Independent public projects confirm the same BLE + Wi-Fi Direct HTTP architecture: `/files/media.config` manifest, `/files/<filename>` payloads, local gallery saving and transfer-mode exit. They add transport reliability and retry handling but do not document original preservation, a read-only GET, a keep-original flag, an alternate endpoint or a way to distinguish catalog hiding from storage deletion. Community implementations span multiple hardware families and firmware versions, so their behavior cannot be assigned to this AIMB-G1 unit. This evidence strengthens protocol architecture only; **C3 NO-GO, G6A OPEN, G6B BLOCKED.** Report: `docs/research/V5_4_PUBLIC_PROTOCOL_IMPLEMENTATION_AUDIT_2026-10-31.md`.
