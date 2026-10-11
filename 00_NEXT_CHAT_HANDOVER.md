# AIMB-G1 / K Site Capture — NEXT CHAT HANDOVER

Prepared: 2026-10-02
Repository: `kphomedocs-hue/Glasses-`
Durable authority: latest GitHub `main`
Project state: **RESEARCH MISSING PRE-TRANSFER / WORK-STATE PREPARATION BEFORE ANY NEW BUILD**

---

## 1. Operating rule for the next chat

Start by reading this file, then verify latest GitHub `main`.

Do **not** rely on chat memory over repository state.

Do **not**:
- rebuild v0.6.8 unless a defect is actually found;
- start v0.6.9 before the exact v0.6.8 two-phase physical test is reviewed;
- mark a physical gate PASS from static/build evidence only;
- require a successfully transferred remote JPG to still exist on the glasses after GET;
- uninstall or clear app data between v0.6.8 Phase 1 and Phase 2.

GitHub physical evidence is authoritative.

---

## 2. Project goal

Replace the Cyan transfer layer with a minimal Android transfer app for AIMB-G1 glasses:

- glasses perform capture;
- phone transfers and stores media locally;
- deterministic local numbering;
- no cloud dependency;
- privacy-safe transfer bookkeeping;
- restart-safe no-duplicate behavior.

Current work is still G6A single-JPG persistent import/restart proof.

---

## 3. Frozen protocol / transport facts

BLE/GATT:
- Cyan service/notify/write physically present.
- CCCD notifications physically working.
- time sync: `0x40`.
- media inventory: `0x41 / 02 04`.
- P2P enter: `0x41 / 02 01 04 01`.
- P2P exit: `0x41 / 02 01 09`.

Hard transport rules:
- exact case-sensitive BLE-reported P2P peer name only;
- wait for enter write callback + valid transfer credentials;
- passive glasses IPv4 from `0x73/0x08`;
- Cyan catalog delay: 1000 ms;
- catalog endpoint: `GET /files/media.config`;
- catalog cap: 65536 bytes;
- media cap: 33554432 bytes;
- redirects disabled;
- retry/resume/Range disabled;
- no raw credential logging/persistence;
- no raw remote filename/path logging/persistence;
- no explicit glasses delete/mutation command;
- generic exit-time `0x41` does not prove exit;
- exit proof uses successful exit write callback + valid `0x73/0x01`;
- local P2P-group absence is verified after cleanup.

---

## 4. Physical gates already completed

Completed physical gates:
- G0 readiness — PASS
- G1 GATT — PASS
- G2 notify channel — PASS
- G2B response semantics — PASS
- G2C time sync — PASS
- G3 media inventory — PASS
- G3B P2P lifecycle — PASS
- G4A Wi-Fi Direct association — PASS
- G4A2 passive glasses IPv4 — PASS
- G4B/G4B2/G4B3 catalog endpoint/parser — PASS
- G5 capture visibility — PASS
- G5.7 single visibility-gated JPG transfer — PASS

G6A is still OPEN.
G6B is BLOCKED until G6A closure.

---

## 5. G6A physical evidence progression

### v0.6.4.2 — no-capture stability

Exact build:
- run `36811812916`
- commit `e97bfc4ac66d6de49007296fdfff311df129801d`

Physical result:
- bounded no-capture inventory/catalog stability PASS;
- 10 JPG + 1 OPUS remained exact across two verified snapshots;
- no media GET.

Evidence:
`releases/v0.6.4.2/builds/run-36811812916-attempt-1/reports/2026-10-01_G6A_NO_CAPTURE_STABILITY_PHYSICAL_PASS.md`

### v0.6.5 — exact one-capture pre-download retention

Exact build:
- run `36893102299`
- commit `598e9aed94546bff2c9b018114932902aca67e05`

Physical result:
- one physical capture produced exact +1 image/JPG;
- no baseline loss;
- exact new JPG survived a verified reconnect before any media GET;
- zero media GET.

Evidence:
`releases/v0.6.5/builds/run-36893102299-attempt-1/reports/2026-10-01_G6A_SINGLE_CAPTURE_RETENTION_PHYSICAL_PASS.md`

### v0.6.6 — post-GET transition reproduced

Exact build:
- run `36895589975`
- commit `1ea64b26f311e6b2f18be536e1548766dabe8aa9`

Physical result:
- pre-GET state exact;
- one JPG GET succeeded;
- immediate post-GET exit inventory no longer matched pre-GET state;
- operator reported the same stopping condition twice;
- diagnostic stopped before fresh post-GET catalog classification.

Evidence:
`releases/v0.6.6/builds/run-36895589975-attempt-1/reports/2026-10-01_G6A_V0_6_6_POST_GET_INVENTORY_MISMATCH_REPRODUCED.md`

### v0.6.7 — remote consumption semantics physically proven

Exact build:
- run `36898159938`
- attempt `1`
- commit `cb90f25b0bea9cfe308a1d79d9f4235c7f2b21fe`
- APK SHA-256 `b152a20f049d8d33e514b177d3abcb8a01dad00677a046ea58a4a8e5de9dd7ab`

Physical sequence:
- baseline: 11 images / 11 JPG / 1 OPUS;
- one capture: 12 images / 12 JPG;
- pre-GET fresh reconnect: still 12 JPG;
- exactly one JPG GET: HTTP 200, 599645 bytes, JPEG SOI/EOI PASS;
- immediate post-GET inventory: 11 images;
- fresh post-GET reconnect: 11 images;
- fresh catalog: 11 JPG + 1 OPUS;
- all 11 baseline JPG identities remained;
- exact downloaded new JPG identity was absent;
- no explicit delete/mutation command was sent.

Physical conclusion:
**On the tested AIMB-G1 path, a successful JPG GET consumes/removes the exact transferred JPG from the glasses-side inventory/catalog.**

This resolves the old v0.6.2 restart mismatch. The earlier assumption that a successfully transferred remote JPG must still exist after transfer was wrong.

Evidence:
`releases/v0.6.7/builds/run-36898159938-attempt-1/reports/2026-10-01_G6A_V0_6_7_POST_GET_REMOTE_CONSUMPTION_PHYSICAL_PROOF.md`

