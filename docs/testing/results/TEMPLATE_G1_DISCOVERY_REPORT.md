# AIMB-G1 Physical Discovery Report

> Commit a sanitized copy only. Do not include Bluetooth MAC addresses, serial numbers, Wi-Fi passwords, account data, client/site media, or other unnecessary identifiers.

## Test metadata

- Date:
- Discovery app version: K G1 Discovery v0.1
- Discovery source SHA-256: `84fb1b957290abdf2464a97b35b81f2d1e8e976ce9c067ecaf571c8c19f22c68`
- Repository APK SHA-256: `c32758f898b91041bc7e13a272096d2629836e4465542ee00c1fbd9764e7479a`
- Android version:
- Phone model:
- Cyan Glasses force-stopped before test: YES / NO
- AIMB-G1 powered on before scan: YES / NO

## Scan result

- Advertised device name:
- RSSI:
- Scan result status:
- Connection status:
- Service discovery GATT status:

## Expected Cyan-family profile

| Item | Expected UUID | Result |
|---|---|---|
| Service | `de5bf728-d711-4e47-af26-65e3012a5dc7` | FOUND / NOT FOUND |
| Notify candidate | `de5bf729-d711-4e47-af26-65e3012a5dc7` | FOUND / NOT FOUND |
| Write candidate | `de5bf72a-d711-4e47-af26-65e3012a5dc7` | FOUND / NOT FOUND |

## Full discovered GATT profile

Record all service UUIDs and characteristic UUIDs below with properties only.

```text
Service:
  Characteristic:
    properties:
```

Allowed property labels:
- READ
- WRITE
- WRITE_NO_RESPONSE
- NOTIFY
- INDICATE

## Standard readable Device Information

Only include values the Discovery app intentionally reads.

- Manufacturer:
- Model:
- Firmware:
- Hardware:
- Software:

Do not add a serial number.

## Errors / anomalies

```text
None
```

## Gate conclusion

- G1 physical profile confirmation: PASS / FAIL / INCONCLUSIVE
- Reason:
- Recommended next gate:

No protocol write should be authorized by this report unless the actual GATT profile has first been reviewed against the documented Cyan evidence.
