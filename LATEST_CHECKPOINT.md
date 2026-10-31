Warning: truncated output (original token count: 23609)
Total output lines: 1314

# LATEST CHECKPOINT

## Project
K Site Capture / AIMB-G1 glasses integration

## Authority rule

Use this file only for the **current state**.

Full historical checkpoint text was archived at:
`docs/history/LATEST_CHECKPOINT_FULL_2026-10-01_PRE_REDTEAM.md`

Version/build evidence policy:
`docs/testing/VERSION_EVIDENCE_LAYOUT.md`

## Current gate status

Physically complete:
- G0 readiness: PASS
- G1 GATT: PASS
- G2 notification channel: PASS
- G2B response semantics: PASS
- G2C time sync: PASS
- G3 media inventory: PASS
- G3B P2P lifecycle: PASS
- G4A Wi-Fi Direct association: PASS
- G4A2 passive glasses IPv4: PASS
- G4B/G4B2/G4B3 catalog endpoint/parser: PASS
- G5/G5.7 bounded single-JPG transfer path: PASS

G6A:
- persistent local JPG import: physically proven;
- persistent local ledger survives restart: proven;
- restart-safe remote identity dedup: NOT proven;
- remote post-transfer retention/catalog stability: unresolved.

G6B:
- **BLOCKED**.

## Most recent physical evidence

### v0.6.2
Part 1:
- baseline 8 JPG;
- exactly one new safe JPG imported;
- persistent file and ledger committed.

Part 2:
- restart returned to 8 JPG / 9 total catalog entries;
- committed new-item exact/basename/lowercase identity absent;
- zero media redownloads.

Exact build bundle:
`releases/v0.6.2/builds/run-36466571422/`

### v0.6.3
Baseline:
- images=8.

After ARMED and intended single capture:
- first usable passive inventory reported images=10;
- exact +1 gate failed closed;
- no Phase-B P2P/media transfer occurred.

Exact physically tested build bundle:
`releases/v0.6.3/builds/run-36585496838/`

APK SHA-256:
`5585bbc9d58082ee1c178f622236c76cafb68b456bcaaf2ed7e1a3fb2fa4bef1`

## No-capture diagnostic status

### v0.6.4
Status: **SUPERSEDED / DO NOT USE**.

Reason:
- logic review found insufficient PASS validation.

### v0.6.4.1
Status: **RED-TEAM BLOCKED / DO NOT PHYSICALLY TEST**.

The build compiled and passed its static audit, but the red-team recheck found material issues:
1. exit response accepts any valid `0x41` while in EXIT_SENT;
2. direct failure/onStop paths can abandon transfer/P2P state without the normal bounded exit/cleanup path;
3. “exact” Wi-Fi Direct peer match is case-insensitive;
4. same-version rebuilds overwrote version-root APKs and broke build↔report provenance;
5. archive source ZIP can diverge from APK build commit because the script pulls mutable `main` before source archiving;
6. manifests became stale after same-version rebuilds;
7. deterministic truncated per-file identity tokens are unnecessary privacy exposure;
8. trimmed catalog lines can mask raw identity changes;
9. debug-signing continuity for in-place upgrades is not proven.

Red-team report:
`releases/v0.6.4.1/reviews/2026-10-01_RED_TEAM_RECHECK.md`

Preserved initial v0.6.4.1 build:
`releases/v0.6.4.1/builds/run-36808762256/`

No v0.6.4.1 build has been physically approved.

## Evidence-layout repair

Exact immutable build bundles now exist for:
- v0.6.1 run `36461373394`
- v0.6.2 run `36466571422`
- v0.6.3 run `36585496838`
- v0.6.4.1 run `36808762256`

Physical reports are tied to exact build folders where applicable.

Version-root APKs are no longer authoritative when multiple builds exist.

## New physical observation — v0.6.4 no-capture run

A physical v0.6.4 report generated `2026-10-01T08:41:51+0530` has been recorded as **diagnostic evidence only**.

Observed:
- Snapshot A BLE inventory: images=10, videos=0, recordings=1;
- Snapshot A catalog: 10 JPG + 1 OPUS;
- Snapshot B BLE inventory: images=10, videos=0, recordings=1;
- Snapshot B catalog: 10 JPG + 1 OPUS;
- all 10 JPG opaque identities unchanged;
- all 11 safe catalog identities unchanged;
- 2 inventory queries;
- 2 P2P enters;
- 2 transfer exits;
- 2 catalog GETs;
- 0 media GETs;
- 0 glasses mutation/deletion.

Interpretation:
- during this specific 30-second no-capture interval, inventory and catalog were stable and internally consistent at 10 JPG;
- this argues against continuous spontaneous churn during that interval;
- it does **not** explain the earlier 8→10 transition;
- it does **not** promote v0.6.4 to a valid gate build because v0.6.4 remains red-team-invalid.

Exact APK provenance is unresolved because two v0.6.4 binaries existed under the same version string. Both exact builds are now preserved:
- `releases/v0.6.4/builds/run-36806675980/` — APK SHA `5b59c2b5...`;
- `releases/v0.6.4/builds/run-36809242952/` — APK SHA `7ad38e64...`.

The v0.6.4 runtime source blob is identical in both build commits, but the physical report does not contain an APK hash, so it is stored at:
`releases/v0.6.4/observations/2026-10-01_NO_CAPTURE_STABILITY_UNRESOLVED_BUILD.md`.

## v0.6.4.2 engineering result

v0.6.4.2 was built and red-teamed in three immutable attempts.

- run `36811380575`: NOT PROMOTED — manifest-generator quoting defect.
- run `36811594297`: NOT PROMOTED — manual state-machine review found P2P discovery could begin before enter write-callback confirmation.
- run `36811812916`: CI/static gates PASS and manual state-machine/provenance red-team PASS.

## Historical v0.6.4.2 physical candidate — completed

**v0.6.4.2 — exact immutable build run `36811812916`, attempt `1`.**

Exact bundle:
`releases/v0.6.4.2/builds/run-36811812916-attempt-1/`

APK:
`releases/v0.6.4.2/builds/run-36811812916-attempt-1/K_G1_G6A_No_Capture_Stability_v0_6_4_2.apk`

Build commit:
`e97bfc4ac66d6de49007296fdfff311df129801d`

APK SHA-256:
`550d4935f17ed6250feda603a9d2963e79aaa716dd20ed59be00124e97f4ae99`

