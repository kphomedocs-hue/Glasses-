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


## NEXT CHAT HANDOVER POINTER

Authoritative resume file:
`00_NEXT_CHAT_HANDOVER.md`

For a new chat, read that file first, then verify latest GitHub `main`. It freezes the current v0.6.8 two-phase physical procedure, exact build provenance, proven consumptive-GET semantics, known nonblocking defects, and G6A/G6B status.


## CURRENT OVERRIDE — 2026-10-02 v0.6.8 credential-handshake blocker / v0.6.8.1 diagnostic

This section supersedes older "current candidate" wording above.

### v0.6.8 physical Phase 1 blocker — reproduced three times

Exact v0.6.8 build:
- run `36903742122`;
- attempt `1`;
- build commit `cf92a44563352ed339d8dd74a825483c7dba39e4`;
- APK SHA-256 `3f94a240030c10162c0025853846f6ea29860de2ed36874742cddbb2a61d4b27`.

Three supplied physical attempts at 09:53:08, 09:55:25, and 10:00:07 +0530 reproduced the same baseline failure:
- Cyan GATT/notify subscription: PASS;
- media-count query: PASS;
- baseline inventory: 11 images / 0 videos / 1 recording;
- P2P-enter write start: PASS;
- P2P-enter write callback: PASS;
- accepted transfer-credential handshake within the existing 10-second window: NOT OBSERVED;
- zero catalog GET;
- zero media GET;
- zero HTTP;
- no persistent import;
- no durable receipt;
- no capture-watch event;
- abort exit write callback: PASS;
- no matching post-exit `0x73/0x01`;
- local Android P2P group absent after cleanup.

Full reproduced evidence:
`releases/v0.6.8/builds/run-36903742122-attempt-1/reports/2026-10-02_G6A_V0_6_8_P2P_CREDENTIAL_HANDSHAKE_FAILURE_REPRODUCED_3X.md`

Therefore v0.6.8 Phase 1 is currently blocked before capture/catalog/import. Do not keep retrying it unchanged.

### Current authorized diagnostic candidate — v0.6.8.1

Purpose: distinguish whether the v0.6.8 blocker is:
1. no valid `0x41` credential candidate;
2. valid `0x41` activity rejected by the unchanged credential structure;
3. a valid credential inside the original 10-second window;
4. a valid credential arriving only during an additional bounded passive 10-second window.

Only authorized diagnostic build:
- run `36965814011`;
- attempt `1`;
- build commit `beceb27754f491fcae6a53433ad4dd4fcfadc22b`;
- package `com.parkarsite.g6acreddiag681`;
- APK SHA-256 `01ba35d297c6d48dce31242185032d8d37ced39856419e827cc9c9013e4246dd`;
- exact source ZIP SHA-256 `f3d65c6ca0bf2b8d815f5e98e62a8f8420730b350a41b6505e2072537cedd8c2`.

Pre-physical status:
- safety audit PASS;
- red-team static audit PASS;
- compile/lint PASS;
- signature PASS;
- immutable archive PASS;
- manual pre-promotion red-team PASS.

Manual review:
`releases/v0.6.8.1/builds/run-36965814011-attempt-1/reviews/MANUAL_RED_TEAM_PREPROMOTION.md`

Physical scope:
- DO NOT TAKE A PHOTO;
- exactly one media-count write;
- exactly one P2P-enter write;
- original 10-second credential window;
- one additional passive 10-second late-observation window only if needed;
- exactly one allow-listed transfer-exit write;
- zero Android Wi-Fi Direct API operations;
- zero catalog/media HTTP;
- zero persistent import/receipt.

**G6A: OPEN — blocked pending v0.6.8.1 diagnostic classification.**
**G6B: BLOCKED.**


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


## 2026-10-07 — v0.6.8.4 recovery verification

Exact build: run `36973886568`, attempt 1, commit `506b3f311be7e2dd8a6cd5ce8f7bf36615f95e25`.

