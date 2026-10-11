# G6A v0.6.8.6 — Passive Peer Identity Diagnostic (SOURCE CANDIDATE)
Derived conservatively from v0.6.8.5 after its inconclusive physical run, with all Wi-Fi Direct connection, pending-connect cancellation, group removal, HTTP and media operations removed.
The diagnostic **only** reads peer-name categories in memory and records bounded CRC-valid 0x73 event IDs in pre-ENTER, pending-ENTER-callback and post-ENTER-callback buckets.
One BLE ENTER, one EXIT, 16-second discovery, independent cleanup deadlines, read-only group preflight and two group-absence checks.
No raw device names, Bluetooth addresses, peer addresses, network addresses, credentials, payloads, media or per-peer tokens in reports.
A model prefix or one visible peer NEVER authorizes association.
Research-only; static and CI verification required before physical review. G6A OPEN / G6B BLOCKED.
