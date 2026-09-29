# K G1 G6A Remote Retention Diagnostic v0.6.3

v0.6.3 follows the v0.6.2 physical finding that the post-capture remote catalog returned from 9 JPGs to 8 JPGs after restart.

The purpose is to determine, without logging remote filenames and without downloading media during verification, whether the remote catalog returns exactly to the saved pre-capture baseline or changes in another way.

## What is persisted

After the single allowed Part-1 JPG import, the app stores a private retention capsule containing only:
- SHA-256 identities for every pre-capture baseline JPG catalog entry;
- SHA-256 identity for the one newly discovered JPG.

No raw remote filename/path text is stored.

The existing v0.6.2 identity capsule is retained for secondary comparison.

## Part 2 classification

Restart verification performs:
- one media-count query;
- one P2P enter;
- one catalog GET;
- zero media-file GETs;
- one transfer exit.

It compares the current JPG hash set with the saved baseline and saved new-item hash and classifies:
- exact baseline reversion, new item absent;
- stable baseline plus new item;
- new item present but catalog membership changed;
- new item absent and catalog membership also changed.

## Physical test

Use a fresh v0.6.3 install.

Part 1:
1. Force-stop Cyan Glasses and keep AIMB-G1 paired.
2. Open v0.6.3.
3. Start baseline.
4. After Phase A completes, arm visibility.
5. Wait for ARMED.
6. Capture exactly one photo.
7. Keep the app foregrounded until persistent import completes.
8. Copy the Part-1 report.
9. Force-stop/reopen without clearing data or uninstalling.

Part 2:
1. Do not take another photo.
2. Tap **Verify restart dedup — NO DOWNLOAD**.
3. Copy the full report.
4. Require zero media-file GETs.

G6B remains blocked until this result is understood.

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
