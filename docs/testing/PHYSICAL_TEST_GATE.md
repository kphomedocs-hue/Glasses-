# Physical AIMB-G1 Test Gate

## Gate 1 — completed

Use **K G1 Discovery v0.1** only.

Allowed:
- Android Bluetooth/Nearby Devices permission
- scan for AIMB-G1
- BLE connect
- GATT service discovery
- enumerate characteristics and properties
- read selected standard Device Information fields when explicitly readable
- save/share a diagnostic report
- disconnect

Not allowed in Gate 1:
- proprietary characteristic writes
- descriptor writes for notifications
- media-mode command
- Wi-Fi/P2P/AP activation
- HTTP requests
- reset/restart/OTA/firmware actions

## Expected physical evidence

Confirm whether AIMB-G1 exposes:

```text
de5bf728-d711-4e47-af26-65e3012a5dc7  service
de5bf729-d711-4e47-af26-65e3012a5dc7  expected notify
de5bf72a-d711-4e47-af26-65e3012a5dc7  expected write
```

The report must also retain all discovered services/characteristics/properties so an unexpected profile can be analysed safely.

## Gate 1 result

**PASS — 2026-09-19.**

The physical AIMB-G1 exposed the expected Cyan service, notify and write characteristic UUIDs. Sanitized evidence is stored at `docs/testing/results/2026-09-19_G1_PHYSICAL_DISCOVERY_PASS.md`.

## Gate 2 — current approved diagnostic

First G2 probe:
- connect to the uniquely identified bonded AIMB-G1-family device,
- subscribe only to the physically confirmed `de5bf729-d711-4e47-af26-65e3012a5dc7` notification path,
- observe for spontaneous notifications for a bounded interval,
- record raw response bytes locally for review,
- disconnect.

Not allowed in the first G2 probe:
- characteristic writes to `de5bf72a-...` or any other proprietary characteristic,
- initialization/time command unless separately reviewed after the notification-only result,
- media-mode command,
- Wi-Fi/P2P/AP,
- HTTP/media transfer,
- reset/restart/OTA/firmware operations.

No automatic media download at Gate 2.

## Gate 3

Read-only media listing over the glasses' local network.

## Gate 4

Download one disposable photo and save it as `0001.jpg`.

## Gate 5

Enable automatic new-media sync for JPG/MP4/OPUS with numbering, dedup, retry and integrity checks.