Result:
- GATT transport recovered and connected normally;
- service discovery and notifications passed;
- one 0x40 response was observed;
- media-count handshake passed;
- current inventory was 0 / 0 / 0;
- P2P-enter write and callback passed;
- the same previously observed short non-credential 0x41 response returned;
- no credential-bearing frame appeared within the bounded 20-second observation;
- exit write callback passed;
- no matching post-exit inventory event was observed;
- zero Wi-Fi Direct / HTTP / catalog / media / capture activity.

Evidence:
`releases/v0.6.8.4/builds/run-36973886568-attempt-1/reports/2026-10-07_G6A_V0_6_8_4_TRANSPORT_RECOVERED_CREDENTIAL_BLOCKER_PERSISTS.md`

Conclusion:
the temporary GATT issue is resolved, but the P2P credential blocker persists after cold recovery. Do not repeat v0.6.8.4 and do not add new proprietary initialization commands.

Next root-cause test should reproduce the historically successful G3B direct-entry sequence: notification subscribe, one direct P2P ENTER, bounded observation, one EXIT, with no time-sync, media-count, Wi-Fi Direct, HTTP, capture, or credential-value logging.

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



## v0.6.8.6 physical result — 2026-10-11
One supervised run at 12:04:39 IST completed. Result INCONCLUSIVE, cleanup verified. One peer observed with nonempty name classified OTHER (no AIMB-G1 family prefix, no full paired BLE suffix); peer identity unknown. No 0x73 event and no Wi-Fi Direct connection. ENTER and EXIT succeeded. Exact report: releases/v0.6.8.6/builds/run-38118060511-attempt-1/reports/2026-10-11_G6A_V0_6_8_6_PHYSICAL_INCONCLUSIVE.md. Research: docs/research/G6A_V0_6_8_6_PHYSICAL_RECHECK_2026-10-11.md. One-run authorization consumed. No unchanged rerun. G6A OPEN; G6B BLOCKED.


## 2026-10-11 Exact Cyan APK provenance blocker
Focused static research is preserved in docs/research/G6A_CYAN_WIFI_NAME_PROVENANCE_BLOCKER_2026-10-11.md. Original Cyan split/base APK not accessible in current working files; repository code search did not recover precise stored-Wi-Fi-name setter/getter implementation. Historical BLE-credential SSID identity proven by G4A, but independent current name remains unknown. Need exact owner-exported Cyan APK matching documented SHA-256 to continue bytecode provenance. No further physical test authorized. G6A OPEN; G6B BLOCKED.


## 2026-10-11 exact Cyan DEX setter/getter provenance resolved
The user supplied six original Cyan DEX files. Each byte-for-byte matches classes.dex through classes6.dex embedded in the verified original base.apk. DEX call-site analysis (not symbol-only) recovered MyBluetoothReceiver.connectStatue's **two direct calls** to UserConfig.setGlassDeviceWifiName, constructing a candidate Wi-Fi name from a selected Bluetooth-name segment plus underscore plus colon-stripped Bluetooth device address; a predefined nine-character password is also set on this BLE-connected path. UserConfig's Wi-Fi name is backed by an MMKV delegate (getString/putString via decodeString/encode, app_config storage). PictureFragment.onPeersChanged compares peer names case-insensitively to the stored name, then checks an additional endsWith suffix based on getDeviceAddressNoClear (whose setter is assigned raw BluetoothDevice.getAddress; no colon stripping at the peer comparison). This corrects the earlier assumption that a current credential-bearing 0x41 is always required to know a trusted candidate name.
Full source/PC evidence: docs/research/G6A_CYAN_EXACT_DEX_WIFI_IDENTITY_CALLSITE_TRACE_2026-10-11.md. The current peer identity remains UNVERIFIED; v0.6.8.5/v0.6.8.6 are INCONCLUSIVE. No arbitrary-peer connect, new APK or physical run authorized. G6A OPEN, G6B BLOCKED.


