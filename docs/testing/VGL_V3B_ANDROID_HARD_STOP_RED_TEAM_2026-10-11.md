# VGL V3B — emulator crash/recovery red-team review
**Date:** 2026-10-11
**Verified release-independent run:** https://github.com/kphomedocs-hue/Glasses-/actions/runs/38163205078 — SUCCESS on exact `6bde5ef045` source.
**Subject:** VGL API36 emulator-only storage instrumentation; **not** a production APK, hardware gate or real glasses test.

## Independent review challenges / findings
| Risk | Finding / tested mitigation | Status |
|---|---|---|
| H1 fake failure masquerades as process kill | Actual `Runtime.getRuntime().halt(73)` exits instrumentation JVM; no caught exception/finally. Fresh instrumentation process separately inspects durable journal. Do NOT use Android `INSTRUMENTATION_CODE: 0` as proof of successful normal completion. | HOST+EMULATOR TEST PASS |
| H2 crash at possible fetch start could trigger retry | After `AFTER_FETCH_MARK` persisted, `recover` gives `REMOTE_UNCERTAIN`, and a later `execute` refuses to invoke Source | PASS for simulator |
| H3 bytes truncated during crash still look complete | Actual kill after writing exactly 3/7 marker bytes; restart verifies `.quarantine` part has 3 bytes and no verified/committed receipt; no retry | PASS for simulator |
| H4 process dies after media verified but before final move | A new process validates staged bytes and promotes receipt using only local files; source never invoked again | PASS for simulator |
| H5 shell exit status incorrectly means process survived | Historical failed CI 38162888909 showed `INSTRUMENTATION_CODE: 0` despite abrupt end. Updated runner demands valid follow-on state, not misleading numeric result. | CORRECTED, PASS |
| H6 instrumentation runs on actual user's phone | Runner requires exactly one ADB emulator serial, qemu prop=1, SDK=36. Builds and installs test APKs only inside GitHub-hosted emulator; cleanup uninstalls. | PASS source and CI guard; not a general OS-enforced distribution policy |
| H7 real device or external network accessed | App/test manifests have zero declared permissions; main Android source lacks BLE/network adapter, synthetic fixture only; no sockets/HTTP/real media. | PASS |
| H8 secrets leak via failure logs | Runner prints only synthetic stage/error-class on failure, not raw logcat, credentials, file paths, device addresses or media. | PASS inspected output; continue hygiene |
| H9 Android filesystem crash equals sudden power cut | No. Test kills a JVM process; emulator kernel/filesystem caches stay alive. | OPEN |
| H10 a correct local receipt means glasses original persists | No. Proven v0.6.7 JPG GET+EXIT removed the remote image. | HARD PRODUCT BLOCK |
| H11 a verified peer-name match proves G6A | No. No current P2P association or current device identification is part of the emulator suite. | OPEN |
| H12 test-only source can be shipped unintentionally | Separate Android simulator application ID, no permission manifest, instrumentation and special modes only in `app/src/androidTest/`, existing shipping source untouched. A separate future production exclusion audit remains necessary. | PASS for current test layout, OPEN for eventual shipping app |

## Conclusions
- **ACCEPT V3B emulator local-recovery gate:** three distinct real Android JVM termination barriers PASS with independent recovery; partial file retained for manual assessment and no automatic re-fetch.
- **REJECT claims of hardware power-loss, live Bluetooth/Direct/IP, actual file authenticity, nonconsumptive transfer or G6A physical pass.**
- Accepting instrumentation exit code 0 by itself would be unsafe; full evidence includes correct separate-process journal/byte-state validation.
- Do not automatically integrate these simulated results with the original v0.6.8 hardware diagnostic source; preserve all prior immutable release archives.

## Follow-up requirements before final APK
1. Design shared production controller with test-only transport dependency injection, avoiding forked state machines.
2. Validate real Android local HTTP behavior only in a dedicated, separately approved simulator flavor, with strict synthetic localhost.
3. Test Android storage-full/lifecycle/permission changes and full decodable synthetic JPG/MP4/OPUS.
4. Determine actual current glasses identity and original-preserving media transfer mechanism through independent, separately approved hardware evidence; do not touch client/baseline photos.

**G6A OPEN, G6B BLOCKED. No physical testing, phone APK or actual media GET authorized.**
