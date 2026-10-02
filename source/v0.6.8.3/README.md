# K G1 Corrected Safe Short Response Diagnostic v0.6.8.3

Diagnostic only. Do not take a photo.

Run once with Cyan Glasses force-stopped and AIMB-G1 powered/paired.

This build performs one media-count query, one P2P ENTER, up to 20 seconds bounded credential observation, then one EXIT. It has no Wi-Fi Direct, HTTP, media-transfer, import, or ledger code.

Only valid `0x41` payloads shorter than 8 bytes observed during ENTER/LATE observation may be shown in hex. Post-EXIT payload bytes are never logged.
