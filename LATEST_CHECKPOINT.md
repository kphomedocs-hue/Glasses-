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

## Current physical candidate

**NONE.**

Do not install v0.6.4 or v0.6.4.1 for the next physical test.

## Next engineering action

Create **v0.6.4.2** with:
- exit-specific confirmation or a stronger evidence-backed post-exit condition;
- centralized fail-safe transfer/P2P cleanup;
- case-sensitive exact peer matching;
- no per-file identity tokens in report;
- rejection of leading/trailing-whitespace catalog lines;
- immutable exact-build archive path;
- source ZIP created from exact `GITHUB_SHA`;
- build provenance that cannot be overwritten by same-version rebuilds.

Before physical use:
1. static safety audit;
2. compile/lint;
3. APK signature verification;
4. red-team state-machine recheck;
5. immutable exact-build bundle verification;
6. concise checkpoint update.

**G6A remains open. G6B remains blocked.**
