# VGL V3B — real API36 emulator process-death recovery
**Date:** 2026-10-11 (IST)
**Latest tested source revision:** `6bde5ef045` (full SHA is resolved from immutable GitHub run below)
**Verified actual Android Emulator run:** https://github.com/kphomedocs-hue/Glasses-/actions/runs/38163205078 — **SUCCESS**.
**Same-source Android compile:** https://github.com/kphomedocs-hue/Glasses-/actions/runs/38163205071 — **SUCCESS**.
**Same-source offline 96 and 7+10 loopback tests:** https://github.com/kphomedocs-hue/Glasses-/actions/runs/38163205073 — **SUCCESS**.
**Same-source hygiene:** https://github.com/kphomedocs-hue/Glasses-/actions/runs/38163205106 — **SUCCESS**.

## Scope
An isolated, separately identified no-permissions Android test APK and instrumentation APK were built **ephemerally on GitHub-hosted Linux**, installed only onto **one QEMU Android 16/API36 emulator** after ADB target and packaged-manifest guards, then uninstalled. No user phone, real AIMB-G1 glasses, Cyan APK, client photographs, BLE device or external Wi-Fi Direct/HTTP peer was accessed. Test media is seven **synthetic JPG-like marker bytes**, not a decodable real image.

The same **actual** `RecoveryImportStore`, shared `VirtualGlassesCore`, `VirtualLabPolicies` and `PeerIdentityPolicy` source is used, with test-only control and injected fault barriers. The test source is `lab/virtual-aimb-g1/android-simulator/app/src/androidTest/java/com/parkarsite/vglandroid/HarnessInstrumentation.java`; strict one-emulator shell safety runner is `lab/virtual-aimb-g1/android-simulator/run_emulator_only.sh`.

## Runtime evidence
Exact GitHub logs:
- `VGL-EMU-01 PASS` — first synthetic app-private receipt/verification, injected exception/recovery and no retry
- `VGL-EMU-02 PASS` — separate process and re-open, force-stop between sessions
- `VGL_EMULATOR 2/2 PASS` — normal instrumentation rounds
- `VGL-EMU-CRASH-01: instrumentation process terminated without normal result`; separate recovery `PASS`: kill by `Runtime.halt(73)` immediately after durable `FETCH_MAY_HAVE_STARTED` marker and **before any Source call**. Fresh process saw `REMOTE_UNCERTAIN`, blocked second Source.
- `VGL-EMU-CRASH-02: instrumentation process terminated without normal result`; separate recovery `PASS`: kill by `Runtime.halt(73)` after verified file SHA/length/JPEG marker and synced `VERIFIED` journal. Fresh process promoted intact local bytes and committed durable receipt with no remote read.
- `VGL-EMU-CRASH-03: instrumentation process terminated without normal result`; separate recovery `PASS`: kill inside the source writer **after exactly three synthetic bytes** of a seven-byte fixture were written, before expected flush/fsync. Fresh process reported `REMOTE_UNCERTAIN`; no final commit; verified exactly three partial bytes are still in local `.quarantine`; second Source attempt blocked.
- `VGL_EMULATOR_HARD_STOP 3/3 PASS` — three independent hard process-death/recovery pairs.

The top-level Android workflow and packaged app/test permission guards are PASS. The offline 96 scenario subset IDs, 7 separate host-local HTTP cases and 10 integrated host-only core→HTTP→store cases remain independently green, but do not become Android network or real-media proof.

## Audit finding and correction (failed CI runs preserved)
Earlier emulator runs (e.g. 38162888909 and 38163032044) failed at the **runner verdict**, even though existing normal V3 instrumentation passed. Android `am instrument` can print `INSTRUMENTATION_CODE: 0` after `Runtime.halt` kills the instrumented process. The first hard-stop checker incorrectly treated that numeric code alone as proof the process was not killed.

Correction at commit `f175dcffe72bf3061daa5df68dc964f724e4435b`: allow absence of a normal result even if the Android command prints code 0; decisively require the **separately executed recovery process** to find the correct persisted marker, quarantine or verified local bytes, and reject any duplicate Source invocation. A normal `status=FAIL`, `status=PASS` during the supposed crash, timeouts and exception-class results are rejected. This is stronger than relying on the command's exit code. The three real hard-stop cases passed in run 38163152914; explicit quarantine-file preservation assertion hardened in source `6bde5ef045` and retested in 38163205078.

## What passed vs what remains
**Verified on virtual Android:** actual journal/fsync/locking behavior through JVM termination and a new process on one Android emulator instance. The after-fetch marker, midstream-partial and after-verified-journal recovery are distinct observed outcomes, not injected exception-only outcomes.

**NOT verified:**
- sudden Android-device power loss or persistence across flash-cache flush failures;
- Android HTTP connection to any endpoint; emulator BLE/Wi-Fi Direct or current physical peer identity;
- production controller integration, actual format-correct decodable JPG/MP4/OPUS, cross-media numbering or real video data;
- no false commit if transport falsely reports success or lies about content length beyond current tested checks;
- long background sync, permission lifecycle, resource exhaustion or OTA/uninstall;
- safe, **nonconsumptive** reading of owner-glasses originals. The known JPG GET+EXIT path REMOVED the original remotely.

The synthetic APK is NOT a candidate for owner-phone installation. It cannot request network or Bluetooth permissions. No actual glasses test authorization is implied.

**G6A remains OPEN; G6B remains BLOCKED.**
