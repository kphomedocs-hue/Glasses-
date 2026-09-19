# Project Manifest

Authoritative inventory for the AIMB-G1 / K Site Capture project.

## Current physical-test baseline

### G1 retry candidate — v0.1.1

| Item | Repository path | SHA-256 / status |
|---|---|---|
| Discovery v0.1.1 source | `releases/v0.1.1/K_G1_Discovery_v0_1_1_source_v1.zip` | `0e2949500cfdc6d04f161164b2324cf811ec852f540a15b59684a3da9ec10d05` |
| Discovery v0.1.1 APK | `releases/v0.1.1/K_G1_Discovery_v0_1_1.apk` | `bdb73a04c2650fefbcd433a13674a32c18dd94a07a2c8ef16a90a8980d3f0358` |
| Discovery v0.1.1 package | `releases/v0.1.1/K_G1_Discovery_v0_1_1_package.zip` | `d7ae14fa0b42c063e7d857ecb119cff25b60a8192249b2835f9e2e5f140031b0` |
| Safety boundary | read-only G1 | PASS |
| Full verification run | GitHub Actions `35393800836` | PASS |

v0.1.1 is a narrow G1 retry candidate. It accepts the `AIMB-G1_*` name family while keeping the frozen v0.1 read-only behavior and forbidden-operation boundary unchanged.

### Frozen v0.1 reference

| Item | Repository path | SHA-256 / status |
|---|---|---|
| Discovery v0.1 frozen source | `releases/v0.1/K_G1_Discovery_v0_1_source_v4.zip` | `84fb1b957290abdf2464a97b35b81f2d1e8e976ce9c067ecaf571c8c19f22c68` |
| Repository-built Discovery APK | `releases/v0.1/K_G1_Discovery_v0_1.apk` | `c32758f898b91041bc7e13a272096d2629836e4465542ee00c1fbd9764e7479a` |
| Repository package | `releases/v0.1/K_G1_Discovery_v0_1_package.zip` | `e6b23d5d0eaed6a07112f7f14343e011299bf706fe0e98f447a74d24f92c827a` |
| Expanded Discovery source | `source/v0.1/` | frozen |
| Build provenance | `releases/v0.1/BUILD_INFO.md` | verified |
| Lint report | `releases/v0.1/lint-results-debug.html` | archived |

## Earlier Discovery development artifacts

These hashes preserve provenance for the local build history. v4 is the frozen source baseline.

| Artifact | SHA-256 |
|---|---|
| `K_G1_Discovery_v0_1_source.zip` | `62c5e1a4518f24075957f2b6c36ddb1a3ff3c7fee210622d3843c95d18be615a` |
| `K_G1_Discovery_v0_1_source_v2.zip` | `a8da9da94da52c19edac69ea7e2a9a8190aeb9d98a1d26bb774f81ca0a18575e` |
| `K_G1_Discovery_v0_1_source_v3.zip` | `8f0f2ae4a956945cceb0736dddb83e3cf365c47aec6bbb47b3a4f71b0da420f9` |
| `K_G1_Discovery_v0_1_source_v4.zip` | `84fb1b957290abdf2464a97b35b81f2d1e8e976ce9c067ecaf571c8c19f22c68` |
| First local debug APK | `ab37790ad13028aa6e8f1e3d16b957c72f8038df0638e8e852b31488d9fc762a` |
| First local artifact ZIP | `ccd4680706c96356b1a3339300f48378e0c969d029a9d301cd61eb31e08ebcbb` |
| First local package ZIP | `608c2645470cd7ade65a8126242b04391333408fb125b51d391278ae8d3906fe` |

The repository-built APK is the canonical installable v0.1 artifact. Earlier debug APK/package bytes are provenance only.

## K Site Capture legacy lineage

| Version | Original ZIP SHA-256 | Role |
|---|---|---|
| v0.1 | `eecbd428a53977c7858b17010db1c646493f5ddf6cc2773ec78b8ac44fee0db6` | initial storage/transport proof |
| v0.2 | `204fa7942637c79e9da1889050503c230e69d9f3f29e88ba960a60a7497af1eb` | iterative preflight |
| v0.3 | `a306bb4a182007f3e9e2974a4611b54673334f94139bcea171aa8ffb587bacdd` | stronger preflight/test baseline |
| v0.4 | `2843b69ac743986d76a8c1d6821e88fc4978e10517a972184b4e32920b621a01` | best legacy media-engine reference |

