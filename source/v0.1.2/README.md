# K G1 Discovery v0.1.2 — READ ONLY

Read-only diagnostic APK for AIMB-G1 BLE service discovery.

This revision preserves the v0.1 read-only safety boundary and adds diagnostic resilience for G1:
- 30-second explicit low-latency BLE observation window,
- accepts both `AIMB-G1` and `AIMB-G1_*`,
- recognizes the known Cyan service UUID when advertised,
- reports only aggregate nearby-device counts and sanitized candidate identity,
- inspects bonded-device metadata read-only,
- if and only if exactly one bonded AIMB-G1-family device exists, attempts a read-only LE GATT connection after a scan miss.

It does not pair/unpair/reset anything and contains no proprietary writes, notification subscription, Wi-Fi, HTTP, media-transfer, OTA or firmware commands.

## Safety boundary
The app can scan, connect, enumerate GATT services/characteristics, and read only selected standard Device Information Service fields that advertise READ. It contains no GATT write, descriptor write, notification subscription, Wi-Fi/network, media-transfer, reset, OTA, or firmware code.

## Expected use
1. Force-stop Cyan Glasses.
2. Turn on AIMB-G1 and keep it near the phone.
3. Install/open K G1 Discovery.
4. Tap **Run read-only diagnostic** and grant Nearby Devices permission.
5. Allow the 30-second observation/fallback sequence to complete.
6. Share or copy the report back to ChatGPT.

## Candidate Cyan profile checked
- Service: `de5bf728-d711-4e47-af26-65e3012a5dc7`
- Notify: `de5bf729-d711-4e47-af26-65e3012a5dc7`
- Write: `de5bf72a-d711-4e47-af26-65e3012a5dc7`

These are checked only for presence; the app never writes to them.
