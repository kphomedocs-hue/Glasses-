# VGL V3B — independent red-team review of Android abrupt-process-recovery lab
Date: 2026-10-11 · Exact audited source commit `6bde5ef0451984e5dd812d0159ecc277bc216aad`.
Evidence: [runtime Android emulator run 38163205078](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38163205078) **SUCCESS**; compile run 38163205071 PASS; 96 offline/HTTP regression run 38163205073 PASS; hygiene 38163205106 PASS.
Full technical result: `docs/testing/results/2026-10-11_VGL_V3B_ANDROID_ABRUPT_PROCESS_RECOVERY_RESULT.md`.

## Reviewed invariants and failure analysis

| Adversarial question | Evidence | Disposition |
|---|---|---|
| Did the older V3 prove *abrupt* process death during a write? | No; prior run force-stopped only between normally completed instrumentation sessions and injected caught exceptions. | **Older scope corrected; V3B extends it.** |
| Is the test running on a physical phone? | Runner checks one ADB emulator serial, `ro.kernel.qemu=1`, API 36 before ephemeral APK installation. | **PASS, guarded.** |
| Can the test APK communicate with glasses or Internet? | Manifests request zero permissions, source preflight rejects Android radio and network APIs; only inline marker bytes are used. | **PASS, no connection path.** |
| Does abort happen after a synced fetch-may-have-started marker? | `RecoveryImportStore.save(started)` precedes the failure injector. On restart `REMOTE_UNCERTAIN`, source callback invoked zero times. | **PASS on emulator.** |
| Are verified bytes recovered without re-GET? | Halt after `VERIFIED` state/fsync; fresh Android process promotes local data and verifies committed receipt. | **PASS on emulator.** |
| Does actual partial write survive JVM termination? | Mid-source lambda writes exactly 3 marker bytes then `Runtime.halt(73)`. New Android process verifies a 3-byte quarantine part, remote uncertain, zero source re-read. | **PASS on emulator.** |
| Could a successful shell exit code falsely certify hard kill? | Early CI failed by rejecting any `INSTRUMENTATION_CODE: 0`. Fixed runner demands absence of a normal status and a *separate* recovery PASS for the expected transaction. | **PASS corrected; do not trust exit code alone.** |
| Does success prove original JPG remains on glasses? | Physical G6A v0.6.7 recorded exact JPG disappearance after GET/EXIT; V3B touches no glasses. | **NO. Still blocked.** |
| Does it prove actual power-loss/flash durability? | JVM termination is not complete device/OS power failure, power-loss reorder, SD storage corruption or flash durability validation. | **NO.** |
| Does a 7-byte marker constitute a valid photo? | It verifies byte length, marker framing and local SHA, not full JPEG decoding. | **NO.** |
| Does code lock out arbitrary peer association? | Core real connect/media flags remain false; test app has no relevant permissions or Android adapters. | **PASS for isolated lab only.** |
| Does source-level completion mean production integration? | Shipping BLE/P2P/HTTP controller, real codec validation, mixed-media naming and Android lifecycle remain open. | **NO.** |

## Explicit gate decision
**ACCEPT V3B as a synthetic, emulator-only Android process-abort/recovery milestone.** Previous false-negative CI runs were preserved, not hidden or reclassified. Tests run on the *same* recovery module imported into the simulator Gradle sourceSets. The old build archives and physical reports remain unchanged.

**REJECT any claim** that real AIMB-G1 media retrieval, true end-to-end exactly-once import, original retention on glasses, radio identity, or G6A/G6B hardware gates have passed.

No new physical test, owner phone APK or real media GET authorized. **G6A OPEN; G6B BLOCKED.**
