# VGL V2 — independent safety review, 2026-10-11

Reviewed source: c16b5898fd3d7caf8b2261ddec2c7ed80f5a2c91.
Verdict: **ACCEPT as source-only Android portability and isolated HOST-HTTP research milestone. REJECT any real-Android, shipping-app or physical-gate promotion.**

## Issues corrected
- RECOVERY-ANDROID: Java Files.readString unavailable in Android SDK compilation. Used bounded strict UTF-8 reader; recovery regressions remain green.
- TEST-CI: Standalone host Java HTTP tests initially missed compiling the shared VirtualClock; fixed and rerun fully.
- ANDROID-COMPAT: Test-only Android module minimum SDK aligned to 36 so the claimed runtime API range is not wider than planned.
- SURFACE-ISOLATION: New Android project has separate app ID and manifest with no Internet/Bluetooth/Wi-Fi/location permissions, compiled from original shared source with no APK assembly.
- HTTP-FAULTS: Local-only HTTP GET path rejects redirects, nonlocal targets and partial/invalid media, and actual RecoveryImportStore prevents blind repeated GET after a possible consumptive request.

## Open findings
1. Android Java compilation is not runtime execution. Instrumentation hook compiled but never installed/run. File locking, directory fsync, atomic move, private storage and JDK class availability need an emulator.
2. Local synthetic media is deliberately seven marker bytes, not a valid decodable JPEG. Full JPEG/MP4/OPUS decode and near-cap stress are not proven.
3. Real shipping Android controller is not refactored to call all lab policies; safe bridge is test-only. No Android production flavor exists and no external network adapter is present.
4. Host HTTP runs on GitHub Linux loopback only, not Android. No real BLE, Wi-Fi Direct, authenticated peer membership, 0x73 IP, 0x41 credential field or vendor file layout is proven.
5. Platform permission, background, reboot, storage-full and crash behaviour remains synthetic/injected. Simulator build-enforcement in a real production release is untested.
6. Candidate group verification relies on synthetic trusted booleans; never promotes one matching peer name to authenticated identity.
7. The historical real JPG GET+EXIT removed the original from glasses. The simulated remote-consumed case faithfully leaves local receipt but still violates originals-on-glasses retention. Thus real transfer is blocked.

## Next safe gate
Compile-level Android proof is complete. A **separately authorized simulator-only APK/emulator run**, if approved, would test private-storage semantics without allowing glass communication. Then integrate selected policies into one shipping-path controller and add isolated test-only HTTP flavor. Actual media GET still requires proof of a nonconsumptive original-preserving method and separate physical authorization.

**G6A OPEN; G6B BLOCKED. No APK built or physical action taken.**
