# G6A Step 3 — consumptive-import recovery: implementation and offline test result
**Date:** 2026-10-11 (IST). **Scope:** crash-fault, local storage and receipt design only. No Android/APK, BLE, Wi-Fi Direct, HTTP, glass capture, or real media.

## Technical baseline inspected
Frozen v0.6.8:
- `source/v0.6.8/app/src/main/java/com/parkarsite/g6a/FileArchive.java`: streams bytes to `.part`, fsync, validates JPEG signatures/byte count, writes opaque `.source` sidecar (ID only), renames to final, then appends ledger row.
- `.../ImportLedger.java`: append-only TSV; recovers committed sidecars when a **nonempty** final file exists, without independent full-content SHA-256 verification. A malformed/torn ledger row fails loading, without built-in transactional recovery.
- `.../SingleItemImportCoordinator.java`: dedups against ledger and validates JPEG SOI/EOI but has no persistent reservation before initiating a possibly consumptive remote fetch.
- `source/v0.6.8/app/src/main/java/com/parkarsite/g6apersist68/MainActivity.java` lines 115–119: calls `FileArchive.removeStaleParts` **before** creating/loading ledger.
- `source/g6a-core` and `modules/sync-ledger` contain earlier deterministic naming and transition selftests. Historical physically verified v0.6.8 remains immutable.

**Risk classifications:** (1) No durable intent before one-shot remote request: HIGH; (2) startup deletion of `.part`: HIGH for forensic/recovery evidence; (3) sidecar consisting only of opaque ID plus nonempty final: MEDIUM-HIGH under crash/corruption; (4) unprotected append-only TSV row torn across crash: MEDIUM; (5) no robust proof of full transfer completion based on SOI/EOI alone: HIGH. These are code-level risk findings, **not reports of actual lost owner media**.

## New isolated reference implementation
- `tools/g6a_consumptive_recovery_lab.py` — Python 3 standard library only, **offline synthetic reference model, not Android production code**.
- `tools/test_g6a_consumptive_recovery_lab.py` — deterministic real-child-process hard exits and assertions.
- `.github/workflows/g6a-consumptive-recovery-lab.yml` — restricted transport-free scope and automatic offline regression on changed lab source.

### State machine (per opaque, device-scoped remote identity)
`NEW` -> durable `IN_FLIGHT` reserve -> write `.part` -> fsync -> require transport-completion claim and exact expected byte count -> local JPEG framing / SHA-256 -> durable checked `.ready.json` marker -> atomic final rename + directory fsync -> SQLite `COMMITTED`.

Crash before durable marker: `NEEDS_REVIEW` on recovery; preserve `.part`; **never silently retry** the potentially consumptive remote fetch.
Crash after durable marker: verify staged or final file size, digest and SOI/EOI, finish local rename/DB receipt **without accessing remote device**.
Crash after commit: verify final against ready marker and DB metadata; existing ID does not rearm download.
Tampering/inconsistency: `NEEDS_REVIEW`, no re-download.
Numbering: one SQLite serial reservation per day, unique day+sequence, never reuse a reserved number after failure; do not infer remote order.
Privacy: caller supplies only opaque SHA-256. Device/source scoping of the digest must be implemented in the future Android integration; test fixtures have no raw owner paths/addresses/passwords.

SQLite stores row identity/day/number/state/checksum/size with `synchronous=FULL` and transactional reservation/commit; marker and local file are flushed and day directories synced. These host/Linux semantics are *not automatically transferable* to Android device storage and permissions.

## CI verified evidence
Exact latest offline CI after completion-proof hardening:
- [G6A Consumptive Import Offline Recovery Lab run 38125598949](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38125598949) — **SUCCESS**.
- [Repository Hygiene run 38125598946](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38125598946) — **SUCCESS**.
- **17 unit/integration tests passed** (Python standard library `unittest`), including independent subprocess termination via `os._exit(97)`.

### Crash boundary matrix
| Injected boundary | Expected safe recovery | CI |
|---|---|---|
| After durable intent, before any data | NEEDS_REVIEW, never auto-redownload | PASS |
| During streamed bytes | NEEDS_REVIEW, keep part for review | PASS |
| After a fully fsynced part but before certified marker | NEEDS_REVIEW, not automatically committed | PASS |
| After fsynced ready marker, before final rename | Verify full staged bytes, rename locally and commit | PASS |
| After final rename, before ledger commit | Verify full local bytes and commit ledger | PASS |
| After DB commit | Already committed; idempotent recovery | PASS |

### Additional tests
Normal complete + restart duplicate suppression; modified final-file rejection; modified marker rejection; invalid JPEG; unique daily numbering without reusing a failed reservation; bad identity/day refusal; size-limit rejection; no remote path or secret written to receipt; missing transfer-completion proof rejection; missing expected-length refusal; JPEG-shaped truncated payload rejected even if it has SOI and EOI markers.

## Independent safety conclusion
This **completes Step 3 as an offline reference and testable failure model**, not as a deployed Android integration:
- Local ledger/receipt recovery is demonstrably possible without a second GET **provided a complete validated local file and durable marker survived**.
- If the process stops before the required complete marker, **the model cannot prove the remote media remains available**; it deliberately moves to manual review without a second GET. No software can guarantee restoration of a remote photo that was consumed before a complete local copy was saved unless the firmware supplies a separate recovery/preservation mechanism.
- `transfer_complete=True` is a *synthetic caller assertion*, not validation of a real HTTP status, connection EOF or server content-length. Android transport integration must separately attest its conditions and failure modes before issuing a ready marker.
- Host subprocess exits on Linux test storage are not verified Android power-loss durability guarantees; sudden hardware/power loss and filesystem/journal faults remain future validation needs.
- This model does not validate decoding entire JPEG bitstreams or exact equality to device original, just the received bytes' SHA-256 once locally available.
- Scoped JPG only; MP4/OPUS, real timestamps, background sync, automatic retries and user-facing review UX remain separate gates.
- The remote-originals-untouched requirement remains **unmet** on the previously proven GET-associated-consumption path.

## Next decision
Maintain **G6A OPEN / G6B BLOCKED**. Prioritize identity proof and original-retention policy before any real media transfer. A separately reviewed Android port can reuse this failure model but must not be substituted automatically for the frozen v0.6.8 APK. Zero glasses interaction authorized by this report.