Exact source ZIP SHA-256:
`5a353681d94957025b6c9c9e4db2c4699ea16e36713cc562caec80fac73b567d`

Package:
`com.parkarsite.g6astability642`

Pre-physical gates:
- safety audit: PASS;
- red-team static audit: PASS;
- compile/lint: PASS;
- APK signature verification: PASS;
- immutable exact-build archive: PASS;
- manual state-machine review: PASS;
- manual provenance review: PASS.

Manual review:
`releases/v0.6.4.2/builds/run-36811812916-attempt-1/reviews/MANUAL_RED_TEAM_PREPROMOTION.md`

Important runtime corrections:
- P2P discovery waits for BOTH enter write callback and valid credential response;
- generic exit-time `0x41` does not prove exit;
- exit requires write callback followed by matching `0x73/0x01` inventory evidence;
- bounded abort attempts one allow-listed exit when appropriate and verifies P2P-group absence;
- peer matching is exact case-sensitive BLE-reported name;
- catalog BOM/whitespace normalization is rejected;
- per-file deterministic hash tokens are not reported;
- image/video/recording counts must match JPG/MP4/OPUS catalog counts;
- report embeds build commit/run/attempt;
- zero media-file GET path.

## Last physical report incorporation recheck

The v0.6.4 physical report generated `2026-10-01T08:41:51+0530` was re-read against the v0.6.4.2 source.

Result: **materially incorporated**.

- Its useful 10→10 / identity-set-stable observation is retained as diagnostic evidence, not promoted to gate proof.
- v0.6.4.2 repeats the same bounded no-capture question because v0.6.4's exit/cleanup/provenance were not trustworthy enough for gate use.
- v0.6.4.2 adds full BLE/catalog media parity, verified group cleanup, stricter catalog identity handling, exact build provenance, dual-condition enter handoff, and stronger post-exit evidence.
- The observed count of 10 JPG is deliberately NOT hard-coded.
- The earlier 8→10 transition remains unresolved and is not explained by the old 30-second stable window.

Detailed incorporation review:
`releases/v0.6.4.2/reviews/2026-10-01_LAST_PHYSICAL_REPORT_INCORPORATION_RECHECK.md`

## v0.6.4.2 physical result — PASS

Exact authorized build:
- run `36811812916`;
- attempt `1`;
- build commit `e97bfc4ac66d6de49007296fdfff311df129801d`;
- APK SHA-256 `550d4935f17ed6250feda603a9d2963e79aaa716dd20ed59be00124e97f4ae99`.

Physical result generated:
`2026-10-01T21:51:54+0530`

Observed:
- Snapshot A inventory: 10 images / 0 videos / 1 recording;
- Snapshot A catalog: 10 JPG / 0 MP4 / 1 OPUS;
- Snapshot B inventory: 10 images / 0 videos / 1 recording;
- Snapshot B catalog: 10 JPG / 0 MP4 / 1 OPUS;
- all 10 JPG identities retained;
- all 11 safe catalog identities retained;
- no missing or unexpected identities;
- full BLE/catalog media parity true in both snapshots;
- enter write/credential handshake complete in both snapshots;
- post-exit matching `0x73/0x01` confirmation complete in both snapshots;
- P2P group absence verified after both snapshots;
- actual monotonic quiet interval: 30030 ms;
- exact operation totals: true;
- media-file GET requests: 0;
- glasses mutation/deletion: 0.

Evidence:
`releases/v0.6.4.2/builds/run-36811812916-attempt-1/reports/2026-10-01_G6A_NO_CAPTURE_STABILITY_PHYSICAL_PASS.md`

Interpretation:
- **short-window no-capture inventory/catalog stability: PASS**;
- continuous spontaneous idle catalog churn was not observed;
- this corroborates the earlier v0.6.4 10→10 observation with corrected exit/cleanup/provenance controls;
- the earlier 8→10 transition remains unexplained;
- overall G6A remains open.

## v0.6.5 physical result — PASS

Exact tested build:
- run `36893102299`;
- attempt `1`;
- build commit `598e9aed94546bff2c9b018114932902aca67e05`;
- APK SHA-256 `985edbcc5a8a7b035cd50c71771c7a51b9f166c9d6a827972b2f4b13423c477f`.

Physical result generated:
`2026-10-01T22:12:52+0530`

Observed:
- baseline BLE inventory: 10 images / 0 videos / 1 recording;
- baseline catalog: 10 JPG / 0 MP4 / 1 OPUS;
- baseline exit + group absence verified;
- one intended physical capture;
- first capture-watch inventory event: 11 images / 0 videos / 1 recording;
- passive capture delta: EXACT +1;
- active inventory independently confirmed 11 images;
- post-capture catalog: 11 JPG / 0 MP4 / 1 OPUS;
- all 10 baseline JPG identities retained;
- exactly 1 new JPG identity;
- all 11 baseline safe identities retained;
- exactly 1 new safe identity;
- post-capture exit + group absence verified;
- retention reconnect inventory: 11 images / 0 videos / 1 recording;
- retention catalog: 11 JPG / 0 MP4 / 1 OPUS;
- full safe identity set retained exactly;
- JPG identity set retained exactly;
- new JPG identity still present;
- baseline JPG identities missing after reconnect: 0;
- media-file GET requests: 0;
- exact operation totals: 3 inventory / 3 P2P enter / 3 exit / 3 catalog GET / 0 media GET.

Evidence:
`releases/v0.6.5/builds/run-36893102299-attempt-1/reports/2026-10-01_G6A_SINGLE_CAPTURE_RETENTION_PHYSICAL_PASS.md`

Interpretation:
- **single-capture exact +1 visibility: PASS**;
- **post-capture exact +1 JPG catalog delta with no baseline loss: PASS**;
- **capture-created JPG retention across verified reconnect before download: PASS**;
- the earlier v0.6.3 8→10 ambiguity was not reproduced;
- ordinary idle churn and simple capture+reconnect behavior no longer explain the earlier v0.6.2 post-transfer/restart reversion.

## Current G6A evidence boundary

Proven:
1. short-window no-capture catalog stability;
2. exact +1 physical capture visibility;
3. exactly one new JPG with no baseline loss;
4. new JPG retention across verified exit/group absence and fresh reconnect;
5. all of the above with zero media downloads.

