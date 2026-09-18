# LATEST CHECKPOINT

## Project
K Site Capture / AIMB-G1 glasses integration

## Frozen baseline
**K G1 Discovery v0.1**

Status: software build gate complete; first physical read-only test pending.

### Verified artifacts
- Repository-built APK SHA-256: `c32758f898b91041bc7e13a272096d2629836e4465542ee00c1fbd9764e7479a`
- Frozen source v4 ZIP SHA-256: `84fb1b957290abdf2464a97b35b81f2d1e8e976ce9c067ecaf571c8c19f22c68`
- Repository package ZIP SHA-256: `e6b23d5d0eaed6a07112f7f14343e011299bf706fe0e98f447a74d24f92c827a`
- Earlier first-build debug APK SHA-256 (provenance): `ab37790ad13028aa6e8f1e3d16b957c72f8038df0638e8e852b31488d9fc762a`
- Earlier local package SHA-256 (provenance): `608c2645470cd7ade65a8126242b04391333408fb125b51d391278ae8d3906fe`

The repository APK/package are fresh verified CI rebuilds from the exact same frozen source. Debug signing and ZIP metadata can make byte hashes differ from earlier locally produced files.

### Build gates passed
- exact source-integrity verification
- read-only safety audit
- Java/Android compilation
- Android Lint: 0 errors
- APK signature verification

## Confirmed Cyan/AIMB-G1 findings
The installed Cyan Glasses package analysed was `com.aitowe.aitoglasses`, version `1.0.2.18_20260811`.

Candidate BLE profile recovered from Cyan:
- Service: `de5bf728-d711-4e47-af26-65e3012a5dc7`
- Notify: `de5bf729-d711-4e47-af26-65e3012a5dc7`
- Write: `de5bf72a-d711-4e47-af26-65e3012a5dc7`

Cyan media-mode payload recovered from app code:
- payload: `02 01 04 01`
- command ID: `0x41`
- packet framing begins `BC 41`
- CRC algorithm: CRC-16/MODBUS, payload-only, little-endian
- candidate complete media-mode frame: `BC 41 04 00 93 5C 02 01 04 01`

Do **not** send this frame until the physical read-only discovery report confirms the expected GATT profile.

Other Cyan findings:
- `media.config`
- `WifiP2pManager`
- local HTTP media retrieval
- OPUS audio
- local Wi-Fi name/password/IP fields
- `glass_album` media metadata database
- `02 01 04 02` appears to be an alternate AP-mode media path
- `02 01 09` appears after download completion
- `02 01 0F` is associated with P2P reset and must not be used in initial testing

## v0.1 safety boundary
Allowed:
- BLE scan
- connect
- discover GATT services/characteristics
- read selected standard Device Information fields
- disconnect
- generate/share report

Forbidden in v0.1:
- BLE characteristic writes
- descriptor writes
- notification subscription
- Wi-Fi/network access
- media-transfer commands
- reset/OTA/firmware operations

## Next action
1. Force-stop Cyan Glasses.
2. Turn on AIMB-G1.
3. Install K G1 Discovery v0.1.
4. Grant Nearby Devices/Bluetooth permission.
5. Scan and inspect AIMB-G1.
6. Share the generated report.
7. Confirm the physical G1 exposes the expected Cyan-family UUIDs.
8. Only after review, design v0.2 for the first tightly controlled protocol write.

This file is authoritative for resuming the project.
