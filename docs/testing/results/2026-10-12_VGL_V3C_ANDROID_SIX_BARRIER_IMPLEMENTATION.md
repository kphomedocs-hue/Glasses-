# VGL V3C — six abrupt Android process-stop boundaries (implementation record)

**Final verification: Android runtime PASS. All six independent abrupt-process-stop/recovery tests passed on a single API36 emulator.**

## Exact implementation
- Synthetic-only instrumentation source commit: `aa06eb75188cf42634a9b3ef6953ccc373ef8030`.
- Six-boundary emulator-only runner commit: `94afd36a8d7df72a0ed43fe5f0a1994ba0c05812`.
- Workflow: https://github.com/kphomedocs-hue/Glasses-/actions/runs/38169936791 . This is the definitive run for the six-boundary change. It **completed SUCCESS**. Its job logs contain VGL-EMU-CRASH-01 through 06 PASS and the aggregate VGL_EMULATOR_HARD_STOP 6/6 PASS.
- Source compile: https://github.com/kphomedocs-hue/Glasses-/actions/runs/38169936749 — SUCCESS.
- Pure Java/offline 96 + 7 + 10 tests: https://github.com/kphomedocs-hue/Glasses-/actions/runs/38169936818 — SUCCESS.
- Repository hygiene: https://github.com/kphomedocs-hue/Glasses-/actions/runs/38169936760 — SUCCESS.

## Crash/recovery matrix
| Phase | Abrupt instrumentation JVM termination point | Expected independent restart conclusion |
|---|---|---|
| 01 | AFTER_FETCH_MARK | REMOTE_UNCERTAIN; no source retry |
| 02 | AFTER_VERIFIED_JOURNAL | Recover validated local bytes and receipt; no source retry |
| 03 | Three bytes into .part write | Retain three bytes in quarantine; REMOTE_UNCERTAIN |
| 04 | AFTER_PART_SYNC | REMOTE_UNCERTAIN even when complete raw bytes exist, because verified journal has not been written |
| 05 | AFTER_FINAL_MOVE | Locally rebuild receipt and commit from verified digest; no remote read |
| 06 | AFTER_RECEIPT | Verify existing receipt/final bytes; idempotently finish durable journal |

## Red-team risk boundaries
- Tests use `Runtime.halt(73)` to kill only the simulator instrumentation process. They are not full-phone reboot, actual power loss, disk-full or hardware filesystem fault injection.
- An instrumentation exit code of zero after abrupt death is **not** counted as pass; independent fresh-process recovery must succeed.
- Test app and instrumentation APKs have zero Bluetooth/Wi-Fi/Internet/location permissions, a distinct simulator-only package ID, no actual camera/media or external data, and install only inside a single API36 emulator (ADB virtual-device identity preflight).
- No source code in the real glasses-app frozen release or real transfer code has changed.
- No real device source GET has been authorized; previous JPG GET+EXIT physically consumed the remote original.
- A six-boundary emulator PASS, if obtained, proves only synthetic Android process interruption/recovery for JPG-shaped marker bytes; it does not close G6A or unblock G6B.

**V3C verified: 6/6 abrupt Android process stop and local recovery PASS; normal VGL_EMULATOR 2/2 PASS. Physical gates G6A OPEN; G6B BLOCKED.**

## Verification addendum
Full GitHub workflow 38169936791 SUCCESS with six explicitly separated hard-stop/recovery passes (not an inferred PASS from exit code alone). Android source compile workflow 38169936749 SUCCESS, existing 96+7+10 offline/host workflow 38169936818 SUCCESS, repository hygiene 38169936760 SUCCESS. All tests use synthetic locally generated marker bytes; no remote endpoint is used.
