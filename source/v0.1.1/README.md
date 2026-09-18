# K G1 Discovery v0.1.1 — READ ONLY

Read-only diagnostic APK for AIMB-G1 BLE service discovery.

This revision preserves the v0.1 safety boundary and broadens target-name recognition only: it accepts both `AIMB-G1` and names beginning `AIMB-G1_` (for example `AIMB-G1_ABCD`).

## Safety boundary
The app can scan, connect, enumerate GATT services/characteristics, and read only selected standard Device Information Service fields that advertise READ. It contains no GATT write, descriptor write, notification subscription, Wi-Fi/network, media-transfer, reset, OTA, or firmware code.

## Expected use
1. Force-stop Cyan Glasses.
2. Turn on AIMB-G1 and keep it near the phone.
3. Install/open K G1 Discovery.
4. Tap **Scan for AIMB-G1** and grant Nearby Devices permission.
5. Wait for **Discovery complete. Disconnected. Report ready.**
6. Share or copy the report back to ChatGPT.

## Candidate Cyan profile checked
- Service: `de5bf728-d711-4e47-af26-65e3012a5dc7`
- Notify: `de5bf729-d711-4e47-af26-65e3012a5dc7`
- Write: `de5bf72a-d711-4e47-af26-65e3012a5dc7`

These are checked only for presence; the app never writes to them.
