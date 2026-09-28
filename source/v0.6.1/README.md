# K G1 G6A Persistent Import v0.6.1

Pre-physical hardening of G6A.

v0.6.1 preserves the verified v0.6.0/G5.7 transport and adds one **read-only restart dedup verification mode** so the persistence claim can be proven physically without capturing or downloading another file.

## Physical test — Part 1: one persistent import

Use a fresh v0.6.1 install.

1. Force-stop Cyan Glasses.
2. Keep AIMB-G1 paired.
3. Open v0.6.1.
4. Confirm the restart-dedup button is disabled because the fresh ledger is empty.
5. Tap **Start G6A baseline**.
6. Wait for Phase A to complete.
7. Do **not** take a photo yet.
8. Tap **Arm visibility watch — then capture ONE photo**.
9. Wait until the app explicitly says **ARMED**.
10. Take exactly one photo using the glasses.
11. Keep the app in the foreground.
12. Let it complete the exact G5.7 visibility/P2P/catalog sequence and import one new JPG.
13. The report must show:
   - exactly +1 image;
   - exactly one new safe JPG;
   - exactly one media GET;
   - HTTP/length/JPEG validation PASS;
   - persistent final file committed;
   - deterministic app-generated local path;
   - ledger delta +1.
14. Copy the full report.
15. Force-stop v0.6.1 and reopen it.

Do not clear app data and do not uninstall between Part 1 and Part 2.

## Physical test — Part 2: restart dedup verification

Do **not** take another photo.

1. Keep Cyan force-stopped and AIMB-G1 paired.
2. Reopen v0.6.1 after the Part-1 force-stop.
3. The **Verify restart dedup — NO DOWNLOAD** button must now be enabled.
4. Tap it once.
5. The app performs only the proven read-only sequence:
   - one media-count query;
   - one P2P enter;
   - one `/files/media.config` GET;
   - in-memory SHA-256 comparison of safe JPG catalog entries against the persistent ledger;
   - transfer exit.
6. It must perform **zero media-file GETs**.
7. PASS requires at least one current catalog JPG to match a committed opaque ledger identity.
8. Copy the second full report.

Expected final line:

`G6A DEDUP VERIFY RESULT: PASS — PERSISTED ITEM RECOGNIZED AFTER RESTART; ZERO MEDIA REDOWNLOAD`

## Safety boundary

- no AP fallback;
- no `02 03`;
- no redirects;
- no Range/resume;
- no repeated polling;
- no MP4/OPUS transfer;
- no background service;
- no remote filename/path persistence or logging;
- no glasses deletion/mutation.

The verification mode adds no media download capability and no new glasses protocol command.
