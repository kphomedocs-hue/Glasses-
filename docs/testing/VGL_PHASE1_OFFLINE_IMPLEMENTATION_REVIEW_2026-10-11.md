# Virtual AIMB-G1 Lab — phase 1 source-only independent review

Date: 2026-10-11. Scope: additive lab/virtual-aimb-g1, packs 01–03 only.

## Baseline provenance / decision
The source of truth before modification was main b4dc372cccd2500b0b71c59c58407d4f6ab8d4fa and its 96-case planning matrix. Historical physical successes through G5.7 are not replaced or retested. The current peer is unidentified, short 0x41 is unknown and the tested JPEG GET+exit is remotely consumptive. Existing protocol and peer identity Java sources are imported unchanged rather than copied; recovery core is not yet integrated.

## Independent adversarial review checklist
- [x] Test code has fixed synthetic input strings; no owner media or captured packets.
- [x] Source imports only Java core and existing transport-free modules.
- [x] No peer connect, HTTP media GET, AP fallback, or auto-retry operation in ports.
- [x] Exactly one BLE ENTER and at most one EXIT for the normal observation path, with deterministic deadline tests.
- [x] Short 0x41 remains shape-only; no secret fields inferred.
- [x] Exact, normalized suffix, raw-colon suffix and generic/OTHER remain separate categories.
- [x] No peer-name category grants trusted identity. Catalogue eligibility needs three independently supplied synthetic prerequisites (peer membership, network binding, correlated IP).
- [x] G6A and G6B status deliberately unchanged.
- [x] Follow-up red team closed a potential stale peer/group/IP attestation hole: changed snapshot invalidates proof, fresh IP required and terminal events inert.
- [x] Synthetic MAC literals rejected by public hygiene were replaced by composed synthetic test data.

## Open findings — not accepted as resolved
1. This is a candidate shared core, not yet adopted by the Android production app. Port equivalence and Android-specific callback provenance need separate review.
2. The longer 0x41 fixture only checks envelope; real credential decoder not in phase 1. Scenario 02-01 is partial by original matrix wording.
3. 0x73 inventory count decoding and P2P group membership are typed synthetic injections, not verified protocol implementations. 02-05 and 02-07 are partial.
4. 03-07/08 do not provide an authenticated physical peer-address oracle. They assert fail-closed behavior only.
5. No transport teardown API/group cleanup exists here. EXIT callback is simulated and asserted, not Android removeGroup proof.
6. No crash-proof storage, media validation, numbering, or preservation-safe media copy is integrated. Existing recovery experiments are separate and not production evidence.
7. Exact verified runtime log: https://github.com/kphomedocs-hue/Glasses-/actions/runs/38130089425 (24/24 safety subsets PASS and separate negative-path suite PASS); hygiene https://github.com/kphomedocs-hue/Glasses-/actions/runs/38130089461 PASS. See the separate evidence report for the initial failed hygiene run and corrective commits. This remains source-only, not a device PASS.

## Promotion rule
Run offline CI and inspect logs. Record the exact tested commit and failures. Preserve scenario matrix historical labels until integration semantics are independently verified; only mark executable safety subset separately. No APK or physical test is authorized.
