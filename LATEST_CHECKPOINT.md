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

Status: software recovery complete; G1 physical read-only GATT confirmation **PASS**.

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

G1 and the notification-only stage of G2 are complete.

**G2B exact static trace: PASS.**

Exact Cyan parser tracing resolved the observed event semantics and the normal initialization order. The passive Event Correlator is now a fallback rather than the next required build.

Current next gate: **G2C — one-command initialization parity**.

Approved design target:
1. connect to exactly one bonded AIMB-G1-family device;
2. subscribe to the confirmed Cyan notify characteristic;
3. construct the exact Cyan-equivalent dynamic `0x40` time-sync payload from phone time/language/timezone;
4. send exactly that one proprietary frame;
5. capture the `0x40` response and any `0x73` reports;
6. send no `0x41` control/media command;
7. disconnect.

Exact static-trace evidence:
`docs/research/CYAN_EXACT_G2B_TRACE_2026-09-19.md`

G3 media-mode control remains blocked until G2C is physically reviewed.

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


## G1 retry checkpoint — K G1 Discovery v0.1.1

Two physical read-only scans using frozen v0.1 returned:
- AIMB-G1 not found during the 12-second scan.

Android separately confirms the paired glasses are present and exposes the device name in the `AIMB-G1_<suffix>` form. No full Bluetooth address or device-specific suffix is recorded in this public repository.

Code review found v0.1 required an exact `AIMB-G1` name match. The frozen v0.1 release and `frozen/discovery-v0.1-readonly` branch remain unchanged.

v0.1.1 changes only the target-name matcher:
- exact `AIMB-G1`: accepted,
- `AIMB-G1_*`: accepted,
- no proprietary BLE writes,
- no descriptor writes / notification subscription,
- no Wi-Fi/P2P/AP,
- no HTTP/media transfer,
- no reset/restart/OTA.

Verified v0.1.1 release:
- Repository APK: `releases/v0.1.1/K_G1_Discovery_v0_1_1.apk`
- APK SHA-256: `bdb73a04c2650fefbcd433a13674a32c18dd94a07a2c8ef16a90a8980d3f0358`
- Source ZIP SHA-256: `0e2949500cfdc6d04f161164b2324cf811ec852f540a15b59684a3da9ec10d05`
- Package ZIP SHA-256: `d7ae14fa0b42c063e7d857ecb119cff25b60a8192249b2835f9e2e5f140031b0`
- Canonical APK build run: `35393397232`
- Latest full verification run: `35393800836`
- Android compile: PASS
- Android Lint: PASS
- Read-only safety audit: PASS
- APK signature verification: PASS

Next action: install v0.1.1 and repeat G1 read-only discovery without changing pairing, reset, Wi-Fi or firmware state.


## G1 diagnostic checkpoint — K G1 Discovery v0.1.2

Purpose: resolve the remaining ambiguity after the first read-only v0.1 scans did not identify the glasses.

v0.1.2 is a diagnostic candidate, not a protocol-control release. It adds:
- 30-second low-latency BLE observation,
- sanitized AIMB-G1-family/Cyan-service candidate detection,
- aggregate scan health metrics,
- bonded-device type inspection,
- direct LE GATT fallback only when one and only one bonded AIMB-G1-family device is identified,
- 30-second GATT connection/service-discovery timeout.

Safety remains read-only:
- no proprietary characteristic writes,
- no descriptor writes,
- no notification subscription,
- no pairing/unpairing/reset,
- no Wi-Fi/P2P/AP,
- no HTTP/media transfer,
- no OTA/firmware command.

Verified artifacts:
- APK SHA-256: `dc08b915581468f2d0d33fb317ca06b05e301b233cb0ced234edf7f0bc282297`
- source ZIP SHA-256: `de89fd0dd15d48c51e1f080158b426f455699187a0f42f28686cf957850f3271`
- package ZIP SHA-256: `d7ddedc782ced77fda0f8b0a18237101a4124c3762a359888b20de017329a667`
- canonical build commit: `f048418960255e1fa292be60da4012c2613eca98`
- build/verification run: `35395830630`
- diagnostic scope verification: PASS
- read-only safety audit: PASS
- Android compile: PASS
- Android Lint: PASS
- APK signature verification: PASS

The v0.1 frozen branch and v0.1/v0.1.1 source/release trees remain unchanged.


## G1 physical result — PASS

Physical test date: 2026-09-19

- K G1 Discovery v0.1.2 connected through the uniquely identified bonded-device LE fallback.
- BLE scan callbacks during the 30-second observation window: 0.
- Physical Cyan service `de5bf728-d711-4e47-af26-65e3012a5dc7`: PRESENT.
- Physical notify characteristic `de5bf729-d711-4e47-af26-65e3012a5dc7`: PRESENT / NOTIFY.
- Physical write characteristic `de5bf72a-d711-4e47-af26-65e3012a5dc7`: PRESENT / WRITE + WRITE_NO_RESPONSE.
- Hardware revision read: `AM01SPG1_V1.4`.
- Firmware revision read returned Android GATT status `133`; retained as an anomaly, not a G1 blocker.
- Sanitized evidence: `docs/testing/results/2026-09-19_G1_PHYSICAL_DISCOVERY_PASS.md`.
- G1 conclusion: **PASS**.
- Next gate: G2 notification/response-channel confirmation only; no proprietary control write yet.


## G2 notification-only probe — verified build ready

**K G1 Response Probe v0.2** is the current physical-test candidate for G2.