Still unresolved:
1. whether **one media-file GET** changes remote catalog retention semantics;
2. whether local persistent import itself changes anything remotely;
3. whether restart-safe dedup can be proven once transfer-side behavior is isolated.

## Next engineering question

The next diagnostic should isolate **the media GET itself** before reintroducing persistent import/ledger logic.

Recommended next version: **v0.6.6 — single-JPG GET retention diagnostic**.

Proposed bounded chain:
1. establish a stable baseline and exact +1 capture using the now-proven v0.6.5 logic;
2. prove the new JPG persists across a pre-download verified reconnect;
3. perform exactly **one GET of that one new JPG**;
4. validate JPEG bounds/SOI/EOI, but do not commit a persistent import ledger;
5. verified transfer exit and P2P-group absence;
6. fresh reconnect;
7. one inventory + one catalog GET;
8. determine whether the exact new JPG and all baseline identities remain remotely present.

Interpretation:
- if the new JPG disappears only after the media GET, the transfer itself is implicated;
- if it survives the GET, the remaining suspect boundary moves to local persistent import/restart/dedup handling;
- if catalog membership changes in another way, classify precisely and stop.

Hard limits should remain:
- exactly one new JPG selected from set delta;
- exactly one media GET;
- no redirects/Range/retry/resume;
- 32 MiB media cap;
- JPEG validation;
- no glasses mutation/deletion;
- no raw remote filename/path persistence;
- immutable exact build provenance.

**G6A remains OPEN. G6B remains BLOCKED.**


## v0.6.6 engineering result

Purpose: isolate whether **one GET of the exact new JPG** changes remote catalog retention semantics.

Build history:
- run `36895263627`: CI/static/archive PASS, but manual review found a PRE-GET post-cleanup failure dead-end; NOT PROMOTED.
- run `36895589975`: dead-end fixed; CI/static/archive PASS; manual state-machine/provenance red-team PASS.

## CURRENT PHYSICAL CANDIDATE — v0.6.6

Only this exact build is authorized:

- run: `36895589975`
- attempt: `1`
- build commit: `1ea64b26f311e6b2f18be536e1548766dabe8aa9`
- APK SHA-256: `29920782f16e5ef5785ef62d835622b3dbff0cb1b53f0f5848306f13cf76343d`
- exact source ZIP SHA-256: `c07333baa71377d0fc8b7c8bd5b7f661946fdfef949883585c332269cd347674`
- package: `com.parkarsite.g6aget66`

Exact bundle:
`releases/v0.6.6/builds/run-36895589975-attempt-1/`

Manual red-team:
`releases/v0.6.6/builds/run-36895589975-attempt-1/reviews/MANUAL_RED_TEAM_PREPROMOTION.md`

## v0.6.6 physical sequence

1. Fresh-install exact v0.6.6.
2. Force-stop Cyan Glasses.
3. Keep AIMB-G1 paired.
4. Start the test.
5. Do not take a photo until the app explicitly says ARMED.
6. Take exactly one photo.
7. Do not take another photo.
8. The app will prove exact +1 post-capture membership and pre-GET retention.
9. Only then it will GET the exact new JPG once.
10. It validates HTTP/JPEG/size, deletes the temporary cache file, and performs verified exit + group cleanup.
11. It reconnects and checks whether the remote catalog is unchanged after GET.
12. Return the complete report.

Required report header:
- App version: `0.6.6`
- Build commit: `1ea64b26f311e6b2f18be536e1548766dabe8aa9`
- Build run: `36895589975`
- Build attempt: `1`

Successful bounded totals:
- media-count queries: 4
- P2P enter writes: 4
- transfer-exit writes: 4
- catalog GET requests: 4
- media-file GET requests: 1
- total HTTP GET requests: 5

The single media GET is the only new experimental variable. Persistent import and ledger remain disabled.

**G6A remains OPEN. G6B remains BLOCKED.**


## v0.6.6 physical result — REPRODUCED POST-GET INVENTORY TRANSITION

Exact tested build:
- run `36895589975`;
- attempt `1`;
- build commit `1ea64b26f311e6b2f18be536e1548766dabe8aa9`;
- APK SHA-256 `29920782f16e5ef5785ef62d835622b3dbff0cb1b53f0f5848306f13cf76343d`.

Supplied physical report generated:
`2026-10-01T22:36:19+0530`

Operator states the test was performed twice and the same stopping condition occurred. One complete raw report was supplied and parsed; the second repeat is recorded as operator-reported reproduction only.

Observed in the supplied run:
- baseline: 11 images / 11 JPG / 1 OPUS;
- one intended capture produced exact +1;
- post-capture: 12 images / 12 JPG / 1 OPUS;
- no baseline identity loss;
- pre-GET verified reconnect retained exact 12-JPG state;
- exactly one new JPG selected from CURRENT catalog by opaque identity;
- exactly one media GET;
- HTTP 200;
- 797965 declared bytes / 797965 downloaded bytes;
- 32 MiB cap PASS;
- JPEG SOI/EOI PASS;
- temporary app-cache file deletion PASS;
- no persistent import;
- no ledger update;
- exit write callback PASS;
- valid post-exit `0x73/0x01` observed;
- **post-exit inventory did not match the 12-image pre-GET snapshot**;
- P2P group absence still verified;
- run stopped before post-GET fresh reconnect/catalog observation.

Evidence:
`releases/v0.6.6/builds/run-36895589975-attempt-1/reports/2026-10-01_G6A_V0_6_6_POST_GET_INVENTORY_MISMATCH_REPRODUCED.md`

Interpretation:
- one media GET transport/JPEG validation: PASS;
- a post-GET inventory transition is physically observed and operator-reported as reproduced;
- this does **not yet prove** remote JPG deletion or catalog removal because the exact changed counts were not logged and the diagnostic stopped before the post-GET fresh catalog read;
- v0.6.6 should not be rerun unchanged.

## Next engineering question

Recommended next diagnostic: **v0.6.7 — post-GET inventory transition observer**.

Critical correction:
- after the one validated media GET, the first valid `0x73/0x01` following the exit-write callback must be treated as an experimental observation, not required to equal the pre-GET snapshot;
- log its exact image/video/recording/config counts;
- still require successful exit write callback and verified P2P group absence;
- then perform a fresh BLE inventory query and fresh catalog GET;
- compare that stable post-GET state against the pre-GET state.

This will distinguish:
1. transient exit-time mismatch only;
2. persistent image-count decrement;
3. downloaded JPG actually absent from catalog;
4. remote identity rewrite with same counts;
5. another class of post-GET state transition.

