# K G1 Init Probe v0.2.1 — G2C SINGLE COMMAND

Purpose: physically validate the first proprietary command that the user's exact Cyan Glasses app queues after GATT service discovery.

Exact trace:
`docs/research/CYAN_EXACT_G2B_TRACE_2026-09-19.md`

## What this app does

- connects to exactly one bonded `AIMB-G1` / `AIMB-G1_*` device,
- discovers the physically confirmed Cyan service,
- enables notifications on `de5bf729-d711-4e47-af26-65e3012a5dc7`,
- writes the standard CCCD enable-notification value,
- constructs Cyan's exact dynamic nine-byte time payload,
- sends exactly one command-`0x40` frame through the confirmed Cyan write characteristic,
- records the Android characteristic-write status,
- observes responses for 12 seconds,
- disconnects.

## Exact time-payload parity

The implementation mirrors the exact Cyan/Oudmon `SyncTime` logic:

- phone time plus one second,
- BCD year/month/day/hour/minute/second,
- Cyan locale-to-language code,
- Cyan timezone encoding,
- trailing `0x01` byte,
- CRC-16/MODBUS over the nine-byte payload,
- `BC | 40 | length | CRC | payload` frame.

## Deliberately not implemented

- no other proprietary command,
- no glasses-control/media command,
- no media-mode payload,
- no Wi-Fi/P2P/AP API,
- no HTTP/network API,
- no pairing/unpairing/reset,
- no restart/OTA/firmware operation,
- no generic raw command field,
- no retry of the proprietary write.

## Physical test

1. Force-stop Cyan Glasses.
2. Keep AIMB-G1 paired; do not reset or unpair.
3. Turn on Bluetooth and the glasses.
4. Open K G1 Init Probe.
5. Tap **Run single-command G2C probe**.
6. Allow the response window to finish.
7. Copy/share the complete report.
8. Do not send any media-mode command until the report is reviewed.
