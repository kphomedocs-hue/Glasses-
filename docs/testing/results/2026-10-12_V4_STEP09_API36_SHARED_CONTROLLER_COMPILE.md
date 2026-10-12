# V4 Step 09 — same shared controller in API36 isolated Android source compile

Date: 2026-10-12. **SOURCE COMPILE ONLY**, not Android runtime import or real glasses test.

## Exact source / CI provenance
- Final source configuration commit: `ff7d3ebc71f3df89a80d6aa881328fe68e1f74de`.
- Android 16/API36 source compile GitHub Actions [38278073484](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38278073484) **SUCCESS** for that exact SHA.
- Job `compile-only`: **Static isolation preflight SUCCESS; compile Android app/AndroidTest Java SUCCESS; postcompile merged-manifest/no APK checks SUCCESS**.
- Raw job log includes `PASS: same production-oriented shared coordinator source, no fixture implementation`, `BUILD SUCCESSFUL`, `PASS: compiled-only, no APK, no permission and no emulator`.
- Android app `lab/virtual-aimb-g1/android-simulator/app/build.gradle.kts` adds an exact sibling `source/g1-shared-workflow/src` sourceSet. It **does not copy or fork** the Java coordinator source.
- CI workflow `.github/workflows/vgl-android-source-compile.yml` watches `source/g1-shared-workflow/src/**`, statically scans the shared source for hardware/network dependencies, and checks Android simulator merged permissions.

## Red-team correction and superseded failure
- Initial CI run [38277893720](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38277893720) FAILED **at the new static preflight**, before Android compilation, because a source-file path used four parent-directory ascents from `android-simulator/`, whereas the workflow's working directory requires three. The four-parent relative path is correct **only from `android-simulator/app/`** inside Gradle.
- Corrected only the workflow preflight path, from `../../../../source/...` to `../../../source/...`, in `ff7d3ebc71f3df89a80d6aa881328fe68e1f74de`. Final compile-only run passed.
- Do not report the initial failed run as compiler incompatibility or discard its evidence.

## Boundaries and explicit NON-claims
- This compilation does not invoke `SharedTransferCoordinator` at Android runtime; the existing `AndroidHarnessController` remains its own synthetic path.
- Host-only `HostSharedWorkflowSelfTest` and the package-private fixture permit implementation were **NOT** introduced to Android main sources; no media fixture was read on an Android runtime in this milestone.
- No Internet, Bluetooth, Wi-Fi Direct, location or media permission was added. No APK/AAB produced, installed or archived, no emulator booted, no phone/glasses contacted and no media GET attempted.
- Android API36 compiling Java interfaces does not verify Android lifecycle/timer semantics or local crash recovery through this new same coordinator.
- Real current peer identity, preservation of remote originals after media GET+EXIT, media decoding/fidelity and mixed JPG/MP4/OPUS daily numbering are unresolved.

## Status
**V4 Step 09: PASS — exact shared source compiles in zero-permission API36 simulator.**
Next step (10) requires a distinct **emulator runtime integration**, with a test-only injected fake source and unchanged production-default-deny behavior; not covered by this compile-only result.
**G6A OPEN; G6B BLOCKED. No physical-test APK or real media transfer authorized.**