## 2026-10-11: 90-assumption historical evidence audit
Completed an evidence-graded audit of 90 materially documented protocol, identity, catalog, capture, remote-transfer, local-ledger, red-team and CI assumptions. Main index: docs/research/ASSUMPTION_AUDIT_2026-10-11_INDEX.md and PART1_PROTOCOL_IDENTITY.md, PART2_MEDIA_STORAGE.md, PART3_TEST_SAFETY.md. Most consequential issues: exact Cyan on-BLE-connect MMKV Wi-Fi-name derivation is now source-verified but has not identified the anonymous v0.6.8.6 OTHER peer; the Cyan-derived name would still end in the normalized BLE address, so absence of a suffix match cannot be explained by lost AIMB prefix alone; successful media JPG GET is remote-consumptive despite no explicit delete; ledger persistence is not universal restart-safe dedup. No new APK or physical run authorized. G6A OPEN; G6B BLOCKED.


## 2026-10-11 assumption-first static closure: completed
Evidence docs: docs/research/G6A_ASSUMPTION_FIRST_STATIC_CLOSURE_2026-10-11.md; docs/research/G6A_ASSUMPTION_FIRST_RESOLUTION_REGISTER_2026-10-11.md. Exact-source comparison established K v0.6.7 credential gate is stricter than necessary for Wi-Fi Direct WPS-PBC peer association setup: historical K consumed SSID and password, but Android WifiP2pConfig passes selected peer address plus WPS PBC, not that password. Cyan constructs an MMKV-backed candidate name from Bluetooth name/address. Yet K .5/.6 normalized full-suffix mismatch means v0.6.8.6 OTHER peer is NOT explained merely by dropping model prefix (given same address input). Actual peer identity unproven. Remote GET consumption verified. 0x41 short response meaning unknown; production exactly-once unresolved. Red-team rejects sole unknown peer connection, reset conjecture and arbitrary media retest. Static-only closed; no APK or physical trial authorized. G6A OPEN, G6B BLOCKED.


## CURRENT EVIDENCE — 2026-10-11 G6A Step 1 & 2 source-only investigations COMPLETED
Completed and archived: docs/research/G6A_STEP1_EXACT_WIFI_IDENTITY_STATIC_COMPARISON_2026-10-11.md; docs/research/G6A_STEP2_NONCONSUMPTIVE_MEDIA_PATH_AUDIT_2026-10-11.md; docs/research/G6A_STEP1_STEP2_INDEPENDENT_RED_TEAM_2026-10-11.md. Exact K source vs prior original Cyan DEX call-site evidence confirms K's v0.6.7 parsed password is an app-level gate while historical WifiP2pConfig/WPS-PBC does not receive the password. Cyan can build persisted candidate Wi-Fi name from BLE name/address. Conditional synthetic comparison confirms a Cyan-derived name from SAME Bluetooth address must match K's full 12-hex suffix, so v0.6.8.6 OTHER still unidentified. No Android peer association attempted. Source review found NO VERIFIED non-consumptive JPG path: P2P config=1 GET associated with removal after EXIT/reconnect; AP/configFileType=2 are different modes, not demonstrated safe alternates. Precise device-side removal moment (HTTP GET versus transfer EXIT) remains unisolated. Independent red team PASS documentation / FAIL production claims. No build or physical test authorized. G6A OPEN; G6B BLOCKED.


## CURRENT MILESTONE — 2026-10-11 G6A Step 3 OFFLINE crash-safety lab verified
An isolated transport-free Python reference model has been added at tools/g6a_consumptive_recovery_lab.py with deterministic subprocess termination tests tools/test_g6a_consumptive_recovery_lab.py and CI workflow .github/workflows/g6a-consumptive-recovery-lab.yml. Exact CI run 38125598949 SUCCESS: 17/17 tests passed, including real process exits at six crash boundaries, byte-count and explicit transfer-completion gating, checksum/marker validation, nonreuse of numbering, and *no automatic retry* when remote media may have been consumed. Repository Hygiene run 38125598946 SUCCESS. Full report docs/research/G6A_STEP3_CRASH_SAFE_OFFLINE_LAB_RESULT_2026-10-11.md; adversarial limitations docs/research/G6A_STEP3_OFFLINE_RECOVERY_RED_TEAM_2026-10-11.md; pending peer identity evidence contract docs/research/G6A_PEER_IDENTITY_NEXT_EVIDENCE_CONTRACT_2026-10-11.md. Frozen physical v0.6.8 code still has removeStaleParts at startup, sidecar-only integrity evidence and append-only TSV; these were NOT modified. This new Python reference is NOT an Android APK, NOT a verified transport adapter, and does NOT guarantee recovery of a remotely consumed JPG if no complete local bytes survive. Hardware original retention remains unproven. No physical run authorized. G6A OPEN / G6B BLOCKED.


