# VGL V3 — Android emulator runtime independent red-team review

**Audited commit:** 7495e9bba9a0bcf0323e859fcd19f10902f251c9
**Run:** https://github.com/kphomedocs-hue/Glasses-/actions/runs/38141611598 — SUCCESS
**Verdict:** Accept **single-emulator synthetic private-storage recovery PASS only**. Reject production/real-radio/original-preservation promotion.

## Safeguards tested
1. **ADB target isolation:** runner checks there is precisely one device, with emulator-prefixed serial and ro.kernel.qemu=1, API 36. A connected physical phone would fail the script.
2. **Package isolation:** separate test-only application ID; no Internet/Bluetooth/Wi-Fi/location permissions, hardware APIs, external media or user files. Both merged/test APK permission sets checked before install.
3. **Ephemeral files:** only runner-local debug emulator packages are installed; uninstall in a cleanup trap, no workflow upload-artifact/release submission, no owner phone APK delivery.
4. **Durable state:** actual RecoveryImportStore invoked within Android app-private no-backup files. VERIFIED journal -> local-only recovery, and FETCH_MAY_HAVE_STARTED -> REMOTE_UNCERTAIN, no automatic repeat Source read.
5. **Restart separation:** two independent Android instrumentation invocations separated by force-stop, preserving private data.
6. **Trace handling:** no raw catalog path, device address, credentials, media or Android logcat surfaced. The initial failure reporting was restricted to error stage + Java exception class.
7. **Cross-runtime bug corrected:** Android ICU rejected unescaped closing right-brace in Catalog regex static initialization; fixed in shared source and reverified on both host JVM and real emulator runtime.

## Residual findings — no claim of resolution
- A reboot, operating-system power loss, kill during an actual byte write, real disk-full and concurrent/process-shared lock semantics remain untested.
- Existing seven-byte synthetic JPEG verifies markers and durable bytes only; no actual decode of JPG/MP4/OPUS and no actual AIMB-G1 file bytes.
- Source policy is compiled into a separate simulator, not yet adopted in a production Android BLE/P2P/HTTP app. Real peer membership/network attestation remains unproven; short 0x41 and passive events remain unclear.
- Wi-Fi Direct and external HTTP operations remain intentionally inaccessible to the emulator app. No physical test or media GET was performed.
- Runtime success doesn't solve consumptive remote GET or preserve the original photograph. The original-preservation product gate is STILL BLOCKED.
- The workflow's rule against ANY requested permissions applies to the simulator test app/test APK; it is not a general design for the eventual real-glasses application that will require properly scoped Android permissions.
- The regex change only demonstrates shared syntax portability, not that synthetic configFileType 2 JSON corresponds to real vendor firmware data.

## Decision
**V3 Android emulator synthetic private-storage gate: PASS.**
**G6A physical: OPEN. G6B: BLOCKED.**
No physical APK, no bulk import, no real photos, no Wi-Fi Direct connect and no remote GET are authorized by these test findings. Future steps should integrate an emulator-only local HTTP adapter and shipping-path decisions while retaining the preservation safety gate.
