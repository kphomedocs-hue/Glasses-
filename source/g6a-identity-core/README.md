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
