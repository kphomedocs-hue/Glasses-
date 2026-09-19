# AIMB-G1 G2C Single Time-Sync Probe — PASS

Date: 2026-09-19

## Test metadata

- App: K G1 Init Probe v0.2.1
- APK SHA-256: `7c0ce5d859e79c6ee2ed05316f5f5db736919912802fbbab040ce612ea368e5c`
- Gate: G2C one-command initialization parity
- Allowed proprietary command: `0x40` time sync only
- Maximum proprietary characteristic writes: 1
- Media/control commands: not implemented
- Public record: sanitized; no Bluetooth address or device-specific suffix retained

## Connection and subscription

- exactly one bonded AIMB-G1-family device identified,
- LE GATT connection: SUCCESS,
- Cyan service: PRESENT,
- Cyan notify characteristic: PRESENT / NOTIFY,
- Cyan write characteristic: PRESENT / WRITE,
- local notification registration: SUCCESS,
- CCCD enable-notification write: SUCCESS.

## Single proprietary write

Dynamic Cyan-equivalent time payload sent:

```text
26 09 19 09 14 23 01 0C 01
```

Frame properties:
- command: `0x40`,
- total frame length: 15 bytes,
- CRC validated before write,
- Android write start: SUCCESS,
- Android characteristic-write callback: SUCCESS,
- no retry,
- proprietary write attempts: exactly 1.

## Observed notifications

```text
BC 73 03 00 53 A1 05 46 00
BC 73 08 00 01 07 01 01 00 00 00 01 00 01
BC 73 03 00 53 51 05 45 00
BC 40 01 00 BF 40 00
```

All four frames validate using the documented length + CRC-16/MODBUS envelope.

The `0x40` response contains payload:

```text
00
```

CRC in frame: `0x40BF`  
Recomputed CRC over payload `00`: `0x40BF`

## Interpretation

The key physical result is direct:
- one Cyan-equivalent `0x40` time-sync command was accepted by Android GATT,
- the glasses returned a valid command-`0x40` framed response,
- no second proprietary write was sent,
- normal asynchronous `0x73` traffic continued.

The exact semantic label of response byte `00` is not required to establish transport success and is not overclaimed here.

## G2C conclusion

**PASS**

The first controlled proprietary write path is physically confirmed.

## Safer next gate

Do not enter P2P/AP media mode yet.

Use a narrower G3 query already present in the exact Cyan app:

```text
command: 0x41
payload: 02 04
purpose: media inventory/count query
```

This query is preferable before `02 01 04 01` / `02 01 04 02` because it validates the `0x41` glasses-control request/response path without intentionally activating Wi-Fi/P2P/AP or starting media transfer.