Verified repository artifacts:
- APK: `releases/v0.2/K_G1_Response_Probe_v0_2.apk`
- APK SHA-256: `86b9738ac909577b173264be233242c17c9715be5bae183fffa6c9d497174ff2`
- Source ZIP SHA-256: `86fa0f9f72171cb90c4edbc04d1a2db2b2c40c19c976978ea08a63c399700d39`
- Package ZIP SHA-256: `b4d2fe59d87866dd993454ff58b53d18bf605891c74d2c9ddce1e9f40af18924`
- Canonical build commit: `e4ef4667ae0db5b9be1e449abe571f988f5064c9`
- Build/verification run: `35413378362`
- G2 notification-only scope verification: PASS
- G2 safety audit: PASS
- Android compile: PASS
- Android Lint: PASS
- APK signature verification: PASS

Safety boundary:
- may enable notifications only on `de5bf729-d711-4e47-af26-65e3012a5dc7`,
- may write only the standard CCCD enable-notification value required for that subscription,
- contains no proprietary characteristic-write API,
- contains no `de5bf72a-...` control-write UUID,
- contains no initialization/media command,
- contains no Wi-Fi/network/reset/OTA behavior.

Next physical action: install v0.2, force-stop Cyan Glasses, leave AIMB-G1 paired, run the 30-second notification-only probe, and return the complete report before any control write is considered.


## G2 physical result — notification path PASS

Physical test date: 2026-09-19

K G1 Response Probe v0.2 successfully:
- connected to exactly one bonded AIMB-G1-family device over LE,
- found the physically confirmed Cyan service and notify characteristic,
- enabled local notifications,
- wrote only the standard CCCD enable-notification value,
- observed three spontaneous notifications in 30 seconds,
- sent no proprietary characteristic write.

Observed frames:
- `BC 73 03 00 52 31 05 47 00`
- `BC 73 08 00 01 07 01 01 00 00 00 01 00 01`
- `BC 73 03 00 53 A1 05 46 00`

All three frames validate against the known Cyan length + CRC-16/MODBUS envelope.

Sanitized evidence:
`docs/testing/results/2026-09-19_G2_NOTIFICATION_ONLY_PASS.md`

G2 response-channel confirmation: **PASS**.

Important: the semantic meaning of command `0x73` and its payloads is still pending. This result confirms passive response-channel traffic; it does not authorize a proprietary control write or media-mode command.

Next action: decode `0x73` / initialization semantics before G3.


## G2B strategy checkpoint — passive first

Decision recorded 2026-09-19:

The next step is **not** a control-write experiment.

The preferred evidence path is:
- targeted Cyan parser tracing,
- plus a passive event-correlation diagnostic if needed.

Planned correlator:
- uses the already confirmed bonded-device LE connection path,
- subscribes only to `de5bf729-d711-4e47-af26-65e3012a5dc7`,
- writes only the standard CCCD required for notification subscription,
- timestamps valid incoming frames,
- provides explicit user event markers,
- computes length/CRC validation and groups repeated payloads,
- produces a sanitized report,
- contains no proprietary characteristic-write API and no Cyan control-write UUID.

Do not advance to G3 until G2B evidence is reviewed.


## G2B exact Cyan trace — PASS

Exact package traced:
- Cyan Glasses `1.0.2.18_20260811`
- split export SHA-256 `1328b3c025f43c06b2a0674d4c17890ec4b76cb6487196a84aa27b2335c2fc49`
- extracted `base.apk` SHA-256 `1e700628d76fa4fa84047632e2ccce3673f1a01966fbf8c583985568f3aaaf64`

Key findings:
- command `0x73` is Cyan's asynchronous device-data reporting channel;
- event `0x01` is a media inventory/count/config report;
- physical `0x01` decodes as image=1, video=0, record=1, configFileType=1; its optional AP-only flag is absent because the physical frame is one byte shorter than the current parser schema;
- event `0x05` is a battery/charging-family report; the physical charging flag is 0 in both observed frames;
- after service discovery, Cyan enables notifications and queues `syncTime(0x40)` first, then device info/settings;
- Cyan does not wait on or inspect the time-sync callback before continuing, so time sync is normal initialization but not proven to be a media-mode handshake gate.

G2B conclusion: **PASS**.

Passive Event Correlator: retained as fallback, not required before G2C.


## G2C verified build ready — K G1 Init Probe v0.2.1

Verified repository artifacts:
- APK: `releases/v0.2.1/K_G1_Init_Probe_v0_2_1.apk`
- APK SHA-256: `7c0ce5d859e79c6ee2ed05316f5f5db736919912802fbbab040ce612ea368e5c`
- Source ZIP: `releases/v0.2.1/K_G1_Init_Probe_v0_2_1_source_v1.zip`
- Source SHA-256: `d14e3e56586a45616c335f6e0d85ac93131bbe7754dd4e422b2661c98c915761`
- Package ZIP: `releases/v0.2.1/K_G1_Init_Probe_v0_2_1_package.zip`
- Package SHA-256: `5c8d2bfaf042cfcb9d54f8fe5804396087e1bc18b84658f851b92fb38fd1e7c8`
- Canonical build commit: `7666393a08e8093ae81524a701f65c80717e2191`
- Archive commit: `92d017ba3ff43ce1ba8fa2831d79306632e7d390`
- Build/verification run: `35416416013`
- G2C single-command scope verification: PASS
- G2C safety audit: PASS
- Android compile: PASS
- Android Lint: PASS
- APK signature verification: PASS

Physical-test boundary:
- notification subscription on the confirmed Cyan response characteristic,
- exactly one proprietary characteristic-write attempt,
- the only implemented proprietary command is dynamically generated Cyan-equivalent `0x40` time sync,
- no retry,
- no `0x41` media/control command,
- no Wi-Fi/P2P/AP,
- no HTTP/media transfer,
- no reset/restart/OTA/firmware operation,
- no arbitrary command input.

Next action: run the verified v0.2.1 APK once and return the complete G2C report. Do not proceed to media mode afterward.
