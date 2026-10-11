# VGL Android synthetic emulator — API 36 runtime verified

**Latest tested source:** 7495e9bba9a0bcf0323e859fcd19f10902f251c9
**GitHub emulator CI:** https://github.com/kphomedocs-hue/Glasses-/actions/runs/38141611598 — SUCCESS; VGL-EMU-01 PASS, VGL-EMU-02 PASS.
**Report:** docs/testing/results/2026-10-11_VGL_V3_EMULATOR_ANDROID_RUNTIME_RESULT.md
**Red team:** docs/testing/VGL_V3_EMULATOR_RED_TEAM_REVIEW_2026-10-11.md

## Source and simulation boundaries
This is a separate, simulation-only Android app; it is NOT the shipping K G1 application. It uses SDK 36, Gradle 9.6, Java 17, a distinct application ID and Android API 36 minimum/target. Its Gradle sourceSets compile the SAME real VirtualGlassesCore, VirtualLabPolicies, protocol/peer-identity and RecoveryImportStore source files as the offline lab. There is no copied policy implementation.

Its Android manifest requests ZERO permissions. The Android app includes no live Bluetooth, Wi-Fi Direct, network, external storage, media GET or vendor glasses access. The fixture is a generated seven-byte marker-only JPG **not a decodable photograph**.

## Verified runtime tests on ONE virtual Android 16 device
- Real Android app-private no-backup storage commit, receipt checksum verification and reopening through RecoveryImportStore.
- Separate injected exceptions at the BEFORE-SOURCE marker and AFTER-VERIFIED journal barriers: prevent blind re-fetch and locally reconstruct receipt from verified bytes.
- **Two separate instrumentation sessions**, with Android app/test force-stop in between; no data or source GET lost/duplicated.
- Android-source safety guard and packaged APK permission guard pass; no phone or real glasses connected.
- Generated debug simulator app+test APKs exist only temporarily on GitHub runner, installed/uninstalled only on virtual device; no upload/release or user-downloadable APK created.

The actual on-emulator runner is:
    bash lab/virtual-aimb-g1/android-simulator/run_emulator_only.sh

It REQUIRES exactly one emulator serial, ro.kernel.qemu=1 and API 36 before Gradle assembly/ADB installation. A real device triggers fail-closed refusal. The full workflow lives in .github/workflows/vgl-android-api36-emulator.yml. A separate existing source-only compile workflow remains intact.

## Debugging lineage
The first API36 run compiled but instrumentation failed during class init. Safe stage/class diagnosis identified an Android ICU regex incompatibility: closing braces in the synthetic JSON catalog pattern were unescaped. The SAME shared VirtualLabPolicies class was fixed and both host+Android tests passed. No emulator failure was reclassified as an earlier pass.

## Limits still open
This verifies **only synthetic Android private-storage semantics**, not physical power-loss, real Android HTTP, location/permission lifecycle, group association, proprietary 0x41/0x73, real JPG/MP4/OPUS integrity, production APK wiring or retention of remote originals on glasses. The host-JVM local HTTP reference tests (7 + 10 integrated cases) remain independent of Android runtime.

The historical physical JPG GET followed by EXIT consumed the original photograph. The owner's original-on-glasses retention requirement remains HARD BLOCKED. No real glasses media transfer, phone installation or physical device experiment has been authorized.

**Physical G6A OPEN; G6B BLOCKED.**
