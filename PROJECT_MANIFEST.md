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
G2B exact response-semantic trace: PASS.  
G2C one-command initialization parity: PASS.  
G3 media-count query: PASS.  
G3B bounded P2P transfer lifecycle: PASS.  
G4A phone-side Wi-Fi Direct association: PASS.  
G4A2 passive glasses P2P-IP notification capture: **PASS**.  
G4B read-only media listing: **PHYSICAL HTTP/BODY READ REACHED — JSON PARSER UNRESOLVED**.  
G4B2 response-shape characterization: **PASS**.  
G4B3 exact line-list parser parity: **PASS**.  
G5 v0.5.0: **SAFE NO-DELTA STOP**.  
G5.1 v0.5.1: **VERIFIED BUILD READY — PHYSICAL TEST PENDING**.

v0.4.2 reached `/files/media.config` but failed because our diagnostic applied the wrong parser. Physical v0.4.4 then established a 67-byte UTF-8 `text/plain` line-oriented response. Deeper exact Cyan bytecode proves `configFileType==2` is the JSON/vf_list branch, while `configFileType!=2` uses `/files/media.config` plus Kotlin `readLines()`. The physical value is 1, so every line is a catalog entry. G5 remains blocked pending one final sanitized line-list parity check.

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


## G3 verified candidate — v0.3

| Item | Repository path | SHA-256 / status |
|---|---|---|
| G3 source | `releases/v0.3/K_G1_Media_Count_Probe_v0_3_source_v1.zip` | `bb5f5e523bcd456a2c88462741c64dd88f0498c956f749bafb35e4832b7dab03` |
| G3 APK | `releases/v0.3/K_G1_Media_Count_Probe_v0_3.apk` | `31191dc90dac56e9aced7db5a6f6b4c65b969f10ec6d673199ee94c6a2f204e9` |
| G3 package | `releases/v0.3/K_G1_Media_Count_Probe_v0_3_package.zip` | `0c016518b7fa7efd3cc4b071c1632c8ac48a6e4f0cbe01f5e082297bfb60fb03` |
| Build/verification | GitHub Actions run `35420261264` | PASS |
| Proprietary write scope | exactly one `0x41 / 02 04` query, no retry | PASS |
| P2P/AP media-mode payload | absent | PASS |
| Wi-Fi/network/transfer | absent | PASS |
| Reset/OTA/arbitrary command input | absent | PASS |

Issue #5 tracks the physical G3 media-count query. G3B media mode remains blocked until its raw response is reviewed.


## G3 physical result

| Item | Result |
|---|---|
| Physical G3 result | PASS |
| Sanitized report | `docs/testing/results/2026-09-19_G3_MEDIA_COUNT_PASS.md` |
| Query | `0x41 / 02 04` |
| Query response | valid `0x41` dataType-4 frame |
| imageCount | 1 |
| videoCount | 0 |
| recordCount | 1 |
| configFileType | 1 |
| onlySupportApImport | false |
| Exact Cyan selected transport | P2P |
| Exact P2P enter payload | `02 01 04 01` |
| Exact exit-transfer payload | `02 01 09` |
| Next gate | G3B P2P enter/exit lifecycle only |


## G3B verified candidate — v0.3.1

| Item | Repository path | SHA-256 / status |
|---|---|---|
| G3B source | `releases/v0.3.1/K_G1_P2P_Lifecycle_Probe_v0_3_1_source_v1.zip` | `3e0d3f3b3f37eb50df2298dfb8111d0fedfd9de92f29c635bf52e1d0666a8c35` |
| G3B APK | `releases/v0.3.1/K_G1_P2P_Lifecycle_Probe_v0_3_1.apk` | `1578b6540596625825c16a95da64f72f46ee29aaad47c866bb9025a3389dce7c` |
| G3B package | `releases/v0.3.1/K_G1_P2P_Lifecycle_Probe_v0_3_1_package.zip` | `2af95704d4b50cec143765d07f6a880f992c1f3d5daef00e9607f654f2c1cd96` |
| Build/verification | GitHub Actions run `35421429624` | PASS |
| Enter payload | `0x41 / 02 01 04 01`, once | PASS |
| Exit payload | `0x41 / 02 01 09`, once | PASS |
| Android Wi-Fi/P2P/network APIs | absent | PASS |
| AP-mode payload | absent | PASS |
| HTTP/media transfer | absent | PASS |

