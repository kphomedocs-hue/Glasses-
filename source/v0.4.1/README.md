# K G1 P2P IP Notify Probe v0.4.1 — G4A2

Purpose: resolve the glasses-side Wi-Fi Direct client IPv4 from already-occurring BLE notifications without adding any new proprietary query.

## Safety boundary

Allowed:
- exact confirmed BLE P2P enter command `0x41 / 02 01 04 01`, once,
- parse returned SSID/password only in memory,
- Wi-Fi Direct discovery and exact BLE-reported peer-name matching,
- WPS PBC with `groupOwnerIntent=0`,
- sanitized `0x73` event-ID reporting only,
- for event `0x08` only, parse raw frame bytes `[7..10]` as IPv4,
- exact transfer exit `0x41 / 02 01 09`, once,
- cleanup/disconnect.

Forbidden:
- `0x41 / 02 03` P2P-IP query,
- any third proprietary BLE write,
- Internet permission,
- HTTP/socket code,
- media.config or file/media listing,
- media download or mutation,
- AP-mode command,
- arbitrary BLE input,
- arbitrary peer fallback,
- logging SSID/password, peer addresses, or unrelated raw `0x73` payloads,
- reset/restart/OTA/firmware actions.

## Physical test

1. Force-stop Cyan Glasses.
2. Keep AIMB-G1 paired; turn Bluetooth and Wi-Fi on.
3. Install/open K G1 P2P IP Notify Probe v0.4.1.
4. Allow the requested Bluetooth/Nearby Wi-Fi permissions.
5. Tap the G4A2 probe once.
6. Let the bounded enter → discovery → association → observe → exit → cleanup lifecycle finish.
7. Copy the complete sanitized report.
8. If no `0x73 / 0x08` event resolves an IPv4, stop. Do not proceed to HTTP or add `02 03` in this gate.
