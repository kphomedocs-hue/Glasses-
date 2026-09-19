# K G1 Capture Visibility Probe v0.5.5 — BLE-only G5 diagnostic

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


## Pre-physical diagnostic-quality correction

A detailed recheck found that v0.5.3 asked the user to capture the photo before tapping the 60-second observation button. BLE notifications were already active, so events would still be received, but the report could not cleanly define the event window relative to the capture.

v0.5.4 corrects only the measurement workflow:
1. baseline inventory completes;
2. user taps **Arm 60s watch**;
3. the app resets armed-window counters and starts the exact bounded window;
4. user captures exactly one photo during the armed window;
5. only events inside that window contribute to the capture-visibility evidence;
6. one final `02 04` inventory query runs at the end.

Network/protocol scope is unchanged:
- BLE only;
- `02 04` only;
- max two proprietary writes;
- no P2P/Wi-Fi/HTTP/media access.


## v0.5.5 metadata/provenance correction

v0.5.5 supersedes v0.5.4 before physical use.

The only runtime change from v0.5.4 is the exported report metadata:
- v0.5.4 UI/package metadata correctly identified itself as v0.5.4, but the report header still printed `App version: 0.5.3`.
- v0.5.5 prints `App version: 0.5.5`.

The BLE protocol, two-write cap, arm-before-capture timing, 60-second watch, parsers, permissions, and zero-network boundary are unchanged.
