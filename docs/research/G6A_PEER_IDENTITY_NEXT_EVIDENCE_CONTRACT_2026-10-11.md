# G6A — trustworthy peer-identity evidence contract (research-only)
Date 2026-10-11. Produced in parallel with Step 3 offline crash-safety investigation. No new APK, BLE write, discovery run or real connection authorized.

## Existing evidence
1. Historical G4A success used exact in-memory Wi-Fi SSID received from a credential-bearing BLE response, matched via WifiP2pDevice.deviceName.
2. Original Cyan DEX computes its own Wi-Fi candidate from Bluetooth name and colon-stripped address on connect, saves in UserConfig MMKV, and matches exact case-insensitive name OR its raw saved Bluetooth-address suffix fallback.
3. K v0.6.8.6 classified one Android peer OTHER (neither model prefix nor paired normalized 12-hex suffix). No P2P connect/group attempted, so that peer's identity is unknown.
4. Conditional logic check: if Cyan's generated name used the same BLE address as K v0.6.8.6, its name would have matched the 12-hex suffix class. Therefore merely restoring exact-name comparison cannot by itself explain the observed OTHER category.

## Minimum identity acceptance contract before any future *separately authorized* connection
- Source exact Bluetooth target from a unique bonded AIMB-G1 family device; Bluetooth address must be readable, valid and scoped to that target, with no raw logging.
- Independently recompute precisely the Cyan-derived expected P2P name **in memory** (last underscore segment if applicable; no fabricated fixed model prefix; colon-stripped BLE address).
- A candidate Wi-Fi P2P peer must uniquely satisfy a strong, exact device-specific equality rule; generic family name, peer count, proximity, and fuzzy substrings never qualify.
- Suffix-only evidence is weaker than a trusted exact derived name and cannot establish actual hardware identity by itself.
- If any peer is OTHER under suffix classification, its origin cannot be inferred. Abort and classify unknown; do not connect to inspect contents.
- If an exact peer is later observed, a separate reviewed association can verify group membership against that exact P2P peer and proper 0x73/08 readiness. No 0x41 short-frame semantic guess may substitute.
- Group cleanup must preserve pre-existing/unattributed groups; force-stop and callback timeouts remain fail-closed.

## Remaining uncertainties
- Android P2P interface deviceAddress is distinct from bonded BLE address; do not equate the two.
- The same Bluetooth bonded address may not correspond to the actual P2P advertising-name suffix used by present firmware.
- Android discovery callbacks may be stale or incomplete. A not-found result is bounded inconclusive.
- No authenticated cryptographic pairing or vendor attestation from an exact name is proven.
- Device-specific peer name/address and credentials must never be copied into GitHub reports.

## Gate
Static research contract complete; current peer identity NOT resolved. This is not authorization to build a peer probe or connect to the unknown peer.
G6A OPEN; G6B BLOCKED.
