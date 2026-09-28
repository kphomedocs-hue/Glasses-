# K G1 G6A Identity Diagnostic v0.6.2

v0.6.2 is a bounded diagnostic follow-up to the v0.6.1 physical restart-dedup mismatch.

The transport boundary remains the proven G5.7/G6A path. The purpose of this build is to determine whether the glasses' remote catalog identity is stable across restart without logging remote filenames/paths and without downloading media during verification.

## What v0.6.2 adds

After the one allowed Part-1 JPG import, the app stores a private diagnostic capsule containing only:
- SHA-256 of the exact remote catalog identity;
- SHA-256 of the basename only;
- SHA-256 of a lowercase form;
- character length;
- path-component count.

No remote filename/path text is stored in the diagnostic capsule.

During restart verification it performs one inventory query, one P2P lifecycle, one catalog GET, and zero media GETs. For each current JPG it derives the same privacy-safe values in memory and classifies a mismatch as:
- exact identity stable;
- directory/path changed while basename stayed stable;
- case-only change;
- identity text changed while nonsecret shape stayed stable;
- substantial identity change / committed item absent from current catalog.

Only truncated 12-hex diagnostic tokens may appear in the report. Raw remote filenames and paths remain unlogged.

## Physical test

Use a fresh v0.6.2 install.

Part 1:
1. Force-stop Cyan Glasses and keep AIMB-G1 paired.
2. Open v0.6.2.
3. Tap **Start G6A baseline** and wait for Phase A completion.
4. Tap **Arm visibility watch — then capture ONE photo**.
5. Wait for **ARMED**, then capture exactly one photo.
6. Keep the app in the foreground through the single persistent import.
7. Copy the full Part-1 report.
8. Force-stop v0.6.2 and reopen it. Do not clear data or uninstall.

Part 2:
1. Do not take another photo.
2. Tap **Verify restart dedup — NO DOWNLOAD**.
3. Copy the complete report.
4. Require zero media-file GETs.

G6B remains blocked until the identity-stability result is understood and G6A restart dedup passes safely.

## Safety boundary

- no AP fallback;
- no 02 03;
- no redirects;
- no Range/resume;
- no repeated polling;
- no MP4/OPUS transfer;
- no background service;
- no raw remote filename/path persistence or logging;
- no glasses deletion/mutation.
