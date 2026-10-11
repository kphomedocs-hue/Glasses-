# K G1 Virtual Glasses Lab — test many final-APK requirements before device runs
**Research and proposed architecture: 2026-10-11 (IST)**. This file is a PLAN, not implementation, measured pass rate, tested emulator setup, physical-test authorization or production gate.

## Problem and current evidence
G0–G5.7 have historical scoped physical passes. G6A remains OPEN; G6B BLOCKED.
- The original Cyan Bluetooth callback constructs a candidate Wi-Fi Direct peer name using device name/address, and includes an alternate raw-address suffix comparator. Current v0.6.8.6 only saw an anonymous OTHER peer; no association took place. Neither unique name nor count authenticates a device.
- Current tested JPG media GET + exit/reconnect is associated with remote disappearance of exactly that JPG, although K sent no explicit delete.
- The new `source/g6a-recovery-core/` offline tests (85 synthetic/process-stop assertions) demonstrate local-only recovery of **already verified** bytes without a second source GET. They do not solve bytes missing after one-time fetch.
- `source/g6a-identity-core/` offline tests (77 assertions including actual frozen v0.6.8.5/.6 matcher files) identify a raw-address-colon suffix blind spot but cannot identify the real current peer.

**Core idea: build ONE testable product state machine and give it three mutually exclusive transport adapters**, rather than creating another throwaway APK for every gate.

## New verified 2026 emulator capability
Google Android Emulator **36.5+** interconnects multiple Android Virtual Devices over shared simulated Wi-Fi and supports testing Wi-Fi Direct/NSD peer connectivity. Official product note dated 2026-04-13 explicitly mentions companion phone/AI-glasses scenarios:
- https://developer.android.com/blog/posts/test-multi-device-interactions-with-the-android-emulator
- https://developer.android.com/studio/run/emulator-networking-interconnect
- https://developer.android.com/studio/run/emulator-networking

The emulator documentation also lists Bluetooth profiles for newer system images, but **do not presume full AIMB-G1 GATT compatibility between two emulators**. First emulate BLE callbacks at an explicit Java port; conduct separate proof-of-feasibility for a virtual BLE peripheral before claiming hardware radio parity.

Android Emulator can simulate BLE signal-strength variations using `-netsim-args="--rssi=ble:-65"` in supported versions; its relevance to the proprietary AIMB-G1 BLE GATT path must be independently checked:
https://developer.android.com/studio/run/emulator-networking-advanced

GitHub standard hosted Linux runners for **public repositories** have 4 CPUs, 16 GB RAM and 14 GB SSD; Linux Android emulator acceleration is supported:
https://docs.github.com/en/actions/reference/runners/github-hosted-runners
A **single** AVD plus local simulated server is the low-resource first option; **two** full AVDs may exceed practical disk/RAM/time headroom and must pass a feasibility CI before adoption. The user's repository is currently public; **never upload original Cyan APKs, device addresses, passwords, real site/client media or unsanitized traces**.
https://github.com/ReactiveCircus/android-emulator-runner

## Three-lane test lab

### Lane A — deterministic pure-Java replay (highest initial ROI)
One deterministic application state machine (clock and event queue injected), behind explicit interfaces:
- `BleTransportPort` for GATT connection/subscription/write callback/event input.
- `PeerDiscoveryPort` for Wi-Fi P2P peer snapshots/group state.
- `LocalCatalogPort` for HTTP catalog and local-file simulated responses.
- `OneShotMediaPort` for bounded synthetic GET and potentially consumptive-server outcomes.
- `DurableStorePort` for transaction, receipt, directory and commit, and `ClockPort` for deadlines.
All ports can be either reference fake ports in tests or Android adapters in later production, with the **same business state machine**. No real Bluetooth/HTTP in this lane.
A synthetic `VirtualAimbG1` emits known/proposed frames with explicit origin labels: VERIFIED_HISTORICAL vs SYNTHETIC_FAULT vs UNKNOWN. No unsupported `0x41` byte is silently given a fabricated protocol meaning.
Use model-based scenario scripts + deterministic seeds; assert every command, deadline, transition, chosen media, absence of retries, cleanup, and no inadvertent GET. Save only scripted fixture IDs, counts and pass/fail.

### Lane B — Android-in-loop networking and local HTTP (feasibility gated)
Option B1 (lightweight): **one Android emulator** runs a simulator-flavor of K G1 against a loopback or host MockWebServer. Use real Android lifecycle, permissions, storage, foreground/background UI, simulated slow responses, disconnect after response headers/body, HTTP 500/timeouts, malicious path, oversized stream, media type validation.
Tool: Square OkHttp MockWebServer (open source). It supports recorded requests, scripted responses and slow/throttled network bodies.
https://square.github.io/okhttp/features/mockwebserver/ (check artifact/API version during implementation)

Option B2 (higher fidelity, only after B1 passes): **two Android emulator instances, v36.5+**:
- virtual phone: K G1 simulator build;
- virtual glasses: a small dedicated virtual peer app hosting a **fake** catalog and synthetic JPG/MP4/OPUS assets, with optional one-time-consume-on-GET behavior;
- exercise Android P2P discovery/association/group membership/IP/network cleanup separately from BLE callback simulation;
- run an additional synthetic third peer to ensure wrong-device selection fails closed.
This tests Android virtual P2P behavior, **not** whether AIMB-G1 advertises the same name or accepts current ENTER. Verify emulator image/API support before implementing virtual-peripheral claims.

