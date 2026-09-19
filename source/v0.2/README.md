# K G1 Response Probe v0.2 — G2 NOTIFICATION ONLY

Purpose: first G2 physical diagnostic after G1 confirmed the Cyan-family GATT profile on the AIMB-G1.

This app deliberately narrows the G2 action to one operation that was prohibited during G1 but is required to observe the response channel:

- connect to exactly one bonded `AIMB-G1` / `AIMB-G1_*` device,
- discover the physically confirmed Cyan service,
- enable local notifications on `de5bf729-d711-4e47-af26-65e3012a5dc7`,
- write the standard Client Characteristic Configuration Descriptor (CCCD, UUID 0x2902) with the standard enable-notification value,
- observe notifications for 30 seconds,
- disconnect.

## Deliberately not implemented

- no write to the proprietary Cyan control characteristic,
- the `de5bf72a-...` write UUID is not present in source,
- no generic BLE command console,
- no Cyan frame/payload generator,
- no initialization/time command,
- no media-mode P2P/AP command,
- no Wi-Fi/P2P/AP APIs,
- no HTTP/network APIs,
- no pairing/unpairing/reset,
- no OTA/firmware operations.

## Test use

1. Force-stop Cyan Glasses.
2. Leave the AIMB-G1 paired; do not reset or unpair it.
3. Turn on the glasses and Bluetooth.
4. Open K G1 Response Probe.
5. Tap **Run notification-only G2 probe**.
6. Allow the subscription and 30-second observation to finish.
7. Copy/share the report for review.
8. Do not proceed to any control write until the G2 report is reviewed.
