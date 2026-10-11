# G6A v0.6.8.5 — 11 October physical result and root-cause recheck

## Authority
- Physical report: `releases/v0.6.8.5/builds/run-38116572973-attempt-1/reports/2026-10-11_G6A_V0_6_8_5_PHYSICAL_INCONCLUSIVE.md`.
- Tested source: `c38203f6e96044b7d36b6b6c12d1775079d750e9`.
- Proven G4A/G4A2 results: `docs/testing/results/2026-09-19_G4A_P2P_ASSOCIATION_PASS.md` and `docs/testing/results/2026-09-19_G4A2_P2P_IP_NOTIFY_PASS.md`.
- Prior Cyan audit: `docs/research/CYAN_P2P_ROOT_CAUSE_RECHECK_2026-10-08.md`.

## Determinations
1. BLE/GATT, notifications, Wi-Fi Direct discovery start, and exactly one ENTER write/callback succeeded.
2. The discovered peer count went from zero to one. Zero peers satisfied the application's identity predicate. No `WifiP2pManager.connect` request was made and no group was formed; therefore Wi-Fi Direct association was not tested.
3. The app's report incorrectly claims a Cyan "name-or-paired-BLE-suffix" predicate. The exact code only calls `DiagnosticSafetyRules.matchesPairedPeerName()` and demands that the peer name end with the bonded BLE address encoded as twelve consecutive hex digits. This cannot be treated as Cyan's full alternative predicate.
4. G4A/G4A2 physically matched the exact P2P name carried by an earlier credential-bearing 0x41 response. The current short 0x41 does not supply that trusted name.
5. One CRC-valid 0x73 frame was counted between starting ENTER and receiving its successful BLE write callback; the current diagnostic intentionally discarded the event ID in this time bucket. It may be useful timing evidence, but its type is unknown.
6. EXIT write/callback, discovery stop and dual read-only group-absence checks all succeeded. Zero HTTP/media/capture/reset/retry operations were performed.
7. The sole observed peer cannot be assigned to AIMB-G1 by proximity or peer count. No evidence shows whether the glasses did or did not advertise or whether P2P connect would have worked.

## Next bounded investigation (proposal, not physical authorization)
- Reconstruct Cyan's alternate trusted-name predicate against frozen source, and determine whether a P2P name is available via already proven BLE evidence without requiring a credential frame.
- If no trustworthy name is available, use a **passive, classification-only** discriminator with no P2P connection. Record *only safe aggregate categories*: peer count, missing-name yes/no, full 12-hex bonded-suffix match yes/no, model-family-prefix match yes/no; never log raw names, addresses, credentials or 0x73 payloads.
- Count and categorize CRC-valid 0x73 event IDs separately for pre-ENTER, ENTER-write-pending and post-ENTER-write-callback timing windows. Do not assume an early 0x73/08 implies successful association.
- Preserve read-only pre-existing-group preflight, one ENTER, one EXIT, strict timeout budget, no group removal without proven test attribution, and dual post-cleanup verification.
- **Never connect to the sole visible peer based only on proximity, count, a generic prefix, or partial MAC coincidence.** Test only after another independent red-team review, verified archive, and explicit physical authorization.

## Gate decision
**v0.6.8.5: INCONCLUSIVE; one authorized run consumed; do not repeat unchanged. G6A OPEN; G6B BLOCKED.**