Do **not** reintroduce persistent import/ledger until this is resolved.

**G6A remains OPEN. G6B remains BLOCKED.**


## v0.6.7 engineering result

Purpose: classify the reproduced post-GET inventory transition instead of aborting at the first changed exit-time inventory event.

Two immutable CI builds were produced concurrently. Only the later exact run is promoted.

## CURRENT PHYSICAL CANDIDATE — v0.6.7

- run: `36898159938`
- attempt: `1`
- build commit: `cb90f25b0bea9cfe308a1d79d9f4235c7f2b21fe`
- APK SHA-256: `b152a20f049d8d33e514b177d3abcb8a01dad00677a046ea58a4a8e5de9dd7ab`
- exact source ZIP SHA-256: `f3ab19b2bdcd390319f3ee7e4659742cc3406417304fb7f29dbc2166ea164092`
- package: `com.parkarsite.g6aobserver67`

Exact bundle:
`releases/v0.6.7/builds/run-36898159938-attempt-1/`

Manual review:
`releases/v0.6.7/builds/run-36898159938-attempt-1/reviews/MANUAL_RED_TEAM_PREPROMOTION.md`

Critical runtime rule:
- baseline/post-capture/non-post-GET exit inventory mismatches remain fatal;
- only after one validated media GET, while still in the PRE_GET_RETENTION stage, a changed valid post-exit `0x73/0x01` is recorded as observation;
- exact changed image/video/recording/config/AP-only values are reported;
- exit write callback and P2P-group absence are still required;
- a fresh BLE inventory query + fresh catalog GET then determine the stable post-GET state.

Possible classifications:
1. transient exit-time transition, exact state recovers;
2. downloaded new JPG persistently absent;
3. baseline JPG membership changed;
4. persistent inventory/catalog count change;
5. identity-set rewrite/change;
6. no remote retention change.

Successful bounded totals remain:
- 4 media-count queries
- 4 P2P enters
- 4 transfer exits
- 4 catalog GETs
- 1 media-file GET
- 5 total HTTP GETs

Persistent import/ledger remains disabled.

**G6A remains OPEN. G6B remains BLOCKED.**


## v0.6.7 physical result — POST-GET REMOTE CONSUMPTION PROVEN

Exact tested build:
- run `36898159938`
- attempt `1`
- build commit `cb90f25b0bea9cfe308a1d79d9f4235c7f2b21fe`
- APK SHA-256 `b152a20f049d8d33e514b177d3abcb8a01dad00677a046ea58a4a8e5de9dd7ab`

Known report-header defect:
- report says `App version: 0.6.6`;
- exact v0.6.7 source at the tested commit contains that stale hard-coded line;
- build commit/run/attempt still uniquely identify the physical APK.

Observed:
- baseline: 11 images / 11 JPG / 1 OPUS;
- one capture: exact +1 to 12 images / 12 JPG;
- fresh pre-GET reconnect retained the 12-JPG state;
- exactly one JPG GET completed: HTTP 200, 599645 bytes, JPEG SOI/EOI PASS;
- immediate post-GET inventory changed 12 → 11 images;
- fresh reconnect remained at 11 images;
- fresh catalog contained 11 JPG + 1 OPUS;
- all 11 baseline JPG identities remained;
- the exact downloaded new JPG identity was absent;
- no persistent import or ledger was used.

Evidence:
`releases/v0.6.7/builds/run-36898159938-attempt-1/reports/2026-10-01_G6A_V0_6_7_POST_GET_REMOTE_CONSUMPTION_PHYSICAL_PROOF.md`

### Revised G6A transfer model

A successful JPG GET on this AIMB-G1 path consumes/removes that exact transferred JPG from the remote inventory/catalog.

Therefore restart-safe dedup must not require the transferred remote identity to still exist after a successful import.

Next:
- persist the transferred JPG atomically to the local numbered archive;
- commit a durable local transfer receipt only after the local file is durable;
- then perform a restart test proving the local file and receipt survive and no duplicate import/redownload occurs.

**G6A remains OPEN. G6B remains BLOCKED.**


## v0.6.8 current physical candidate — persistent import + restart receipt

Only authorized build:
- version: `0.6.8`
- run: `36903742122`
- attempt: `1`
- build commit: `cf92a44563352ed339d8dd74a825483c7dba39e4`
- APK SHA-256: `3f94a240030c10162c0025853846f6ea29860de2ed36874742cddbb2a61d4b27`
- exact source ZIP SHA-256: `0dd1535b280d383e102a338eb2a9fc5b7106438bea3eb106e56808b9b6e12d54`
- package: `com.parkarsite.g6apersist68`

Exact bundle:
`releases/v0.6.8/builds/run-36903742122-attempt-1/`

Pre-physical gates:
- safety PASS;
- red-team static PASS;
- compile/lint PASS;
- signature PASS;
- immutable archive PASS;
- manual storage/state-machine review PASS.

### Phase 1

Fresh-install this package. Force-stop Cyan Glasses. Start the persistent-import proof. Take exactly one photo only after ARMED.

A successful Phase 1 must prove:
- exact +1 capture;
- exact pre-transfer retention;
- exactly one persistent JPG import;
- final app-private numbered JPG durable and JPEG-valid;
- exactly one durable opaque receipt;
- post-transfer remote catalog equals the original baseline exactly, proving only the transferred JPG was consumed;
- exact network totals: 4 inventory / 4 P2P enter / 4 exit / 4 catalog GET / 1 media GET / 5 HTTP GET.

Copy and preserve the complete Phase 1 report.

### Phase 2 — real restart verification

Only after Phase 1 PASS:
1. do not uninstall the app;
2. do not clear app data;
3. force-stop the v0.6.8 app itself;
4. reopen the same app;
5. tap `Verify after app restart — ZERO DOWNLOAD`.

Phase 2 is local-only and must prove:
- exactly one durable receipt loaded from disk;
- exactly one numbered local JPG exists;
- byte count and JPEG validate after restart;
- zero media GET;
- zero HTTP GET;
- zero BLE/P2P operations;
- zero duplicate local file.

If both physical reports pass under this exact build, G6A has the intended evidence needed for closure review.

**G6A remains OPEN pending the two reports. G6B remains BLOCKED.**


## HANDOVER-PREP AUTHORITATIVE CURRENT STATE — 2026-10-02

