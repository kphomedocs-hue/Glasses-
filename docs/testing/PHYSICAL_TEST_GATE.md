# Physical AIMB-G1 Test Gate

## Gate 1 — current approved test

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

## Gate 2 — only after Gate 1 review

Potential next diagnostic:
- subscribe to the known response path,
- perform only required initialization,
- allow one explicitly allow-listed media-mode test command,
- record the response,
- stop.

No automatic media download at Gate 2.

## Gate 3

Read-only media listing over the glasses' local network.

## Gate 4

Download one disposable photo and save it as `0001.jpg`.

## Gate 5

Enable automatic new-media sync for JPG/MP4/OPUS with numbering, dedup, retry and integrity checks.
