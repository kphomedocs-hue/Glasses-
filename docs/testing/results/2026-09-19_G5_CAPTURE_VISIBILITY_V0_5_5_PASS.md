# G5 Capture Visibility v0.5.5 — PHYSICAL PASS

Date: 2026-09-19
Generated: 2026-09-19T23:40:38+0530
App: K G1 Capture Visibility Probe v0.5.5
Gate: G5 capture visibility / BLE only

## Scope

- one continuous BLE connection through user capture;
- exactly two proprietary writes total, both `0x41 / 02 04`;
- passive sanitized `0x73` event observation;
- no P2P, Wi-Fi, HTTP, catalog access, media GET, file write/delete, AP mode or `02 03`.

## Baseline

- image count: 5
- video count: 0
- recording count: 1
- configFileType: 1
- onlySupportApImport: false
- write callback: SUCCESS
- inventory response: VALID

## Armed 60-second capture window

The app armed the observation window before the user captured exactly one disposable photo.

A passive `0x73 / 0x01` media-inventory event was observed during the armed window:
- images: 6
- videos: 0
- recordings: 1
- configFileType: 1

Observed `0x73` event IDs during the session:
- `0x01`
- `0x05`

## Final inventory recheck

Second `0x41 / 02 04`:
- write callback: SUCCESS
- response: VALID
- images: 6
- videos: 0
- recordings: 1
- configFileType: 1
- onlySupportApImport: false

Deltas:
- images: +1
- videos: 0
- recordings: 0

## Conclusion

**PASS — the physical capture became visible to the glasses inventory within the bounded 60-second BLE-only observation window.**

This resolves the earlier G5.0/G5.2 no-delta uncertainty: the capture pipeline is not permanently missing the image; visibility can occur asynchronously after capture.

The passive `0x73 / 0x01` increase was observed before the final active inventory query, and the final `02 04` query independently confirmed the +1 image count.

## Next gate

Do not jump to G6.

The next G5 candidate should be **visibility-gated single-JPG transfer**:
1. obtain a baseline catalog safely;
2. keep or restore BLE observation while transfer mode is OFF;
3. user captures exactly one disposable JPG;
4. wait for physical inventory visibility (prefer passive `0x73/0x01`; bounded `02 04` recheck may confirm);
5. only after image count increases by exactly one, enter the proven P2P path;
6. fetch the catalog once;
7. require exactly one new safe relative `.jpg` by set difference;
8. perform at most one media GET;
9. validate and stop.

All previous G5 safety guards remain mandatory.
