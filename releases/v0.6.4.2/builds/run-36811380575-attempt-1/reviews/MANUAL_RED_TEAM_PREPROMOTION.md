# Manual Red-Team Pre-Promotion Review — run 36811380575 attempt 1

Date: 2026-10-01

## Runtime/source checks

PASS:
- generic exit-time 0x41 frames are ignored for exit confirmation;
- post-exit confirmation can be set only from a valid 0x73/0x01 event after the exit write callback;
- the post-exit inventory must match the active snapshot counts/config type;
- failures route through centralized abort logic;
- abort attempts the single allow-listed exit when enter actually started and exit has not yet been attempted;
- local P2P cleanup is bounded and group absence is read back;
- peer-name equality is case-sensitive;
- per-file deterministic hash tokens are not reported;
- whitespace-normalized catalog identities are rejected;
- BOM-bearing catalogs are rejected;
- full image/video/recording vs JPG/MP4/OPUS parity is required;
- exact operation totals are required;
- runtime report embeds build commit/run/attempt;
- package ID is new, so no in-place update-signing assumption is required.

## Provenance check

FAIL:
- the generated VERSION_MANIFEST entry lost the exact bundle path, build commit, APK SHA, and source SHA.
- root cause: Markdown backticks in an unquoted shell heredoc were executed as shell command substitutions.

## Decision

**NOT PROMOTED. DO NOT PHYSICALLY RUN THIS BUILD.**

The exact build bundle itself remains preserved for provenance. A corrected archive generator must produce a new immutable build instance and that new instance must be manually rechecked before promotion.