Known historical v0.6.7 reporting defect:
- the runtime report printed `App version: 0.6.6`;
- exact v0.6.7 source itself contains that stale hard-coded line;
- commit/run/attempt/package provenance uniquely identifies the tested APK;
- do not rebuild/rerun v0.6.7 just to correct the historical text.

---

## 6. Current and only authorized physical candidate

### v0.6.8 — persistent local import + durable receipt + restart/no-duplicate proof

Only authorized build:
- version: `0.6.8`
- run: `36903742122`
- attempt: `1`
- build commit: `cf92a44563352ed339d8dd74a825483c7dba39e4`
- package: `com.parkarsite.g6apersist68`
- APK SHA-256: `3f94a240030c10162c0025853846f6ea29860de2ed36874742cddbb2a61d4b27`
- exact source ZIP SHA-256: `0dd1535b280d383e102a338eb2a9fc5b7106438bea3eb106e56808b9b6e12d54`

Exact bundle:
`releases/v0.6.8/builds/run-36903742122-attempt-1/`

Pre-physical gates:
- safety audit PASS;
- red-team static audit PASS;
- Android compile PASS;
- Android Lint PASS;
- APK signature verification PASS;
- immutable exact-build archive PASS;
- manual storage/state-machine review PASS.

Manual review:
`releases/v0.6.8/builds/run-36903742122-attempt-1/reviews/MANUAL_RED_TEAM_PREPROMOTION.md`

Do not substitute any earlier v0.6.8 CI run.

Known nonblocking v0.6.8 issue:
- one failure-only generic message retains an older diagnostic label;
- successful headers/build identity and runtime behavior are correct;
- do not rebuild only for this wording issue.

---

## 7. v0.6.8 Phase 1 — exact physical procedure

Prerequisites:
1. install the exact authorized v0.6.8 APK;
2. fresh app data is required for Phase 1;
3. force-stop Cyan Glasses;
4. keep AIMB-G1 paired;
5. open v0.6.8;
6. start the persistent-import proof;
7. do **not** take a photo until the app explicitly says ARMED;
8. take exactly one photo;
9. do not take another photo.

The app must prove:
- exact +1 physical capture;
- exact post-capture catalog delta;
- exact pre-transfer retention;
- exactly one media GET;
- HTTP/JPEG/size validation;
- app-private partial file;
- fsync before validation/finalization;
- final numbered JPG exists;
- durable opaque receipt committed only after local file durability;
- exact transferred remote JPG is consumed;
- fresh remote catalog returns exactly to the original baseline identity set.

Expected successful network totals:
- media-count queries: 4
- P2P enters: 4
- transfer exits: 4
- catalog GETs: 4
- media-file GETs: 1
- total HTTP GETs: 5

Required Phase 1 PASS line:

`G6A PERSISTENT IMPORT RESULT: PASS — LOCAL FILE + DURABLE RECEIPT COMMITTED; EXACT REMOTE JPG CONSUMED`

After Phase 1 PASS:
- copy/preserve the complete report;
- **do not uninstall**;
- **do not clear app data**;
- force-stop the v0.6.8 app itself;
- reopen the same app.

---

## 8. v0.6.8 Phase 2 — real restart verification

After a real force-stop/reopen, the UI must load the durable receipt at process startup and enable:

`Verify after app restart — ZERO DOWNLOAD`

Phase 2 is local-only.

It must prove:
- same v0.6.8 build identity;
- receipt was not committed in the new process;
- exactly one durable receipt loads from disk;
- durable receipt status is COMMITTED;
- exactly one numbered committed local media file exists;
- local file exists;
- local byte count matches receipt;
- local JPEG SOI/EOI validates after restart;
- no duplicate local file is created;
- media-file GET requests: 0;
- total HTTP GET requests: 0;
- BLE/P2P operations: 0.

Required Phase 2 PASS line:

`G6A RESTART PERSISTENCE / NO-DUPLICATE RESULT: PASS — DURABLE LOCAL FILE + RECEIPT SURVIVED RESTART; ZERO REDOWNLOAD; ZERO DUPLICATE`

Return the complete Phase 1 and Phase 2 reports.

---

## 9. What to do when the two v0.6.8 reports arrive

First verify both headers:
- App version `0.6.8`
- Build commit `cf92a44563352ed339d8dd74a825483c7dba39e4`
- Build run `36903742122`
- Build attempt `1`

Then review Phase 1:
- exact +1 capture;
- pre-GET retention exact;
- one persistent import;
- local final file durable/JPEG-valid;
- one durable receipt;
- exact post-GET consumption;
- fresh remote catalog equals original baseline;
- exact bounded network totals.

Then review Phase 2:
- one receipt loaded at startup;
- one numbered local JPG;
- size/JPEG valid;
- zero media GET;
- zero HTTP GET;
- zero BLE/P2P;
- zero duplicate.

If and only if both reports pass:
1. store both complete sanitized reports under the exact v0.6.8 immutable build folder;
2. update v0.6.8 VERSION_MANIFEST;
3. update LATEST_CHECKPOINT.md;
4. update docs/testing/GATE_ROADMAP.md;
5. perform G6A closure review;
6. only after closure may G6B be unblocked.

Do not create v0.6.9 merely because both reports passed; first close G6A formally.

---

## 10. Current gate state

**G6A: OPEN — waiting only for v0.6.8 Phase 1 + real restart Phase 2 physical reports.**

**G6B: BLOCKED until formal G6A closure.**

There is no v0.6.9 or newer physical candidate at handover freeze.

---

## 11. Repository handover sources to read in a new chat

Read in this order:
1. `00_NEXT_CHAT_HANDOVER.md`
2. `LATEST_CHECKPOINT.md`
3. `docs/testing/GATE_ROADMAP.md`
4. `releases/v0.6.8/VERSION_MANIFEST.md`
5. `releases/v0.6.8/builds/run-36903742122-attempt-1/BUILD_INFO.md`
6. `releases/v0.6.8/builds/run-36903742122-attempt-1/reviews/MANUAL_RED_TEAM_PREPROMOTION.md`
7. v0.6.7 physical proof file listed above.