v0.4 is being preserved under `archive/site-capture/v0.4/` in expanded, browsable form. Earlier versions are retained in this manifest for lineage and should never replace v0.4.

## Third-party Cyan input

The analysed Cyan export is intentionally **not committed** because this repository is public and the APK is third-party software.

- File: `Cyan Glasses-1.0.2.18_20260811-split-apks.zip`
- Size: 139,799,818 bytes
- SHA-256: `1328b3c025f43c06b2a0674d4c17890ec4b76cb6487196a84aa27b2335c2fc49`
- Package: `com.aitowe.aitoglasses`
- Version: `1.0.2.18_20260811`
- Version code: `85`

Only interoperability findings and provenance are stored in this public repository.

## Current gate

G0: PASS.  
G1 physical GATT confirmation: PASS.  
G2 notification/response-channel confirmation: PASS.  
G2B exact response-semantic trace: **PASS**.  
G2C one-command initialization parity: **NEXT**.  
G3 media-mode control: **BLOCKED pending G2C**.

The next authorized implementation is a single-command Cyan-equivalent `0x40` time-sync probe. No `0x41` control/media command, Wi-Fi transition, HTTP transfer, reset or OTA action is authorized in G2C.

## Discovery v0.1.2 diagnostic candidate

| Item | Repository path | SHA-256 / status |
|---|---|---|
| v0.1.2 source | `releases/v0.1.2/K_G1_Discovery_v0_1_2_source_v1.zip` | `de89fd0dd15d48c51e1f080158b426f455699187a0f42f28686cf957850f3271` |
| v0.1.2 APK | `releases/v0.1.2/K_G1_Discovery_v0_1_2.apk` | `dc08b915581468f2d0d33fb317ca06b05e301b233cb0ced234edf7f0bc282297` |
| v0.1.2 package | `releases/v0.1.2/K_G1_Discovery_v0_1_2_package.zip` | `d7ddedc782ced77fda0f8b0a18237101a4124c3762a359888b20de017329a667` |
| GitHub Actions build/verification | run `35395830630` | PASS |
| Safety scope | G1 read-only observation + unique bonded fallback | PASS |

v0.1.2 does not replace the frozen v0.1 baseline. It is the next diagnostic G1 candidate after scan-identification ambiguity.


## G1 physical confirmation

| Item | Result |
|---|---|
| Physical G1 result | PASS |
| Sanitized report | `docs/testing/results/2026-09-19_G1_PHYSICAL_DISCOVERY_PASS.md` |
| Cyan service `de5bf728-...` | PRESENT |
| Cyan notify `de5bf729-...` | PRESENT / NOTIFY |
| Cyan write `de5bf72a-...` | PRESENT / WRITE + WRITE_NO_RESPONSE |
| Hardware revision | `AM01SPG1_V1.4` |
| BLE scan callbacks | 0; bonded LE fallback used |
| Next gate | G2 notification-only response-channel confirmation |


## G2 response-channel candidate — v0.2

| Item | Repository path | SHA-256 / status |
|---|---|---|
| G2 source | `releases/v0.2/K_G1_Response_Probe_v0_2_source_v1.zip` | `86fa0f9f72171cb90c4edbc04d1a2db2b2c40c19c976978ea08a63c399700d39` |
| G2 APK | `releases/v0.2/K_G1_Response_Probe_v0_2.apk` | `86b9738ac909577b173264be233242c17c9715be5bae183fffa6c9d497174ff2` |
| G2 package | `releases/v0.2/K_G1_Response_Probe_v0_2_package.zip` | `b4d2fe59d87866dd993454ff58b53d18bf605891c74d2c9ddce1e9f40af18924` |
| GitHub Actions build/verification | run `35413378362` | PASS |
| Scope | notification subscription on confirmed Cyan response characteristic only | PASS |
| Proprietary characteristic write API | absent | PASS |
| Cyan control-write UUID `de5bf72a-...` | absent from v0.2 source | PASS |

