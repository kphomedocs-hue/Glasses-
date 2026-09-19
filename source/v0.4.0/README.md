# K G1 P2P Association Probe v0.4.0 — G4A

Purpose: prove phone-side Wi-Fi Direct discovery/association to the glasses without making any HTTP/socket/media request.

## Safety boundary

Allowed:
- exact confirmed BLE P2P enter command `0x41 / 02 01 04 01`, once,
- parse returned SSID/password only in memory,
- never log/persist credential values,
- Wi-Fi Direct discovery,
- exact peer-name matching against the BLE-reported SSID only,
- one Wi-Fi Direct connect request using WPS PBC + groupOwnerIntent=0,
- sanitized connection/group metadata,
- exact transfer exit `0x41 / 02 01 09`, once,
- remove temporary P2P group.

Forbidden:
- Internet permission,
- HTTP/socket code,
- media.config,
- media/file listing,
- media download,
- file mutation,
- AP-mode command,
- retry loops,
- fallback to arbitrary/first peer,
- logging nearby peer names/addresses,
- reset/restart/OTA.

## Physical test

1. Force-stop Cyan Glasses.
2. Keep AIMB-G1 paired.
3. Turn on Bluetooth and Wi-Fi.
4. Open K G1 P2P Association Probe.
5. Allow Bluetooth/Nearby Wi-Fi permissions.
6. Tap Run once.
7. Let the app complete enter → discovery → association → exit → cleanup.
8. Copy the complete report.