This section supersedes any older section titled "current physical candidate" above. Historical sections are retained for evidence only.

### Latest physically proven transfer semantics

v0.6.7 physically established:
- one exact new JPG remains present before download;
- exactly one successful GET validates the JPG;
- immediate inventory changes by exactly -1 image;
- fresh reconnect remains decremented;
- all baseline JPG identities remain;
- the exact downloaded JPG identity is absent from the fresh catalog.

Therefore, on the tested AIMB-G1 path, **a successful JPG GET consumes/removes the exact transferred JPG from the glasses-side inventory/catalog**.

Evidence:
`releases/v0.6.7/builds/run-36898159938-attempt-1/reports/2026-10-01_G6A_V0_6_7_POST_GET_REMOTE_CONSUMPTION_PHYSICAL_PROOF.md`

Known v…11609 tokens truncated…No physical phone/glasses/real transfer, no full JPEG decode, no phone power outage, no final Android shipping-path integration, no original-retention solution. NO hardware/media test authorized. **G6A OPEN; G6B BLOCKED.**


## 2026-10-12 V3C VERIFIED — SIX REAL ANDROID JVM HARD-STOP BARRIERS PASS (EMULATOR ONLY)
Tested exact source commit 94afd36a8d7df72a0ed43fe5f0a1994ba0c05812. GitHub API36 single-emulator workflow 38169936791 SUCCESS; raw CI job log contains VGL-EMU-CRASH-01..06 PASS and `VGL_EMULATOR_HARD_STOP 6/6 PASS`, plus normal VGL_EMULATOR 2/2 PASS. Crash boundaries: AFTER_FETCH_MARK remote uncertain; AFTER_VERIFIED_JOURNAL local recovery; MID_PART_WRITE 3-byte quarantine; AFTER_PART_SYNC remote uncertain; AFTER_FINAL_MOVE local verified move recovery; AFTER_RECEIPT idempotent receipt recovery. All are genuine Android instrumentation process halts via Runtime.halt(73), followed by new-process checks. Companion compile 38169936749, offline 96+7+10 38169936818, hygiene 38169936760 all SUCCESS. Detailed report `docs/testing/results/2026-10-12_VGL_V3C_ANDROID_SIX_BARRIER_IMPLEMENTATION.md`. No real phone/glasses/BT/Wi-Fi/Internet/media source, no decodable JPEG, no Android power-cut or production shipping adapter. Never infer retention of remote JPG from local recovery. G6A OPEN, G6B BLOCKED.

## 2026-10-12 — RETHOUGHT V4 PLAN, SOURCE-BASED ONLY (NO GATE PROMOTION)
New authoritative proposed NEXT DEVELOPMENT plan: `docs/testing/V4_RISK_FIRST_PLAN_2026-10-12.md`. The previous conversational HTTP-first twelve-step plan duplicated seven localhost HTTP checks, ten integrated host cases and six Android crash barriers already PASS. Revised risk-first parallel tracks: (H) static evidence for **original-on-glasses preservation** and actual P2P identity; (S) converge shared core, catalog/transfer/store interfaces into ONE testable Android-shipping path while keeping media authorization default-off, zero-permission synthetic emulator first, realistic JPEG and persistent mixed-media numbering; (C) only later independently authorized device evidence, preservation GO/NO-GO and release qualification. 14 tightly scoped microsteps and separate gates are in the document; they are PROPOSED and NOT IMPLEMENTED. Do not add Internet permission to current isolated emulator merely to duplicate host tests. Existing results and freeze preserved. **G6A OPEN, G6B BLOCKED**.


## 2026-10-12 — V4 INITIAL EXECUTION (STEPS 01–04 RESEARCH; STEP 05 CONTRACT SKELETON)
Authoritative V4 plan remains `docs/testing/V4_RISK_FIRST_PLAN_2026-10-12.md`. Step 01 shipping/simulator source audit was committed at `0254c02d19497a35472385ac45cdb368100cd377` to `docs/testing/V4_STEP01_SHIPPING_SIMULATOR_AUDIT_2026-10-12.md`. Steps 02–04 physically bounded evidence comparison, ranked unproven preservation alternatives, and passive peer observer acceptance criteria committed at `49188f88c16016c0a655364a70357f32b50bb28d` to `docs/testing/V4_STEPS02_04_STATIC_EVIDENCE_AND_PEER_SPEC_2026-10-12.md`. Step 05 only **BEGUN**, not declared integrated: new pure Java `source/g1-shared-workflow/` contracts and isolated self-test; local Java 17 `V4-05-PURE-CONTRACT PASS` was observed. Workflow `.github/workflows/v4-shared-contract.yml` added in commit `b283fab71aa670a8b64c2de7e5ca3100efe53d66`. New GitHub Actions green run has **NOT BEEN VERIFIED** yet (commit combined-status API returned no statuses); do not call CI PASS. No Android production controller exists yet. No frozen source/release changed, no hardware/GET/APK was used. Original-on-glasses preservation still unproven; peer identity still unknown. G6A **OPEN**, G6B **BLOCKED**. Next: verify new Actions, red-team contract, then implement default-deny typed shared coordinator incrementally.


## 2026-10-12 — V4 SHARED OBSERVATION CONTROLLER + FAIL-CLOSED HOST CI VERIFIED
After Step 01 source audit and Steps 02–04 static evidence/identity requirements, a new Java 17 contract, typed per-arm cancellable scheduler and `SharedObservationController` were committed under `source/g1-shared-workflow/` (not under frozen `source/v*`). Final verified controller/test source commit `454e156713e39a270ef326282dfb0afc8b48972e`. GitHub Actions [38276020372](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38276020372) SUCCESS: `V4-05-PURE-CONTRACT PASS` and `V4-OBSERVATION-FAIL-CLOSED PASS (57 assertions)`; separate repository hygiene [38276020414](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38276020414) SUCCESS for same commit. Source review and fixes documented in `docs/testing/V4_STEP05_06_CONTROLLER_RED_TEAM_2026-10-12.md`: timer cross-instance collision eliminated via distinct cancellation handles, cleanup attempts remain independent across failure paths, synchronous timeout race stops later BLE work. Source/Store NEVER invoked, `remoteMediaGetAuthorized()` and `physicalConnectAuthorized()` hard-false even for fabricated preservation evidence. This is a **host-only research coordinator**, not final shipping production orchestration or real BLE/P2P/HTTP/Android integration. Step 05 **partial**, Step 06 default-deny **host-only guard proven**, Step 07 onward not promoted. Hardware original-on-glasses retention remains **UNPROVEN**; actual peer identity remains **UNKNOWN**. No physical phone/glasses tests, media GET or physical-test APK authorized or run. **G6A OPEN; G6B BLOCKED**.


