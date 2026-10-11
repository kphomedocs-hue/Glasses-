# G6A Cyan stored Wi-Fi name — exact-byte forensic blocker
Date: 2026-10-11
Scope: static-only forensic continuation. No device write, diagnostic APK, or physical test.

## Checks performed
1. Checked the accessible runtime for the Cyan Glasses split APK / Cyan base APK. Neither was available. The located APKs were K-made v0.6.8.5 and v0.6.8.6 diagnostics, not Cyan.
2. Queried repository code search for likely concrete stored-name getter/setter names; no implementation hit. The repository intentionally does not store Cyan APK bytes or bulk decompiled third-party code.
3. Retained authoritative Cyan package fingerprints from `docs/research/CYAN_PROVENANCE.md`:
   - Cyan 1.0.2.18_20260811 split APK ZIP: SHA-256 `1328b3c025f43c06b2a0674d4c17890ec4b76cb6487196a84aa27b2335c2fc49`.
   - Extracted base APK: SHA-256 `1e700628d76fa4fa84047632e2ccce3673f1a01966fbf8c583985568f3aaaf64`.
4. Reviewed `docs/research/G6A_CYAN_WIFI_IDENTITY_PROVENANCE_AUDIT_2026-10-11.md`, `docs/research/CYAN_P2P_ROOT_CAUSE_RECHECK_2026-10-08.md`, G3B and G4A/G4A2 physical controls, and the v0.6.8.5/v0.6.8.6 inconclusive reports.

## Proven vs unresolved
- Proven: historical exact peer association used the BLE 0x41 credential-bearing SSID in memory; the Cyan peer predicate can also compare against a separately stored Wi-Fi name or a full BLE-address suffix.
- Proven: v0.6.8.6 observed one nonempty-name OTHER Wi-Fi peer, not a validated identity.
- Unresolved: exact source and persistence lifetime of Cyan's stored name, setter/getter chain, whether it can exist without a fresh credential frame, and any independent validated way for K to obtain a current exact name.
- Unknown: whether the visible OTHER peer was the glasses. Do not infer identity from the one-peer count, generic model names, or name-length coincidences.

## Required next input to close bytecode provenance
Obtain the **same user-exported Cyan split package** (or extracted base APK) from the owner, check the corresponding SHA-256, and inspect only focused methods:
1. Stored Wi-Fi name field/accessors and storage preferences/database reads and writes.
2. Assignment from BLE credential callback to the stored name.
3. Device-information callbacks or read-only configuration paths that may populate the name.
4. Exact Wi-Fi Direct peer-matching condition and fallback rules.
Record class/method evidence, paths, and hashes; never publish credentials, full device names, addresses, or third-party APK bytes in the public repository.

Do not substitute an unrelated downloaded Cyan version and call it equivalent. Do not implement arbitrary-peer association or a third near-identical physical scan.

**Gate: G6A OPEN; G6B BLOCKED. No physical APK authorized.**
