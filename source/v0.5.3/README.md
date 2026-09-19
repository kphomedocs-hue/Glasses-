# K G1 Capture Visibility Probe v0.5.3 — BLE-only G5 diagnostic

Purpose: determine when one physical photo becomes visible to the glasses inventory before attempting another media download.

## Why this exists

G5.2 physically showed:
- Phase A inventory: images=2, videos=0, recordings=1;
- one user-confirmed photo capture;
- Phase B inventory: unchanged 2 / 0 / 1;
- Phase B media.config: unchanged 3 entries / 67 bytes;
- media GETs: 0.

Therefore the current uncertainty is upstream of HTTP/download selection.

## Physical flow

1. Connect to exactly one bonded AIMB-G1-family device.
2. Subscribe to the confirmed Cyan notify characteristic.
3. Send one baseline `0x41 / 02 04` inventory query.
4. Keep the same BLE connection and notification subscription alive.
5. User captures exactly one disposable photo while transfer mode remains off.
6. User taps the post-capture button.
7. Observe passive `0x73` notifications for 60 seconds.
8. Send exactly one final `0x41 / 02 04` inventory query.
9. Compare baseline/final image/video/record counts and disconnect.

## Passive notification handling

- all valid `0x73` event IDs are counted;
- event `0x01` is parsed only into:
  - image count,
  - video count,
  - recording count,
  - configFileType,
  - optional onlySupportApImport flag;
- no raw notification bytes are logged;
- no MAC/device address is logged.

## Hard boundary

Allowed proprietary payload:
- `02 04` only.

Maximum proprietary writes:
- 2 total.

Not implemented:
- P2P enter/exit;
- Wi-Fi or Wi-Fi Direct;
- INTERNET permission;
- HTTP / URL / sockets;
- media.config access;
- media-file GET;
- storage write;
- AP mode;
- `02 03`;
- reset/restart/OTA;
- arbitrary command input.

This diagnostic does not download media and does not change glasses files.
