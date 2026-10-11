# G6A Steps 1–2 — adversarial static red-team review
2026-10-11. Scope: source-only identity and preservation research; no APK and no glasses actions.

## Evidence reviewed
- `G6A_STEP1_EXACT_WIFI_IDENTITY_STATIC_COMPARISON_2026-10-11.md`
- `G6A_STEP2_NONCONSUMPTIVE_MEDIA_PATH_AUDIT_2026-10-11.md`
- Frozen K v0.4.0, v0.6.7, v0.6.8.5, v0.6.8.6 sources.
- Historical exact Cyan DEX call-site report `G6A_CYAN_EXACT_DEX_WIFI_IDENTITY_CALLSITE_TRACE_2026-10-11.md`, recorded against verified Cyan base APK hash `1e700628d76fa4fa84047632e2ccce3673f1a01966fbf8c583985568f3aaaf64`.
- Exact v0.6.8.5/.6 physical outputs, v0.6.5/.6/.7 remote media reports.
- Official Android Wi-Fi Direct P2P guide: https://developer.android.com/develop/connectivity/wifi/wifi-direct.

## Findings challenged and dispositions
| # | Critical adversarial question | Disposition |
|---|---|---|
| R01 | Does name construction being possible without credential response prove current glasses advertise that name? | **NO.** Code-path confirmed, physical name not verified. |
| R02 | Could merely using Cyan name algorithm convert v0.6.8.6 OTHER to an authenticated peer? | **NO on same-address premise.** Constructed name retains normalized full address suffix that K tested and did not see. |
| R03 | Is Cyan raw-address `endsWith` fallback equivalent to K stripped 12-hex suffix? | **NO.** They differ. Never claim code parity. |
| R04 | Does K's WPS-PBC not supplying parsed password mean Wi-Fi connection always succeeds without it? | **NO.** API does not require password argument when peers support Direct, but correct peer, P2P mode and association still required. |
| R05 | Does one peer in Wi-Fi Direct results imply BLE identity? | **NO.** One peer can be unrelated or stale; not authenticated. |
| R06 | Do `WifiP2pDevice.deviceAddress` and BLE `BluetoothDevice.getAddress()` have the same guaranteed value? | **NO such guarantee in inspected evidence.** Separate interfaces and identity contexts. |
| R07 | Did v0.6.8.5/.6 prove failure to associate? | **NO.** Neither attempted `connect`. Thus hardware group failure not observed. |
| R08 | Is short 0x41 a rejection/busy code or proof ENTER worked? | **UNKNOWN.** Out of work-type-4 parser's accepted response; avoid semantics. |
| R09 | Does configFileType=2 represent a harmless optional alternate download on this device? | **NO.** Physically reported config type1; type2 different firmware/branch and endpoint, not interchangeable. |
| R10 | Does choosing AP over P2P preserve media? | **UNPROVEN.** Network transport change does not establish changed HTTP side effects. |
| R11 | Does no explicit K delete prove read-only GET retained remote copy? | **NO.** Physical JPG removal observed despite no K delete command. |
| R12 | Did experiment isolate *instantaneous* server-side consume on GET itself? | **NOT FULLY.** The observed disappearance was after GET followed by exit/reconnect; specific firmware trigger timing remains unresolved. |
| R13 | Does one consumed JPG mean videos/OPUS or all firmware consume? | **NO.** Evidence is single JPG transfer path only. |
| R14 | Can a temp file and ledger fully protect against a remote one-time GET and process death? | **NOT YET.** Failure atomicity and recovery unverified; a partial GET could pose irreversible media-loss risk. |
| R15 | Did we freshly reverse-engineer all methods of uploaded Cyan APK this step? | **NO.** Reused previous verified exact-DEX trace and retained static research, plus fresh K source comparison. Do not assert exhaustive absence of alternatives. |
| R16 | Does the physical G4A pass guarantee current association with no credential frame? | **NO.** Different observed response state/timing. |
| R17 | Can new diagnostic be promoted merely from these static notes? | **NO.** No implementation, CI/manual review or physical authorization granted. |

## Minor source quality checks
- v0.6.8.6 categories prioritize suffix before generic family: `AIMB-G1_AABB...` would be FULL-SUFFIX, not FAMILY. Step 1 reflects this.
- v0.6.8.5's header claims name-or-suffix but actual runtime calls suffix-only policy. Step 1 describes code, not header.
- `WifiP2pConfig.deviceAddress` is a P2P device address; do not confuse with BLE address used for synthetic name derivation.
- Cyan might overwrite, retain or reload its MMKV name; an accurate static name-builder does not guarantee freshest advertisement.
- `0x73/0x08` gives readiness/IP in historical formed-group tests, but lack of event in .6 passive-only test does not rule out readiness after a future connection.
- Remote consumption may occur at GET completion or at device transfer cleanup. No destructive experiments authorized to disambiguate.

## Red-team decision
- **PASS** for documentation correctness and safe, bounded investigation with explicitly stated unknowns.
- **FAIL** for any claim that Wi-Fi peer identity is solved, that a non-consumptive media alternative is demonstrated, or that the current G6A app can resume import.
- **NO PHYSICAL PROMOTION.** No new APK, no peer connection, no reset/retry/alternate endpoint, no media GET permitted by this review.
- Best next offline work: design a privacy-preserving *exact peer observation specification* under the unresolved Bluetooth-address ambiguity; and a crash-consistency fault model for consumptive GET. Both would need another red-team/CI review before any hardware trial. The original-preservation requirement requires explicit user decision if alternatives remain unavailable.

**Gate state: G6A OPEN; G6B BLOCKED.**
