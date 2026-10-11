# Virtual AIMB-G1 V2B — integrated host core + local HTTP + durable recovery
**Date:** 2026-10-11 (IST)
**Exact tested commit:** `54b3540c437c39fd81e1d38132462d26de7078b1`
**Gate type:** host-JVM synthetic integration, NOT Android emulator, NOT current glasses, NOT release APK.
**GitHub CI evidence:** [Virtual AIMB-G1 Lab Offline 96 — run 38137429577](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38137429577) SUCCESS.
**Privacy/hygiene:** [Repository Hygiene — run 38137429591](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38137429591) SUCCESS.

## What is materially new
The preceding V2 report ran 96 offline decision assertion subsets and seven **separately hosted** local HTTP/receipt cases. This V2B addendum runs the **actual** `VirtualGlassesCore`, `PeerIdentityPolicy`, `VirtualLabPolicies.Catalog`, `VirtualLabPolicies.Capture`, a concrete `127.0.0.1` `HttpServer` and `RecoveryImportStore` together in one synthetic flow.

New source:
`lab/virtual-aimb-g1/host-http/src/com/parkarsite/g1/virtual/host/HostCoreIntegrationSelfTest.java`

The module’s `run_selftest.sh` now compiles shared core, protocol, identity and recovery source classes **without copying their implementation**, then executes old `LoopbackHttpSelfTest` 7 cases plus the new integrated 10 cases. GitHub workflow already invokes that script and enforces G6A OPEN / G6B BLOCKED.

## Exact executed synthetic integration cases

| Case | Scenario | Verified assertion |
|---|---|---|
| CORE-HTTP-01 | Exact candidate + explicitly injected group membership/network/IP + one new JPG | Real local HTTP catalog ×2, one localhost media GET, real durable `RecoveryImportStore` receipt, no repeated GET, one EXIT and no Range |
| CORE-HTTP-02 | Sole unrelated OTHER peer | Candidate verdict NO_CANDIDATE; no catalog/media HTTP |
| CORE-HTTP-03 | Generic AIMB-G1 family prefix only | Not trusted; no HTTP |
| CORE-HTTP-04 | Exact candidate but `trustedSource=false` | No group/cat eligibility and no HTTP |
| CORE-HTTP-05 | Exact candidate but no correlated synthetic IP | No catalog/media GET |
| CORE-HTTP-06 | Pre-existing baseline JPG missing from post snapshot | Capture delta BLOCK, no consumptive media GET |
| CORE-HTTP-07 | Two newly seen JPG identities after one capture | Capture delta AMBIGUOUS, no arbitrary GET |
| CORE-HTTP-08 | No explicit synthetic-only permit or session already exited | No synthetic media GET |
| CORE-HTTP-09 | Synthetic HTTP 500 after source request | Durable state REMOTE_UNCERTAIN, no second GET |
| CORE-HTTP-10 | Two plausible peer names | AMBIGUOUS, no catalog/media HTTP |

CI log directly shows `HOST_CORE 10/10 PASS`, `HOST_HTTP 7/7 PASS`, and 96 unique `VGL-xx-xx OFFLINE_ASSERTIONS_PASS` plus two offline red-team test suites.

## Strictly bounded technical claims
- **What this verifies:** A real host-JVM `HttpURLConnection`/localhost server and actual file-backed `RecoveryImportStore` can be exercised in a single synthetic application state-machine scenario, preserving no-retry and fail-closed decisions.
- **What this does NOT verify:** Android runtime, Wi-Fi Direct radio/network identity or BLE implementation; the group `selectedMember`, `networkBound` and `trustedSource` booleans are injected synthetic assumptions, not independent hardware attestation.
- **Critically:** The core's `connectAuthorized()==false` and `mediaGetAuthorized()==false` remain intact. The integrated test's dedicated `fakeOnlyDownload` **explicitly bypasses the production media permission solely for a synthetic loopback fixture**; it exists exclusively under `host-http`, outside Android simulator sourceSets and production code. Without its explicit test-only permit, all local GET operations are blocked. No legitimate device media GET becomes permitted from this milestone.
- Media fixture is a seven-byte marker shaped like a JPEG, **not a decodable full photograph**. JPG/MP4/OPUS fidelity, actual camera timestamps and Android filesystem durability remain unresolved.
- Real v0.6.7 JPG GET + EXIT consumed the original remote JPG. Current original-preservation requirement is **unmet** and the synthetic consume case is not a workaround.

## Adversarial red-team review
- **R1 unsafe peer substitution:** Even a single model-prefix-only peer and an OTHER peer produced zero HTTP. PASS synthetic.
- **R2 forged exact string:** Exact candidate plus `trustedSource=false` produced zero HTTP. PASS synthetic; this is not authenticating a real peer.
- **R3 ambiguous media:** baseline loss or +2 new JPG caused zero media GET. PASS.
- **R4 repeat-after-consumption:** duplicate `RecoveryImportStore` operation produced no second HTTP GET. PASS.
- **R5 ambiguous selection:** two plausible peers never allowed catalog. PASS.
- **R6 terminal cleanup:** final EXIT prevents further synthetic import. PASS.
- **R7 simulation segregation:** test source only under `lab/virtual-aimb-g1/host-http/`; `VirtualGlassesCore.mediaGetAuthorized` remains `false`, and Android source has no Internet/Bluetooth/Wi-Fi permissions. PASS from source review; not an installed Android manifest runtime proof.
- **Residual R8:** injected `onGroupEvidence(true,true,true)` is not cryptographic proof; so the test doesn't solve physical peer identity. NOT SOLVED.
- **Residual R9:** successful local receipt cannot reconstruct a consumptively fetched original never saved. NOT SOLVED.

**Decision:** V2B host-only integration PASS. The 96 matrix scenario rows remain INTEGRATION PENDING until the shipping-path Android adapter executes and is verified; the 10 additional CORE-HTTP IDs must not be silently counted as G6A physical passes.

**Next safe milestone:** independent Android instrumentation/emulator runtime validation, which requires **separate authorization** to create/install even a simulator-only APK; alternatively further pure-source and localhost safeguards without APK. No physical connection authorized.

**G6A OPEN. G6B BLOCKED.**
