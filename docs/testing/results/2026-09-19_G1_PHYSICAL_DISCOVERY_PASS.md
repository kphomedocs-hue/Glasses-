# AIMB-G1 G1 Physical Discovery Result — PASS

Date: 2026-09-19

## Test metadata

- Discovery app: K G1 Discovery v0.1.2
- APK SHA-256: `dc08b915581468f2d0d33fb317ca06b05e301b233cb0ced234edf7f0bc282297`
- Source ZIP SHA-256: `de89fd0dd15d48c51e1f080158b426f455699187a0f42f28686cf957850f3271`
- Safety mode: READ ONLY
- Public record: sanitized; no Bluetooth address or device-specific name suffix retained

## Discovery path

The Android BLE scan returned zero callbacks during the 30-second observation window. The diagnostic therefore used the pre-approved fallback: exactly one bonded AIMB-G1-family device was present, so the app opened a read-only LE GATT connection to that uniquely identified bonded device.

Connection result: **PASS**

## Physically confirmed Cyan-family profile

| Item | UUID | Physical result |
|---|---|---|
| Service | `de5bf728-d711-4e47-af26-65e3012a5dc7` | PRESENT |
| Notify candidate | `de5bf729-d711-4e47-af26-65e3012a5dc7` | PRESENT / NOTIFY |
| Write candidate | `de5bf72a-d711-4e47-af26-65e3012a5dc7` | PRESENT / WRITE + WRITE_NO_RESPONSE |

## Full discovered GATT profile

```text
SERVICE 00001800-0000-1000-8000-00805f9b34fb
  CHAR 00002a00-0000-1000-8000-00805f9b34fb [READ|WRITE]
SERVICE 0000ae30-0000-1000-8000-00805f9b34fb
  CHAR 0000ae01-0000-1000-8000-00805f9b34fb [WRITE_NO_RESPONSE]
  CHAR 0000ae02-0000-1000-8000-00805f9b34fb [NOTIFY]
  CHAR 0000ae03-0000-1000-8000-00805f9b34fb [WRITE_NO_RESPONSE]
  CHAR 0000ae04-0000-1000-8000-00805f9b34fb [NOTIFY]
  CHAR 0000ae05-0000-1000-8000-00805f9b34fb [INDICATE]
  CHAR 0000ae10-0000-1000-8000-00805f9b34fb [READ|WRITE]
SERVICE 00003802-0000-1000-8000-00805f9b34fb
  CHAR 00004a02-0000-1000-8000-00805f9b34fb [READ|WRITE|NOTIFY]
SERVICE 0000ae3a-0000-1000-8000-00805f9b34fb
  CHAR 0000ae3b-0000-1000-8000-00805f9b34fb [WRITE_NO_RESPONSE]
  CHAR 0000ae3c-0000-1000-8000-00805f9b34fb [NOTIFY]
SERVICE 00001801-0000-1000-8000-00805f9b34fb
  CHAR 00002a05-0000-1000-8000-00805f9b34fb [INDICATE]
SERVICE 6e40fff0-b5a3-f393-e0a9-e50e24dcca9e
  CHAR 6e400002-b5a3-f393-e0a9-e50e24dcca9e [WRITE|WRITE_NO_RESPONSE]
  CHAR 6e400003-b5a3-f393-e0a9-e50e24dcca9e [NOTIFY]
SERVICE de5bf728-d711-4e47-af26-65e3012a5dc7
  CHAR de5bf72a-d711-4e47-af26-65e3012a5dc7 [WRITE|WRITE_NO_RESPONSE]
  CHAR de5bf729-d711-4e47-af26-65e3012a5dc7 [NOTIFY]
SERVICE 0000180a-0000-1000-8000-00805f9b34fb
  CHAR 00002a25-0000-1000-8000-00805f9b34fb [READ]
  CHAR 00002a27-0000-1000-8000-00805f9b34fb [READ]
  CHAR 00002a26-0000-1000-8000-00805f9b34fb [READ]
  CHAR 00002a23-0000-1000-8000-00805f9b34fb [READ]
SERVICE 0000fee1-0000-1000-8000-00805f9b34fb
  CHAR 0000fee3-0000-1000-8000-00805f9b34fb [READ|WRITE|NOTIFY]
```

## Standard Device Information read

- Hardware revision: `AM01SPG1_V1.4`
- Firmware revision: unavailable; Android GATT status `133`
- Serial number characteristic was enumerated but was deliberately not read.

## G1 conclusion

**PASS**

The physical AIMB-G1 exposes the exact Cyan service/notify/write UUID family recovered from the Cyan application. Read-only LE GATT connection and service enumeration succeeded.

The zero BLE scan callbacks remain a phone/device discovery-path anomaly, but do not invalidate G1 because the uniquely identified bonded-device fallback connected and produced the complete GATT table.

## Next gate

G2 — response-channel confirmation.

The first G2 diagnostic is limited to enabling notifications on the physically confirmed `de5bf729-...` response characteristic, observing for spontaneous traffic, and disconnecting. It must not write to `de5bf72a-...` or send a media-mode/control command.
