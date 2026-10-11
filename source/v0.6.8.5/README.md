# K G1 G6A Cyan P2P State Diagnostic v0.6.8.5

One bounded diagnostic for the unresolved short `0x41` response.

It starts Wi-Fi Direct discovery before one P2P ENTER, connects only to one
Cyan-matching peer using WPS-PBC, observes sanitized CRC-valid `0x73` event
IDs, sends one EXIT, and verifies cleanup.

It has no Internet permission, HTTP/socket/media code, capture, import,
credential parsing/logging, reset, retry, AP mode, or arbitrary-peer fallback.
It is diagnostic-only and does not close G6A or unblock G6B.

2026-10-11 source correction: independent preflight/discovery/BLE/association/EXIT/cleanup watchdogs; fail-closed group ownership/removal/verification, bounded terminal cleanup, post-ENTER event fencing, and host-JVM policy regression tests. This remains an unbuilt, unpromoted research-only candidate.
