# G6A v0.6.8.5 — supervised physical diagnostic (11 October 2026)

Exact APK: run 38116572973, attempt 1, source commit c38203f6e96044b7d36b6b6c12d1775079d750e9.
Generated on device: 2026-10-11T11:45:57+0530.

## Complete sanitized operator report

```text
K G1 G6A CYAN P2P STATE DISCRIMINATOR REPORT
Generated: 2026-10-11T11:45:57+0530
App version: 0.6.8.5
Build commit: c38203f6e96044b7d36b6b6c12d1775079d750e9
Build run: 38116572973
Build attempt: 1
Scope: research-only; G6A stays OPEN and G6B BLOCKED
BLE writes allowed: at most one ENTER and one EXIT attempt
0x73: CRC-valid post-ENTER callback IDs only; no payloads
Peer matching: Cyan name-or-paired-BLE-suffix predicate, in memory only
Credential parsing and storage: NONE; INTERNET permission: ABSENT
HTTP/socket/media/capture/reset/retry operations: 0 by design

BONDED TARGET CHECK
Bonded devices total: 7
AIMB-G1-family matches: 1
Bonded target: AIMB-G1_<suffix>
Addresses: NOT LOGGED

WI-FI DIRECT GROUP PREFLIGHT
Pre-existing group: ABSENT
GATT CONNECTION (before 16s Wi-Fi discovery window)
Target name: AIMB-G1_<suffix>
GATT: connected
CYAN SERVICE/NOTIFY/WRITE: PRESENT
Notification subscription write: STARTED
Notification subscription: SUCCESS

WI-FI DIRECT DISCOVERY
Discover before ENTER: TRUE
discoverPeers: SUCCESS

ENTER P2P MODE
Command: 0x41 / 02 01 04 01
No retry: TRUE
ENTER write: STARTED
ENTER write callback: SUCCESS
Peers seen: 0; Cyan matches: 0 (identities hidden)
Peers seen: 1; Cyan matches: 0 (identities hidden)
DIAGNOSTIC STOP: Cyan-matching P2P peer not found within 16 seconds.

EXIT TRANSFER MODE
Command: 0x41 / 02 01 09; at most one attempt
EXIT write: STARTED
EXIT callback: SUCCESS

BOUNDED TERMINAL CLEANUP
stopPeerDiscovery: SUCCESS
cancelConnect: NOT NEEDED
Cleanup group inspection: ABSENT
First post-cleanup P2P group absent: true
Final post-cleanup P2P group absent: true

SUMMARY
Peer matched: false
P2P group formed: false
ENTER attempt: true; callback success: true
EXIT attempted: true; started: true; callback success: true
Pre-ENTER 0x73 events ignored: 0
Pending-ENTER-callback 0x73 events ignored: 1
Post-ENTER callback 0x73 event IDs: none
HTTP/socket/media/capture/reset operations: 0
Credential values logged/persisted: NO
Confirmed group absent after cleanup: true
Cleanup issue: false
CLEANUP RESULT: VERIFIED
G6A RESULT: INCONCLUSIVE — Cyan-matching P2P peer not found within 16 seconds.
G6A GATE: OPEN; G6B GATE: BLOCKED
END REPORT
```

## Verified interpretation

One Wi-Fi Direct peer was observed but did not match the application's suffix-only matcher; its identity is unknown. No connect request or P2P group formation attempt followed. One valid 0x73 notification was counted while ENTER callback was pending, but its event ID was not saved; no inference about its meaning is justified. EXIT and group-absence cleanup passed. G6A OPEN; G6B BLOCKED. No unchanged repeat.
