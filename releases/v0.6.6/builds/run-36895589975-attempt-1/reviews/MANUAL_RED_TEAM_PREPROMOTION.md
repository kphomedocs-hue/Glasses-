# Manual Red-Team Pre-Promotion Review — v0.6.6 run 36895589975 attempt 1

Date: 2026-10-01

## Exact build identity

- Version: v0.6.6
- Build commit: `1ea64b26f311e6b2f18be536e1548766dabe8aa9`
- Build run: `36895589975`
- Build attempt: `1`
- APK SHA-256: `29920782f16e5ef5785ef62d835622b3dbff0cb1b53f0f5848306f13cf76343d`
- Exact source ZIP SHA-256: `c07333baa71377d0fc8b7c8bd5b7f661946fdfef949883585c332269cd347674`
- MainActivity blob: `185fa521b306f56c22bf3bc2cd4df116c8a2068e`

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

v0.6.4.2 physically proved short-window no-capture stability.

v0.6.5 physically proved:
- one intended photo produced exact +1 BLE image count;
- post-capture catalog gained exactly one JPG/safe entry;
- no baseline identity disappeared;
- the one new JPG survived verified exit/group cleanup and a fresh reconnect;
- zero media-file GETs occurred.

v0.6.6 does not hard-code prior counts. It derives all expected values from its runtime baseline/post-capture sets.

## State-machine review

### Baseline + capture + post-capture — PASS
- v0.6.5 exact +1 passive gate retained;
- active `02 04` +1 confirmation retained;
- exact one-JPG / one-safe-entry set delta retained;
- no baseline loss allowed;
- verified exit and P2P-group absence retained.

### Pre-GET retention — PASS
- fresh reconnect before any media GET;
- inventory/catalog must exactly equal the post-capture snapshot;
- new JPG identity must still be present;
- all baseline identities must remain;
- full BLE/catalog media parity required;
- media GET is blocked unless this stage is exact.

### Exact media selection — PASS
- candidate is resolved from the CURRENT pre-GET catalog;
- selection uses the in-memory SHA-256 identity of the one new JPG;
- final request guard rechecks exact candidate hash, JPG extension, strict relative path and confirmed P2P IPv4;
- raw remote filename/path is not logged/persisted.

### One media GET — PASS
- exactly one media-GET counter increment site;
- one catalog URL constructor + one media URL constructor only;
- one catalog GET callsite + one media GET callsite only;
- redirects disabled;
- no custom headers/Range;
- no retry/resume;
- 32 MiB hard cap;
- HTTP 200 required;
- Content-Length consistency required when supplied;
- JPEG SOI and EOI required;
- app-private cache file only;
- file deletion required before success;
- no persistent import;
- no ledger update.

### Post-GET retention — PASS
- verified exit and P2P-group absence after GET;
- fresh reconnect after bounded delay;
- inventory/catalog compared to the exact post-capture/pre-GET state;
- full safe identity-set equality required;
- JPG identity-set equality required;
- downloaded new JPG identity must still be present;
- zero baseline JPG loss required;
- final exit and group absence required.

### Successful operation boundary — PASS
Final PASS requires exactly:
- media-count queries: 4;
- P2P enter writes: 4;
- transfer-exit writes: 4;
- catalog GET requests: 4;
- media-file GET requests: 1;
- total HTTP GET requests: 5.

### Failure cleanup — PASS
The run-1 manual defect was corrected:
- the PRE-GET post-cleanup fallback no longer calls the generic abort handler while already in CLEANUP;
- it directly records failure and re-enters the finalizer so the report terminates fail-closed.

## Privacy/mutation boundary

PASS:
- no credentials logged/persisted;
- no raw remote filename/path logged/persisted;
- no deterministic per-file hash tokens exported;
- no SharedPreferences/database/media-store persistence path;
- no glasses-side delete/rename/mutation command.

## Decision

**PASS FOR ONE BOUNDED PHYSICAL v0.6.6 TEST.**

Only exact run `36895589975-attempt-1` is authorized.

The report must identify:
- App version `0.6.6`;
- Build commit `1ea64b26f311e6b2f18be536e1548766dabe8aa9`;
- Build run `36895589975`;
- Build attempt `1`.

G6A remains open. G6B remains blocked.
