# G5 v0.5.0 Physical Result — SAFE NO-DELTA STOP

Date: 2026-09-19  
App: K G1 Disposable Photo Probe v0.5.0  
Gate: G5 one disposable JPG download by two-phase catalog delta  
Result: **SAFE STOP — no new catalog entry; zero media-file GETs**

## Phase A

Confirmed:
- exactly one bonded AIMB-G1-family target;
- Cyan GATT service/notify/write present;
- notification subscription succeeded;
- P2P enter `0x41 / 02 01 04 01` succeeded once;
- transfer credentials were valid and not logged;
- exact BLE-reported Wi-Fi Direct peer matched;
- P2P group formed with phone as group owner;
- passive `0x73 / 0x08` resolved the glasses IPv4;
- one GET to `/files/media.config`;
- HTTP 200, `text/plain`, 67 bytes, valid UTF-8, no BOM;
- line-list entries: 3;
- safe relative entries: 3;
- duplicate/unsafe entries: 0;
- extensions: `.jpg=2, .opus=1`;
- baseline held in memory only;
- zero media-file GETs;
- transfer exit succeeded.

## User action

The app reported Phase A complete and transfer mode exited.  
The user then captured exactly one disposable JPG with the glasses before starting Phase B.

## Phase B

Confirmed:
- second P2P enter succeeded;
- exact peer association succeeded;
- passive glasses IP resolved again;
- second GET to `/files/media.config`;
- HTTP 200;
- response remained exactly 67 bytes;
- catalog remained 3 safe entries;
- extension summary remained `.jpg=2, .opus=1`;
- complete Phase A baseline was still present;
- **new catalog entries: 0**.

Because the exact-one-new-entry guard failed, the app:
- performed **zero media-file GET requests**;
- sent the transfer-exit command;
- cleaned up P2P;
- logged no remote filename/path values.

Totals:
- P2P enter writes: 2;
- transfer-exit writes: 2;
- catalog GETs: 2;
- media-file GETs: 0;
- total HTTP GETs: 2.

## Interpretation

This is not a media transport failure. The safety gate stopped before any media request because the manifest did not reflect the newly captured photo.

The unchanged 67-byte catalog suggests the Phase B request read the same manifest state as Phase A.

A static recheck of Cyan immediately after this result found two parity behaviors absent from v0.5.0:
1. `PictureFragment.loadDataData()` calls `readAlbumCounts()`, which sends the already-proven `0x41 / 02 04` media inventory query before the user starts import.
2. `PictureFragment.downloadMediaConfig()` waits **1000 ms** after the P2P-ready conditions before its delayed lambda fetches the catalog.

Those findings are recorded separately in:
`docs/research/CYAN_G5_REFRESH_PARITY_2026-09-19.md`.

## Conclusion

**v0.5.0: SAFE NO-DELTA STOP.**

No evidence supports downloading an existing catalog entry by guesswork.  
The next candidate must restore the two missing exact-Cyan parity behaviors while preserving the same exact-one-new-JPG selection rule.

G6 remains blocked.