GitHub remains the durable source of truth.

---

## 12. Recommended new-chat opening prompt

`Resume my AIMB-G1 Smart Glasses / K Site Capture project from GitHub repo kphomedocs-hue/Glasses-. Read 00_NEXT_CHAT_HANDOVER.md first, verify latest main, and continue from the exact v0.6.8 two-phase physical test. Do not rely on chat memory over GitHub. Do not build v0.6.9 until the v0.6.8 Phase 1 and real restart Phase 2 reports are reviewed and G6A closure is decided.`


---

## 13. CURRENT OPERATIONAL OVERRIDE — v0.6.8.1 credential diagnostic

This section supersedes Sections 6–10 for the **next immediate physical action**.

The exact v0.6.8 persistent-import build was attempted three times on 2026-10-02 and reproduced the same baseline blocker before capture:
- media inventory succeeded at 11 images / 0 videos / 1 recording;
- P2P-enter BLE write started and its write callback succeeded;
- the required transfer-credential handshake was not accepted inside the existing 10-second window;
- zero catalog/media HTTP;
- zero import/receipt;
- no photo was taken for the test;
- the third run reproduced the same blocker after a clean glasses power cycle.

Durable evidence:
`releases/v0.6.8/builds/run-36903742122-attempt-1/reports/2026-10-02_G6A_V0_6_8_P2P_CREDENTIAL_HANDSHAKE_FAILURE_REPRODUCED_3X.md`

Do **not** keep retrying v0.6.8 unchanged.

### Current and only authorized next physical build

**v0.6.8.1 — bounded credential-handshake diagnostic**

- run: `36965814011`
- attempt: `1`
- build commit: `beceb27754f491fcae6a53433ad4dd4fcfadc22b`
- package: `com.parkarsite.g6acreddiag681`
- APK SHA-256: `01ba35d297c6d48dce31242185032d8d37ced39856419e827cc9c9013e4246dd`
- exact source ZIP SHA-256: `f3d65c6ca0bf2b8d815f5e98e62a8f8420730b350a41b6505e2072537cedd8c2`
- exact bundle: `releases/v0.6.8.1/builds/run-36965814011-attempt-1/`

All pre-physical gates PASS, including manual red-team:
`releases/v0.6.8.1/builds/run-36965814011-attempt-1/reviews/MANUAL_RED_TEAM_PREPROMOTION.md`

### Exact next physical action

1. Install the exact v0.6.8.1 APK.
2. Force-stop Cyan Glasses.
3. Keep AIMB-G1 powered and paired.
4. **Do not take any photo.**
5. Open v0.6.8.1 and tap **Start credential diagnostic** once.
6. Keep the app foregrounded until it finishes.
7. Copy and return the complete report.

The diagnostic performs no Android Wi-Fi Direct discovery/connection, no catalog or media HTTP, no media import, and no receipt/ledger writes.

Possible classifications include:
- `NORMAL_VALID`;
- `LATE_VALID`;
- `REJECTED_0x41_CANDIDATE_ACTIVITY`;
- `INVALID_FRAME_ACTIVITY_ONLY`;
- `NO_0x41_CREDENTIAL_CANDIDATE`.

Do not resume the v0.6.8 persistent-import Phase 1 until this diagnostic report is reviewed.

**G6A = OPEN.**
**G6B = BLOCKED.**


## CURRENT OVERRIDE — 2026-10-02 v0.6.8.2 safe short-response diagnostic

v0.6.8.1 physical diagnostic completed at 2026-10-02T10:29:19+0530:
- baseline inventory 11 / 0 / 1;
- P2P-enter write and callback succeeded;
- one valid `0x41` frame arrived at +72 ms;
- that frame was too short for the credential structure;
- no credential-bearing frame appeared in 10 s normal + 10 s passive late window;
- zero Wi-Fi Direct, HTTP, catalog/media GET, capture, import, or receipt activity.

Evidence:
`releases/v0.6.8.1/builds/run-36965814011-attempt-1/reports/2026-10-02_G6A_V0_6_8_1_CREDENTIAL_DIAGNOSTIC_PHYSICAL_RESULT.md`

### Current and only authorized next physical build

v0.6.8.2 safe short-response diagnostic:
- run `36967449098`;
- attempt `1`;
- build commit `863062eca47d857b1e74fe3f4662125d7f7e12d7`;
- package `com.parkarsite.g6acreddiag682`;
- APK SHA-256 `74c2141d8d42fc65a28515f7f3e779aa9d1f8101fbcf4ba802eade7d168df892`;
- exact source ZIP SHA-256 `40e2d49cbe6a62155202de113cbe43505728ff9ebe396e04fac842e625dbd3fe`.

Purpose: determine whether the safe short `0x41` response is exact ENTER-command-shaped, ENTER-prefix-plus-status, or some other payload shorter than 8 bytes. Payload hex is permitted only when declared payload length <8 bytes, which is below the preserved credential minimum structure and therefore cannot contain SSID/password value bytes.

Pre-physical gates:
- safety audit PASS;
- red-team static PASS;
- compile/lint PASS;
- signature PASS;
- immutable archive PASS;
- manual red-team PASS.

Manual review:
`releases/v0.6.8.2/builds/run-36967449098-attempt-1/reviews/MANUAL_RED_TEAM_PREPROMOTION.md`

Run once:
1. force-stop Cyan Glasses;
2. keep AIMB-G1 powered and paired;
3. do not take a photo;
4. run v0.6.8.2 once;
5. return the complete report.

Run `36967445804` is NOT PROMOTED.

**G6A: OPEN.**
**G6B: BLOCKED.**


## CURRENT OVERRIDE — 2026-10-02 final red-team-cleared v0.6.8.3 diagnostic

Second red-team review revoked v0.6.8.2 before physical use because:
- post-EXIT short-frame classification could contaminate ENTER-observation counters;
- final report wording inaccurately said no raw notification payload was logged even though safe <8-byte short payload logging was intentional.

v0.6.8.3 corrects those issues and an additional mixed-short-response classification edge case.

