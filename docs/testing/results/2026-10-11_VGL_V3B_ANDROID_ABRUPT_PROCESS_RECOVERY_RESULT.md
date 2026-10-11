# Virtual Glasses Lab V3B — real Android process-abort recovery
**Date:** 2026-10-11 (Asia/Kolkata) · **Scope:** ONE API 36 emulator, synthetic app-private storage only.
**Verified source commit:** `6bde5ef0451984e5dd812d0159ecc277bc216aad`.
**Verified [API36 emulator CI run 38163205078](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38163205078): SUCCESS.**
Companion same-source workflows: [Android compile 38163205071](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38163205071) SUCCESS; [96 synthetic host cases + HTTP 38163205073](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38163205073) SUCCESS; [repository hygiene 38163205106](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38163205106) SUCCESS.

## What changed beyond V3
Previous V3 tested two normal instrumentation sessions separated by `am force-stop` and injected *caught exceptions* within the import. Those are valuable but not a real process termination *during a source write*.

V3B extends the **existing** `HarnessInstrumentation` with a dedicated `vgl_mode` in the isolated Android test APK and the emulator-only `run_emulator_only.sh` runner. It creates three independent `RecoveryImportStore` transactions with fixed synthetic opaque identities, executes `Runtime.getRuntime().halt(73)` at a particular failure barrier (terminating the instrumentation VM without normal catch/finally), then launches a **new instrumentation process** and validates the persistent state. Existing two normal emulator runs and zero-permission guards remain mandatory.

| Test | Exact emulator trigger | Required fresh-process recovery | Verified |
|---|---|---|---|
| VGL-EMU-CRASH-01 | AFTER_FETCH_MARK: durable `FETCH_MAY_HAVE_STARTED` journal already written but Source not invoked | `REMOTE_UNCERTAIN`; no second Source read | **PASS** |
| VGL-EMU-CRASH-02 | AFTER_VERIFIED_JOURNAL: synthetic 7-byte marker body and SHA locally validated and verified journal persisted, before final move | Local-only file promotion and committed receipt; no Source read | **PASS** |
| VGL-EMU-CRASH-03 | Mid-`Source.streamTo`: 3 bytes of a synthetic marker JPG written into `.quarantine/<opaque>.part`; `Runtime.halt(73)` before normal close or sync | `REMOTE_UNCERTAIN`, no second Source read; `.part` file still exists with **exactly 3 bytes** in app-private quarantine | **PASS** |

CI logs for exact run 38163205078:
- `VGL_EMULATOR 2/2 PASS` — original normal Android storage/restart checks retained.
- `VGL-EMU-CRASH-01 PASS`, `VGL-EMU-CRASH-02 PASS`, `VGL-EMU-CRASH-03 PASS` — each a separate fresh Android instrumentation process.
- `VGL_EMULATOR_HARD_STOP 3/3 PASS`.
- Gate status guard PASS; no release APK upload.

## Investigated and corrected test harness false negative
Earlier hard-stop CI runs (for example 38162888909 and 38163013631) failed because the shell incorrectly asserted that expected abrupt process death must produce a nonzero `INSTRUMENTATION_CODE`. Android's `am instrument` can print `INSTRUMENTATION_CODE: 0` after `Runtime.halt`, even without a normal test result. The corrected runner:
1. Rejects timeout, `status=FAIL`, a normal `status=PASS`, or an error class.
2. Does **not** accept `INSTRUMENTATION_CODE: 0` alone as proof of either success or failure.
3. **Requires** a separate post-crash instrumentation process to successfully recover the exact committed/uncertain transaction with an expected result marker.
4. Fails the workflow on any missing recovery or unexpected second source invocation.

This avoids incorrectly promoting a broken test or silently treating a normal completion as a forced process death.

## Red-team boundaries / what remains unproven
- This is a **single virtual Android 16/API36 device**, not the owner's phone or physical AIMB-G1.
- Only the isolated `lab/virtual-aimb-g1/android-simulator/` app and its `androidTest` package are assembled as **ephemeral debug APKs** on the GitHub runner and installed/uninstalled **on the emulator only**. The app/test manifests request zero permissions; a physical ADB target is refused.
- No Bluetooth, Wi-Fi Direct, Internet, real HTTP media read, camera capture, vendor APK, user photograph, original file or transfer command was used.
- The test fixture is seven synthetic marker bytes, **not a decodable JPEG**. This demonstrates durable journal/file transitions, not content or original-media fidelity.
- A process exit is not a phone power cut, filesystem storage failure, real network drop, disk full, device reboot, or physical firmware behavior.
- A partial local byte stream remains **REMOTE_UNCERTAIN**. The model prevents blind retry; it cannot recreate remote bytes already consumed and not saved.
- The simulation core's real `mediaGetAuthorized()` stays false. The historical real-glasses JPG GET + transfer exit caused original JPG disappearance. The user's originals-retained-on-glasses requirement is still **UNMET**.
- The shipping-path Android BLE/P2P/network controller is not yet integrated or physically verified. No claim of end-to-end exactly-once synchronisation.

## Gate decision
**V3B PASS — actual Android emulator crash-boundary/recovery behavior for three synthetic transaction states.** This improves the quality of software recovery evidence; does NOT change the physical gate.

**G6A OPEN · G6B BLOCKED.**

Next valuable non-physical work: integrate shared policies into an Android shipping-path controller behind disconnected test adapters, then independently review a separate emulator-only HTTP/permission test flavor. Real original-preserving transfer and true current P2P identity still need specific independent evidence and consent.
