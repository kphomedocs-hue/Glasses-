# G2B Passive Event Correlation Plan

Status: **FALLBACK ONLY — G2B static trace passed on 2026-09-19.**

The exact Cyan APK resolved the observed `0x73` semantics and initialization order before this correlator needed to be built.

Primary evidence:
`docs/research/CYAN_EXACT_G2B_TRACE_2026-09-19.md`

Current next gate:
**G2C — one-command initialization parity (`0x40` time sync only).**

This document is retained as a reusable fallback if a future unknown asynchronous event cannot be decoded from exact Cyan/static evidence.

## When to use this fallback

Use passive event correlation only when:
- a new `0x73` event type is physically observed,
- exact Cyan/Oudmon code does not establish its meaning,
- a physical action/event can be safely repeated without a proprietary control write.

## Passive correlator safety boundary

Allowed:
- connect to exactly one bonded AIMB-G1-family device,
- discover the confirmed Cyan service,
- subscribe to `de5bf729-d711-4e47-af26-65e3012a5dc7`,
- write only the standard CCCD enable-notification value,
- timestamp incoming frames,
- validate envelope length/CRC,
- add neutral user event markers,
- generate sanitized local reports.

Forbidden:
- BLE characteristic write API,
- `de5bf72a-d711-4e47-af26-65e3012a5dc7`,
- Cyan command-frame generator,
- initialization/time command,
- media-mode command,
- Wi-Fi/P2P/AP,
- Internet/HTTP/media transfer,
- pairing/unpairing/reset,
- restart/OTA/firmware actions,
- generic raw BLE console.

## Correlation discipline

For any candidate semantic mapping:
- repeat the same safe physical action at least three times when practical,
- include baseline intervals,
- record negative trials,
- treat one-off timing coincidences as inconclusive,
- prefer direct exact-code evidence whenever available.

## Privacy

Do not publish Bluetooth addresses, device-specific suffixes, serial values, Wi-Fi credentials, unrelated nearby-device data, account identifiers or captured client/site media.