### Current and only authorized physical diagnostic build

- version: `0.6.8.3`
- run: `36968820818`
- attempt: `1`
- build commit: `e282da324e8a77d56b4e3cdb6ce0b97711bd22ae`
- package: `com.parkarsite.g6acreddiag683`
- APK SHA-256: `2def2dbb77b260d9d9773349df99c6affacc026e7a1d8fdd44882f2b71edb6ec`
- exact source ZIP SHA-256: `1e179e4d488b7aaaec70aabf6ead252a244f3cd5cca06c0fa3bbcd47953c5003`

Pre-physical verification:
- artifact hash independently rechecked: PASS;
- source ZIP hash independently rechecked: PASS;
- bundled safety audit re-run on exact archived source: PASS;
- bundled red-team audit re-run on exact archived source: PASS;
- compile/lint/signature/immutable archive: PASS;
- app DEX scan: no app-scope Wi-Fi Direct/HTTP/media-transfer references;
- transport UUIDs/commands unchanged from v0.6.8.1;
- post-EXIT 0x41 payload is not classified/logged;
- credential-capable payload bytes are never logged;
- mixed short-response activity has an explicit non-ONLY classification.

Manual red-team:
`releases/v0.6.8.3/builds/run-36968820818-attempt-1/reviews/MANUAL_RED_TEAM_PREPROMOTION.md`

NOT PROMOTED / do not use:
- v0.6.8.2 runs `36967445804`, `36967449098`;
- v0.6.8.3 runs `36968350504`, `36968598634`.

Next physical action:
1. install exact v0.6.8.3;
2. force-stop Cyan Glasses;
3. keep AIMB-G1 powered and paired;
4. do not take a photo;
5. run once;
6. return complete report.

**G6A = OPEN.**
**G6B = BLOCKED.**


## CURRENT OVERRIDE — 2026-10-02 v0.6.8.3 physical classification complete

Exact v0.6.8.3 diagnostic run `36968820818`, attempt 1 completed physically.

Result:
- baseline inventory 11 images / 0 videos / 1 recording;
- media-count handshake PASS;
- P2P-enter write + callback PASS;
- one valid framed short `0x41` response arrived about 100 ms after ENTER;
- declared payload length 5 bytes;
- classification `OTHER_SAFE_SHORT_0x41_ONLY`;
- no accepted credential-bearing frame in original 10-second window;
- no accepted credential-bearing frame in additional 10-second passive window;
- exit write callback PASS;
- no matching post-exit `0x73/0x01`;
- zero Wi-Fi Direct / HTTP / catalog / media / capture / import / receipt activity.

Evidence:
`releases/v0.6.8.3/builds/run-36968820818-attempt-1/reports/2026-10-02_G6A_V0_6_8_3_PHYSICAL_CLASSIFICATION.md`

Durable prior evidence relevant to the next hypothesis:
- exact Cyan static trace shows normal post-service-discovery initialization queues dynamic `0x40` time sync first;
- G2C physically proved the exact Cyan-equivalent one-command `0x40` time-sync path;
- static trace does not establish `0x40` as a handshake gate, so initialization dependency is still only a hypothesis.

Next bounded diagnostic question:
test one already-proven `0x40` initialization write immediately before media-count + P2P ENTER, with no photo, Wi-Fi Direct, HTTP, catalog/media GET, import, or receipt logic.

Do not resume v0.6.8 persistent-import Phase 1 until that diagnostic is reviewed.

**G6A = OPEN.**
**G6B = BLOCKED.**


## CURRENT OVERRIDE — 2026-10-02 v0.6.8.4 one-0x40 initialization diagnostic

v0.6.8.3 physically established:
- media-count handshake PASS;
- P2P-enter write + callback PASS;
- one prompt valid short non-credential `0x41` response;
- no credential-bearing frame within 10 s normal + 10 s passive late observation;
- zero Wi-Fi Direct / HTTP / catalog / media / capture / import / receipt activity.

Durable evidence:
`releases/v0.6.8.3/builds/run-36968820818-attempt-1/reports/2026-10-02_G6A_V0_6_8_3_PHYSICAL_CLASSIFICATION.md`

Relevant prior Cyan evidence:
- exact Cyan static trace shows normal post-service-discovery initialization queues dynamic `0x40` time sync before later initialization/media operations;
- G2C physically proved the Cyan-equivalent `0x40` builder and write path;
- static trace does not establish `0x40` as a handshake gate, so this remains a bounded hypothesis test.

### Current and only authorized next physical build

v0.6.8.4 one-0x40 initialization credential diagnostic:
- run `36973886568`;
- attempt `1`;
- build commit `506b3f311be7e2dd8a6cd5ce8f7bf36615f95e25`;
- package `com.parkarsite.g6ainitdiag684`;
- APK SHA-256 `06184f7c634ccb940fa64989c035a03ed96cf43835d0c413aea3a43931fae7e1`;
- exact source ZIP SHA-256 `16a139b81cc05caadd2f05b93d74bea4b4be46ddc1e17f4c84af7c006f112789`.

Sequence:
1. subscribe;
2. exactly one dynamic Cyan-equivalent `0x40` time-sync write;
3. wait only for its BLE write callback;
4. exactly one `0x41 / 02 04` media-count query;
5. exactly one `0x41 / 02 01 04 01` P2P ENTER;
6. bounded 10 s normal + 10 s passive late credential observation;
7. exactly one `0x41 / 02 01 09` EXIT.

Pre-physical verification:
- safety audit PASS;
- strengthened red-team static audit PASS;
- compile/lint/signature PASS;
- immutable archive PASS;
- downloaded APK/source hashes independently rechecked;
- bundled audits re-run from exact archived source: PASS;
- `0x40` payload builder checked field-by-field against physically proven G2C: PASS;
- transport UUIDs and media/ENTER/EXIT payloads unchanged from v0.6.8.3;
- no app-source/app-package Wi-Fi Direct, HTTP, catalog/media, capture, import, receipt, or retry path.

Manual review:
`releases/v0.6.8.4/builds/run-36973886568-attempt-1/reviews/MANUAL_RED_TEAM_PREPROMOTION.md`

