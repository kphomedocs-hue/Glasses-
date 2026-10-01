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

## Current physical candidate

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

## Next engineering question

The unresolved boundary is now **single-capture behavior**, not idle behavior.

Next diagnostic should start from the current stable catalog, take exactly one photo, and determine—without downloading any media file—whether:
1. BLE inventory changes by exactly +1;
2. the refreshed catalog gains exactly one JPG;
3. no baseline JPG disappears;
4. the one-new-JPG membership persists through a verified exit and fresh reconnect.

This isolates capture/catalog-retention semantics from media-download effects.

Recommended next version: **v0.6.5 — single-capture catalog retention, zero media GETs**.

Hard boundary should remain:
- baseline inventory + catalog;
- verified exit/group absence before capture;
- exactly one intended physical capture;
- bounded visibility confirmation;
- one post-capture inventory confirmation;
- one post-capture catalog GET;
- verified exit/group absence;
- fresh reconnect;
- one retention inventory + catalog GET;
- zero media-file GETs throughout;
- no glasses mutation/deletion;
- exact immutable build provenance.

If the capture produces +2 again, stop and classify capture-side ambiguity.  
If it produces +1 but the new JPG disappears after reconnect, classify remote retention instability without download.  
If it produces +1 and persists, the earlier disappearance/reversion becomes more likely tied to the previous transfer/download/restart path rather than idle catalog behavior.

**G6A remains open. G6B remains blocked.**
