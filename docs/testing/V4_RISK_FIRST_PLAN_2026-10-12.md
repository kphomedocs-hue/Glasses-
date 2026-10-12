# V4 REVISED — risk-first roadmap toward final AIMB-G1 app
Status: **PROPOSAL / SOURCE REVIEW ONLY**; 2026-10-12. Supersedes the conversation's uncommitted 12-step V4 HTTP-first outline, **not** any completed CI tests, gate evidence or frozen code. No new APK, media GET, glasses connection or physical test is authorized by this plan.

## Basis actually checked on GitHub
Current main checked: `f1aac05f095f1b847775516c976b8867b228fc20`.
- V3C six Android emulator abrupt process stops PASS: run [38169936791](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38169936791), documented in `docs/testing/results/2026-10-12_VGL_V3C_ANDROID_SIX_BARRIER_IMPLEMENTATION.md`.
- Existing 96 synthetic JVM checks + 7 local HTTP and 10 integrated host tests PASS: run [38169936818](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38169936818).
- `VirtualGlassesCore` has research-only `BleTransportPort` and `PeerDiscoveryPort`. Its `LocalCatalogPort`, `OneShotMediaPort`, `DurableStorePort` are presently empty placeholders. `mediaGetAuthorized()` and `connectAuthorized()` intentionally return false.
- `AndroidHarnessController` verifies synthetic capture-delta and BLE session, then independently executes `RecoveryImportStore` on a hardcoded seven-byte marker: **not a continuous gate-authorized app transfer**.
- Host `HostCoreIntegrationSelfTest.fakeOnlyDownload` deliberately overrides the zero-transfer guard **only inside host test code**; it is not a production authorization path. Android tests do not yet run HTTP.
- `RecoveryImportStore` has strong JPG-only synthetic transaction evidence; mixed JPG/MP4/OPUS storage durability, full decoders and production shared numbering remain to be built and validated.
- Cyan research and G6A v0.6.5–.7 physical evidence associate JPG GET + subsequent exit/reconnect with **loss of exact remote JPG**; no non-consumptive method has been demonstrated. Causality is GET-path-associated, **not proved to happen precisely on the GET operation rather than exit**.
- Current physical Wi-Fi Direct peer identity is UNKNOWN; raw-colon Bluetooth-address suffix is a supported static **candidate classification**, never device authentication.

## What not to do next
1. **Do not repeat** a standalone synthetic HTTP server/catalog reader, 500/redirect failure suite, "+1"/"+2" media delta, 6 Android crash barriers, or a fixed-name peer matcher merely to raise the test count; those have scoped software evidence already.
2. **Do not make Android localhost HTTP the first blocking task.** Enabling `INTERNET` in the current zero-permission simulator would weaken its existing hardware/network isolation while largely duplicating host tests. If true Android socket behavior is later essential, create an *independently audited separate test-only flavor* with tightly controlled localhost endpoint and APK manifest checks, never a universal permission change.
3. **Do not run dual-emulator Wi-Fi Direct solely for coverage statistics.** Its behavior doesn't authenticate actual glasses, and it has extra emulator cost and risk.
4. **Do not declare the physical G6A pass because a local receipt is durable.** Local storage cannot reconstruct a remote original that disappears on transfer.
5. **Do not write a production import flow that bypasses `mediaGetAuthorized()==false` or makes P2P-name-only selection sufficient.**

## Two parallel tracks plus a mandatory convergence gate

### Track H — hardware blockers, initially STATIC ONLY
H1. **Preservation requirement and evidence:** document source-retention as a hard acceptance criterion. Consolidate v0.6.5 no-GET retention, v0.6.7 GET+EXIT disappearance, full static Cyan media/downloader/exit call-site references, and what timing is NOT proven. Output: explicit alternatives/evidence matrix, not speculative firmware commands.
H2. **Non-consumptive candidates:** prioritize only mechanisms found in primary static evidence/vendor documentation; mark `SUPPORTED_BY_SOURCE`, `SPECULATIVE`, `PROVEN_PHYSICAL`. Do not try HEAD, Range, AP mode or new proprietary commands without independent evidence and consent. If no supported mechanism, retain HARD BLOCK on real import.
H3. **Peer identity discriminator:** retain distinct Cyan derived-name / normalized BLE suffix / raw-colon suffix / generic AIMB-G1 / OTHER classifications, separate before/after P2P ENTER time buckets and independently confirm Android location/permission/pre-existing-group evidence. A single peer name is neither authenticated identity nor grounds for automatic connection.
H4. **Physical protocol later, only on separate explicit authorization:** independently audit exact-build passive observer, prove bounded EXIT/group cleanup, stop if target ambiguous, no transfer GET or deletion. A later one-disposable-photo test must have its own consent and cannot be proposed as original-preserving without evidence.