Issue #2 tracks the physical G2 test. No control-characteristic write is authorized until its notification-only result is reviewed.


## G2 physical notification result

| Item | Result |
|---|---|
| G2 notification-only physical result | PASS |
| Sanitized report | `docs/testing/results/2026-09-19_G2_NOTIFICATION_ONLY_PASS.md` |
| Cyan notify subscription | SUCCESS |
| Standard CCCD write | SUCCESS |
| Proprietary characteristic write | NONE |
| Spontaneous notifications | 3 frames / 32 bytes |
| Physically observed response command | `0x73` |
| Envelope validation | length + CRC-16/MODBUS PASS for all 3 frames |
| Payload semantics | pending |
| Initialization/time requirement | not yet established |
| G3 media-mode command | BLOCKED pending semantic review |


## G2B passive-correlation plan

| Item | Status |
|---|---|
| Strategy | static Cyan parser trace + passive physical event correlation |
| Plan | `docs/testing/G2B_PASSIVE_CORRELATION_PLAN.md` |
| Tracking | GitHub Issue #3 |
| Notify path | physically confirmed `de5bf729-...` |
| Proprietary characteristic writes | PROHIBITED |
| Media-mode command | PROHIBITED |
| Wi-Fi / HTTP transfer | PROHIBITED |
| G3 | BLOCKED pending G2B review |

No G2B executable has been started at this checkpoint; this entry records the approved method before implementation begins.


## G2B exact static-trace result

| Item | Result |
|---|---|
| Exact trace evidence | `docs/research/CYAN_EXACT_G2B_TRACE_2026-09-19.md` |
| `0x73` channel | asynchronous device-data reporting |
| `0x01` event | media inventory/count/config report |
| Physical `0x01` stable fields | image=1, video=0, record=1, configFileType=1 |
| Physical schema note | optional AP-only flag absent from shorter physical frame |
| `0x05` event | battery/charging family; charging flag directly confirmed |
| Cyan first post-discovery proprietary command | `0x40` syncTime |
| Time-sync callback | empty / not used as gate |
| Passive correlator | fallback only |
| Next gate | G2C single `0x40` time-sync command |
| Media-mode `0x41` | BLOCKED pending G2C |


## G2C verified candidate — v0.2.1

| Item | Repository path | SHA-256 / status |
|---|---|---|
| G2C source | `releases/v0.2.1/K_G1_Init_Probe_v0_2_1_source_v1.zip` | `d14e3e56586a45616c335f6e0d85ac93131bbe7754dd4e422b2661c98c915761` |
| G2C APK | `releases/v0.2.1/K_G1_Init_Probe_v0_2_1.apk` | `7c0ce5d859e79c6ee2ed05316f5f5db736919912802fbbab040ce612ea368e5c` |
| G2C package | `releases/v0.2.1/K_G1_Init_Probe_v0_2_1_package.zip` | `5c8d2bfaf042cfcb9d54f8fe5804396087e1bc18b84658f851b92fb38fd1e7c8` |
| Build/verification | GitHub Actions run `35416416013` | PASS |
| Proprietary write scope | exactly one command-`0x40` time-sync attempt, no retry | PASS |
| Media/control command `0x41` | absent | PASS |
| Wi-Fi/network/reset/OTA | absent | PASS |

Issue #4 tracks the physical G2C test. G3 media-mode control remains blocked until that report is reviewed.


## G2C physical result

| Item | Result |
|---|---|
| Physical G2C result | PASS |
| Sanitized report | `docs/testing/results/2026-09-19_G2C_TIME_SYNC_PASS.md` |
| Proprietary command | `0x40` time sync |
| Proprietary write attempts | exactly 1 |
| Android write callback | SUCCESS |
| `0x40` response | `BC 40 01 00 BF 40 00` |
| Response frame validation | PASS |
| Additional proprietary writes | 0 |
| Media-mode command | NONE |
| Next gate | G3 `0x41` payload `02 04` media-count query |