## 2026-10-11 G6A STEP 3 — OFFLINE CONSUMPTIVE RECOVERY PROTOTYPE PASS (RESEARCH ONLY)
Created separate `source/g6a-recovery-core/` Java 17 reference model with write-ahead FETCH_MAY_HAVE_STARTED marker, no-retry/remote-uncertain states, full-length/JPEG-shape/sha local validation, synced verified journal, final move, receipt, local-only restart recovery and stable daily JPG naming. CI `.github/workflows/g6a-recovery-fault-tests.yml` PASS: run 38125976170, 85 assertions, six injected failure points plus six separate abrupt JVM halt/restart tests; repository hygiene run 38125976168 PASS. See `docs/research/G6A_STEP3_OFFLINE_CONSUMPTIVE_RECOVERY_RED_TEAM_2026-10-11.md`. Exact Java blob SHA: store 345953c518812157e2fe8a1cae918cf7b2573fb6, test 66f5ecdbc5894e50aa86f154139c01134624ff44. Does NOT prove power-loss durability, preserve originals on glasses, or end-to-end exactly-once; the remote state after any partial GET is uncertain and cannot be retried automatically. Existing v0.6.8/release artifacts untouched. No APK or physical test authorized. G6A OPEN, G6B BLOCKED.


## 2026-10-11 G6A STEP 4 — SOURCE-ONLY PEER IDENTITY RED TEAM COMPLETE
Research report: docs/research/G6A_STEP4_PEER_IDENTITY_RED_TEAM_AND_TEST_SPEC_2026-10-11.md. New isolated source-only pure-Java library: source/g6a-identity-core/ (no Android/BLE/P2P/HTTP). It reconstructs prior exact-Cyan-derived candidate name from bonded BLE name/address, compares against hypothetical peer names, distinguishes exact derived name vs normalized suffix vs raw-address research fallback vs generic family vs OTHER, and reports only aggregate counts. Even one exact name is explicitly SINGLE_UNVERIFIED_CANDIDATE and never AUTHENTICATED/CONNECT_AUTHORIZED. Java 17 CI run 38126649073 PASS with 54 synthetic assertions after public-API privacy hardening; hygiene run 38126649087 PASS. Prior v0.6.8.6 OTHER peer remains unidentified: potential unrelated/cached peer, differing address/name, or P2P nonadvertisement; no same-address prefix-only fix. Future test must distinguish a new hypothesis instead of repeating identical 16-second scan. Original-on-glasses preservation still unproven for GET-associated remote consumption. No new Android APK, no media GET, no physical run. G6A OPEN; G6B BLOCKED.


## 2026-10-11 G6A STEP 4 RETRY — ORIGINAL CYAN DEX RECHECK + EXACT LEGACY MATCHER BLIND SPOT
Re-read original owner-supplied Cyan 1.0.2.18 DEX bytecode locally (base.apk SHA-256 1e700628d76fa4fa84047632e2ccce3673f1a01966fbf8c583985568f3aaaf64); exact invocation offsets reaffirm MyBluetoothReceiver.connectStatue candidate setter (416, 487), raw Bluetooth address setter (269), and PictureFragment.onPeersChanged exact-name equality (59–64) plus raw-address endsWith fallback (83–90). Discovered significant previously untested v0.6.8.6 diagnostic blind spot: a hypothetical advertised peer name ending in colon-formatted BLE address can be categorized OTHER by K's 12-hex-only suffix matcher while satisfying Cyan's raw-address fallback. Actual v0.6.8.6 OTHER peer name was never retained; cannot prove this is the true peer. New cross-version Java tests compile EXACT frozen v0.6.8.5/v0.6.8.6 DiagnosticSafetyRules.java, 54+8+15=77 assertions PASS, CI run 38127615703; hygiene run 38127615701 PASS. See docs/research/G6A_STEP4_RETRY_ORIGINAL_DEX_RAW_ADDRESS_BLIND_SPOT_2026-10-11.md and source/g6a-identity-core/. No connection authorized from a name match, no new APK/physical test. Critical current peer identity remains UNKNOWN; remote JPG preservation still unproven. G6A OPEN, G6B BLOCKED.


