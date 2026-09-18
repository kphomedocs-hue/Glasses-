# AIMB-G1 / K Site Capture

Authoritative repository for the AIMB-G1 glasses media-capture integration project.

## Current frozen baseline

**K G1 Discovery v0.1**

Purpose: read-only physical BLE discovery before any media-transfer command is sent to the glasses.

Current safety boundary:
- BLE scan and GATT connect/discovery only
- Enumerates services/characteristics/properties
- Reads only standard readable device-information fields
- No BLE characteristic writes
- No notification subscription
- No Wi-Fi/media-transfer command
- No Internet permission
- No OTA/reset/firmware operations

## Verified build

Repository-built APK SHA-256:

`c32758f898b91041bc7e13a272096d2629836e4465542ee00c1fbd9764e7479a`

Earlier first-build debug APK SHA-256 (provenance only):

`ab37790ad13028aa6e8f1e3d16b957c72f8038df0638e8e852b31488d9fc762a`

Both were built from the same frozen source; debug signing/build-environment differences can change APK bytes.

Source ZIP SHA-256:

`84fb1b957290abdf2464a97b35b81f2d1e8e976ce9c067ecaf571c8c19f22c68`

The v0.1 CI gate passed source-integrity verification, the read-only safety audit, Android compilation, Android Lint, and APK signature verification.

## Next physical gate

1. Force-stop Cyan Glasses.
2. Turn on AIMB-G1.
3. Install K G1 Discovery v0.1.
4. Grant Nearby Devices / Bluetooth permission.
5. Scan and inspect AIMB-G1.
6. Share the generated report.
7. Verify the physical device exposes the expected Cyan-family UUIDs before enabling any BLE command.

Expected candidate service family:
- `de5bf728-d711-4e47-af26-65e3012a5dc7`
- `de5bf729-d711-4e47-af26-65e3012a5dc7`
- `de5bf72a-d711-4e47-af26-65e3012a5dc7`

Do not add media-transfer, reset, OTA, or generic BLE-write functionality until the read-only physical discovery gate is reviewed.


Repository package SHA-256:

`e6b23d5d0eaed6a07112f7f14343e011299bf706fe0e98f447a74d24f92c827a`
