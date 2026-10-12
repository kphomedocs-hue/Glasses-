# V4 Step 05 / early Step 06 — fail-closed Java controller red-team record

Date: 2026-10-12. Scope: **pure Java host source only**. No physical glasses, Android shipping adapter, P2P discovery, network, media GET, APK build, or emulator run.

## Tested exact source
- Shared contract and single observation controller: `source/g1-shared-workflow/src/com/parkarsite/g1/workflow/`.
- Host tests: `source/g1-shared-workflow/selftest/com/parkarsite/g1/workflow/`.
- GitHub Actions Java 17: [38276020372](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38276020372) **SUCCESS**, exact head `454e156713e39a270ef326282dfb0afc8b48972e`.
- Raw CI job log: `V4-05-PURE-CONTRACT PASS`; `V4-OBSERVATION-FAIL-CLOSED PASS (57 assertions)`.
- Repository hygiene: [38276020414](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38276020414) **SUCCESS** for the same SHA.
- Counts represent test assertions and include checks made by the fake deadline scheduler; **not 57 distinct hardware experiments**.

## Production safety boundary
The coordinator explicitly returns `false` from `physicalConnectAuthorized()` and `remoteMediaGetAuthorized()`. `considerMediaImport()` returns a denial even if a synthetic caller supplies the enum `PHYSICALLY_PROVEN_SAFE`; it never calls `OneShotByteSource` or `DurableStore`. No real IO adapter is implemented. A classified peer remains a candidate, **not authenticated identity**.

## Red-team issues fixed before final CI
1. **Independent timer handles:** an initial design used global string timer keys, which could cancel another controller's deadline when one shared scheduler served multiple runs. Revised `DeadlineScheduler.arm` to return a per-arm `Cancellation`; two-instance isolation test PASS.
2. **Missing teardown on scheduler/BLE exception:** corrected `requestExitSafely` to attempt BLE EXIT and platform teardown independently, even if timer scheduling or one cleanup operation throws. Injected scheduler/EXIT/teardown failures PASS.
3. **Inline timeout startup race:** if a deadline adapter synchronously invokes its callback during `arm`, naive startup could continue performing BLE observation work after timeout. Added stage rechecks immediately after arming and after begin observation. Inline callback test PASS with zero subsequent BLE start/inventory operations.

## New fail-closed regressions (not repetitions of the 96 offline subsets)
- Synthetic purported fully-attested peer plus fabricated preservation approval still cannot invoke Source or Store.
- Multiple peer candidates remain observational only; no authenticated connection/GET.
- Only distinct BLE exit-write, protocol post-exit inventory event and group-absence evidence may mark observation safely closed.
- Observation timeout initiates cleanup but never ends in success, even when cleanup is verified.
- Exit deadline and late callbacks are isolated; no terminal state resurrection.
- Exit is requested at most once per run; repeated completion events have no effect.
- Timer creation, BLE EXIT and platform teardown exceptions are fail-closed.
- A separate pure Java guard forbids Android/network/media-source execution references in shared controller source.

## Residual issues / do not promote
- Controller uses a synthetic `BleSession.beginObservation()` method; it has **no authorized live BLE adapter** and must not be interpreted as a physical passive observer.
- In-memory `PeerEvidence` flags and `Lifecycle.groupAbsent()` are test adapter outputs, not independent Android device/group/IP proof.
- A caller-supplied post-EXIT boolean is not actual validated `0x73/0x01` proof.
- Java timer contract cannot assure real OS scheduling or cleanup completion without audited Android implementations.
- Catalog and media interfaces remain **contract only**; no single source-to-store production path, no physically safe original-preserving transfer, no JPG/MP4/OPUS format verification.
- This neither closes G6A nor unblocks G6B. Source-retention blocker remains absolute.

## Next V4 increment
Retain read-only/test-only defaults. Add a typed, explicit correlation of real selected peer/group/network evidence to passive observer snapshots only in a separately audited adapter. Independently add one **synthetic** source-to-store orchestration path with test-only injection that cannot be packaged into production. Reuse existing host HTTP and Android crash tests rather than rerunning them for volume. No real GET until original preservation is established and physical test consent is separately granted.

**Disposition:** Step 05 pure Java controller slice VERIFIED in host CI; overall production integration still OPEN. Early Step 06 default-deny guard VERIFIED in source/host scope. G6A OPEN; G6B BLOCKED.
