# Virtual AIMB-G1 Lab — 96 Lane A offline safety-subset assertions

**Source verification:** 34a218ccec0116909cc07fbc75c2a729b63a791a
**GitHub Actions:** https://github.com/kphomedocs-hue/Glasses-/actions/runs/38131350988 SUCCESS.
**Repository Hygiene:** https://github.com/kphomedocs-hue/Glasses-/actions/runs/38131350927 SUCCESS.

Run from repository root: bash lab/virtual-aimb-g1/run_selftest.sh (Java 17).

## Source-only architecture
- VirtualClock: deterministic separately scoped timeouts.
- FrameEvidence: actual module protocol CRC and synthetic 0x41/0x73 envelopes; short 0x41 meaning UNKNOWN.
- VirtualGlassesCore: BLE lifecycle, peer candidate classification via the actual PeerIdentityPolicy, no real peer connect or media GET.
- VirtualLabPolicies: synthetic group teardown, bounded line-list/limited synthetic JSON catalog, capture-delta selection, one-shot response failure policy, JPG/MP4/OPUS shape-only verification, candidate mixed-media naming, injected platform policies, release report sanitation.
- VirtualScenarioSelfTest covers packs 01–03 (24 IDs).
- VirtualRemainingScenarioSelfTest covers packs 04–12 (72 IDs). Pack 09 actually exercises RecoveryImportStore, including crashes and local-only recovery.
- VirtualRedTeamSelfTest and VirtualPhase2RedTeamSelfTest perform independent adversarial checks.
- The runner enforces exactly all 96 unique scenario IDs, no missing/duplicate case.

## Critical limits
96/96 means only **source-only synthetic safety assertion subsets PASS**. None is certified end-to-end against the final Android production code path. These tests contain no Android app build, real Bluetooth, Wi-Fi Direct, HTTP socket, new APK, physical capture or hardware run. Core ports are still not wired to production Android, and additional new decision models are candidate-only.

Real type-2 JSON, 0x73 typed content, short 0x41 meaning, media codecs and peer authentication are not proven. Simulated group/Android lifecycle evidence uses injected boolean values. Actual mixed-kind persisted counters are not implemented; the current RecoveryImportStore is JPG-only. JPEG and media container shape is NOT proof of full image/video/audio decoding.

## Remaining development/physical gates
1. Wire all safety policies into one adopted production state machine, Android adapters and durable storage; prohibit unsafe media GET.
2. Validate one Android emulator with synthetic HTTP, permissions/lifecycle, error cases and simulated capture; then investigate dual-AVD virtual P2P only if feasible.
3. Separately establish true device identity, current proprietary event semantics and media fidelity, and a method to import while keeping originals on glasses before physical sync.

Previous physical JPG GET+EXIT consumed the original from glasses. **No physical media GET or new physical test has been authorized by this work.**

**G6A OPEN; G6B BLOCKED.**

Detailed evidence: docs/testing/results/2026-10-11_VGL_LANE_A_96_OFFLINE_TEST_RESULT.md
Independent findings: docs/testing/VGL_LANE_A_96_RED_TEAM_REVIEW_2026-10-11.md
