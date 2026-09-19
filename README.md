# AIMB-G1 / K Site Capture

Authoritative repository for the AIMB-G1 glasses media-capture integration project.

## Current project state

Evidence-gated status as of 2026-09-19:

- **G0 — software/recovery readiness:** PASS
- **G1 — physical GATT confirmation:** PASS
- **G2 — response-channel confirmation:** PASS
- **G2B — response-semantic correlation:** NEXT / NO CONTROL WRITE
- **G3 — first allow-listed control command:** BLOCKED pending G2B evidence

The current strategy is deliberately passive: correlate physical glasses events with spontaneous notifications while separately tracing the Cyan parser. Do not guess payload meanings and do not send a proprietary BLE control write until G2B is reviewed.

Authoritative checkpoint:
- `LATEST_CHECKPOINT.md`
- `docs/testing/G2B_PASSIVE_CORRELATION_PLAN.md`
- tracked work: GitHub Issue #3

## Confirmed physical AIMB-G1 profile

G1 physically confirmed the Cyan-family BLE profile:

```text
Service  de5bf728-d711-4e47-af26-65e3012a5dc7
Notify   de5bf729-d711-4e47-af26-65e3012a5dc7
Write    de5bf72a-d711-4e47-af26-65e3012a5dc7
```

Sanitized evidence:
`docs/testing/results/2026-09-19_G1_PHYSICAL_DISCOVERY_PASS.md`

The physical hardware revision read successfully as `AM01SPG1_V1.4`.

## Confirmed response channel

K G1 Response Probe v0.2 enabled only the standard CCCD notification subscription on the confirmed Cyan notify characteristic. No proprietary characteristic write was implemented or sent.

During the 30-second passive observation, the glasses emitted three valid framed notifications:

```text
BC 73 03 00 52 31 05 47 00
BC 73 08 00 01 07 01 01 00 00 00 01 00 01
BC 73 03 00 53 A1 05 46 00
```

All three validate against the recovered Cyan envelope:
`BC | command | len_le16 | crc16_modbus(payload) | payload`.

Sanitized evidence:
`docs/testing/results/2026-09-19_G2_NOTIFICATION_ONLY_PASS.md`

The semantic meaning of command `0x73` and its payloads is **not yet established**.

## Current verified diagnostic builds

### Frozen reference — K G1 Discovery v0.1

- APK: `releases/v0.1/K_G1_Discovery_v0_1.apk`
- APK SHA-256: `c32758f898b91041bc7e13a272096d2629836e4465542ee00c1fbd9764e7479a`
- frozen source SHA-256: `84fb1b957290abdf2464a97b35b81f2d1e8e976ce9c067ecaf571c8c19f22c68`
- frozen branch: `frozen/discovery-v0.1-readonly`

### G1 diagnostic — K G1 Discovery v0.1.2

- APK: `releases/v0.1.2/K_G1_Discovery_v0_1_2.apk`
- APK SHA-256: `dc08b915581468f2d0d33fb317ca06b05e301b233cb0ced234edf7f0bc282297`
- build/verification run: `35395830630`

### G2 response probe — K G1 Response Probe v0.2

- APK: `releases/v0.2/K_G1_Response_Probe_v0_2.apk`
- APK SHA-256: `86b9738ac909577b173264be233242c17c9715be5bae183fffa6c9d497174ff2`
- source SHA-256: `86fa0f9f72171cb90c4edbc04d1a2db2b2c40c19c976978ea08a63c399700d39`
- package SHA-256: `b4d2fe59d87866dd993454ff58b53d18bf605891c74d2c9ddce1e9f40af18924`
- build/verification run: `35413378362`

## Next method — G2B passive correlation

Before G3, use two evidence streams in parallel:

1. **Targeted Cyan parser tracing**
   - follow notification callback → frame parser → command dispatch → state/UI consumer,
   - specifically resolve command `0x73`,
   - identify initialization/time semantics from Cyan evidence if present,
   - do not infer semantics from byte values alone.

2. **Passive physical event correlation**
   - subscribe to the already confirmed notify path,
   - send no proprietary characteristic command,
   - timestamp received frames,
   - timestamp user-marked physical actions,
   - repeat each safe action several times,
   - compare repeated event/frame associations,
   - retain only sanitized evidence.

The planned correlator must not include:
- proprietary characteristic writes,
- media-mode control commands,
- Wi-Fi/P2P/AP,
- HTTP/media transfer,
- pairing/unpairing/reset,
- OTA/firmware functions,
- generic BLE write console.

See `docs/testing/G2B_PASSIVE_CORRELATION_PLAN.md`.

## Architecture

See:
- `docs/architecture/ARCHITECTURE.md`
- `docs/architecture/MIGRATION_PLAN.md`
- `PROJECT_MANIFEST.md`

Repository roles:
- `apps/` — product/app lineages
- `modules/` — target modular production boundaries
- `source/v0.1/` — frozen Discovery source
- `source/v0.1.2/` — successful G1 diagnostic source
- `source/v0.2/` — successful G2 notification-only source
- `releases/` — verified installable diagnostics and provenance
- `archive/site-capture/v0.4/` — preserved legacy media-sync engine/reference
- `docs/protocol/` — protocol evidence independent of execution code
- `docs/testing/` — physical gate rules and sanitized results
- `docs/research/` — provenance of third-party research inputs

## Legacy K Site Capture work

The useful v0.4 source tree is preserved under:
`archive/site-capture/v0.4/`

It retains the transport abstraction, remote-media model, chronological import coordinator, daily numbering, streaming file writes, size verification, crash recovery, dedup ledger, OPUS support, core self-tests and historical tools.

Do not merge the legacy transport into the active diagnostic line. Reuse only proven responsibilities after the protocol gates are satisfied.

## Public-repository rule

The third-party Cyan APK, Bluetooth addresses, device-specific name suffixes, credentials, personal data, client/site media and unsanitized diagnostic captures are not committed. Store only reviewed interoperability findings and sanitized physical evidence.