## 2026-10-11 — PROPOSED VIRTUAL AIMB-G1 LAB TEST STRATEGY (RESEARCH ONLY)
New strategy: `docs/testing/G6A_VIRTUAL_GLASSES_LAB_STRATEGY_2026-10-11.md`; 96 **PLANNED / NOT RUN** scenario cases: `docs/testing/G6A_VIRTUAL_GLASSES_SCENARIO_MATRIX_2026-10-11.md`. Current 2026 Android Emulator 36.5+ supports simulated multi-AVD Wi-Fi Direct; proposal combines (A) shared state-machine protocol replay, (B) one-AVD MockWebServer then feasibility-gated two-AVD virtual P2P peer, (C) only a separately approved minimal real-glasses identity/readiness check. New Cyan GATT radio emulation is NOT confirmed; use explicit injected BLE-event adapter until proven. GitHub public CI first; no original Cyan binaries/device identifiers/client media in fixtures. 96 scenarios are NOT TESTED and no physical gate is promoted. Known JPG GET+EXIT remote consumption, current OTHER peer and short 0x41 meaning remain unresolved; preserve original-on-glasses requirement. No new APK, glasses commands or media GET authorized. G6A OPEN, G6B BLOCKED.


## 2026-10-11 — VIRTUAL AIMB-G1 LAB PHASE 1: 24/24 OFFLINE SUBSETS PASS; NO PHYSICAL PROMOTION
Read lab/virtual-aimb-g1/README.md, docs/testing/results/2026-10-11_VGL_PHASE1_24_OFFLINE_TEST_RESULT.md, and docs/testing/VGL_PHASE1_OFFLINE_IMPLEMENTATION_REVIEW_2026-10-11.md. Source commit 1c195bd6fb6254365994520cabc48a493e6ed95c. GitHub Actions Java 17 synthetic scenarios 01–03 (24 IDs) run 38130089425 SUCCESS; separate red-team tests PASS; public repository hygiene run 38130089461 SUCCESS. Previous hygiene failure from literal fabricated BLE identifiers corrected with no real owner data uploaded. New core reuses frozen protocol CRC and G6A identity modules by compilation; frame decoding keeps short 0x41 UNKNOWN, exact/suffix names untrusted, typed group/IP provenance fail-closed, independent timers, bounded events and terminal callback isolation. No media GET, no APK, no emulator/physical run and no frozen-release edits. 24 offline safety assertion sets executed; full production integration unverified, remaining 72 planned. Preserve source originals on glasses requirement despite historical GET-related consumption. **G6A OPEN; G6B BLOCKED.**


## LATEST OVERRIDE — 2026-10-11: ALL 96 VGL LANE A OFFLINE SUBSETS VERIFIED
Latest code verified at source commit 34a218ccec0116909cc07fbc75c2a729b63a791a. GitHub offline workflow 38131350988 SUCCESS, hygiene 38131350927 SUCCESS. All 96 distinct planned scenario IDs (12 packs x 8) executed synthetic safety-subset assertions; two red-team suites and an exact-ID manifest guard PASS. Pack 09 executes ACTUAL offline RecoveryImportStore filesystem/crash-barrier paths; the other new policy models are candidate-only and NOT wired to the shipping Android app. New audit fixes reject wrong-shaped HTTP bodies, incomplete reserved receipts, missing baseline photos, unsafe preflight cleanup and tested raw identifier/secret leaks. Initial runner-shell quotation failure in 38131306328 was fixed before final green CI. Detailed result: docs/testing/results/2026-10-11_VGL_LANE_A_96_OFFLINE_TEST_RESULT.md. Review: docs/testing/VGL_LANE_A_96_RED_TEAM_REVIEW_2026-10-11.md. This supersedes earlier statements that 72 are unimplemented, but DOES NOT constitute 96 full production-integrated PASS or any Android/emulator, peer/firmware, originals-retention or physical PASS. No APK, glasses commands or media GET authorized. Existing releases unchanged. Next priorities: single adopted core integrated into Android, safe one-emulator fake-local-server test, perhaps feasibility-gated dual AVD, and separate real identity + originals-preservation research. **G6A OPEN; G6B BLOCKED.**