### Track S — software production convergence, safe synthetic research
S1. **Single app-workflow contract**: inventory existing `apps/site-capture/`, `modules/`, legacy v0.4 and current `VirtualGlassesCore`. Adopt ONE shared coordinator and typed capabilities for BLE, peer/network attestations, catalog source, one-shot bytes source, durable store, clock, lifecycle. Preserve existing port safety policies; no arbitrary BLE write or real GET.
S2. **Transfer authority boundary:** use typed context of actual selected P2P device membership, verified group/network binding and correlated IP. Enforce `CANNOT_TRANSFER` until the remote-original preservation gate passes. Synthetic-only authorization must be delivered solely through a DI-injected test adapter, never by an Android-shipping code bypass.
S3. **E2E offline state machine:** same coordinator executes `bonded BLE candidate → observation → peer/group/IP evidence → synthetic catalog snapshots → exact +1 → durable intent → streamed fake bytes → validated durable receipt → EXIT/absence`. Drive host and emulator with replaceable adapters. Property tests: all failure/timeout/peer ambiguity/no original evidence paths block media; no duplicate remote fetch; terminal teardown remains bounded.
S4. **One new Android runtime end-to-end milestone:** keep current zero-permission simulator; use a bounded in-memory or file-backed test byte source *inside the shared coordinator* to prove the state-to-store orchestration with API36, including process halt and local receipt recovery. Do not claim Android socket proof from this.
S5. **Realistic local fixture quality:** replace seven-byte JPEG marker as the only acceptance proof with a generated fully decodable synthetic JPEG; validate actual decode on Android. Add MP4/OPUS finite sample fixtures only after codec/container/metadata validators are scoped; do not fabricate AIMB-G1 format parity.
S6. **Persistent mixed-media numbering:** shared daily sequence across JPG/MP4/OPUS, mid-import restart, timezone midnight, same timestamps, zero overwrite and dedup; test on host and emulator; preserve capture timestamps vs first-seen timestamps separately.
S7. **Red-team release separation:** no test endpoint, synthetic bypass, hardcoded fabricated BLE IDs, raw addresses, owner media or test menus in eventual production artifact; source/build guards, permission allowlists, APK provenance and no unapproved auto-GET.

### Convergence / acceptance — what can actually be promoted
C1. **Software-only candidate gate:** unchanged shared coordinator code passes host unit/HTTP tests, Android runtime orchestration, crash recovery, privacy and mixed-media integrity. Document residual untested Android socket, Bluetooth and P2P adapters; do not call it final G6A.
C2. **Hardware readiness gate:** real peer identity supported by fresh discriminating evidence, legitimate association and cleanup independently tested. Physical GET still blocked by originals-preservation policy.
C3. **Preservation go/no-go:** **No final automatic-sync APK or media transfer** unless strong evidence establishes a safe way to keep original photographs on glasses (or user explicitly revises the requirement). If no safe path is found, report a strictly read-only/catalog-only app as a *different limited scope*, not a silent substitute.
C4. **Final verification:** only after C1–C3 and explicit physical-test authorization, test exact device/media types, phone OS/lifecycle/permissions, filenames and retention. Build full release with signed artifact, app/CI provenance and independent red-team review.

## Suggested work order: 14 small evidence steps, some in parallel
| Microstep | Track | Minimum output | Gate |
|---|---|---|---|
| 01 | S1 | Map shipping-vs-simulator classes and real remaining interfaces | Source audit |
| 02 | H1 | One-page precise GET+EXIT preservation evidence and uncertainty | Static-only |
| 03 | H2 | Evidence-ranked alternative paths, with unsupported ideas explicitly rejected | Static-only |
| 04 | H3 | Passive identity observer acceptance criteria (not an APK) | Source/test spec |
| 05 | S1 | Implement typed shared orchestration interfaces without network/radio | Pure Java |
| 06 | S2 | Introduce default-off transfer authority tied to preservation decision | Red-team |
| 07 | S3 | One integrated JVM synthetic state-to-store scenario | CI |
| 08 | S3 | Wrong peer, +2, no IP, malformed catalog, timeout, repeated GET regressions | CI |
| 09 | S4 | Port same shared coordinator into existing API36 zero-permission simulator | Compile |
| 10 | S4 | Real emulator end-to-end in-memory source and crash barrier recovery | Emulator CI |
| 11 | S5 | Fully decodable generated JPG and shape/metadata assertions | Android runtime |
| 12 | S6 | Durable shared daily mixed-media numbering and migrations | Host+Android CI |
| 13 | S7 | Isolation, permissions, source parity, provenance and independent red team | CI |
| 14 | C1 | Reconcile software coverage and hardware block list; update checkpoint | Documented decision |

**Parallelism:** 02–04 can run concurrently with 01, 05–13; physical H4/C2/C3 remain separately gated. This avoids waiting on unknown glasses firmware for safe app engineering, without confusing synthetic PASS with device PASS.

## Explicit remaining blockers
- Source-retention on real glasses: NOT SOLVED.
- Real current peer identity/association and short 0x41 response semantics: NOT SOLVED.
- Shipping-path Android BLE/P2P/HTTP integration: NOT TESTED.
- Actual file format/capture timestamp parity and multi-media stability: NOT PROVEN.
- V3C synthetic Android crash recovery: VERIFIED in scope, **do not need to retest identical six boundaries until same real coordinator is integrated**.

Current physical gates unchanged: **G6A OPEN; G6B BLOCKED**.
