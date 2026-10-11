# G6A Step 1 — exact Wi-Fi Direct identity comparison (STATIC ONLY)
Date: 2026-10-11. No new glasses run, diagnostic APK or association.

## Grounded sources
- Previously byte-for-byte verified Cyan `base.apk` and six DEX entries; call-site trace: `docs/research/G6A_CYAN_EXACT_DEX_WIFI_IDENTITY_CALLSITE_TRACE_2026-10-11.md`.
- Frozen K v0.4.0 MainActivity, v0.6.7 MainActivity, v0.6.8.5 MainActivity and DiagnosticSafetyRules, v0.6.8.6 MainActivity and DiagnosticSafetyRules, read from GitHub main on 2026-10-11.
- Physical results: 2026-09-19 G4A/G4A2; 2026-10-11 v0.6.8.5/.6.
- Independent Android Wi-Fi Direct documentation: https://developer.android.com/develop/connectivity/wifi/wifi-direct (the official example uses WifiP2pConfig.deviceAddress + WpsInfo.PBC; it says Wi-Fi Direct devices need not explicitly request group password).
- This turn cross-checked repository source and prior exact-DEX report; it did not claim a fresh raw DEX disassembly, and did not access private credentials or peer names.

## Construction and comparators
**Cyan BLE connected branch** (previous exact-DEX call-site proof):
- `MyBluetoothReceiver.connectStatue` obtains a Bluetooth name/address (and documented fallback), stores Bluetooth device info.
- Constructs `wifiCandidate=(last underscore-separated name segment, if underscore exists; else full name) + "_" + BluetoothDevice.getAddress().replace(":","")`.
- Invokes `UserConfig.setGlassDeviceWifiName` on two code paths; delegate backed by MMKV `app_config`.
- In `PictureFragment.onPeersChanged` compares `WifiP2pDevice.deviceName` to `UserConfig.getGlassDeviceWifiName`, ignoring case. Alternative `endsWith(peerName, UserConfig.getDeviceAddressNoClear())` reads raw stored Bluetooth address in examined setter branch; the suffix comparator does NOT normalize in that method. The live success of this *alternate* branch is not proved.

**K v0.4.0** (source method lines 221-228, 258-278):
- Parses a long `0x41` ENTER response to hold an exact SSID and password in memory, with no persistence.
- Peer discovery starts only after expected name exists. `WifiP2pDevice.deviceName.equalsIgnoreCase(expectedP2pName)` selects peer; Android `WifiP2pConfig` sets `deviceAddress=d.deviceAddress`, `wps.setup=WpsInfo.PBC`, `groupOwnerIntent=0`. No parsed password is passed to the Android P2P config.
- G4A proved this historical flow with one exact BLE-reported name and formed group.

**K v0.6.7** (MainActivity lines 392-410, 458-465):
- `maybeAdvanceAfterEnter` requires successful ENTER callback plus credential-bearing frame AND nonempty in-memory SSID/password before starting discovery. The missing long frame is consequently a direct application-state-machine blocker.
- It is NOT established from this that the glasses never advertised, or that Android would refuse association absent a parsed password.

**K v0.6.8.5** (MainActivity lines 602-607; `DiagnosticSafetyRules` 16-23):
- `BluetoothDevice.getAddress().replace(":","").toUpperCase(Locale.US)` gives full 12-hex `pairedAddressSuffix`.
- `matchesCyanPeer` calls suffix-only matcher, case-insensitive and trimmed; exact stored-name OR branch is **absent**, despite broad report header.
- Physical result: one peer, zero suffix match. No Android connect/group attempted.

**K v0.6.8.6** (MainActivity lines 321-335, 548; `DiagnosticSafetyRules` 9-20):
- `classifyPeerName` marks full suffix FIRST, generic `AIMB-G1` prefix SECOND, otherwise OTHER; categories are mutually exclusive.
- Physical result: one peer with nonempty name, category OTHER; no valid `0x73` events, no Android connect/group attempted; safe EXIT and group absence.

## Counterfactual test (synthetic names only, no owner device identifiers)
Let Bluetooth name `AIMB-G1_EXAMPLE`, normalized Bluetooth address `AABBCCDDEEFF` (the colon-free form).
The verified Cyan formula produces `EXAMPLE_AABBCCDDEEFF` — K v0.6.8.6 classifies it FULL-SUFFIX, **not OTHER**.
With `AIMB-G1_ALPHA_BETA` Cyan formula produces `BETA_AABBCCDDEEFF` and still FULL-SUFFIX.
A name `EXAMPLE_AABBCCDDEE01` constructed from a different Bluetooth address is OTHER to K's normalized paired address `AABBCCDDEEFF`.
Evaluated these predicates in an isolated pure-JavaScript synthetic truth table; three assertions passed. This validates comparator logic only, **not current hardware**.

## Resolved source-level questions
1. Constructing a candidate name does **not universally require fresh long `0x41` credentials**; Cyan has a Bluetooth connection path doing so.
2. Requiring the parsed nine-character password for K's WPS-PBC `WifiP2pConfig` was an **application prerequisite**, not an Android API parameter requirement.
3. A generic model prefix is neither necessary nor sufficient to identify the glasses.
4. Cyan's stored exact-name OR predicate and raw-address suffix fallback differ from K's suffix-only matcher; don't claim exact parity.
5. The v0.6.8.6 `OTHER` result cannot be explained solely by removal of the model prefix, conditional on the same name-formula and Bluetooth address.

## STILL OPEN / red-team
- The actual observed OTHER peer may be unrelated, stale/cached, use another address basis, or advertise a different name; evidence cannot choose among these alternatives.
- `WifiP2pDevice.deviceAddress` is an Android P2P interface address used to initiate connection and must NOT be presumed equal to the Bluetooth address embedded in an advertised name.
- A candidate peer-name equality is evidence of identity consistency, **not** cryptographic peer authentication or proof of group membership. Avoid arbitrary or sole-peer connection.
- The current 5-byte `0x41` frame meaning is unknown. Code design may omit dependence on its content but cannot declare it success.
- No change to source and no physical run authorized. Future proposal would require exact current peer-name comparison and fail-closed group membership, before anything involving media.

**Gate: G6A OPEN / G6B BLOCKED.**