NOT PROMOTED:
- v0.6.8.4 run `36973860827`;
- v0.6.8.4 run `36973982291`.

Next physical action:
- force-stop Cyan Glasses;
- keep AIMB-G1 powered and paired;
- do not take a photo;
- run exact v0.6.8.4 once;
- return the complete report.

**G6A = OPEN.**
**G6B = BLOCKED.**


## CURRENT OVERRIDE — 2026-10-02 v0.6.8.4 physical run completed

Authorized v0.6.8.4 run `36973886568`, attempt 1, was executed physically.

The added one-command initialization step completed successfully, but the subsequent transfer-entry behavior remained unchanged from the preceding diagnostic. No transfer setup response was obtained during the bounded observation.

Conclusion: the simple missing-initialization hypothesis is not supported.

Do not build another protocol variant yet. First perform a cold device-state recovery using the existing authorized diagnostic only:
- force-stop Cyan Glasses and all K G1 test apps;
- fully power the AIMB-G1 glasses off using the normal hardware method;
- leave them off briefly, then power them back on;
- do not unpair, clear app data, take a photo, or open the vendor transfer workflow;
- rerun the same authorized v0.6.8.4 once after recovery.

If the transfer setup response returns after cold recovery, classify the blocker as transient device state. If behavior is unchanged, continue root-cause analysis before any new command/build.

G6A remains OPEN. G6B remains BLOCKED.


## CURRENT OVERRIDE — 2026-10-02 17:05 +0530 cold-recovery rerun hit GATT timeout

Exact authorized v0.6.8.4 run `36973886568`, attempt 1, was retried after cold device recovery.

Observed:
- bonded devices total: 7;
- exactly one AIMB-G1-family bonded match;
- target identity resolved;
- GATT did not reach CONNECTED within the 30-second bound;
- diagnostic ended as `PRECONDITION_OR_TRANSPORT_FAILURE`.

No protocol stage was reached:
- 0x40 writes: 0;
- media-count writes: 0;
- P2P-enter writes: 0;
- transfer-exit writes: 0;
- Wi-Fi Direct / HTTP / catalog / media / capture operations: 0.

Evidence:
`releases/v0.6.8.4/builds/run-36973886568-attempt-1/reports/2026-10-02_G6A_V0_6_8_4_COLD_RECOVERY_GATT_TIMEOUT.md`

Interpretation:
this run does not retest the P2P credential blocker. It failed earlier at BLE transport establishment. The Android bonded record is present, but that does not prove the glasses were BLE-connectable/advertising during the attempt.

Next action is transport recovery only, not a new build:
1. force-stop Cyan Glasses and v0.6.8.4;
2. turn phone Bluetooth OFF;
3. fully power AIMB-G1 OFF;
4. wait briefly;
5. turn phone Bluetooth ON;
6. power AIMB-G1 ON;
7. wait for Bluetooth to settle before opening any app;
8. do not unpair, clear app data, open Cyan, or take a photo;
9. run the same exact v0.6.8.4 once.

Because the failed rerun reached zero proprietary writes, one transport-precondition retry is authorized.

G6A remains OPEN. G6B remains BLOCKED.


## CURRENT OVERRIDE — 2026-10-07 final pre-handover status

Latest exact physical evidence:
- v0.6.8.4 run `36973886568`, attempt 1, commit `506b3f311be7e2dd8a6cd5ce8f7bf36615f95e25`;
- GATT transport recovered and connected normally;
- service discovery and notifications passed;
- one Cyan-equivalent `0x40` write and response were observed;
- media-count handshake passed;
- current inventory was 0 / 0 / 0;
- P2P-enter write/callback passed;
- the same short non-credential `0x41` response returned;
- no credential-bearing frame appeared in 10 s normal + 10 s passive late observation;
- exit write callback passed;
- no matching post-exit `0x73/0x01`;
- zero Wi-Fi Direct / HTTP / catalog / media / capture activity.

Evidence:
`releases/v0.6.8.4/builds/run-36973886568-attempt-1/reports/2026-10-07_G6A_V0_6_8_4_TRANSPORT_RECOVERED_CREDENTIAL_BLOCKER_PERSISTS.md`

### What is now ruled out
- transient BLE/GATT failure as the main blocker;
- simply waiting longer for the credential frame;
- credential parser boundary as the obvious primary fault;
- missing single `0x40` time-sync initialization;
- media inventory count as the main cause.

### Historical controls that still matter
- G3B v0.3.1 physically succeeded with direct subscribe -> `02 01 04 01` -> credential response -> EXIT.
- v0.6.7 physically completed four P2P-enter cycles on 2026-10-01.

### Rethink direction
Do not keep repeating v0.6.8.4 and do not create another timeout/parser variant.

Current research priority:
1. reconstruct the exact Cyan/Oudmon pre-transfer state preparation beyond single `0x40`;
2. determine whether device work-state reset / transfer-finished / P2P-reset semantics are required before ENTER;
3. compare the successful G3B/v0.6.7 path against the currently failing state;
4. only after that, authorize the smallest bounded physical test.

External implementation evidence worth reviewing:
- some Oudmon/HeyCyan implementations explicitly use workType 15 as a P2P/reset-state operation;
- workType 9 is treated as transfer-finished cleanup;
- one implementation documents a broader pre-transfer preparation sequence including battery, time sync, device info, wear support, volume, and media-count activity before transfer mode.

These external semantics are hypotheses/supporting evidence only until matched against Cyan/repository evidence. Do not promote them to fact without confirmation.

### Gate state
- G6A = OPEN.
- G6B = BLOCKED.
- No new APK is authorized yet.

## CURRENT UPDATE — 2026-10-11 automated v0.6.8.5 CI build verified

