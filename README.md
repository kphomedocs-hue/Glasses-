# AIMB-G1 / K Site Capture

Authoritative repository for the AIMB-G1 glasses media-capture integration project.

## Current physical-test baseline

**Frozen reference:** K G1 Discovery v0.1  
**Current G1 retry candidate:** K G1 Discovery v0.1.1

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

Frozen v0.1 repository APK:

`releases/v0.1/K_G1_Discovery_v0_1.apk`

Current v0.1.1 G1 retry APK:

`releases/v0.1.1/K_G1_Discovery_v0_1_1.apk`

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


## G1 retry note — v0.1.1

Two read-only v0.1 scans completed without finding the glasses. Android identifies the paired device using the `AIMB-G1_<suffix>` naming form rather than the exact `AIMB-G1` string expected by v0.1.

v0.1.1 changes only target-name recognition:
- accepts exact `AIMB-G1`,
- accepts `AIMB-G1_*`,
- preserves the same read-only BLE/GATT safety boundary.

Verified v0.1.1 artifacts:
- APK SHA-256: `bdb73a04c2650fefbcd433a13674a32c18dd94a07a2c8ef16a90a8980d3f0358`
- source ZIP SHA-256: `0e2949500cfdc6d04f161164b2324cf811ec852f540a15b59684a3da9ec10d05`
- package ZIP SHA-256: `d7ae14fa0b42c063e7d857ecb119cff25b60a8192249b2835f9e2e5f140031b0`
- latest verification run: `35393800836`

Do not unpair or reset AIMB-G1 for this retry.


## G1 diagnostic candidate — v0.1.2

After the v0.1 exact-name scan misses, v0.1.2 adds diagnostic resilience while preserving the read-only boundary.

It adds:
- a 30-second explicit low-latency BLE observation window,
- AIMB-G1 / AIMB-G1_* family matching,
- known Cyan service UUID observation,
- aggregate BLE observation counts without logging unrelated nearby-device names,
- read-only inspection of bonded-device metadata,
- a read-only LE GATT fallback only when exactly one bonded AIMB-G1-family device is available,
- a bounded GATT timeout.

It still contains no proprietary BLE writes, descriptor writes, notification subscriptions, pairing/unpairing operations, Wi-Fi/P2P/AP, HTTP/media transfer, reset/restart/OTA or firmware commands.

Verified release:
- APK: `releases/v0.1.2/K_G1_Discovery_v0_1_2.apk`
- APK SHA-256: `dc08b915581468f2d0d33fb317ca06b05e301b233cb0ced234edf7f0bc282297`
- source ZIP SHA-256: `de89fd0dd15d48c51e1f080158b426f455699187a0f42f28686cf957850f3271`
- package ZIP SHA-256: `d7ddedc782ced77fda0f8b0a18237101a4124c3762a359888b20de017329a667`
- build/verification run: `35395830630`
