# G6A Step 3 — offline consumptive-transfer recovery prototype and adversarial audit
Date: 2026-10-11. **Scope: static + offline execution ONLY**. No APK, glasses activity or media GET.

## Source and proof of reproducibility
New isolated module: `source/g6a-recovery-core/`. Frozen `source/v0.6.8/` and historical build artifacts are unchanged.
- `src/com/parkarsite/g6a/recovery/RecoveryImportStore.java`: source Git blob `345953c518812157e2fe8a1cae918cf7b2573fb6`.
- `selftest/com/parkarsite/g6a/recovery/RecoveryImportSelfTest.java`: tested source Git blob `66f5ecdbc5894e50aa86f154139c01134624ff44`.
- `run_selftest.sh`: Java 17 standalone runner.
- CI: [G6A Offline Recovery Tests, run 38125976170](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38125976170) — **SUCCESS**; dependency guard and restart/fault-injection steps PASS.
- Repository Hygiene, run `38125976168` — **SUCCESS** on the same source commit.
- Local `javac` and `java` selftest — **PASS: 85 assertions**. Local Git object SHA comparison matched the exact two committed Java sources.

## What the model demonstrably enforces in its own isolated code
1. A restricted opaque SHA-256 remote identity is stored, never the remote path or password. File naming is locally deterministic using date folder + reserved sequential JPG names; incomplete reservations consume their slot rather than creating collisions.
2. An atomic+synced `FETCH_MAY_HAVE_STARTED` journal update precedes any call to the caller-supplied source writer. On exception/hard-stop before `VERIFIED`, restart classifies `REMOTE_UNCERTAIN`, does not discard a partial file, and never invokes remote source automatically.
3. Requires an externally supplied positive exact expected byte length <= 32 MiB. Bounds writer output, checks length equality, JPEG SOI/EOI, and SHA-256 of the local file. Only then writes durable `VERIFIED` metadata.
4. On later local-only recovery, a verified partial or already-moved final file is integrity-checked and promoted to a stable final path plus separate receipt; no remote byte source is called again. Invalid/missing/ambiguous local files are not silently promoted.
5. On a `COMMITTED` transaction, the receipt and the final bytes must match the journal; duplicate writes for the same identity are rejected. Tampered receipt fails closed.
6. Uses a single-writer OS lock, file sync, atomic metadata replacement, atomic final-file move, and directory sync attempts. Unsupported operations may throw and leave a reviewable state; no fallback claims of guaranteed durability.
7. Has NO imports or runtime calls for Android, Bluetooth GATT, Wi-Fi Direct, network URLs, sockets or glasses control.

## Test coverage (synthetic media only)
- Normal successful import and restart: valid file + receipt, no duplicate invocation.
- Reservation crash before network: safe `NOT_FETCHED` state.
- Invalid/missing expected length: no source call.
- Six injected exception barriers: after `FETCH_MAY_HAVE_STARTED`, after part fsync, after `VERIFIED`, after file rename, after receipt, after COMMITTED journal.
- Six **separate JVMs forcibly halted** using `Runtime.halt(37)` at those same barriers; a new recovery instance then validated expected `REMOTE_UNCERTAIN` or local-only recovery/receipt state.
- Truncated media, malformed JPEG, midstream source failure, corruption after local VERIFIED, tampered receipt, part-file quarantine across restart, unique numbering, and no raw remote identity in metadata.
- **Result: 85 assertions passed** locally and in CI. No user photograph and no hardware touched.

## Red-team challenges — residual limits and explicit fails
| Question | Result | Impact |
|---|---|---|
| Does a hard-JVM-stop test prove true on-device sudden power loss? | **NO** | Filesystem, flash caching, Android process/lifecycle and power-cut behavior untested |
| Can the module recover remote media not completely verified before a one-time GET consumes it? | **NO** | Correct behavior is `REMOTE_UNCERTAIN` and manual assessment, not a guaranteed recovered original |
| Does `FETCH_MAY_HAVE_STARTED` guarantee a future Android caller cannot open a network request before the marker? | **NO** | Integration must enforce that ordering and use one-shot, no-retry byte source |
| Does JPEG SOI/EOI + length + SHA-256 prove complete visual fidelity? | **NO** | SHA proves local stability after check, not identity against authoritative remote checksum; full decoder test later |
| Is remote identity stable across firmware changes or replacement? | **NOT PROVEN** | Opaque SHA of remote path may need device-context namespace and firmware-specific handling |
| Do simulated crashes establish prevention of a second GET from other app components? | **NO** | Retry policy must be enforced by transport/session code not included here |
| Does a temporary/verified file guarantee phone and glasses originals both survive? | **NO** | Glasses-side remote consumption confirmed previously; not solved here |
| Are concurrent independent writers outside the cooperative file lock impossible? | **NO** | Prototype assumes application-private, cooperative single-writer storage |
| Does the prototype support videos, audio, metadata or daily shared cross-media counter? | **NO** | JPG-only prototype; G6B remains blocked |
| Is failure atomicity perfect if storage corruption occurs mid-rename? | **NO** | Code fails closed where detected; future filesystem/power-loss validation required |
| Is app production recovery completed? | **NO** | No Android integration, real transport, format-specific verifier, installation or device trial |

## Decision
**PASS** for transport-free reference prototype and tested fail-closed local recovery transitions. This resolves the design question of whether *locally verified bytes can be recovered without a second remote GET*. **FAIL** for any claim of full end-to-end exactly-once media import under all hardware failures.

Important operational constraint: the older frozen v0.6.8 code deletes stale partial files during recovery; it must NOT be silently reused for a consumptive one-time GET without the new reviewed policies. This prototype is additive and not integrated.

Next work requiring separate approval: design a safe, authenticated live P2P peer identity test and resolve the user's original-on-glasses retention requirement before any real media GET. Until then **no APK build or physical test is authorized**.

G6A OPEN; G6B BLOCKED.
