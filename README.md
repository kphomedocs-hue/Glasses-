# AIMB-G1 / K Site Capture

Authoritative repository for the AIMB-G1 glasses media-capture integration project.

## Current physical-test baseline

**K G1 Discovery v0.1**

Purpose: read-only BLE/GATT confirmation on the user's physical AIMB-G1 before any proprietary control command is sent.

Safety boundary:
- BLE scan/connect/GATT discovery only
- enumerates all services/characteristics/properties
- reads only selected standard readable device-information fields
- no proprietary BLE writes
- no notification subscription
- no Wi-Fi/media-transfer command
- no Internet permission
- no reset/restart/OTA/firmware operations

## Verified build

Canonical repository APK:

`releases/v0.1/K_G1_Discovery_v0_1.apk`

SHA-256:

`c32758f898b91041bc7e13a272096d2629836e4465542ee00c1fbd9764e7479a`

Frozen source SHA-256:

`84fb1b957290abdf2464a97b35b81f2d1e8e976ce9c067ecaf571c8c19f22c68`

The CI gate passes source verification, read-only safety audit, Android compilation, Android Lint and APK signature verification.

## Architecture

See:

- `docs/architecture/ARCHITECTURE.md`
- `docs/architecture/MIGRATION_PLAN.md`
- `PROJECT_MANIFEST.md`

Repository roles:

- `apps/` — product/app lineages
- `modules/` — target modular production boundaries
- `source/v0.1/` — frozen Discovery source
- `releases/v0.1/` — installable Discovery release and QA
- `archive/site-capture/v0.4/` — preserved legacy media-sync engine/reference
- `docs/protocol/` — protocol evidence independent of execution code
- `docs/testing/` — physical gate rules
- `docs/research/` — provenance of third-party research inputs

## Legacy K Site Capture work

The full useful v0.4 source tree is preserved under:

`archive/site-capture/v0.4/`

It retains:
- fake/real transport abstraction
- remote media model
- chronological import coordinator
- daily numbering
- streaming file writes
- declared-size verification
- crash recovery
- persistent dedup ledger
- OPUS support
- core self-tests
- historical preflight/tools

Earlier v0.1–v0.3 lineage hashes are retained in `PROJECT_MANIFEST.md` and `archive/site-capture/README.md`.

## Protocol evidence

See `docs/protocol/AIMB_G1_PROTOCOL.md`.

Expected physical BLE family:

```text
de5bf728-d711-4e47-af26-65e3012a5dc7
de5bf729-d711-4e47-af26-65e3012a5dc7
de5bf72a-d711-4e47-af26-65e3012a5dc7
```

## Next physical gate

1. Force-stop Cyan Glasses.
2. Turn on AIMB-G1.
3. Install K G1 Discovery v0.1.
4. Grant Nearby Devices/Bluetooth permission.
5. Scan and inspect AIMB-G1.
6. Share the generated report.
7. Review the actual GATT profile before enabling any control write.

See `docs/testing/PHYSICAL_TEST_GATE.md`.

## Public-repository rule

The third-party Cyan APK, credentials, personal data and future device-specific reports containing sensitive identifiers are not committed here. Their safe provenance/findings are documented instead.
