# K G1 P2P Lifecycle Probe v0.3.1 — G3B

Purpose: validate a bounded P2P transfer-mode enter/exit lifecycle over BLE before enabling any phone-side Wi-Fi Direct or media transfer.

## Exact current Cyan evidence

Physical G3 selected P2P:
- configFileType = 1
- onlySupportApImport = false

Exact Cyan app payloads:
- `PictureFragment.importAlbum()` → `02 01 04 01`
- `PictureFragment.fileDownloadComplete()` → `02 01 09`

## What this app does

1. Connect to exactly one bonded AIMB-G1-family device.
2. Subscribe to the confirmed Cyan response characteristic.
3. Send exactly one P2P enter frame:
   `BC 41 04 00 93 5C 02 01 04 01`
4. Observe BLE only for 8 seconds.
5. If the Android enter-write callback succeeded, send exactly one exit frame:
   `BC 41 03 00 11 96 02 01 09`
6. Observe BLE only for 8 seconds.
7. Disconnect.

## Deliberately not implemented

- no Android WifiP2pManager,
- no WifiManager,
- no AP-mode payload `02 01 04 02`,
- no Internet/network/socket/HTTP code,
- no media listing/download,
- no file deletion/modification,
- no reset/restart/OTA,
- no arbitrary command input,
- no proprietary-write retry loop.

## Physical test

1. Force-stop Cyan Glasses.
2. Keep AIMB-G1 paired.
3. Turn Bluetooth and the glasses on.
4. Open K G1 P2P Lifecycle Probe.
5. Tap **Run bounded P2P lifecycle** once.
6. Do not use Cyan or manually connect Wi-Fi during the test.
7. Let both observation phases finish.
8. Copy/share the complete report.
