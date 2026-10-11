# G6A Step 3 — independent adversarial static review
Date: 2026-10-11. Reviewer method: separate adversarial inspection of lab design and exact historical sources; no separate human reviewer or device run is claimed.

## Evidence reviewed
`tools/g6a_consumptive_recovery_lab.py`, `tools/test_g6a_consumptive_recovery_lab.py`, `.github/workflows/g6a-consumptive-recovery-lab.yml`, v0.6.8 `FileArchive`, `ImportLedger`, `SingleItemImportCoordinator` and MainActivity startup, physical v0.6.7 GET-associated consumption, and CI run 38125598949.

## Adversarial questions and dispositions
| ID | Finding | Classification | Resolution or hard limit |
|---|---|---|---|
| RT01 | Can process die after potential remote GET begins but before local file is complete? | CRITICAL, unavoidable without hardware-side recovery | Durable intent survives; uncertain state NEEDS_REVIEW; no automatic remote retry. **Remote original loss remains possible**. |
| RT02 | Can it die with all bytes flushed but no certified ready marker? | CAUTIOUS FALSE NEGATIVE | Keep unmarked part and mark for manual review, do not auto-promote. Prevents guessing completeness from JPG delimiters. |
| RT03 | Can it die after ready marker but before final rename? | OFFLINE PASS | Verify exact SHA, size, local JPEG delimiters, and commit locally. Real child process kill test passed. |
| RT04 | Can it die after final rename but before ledger update? | OFFLINE PASS | Verified final+marker permits idempotent DB recovery, no GET. |
| RT05 | Can DB claim success after file corruption/deletion? | OFFLINE PASS in single-file tamper test | Startup verification flags NEEDS_REVIEW; production needs robust reconciliation/storage fault handling. |
| RT06 | Can marker lie about another ID? | OFFLINE PASS | Identity/path mismatch flags review and blocks promotion. |
| RT07 | Can caller submit incomplete JPEG with valid SOI/EOI? | PARTIALLY MITIGATED | Mandatory transfer-complete assertion and exact byte count; **source can still falsely assert success**. Transport-specific proof in Android remains BLOCKED. |
| RT08 | Does SHA-256 establish fidelity to remote original? | NO | It protects received local bytes against later changes; no independent remote original hash exists in tests. |
| RT09 | Does SQLite transaction make file+row a single atomic unit? | NO | Intermediate marker and recovery bridge the gap; crashes at certain unmodeled system faults still require review. |
| RT10 | Does older v0.6.8 preserve partial files? | NO | Calls `removeStaleParts` on startup; report documents this hazard but historical APK remains immutable. |
| RT11 | Does lab guarantee multi-process writer safety? | NO | SQLite unique reservation helps but filesystem operations are not cross-process locked. Android design must enforce single writer and exclusive per-device import transaction. |
| RT12 | Are day numbering and identity safe against remote-ID reuse from a different device? | CONDITION | Caller must generate privacy-preserving **device-scoped opaque identity**; simple SHA of unscoped path may collide semantically across devices. This lab does not implement device discovery. |
| RT13 | Is `os.replace` and directory `fsync` portable to Android? | NO | Host/Linux evidence only. Android FileDescriptor.sync, AtomicFile/SQLite/SAF and filesystem behavior require separate design/test. |
| RT14 | Does current test include physical sudden power loss, storage exhaustion, concurrent OS process kills, DB corruption or metadata restore? | NO | Existing tests cover specified subprocess crash boundaries and data tampering. Full production fault matrix remains open. |
| RT15 | May reviewed model auto-retry because reserve is IN_FLIGHT? | NO, offline guarded | Restart never re-arms in-flight key. Manual review only; retries of potentially consumptive GET prohibited without explicit new protocol evidence. |
| RT16 | Does offline lab establish original-on-glasses preservation? | NO | Recovered local file cannot restore device original; product requirement remains blocker. |
| RT17 | Can model be installed or called as Android code without adaptation? | NO | Isolated Python reference, no Android or media-fetch dependencies; no APK built. |

## Verdict
- **PASS**: scoped offline reference model, 17 passing tests, hard process termination at six boundaries, fail-closed no-retry policy, verifiable local receipt recovery, integrity mismatch handling, and separate workflow.
- **BLOCKED**: production claims, real Android durability, preservation of glasses originals, peer identification, true end-to-end media completeness, exactly-once guaranteed remote fetch, and any new physical/media test.
- **ACTION**: Do not modify older physical release artifacts. Treat any future Android port as a new separately reviewed source candidate, with transport attestation and device-scoped ID as mandatory gates.

**G6A OPEN / G6B BLOCKED.**