### Lane C — one minimal real-glasses evidence session (only after approval)
Only unresolved hardware-dependent findings are tested, using explicit stop points:
1. Confirm target BLE bonded identity and service, preexisting-group absence, exact approved APK/hash, permission preflight.
2. A deliberately new *research-only* observer could independently count exact candidate vs normalized suffix vs raw-colon suffix vs generic family vs OTHER, with before/after ENTER timing and no names/addresses in public reports; this must not connect to an unverified peer.
3. If target remains unverified, EXIT + verified group absence and STOP (no connect or GET).
4. If a separately justified trust boundary is met, another approved phase may test association, target membership and `0x73/08`, and **read-only** catalog retrieval without media GET; no blanket approval implied by this plan.
5. **Do not media GET while the owner's requirement is to keep originals on glasses**: the observed tested path is consumptive. A disposable test photo requires a separate specific consent and red-team review; do not touch baseline/client media.
Each physical question has an independent conclusion; reaching step 1 does not mark steps 2–5 passed.

## 12 scenario packs × 8 targeted fixtures = 96 PLANNED software test cases
Use `docs/testing/G6A_VIRTUAL_GLASSES_SCENARIO_MATRIX_2026-10-11.md` as the planned catalog. These are **NOT 96 tests already implemented or passing**. Run in stages: source-only deterministic first, then Android single-device, then dual-AVD if feasible. A case is PASS only after automated assertions execute on the same code path intended for production.

Covers BLE/0x41/0x73, P2P identity + hostile/duplicate peers, callback races and cleanup, catalog config1/config2, delayed capture snapshots and ambiguous deltas, HTTP response faults, JPG/MP4/OPUS synthetic integrity, consumptive GET, persistent receipt/recovery, local daily numbering, Android permission/lifecycle, and redaction/release controls.

## Critical engineering rule: same code, different adapters
A simulator that tests a completely different control flow from the final APK provides false confidence. Put protocol, timeouts, peer-eligibility, media selection, storage state and privacy decisions in the shared core. Keep test-only adapters, fixture endpoint configuration, fake media and simulation screens in a **separate simulation application ID/build flavor**. Production build must refuse any simulation endpoint, prohibit arbitrary remote host input, have no test-menu accessible to users, and remain local-only.

## Proposed implementation milestones / acceptance gates
- **V0 evidence compiler (no Android)**: stable source-derived fixtures, falsifiable observed-versus-synthetic labels, deterministic 96-test manifest. Gate: red-team approves each fixture provenance and expected result; never claim knowledge about short 0x41.
- **V1 state-machine reconstitution**: ports and event-driven core, 96 scenario cases passing (after they are implemented), plus fuzz/seed reproductions, no emitted real commands. Existing G6A offline recovery and identity modules integrated by adapter.
- **V2 one-emulator Android integration**: minimum UI, permissions, private storage, mock local HTTP, Android lifecycle; check restart/storage-full/day rollover; no live BLE.
- **V3 dual-AVD P2P spike**: first demonstrate a *single benign virtual P2P association + synthetic catalog* on emulator 36.5+ (feasibility not yet proven). Then run varied peers, callbacks, loss and teardown. Fail open? NEVER. If dual AVD impractical, retain B1 and physical single-peer scoped evidence.
- **V4 candidate final app**: one codebase, production adapter, build provenance, no mock endpoints in release, red-team + CI complete. This is **not automatically deployable on glasses**.
- **V5 separate physical gates**: only new identity/readiness information and at most a specifically approved disposable-file import after preservation policy is settled; G6A PASS and G6B progress require those exact physical proofs, not CI alone.

## Resource feasibility and economics
- K G1 GitHub repo currently public. Official GitHub docs list standard public runner compute as no-charge/unlimited. No paid service is required for the initial pure-Java + GitHub CI lane; package downloads, setup time, disk and runner availability still need practical verification.
- Two current Android AVDs are heavy. Official Android Emulator best-experience recommendation is >=16 GB RAM and >=16 GB disk, so **do not plan two simultaneous AVDs on a known 8-GB laptop**.
- B1 single emulator CI is prioritized over B2; B2 must verify sufficient RAM/disk/network state and automated cleanup first.
- No obligation to install paid third-party capture/cloud tooling.

## Honest blocker accounting
**Can be substantially tested virtually:** parser correctness, state machine/races, no blind retry, cleanup decision tree, catalog format, synthetic media integrity, local numbering and receipts, Android UI/permission/lifecycle, app CPU/storage behavior.
**Cannot be declared solved virtually:** actual glasses P2P SSID and identity, proprietary short `0x41` semantics, actual `0x73` after current ENTER, P2P association with owner glasses, firmware remote-consumption trigger and non-consumptive alternative, radio performance and battery on the actual OnePlus/glasses, real video/audio formats/timestamps, physical power-loss guarantees.
No invented percent coverage; track required assertions and hardware-dependent claims separately.

## Red-team stop conditions
- No uploading real Cyan APK/DEX, addresses, SSID/passwords, owner images or raw traffic to a public workflow.
- No synthetic fixture gets labelled REAL/PHYSICAL PASS.
- No production app automatic retry following a possibly consumptive media fetch.
- No connecting to a sole OTHER peer or generic AIMB-G1 prefix.
- No unapproved reset, AP fallback, arbitrary HTTP range/HEAD, OTA or bulk import.
- No replacing frozen release source or claiming a software simulated PASS fulfills a real gate.
- Do not assume simulated BLE inter-emulator radio works until independently verified.
- If G6A originals-on-glasses preservation is a hard requirement, keep real media GET blocked pending proven non-consumptive mechanism.

**Decision request for implementation**: First implement V0/V1 in a new `lab/virtual-aimb-g1/` tree with CI, retaining all existing releases and gate status. Hardware interaction remains separately authorized.

**Current:** G6A OPEN; G6B BLOCKED. This plan is research architecture only.
