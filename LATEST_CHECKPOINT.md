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

Known v0.6.7 reporting defect:
- its runtime report printed `App version: 0.6.6`;
- exact build commit/run/attempt and package provenance still uniquely identify the tested v0.6.7 APK;
- do not rerun or rebuild v0.6.7 merely to correct this historical header.

### Current and only authorized physical candidate

**v0.6.8 — persistent local import + durable receipt + real restart/no-duplicate proof**

- run: `36903742122`
- attempt: `1`
- build commit: `cf92a44563352ed339d8dd74a825483c7dba39e4`
- APK SHA-256: `3f94a240030c10162c0025853846f6ea29860de2ed36874742cddbb2a61d4b27`
- exact source ZIP SHA-256: `0dd1535b280d383e102a338eb2a9fc5b7106438bea3eb106e56808b9b6e12d54`
- package: `com.parkarsite.g6apersist68`
- exact bundle: `releases/v0.6.8/builds/run-36903742122-attempt-1/`

Pre-physical gates:
- safety: PASS;
- red-team static: PASS;
- compile/lint: PASS;
- signature: PASS;
- immutable archive: PASS;
- manual storage/state-machine review: PASS.

### Remaining G6A physical work

**Phase 1 — persistent import under consumptive GET**
- one physical photo only after ARMED;
- exact +1 capture and pre-transfer retention;
- one JPG GET/import only;
- app-private `.part` + fsync;
- JPEG validation;
- final numbered local JPG commit;
- durable opaque receipt committed only after local file durability;
- remote catalog must return exactly to the original baseline after the transferred JPG is consumed;
- exact network totals: 4 inventory / 4 enter / 4 exit / 4 catalog GET / 1 media GET / 5 HTTP GET.

**Phase 2 — real restart verification**
- do not uninstall or clear app data;
- force-stop/reopen same v0.6.8 app;
- run local-only restart verification;
- exactly one durable receipt loads;
- exactly one numbered local JPG exists;
- bytes/JPEG revalidate;
- zero HTTP, zero media GET, zero BLE/P2P operations;
- zero duplicate local file.

If both reports pass under the exact v0.6.8 build, proceed to G6A closure review. Do not mark G6A PASS before those reports are reviewed.

**G6A: OPEN — current work is v0.6.8 two-phase physical proof.**
**G6B: BLOCKED until G6A closure.**
