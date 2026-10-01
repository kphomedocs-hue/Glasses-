# K G1 G6A No-Capture Catalog Stability v0.6.4.2

v0.6.4.2 is the red-team-corrected successor to blocked v0.6.4.1.

## Purpose

Test whether AIMB-G1 BLE inventory and the local `/files/media.config` catalog remain stable during a bounded no-capture interval, with zero media-file downloads.

## Red-team corrections

- RT-01: generic command-`0x41` frames no longer prove exit. After the exit write callback, the app requires a valid physically-established `0x73/0x01` inventory event whose image/video/recording counts and config type match the current snapshot.
- RT-02: failures use one centralized abort path. If P2P enter was actually started and exit has not been attempted, the app attempts the single allow-listed exit once, then performs bounded local P2P cleanup/readback.
- RT-03: Wi-Fi Direct peer name comparison is exact and case-sensitive.
- RT-04/RT-06: build artifacts are immutable under `releases/v0.6.4.2/builds/run-<run>-attempt-<attempt>/`; no build overwrites a previous build.
- RT-05: source ZIP is created with `git archive` from the exact `GITHUB_SHA` before mutable `main` is checked out for archival commit.
- RT-08: per-file deterministic hash tokens are not reported.
- RT-09: any catalog line that differs from `line.trim()` is rejected; BOM-bearing catalogs are rejected rather than normalized.
- RT-10: v0.6.4.2 uses a new package ID, `com.parkarsite.g6astability642`, so physical instructions use a fresh install and do not rely on unproven debug-signing upgrade continuity.

Additional tightening:
- BLE/catalog parity covers JPG/images, MP4/videos, and OPUS/recordings in both snapshots.
- exact operation totals remain mandatory for PASS.
- P2P group absence is verified after each snapshot.
- the report embeds build commit, run ID, and run attempt for exact provenance.

Do not physically run a build until its CI, red-team audit, immutable bundle, and manual state-machine review are complete.
