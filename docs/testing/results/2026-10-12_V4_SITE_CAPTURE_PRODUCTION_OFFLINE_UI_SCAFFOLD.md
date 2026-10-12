# K Site Capture — production-oriented, zero-transfer offline backend/UI milestone
Date: 2026-10-12. **SAFE LOCAL-ONLY PROTOTYPE SOURCE — NOT FINAL AIMB-G1 APP.**

## New code
- `apps/site-capture/src/main/java/com/parkarsite/g1/sitecapture/SiteCaptureOfflineCore.java`: production-facing pure-Java facade for local file/receipt inspection, local-only recovery, uncertain/manual-review counts and hard original-preservation transfer veto.
- `apps/site-capture/src/test/java/com/parkarsite/g1/sitecapture/OfflineCoreSelfTest.java`: host test of restart, corrupt journals, undecidable remote states, and absence of media source or test grant in the facade's public methods.
- `apps/site-capture/android-preview/`: small Android 16/API 36 zero-permission, read-only app *source* with one screen. Reads app-private `getNoBackupFilesDir()` only. Displays verified local media counts and a conspicuous non-destructive-transfer blocker; no scanning, connecting, HTTP, capture, background scheduling or sync buttons.
- Production preview consumes exactly `source/g1-shared-workflow/src` and `apps/site-capture/src/main/java` as shared sourceSets, **no copied/forked core**.

## Verified exact GitHub actions
- Java 17 `Site Capture Offline Production-Facing JVM`, [run 38284068924](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38284068924), exact source commit `8f354166521dd5f7136940ada0e13b26369e56a0`: **SUCCESS**, `V4-SITE-CAPTURE-OFFLINE PASS (16 assertions)`. The source scan found no transfer transport.
- API36 `K Site Capture Read-Only Android Compile`, [run 38284338093](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38284338093), source commit `9ad41690822a778968ed5b05441e250a9a23c75e`: **SUCCESS**. Raw logs: `PASS: read-only app source has no transfer API and zero permissions`; `BUILD SUCCESSFUL`; `PASS: Android source compiles, no media or radio permission, zero installable artifacts`.
- Android merged manifests scanned for any requested permission/feature. Neither APK nor AAB assembled, signed, installed, uploaded or published. This is **not an Android runtime/UI screenshot test** and must not be called one.

## Static external manufacturer documentation check
- AIMB-G1 manual, https://manuals.plus/m/62705ea125d314ab056115558075f5740abde4741bae199d83e660b6f163b93f, describes importing pictures/audio through CyanGlasses app to phone. It does **not say** that original device photos are retained after import, and does not describe a nonconsumptive alternative.
- Google Play `com.aitowe.aitoglasses` Cyan Glasses listing confirms official media sync as an advertised function, https://play.google.com/store/apps/details?id=com.aitowe.aitoglasses. A marketing feature statement is **not preservation proof**.
- Previously verified project physical v0.6.7 one-JPG GET+EXIT removed exact new JPG from remote catalog after a successful download. No full causal instant is proven. Nothing about the external manual negates or explains this. **Do not enable GET**.

## Independent red-team observations
1. `SiteCaptureOfflineCore` public interface offers only local inspection and explicit `canSyncFromGlasses()=false`, `canConnectToGlasses()=false`, `canReadRemoteMedia()=false`; no externally supplied Source, no BLE commands, no sample fixture permit. This supplements but does **not eliminate** the test-only grant still present in shared source elsewhere.
2. Local `recover()` is allowed to promote **already verified locally persisted bytes**. It can never reach remote device media, clear a risky pending journal, or auto-retry a Source.
3. Corrupted/unreadable local journal yields `REVIEW_REQUIRED` and `LOCAL_LEDGER_UNVERIFIED`; no false empty/success classification.
4. UI only displays local status with a hard warning; no interactive download button to misrepresent release readiness.
5. The Android preview source is an isolated project with package `com.parkarsite.g1.readonlypreview`, `minSdk/targetSdk=36`, `allowBackup=false`, `usesCleartextTraffic=false`, zero permissions and compile-only workflow. It is **not** the signed final app, and no physical build is authorized.
6. Remaining unchecked: actual screen rendering on emulator/device; production archive file browsing/export; runtime BLE/P2P/HTTP integration and lawful permissions; real glasses peer proof; vendor JPG/MP4/OPUS compatibility; full release DEX/provenance verification.

## Gate
Production-facing **offline-only backend/UI source compile PASS**. Full G6A/G6B app still **BLOCKED** by remote-original preservation and actual peer identity. Do not claim C1 production acceptance or C3 preservation success.


## Final verification update — Android UI run + compiled class exclusion
The initial compile-only scope above remains valid for run 38284338093, but the project was subsequently hardened and a **separate emulator UI validation was completed**. This addendum supersedes the original “no Android runtime/UI test” limitation for the read-only preview **only**.

- **Final hardening source commit**: `6d15afbfa2b251f32fa02e0dc26609367a4b9ecc`.
- **Final preview API36 compile-only run** [38285067603](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38285067603): SUCCESS. The merged manifest had zero permissions. A postcompile guard found no `SharedTransferCoordinator.class`, `SharedObservationController.class` or `SharedTransferContracts.class` in the preview's Java compilation output. No APK/AAB was generated by this compile-only workflow.
- **Final API36 emulator UI run** [38285067667](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38285067667): SUCCESS. One emulator only, no connected physical devices. The virtual-device temporary APK requested zero permissions. Android `uiautomator` reported visible `Local Archive`, `Read-only development preview`, and `Media transfer disabled`. Exact log: `V4-SITE-CAPTURE-UI PASS`; `V4-SITE-CAPTURE-ISOLATION PASS`. The APK was never uploaded as an artifact or published.
- **Same-source host test** [38285067620](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38285067620): SUCCESS. Repository hygiene [38285067652](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38285067652): SUCCESS.

### Red-team correction
The first attempt to exclude transfer classes from the `AndroidSourceDirectorySet.java` sourceSet via `.exclude(...)` caused a Kotlin DSL Gradle configuration error in compile jobs 38284899717 and 38284932339. Moved exclusions into Gradle `tasks.withType<org.gradle.api.tasks.compile.JavaCompile>().configureEach { exclude(...) }`. Subsequent source-compile, class-absence guard and emulator UI passed at the final SHA above. **Do not claim those failed intermediate runs passed.**

### Boundaries unchanged
This demonstrates a visually rendered, local-only API36 development preview. It is NOT a real-device G6A observer, transfer-capable application or final signed production release. No BLE/P2P/HTTP functionality is included; real media GET remains blocked by original-retention concerns. Existing separately scoped synthetic mixed-media evidence remains valid, but no actual vendor media codec parity was proven.
