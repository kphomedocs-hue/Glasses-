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


## 2026-10-11 V2 VERIFIED OVERRIDE — ANDROID SOURCE COMPILES, HOST-LOCAL HTTP 7/7 PASS
Latest V2 verified source: c16b5898fd3d7caf8b2261ddec2c7ed80f5a2c91. Read docs/testing/results/2026-10-11_VGL_V2_ANDROID_COMPILE_HOST_HTTP_RESULT.md and docs/testing/VGL_V2_ANDROID_SOURCE_RED_TEAM_2026-10-11.md. Additive Android simulator-only Gradle project lab/virtual-aimb-g1/android-simulator/ compiles the ACTUAL shared VirtualGlassesCore, policies, protocol, identity and RecoveryImportStore into a no-permissions, source-only Android Activity plus compiled but UNEXECUTED instrumentation hook. API 36, JDK17, Gradle 9.6. Android CI 38132519731 SUCCESS: compile-only; verified merged manifest and NO APK built or installed, NO emulator. Recovery core Files.readString ported to strict 4 KiB bounded UTF-8 read to permit Android Java compilation; previous recovery CI 38132290876 PASS. A separate HOST JVM loopback HTTP simulator in lab/virtual-aimb-g1/host-http/ runs 7/7 synthetic fault cases (GET200, 500, 302, truncation, corrupt JPEG, consumptive-on-GET, forbidden other host) against ACTUAL RecoveryImportStore; together with 96/96 offline synthetic IDs and both red teams CI 38132519735 PASS, hygiene 38132519714 PASS. These are NOT Android emulator runtime tests, real phone tests, production adapter integration, physical identity proof or original-preserving sync. Historical remote JPG GET consumes original; OWNER REQUIREMENT remains unsatisfied. DO NOT BUILD/INSTALL EXPERIMENTAL APK OR RUN PHYSICAL MEDIA GET without separately scoped authorization. Next: one-AVD emulator-only instrumentation after specific approval, full shipping-path adapter integration, then independently verify actual peer and originals-preserving method. G6A OPEN; G6B BLOCKED.
