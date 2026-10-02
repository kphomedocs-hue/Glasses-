# K G1 0x40 Initialization Credential Diagnostic v0.6.8.4

Diagnostic only. Do not take a photo.

Sequence:
- one Cyan-equivalent 0x40 time sync;
- one media-count query;
- one P2P ENTER;
- bounded credential observation;
- one EXIT.

The 0x40 response is not a gate. Only its BLE write callback is required before the media-count query.

No Wi-Fi Direct, HTTP, catalog/media access, download, persistent import, receipt, or mutation code exists in this build.
