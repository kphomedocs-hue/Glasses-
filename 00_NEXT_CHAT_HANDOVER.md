# AIMB-G1 / K Site Capture — NEXT CHAT HANDOVER

Prepared: 2026-10-02
Repository: `kphomedocs-hue/Glasses-`
Durable authority: latest GitHub `main`
Project state: **READY TO RESUME AT v0.6.8.2 SAFE SHORT-RESPONSE DIAGNOSTIC**

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
