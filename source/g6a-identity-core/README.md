# G6A source-derived peer identity research (offline only)

This self-contained Java 17 reference model compares synthetic Wi-Fi Direct peer names against the historically verified Cyan Glasses 1.0.2.18 Bluetooth-derived *candidate* Wi-Fi name. It **never authenticates a peer, selects a Wi-Fi P2P device address, makes a network request or initiates a connection**.

## Basis
Exact Cyan DEX work recorded in `docs/research/G6A_CYAN_EXACT_DEX_WIFI_IDENTITY_CALLSITE_TRACE_2026-10-11.md`. Its BLE connection handler derives the configured name from the final underscore-separated Bluetooth-name segment (or full name) plus underscore plus colon-stripped Bluetooth address. The config is saved with MMKV; `PictureFragment` uses case-insensitive equality with this saved name, with an alternate raw-address suffix predicate in the examined path.

K v0.6.8.5 matched **normalized full Bluetooth-address suffix only**; v0.6.8.6 classified one unknown peer as OTHER. Therefore removing the model prefix alone cannot explain v0.6.8.6's missed full-suffix match if both apps used the same Bluetooth address.

## Candidate classifications
- EXACT_DERIVED_CANDIDATE (derived name equality, ignoring case)
- NORMALIZED_SUFFIX_CANDIDATE (full 12-hex address suffix, but not exact name)
- RAW_ADDRESS_SUFFIX_RESEARCH (separate historic Cyan alternate predicate; not equivalent to normalized suffix)
- GENERIC_FAMILY_ONLY (not sufficient evidence)
- OTHER (cannot be identified from available name evidence)
- EMPTY_NAME

`assess` gives counts and one of INVALID_BONDED_INPUT / NO_CANDIDATE / SINGLE_UNVERIFIED_CANDIDATE / AMBIGUOUS_CANDIDATES. It returns **no P2P name, Bluetooth address, raw peer address, username or credentials**. `authenticated()` and `connectAuthorized()` are always false.

## Local tests
Run `bash source/g6a-identity-core/run_selftest.sh` with Java 17. The tests use fabricated identity strings only and verify no auto-selection for a generic prefix or sole OTHER peer, exact vs suffix semantics, malformed addresses, duplicates, ambiguities, case behavior and redaction.

## Integration limits
The source is a **reference policy**, not exact Cyan Kotlin bytecode or proven device identity. Cyan may use a saved fallback name/another active address; a forged or stale Wi-Fi name can satisfy a name comparator. The P2P peer interface address is not assumed to be equal to the bonded BLE address. Group membership and a BLE `0x73/08` correlated to the formed group were seen only in historical, separate tests; no current association is proven. Android scan timing, permissions and cleanup are outside this Java module.

**No live APK or physical run authorized.** G6A OPEN; G6B BLOCKED.

## 2026-10-11 retry — direct comparison against historical K source

Run `bash source/g6a-identity-core/run_selftest.sh`: it now compiles the **unchanged** v0.6.8.5 and v0.6.8.6 `DiagnosticSafetyRules.java` files alongside this reference library. A new cross-version regression demonstrates that a peer name ending in a **colon-formatted paired BLE address** would be OTHER in v0.6.8.6 (or fail the v0.6.8.5 normalized-suffix test), yet satisfy the separate raw-address predicate previously traced in original Cyan. This is a hypothetical case, **not the actual unlogged peer's identity**.

GitHub Java 17 [run 38127615703](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38127615703): PASS, 54 module + 8 frozen v0.6.8.5 + 15 frozen v0.6.8.6 = 77 synthetic assertions. Hygiene [run 38127615701](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38127615701): PASS. See `docs/research/G6A_STEP4_RETRY_ORIGINAL_DEX_RAW_ADDRESS_BLIND_SPOT_2026-10-11.md`.

Even one raw-suffix match remains UNVERIFIED and does not authorize Android Wi-Fi association. No physical test or new APK was performed.
