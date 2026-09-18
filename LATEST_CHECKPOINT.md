# LATEST CHECKPOINT

## Project
K Site Capture / AIMB-G1 glasses integration

## Authoritative repository state

The repository now separates:
- frozen Discovery v0.1 release/source,
- preserved Site Capture v0.4 media-engine source,
- protocol evidence,
- physical-test gates,
- modular future architecture,
- artifact/provenance manifest.

Primary navigation:
- `README.md`
- `PROJECT_MANIFEST.md`
- `docs/architecture/ARCHITECTURE.md`
- `docs/architecture/MIGRATION_PLAN.md`
- `docs/protocol/AIMB_G1_PROTOCOL.md`
- `docs/testing/PHYSICAL_TEST_GATE.md`
- `archive/site-capture/v0.4/`

## Frozen physical-test baseline
**K G1 Discovery v0.1**

Status: software build gate **and clean-room recovery gate complete**; first physical read-only test pending.

### Verified artifacts
- Repository APK: `releases/v0.1/K_G1_Discovery_v0_1.apk`
- APK SHA-256: `c32758f898b91041bc7e13a272096d2629836e4465542ee00c1fbd9764e7479a`
- Frozen source ZIP SHA-256: `84fb1b957290abdf2464a97b35b81f2d1e8e976ce9c067ecaf571c8c19f22c68`
- Repository package ZIP SHA-256: `e6b23d5d0eaed6a07112f7f14343e011299bf706fe0e98f447a74d24f92c827a`
- Earlier first-build debug APK hash retained for provenance: `ab37790ad13028aa6e8f1e3d16b957c72f8038df0638e8e852b31488d9fc762a`

### Build gates passed
- exact source-integrity verification
- read-only safety audit
- Android compilation
- Android Lint: 0 errors
- APK signature verification

## Preserved legacy media engine

**K Site Capture v0.4** is archived in expanded browsable form under `archive/site-capture/v0.4/`.

Important reusable components:
- `G1Transport`
- `FakeAimbG1Transport`
- `RemoteMedia`
- `ProbeRunner`
- `NumberingPolicy`
- `FileMediaArchive`
- `FileImportLedger`
- protocol framing/CRC reference
- core self-test suite
- historical diagnostic tools

Original v0.4 ZIP SHA-256:
`2843b69ac743986d76a8c1d6821e88fc4978e10517a972184b4e32920b621a01`

Earlier v0.1–v0.3 hashes are recorded in `PROJECT_MANIFEST.md`.

## Confirmed Cyan evidence

Analysed app:
- package: `com.aitowe.aitoglasses`
- version: `1.0.2.18_20260811`
- export SHA-256: `1328b3c025f43c06b2a0674d4c17890ec4b76cb6487196a84aa27b2335c2fc49`

Recovered BLE family:
- Service: `de5bf728-d711-4e47-af26-65e3012a5dc7`
- Notify: `de5bf729-d711-4e47-af26-65e3012a5dc7`
- Write: `de5bf72a-d711-4e47-af26-65e3012a5dc7`

Recovered framing:
- magic: `BC`
- glasses-control command ID: `41`
- length: little-endian
- CRC: CRC-16/MODBUS over payload, little-endian

Recovered media payloads:
- P2P-associated: `02 01 04 01`
- AP-associated: `02 01 04 02`

Confirmed local media-path evidence:
- `media.config`
- Wi-Fi P2P/AP support
- local glasses IP/name/password fields
- local HTTP download
- `/files/`
- OPUS support
- local `glass_album` metadata table

See `docs/protocol/AIMB_G1_PROTOCOL.md` for evidence levels and limitations.

## Architecture rule

Do not merge the legacy v0.4 transport into Discovery v0.1.

After physical confirmation, migrate proven v0.4 responsibilities into separate modules:
- protocol
- device-ble
- device-network
- media-transfer
- media-storage
- sync-ledger
- diagnostics

See `docs/architecture/MIGRATION_PLAN.md`.

## v0.1 safety boundary

Allowed:
- BLE scan
- connect
- discover GATT services/characteristics
- read selected standard Device Information fields
- disconnect
- generate/share report

Forbidden:
- proprietary BLE characteristic writes
- descriptor writes/notification subscription
- Wi-Fi/network access
- media-transfer commands
- reset/restart/OTA/firmware operations

## Next action

1. Force-stop Cyan Glasses.
2. Turn on AIMB-G1.
3. Install K G1 Discovery v0.1.
4. Grant Nearby Devices/Bluetooth permission.
5. Scan and inspect AIMB-G1.
6. Share the generated report.
7. Confirm the physical profile.
8. Only after review, design the next controlled test.

This file remains authoritative for resuming the project.


## Software recovery verification

- Cold Recovery Gate run `35387241734`: PASS
- Hardened Cold Recovery run `35387536088` (expanded source == frozen ZIP): PASS
- Core Module Regression run `35387408775`: PASS
- Expanded Core Module Regression run `35387812463`: PASS
- Frozen v0.1 reference branch: `frozen/discovery-v0.1-readonly`
- Detailed record: `docs/testing/results/2026-09-19_SOFTWARE_RECOVERY_GATE.md`

This confirms GitHub is sufficient to recover and rebuild the current project without the previous chat/workspace.


## Additional pure-core readiness

Diagnostics redaction and sync-ledger core models are now covered by CI without introducing Android, BLE, Wi-Fi or HTTP dependencies into those modules.

- Diagnostics sanitizer: PASS
- Sync-ledger state model: PASS
- Expanded core regression run: `35387812463`


## G1 report and repository hygiene readiness

Pre-hardware report handling is complete:

- Discovery Report Validation run `35389022993`: PASS
- Repository Hygiene run `35389022968`: PASS
- Report sanitizer: `tools/validate_g1_report.py`
- Physical result template: `docs/testing/results/TEMPLATE_G1_DISCOVERY_REPORT.md`
- Public data rules: `docs/security/PUBLIC_REPO_DATA_RULES.md`
- Tracked G1 physical-test issue: #1

The repository now rejects/sanitizes reports containing MAC addresses, passwords or serial data and blocks accidental third-party Cyan packages, packet captures, raw media and oversized files from normal tracked content.

No further protocol implementation should be added before the physical G1 report is reviewed.