## 2026-10-11 V2 VERIFIED OVERRIDE — ANDROID SOURCE COMPILES, HOST-LOCAL HTTP 7/7 PASS
Latest V2 verified source: c16b5898fd3d7caf8b2261ddec2c7ed80f5a2c91. Read docs/testing/results/2026-10-11_VGL_V2_ANDROID_COMPILE_HOST_HTTP_RESULT.md and docs/testing/VGL_V2_ANDROID_SOURCE_RED_TEAM_2026-10-11.md. Additive Android simulator-only Gradle project lab/virtual-aimb-g1/android-simulator/ compiles the ACTUAL shared VirtualGlassesCore, policies, protocol, identity and RecoveryImportStore into a no-permissions, source-only Android Activity plus compiled but UNEXECUTED instrumentation hook. API 36, JDK17, Gradle 9.6. Android CI 38132519731 SUCCESS: compile-only; verified merged manifest and NO APK built or installed, NO emulator. Recovery core Files.readString ported to strict 4 KiB bounded UTF-8 read to permit Android Java compilation; previous recovery CI 38132290876 PASS. A separate HOST JVM loopback HTTP simulator in lab/virtual-aimb-g1/host-http/ runs 7/7 synthetic fault cases (GET200, 500, 302, truncation, corrupt JPEG, consumptive-on-GET, forbidden other host) against ACTUAL RecoveryImportStore; together with 96/96 offline synthetic IDs and both red teams CI 38132519735 PASS, hygiene 38132519714 PASS. These are NOT Android emulator runtime tests, real phone tests, production adapter integration, physical identity proof or original-preserving sync. Historical remote JPG GET consumes original; OWNER REQUIREMENT remains unsatisfied. DO NOT BUILD/INSTALL EXPERIMENTAL APK OR RUN PHYSICAL MEDIA GET without separately scoped authorization. Next: one-AVD emulator-only instrumentation after specific approval, full shipping-path adapter integration, then independently verify actual peer and originals-preserving method. G6A OPEN; G6B BLOCKED.


## 2026-10-11 V2B COMPLETED — VIRTUAL CORE + HOST-LOCAL HTTP + DURABLE STORAGE INTEGRATION
Source milestone commit `54b3540c437c39fd81e1d38132462d26de7078b1`. New host-JVM `HostCoreIntegrationSelfTest` drives real `VirtualGlassesCore`, identity/classification, Catalog/Capture, 127.0.0.1 HttpServer and actual file-backed `RecoveryImportStore` in one flow. CI [38137429577] PASS: previously 96/96 source-only scenario assertion IDs and 7/7 host HTTP tests PLUS 10/10 new integrated `CORE-HTTP` tests; hygiene [38137429591] PASS. Read `docs/testing/results/2026-10-11_VGL_V2B_INTEGRATED_HOST_CORE_HTTP_RESULT.md`. Tests deny HTTP for unrelated, generic, untrusted, no-IP and ambiguous peer states; fail closed on missing baseline and +2 new media, no duplicate/HTTP500 retry. Core's real media and connection authorization are still hard-false; only a separate localhost host-test fixture has synthetic-only explicit download permission. NOT Android emulator, final app, real BLE/Wi-Fi Direct, physical G6A, or originals-retention proof. No APK build/install or glasses interaction. G6A OPEN, G6B BLOCKED.