Issue #6 tracks the physical G3B lifecycle test. G4 local-network/media listing remains blocked until this result is reviewed.


## G3B physical result

| Item | Result |
|---|---|
| Physical G3B result | PASS |
| Sanitized report | `docs/testing/results/2026-09-19_G3B_P2P_LIFECYCLE_PASS.md` |
| P2P enter command | `0x41 / 02 01 04 01` |
| Enter response | valid `0x41` frame |
| Credential structure | 20-byte SSID + 9-byte password |
| Credential values | deliberately not stored publicly |
| Exit command | `0x41 / 02 01 09` |
| Exit response | valid `0x41` frame |
| Proprietary writes | exactly 2 |
| Phone-side Wi-Fi/P2P | none |
| HTTP/media transfer | none |
| Next gate | G4A Wi-Fi Direct discovery/association only |


## G4A physical result

| Item | Result |
|---|---|
| Physical G4A result | PASS |
| Sanitized report | `docs/testing/results/2026-09-19_G4A_P2P_ASSOCIATION_PASS.md` |
| Exact P2P peer match | YES |
| Wi-Fi Direct connect request | SUCCESS |
| P2P group formed | TRUE |
| Phone is group owner | TRUE |
| Group-owner address | `192.168.49.1` (phone-side) |
| HTTP/socket operations | 0 |
| Media/file access | 0 |
| Credentials persisted/logged | NO |
| Transfer exit | SUCCESS |
| Cleanup anomaly | `removeGroup` reason 2 / BUSY |
| Next gate | G4A2 passive glasses-IP notification capture |


## G4A2 verified candidate — v0.4.1

| Item | Repository path | SHA-256 / status |
|---|---|---|
| G4A2 source | `releases/v0.4.1/K_G1_P2P_IP_Notify_Probe_v0_4_1_source_v1.zip` | `1888a096f0312c1e7f513f24fce5b5667f5e6dfaccdb532c4f1a1c11070a2a1c` |
| G4A2 APK | `releases/v0.4.1/K_G1_P2P_IP_Notify_Probe_v0_4_1.apk` | `3c9104c34fbf06fb06631a09c67cac9eab3e2a626078abcc14401cf09a2cddf3` |
| G4A2 package | `releases/v0.4.1/K_G1_P2P_IP_Notify_Probe_v0_4_1_package.zip` | `45aedb093424797a1d9764016e63b83f3a312e79a4921d31d400f75e93b77360` |
| Canonical build commit | — | `a1eafae6da7202b0af32ff8ee3fc017422608a4b` |
| Archive commit | — | `1291b782451b12c0bb35436c30f0b660c09b2667` |
| GitHub Actions build/verification | run `35429585616` | PASS |
| G4A2 safety audit | — | PASS |
| Android compile + lint | — | PASS |
| APK signature verification | — | PASS |
| P2P-IP query `02 03` | — | ABSENT |
| Internet / HTTP / sockets / media access | — | ABSENT |
| Proprietary writes | — | enter once + exit once only |
| `0x73` report scope | — | event IDs only; IPv4 only for event `0x08` |
| Physical G4A2 result | `docs/testing/results/2026-09-19_G4A2_P2P_IP_NOTIFY_PASS.md` | PASS |
| Observed async events | — | `0x0B`, `0x08`, `0x01` |
| Phone group-owner IP | — | `192.168.49.1` |
| Glasses client IP from `0x73 / 0x08` | — | `192.168.49.176` |
| HTTP/socket/media operations | — | 0 |
| `0x41 / 02 03` query | — | NOT USED |

Issue #8 is complete. G4B is unblocked for separate read-only design and verification.

## G4B verified candidate — v0.4.2

