# G6A — Cyan Wi-Fi Direct identity provenance audit
Date: 2026-10-11
Status: static research only; no new APK / physical authorization

## Investigated exact evidence
- `docs/research/CYAN_P2P_ROOT_CAUSE_RECHECK_2026-10-08.md` derived from Cyan 1.0.2.18_20260811, split APK SHA-256 `1328b3c025f43c06b2a0674d4c17890ec4b76cb6487196a84aa27b2335c2fc49`.
- `docs/research/CYAN_PROVENANCE.md`, `docs/protocol/AIMB_G1_PROTOCOL.md`.
- Exact K `source/v0.4.0/`, `source/v0.6.7/app/src/main/java/com/parkarsite/g6aobserver67/MainActivity.java`.
- G3B, G4A and G4A2 historical physically confirmed evidence (2026-09-19).
- Exact v0.6.8.5 and v0.6.8.6 physical reports on 2026-10-11.

## Concrete provenance chain established
1. Historical BLE P2P ENTER `0x41 / 02 01 04 01` returned a CRC-valid credential-bearing frame, containing a 20-byte SSID and 9-byte password, as confirmed in G3B and G4A. Values intentionally not stored in public GitHub.
2. K v0.4.0 and v0.6.7 consumed that frame in memory via `parseTransferCredentials()`; the code assigned `expectedP2pName` from the SSID bytes, and Android `requestPeers()` compared `WifiP2pDevice.deviceName` against that exact string. This path physically succeeded in G4A/G4A2.
3. Earlier exact-Cyan APK static research documented an **OR** peer predicate: case-insensitive equality with the **stored glasses Wi-Fi name** OR a peer name ending in the bonded BLE address without separators.
4. Cyan also retains glasses Wi-Fi name/password/IP fields, but the frozen public evidence does not identify the precise storage-write method, key, lifetime, and every possible data source for that stored name. It does **not** show that K can read Cyan's private app storage.
5. K v0.6.8.5 implemented only the suffix branch (despite its overbroad report text) and never connected. K v0.6.8.6 classified its sole visible peer `OTHER` (nonempty name; neither AIMB-G1 prefix nor full BLE suffix). Both returned verified cleanup and an inconclusive identity result.
6. Recent 0x41 short five-byte response is not the old credential-bearing response. No previously verified, separate read-only BLE call that produces the trustworthy *current* P2P name has been established.

## Epistemic limits
- **Cyan persisted the name** is supported only to the extent that its researched match reads a stored Wi-Fi name; a precise preference/database provenance trace is still missing.
- The one `OTHER` peer **cannot** be assigned to or excluded from the glasses based on category/count.
- The 20-byte historical SSID length and BLE name prefix are insufficient to reconstruct a current identity; no guess, substring fallback, candidate hash, or unsafe connection is justified.
- Do not presume Cyan can export its stored name to another application. Android app-private preferences/databases are normally isolated.
- The previous v0.6.8.5 early 0x73 event did not recur in v0.6.8.6; its meaning is unresolved.

## Next *static-only* work to close missing provenance
A. Obtain the exact, user-supplied Cyan APK **if still locally available**; verify its SHA-256 matches the archived provenance before inspecting. The public GitHub repository intentionally excludes third-party binary bytes.
B. Trace concrete writes/reads of the stored Wi-Fi name through Cyan's internal UserConfig / preferences model (field setter, event/callback assignment, storage key, startup reload) and the WifiP2pManager peer predicate. Distinguish: credential response -> cache; persistent reload; device-info response; other mechanisms. Record decompiled class and method names, not secrets or raw strings.
C. If the exact app cannot be made available, restrict conclusions to the frozen earlier static audit. Neither historical name values nor nearby peer-category evidence suffice for safe association.
D. Before any new physical build, independently review whether an already-approved, read-only BLE command can supply a *trusted exact* current Wi-Fi name. No reset, retry, AP mode, arbitrary peer fallback, or automatic association.

## Current decision
Static provenance unresolved. No more near-identical peer-discovery diagnostic justified yet.
G6A OPEN; G6B BLOCKED; no new physical APK authorized.
