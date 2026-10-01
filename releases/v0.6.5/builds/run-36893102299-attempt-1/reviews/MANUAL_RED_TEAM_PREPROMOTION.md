# Manual Red-Team Pre-Promotion Review — v0.6.5 run 36893102299 attempt 1

Date: 2026-10-01

## Exact build identity

- Version: v0.6.5
- Build commit: `598e9aed94546bff2c9b018114932902aca67e05`
- Build run: `36893102299`
- Build attempt: `1`
- APK SHA-256: `985edbcc5a8a7b035cd50c71771c7a51b9f166c9d6a827972b2f4b13423c477f`
- Exact source ZIP SHA-256: `40af18c38038f03ab2ec7745b3dd3efbf50f72757fff41f67d1e6d751efbf1e1`
- MainActivity blob: `eedef01c529fa2b41dc580b33ba4c76b477676a3`

## CI gates

PASS:
- safety audit;
- dedicated red-team static audit;
- Android compile;
- Android Lint;
- APK signature verification;
- immutable exact-build archive;
- artifact upload.

## Prior physical evidence incorporated

The v0.6.4.2 exact build physically proved:
- short-window no-capture stability;
- 10→10 image/JPG stability in that run;
- exact identity-set stability;
- corrected enter/exit handshakes;
- verified P2P-group absence;
- zero media downloads.

v0.6.5 does **not** hard-code 10. All expected post-capture/retention values are derived from its runtime baseline.

## State-machine review

### Baseline — PASS
- one active media-count query;
- one P2P enter only after count write callback + valid count response;
- one strict catalog GET;
- requires image/JPG, video/MP4, recording/OPUS parity;
- stores baseline opaque identity sets in memory;
- verified exit requires exit write callback + matching post-exit 0x73/0x01;
- P2P group absence must be read back before capture watch begins.

### Capture watch — PASS
- fresh BLE/notification session after baseline cleanup;
- no P2P discovery/connect or HTTP request is initiated by the capture-watch path;
- user is told not to capture until ARMED;
- app issues no capture command;
- unchanged passive 0x73/0x01 inventory may be ignored;
- first changed inventory must be exactly baseline images +1;
- video/recording/config branch must remain unchanged;
- +2 or any other changed delta fails closed before post-capture P2P;
- exact +1 triggers exactly one active 02 04 confirmation.

### Post-capture catalog — PASS
- active inventory must independently confirm exact +1 image;
- one P2P enter then one catalog GET;
- current catalog must be baseline total +1 and JPG +1 only;
- every baseline JPG must remain;
- every baseline safe identity must remain;
- exactly one new JPG identity and exactly one new safe identity are required;
- the new JPG opaque identity and complete post-capture sets are retained in memory;
- zero media GETs;
- verified exit and P2P-group absence required before retention stage.

### Retention reconnect — PASS
- fixed 5000 ms reconnect delay after verified post-capture cleanup;
- fresh BLE/P2P/catalog cycle;
- one active inventory query;
- one catalog GET;
- compares current inventory and catalog counts to the post-capture snapshot;
- requires exact full safe-set equality and exact JPG-set equality for PASS;
- requires the one new JPG identity still present;
- requires zero missing baseline JPG identities;
- full BLE/catalog media parity required;
- verified exit and P2P group absence required before final result.

## Successful operation boundary

PASS condition requires exactly:
- media-count queries: 3;
- P2P enter writes: 3;
- transfer-exit writes: 3;
- catalog GETs: 3;
- total HTTP GETs: 3;
- media-file GETs: 0.

Static source contains:
- one media-count write callsite;
- one P2P-enter write callsite;
- one transfer-exit write callsite;
- one URL constructor;
- one HTTP GET callsite;
- zero media-download/import paths.

## Privacy and mutation boundary

PASS:
- raw remote filenames/paths are not logged or persisted;
- per-file deterministic hash tokens are not exported;
- credentials are not logged/persisted;
- catalog identities remain opaque/in-memory;
- no glasses file deletion/mutation command exists.

## Concurrent earlier run

Run `36893051068` passed safety/red-team/compile/lint/signature but its archive push failed because the later run had already advanced `main`:
`cannot lock ref ... main is at 390cf399... but expected 598e9aed...`.

This is a CI archival concurrency failure, not an app-code failure. It is **NOT PROMOTED** and has no authoritative exact-build bundle.

## Decision

**PASS FOR ONE BOUNDED PHYSICAL v0.6.5 TEST.**

This authorizes only the exact run `36893102299-attempt-1`.

The physical report must identify:
- App version `0.6.5`;
- Build commit `598e9aed94546bff2c9b018114932902aca67e05`;
- Build run `36893102299`;
- Build attempt `1`.

G6A remains open. G6B remains blocked.