| Item | Repository path | SHA-256 / status |
|---|---|---|
| G4B source | `releases/v0.4.2/K_G1_Media_Catalog_Probe_v0_4_2_source_v1.zip` | `b81f9c9c22c74460e06799ca817d7a4bf2453c9534207f308d256ad86196130f` |
| G4B APK | `releases/v0.4.2/K_G1_Media_Catalog_Probe_v0_4_2.apk` | `ffd7233a7006909d7090e39291afb214e0dc72cc771d9f54896d0612c1c5d6f2` |
| G4B package | `releases/v0.4.2/K_G1_Media_Catalog_Probe_v0_4_2_package.zip` | `8dc6f962ff4deabc9a1dc675b006fa82ee59c3c02b9e322067659c65bbe9c0c5` |
| Canonical build commit | — | `d9cd2e28fdd1a5f62b25e21fd7d6eb81c5840508` |
| Archive commit | — | `2fbe008aaec3192cb846d91d7839c6fee51366c6` |
| GitHub Actions build/verification | run `35431413193` | PASS |
| Repository Hygiene on source commit | run `35431413187` | PASS |
| G4B safety audit | — | PASS |
| Android compile + lint | — | PASS |
| APK signature verification | — | PASS |
| Package ID | — | `com.parkarsite.g1mediacatalogprobe` |
| Exact Cyan catalog branch | — | `configFileType=1 → /files/media.config` |
| HTTP method/sites | — | GET only / exactly one URL constructor |
| Redirects | — | DISABLED |
| Catalog response cap | — | 65,536 bytes |
| P2P-IP query `02 03` | — | ABSENT |
| Media-file GET/download/mutation | — | ABSENT |
| Filename/path value logging | — | ABSENT |
| Physical G4B result | `docs/testing/results/2026-09-19_G4B_CATALOG_PARSE_FAIL.md` | HTTP/body read reached; `JSONException` at diagnostic parser |
| Media-file GET requests | — | 0 |
| Transfer exit | — | SUCCESS |

Exact static evidence: `docs/research/CYAN_EXACT_G4B_TRACE_2026-09-19.md`.

## G4B2 physical result — PASS

| Item | Result |
|---|---|
| Physical report | `docs/testing/results/2026-09-19_G4B2_RESPONSE_SHAPE_PASS.md` |
| HTTP status | 200 |
| Content-Type | `text/plain` |
| Response bytes | 67 |
| UTF-8 | valid |
| BOM | none |
| Diagnostic line count | 4 |
| JSON parse | not applicable to physical configFileType=1 |
| Media-file GET requests | 0 |
| Raw body / filename/path / fingerprint logging | ABSENT |
| Transfer exit | SUCCESS |

Corrected exact static evidence: `docs/research/CYAN_EXACT_G4B_TRACE_2026-09-19.md`.

## G4B3 physical result — PASS

| Item | Result |
|---|---|
| Physical report | `docs/testing/results/2026-09-19_G4B3_CATALOG_LINE_PASS.md` |
| Catalog entries | 3 |
| Non-empty / safe relative | 3 / 3 |
| Extensions | `.jpg=2, .opus=1` |
| Unsafe path flags | 0 |
| Media-file GETs | 0 |
| Private catalog values logged | NO |
| Transfer exit | SUCCESS |

G4B2's diagnostic line-count 4 is superseded by the Cyan-equivalent G4B3 parser: three actual entries plus a terminal newline.

## G5 static trace / design

Exact Cyan media download evidence:
- `docs/research/CYAN_EXACT_G5_TRACE_2026-09-19.md`
- URL form: `http://<glassDeviceWifiIP>/files/<catalog-line>`
- download caller: `AndroidNetworking.download(url, albumDir, filename)`
- tag: `download_file`
- priority: MEDIUM
- progress listener + download listener
- queue completion eventually reaches the already confirmed transfer-exit lifecycle

First G5 selection is delta-based, not queue-position based. It requires a baseline catalog, one disposable user-captured JPG while transfer mode is off, then exactly one new safe JPG on the second catalog read. Only that new entry may be requested.

## G5 verified candidate — v0.5.0

