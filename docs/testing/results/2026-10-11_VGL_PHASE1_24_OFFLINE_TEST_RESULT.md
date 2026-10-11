# K G1 Virtual AIMB-G1 Lab — phase 1 / 24 offline scenario subsets

Evidence date: 2026-10-11 IST. Source commit **1c195bd6fb6254365994520cabc48a493e6ed95c** on main. No APK and no real device.

## Exact reproducible result
- Test workflow: https://github.com/kphomedocs-hue/Glasses-/actions/runs/38130089425 — **SUCCESS**, job offline-24.
- Repository hygiene: https://github.com/kphomedocs-hue/Glasses-/actions/runs/38130089461 — **SUCCESS**.
- Tested command: bash lab/virtual-aimb-g1/run_selftest.sh, GitHub-hosted Linux + Temurin Java 17.
- Scenario output: **24/24 OFFLINE SYNTHETIC ASSERTION SETS PASS; ZERO PHYSICAL GATES**. All IDs VGL-01-01 to 01-08, 02-01 to 02-08, 03-01 to 03-08 each printed OFFLINE_ASSERTIONS_PASS.
- A separate VirtualRedTeamSelfTest also printed PASS, including timer independence, no automatic media GET, no name-based peer trust, and invalid frame rejection.
- Workflow guards confirmed no executable Android, BLE or HTTP transport in the virtual source and preserved **G6A OPEN / G6B BLOCKED**.

## Correction history (do not conceal early failures)
1. Initial e9bb2e233ce8390d191eb351e3d0b4a34324247c: synthetic suite succeeded in run 38129925143, but repository-hygiene run 38129925173 **FAILED** because the public-file scanner correctly rejected literal address-format examples, even though they were fabricated.
2. Corrected source 05fcd2f82af5261d1ae3ba6b7e5018de59d8c26a uses composed synthetic values rather than literal MAC strings; scenario run 38130005542 and hygiene 38130005529 **PASS**. The matrix was annotated as OFFLINE SUBSET RUN; INTEGRATION PENDING.
3. Red-team hardening 1c195bd6fb6254365994520cabc48a493e6ed95c: candidate snapshot required before group attestation, change of snapshot invalidates group/IP proof, new group evidence resets correlated IP, and terminal callbacks do not change event counts. Exact CI 38130089425 and hygiene 38130089461 **PASS**.

## Functional boundary actually exercised
- Pack 01: select unique bonded target, subscription, GATT/CCCD failure/timeout, ENTER callback ordering, EXIT once and timeout uncertainty.
- Pack 02: CRC-valid/invalid synthetic BLE frame envelopes, **unknown** short 0x41, bounded 0x73 event-ID counters, typed synthetic inventory and correlated group/IP gate.
- Pack 03: exact candidate, normalized full suffix, raw colon-form research bucket, generic and OTHER, duplicate plausible peers, spoofing and interface address mismatch precautions, always NO CONNECT.
- The same unchanged existing Java CRC and peer-name classification modules compile with the lab state-machine candidate. All fixtures and inputs are fabricated.

## Evidence limitations
These are 24 **executed source-only safety assertion sets**, **not 24 full production Android integration passes**. In particular 02-01 has no proven long-credential field decoder; 02-05 has synthetic typed counts; 02-07 and 03-07/08 have no Android/physical attestation oracle. VirtualGlassesCore is intended for future shared production integration but is not yet wired to the APK. There is no actual media integrity/receipt, one-AVD or two-AVD execution yet.

The publicly stored matrix intentionally labels them OFFLINE SUBSET RUN; INTEGRATION PENDING. The remaining **72 scenarios are planned/unrun**. Short 0x41 semantics, real peer identity, and preservation-safe transfer are unresolved. Real JPG GET + exit has physically been associated with original disappearance; do not run another hardware GET without separately approved, verified preservation policy. G6A OPEN. G6B BLOCKED.

Next safe phase: packs 04–06 offline, then connect immutable RecoveryImportStore with a production-style policy port, add negative no-retry tests, and only then evaluate single-emulator Android integration. Reassess test equivalence to production before promoting any scenario.
