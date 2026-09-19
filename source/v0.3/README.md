# K G1 Media Count Probe v0.3 — G3 SINGLE QUERY

Purpose: validate the exact Cyan `0x41` glasses-control request/response path with the narrow media inventory/count query before any media-mode transition.

## Exact query

```text
outer command: 0x41
payload: 02 04
full frame: BC 41 02 00 01 13 02 04
```

The CRC is CRC-16/MODBUS over payload `02 04`.

## What the app does

- connects to exactly one bonded `AIMB-G1` / `AIMB-G1_*` device,
- subscribes to the confirmed Cyan notify characteristic,
- sends exactly one `0x41 / 02 04` query,
- records raw framed notifications for 12 seconds,
- records Android characteristic-write status,
- disconnects.

## Deliberately not implemented

- no P2P media-mode payload `02 01 04 01`,
- no AP media-mode payload `02 01 04 02`,
- no Wi-Fi/P2P/AP APIs,
- no HTTP/network/media transfer,
- no file delete/modify,
- no reset/restart/OTA/firmware operation,
- no time-sync write,
- no arbitrary raw command input,
- no retry of the proprietary write.

## Parsing policy

The APK records raw response frames and validates framing/CRC only. It does not guess media-count field offsets. Semantic decoding happens after the physical report is reviewed against exact Cyan evidence.

## Physical test

1. Force-stop Cyan Glasses.
2. Keep AIMB-G1 paired.
3. Turn on Bluetooth and the glasses.
4. Open K G1 Media Count Probe.
5. Tap **Run single media-count query** once.
6. Let the 12-second response window finish.
7. Copy/share the complete report.
8. Do not run any media-mode command afterward.