| Item | Repository path | SHA-256 / status |
|---|---|---|
| G5 source | `releases/v0.5.0/K_G1_Disposable_Photo_Probe_v0_5_0_source_v1.zip` | `9b4023279957adb38f79cae4dd323e0ffef314e13e17e31632b19c29d924e7f9` |
| G5 APK | `releases/v0.5.0/K_G1_Disposable_Photo_Probe_v0_5_0.apk` | `7966cb3f5ecb1be89f876a6686d34b74dbf693c9231603cd8a92dc2b8d361712` |
| G5 package | `releases/v0.5.0/K_G1_Disposable_Photo_Probe_v0_5_0_package.zip` | `97badec697dc20abee2b16b30d58d86b599e23b634eb2e97bce7de20e6b6c0a7` |
| Canonical hardened build commit | — | `b9152f13ed7bacbd2d895a161da3dafbb9461406` |
| Archive commit | — | `3749b2398c300f8b8677496157fd5d45ad51bd9b` |
| Build / verification | run `35435019643` | PASS |
| Hardened source Repository Hygiene | run `35435019654` | PASS |
| Safety audit / compile / lint / signature | — | PASS |
| Package ID | — | `com.parkarsite.g1singlephotoprobe` |
| Selection | — | baseline + exactly one new safe JPG delta |
| Catalog GETs | — | max 2 |
| Media GETs | — | max 1 |
| Media cap | — | 32 MiB |
| Local target | — | app-private cache |
| JPEG validation | — | SOI + EOI |
| Redirect / retry / Range / resume | — | ABSENT |
| Remote name/path/raw catalog/fingerprint logging | — | ABSENT |
| Glasses mutation / AP fallback / `02 03` | — | ABSENT |
| Physical G5 result | — | PENDING |

The earlier first v0.5.0 build is superseded; a pre-physical lifecycle recheck found a delayed BLE-disconnect callback race between phases. The approved artifact is the rebuilt hardened candidate identified by the APK SHA-256 above.


## G5.1 verified candidate — v0.5.1

| Item | Value |
|---|---|
| APK | `releases/v0.5.1/K_G1_Disposable_Photo_Probe_v0_5_1.apk` |
| APK SHA-256 | `20cab114c980e10c2e277cb82960b761975aac8dad467c36d7c88023dd7a403f` |
| Source ZIP SHA-256 | `dbca801c17415cae2b27c7b8246dc7530939981081fcf2967e016ae2fe22ced9` |
| Package ZIP SHA-256 | `5928b44f68f1c0efbbf60c3d692891505c496a184bc9ae974487001d431dcba0` |
| Source/build commit | `c8e54b68ed817f899aef57030d9e9bf4a442ea2e` |
| Build run | `35436132719` — PASS |
| Archive commit | `353bbd72bbf9d72a81b3ff82f6664a58aa864807` |
| Package ID | `com.parkarsite.g1singlephotoprobe51` |
| Per-phase inventory query | one `0x41 / 02 04` |
| Catalog readiness | exact 1000 ms Cyan delay |
| Per-phase P2P enter/exit | one / one |
| Catalog GETs | one per phase |
| Media GETs | one maximum |
| Selection | exactly one new strict-safe relative JPG |
| Media cap | 32 MiB |
| Redirect / retry / Range / resume | absent |
| AP fallback / `02 03` | absent |
| Glasses mutation | absent |
| Physical result | pending |

v0.5.0 remains preserved as the safe no-delta physical record.


## G5.2 verified candidate — v0.5.2

| Item | Value |
|---|---|
| APK | `releases/v0.5.2/K_G1_Disposable_Photo_Probe_v0_5_2.apk` |
| APK SHA-256 | `14fa9447ee938d20c35c935f5665bbcd72f0edb61b33f1e266d96aacb18bfe7a` |
| Source ZIP SHA-256 | `d04bc3382eee299d94ecfe2a012a2b9cb50423391f50482e03ac47d5958f9aae` |
| Package ZIP SHA-256 | `bf338e8358ffaa35a4d613ab13c45ed9ef643fa077764f8e5b2e35fa13f1306e` |
| Source/build commit | `17e0a33c9d7490b57fc2f3d6f468f1c6be94d2c0` |
| Build run | `35436808167` — PASS |
| Archive commit | `7b2c41fe5121d19b585d0ab993329de15bc0414b` |
| Package ID | `com.parkarsite.g1singlephotoprobe52` |
| Count handoff | waits for write callback + valid response |
| Failure recovery | resets to BASELINE |
| Physical result | pending |

v0.5.1 is superseded before physical use.