## LATEST — 2026-10-12 V4 STEPS 07–09 VERIFIED IN BOUNDED HOST / ANDROID-COMPILE SCOPE
After the V4 Steps 01–04 static audit and Step 05/early-06 default-deny controller slice, the single new `SharedTransferCoordinator` in `source/g1-shared-workflow/src/` now verifies baseline catalog +1 opaque JPG delta, durable host-only fixture import via unchanged `RecoveryImportStore`, local receipt/reopen, and separately injected EXIT/group absence. The **public production constructor has no host test capability and always blocks remote transfer**. Package-private host-test injection exists only for offline tests. Step 07 host JVM [38277694218](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38277694218) SUCCESS: 33 new integrated assertions; Step 08 in the SAME exact run SUCCESS: 43 adversarial assertions for peer/identity revocation, no IP, +2 captures, duplicate/changed catalog, timeout, failed fixture permission and no source replay. Existing observation 57 assertions and contract tests also pass. Repository hygiene [38277694277](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38277694277) SUCCESS. Tested host commit `ba7f3e4619ed88249f531a082e6b38ad0388be7e`. Report: `docs/testing/results/2026-10-12_V4_STEPS07_08_SHARED_HOST_INTEGRATION.md`. No network calls or physical glasses. The fake JPG is **seven marker bytes, NOT a decodable photo**.
Step 09 (same exact Java source wired into Android API36 simulator sourceSet) compiled on the zero-permission Android SDK in [run 38278073484](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38278073484) **SUCCESS** at source+CI commit `ff7d3ebc71f3df89a80d6aa881328fe68e1f74de`: static permissions isolation PASS; app and AndroidTest Java compile PASS; no APK/AAB produced; no emulator or real device run. An earlier run `38277893720` FAILED in static preflight because its workflow relative source path used four ascents instead of three; error corrected before successful final run. Report: `docs/testing/results/2026-10-12_V4_STEP09_API36_SHARED_CONTROLLER_COMPILE.md`.
**Boundary:** Step 10 same-coordinator Android emulator *runtime* integration is NOT DONE; legacy Android private-storage crashes were on prior controller, not this new coordinator. Full Android BLE/P2P/HTTP adapters, JPG/MP4/OPUS fidelity/numbering and original-on-glasses preservation remain OPEN. Real Wi-Fi Direct peer identity UNKNOWN. No physical-test APK, no physical glasses access and no real media GET authorized or attempted. **G6A OPEN; G6B BLOCKED**.


## LATEST V4 OVERRIDE — 2026-10-12 STEPS 10–11 SAME SHARED COORDINATOR ON API36 EMULATOR VERIFIED
Source commit `46c242cd183a116521a3d81477903dddcbef3695` verified by GitHub Android emulator run [38280843057](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38280843057) **SUCCESS**, matching Android source compile [38280843127](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38280843127) SUCCESS, offline suite [38280843183](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38280843183) SUCCESS and hygiene [38280843017](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38280843017) SUCCESS. New AndroidTest-only synthetic injection executes the **same** `SharedTransferCoordinator` compiled into app main from `source/g1-shared-workflow/src`. It selected exact one new synthetic JPG, durably committed it via actual `RecoveryImportStore`, reopened receipt in another instrumentation process, and verified public/shipping constructor denies Source. A full **Android-generated, bitmap-decodable 32x24 JPEG** was decoded before and after storage, replacing seven-byte marker as the sole accepted new integration proof. At a new-coordinator VERIFIED journal barrier, a true instrumentation `Runtime.halt(73)` was followed by a fresh Android process that recovered local bytes/receipt with no re-fetch. Raw markers: `V4_SHARED_FULL_JPEG_COMMITTED PASS`, `V4_SHARED_REOPEN_NO_DUPLICATE PASS`, `V4_SHARED_HALT_RECOVERED_NO_REFETCH PASS`. Earlier compile failure on unavailable Android `Files.readString` corrected to bounded byte read; see `docs/testing/results/2026-10-12_V4_STEP10_11_ANDROID_SHARED_RUNTIME_AND_FULL_JPEG.md`. Older 6/6 storage-only barriers are unchanged prior scope, not counted as six newly-integrated proofs. Scope: one API36 virtual Android device, synthetic test-only JPEG/no permissions, no real glasses, Wi-Fi Direct, HTTP or APK published. Step 10 software **PASS**, Step 11 **PASS for generated full-JPEG decode only**; vendor-media fidelity and EXIF parity unknown. **Step 12 persistent mixed JPG/MP4/OPUS numbering; Step 13 final release isolation; Step 14 C1 reconciliation remain to complete**. The remote-original preservation blocker is unchanged, current real P2P identity is UNKNOWN, G6A OPEN, G6B BLOCKED. Do not build/install physical-test APK or issue real GET without separate authorization.


## FINAL 2026-10-12 V4 SOFTWARE-LAB OVERRIDE — STEPS 10–12 VERIFIED; FULL PRODUCTION NO-GO
Authoritative V4 14-step risk-first plan remains `docs/testing/V4_RISK_FIRST_PLAN_2026-10-12.md`. Same shared-controller synthetic API36 runtime & bitmap-decodable JPG verified on source `46c242cd183a116521a3d81477903dddcbef3695`: emulator [38280843057](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38280843057) SUCCESS, matching compile 38280843127/offline 38280843183/hygiene 38280843017 SUCCESS. New controller ran baseline/exact +1 synthetic JPG, durable receipt, independent fresh-process reopen and actual `Runtime.halt(73)` at VERIFIED journal followed by local-only recovery. Details `docs/testing/results/2026-10-12_V4_STEP10_11_ANDROID_SHARED_RUNTIME_AND_FULL_JPEG.md`. Synthetic JPEG 32x24 decodes on Android before and after storage; not real AIMB-G1 JPEG/EXIF proof. Previous six storage-only process barriers retained as historical, not new controller counters.