- Automatic CI builds have been restored for changes to `source/v0.6.8.5/**`, its build workflow, or exact-build archive script on `main`; manual dispatch remains available.
- Release archive-only commits do **not** trigger another build; workflow concurrency serializes v0.6.8.5 builds.
- First automatic build `38116492149` failed before compilation because the CI runner lacked `rg` (ripgrep). Both static audits were changed to portable `grep`; no new glasses protocol commands were introduced.
- Successful automatic CI run: `38116572973`, attempt `1`, exact source commit `c38203f6e96044b7d36b6b6c12d1775079d750e9`.
- Safety audit, red-team static audit, host-JVM regression checks, Android compilation, Android lint, APK signature verification, immutable release archiving, and Actions artifact upload: **PASS**.
- Exact build: `releases/v0.6.8.5/builds/run-38116572973-attempt-1/`.
- APK SHA-256: `49a134cc3e971faf1f9c3c5560cdbe4bf674b058ab58f64e1213469bdce9ddaf`.
- Source ZIP SHA-256: `2d1f149a8f62ef3043bcb4d8b669ebb31a78d6aba14478c8526b8e518deb152c`.
- **Build success is NOT physical authorization.** Separate exact-build manual lifecycle/provenance review and explicit physical promotion are still required; do not install or run the APK yet.
- **G6A OPEN. G6B BLOCKED.**

## CURRENT OVERRIDE — 2026-10-11 v0.6.8.5 exact-build manual review and bounded physical authorization

The one-time v0.6.8.5 diagnostic CI candidate is now **authorized for ONE supervised bounded PHYSICAL DIAGNOSTIC ONLY** after an independent exact-build manual review. This supersedes earlier generic "not authorized" wording in source-level prebuild documentation; it does not authorize a production APK or G6A closure.

- Exact build source commit: `c38203f6e96044b7d36b6b6c12d1775079d750e9`.
- GitHub Actions run: `38116572973`, attempt `1`; CI conclusion: **SUCCESS**.
- Exact immutable bundle: `releases/v0.6.8.5/builds/run-38116572973-attempt-1/`.
- APK SHA-256: `49a134cc3e971faf1f9c3c5560cdbe4bf674b058ab58f64e1213469bdce9ddaf`.
- Exact source ZIP SHA-256: `2d1f149a8f62ef3043bcb4d8b669ebb31a78d6aba14478c8526b8e518deb152c`.
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



## CURRENT RESEARCH OVERRIDE — 2026-10-11 assumption-first closure
After the full 90-row audit, compare exact K v0.4.0/v0.6.7/v0.6.8.5/v0.6.8.6 source and original Cyan DEX. Conclusions and adversarial blockers recorded in docs/research/G6A_ASSUMPTION_FIRST_STATIC_CLOSURE_2026-10-11.md and docs/research/G6A_ASSUMPTION_FIRST_RESOLUTION_REGISTER_2026-10-11.md. An old K credential-bearing 0x41 gate is an app-level prerequisite, not required by WPS-PBC WifiP2pConfig. Cyan creates candidate Wi-Fi name using paired Bluetooth name/address; observed OTHER peer still does not match full normalized suffix and cannot be identified without further evidence. No unsupported peer fallback or current physical association. Media GET is consumptive for observed JPG. Durability under crashes and preservation policy are open. No current test authorization. G6A OPEN; G6B BLOCKED.


## CURRENT OVERRIDE — 2026-10-11 Step 1/Step 2 static investigations and red-team complete
Read docs/research/G6A_STEP1_EXACT_WIFI_IDENTITY_STATIC_COMPARISON_2026-10-11.md, docs/research/G6A_STEP2_NONCONSUMPTIVE_MEDIA_PATH_AUDIT_2026-10-11.md and docs/research/G6A_STEP1_STEP2_INDEPENDENT_RED_TEAM_2026-10-11.md. Candidate Cyan name constructed from Bluetooth name/address; K's credential-password requirement is application gating not WifiP2pConfig input. v0.6.8.6 OTHER peer was neither family nor paired full suffix and cannot be identified without fresh trusted evidence. P2P/config1 media GET demonstrably followed by disappearance of exact remote JPG; no safe preservation alternative known from examined evidence. AP/config2 not interchangeable, no destructive probing. Removal instant/trigger not pinpointed (GET plus exit interval). No physical run or new APK authorized. G6A OPEN; G6B BLOCKED.


## CURRENT OVERRIDE — 2026-10-11 G6A Step 3 offline recovery lab COMPLETE (no APK)
Verified GitHub CI run 38125598949: 17/17 crash/receipt process-exit and negative tests PASS; repository hygiene 38125598946 PASS. Transport-free reference tools/g6a_consumptive_recovery_lab.py, tests tools/test_g6a_consumptive_recovery_lab.py and dedicated CI workflow are archived. Read docs/research/G6A_STEP3_CRASH_SAFE_OFFLINE_LAB_RESULT_2026-10-11.md and G6A_STEP3_OFFLINE_RECOVERY_RED_TEAM_2026-10-11.md plus G6A_PEER_IDENTITY_NEXT_EVIDENCE_CONTRACT_2026-10-11.md. The model persists IN_FLIGHT before remote action, retains partials, refuses auto-repeat, validates ready marker/bytes, and recovers locally if complete verified bytes survive. This is ONLY a Linux offline fault model, not deployed Android code or device proof. Old v0.6.8 source and all physical build folders unchanged. Do not use unknown peer, force AP fallback or claim glass originals retained. G6A OPEN; G6B BLOCKED.


## LATEST G6A STEP 3 RESULTS — OFFLINE CODE ONLY (2026-10-11)
Read `source/g6a-recovery-core/README.md` and `docs/research/G6A_STEP3_OFFLINE_CONSUMPTIVE_RECOVERY_RED_TEAM_2026-10-11.md` before implementation changes. A new separate Java 17 recovery model passed 85 synthetic/abrupt-JVM-stop assertions; GitHub Actions fault test 38125976170 PASS; repository hygiene 38125976168 PASS. It durably records possible fetch start before invoking byte writer; interrupted transfer remains REMOTE_UNCERTAIN with quarantine, no auto-retry; after durable VERIFIED state it can restore local bytes, final path and receipt without a remote GET. Strictly no Android/BLE/HTTP, no APK, no physical test. This DOES NOT establish end-to-end exactly-once, actual power-cut safety, current P2P peer identity or remote-original preservation. Historical releases remain frozen. G6A OPEN, G6B BLOCKED.


