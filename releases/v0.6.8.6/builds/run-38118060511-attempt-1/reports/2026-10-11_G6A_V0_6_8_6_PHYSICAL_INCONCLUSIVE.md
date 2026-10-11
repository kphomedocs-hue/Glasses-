# G6A v0.6.8.6 — supervised passive diagnostic
Generated 2026-10-11T12:04:39+0530. Exact source commit `90c2acb4f8736acf6ff024bfa3ba660ead09dad9`; run `38118060511`; attempt `1`.

## Sanitized operator-provided report
```text
K G1 G6A PASSIVE PEER IDENTITY REPORT
Generated: 2026-10-11T12:04:39+0530
App version: 0.6.8.6
Build commit: 90c2acb4f8736acf6ff024bfa3ba660ead09dad9
Build run: 38118060511
Build attempt: 1
Scope: research-only; G6A stays OPEN and G6B BLOCKED
BLE writes allowed: at most one ENTER and one EXIT attempt
0x73: CRC-valid event ID buckets pre-ENTER, pending-ENTER-callback, post-ENTER-callback; no payloads
Passive identity classification only; no Wi-Fi Direct connection
Peer categories: missing-name, AIMB-G1-family prefix, exact 12-hex BLE suffix, other; aggregate only
Credential parsing and storage: NONE; INTERNET permission: ABSENT
P2P connect, cancel-connect, group removal, HTTP/socket/media/capture/reset/retry: 0 by design

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
Peer snapshot 1: total=0; name missing=0; family prefix=0; paired full suffix=0; other=0 (no names/addresses)
Peer snapshot 2: total=1; name missing=0; family prefix=0; paired full suffix=0; other=1 (no names/addresses)
DIAGNOSTIC STOP: Cyan-matching P2P peer not found within 16 seconds.

EXIT TRANSFER MODE
Command: 0x41 / 02 01 09; at most one attempt
EXIT write: STARTED
EXIT callback: SUCCESS

BOUNDED TERMINAL CLEANUP
stopPeerDiscovery: SUCCESS
Cleanup group inspection: ABSENT
First post-cleanup P2P group absent: true
Final post-cleanup P2P group absent: true

SUMMARY
Passive observation snapshots stored: 2; extra callbacks not listed: 0
Last peer categories: total=1, missing=0, family=0, full BLE suffix=0, other=1
Wi-Fi Direct connect/cancel/remove requests: 0
ENTER attempt: true; callback success: true
EXIT attempted: true; started: true; callback success: true
Pre-ENTER CRC-valid 0x73 IDs: none; overflow=0
Pending ENTER-callback CRC-valid 0x73 IDs: none; overflow=0
Post-ENTER callback CRC-valid 0x73 IDs: none; overflow=0
HTTP/socket/media/capture/reset operations: 0
Credential values logged/persisted: NO
Confirmed group absent after cleanup: true
Cleanup issue: false
CLEANUP RESULT: VERIFIED
G6A RESULT: INCONCLUSIVE — Cyan-matching P2P peer not found within 16 seconds.
G6A GATE: OPEN; G6B GATE: BLOCKED
END REPORT
```

## Interpretation
The one observed Wi-Fi Direct peer was classified OTHER: it had a nonempty name, but neither the AIMB-G1 family prefix nor the exact paired 12-hex BLE suffix. This category is not proof that it was or was not the glasses. No connect/group formation was attempted. No 0x73 event occurred in any observation bucket, in contrast to one pending ENTER event in v0.6.8.5. EXIT succeeded; cleanup was verified twice. The terminal reason is inherited from v0.6.8.5 and incorrectly says "Cyan-matching P2P peer not found" in a passive observer; this is wording, not a connection attempt. Classification INCONCLUSIVE. Do not repeat unchanged. G6A OPEN; G6B BLOCKED.
