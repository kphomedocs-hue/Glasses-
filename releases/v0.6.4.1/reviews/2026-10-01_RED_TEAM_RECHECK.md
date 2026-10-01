# Red-Team Recheck — v0.6.4.1 + Repository Evidence Chain

Date: 2026-10-01
Scope: runtime correctness, BLE/P2P sequencing, PASS criteria, privacy, release provenance, version-local evidence structure, and current checkpoint usability.

## Decision

**v0.6.4.1 is BLOCKED from physical testing.**

The build compiles and passes its static audit, but red-team review found runtime-validation gaps and release-provenance defects that are material to the purpose of this diagnostic.

G6A remains open. G6B remains blocked.

## High-severity findings

### RT-01 — Exit response is under-validated

In v0.6.4.1, any valid command-`0x41` notification received while the state is `EXIT_SENT` sets `exitResponseReceived=true`.

The response payload is not checked to prove that it corresponds to the `02 01 09` exit command.

Risk:
- a delayed or unrelated valid `0x41` response could be mistaken for exit confirmation;
- Snapshot A → quiet interval separation could be falsely proven.

Required correction:
- validate an exit-specific response shape/prefix if protocol evidence supports it;
- if exact exit response semantics cannot be proven, use a stronger bounded post-exit condition derived from physical evidence and do not label a generic `0x41` as an exit response.

### RT-02 — Failure/onStop cleanup can abandon transfer state

Several direct `fail(...)` paths bypass `failWithExit(...)`.

Most importantly, `onStop()` calls `fail(...)` directly while a run may already be in P2P transfer mode.

Risk:
- app can leave foreground after P2P entry and close local BLE/runtime state without attempting the proven `02 01 09` exit;
- a local Wi-Fi Direct group may also remain.

Required correction:
- centralize abort handling;
- if transfer entry was attempted/accepted, attempt the single allowed exit when possible;
- always attempt one bounded local P2P cleanup/readback before final failure reporting when a group may exist;
- never add retries.

### RT-03 — “Exact peer” match is case-insensitive

The Wi-Fi Direct peer matcher uses `equalsIgnoreCase(expectedP2pName)`.

The project boundary says exact BLE-reported P2P name only.

Risk:
- a peer differing only by case can satisfy a test that reports “exact” match.

Required correction:
- use exact case-sensitive string equality unless separate physical/protocol evidence explicitly proves case folding is intended.

### RT-04 — Release artifacts were overwritten by same-version rebuilds

Archive-script maintenance triggered new builds under existing version numbers and replaced top-level APK artifacts.

Confirmed example:
- v0.6.3 physical evidence belongs to original APK SHA-256
  `5585bbc9d58082ee1c178f622236c76cafb68b456bcaaf2ed7e1a3fb2fa4bef1`;
- later housekeeping rebuild changed the top-level v0.6.3 APK to a different hash.

This broke the exact build↔physical-report chain until the original build was restored into:
`releases/v0.6.3/builds/run-36585496838/`.

Required correction:
- builds must be immutable;
- never overwrite an existing build artifact under a version;
- use `releases/vX/builds/run-<run-id>/` or an equivalent immutable key;
- physical reports belong under the exact build folder.

### RT-05 — Archive source provenance can diverge from APK build provenance

The archive script performs `git pull --rebase origin main` before creating the source ZIP.

If `main` advances while CI is running:
- APK can be compiled from `GITHUB_SHA`;
- source ZIP can be created from a later `main`;
- BUILD_INFO can still claim the original `GITHUB_SHA`.

Required correction:
- create source archive from the exact `GITHUB_SHA`, not mutable working-tree HEAD;
- stage artifacts outside the repository;
- only then update from `origin/main` for the archive commit.

## Medium-severity findings

### RT-06 — Version manifests became stale

Same-version rebuilds changed APK hashes/build runs while `VERSION_MANIFEST.md` retained older values.

Confirmed for v0.6.3 and v0.6.4.1.

Required correction:
- manifest must list immutable build instances, not one mutable “current APK” identity;
- never silently replace the build referenced by a physical report.

### RT-07 — The old LATEST_CHECKPOINT.md was unsafe as an authority

The append-only document contained:
- an obsolete top-level statement that the “Current physical candidate” was v0.5.5;
- multiple historical “Next action” instructions;
- much newer v0.6.4.1 guidance only near the bottom.

Risk:
- a future resume can follow obsolete instructions from the same file called “LATEST”.

Required correction:
- keep `LATEST_CHECKPOINT.md` short and current-only;
- archive full history separately.

### RT-08 — Deterministic truncated identity tokens are unnecessary disclosure

v0.6.4.1 reports 12-hex SHA-256-derived identity tokens.

Although filenames are not logged directly, deterministic hashes of predictable remote names can be dictionary-tested.

Required correction:
- for the no-capture stability gate, keep full hashes in memory but report only counts/overlap/missing/unexpected values;
- do not emit per-file deterministic tokens unless they are essential.

### RT-09 — Parser normalization can hide identity changes

Catalog lines are `trim()`med before validation/hash storage.

A raw line changing only by leading/trailing whitespace can collapse to the same identity and be called stable.

Required correction:
- reject lines where `line.equals(line.trim())` is false;
- compare accepted exact line text, not normalized text.

### RT-10 — Update-install continuity is not proven

v0.6.4/v0.6.4.1 use debug builds with no explicit stable signing configuration.

Same package ID + higher versionCode alone does not prove Android will accept an in-place update if CI signing certificates differ.

Required correction:
- do not promise upgrade continuity unless signing certificate continuity is verified;
- for test instructions, allow a fresh install unless the exact signing lineage is proven.

## What passed red-team review

- no media-file GET code path in the no-capture diagnostic;
- no POST/PUT/PATCH/DELETE/HEAD HTTP path;
- redirects remain disabled;
- no Range/resume/custom header path;
- no AP-mode command;
- no `02 03` P2P-IP query;
- no arbitrary endpoint input;
- media-count parser requires both BLE write callback and valid `02 04` response;
- transfer credential parser validates the `02 01 04 01` prefix;
- catalog size/item/path/duplicate bounds are fail-closed;
- Snapshot A/B identity sets are held in memory;
- cross-channel BLE-image/catalog-JPG parity was added in v0.6.4.1;
- exact final operation totals are checked;
- P2P group absence is now read back after one cleanup request;
- cleanup callback is bounded;
- quiet interval uses monotonic elapsed time;
- glasses-side file mutation/deletion remains absent.

## Evidence repair completed during this recheck

Immutable build folders were created for the physically relevant builds:

- `releases/v0.6.1/builds/run-36461373394/`
- `releases/v0.6.2/builds/run-36466571422/`
- `releases/v0.6.3/builds/run-36585496838/`
- `releases/v0.6.4.1/builds/run-36808762256/`

The v0.6.3 folder restores the exact original APK/artifacts associated with its physical +2 report.

## Required next engineering candidate

Create **v0.6.4.2** only after applying RT-01 through RT-05 and the relevant medium-severity fixes.

Do not physically test v0.6.4 or v0.6.4.1.

Before v0.6.4.2 is handed to the user:
1. static safety audit;
2. compile/lint;
3. signature verification;
4. red-team state-machine review;
5. exact build provenance verification;
6. immutable build folder creation;
7. concise checkpoint update.

Only then resume the no-capture physical stability test.