## LATEST: G6A source-only identity review completed 2026-10-11
Read source/g6a-identity-core/README.md and docs/research/G6A_STEP4_PEER_IDENTITY_RED_TEAM_AND_TEST_SPEC_2026-10-11.md. Strict offline candidate lab 54 Java assertions PASS (CI 38126649073) and repository hygiene PASS (38126649087). Exact Cyan BLE-derived Wi-Fi candidate formula is implemented only as synthetic string comparator, never used to connect; name equality is not authentication. K v0.6.8.6 single OTHER peer cannot be explained by losing AIMB model prefix when Bluetooth address is the same; still cannot determine unrelated/stale/alternate identity/no advertisement. Do not repeat old v0.6.8.6 diagnostic or connect unknown peer. Physically nonconsumptive JPG import and end-to-end sync remain unproven. No APK/physical run approved. G6A OPEN; G6B BLOCKED.


## NEW RETRY FINDING — 2026-10-11 Cyan raw-address peer fallback is untested by K v0.6.8.6
Research file: docs/research/G6A_STEP4_RETRY_ORIGINAL_DEX_RAW_ADDRESS_BLIND_SPOT_2026-10-11.md. The authentic Cyan DEX was directly re-parsed: UserConfig.getDeviceAddressNoClear gets raw BluetoothDevice.getAddress, and PictureFragment.onPeersChanged uses a separate raw-address endsWith fallback after stored-name equality. The single OTHER peer in K v0.6.8.6 was classified with 12-hex-colon-free suffix and generic AIMB prefix ONLY, so a colon-format suffix (without AIMB prefix) would have appeared OTHER despite meeting Cyan's alternate predicate. Unknown if the actual peer had that format; never connect by inference. Exact frozen v685 and v686 matchers are compiled in the new source/g6a-identity-core/ tests: 77 synthetic assertions PASS on run 38127615703, hygiene run 38127615701 PASS. Next potential safe observation would need an additional aggregate raw-BLE-address suffix bucket and phase timing, separately reviewed and authorized. No new APK, reset, GET or current live identity proof. G6A OPEN; G6B BLOCKED.


## LATEST NEXT DEVELOPMENT OPTION — Virtual AIMB-G1 Lab proposal, 2026-10-11
Read `docs/testing/G6A_VIRTUAL_GLASSES_LAB_STRATEGY_2026-10-11.md` and `docs/testing/G6A_VIRTUAL_GLASSES_SCENARIO_MATRIX_2026-10-11.md` before creating more experimental APKs. 96 proposed software cases are NOT RUN. Research verified 2026 Android Emulator 36.5+ virtual Wi-Fi Direct between emulator instances, GitHub public Ubuntu hosted runner supports hardware acceleration, but two AVDs on runner with 14-GB SSD need a feasibility gate and BLE GATT hardware equivalence is NOT established. Proposed same-core three-lane harness: synthetic replay Java, single-emulator MockWebServer, then optionally dual-emulator Wi-Fi Direct; a minimal exact-device physical experiment only after independent authorization. Keep all previous release archives immutable. G6A OPEN, G6B BLOCKED.


## LATEST OVERRIDE — 2026-10-11 Virtual AIMB-G1 Lab phase 1 implemented and CI verified
New additive source-only shared-state candidate: lab/virtual-aimb-g1/ on main (source commit 1c195bd6fb6254365994520cabc48a493e6ed95c). First **24/24 executed synthetic safety assertion sets** from packs 01–03 PASS, with separate red-team negative suite; CI run 38130089425 SUCCESS, repository hygiene 38130089461 SUCCESS. Exact evidence: docs/testing/results/2026-10-11_VGL_PHASE1_24_OFFLINE_TEST_RESULT.md and docs/testing/VGL_PHASE1_OFFLINE_IMPLEMENTATION_REVIEW_2026-10-11.md. Fix history includes an initial public-hygiene failure due to fabricated address literals; corrected before final PASS. Candidate core is not integrated with Android; these are subset assertions, NOT full production-path, emulator, or hardware passes. The other 72 remain unrun. Continue packs 04–06 and recovery-port integration with red team before any APK. No APK built or physical testing authorized. Current peer unidentified, 0x41 short response unknown, originals-preserving media transfer unproven. **G6A OPEN; G6B BLOCKED.**


## LATEST OVERRIDE — 2026-10-11: ALL 96 VGL LANE A OFFLINE SUBSETS VERIFIED
Latest code verified at source commit 34a218ccec0116909cc07fbc75c2a729b63a791a. GitHub offline workflow 38131350988 SUCCESS, hygiene 38131350927 SUCCESS. All 96 distinct planned scenario IDs (12 packs x 8) executed synthetic safety-subset assertions; two red-team suites and an exact-ID manifest guard PASS. Pack 09 executes ACTUAL offline RecoveryImportStore filesystem/crash-barrier paths; the other new policy models are candidate-only and NOT wired to the shipping Android app. New audit fixes reject wrong-shaped HTTP bodies, incomplete reserved receipts, missing baseline photos, unsafe preflight cleanup and tested raw identifier/secret leaks. Initial runner-shell quotation failure in 38131306328 was fixed before final green CI. Detailed result: docs/testing/results/2026-10-11_VGL_LANE_A_96_OFFLINE_TEST_RESULT.md. Review: docs/testing/VGL_LANE_A_96_RED_TEAM_REVIEW_2026-10-11.md. This supersedes earlier statements that 72 are unimplemented, but DOES NOT constitute 96 full production-integrated PASS or any Android/emulator, peer/firmware, originals-retention or physical PASS. No APK, glasses commands or media GET authorized. Existing releases unchanged. Next priorities: single adopted core integrated into Android, safe one-emulator fake-local-server test, perhaps feasibility-gated dual AVD, and separate real identity + originals-preservation research. **G6A OPEN; G6B BLOCKED.**


