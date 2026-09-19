# AIMB-G1 G2 Notification-Only Response Probe — PASS

Date: 2026-09-19

## Test metadata

- App: K G1 Response Probe v0.2
- APK SHA-256: `86b9738ac909577b173264be233242c17c9715be5bae183fffa6c9d497174ff2`
- Gate: G2 response-channel confirmation
- Safety mode: notification only
- Proprietary characteristic writes: not implemented
- Public record: sanitized; no Bluetooth address or device-specific suffix retained

## Target and connection

- Exactly one bonded AIMB-G1-family device was identified.
- Device type: DUAL.
- Transport requested: LE.
- GATT connection: PASS.
- Cyan service `de5bf728-d711-4e47-af26-65e3012a5dc7`: PRESENT.
- Cyan notify characteristic `de5bf729-d711-4e47-af26-65e3012a5dc7`: PRESENT / NOTIFY.

## Notification subscription

- Local notification registration: SUCCESS.
- CCCD `00002902-0000-1000-8000-00805f9b34fb` write start: SUCCESS.
- CCCD write status: SUCCESS.
- Proprietary characteristic write: NONE.

## Passive observation

Observation window: 30 seconds.

Three spontaneous notifications were received without any proprietary command:

```text
BC 73 03 00 52 31 05 47 00
BC 73 08 00 01 07 01 01 00 00 00 01 00 01
BC 73 03 00 53 A1 05 46 00
```

Total notifications: 3  
Total notification bytes: 32

## Frame validation

The observed frames match the existing Cyan envelope:

```text
BC
COMMAND
LEN_LOW
LEN_HIGH
CRC_LOW
CRC_HIGH
PAYLOAD...
```

For all three packets, the little-endian payload length and CRC-16/MODBUS validate exactly.

| Frame | Command | Payload | Length | CRC from frame | Recomputed CRC |
|---|---:|---|---:|---:|---:|
| 1 | `0x73` | `05 47 00` | 3 | `0x3152` | `0x3152` |
| 2 | `0x73` | `01 01 00 00 00 01 00 01` | 8 | `0x0701` | `0x0701` |
| 3 | `0x73` | `05 46 00` | 3 | `0xA153` | `0xA153` |

## Conclusion

**G2 response-channel confirmation: PASS.**

Physically confirmed:
- the Cyan response characteristic can be subscribed,
- the standard CCCD enable-notification operation succeeds,
- the AIMB-G1 emits spontaneous framed traffic on that path,
- command/envelope byte `0x73` is physically observed on the notification channel,
- the received packets use the same length + CRC-16/MODBUS framing model already recovered from Cyan.

## Remaining uncertainty

The semantic meaning of command `0x73` and these payloads is not yet established.

The data proves that no proprietary write is required merely to receive spontaneous notification traffic. It does **not** prove that media-mode control can be entered without initialization, nor does it establish the meaning of the observed payloads.

No control-characteristic write is authorized from this result alone.

## Next step

Decode the observed `0x73` notification frames against Cyan/static evidence or, if static semantics cannot be recovered, use a separate passive event-correlation probe. Do not advance to a media-mode command until initialization/response semantics have been reviewed.
