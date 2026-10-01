# Manual Red-Team Pre-Promotion Review — run 36811812916 attempt 1

Date: 2026-10-01

## Exact build identity

- Version: v0.6.4.2
- Build commit: `e97bfc4ac66d6de49007296fdfff311df129801d`
- Build run: `36811812916`
- Build attempt: `1`
- APK SHA-256: `550d4935f17ed6250feda603a9d2963e79aaa716dd20ed59be00124e97f4ae99`
- Exact source ZIP SHA-256: `5a353681d94957025b6c9c9e4db2c4699ea16e36713cc562caec80fac73b567d`
- MainActivity blob at build commit: `1e58b94345889e6fdceb9c7ff19e275128eebc3a`

## CI gates

PASS:
- safety audit;
- dedicated red-team static audit;
- Android compile;
- Android Lint;
- APK signature verification;
- immutable exact-build archive;
- artifact upload.

## Runtime/state-machine review

PASS:
- media-count handoff requires both Android write callback and valid `02 04` response;
- P2P-enter handoff requires both Android write callback and valid `02 01 04 01` credential response before discovery;
- exactly case-sensitive BLE-reported P2P peer name is used;
- one catalog GET code path only;
- zero media-file GET/import code path;
- generic exit-time `0x41` frames are explicitly ignored for exit proof;
- exit proof requires exit write callback followed by a valid matching `0x73/0x01` inventory event;
- abort path attempts at most one allow-listed exit after an enter write was started and then performs bounded local P2P cleanup;
- P2P group absence is read back after cleanup;
- catalog BOM is rejected;
- leading/trailing-whitespace catalog lines are rejected rather than normalized;
- no per-file deterministic hash tokens are exported;
- BLE/catalog parity covers images/JPG, videos/MP4, recordings/OPUS;
- exact full-run operation totals are required for PASS;
- report embeds build commit, run ID, and run attempt.

## Provenance review

PASS:
- exact build bundle is under immutable run/attempt path;
- manifest entry contains nonblank exact path, build commit, APK hash, and exact source hash;
- source ZIP was generated from exact `GITHUB_SHA` before switching to mutable `main`;
- APK and source hashes in BUILD_INFO agree with their dedicated SHA files.

## Non-blocking note

BUILD_INFO contains a literal `\n` between two descriptive bullet lines due to shell string formatting in the archive script. This is cosmetic only; build identity, APK hash, source hash, and runtime behavior are unaffected.

## Decision

**PASS FOR BOUNDED PHYSICAL TESTING.**

This does not pass G6A itself. It only authorizes one physical no-capture stability run using this exact APK.

Physical report must identify:
- Build commit `e97bfc4ac66d6de49007296fdfff311df129801d`;
- Build run `36811812916`;
- Build attempt `1`.

If those identifiers do not match, do not attach the report to this build.