## 2026-10-11 V2 VERIFIED OVERRIDE — ANDROID SOURCE COMPILES, HOST-LOCAL HTTP 7/7 PASS
Latest V2 verified source: c16b5898fd3d7caf8b2261ddec2c7ed80f5a2c91. Read docs/testing/results/2026-10-11_VGL_V2_ANDROID_COMPILE_HOST_HTTP_RESULT.md and docs/testing/VGL_V2_ANDROID_SOURCE_RED_TEAM_2026-10-11.md. Additive Android simulator-only Gradle project lab/virtual-aimb-g1/android-simulator/ compiles the ACTUAL shared VirtualGlassesCore, policies, protocol, identity and RecoveryImportStore into a no-permissions, source-only Android Activity plus compiled but UNEXECUTED instrumentation hook. API 36, JDK17, Gradle 9.6. Android CI 38132519731 SUCCESS: compile-only; verified merged manifest and NO APK built or installed, NO emulator. Recovery core Files.readString ported to strict 4 KiB bounded UTF-8 read to permit Android Java compilation; previous recovery CI 38132290876 PASS. A separate HOST JVM loopback HTTP simulator in lab/virtual-aimb-g1/host-http/ runs 7/7 synthetic fault cases (GET200, 500, 302, truncation, corrupt JPEG, consumptive-on-GET, forbidden other host) against ACTUAL RecoveryImportStore; together with 96/96 offline synthetic IDs and both red teams CI 38132519735 PASS, hygiene 38132519714 PASS. These are NOT Android emulator runtime tests, real phone tests, production adapter integration, physical identity proof or original-preserving sync. Historical remote JPG GET consumes original; OWNER REQUIREMENT remains unsatisfied. DO NOT BUILD/INSTALL EXPERIMENTAL APK OR RUN PHYSICAL MEDIA GET without separately scoped authorization. Next: one-AVD emulator-only instrumentation after specific approval, full shipping-path adapter integration, then independently verify actual peer and originals-preserving method. G6A OPEN; G6B BLOCKED.


## CURRENT V2B NEXT HANDOVER — 2026-10-11 integrated HOST-only Virtual Glasses Lab
Source-of-truth report: `docs/testing/results/2026-10-11_VGL_V2B_INTEGRATED_HOST_CORE_HTTP_RESULT.md`. Tested source `54b3540c437c39fd81e1d38132462d26de7078b1`. GitHub Actions 38137429577 SUCCESS: 96 JVM synthetic scenario checks + 7 standalone localhost HTTP faults + 10 NEW integrated core→identity→catalog/capture→localhost synthetic GET→RecoveryImportStore cases. Real shared code compiled, no copy. Host-only fake media permit exists exclusively within host-http fixture; core real mediaGetAuthorized remains false. No Android emulator booted, APK built, phone/glasses operation or authenticated peer. G6A OPEN; G6B BLOCKED. Next source-only options: harden host simulated HTTP gate/receipt paths, or request explicit authorization for a simulator-only APK/emulator test with no physical radio/media. Do not interpret V2B as physical G6A or override originals-on-glasses constraint.


## LATEST OVERRIDE — 2026-10-11 V3: ANDROID EMULATOR RUNTIME PRIVATE STORAGE PASS
Actual single Google APIs Android 16/API36 emulator runtime verified on source revision 7495e9bba9a0bcf0323e859fcd19f10902f251c9. GitHub workflow https://github.com/kphomedocs-hue/Glasses-/actions/runs/38141611598 SUCCESS with exact VGL-EMU-01 PASS, VGL-EMU-02 PASS, VGL_EMULATOR 2/2 PASS. App-private no-backup RecoveryImportStore commit/receipt/reopen, injected AFTER_FETCH_MARK -> REMOTE_UNCERTAIN with no retry, and AFTER_VERIFIED_JOURNAL -> local-only recovery verified on Android. Runner force-stops app/test before independent second instrumentation session; this is NOT kill during a write or power-loss testing. Source-only Android emulator package and instrumentation were assembled as ephemeral APKs, installed and uninstalled **ONLY ON THE GitHub-hosted virtual device** with zero requested app/test permissions. No upload or physical phone APK. The initial runtime run 38141069637 failed; red-team sanitized trace in 38141319056 isolated ExceptionInInitializerError during first import, traced to Android ICU pattern differences for unescaped JSON closing braces; corrected shared Catalog regex, then verified success. Evidence: docs/testing/results/2026-10-11_VGL_V3_EMULATOR_ANDROID_RUNTIME_RESULT.md and docs/testing/VGL_V3_EMULATOR_RED_TEAM_REVIEW_2026-10-11.md. No Android HTTP yet, no production app adapter, no Bluetooth/P2P, no firmware peer identity, no actual decodable photo, NO remote original preservation. The previous 96 offline scenario IDs remain OFFLINE SUBSET RUN; INTEGRATION PENDING rather than upgraded to end-to-end. Remote JPG GET+EXIT previously consumed original; do not issue real GET or install a physical APK without separately scoped authorization. Existing frozen releases unchanged. **G6A OPEN; G6B BLOCKED.**


## CURRENT TESTING BASELINE — 2026-10-11 V3B REAL ANDROID PROCESS STOP 3/3 PASS
Read `docs/testing/results/2026-10-11_VGL_V3B_ANDROID_ABRUPT_PROCESS_RECOVERY_RESULT.md` and `docs/testing/VGL_V3B_ANDROID_HARD_STOP_RED_TEAM_REVIEW_2026-10-11.md`. Tested GitHub source `6bde5ef0451984e5dd812d0159ecc277bc216aad`. Emulator run 38163205078 SUCCESS: original V3 Android private storage 2/2 PASS, PLUS 3 real JVM hard-stop boundaries with fresh Android process recovery PASS, including precisely three preserved quarantine bytes after a midstream termination. Companion compile 38163205071, offline 96/HTTP 38163205073, hygiene 38163205106 SUCCESS. Recovery never reopens uncertain potentially consumptive source. Runner false rejection of Android INSTRUMENTATION_CODE 0 on expected process death was fixed before final green run. No real device, actual Wi-Fi Direct/BLE, media download or original-on-glasses preservation. Next: source-only production-shared controller convergence, then separately reviewed emulator-only HTTP/permission flavor; don't repeat old proof or build real-phone APK without approval. **G6A OPEN; G6B BLOCKED.**
