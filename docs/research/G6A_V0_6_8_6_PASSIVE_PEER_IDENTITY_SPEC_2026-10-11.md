# AIMB-G1 G6A — v0.6.8.6 passive peer identity and early-event discriminator
**Date:** 2026-10-11  
**Status:** Research specification; no physical test approved.

## Current source of truth
- Exact v0.6.8.5 tested build: commit `c38203f6e96044b7d36b6b6c12d1775079d750e9`, run `38116572973`, attempt 1.
- Complete physical report: `releases/v0.6.8.5/builds/run-38116572973-attempt-1/reports/2026-10-11_G6A_V0_6_8_5_PHYSICAL_INCONCLUSIVE.md`.
- Primary Cyan investigation: `docs/research/CYAN_P2P_ROOT_CAUSE_RECHECK_2026-10-08.md` (frozen Cyan version/source hashes inside).
- Physical controls: `docs/testing/results/2026-09-19_G4A_P2P_ASSOCIATION_PASS.md` and `docs/testing/results/2026-09-19_G4A2_P2P_IP_NOTIFY_PASS.md`.

## Determination after direct cross-check
Cyan's peer predicate accepts (A) an exact case-insensitive match to **a stored glasses Wi-Fi name** or (B) a Wi-Fi Direct device name ending with the bonded BLE address with separators removed. v0.6.8.5 implemented B only but inaccurately described A OR B in its report.

Earlier G4A/G4A2 used exact P2P name from a **credential-bearing 0x41** and physically associated. The current device repeatedly emits a five-byte non-credential 0x41 instead. The sanitized repository has no reproducible, current device-specific P2P name that a new application can use independently without trusting that missing response. A historical SSID *length* of 20 bytes cannot identify a current peer. Cyan stores a P2P name within its own app context, but that is not proof the K app can access it. No independent safe read-only name query is currently documented.

Thus the v0.6.8.5 one-peer sighting is NOT proof that the peer was AIMB-G1. Its early CRC-valid 0x73 event is also not semantically identifiable because only the count was captured during ENTER-write-pending. Wi-Fi Direct association and readiness remain untested.

## Smallest next bounded discriminator — observation ONLY
1. One target: require exactly one bonded AIMB-G1-family BLE entry and required permissions.
2. Read-only Wi-Fi Direct group preflight; abort if a group exists or group state is indeterminate. Do not remove it.
3. Establish GATT notify and register Wi-Fi Direct receiver **before** the discovery budget.
4. Start one `discoverPeers` request, then exactly one previously verified `0x41/02 01 04 01` ENTER. 16-second discovery/observation deadline, no retry.
5. Poll/request peer list only on bounded Android change notifications and once after successful ENTER callback. Inspect peer names **in memory only**. Record bounded aggregate counts for: missing/empty name, generic AIMB-G1 family prefix, full 12-hex paired Bluetooth-address suffix match, and other; no names, partial suffixes, raw hashes, device IDs, or per-peer tokens.
6. Do **not** call `WifiP2pManager.connect`, `cancelConnect`, `removeGroup`, `requestConnectionInfo`, or any HTTP/network transfer method, even if one peer appears.
7. Capture CRC-valid `0x73` event IDs (one-byte types only, never full payloads) in separate time buckets: before ENTER requested; ENTER-write-attempted but callback pending; after successful ENTER callback. Cap observed events to avoid unbounded memory/log output. These are temporal observations, not success proofs.
8. After a started ENTER, attempt exactly one already-proven `0x41/02 01 09` EXIT; if ENTER's BLE write slot is pending, wait boundedly and record failure if it never settles.
9. Stop discovery with independent deadline, perform **two** read-only group-absence verifications. Any unexpected group must be reported, never deleted. Separate primary observation from cleanup status. OS process death still prevents guaranteed cleanup.
10. Export build commit/run/attempt, operation counters, aggregate categories, safe event-ID buckets, deadlines, EXIT and cleanup results. No raw identifiers.

## Interpretation matrix
- One generic family-prefix peer, zero suffix matches: peer-name class observed, **identity remains unproven**.
- One peer with no name: Android name unavailable; inconclusive.
- One full paired-address-suffix match: stronger identity clue, **still not proof of association** because no connect is allowed in this version.
- A pending-ENTER event ID such as 0x0B: early BLE event timing evidence only; not credential or connection proof.
- No peer: inconclusive; Android scanning/callback timing and glasses visibility remain alternatives.
- Any cleanup uncertainty: fail closed, prohibit rerun pending inspection.

## Red-team boundaries
- Static audits must assert **zero** `connect(`, `cancelConnect(`, `removeGroup(`, HTTP/socket, media, credential parser, reset, retry or AP fallback APIs; also no INTERNET permission.
- Verify single ENTER/single EXIT paths, bounded timers, no group deletion, no raw names/MAC/IP/SSID/password logging, no generic peer selection, no unbounded event collection.
- Compile, Android Lint and signature/provenance checks are necessary but not physical proof.
- Do not reuse the failed v0.6.8.5 APK. A later v0.6.8.6 exact-build prepromotion review is mandatory.

**Gate:** G6A OPEN, G6B BLOCKED. No physical APK authorized by this specification.
