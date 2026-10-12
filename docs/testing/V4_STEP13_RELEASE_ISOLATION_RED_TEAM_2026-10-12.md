# V4 Step 13 — release-isolation and production-readiness red-team review

2026-10-12. **SOURCE-LEVEL AUDIT; NO SIGNED PRODUCTION APK EXISTS.** Do NOT promote this to final shipping release certification or G6A.

## Inspected
- Exact Android simulator Gradle sourceSet `lab/virtual-aimb-g1/android-simulator/app/build.gradle.kts` references shared `source/g1-shared-workflow/src`, not copied code.
- Shared `SharedTransferCoordinator`, `SharedObservationController`, `MixedMediaRecoveryStore`, `RecoveryImportStore`.
- Android test-only `app/src/androidTest/java/com/parkarsite/g1/workflow/SharedCoordinatorInstrumentationProbe.java`, `app/src/androidTest/java/com/parkarsite/vglandroid/MixedMediaInstrumentationProbe.java`, `HarnessInstrumentation.java`.
- Host fixtures in `source/g1-shared-workflow/selftest`, not in Android `main` source tree.
- Manifest `app/src/main/AndroidManifest.xml`: zero requested permissions, `allowBackup=false`, `usesCleartextTraffic=false`; no SDK-triggered device capabilities are claimed.
- Compile-only and emulator GitHub Actions enforce permission checks; emulator runner confirms both ephemeral virtual-device APKs request zero permissions.
- V4 risk-first plan and exact published physical evidence of GET+EXIT remote JPG catalog loss.

## Verified source-level positives
1. The public `SharedTransferCoordinator` constructor has no host fixture permit, returns `false` for real connect and remote media GET; missing synthetic injection blocks Source.
2. The fake import authorization implementation is **not** located in `source/g1-shared-workflow/src` or Android `app/src/main/java`. The simulator build references common pure-Java source but test fixture is in AndroidTest, not main sourceSet. New emulator-workflow preflight explicitly asserts test placement and no corresponding main-source fixture file.
3. New `MixedMediaRecoveryStore` is pure-Java offline local storage, not a network or glass-mutation implementation.
4. Static/host evidence remains segregated from emulator hardware evidence and historical physical releases.
5. No personal photos or vendor/Cyan APK, credentials, device MACs, raw original media paths or owner-capture content are required by new synthetic test fixtures.

## Critical residual issues / build restrictions
- **BLOCK — NOT A PRODUCTION APP:** the existing Android package is named `com.parkarsite.vglandroid.simulator` and has an on-device test harness activity. It is not a shipping K Site Capture interface. Never install it on a user's physical phone or relabel it as production.
- **BLOCK — TEST PERMIT SURFACE:** the shared coordinator still compiles a package-private `HostFixturePermit` pathway. Its present implementation is solely in selftest/AndroidTest. Package-private visibility is **not** cryptographic or build-system authorization; a future production class added to the same package could impersonate a permit. A final build must remove/compile-gate this synthetic path and inspect resulting DEX/source provenance for no fixture implementation.
- **BLOCK — NO LIVE PROTOCOL ADAPTERS:** actual bonded BLE, real selected Wi-Fi Direct group membership, binding to Android `Network`, correlated IPv4, 0x41/0x73 handling, HTTP catalog and one-shot I/O are not connected to the new coordinator.
- **BLOCK — NO REAL PRESERVING TRANSFER:** physically tested JPG GET+EXIT caused remote original catalog disappearance. The current source-retention acceptance requirement forbids real media GET until new trustworthy evidence justifies a preserving alternative or the user explicitly changes requirements.
- **BLOCK — VENDOR FIDELITY UNKNOWN:** Android-generated 32x24 JPG decodes, but vendor EXIF/JPEG orientation and MP4/OPUS codec/metadata parity not verified. Mixed store accepts synthetic container-shapes only.
- **OPEN — CONTROLLER ↔ MIXED STORE:** `MixedMediaRecoveryStore` is an independent durable storage engine, not an integrated production auto-import path. Any future integration must preserve idempotent one-shot intent journal and no silent migration.
- **OPEN — LEGACY MIGRATION:** pending legacy JPG numbering slots and orphan receipts now cause conservative collision prevention; unrelated pre-v0.6.8 legacy schemas are not automatically migrated. Unknown/malformed journal formats block rather than guess; user-visible migration requires a separate bounded plan.
- **OPEN — FINAL APK PROVENANCE:** ephemeral simulator APKs are created and removed on GitHub's virtual device; no signed final candidate, complete production manifest/package or independent production DEX audit has been verified. No build is approved for phone installation.

## Step 13 decision
**Static release boundary / existing simulator isolation reviewed; no known new hardware-permission path introduced. Full S7 release-isolation gate: NOT PROMOTED.**
A signed production candidate and independent DEX/class/package scan cannot be certified before missing production integration and physical-preservation blockers are resolved.
**G6A OPEN; G6B BLOCKED.**
