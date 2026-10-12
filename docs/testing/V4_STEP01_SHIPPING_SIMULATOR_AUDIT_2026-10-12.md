# V4 Step 01 — shipping versus simulator integration audit
Date: 2026-10-12. Classification: **STATIC SOURCE AUDIT / NO IMPLEMENTATION OR HARDWARE CLAIM**.
Authority: `docs/testing/V4_RISK_FIRST_PLAN_2026-10-12.md` on main. This file does not promote G6A.

## Examined source
- `lab/virtual-aimb-g1/src/com/parkarsite/g1/virtual/VirtualGlassesCore.java`
- `lab/virtual-aimb-g1/android-simulator/app/src/main/java/com/parkarsite/vglandroid/AndroidHarnessController.java`
- `source/g6a-recovery-core/src/com/parkarsite/g6a/recovery/RecoveryImportStore.java`
- `lab/virtual-aimb-g1/android-simulator/README.md`
- V4 plan, current next-chat handover and latest checkpoint.

## Source-of-truth boundary map
| Component | What it actually does | Production integration missing |
|---|---|---|
| Frozen `source/v*` and historical Android diagnostics | Previously exercised bounded BLE/P2P and selected media paths physically | No adopted, unified production Android coordinator; historical classes must remain immutable |
| `VirtualGlassesCore` | Transport-free BLE/observation lifecycle; peer names classified as candidates; default `connectAuthorized()==false`, `mediaGetAuthorized()==false` | Actual authenticated BLE, selected P2P peer/group, bound network, correlated IP, typed catalog and media/store operations |
| `AndroidHarnessController` | Fake BLE events and synthetic peer snapshot, then **separate** seven-byte JPG-marker local-store transaction | One common orchestrator must authorize each transition, including a production-default-deny media operation |
| `RecoveryImportStore` | Single-writer JPG-only journal, quarantine, fsync, atomic rename, opaque receipt, local-only recovery | Type-safe mixed-media store and calendar numbering; no GET transport or fidelity decoding built in |
| `VirtualLabPolicies` and host HTTP tests | Synthetic safety rules and localhost-only HTTP faults; isolated test-only bypass | Prohibit bypass in production; no claim of Android HTTP, camera data or physical identity fidelity |
| API36 emulator | Zero-permission synthetic app-private recovery and six hard-stop barriers | Same future production-oriented coordinator must run through isolated Android adapters; no actual radios or network proven |

## Interfaces for Step 05, before Android I/O
1. **Clock/deadlines**: monotonic event deadline, cancellation and terminal-state isolation; distinguish wall-clock date for filename allocation.
2. **Bonded BLE candidate**: bounded connect, subscription, inventory/ENTER/EXIT write results and parsed event evidence; do not infer a credential from short `0x41`.
3. **Passive peer observer**: timestamped pre/post ENTER snapshots and classified candidates, never implicit connection authority.
4. **Network attestation**: selected real P2P device is group member, group identity, Android network binding and correlated glasses IP; injected booleans only in tests.
5. **Catalog source**: bounded bytes, safe path parsing, immutable baseline/current snapshots and exact single-media delta.
6. **Preservation evidence / transfer authority**: explicit production-default-deny decision independent of peer name; do not enable media GET until originals-on-glasses is evidenced.
7. **One-shot byte source**: at most one request, no retry/resume/Range/redirect, bounded length and opaque identity.
8. **Durable store**: prepare/mark-fetch-before-read/quarantine/checksum/validate/fsync/atomic final/receipt/recover without source callback.
9. **Lifecycle and teardown**: terminal EXIT evidence and confirmed P2P group absence; explicit errors for timeout/uncertainty.
10. **Privacy and observability**: store only opaque identifiers and sanitized classifications, never real MAC, SSID/password or remote raw filenames.

## Red-team findings
- **CRITICAL / OPEN:** a fake lifecycle can reach `SAFE_FINISH` without a real peer identity or verified physical media path. It must never be treated as an authorization token.
- **CRITICAL / OPEN:** `AndroidHarnessController` executes `RecoveryImportStore.execute` independently of `VirtualGlassesCore`; future production design MUST place its transfer authority before invoking a Source.
- **CRITICAL / OPEN:** `VirtualGlassesCore.catalogEligible()` is only a simulated group/IP predicate and is not authority to fetch media.
- **HIGH / OPEN:** `RecoveryImportStore.prepare` emits JPG-only date-based paths; typed JPG/MP4/OPUS common sequence and rollover unimplemented.
- **HIGH / OPEN:** marker-only JPEG validation does not prove Android bitmap decoding or actual glasses data fidelity.
- **HIGH / OPEN:** previous media GET plus EXIT is associated with loss of source photograph, which violates the project's no-loss goal.
- **HIGH / OPEN:** no current authenticated Wi-Fi Direct peer; raw-colon suffix matching alone cannot authenticate.

## Next permitted implementation
Implement Step 05 as a new **pure Java interface/contract module**, no changes to frozen diagnostics; preserve default-off media authority, add independently runnable host contract tests; only later integrate into the existing zero-permission API36 simulator. A separately approved hardware protocol experiment is still required for physical verification.

## Gates
V4-01: **AUDIT DOCUMENTED**, NOT code integration implemented or CI tested by this document.
G6A: OPEN. G6B: BLOCKED. No APK or real GET authorized.
