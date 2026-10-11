# G6A Step 4 retry — original Cyan DEX / K v0.6.8.5–.6 cross-version blind-spot proof
Date: 2026-10-11. No new Android APK, glasses test, BLE command, Android Wi-Fi connection, device data logging or remote GET.

## Why retry Step 4
The previous Step 4 correctly held the real peer as UNKNOWN but had not **run the exact frozen old K peer matchers** against Cyan's distinct raw-address fallback. This retry recovers that missing discriminating hypothesis and performs cross-version source tests, not a repeated scan.

## Original Cyan APK provenance
Original owner-supplied `base.apk` on local isolated forensic workspace:
`sha256=1e700628d76fa4fa84047632e2ccce3673f1a01966fbf8c583985568f3aaaf64`.
Its six DEX files were previously byte-for-byte verified; this retry independently parsed the DEX headers, class methods, code items and instruction references of the exact original files. No third-party APK/DEX binaries or credentials are in GitHub.

**Direct code-unit evidence in `classes.dex`:**
- `MyBluetoothReceiver.connectStatue`: offset 269 invokes `UserConfig.setDeviceAddressNoClear`; earlier at 262 obtains `BluetoothDevice.getAddress()`.
- 278 `getDeviceName`; 291 `StringsKt.contains$default` for underscore; 322 `StringsKt.split$default`; 326–347 size/last-element selection; 356 and 445 `getDeviceAddress`; 367 and 456 `StringsKt.replace$default` for colon removal; 416 and 487 `setGlassDeviceWifiName`; 425 and 496 `setGlassDeviceWifiPassword`.
- There is also a conditional 20-character truncation **only when the colon-stripped address value exceeds 20 characters** in the earlier branch (length check 371–379). This does not affect an ordinary 12-hex normalized Bluetooth address, but is an extra source nuance omitted from the simplified older formula.

**Direct code-unit evidence in `classes4.dex`:**
- `PictureFragment.onPeersChanged`: 59 `getGlassDeviceWifiName`; 64 Kotlin case-insensitive `equals`; on its alternate path, 83 `getDeviceAddressNoClear`; 90 `StringsKt.endsWith$default` acting on the unnormalized saved address.
- This is not exact equivalence with K's normalized-address suffix-only comparator, regardless of how the Cyan candidate name was initially constructed.

The static source path establishes the **possible raw-colon Bluetooth-address matching branch**. Its successful use on the user's current glasses has NOT been observed.

## Cross-version distinction that v0.6.8.6 could not see
| Synthetic advertised P2P name (fabricated address only) | K v0.6.8.6 actual matcher | Cyan alternative source predicate | Offline research policy |
|---|---|---|---|
| `TEST_` + colon-free 12-hex paired BLE address | FULL-SUFFIX | Exact derived name potentially equals | EXACT_DERIVED_CANDIDATE |
| `DIFFERENT_` + colon-free 12-hex paired BLE address | FULL-SUFFIX | Exact constructed name differs | NORMALIZED_SUFFIX_CANDIDATE |
| `DIFFERENT_` + **colon-formatted paired BLE address** | **OTHER** | **raw-address endsWith may match** | RAW_ADDRESS_SUFFIX_RESEARCH |
| `AIMB-G1_` + colon-formatted paired BLE address | FAMILY | raw-address endsWith may match | RAW_ADDRESS_SUFFIX_RESEARCH |
| `OTHER_` + unrelated address | OTHER | Does not match known identity predicates | OTHER |

This is a **logical counterexample to concluding that OTHER excludes all Cyan matching possibilities**. It is not proof of the actual observed peer's advertised name. Only K's prior aggregate `OTHER=1` is stored for v0.6.8.6, so retrospectively identifying that peer is impossible from the saved report.

## Exact-source test, not paraphrased comparator
New offline Java 17 test classes compile the frozen unchanged:
- `source/v0.6.8.5/app/src/main/java/com/parkarsite/g6ap2pdiag685/DiagnosticSafetyRules.java`
- `source/v0.6.8.6/app/src/main/java/com/parkarsite/g6apeerdiag686/DiagnosticSafetyRules.java`
alongside `source/g6a-identity-core/PeerIdentityPolicy.java`. No Android classes are compiled; both frozen rules are pure Java.

Successful GitHub CI:
- [G6A Identity Candidate Offline Tests 38127615703](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38127615703) — **PASS**, 54 base + 8 v0.6.8.5 + 15 v0.6.8.6 = **77 assertions**.
- [Repository Hygiene 38127615701](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38127615701) — **PASS**.
- GitHub commits for exact-source regression: `43971d75`, `75a02bc6`, test runner `23171761`.

## Red-team implications
- The raw-address suffix is a stronger *candidate condition than generic AIMB-G1 prefix*, but **not authenticated hardware identity**. User-friendly peer names can be copied, and P2P MAC does not equal BLE MAC by assumption; Android may randomize Wi-Fi Direct device/interface addresses.
- The earlier v0.6.8.6 peer could have been unrelated, displayed a raw-colon address suffix, used an alternate address basis, been stale, or other possibilities. The old report does not discriminate them.
- A new static matcher is NOT alone a root-cause fix; no connection to a raw-address-only candidate is authorized.
- Never expose raw discovered P2P names, BLE/P2P addresses, Wi-Fi credentials, IP, user files or catalog lines to public logs/reports.
- The next *research-only* observer would need separate **mutually exclusive counters** for exact Cyan-derived name, normalized full Bluetooth-address suffix, raw-colon Bluetooth-address suffix, family prefix and OTHER, plus phase/time buckets (baseline before ENTER versus after ENTER). It must preserve BLE EXIT and Wi-Fi group-absence cleanup, never connect, never download or modify original media, and must not be deployed without explicit approval and independent APK red-team review.
- If even that observer returns OTHER=1 and raw-colon=0, do not repeat an unchanged scan; investigate alternate BLE name/address sources or transport advertising state.
- `discoverPeers` success only establishes that scan initiation succeeded, not peer identity or advertisement. Android permissions and Location Mode should be checked in the test specification, not inferred from a nonempty peer list. References: https://developer.android.com/develop/connectivity/wifi/wifip2p and https://developer.android.com/reference/android/net/wifi/p2p/WifiP2pDevice

## Decision
**Static blind spot CONFIRMED with original bytecode and direct legacy-source tests. Actual anonymous 2026-10-11 peer identity STILL UNVERIFIED.** No new APK or physical operation. No change to frozen v0.6.8.5/.6 sources. The G6A remote-original preservation requirement remains unsatisfied by known JPG transfer path.

**G6A OPEN. G6B BLOCKED.**
