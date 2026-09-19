# Physical AIMB-G1 Test Gate

This document defines what may be done to the physical glasses at each current gate.

## G1 — physical GATT confirmation

Status: **PASS — 2026-09-19**.

Evidence:
`docs/testing/results/2026-09-19_G1_PHYSICAL_DISCOVERY_PASS.md`

Physically confirmed:
- service `de5bf728-d711-4e47-af26-65e3012a5dc7`,
- notify characteristic `de5bf729-d711-4e47-af26-65e3012a5dc7`,
- write characteristic `de5bf72a-d711-4e47-af26-65e3012a5dc7`,
- hardware revision `AM01SPG1_V1.4`.

## G2 — notification response-channel confirmation

Status: **PASS — 2026-09-19**.

Evidence:
`docs/testing/results/2026-09-19_G2_NOTIFICATION_ONLY_PASS.md`

The probe:
- connected to exactly one bonded AIMB-G1-family device,
- enabled notifications on the confirmed notify characteristic,
- wrote only the standard CCCD enable-notification value,
- sent no proprietary characteristic command,
- observed three valid spontaneous `0x73` frames.

## G2B — current approved physical diagnostic

Current method: **passive event correlation only**.

A future G2B Event Correlator may:
- connect to exactly one bonded AIMB-G1-family device,
- subscribe only to `de5bf729-d711-4e47-af26-65e3012a5dc7`,
- perform the standard CCCD enable-notification write,
- timestamp every valid notification,
- validate envelope length and CRC,
- allow the user to insert timestamped event markers,
- group repeated frames/payloads,
- export a sanitized report.

It must not:
- call a BLE characteristic-write API,
- include or use `de5bf72a-d711-4e47-af26-65e3012a5dc7`,
- send a Cyan control frame,
- send initialization/time data,
- enter media mode,
- activate Wi-Fi/P2P/AP,
- use HTTP/media transfer,
- pair/unpair/reset the device,
- perform OTA/firmware operations,
- provide a generic raw BLE console.

### Event-correlation discipline

Use safe, observable physical actions only. Mark the event in the app at the time it happens and repeat the same action several times.

Do not assign semantic meaning from one coincidental packet. A proposed mapping must show repeatability and should be checked against Cyan static evidence.

See:
`docs/testing/G2B_PASSIVE_CORRELATION_PLAN.md`

## G3 — first control write

Status: **BLOCKED**.

No proprietary characteristic write is authorized until G2B evidence has been reviewed and one explicit allow-listed command has been documented.

## Later gates

- G4: local network + read-only media listing.
- G5: one disposable media download.
- G6: automatic media sync.
- G7: hardening.

No destructive maintenance command is part of the roadmap.
