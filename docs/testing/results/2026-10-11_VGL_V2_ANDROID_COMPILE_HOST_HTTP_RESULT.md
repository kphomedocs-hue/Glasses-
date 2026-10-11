# VGL V2 — Android compile and localhost HTTP evidence, 2026-10-11

**Verified source revision:** c16b5898fd3d7caf8b2261ddec2c7ed80f5a2c91

## Direct GitHub Actions results
- Android simulator **source compilation only** including shared core, Android Activity and instrumentation hook: https://github.com/kphomedocs-hue/Glasses-/actions/runs/38132519731 — SUCCESS.
- 96/96 JVM synthetic scenario IDs, two red-team suites, exact-ID manifest, plus **7/7 host localhost HTTP cases**: https://github.com/kphomedocs-hue/Glasses-/actions/runs/38132519735 — SUCCESS.
- Repository hygiene: https://github.com/kphomedocs-hue/Glasses-/actions/runs/38132519714 — SUCCESS.
- Existing RecoveryImportStore regression after portability changes: https://github.com/kphomedocs-hue/Glasses-/actions/runs/38132290876 — SUCCESS.

## Implemented
- New additive lab/virtual-aimb-g1/android-simulator Android Gradle 9.6, SDK/API 36, Java 17 project with a separate application ID. Its Gradle sourceSets compile the exact Java lab, protocol, identity and recovery source modules; no copied core.
- AndroidHarnessController and MainActivity are an offline test-only UI/path. Controller uses fake BLE callbacks, an entirely synthetic marker-byte JPG, actual shared capture/retention policy and real RecoveryImportStore in Android no-backup private storage. Its Android runtime behaviour has NOT BEEN EXECUTED.
- Source-only instrumentation hook tests the same controller twice and would check local idempotence; it has COMPILED but was NOT RUN on an emulator.
- No Internet, Bluetooth, Wi-Fi, location permission, radio adapter or remote GET. CI checked merged manifest and no generated APK/AAB. No build was installed.
- Android SDK did not expose Files.readString used by RecoveryImportStore. Replaced three metadata reads with strict UTF-8 decoding bounded to 4096 bytes, preserving malformed-record rejection. Java recovery tests and new Android compile passed.
- New lab/virtual-aimb-g1/host-http isolated Java 17 loopback-only HTTP server/client at 127.0.0.1. All seven real HOST-JVM HTTP tests passed: successful single synthetic GET/durable commit/duplicate blocking, status 500, redirect 302, short body, invalid JPEG bytes, simulated remote-consume-on-GET with local receipt, and nonloopback host refusal. No glasses host or owner media was used.

## Corrected failures
- Initial Android CI 38132188798 FAILED on Files.readString SDK compatibility; fixed at source commit 5d0c1d2a1d3b385efd4010b1549ce40b835e10de, passed Android run 38132290819.
- First loopback CI 38132421167 FAILED in javac because VirtualClock.java was missing from compile inputs; corrected script at 8b7682b758c428c545f0f48f0412201d94e1d6c4, then 7/7 PASS at 38132455549.
- Android minSdk corrected from 26 to API 36 at c16b5898fd3d7caf8b2261ddec2c7ed80f5a2c91. Final Android, 96+7 and hygiene runs all SUCCESS.

## Carefully bounded verdict
**VERIFIED:** Android source compiles with real shared Java and recovery store, no-permissions/no-APK guard passes; 96 synthetic JVM assertion subsets and 7 real local-host HTTP fault simulations pass. **NOT VERIFIED:** Android runtime instrumentation, Android socket/network permissions, emulator installation, phone or glasses, Wi-Fi Direct P2P, authenticated current AIMB-G1, short 0x41 meaning, original-preserving import, actual codec fidelity or final production app integration.

The consume-on-GET synthetic case demonstrates that a local receipt does NOT retain the remote original. The previous real glasses media GET/EXIT consumed the original. The user's original-retention requirement is unsatisfied.

No APK assembled, no Android emulator booted, no phone/glasses/media touched. Historic releases unchanged.
**G6A OPEN; G6B BLOCKED.**
