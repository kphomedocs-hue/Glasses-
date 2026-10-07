# v0.6.8.4 Version Manifest

- Version: v0.6.8.4
- Build role: bounded one-`0x40` initialization -> media-count -> P2P-enter credential diagnostic
- Package ID: `com.parkarsite.g6ainitdiag684`
- Gate role: **DIAGNOSTIC ONLY**; cannot close G6A or unblock G6B
- Physical status: **TRANSPORT RECOVERED; P2P CREDENTIAL BLOCKER PERSISTS AFTER RECOVERY**

## Question

Does one already physically proven Cyan-equivalent `0x40` time-sync initialization write immediately before the existing media-count + P2P-enter sequence change the credential response behavior seen in v0.6.8.3?

## Frozen scope

Exactly:
1. subscribe to Cyan notify;
2. one dynamic Cyan-equivalent `0x40` time-sync write;
3. wait for its BLE write callback only;
4. one `0x41 / 02 04` media-count query;
5. one `0x41 / 02 01 04 01` P2P-enter write;
6. 10 s normal + at most 10 s passive late credential observation;
7. one `0x41 / 02 01 09` transfer-exit write.

No photo, Wi-Fi Direct, HTTP, catalog/media GET, persistent import, receipt, delete/mutation, or retry loop.

### run-36973860827-attempt-1 — NOT PROMOTED
- exact bundle: `builds/run-36973860827-attempt-1/`
- build commit: `9773de8611d4762a127a3d09e9ff5521e0976451`
- APK SHA-256: `73c01e539cd0dd6183def175a0a53cca9ed8c99c98c83e8d4e8b655638bf984d`
- exact source ZIP SHA-256: `02dc3f8b07c713e535b43ac121fc39d642e6042a94649b3de16a89ca996be56a`
- CI/static status: verified
- physical status: not run

### run-36973886568-attempt-1 — AUTHORIZED DIAGNOSTIC CANDIDATE
- exact bundle: `builds/run-36973886568-attempt-1/`
- build commit: `506b3f311be7e2dd8a6cd5ce8f7bf36615f95e25`
- APK SHA-256: `06184f7c634ccb940fa64989c035a03ed96cf43835d0c413aea3a43931fae7e1`
- exact source ZIP SHA-256: `16a139b81cc05caadd2f05b93d74bea4b4be46ddc1e17f4c84af7c006f112789`
- CI/static status: verified
- physical status: first physical run completed; cold-recovery rerun at 17:05 +0530 failed at GATT connection timeout before any protocol write

### run-36973982291-attempt-1 — NOT PROMOTED
- exact bundle: `builds/run-36973982291-attempt-1/`
- build commit: `fe745397c862204ed8fabbbd234bdba16cc5c6db`
- APK SHA-256: `96648639e5ec515277887586c80b88da7de75cd22efa11666c6e4347e3eb335b`
- exact source ZIP SHA-256: `ab78706ba58947e96853b59e946cd980ee69e972863ff338ad3775955e4f0d1e`
- CI/static status: verified
- physical status: not yet run


## Current and only authorized diagnostic candidate

- run: `36973886568`
- attempt: `1`
- build commit: `506b3f311be7e2dd8a6cd5ce8f7bf36615f95e25`
- package: `com.parkarsite.g6ainitdiag684`
- APK SHA-256: `06184f7c634ccb940fa64989c035a03ed96cf43835d0c413aea3a43931fae7e1`
- exact source ZIP SHA-256: `16a139b81cc05caadd2f05b93d74bea4b4be46ddc1e17f4c84af7c006f112789`
- exact bundle: `builds/run-36973886568-attempt-1/`

Manual red-team:
`builds/run-36973886568-attempt-1/reviews/MANUAL_RED_TEAM_PREPROMOTION.md`

The other v0.6.8.4 CI runs are not authorized for physical use.

Current physical instruction:
- no uninstall is needed if v0.6.8.4 is the only K G1 test app installed;
- force-stop Cyan Glasses and v0.6.8.4;
- fully power-cycle AIMB-G1 and leave it off briefly;
- power it back on and wait for Bluetooth to settle;
- do not open Cyan, unpair, clear app data, or take a photo;
- run the same exact v0.6.8.4 once after recovery;
- return the complete report.

G6A remains OPEN. G6B remains BLOCKED.


## Physical completion note

The authorized run `36973886568` was executed on 2026-10-02.

Result: the added one-command initialization step completed successfully, but the subsequent transfer-entry behavior remained unchanged from the preceding diagnostic. No transfer setup response was obtained during the bounded observation.

Conclusion: the single initialization-step hypothesis is not supported by this physical run.

G6A remains OPEN. G6B remains BLOCKED.


## Cold-recovery rerun — 2026-10-02T17:05:02+0530

The same authorized v0.6.8.4 build was rerun after cold recovery.

Result:
- exactly one AIMB-G1-family bonded record was found;
- GATT did not reach CONNECTED within the existing 30-second bound;
- no 0x40/media-count/P2P-enter/exit write occurred;
- no Wi-Fi Direct, HTTP, catalog, media, or capture activity occurred.

Evidence:
`builds/run-36973886568-attempt-1/reports/2026-10-02_G6A_V0_6_8_4_COLD_RECOVERY_GATT_TIMEOUT.md`

This is a BLE transport-precondition failure, not a repeat of the credential-response result.

One transport-recovery retry of the same exact build is authorized because the failed run reached zero proprietary writes. Do not build a new protocol variant.

Transport recovery:
- force-stop Cyan Glasses and v0.6.8.4;
- cycle phone Bluetooth OFF then ON;
- fully power AIMB-G1 OFF then ON;
- allow Bluetooth to settle;
- do not unpair, clear app data, open Cyan, or take a photo;
- run the same exact v0.6.8.4 once.

G6A remains OPEN. G6B remains BLOCKED.


## 2026-10-07 recovery verification

Exact authorized build was rerun after Bluetooth/device recovery.

- GATT connected normally.
- Notification subscription passed.
- 0x40 write and response were observed.
- Media-count handshake passed.
- Current inventory was 0 / 0 / 0.
- P2P-enter write/callback passed.
- The same short non-credential response reappeared.
- No credential-bearing response appeared within the bounded observation.
- Zero Wi-Fi Direct, HTTP, catalog/media, or capture activity.

Evidence:
`builds/run-36973886568-attempt-1/reports/2026-10-07_G6A_V0_6_8_4_TRANSPORT_RECOVERED_CREDENTIAL_BLOCKER_PERSISTS.md`

Do not repeat v0.6.8.4. Next root-cause test is the historically successful direct G3B entry sequence, without adding a new proprietary command.