**Step 12 updated:** New `source/g1-shared-workflow/src/com/parkarsite/g1/workflow/MixedMediaRecoveryStore.java` implements offline one-shot journal/receipt with **one shared daily JPG/MP4/OPUS sequence**, Asia/Kolkata midnight rollover, separate capture/first-seen metadata, strict no overwrite/retry, VERIFIED local recovery, and conservative migration guard for pending historical JPG reservations and old/orphan receipts. Frozen JPG-only RecoveryImportStore unchanged. Final enhanced host Java 17 [38282545306](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38282545306) SUCCESS `V4-12-MIXED-DURABLE PASS (38 assertions)`. Final Android API36 recheck [38282672292](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38282672292) SUCCESS, including `V4_MIXED_JPG_MP4_OPUS_NUMBERED`, `V4_MIXED_REOPEN_NO_DUPLICATES`, `V4_MIXED_MP4_RECOVERED_NO_RETRY`, test-only injection source-placement preflight. Report: `docs/testing/results/2026-10-12_V4_STEP12_MIXED_DURABLE_HOST_ANDROID.md`. MP4/OPUS synthetic container-shape only; no real vendor media parity. Earlier enhanced host migration test failed due literal slash-t test fixture (run 38282455067), corrected before final PASS; do not count predecessor passes as final.

Step 13 source-only red-team `docs/testing/V4_STEP13_RELEASE_ISOLATION_RED_TEAM_2026-10-12.md` completed, but **full signed production APK isolation not certified**. Step 14 reconciliation `docs/testing/V4_STEP14_SOFTWARE_HARDWARE_CONVERGENCE_DECISION_2026-10-12.md`: **C1 HOLD** (no live BLE/P2P/HTTP production integration/mixed vendor fidelity); **C2 BLOCKED** (real peer unknown); **C3 NO-GO** (physically observed exact JPG disappearance after media GET+EXIT; preserving alternative unproven); **C4 BLOCKED** (no approved final sync APK/real media verification). Synthetic transfer permit is test-only but compiled coordinator still has a package-private test capability; must be explicitly stripped/audited in any future production artifact. No real glasses contacted, no physical APK built, no media GET. **G6A OPEN. G6B BLOCKED.**


## 2026-10-12 — PRODUCTION-FACING K SITE CAPTURE OFFLINE UI VERIFIED; FINAL MEDIA SYNC STILL NO-GO
After V4 risk-first synthetic Steps 01–12 and source-level release audit, added a **new production-oriented, local-only Site Capture facade** under `apps/site-capture/src/main/java/`: only app-private mixed-media journal inspection and recovery, immutable sync/connect/GET hard-deny; no exposed Source, device transport, test grant or automatic retry. Host Java 17 [38284068924](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38284068924) PASS 16 assertions; later same-source host [38285067620](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38285067620) SUCCESS.

Added `apps/site-capture/android-preview/` with a minimal **read-only** local status Activity, no BLE/P2P/HTTP commands or permissions. Final hardened preview source commit `6d15afbfa2b251f32fa02e0dc26609367a4b9ecc`. API36 Android **compile-only** [38285067603](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38285067603) SUCCESS: no APK/AAB created, zero permissions, **no SharedTransferCoordinator/SharedObservationController/SharedTransferContracts class compiled** into this isolated preview. API36 **one-emulator UI** [38285067667](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38285067667) SUCCESS: temporary zero-permission virtual-device APK displayed `Local Archive`, `Read-only development preview`, `Media transfer disabled` on screen. No physical phone/glasses and no published APK. Final repository hygiene [38285067652](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38285067652) SUCCESS. A previous AGP sourceSet `.exclude` attempt broke Gradle Kotlin DSL (runs 38284899717, 38284932339); corrected using `JavaCompile.exclude` before final passes. Full report `docs/testing/results/2026-10-12_V4_SITE_CAPTURE_PRODUCTION_OFFLINE_UI_SCAFFOLD.md`.

Static manual recheck: public AIMB-G1 user manual says CyanGlasses `Import` saves media to phone but does not promise originals remain on glasses. No vendor-documented preserving GET proven. Exact required primary-source answers listed in `docs/research/G6A_ORIGINAL_PRESERVATION_VENDOR_EVIDENCE_REQUEST_2026-10-12.md` (no external outreach performed). Historical real v0.6.7 GET+EXIT still associated with exact JPG removal from remote catalog; current P2P identity still unknown. **This is a genuine read-only preview, NOT the final syncing application. C1 HOLD; C2 BLOCKED; C3 NO-GO; C4 BLOCKED; G6A OPEN; G6B BLOCKED. No real GET and no physical-test APK authorized.**


## LATEST — 2026-10-13 SITE CAPTURE POPULATED LOCAL ARCHIVE UI + API35 SUPPORT VERIFIED
Following V4 steps and the zero-permission read-only preview, **actual app-private synthetic mixed-media receipts now drive the exact preview Android Activity UI**. An AndroidTest-only fixture (not in preview main classes) prepared a decodable local synthetic JPG plus shape-only MP4/OPUS; an actual Android UI Automator on-screen hierarchy check confirmed `Locally verified files: 3`, clean status, then 3 again after force-stop/relaunch with no re-seeding, then an intentionally `FETCH_MAY_HAVE_STARTED` uncertain OPUS showing `LOCAL_REVIEW_NO_REMOTE_RETRY`, then a corrupted JPG journal showing `LOCAL_LEDGER_UNVERIFIED` and no fabricated verified media count. **4/4 PASS** on API36 [38340827600](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38340827600), matching read-only compile [38340827614](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38340827614), offline host [38340827615](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38340827615), hygiene [38340827599](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38340827599), all SUCCESS at source SHA `76c80dcdcb5a6abc6b6558df75406a396309b095`. Evidence: `docs/testing/results/2026-10-13_V4_SITE_CAPTURE_POPULATED_ARCHIVE_ANDROID_RUNTIME.md`.

Preview `minSdk` reduced from API36 to **API35 (Android 15)** without permissions or real transport. Same read-only preview and all four cases independently **PASS on Android15/API35** [38341334991](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38341334991) source/workflow SHA `1ff058a8e365841333d14a5f230d19395b705f70`, and **PASS on Android16/API36** [38341308157](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38341308157) same Android source. MinSdk35 compile-only [38341320200](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38341320200) SUCCESS, hygiene [38341334986](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38341334986) SUCCESS. A transient static CI guard expected minSdk36 (run 38341291330 FAILED); fixed before accepted result. Report: `docs/testing/results/2026-10-13_V4_SITE_CAPTURE_API35_API36_COMPATIBILITY.md`.

