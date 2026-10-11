# VGL V3 — API 36 single-emulator private-storage runtime result

**Date:** 2026-10-11 (Asia/Kolkata)
**Exact tested source commit:** 7495e9bba9a0bcf0323e859fcd19f10902f251c9
**Verified Android emulator workflow:** https://github.com/kphomedocs-hue/Glasses-/actions/runs/38141611598 — SUCCESS
**Scope:** A GitHub-hosted single Android 16/API 36 virtual device running isolated synthetic-only instrumentation. No owner's phone, AIMB-G1 glasses or client media.

## Executed evidence

- The Android emulator runner created/booted ONE Google APIs x86_64 API 36 virtual device with virtual-hardware check; source manifest permission/network/radio preflight PASS.
- Android Gradle built ephemeral, separately identified debug **simulator app and instrumentation APKs** on the runner only. These were installed only on the emulator; the runner uninstalled both. No APK was uploaded to releases or provided for physical installation.
- The pre-install permission check rejected any requested permissions in both APKs; no Internet, Bluetooth, Wi-Fi Direct or location permission could be used by the Android simulator.
- Instrumentation executed twice in **separate Android process invocations**, with app/test force-stop in between. Durable app-private no-backup storage was retained for the second invocation.
- GitHub job logs include **VGL-EMU-01 PASS**, **VGL-EMU-02 PASS** and **VGL_EMULATOR 2/2 PASS**, followed by the physical-gate guard PASS.
- Both invocations called actual AndroidHarnessController and shared VirtualGlassesCore / VirtualLabPolicies / PeerIdentityPolicy and actual RecoveryImportStore rather than a copied code path.
- Both invocations verified app-private opaque receipt and byte integrity, local reopen; a synthetic interruption immediately AFTER_FETCH_MARK (with zero Source reads) classified REMOTE_UNCERTAIN and rejected a later Source retry; a fault after VERIFIED journal recovered already written bytes locally and reconstructed a committed receipt with no second remote GET.
- No network traffic, external storage, source glasses operation, media GET, new photo capture or real sensor use was performed by application code.
- The media fixture is **seven literal marker bytes**, NOT a valid decodable photograph. These tests establish filesystem transaction behavior, not image fidelity.

## Failure and corrective evidence

1. Initial instrumented runtime workflow https://github.com/kphomedocs-hue/Glasses-/actions/runs/38141069637: FAILURE after successful boot and compilation; runner suppressed raw output and no runtime PASS claim was made.
2. Added privacy-bounded reporting of synthetic execution stage and exception class only: source commit 40e1bcb98a7e9da45cae76fedafb3f4cb8b9f6ea, workflow https://github.com/kphomedocs-hue/Glasses-/actions/runs/38141319056. Observed **ExceptionInInitializerError** during FIRST_APP_PRIVATE_IMPORT. No raw device logs published.
3. Android uses ICU-based regular expressions, which reject unescaped literal right braces; the shared Catalog JSON patterns contained unescaped literal closing braces (and a bracket), tolerated by desktop Java but invalid for Android. Corrected patterns in the ORIGINAL shared VirtualLabPolicies.java at commit 7495e9bba9a0bcf0323e859fcd19f10902f251c9. Android emulator test **38141611598 SUCCESS**, and same-commit desktop 96-case/red-team tests, Android source compile and repository hygiene also SUCCESS.
4. The final run verified the shell guard emitted exactly 2/2 runtime session markers; no physical gate was promoted.

## Strict conclusions

**PASS — one-emulator local-storage gate:** Shared Java control/capture logic and durable RecoveryImportStore run on actual virtual Android API36. Controlled local interruption barriers and app-process force-stop/reopen are successful with no remote resumption. This materially strengthens V2 source compilation.

**NOT TESTED OR SOLVED:**
- Actual firmware, Bluetooth GATT, Wi-Fi Direct P2P discovery/association, peer authentication, real catalog content, proprietary short 0x41 meaning or 0x73 payload semantics.
- Real hardware or phone power cut during a write; force-stop happened BETWEEN completed tests, not physically while a file was midwrite. Injected journal barrier exceptions are synthetic.
- Android HTTP socket/MockWebServer, network binding, permission denials during operations, lifecycle background restrictions, near-cap video/audio, actual decoding or durable shared multi-format naming.
- The shipping production APK has NOT been built or validated. The emulator-only source includes fake BLE callbacks and NO network permission.
- **The owner's requirement that original photographs remain on glasses remains unmet**. Previous physical GET+EXIT consumed originals. A local committed receipt does not restore the remote original.

**G6A OPEN, G6B BLOCKED.** Do not interpret the emulator result as authorization to install an experimental app on the owner's phone or access any glasses media. All frozen release archives remain unchanged.

## Next engineering direction
Converge safe policy decisions and actual Android storage into one separately gated production-shared controller. Add a local-only Android HTTP fault adapter exclusively within an emulator test flavor if separately reviewed; then evaluate dual-AVD virtual P2P feasibility. Separately investigate and prove a nonconsumptive original-preserving mechanism before any real-media GET.