**Crucial scope:** a local-only read-only preview with synthetic media and no Bluetooth/Wi-Fi/network/GET capability. It is not a production signed syncing APK and has not run on the physical phone or AIMB-G1. No media moved from glasses. The exact physically tested JPG is still observed missing after completed GET+EXIT; source retention unproven. Real P2P identity unknown; `G6A OPEN, G6B BLOCKED; C1 shipping HOLD; C2 BLOCKED; C3 NO-GO; C4 BLOCKED`. Do not issue real media GET or install a physical-test APK without distinct scope and authorization.


## LATEST 2026-10-13 — V5.1 CYAN ORIGINAL-DEX MEDIA GET/EXIT FORENSICS COMPLETED (STATIC, NO PHYSICAL TEST)
Owner's original Cyan Glasses `base.apk` (SHA-256 `1e700628d76fa4fa84047632e2ccce3673f1a01966fbf8c583985568f3aaaf64`) was recovered privately and verified against its historical exported manifest; all SIX previously extracted classes*.dex files match the embedded APK entries **byte-for-byte**. A newly executed targeted original-DEX method/call graph audit **PASS 27/27 assertions**, using decoded DEX method IDs and code-unit offsets. Full research `docs/research/V5_1_CYAN_MEDIA_DOWNLOAD_EXIT_FORENSICS_2026-10-13.md`, independent 17-point red-team `docs/research/V5_1_CYAN_MEDIA_EXIT_RED_TEAM_2026-10-13.md`, private-original static proof register `docs/testing/results/2026-10-13_V5_1_CYAN_MEDIA_EXIT_STATIC_AUDIT.md`. **The original proprietary APK/DEX bytes were NOT committed to public GitHub.**

**Exact source result:** `AlbumDepository.downloadGlassFile` calls `AndroidNetworking.download(url, phone-local directory, phone-local filename)` at PC `0x00d8` and `ANRequest.startDownload` at PC `0x00fd`. The normal onDownloadComplete callback advances PHONE `BlockingDeque.removeFirst`, schedules local `processDownloadedFile` on executor, and reschedules next queue request. When the phone queue becomes empty `WifiFilesDownloadListener.fileDownloadComplete` runs; `PictureFragment.fileDownloadComplete` transmits BLE `02 01 09` via `glassesControl` and schedules P2P/AP cleanup. The specific EXIT response callback is EMPTY; no ack-before-cleanup ordering guarantee from that callback. The named `GFileUtilKt.deleteFile(String)` in local postprocessing was verified to invoke **phone `java.io.File.delete()`**, NOT remote deletion. The Cyan onError callback can RETRY and, when retry count is exhausted, clears PHONE queue; retry/cleanup concurrency is unsafe to assume harmless. No preserving transfer switch was recovered in these audited methods, but full firmware HTTP implementation was not inspected.

**Safety decision unchanged:** the earlier v0.6.5 physical no-GET control retained new JPG across EXIT; the v0.6.7 single JPG GET+EXIT caused exact downloaded JPG identity to disappear after fresh remote catalog reconnect. This comparison DOES NOT isolate GET versus EXIT versus firmware asynchronous commit, and absence from catalog is not flash-level erasure proof. **Original-preservation alternative NOT PROVEN; G6A OPEN, G6B BLOCKED; C1 HOLD, C2 BLOCKED, C3 NO-GO, C4 BLOCKED. NO physical APK installed or built, NO BLE command, NO HTTP GET executed for V5.1.** Next V5.2: offline causal evidence matrix/source enquiry; do NOT test HEAD, Range, AP workaround, truncated GET or skipped EXIT on actual glasses without trustworthy preserving evidence and separately scoped authorization.

## LATEST OVERRIDE — 2026-10-31 V5.2 RESEARCH SLICE COMPLETE (NO PHYSICAL TEST)
V5.2 completed as offline research on current `main`; V5.1 original APK/DEX extraction was not repeated. New reports:
- `docs/research/V5_2_GET_EXIT_CAUSAL_EVIDENCE_MATRIX_2026-10-31.md` — ranks the existing evidence and competing causal models. Strong evidence supports only that the completed GET→EXIT/teardown lifecycle was associated with the exact JPG becoming absent from a fresh remote catalog. The no-media-GET control retained its JPG across EXIT. Neither observation isolates GET versus EXIT/timing, and catalog absence is not proof of flash erasure.
- `docs/research/V5_2_MANUFACTURER_TRANSFER_PRESERVATION_EVIDENCE_2026-10-31.md` — reviews device-specific manual and vendor/OEM listings. Public material documents Cyan/CyanGlasses app import over Wi-Fi, but does not promise source retention or document a preserving mode/alternate transfer. Exact hardware/firmware authority remains to be confirmed. Contains a manufacturer/support evidence request that was prepared but NOT sent.
- `docs/research/V5_2_RED_TEAM_AND_DECISION_2026-10-31.md` — red-team challenges speculative HTTP/EXIT/AP/interruption/retry proposals and records stop decisions.

No device, media GET, BLE command, experimental APK, or outside party was contacted. No physical gates advanced. Original-preserving transfer remains unproven: **C3 NO-GO; G6A OPEN; G6B BLOCKED; C1 HOLD; C2 BLOCKED; C4 BLOCKED.** Keep remote media GET disabled. Next safe evidence is exact model/firmware-scoped authoritative manufacturer/authorized-support documentation or answer; it must distinguish source-byte retention from catalog visibility and state interrupted-transfer behavior. Any future physical validation remains separately scoped and requires explicit approval.

## 2026-10-31 — V5.4 PUBLIC PROTOCOL IMPLEMENTATION AUDIT
Independent public projects confirm the same BLE + Wi-Fi Direct + `/files/media.config` + `/files/<filename>` architecture and describe local gallery saving, timeout handling and transfer-mode exit. No reviewed public implementation documents a read-only GET, keep-original flag, alternate preserving endpoint, or a supported way to establish that a catalog-hidden item remains in storage. Community implementation evidence strengthens the architecture description only; it does not prove original retention. Report: `docs/research/V5_4_PUBLIC_PROTOCOL_IMPLEMENTATION_AUDIT_2026-10-31.md`. **C3 NO-GO; G6A OPEN; G6B BLOCKED.**
