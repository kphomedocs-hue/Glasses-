# AIMB-G1 Protocol Knowledge

This document separates **confirmed Cyan application evidence** from **physical AIMB-G1 confirmation**.

## Evidence source

Static analysis was performed against:

- App: Cyan Glasses
- Package: `com.aitowe.aitoglasses`
- Version: `1.0.2.18_20260811`
- Version code: `85`
- Export SHA-256: `1328b3c025f43c06b2a0674d4c17890ec4b76cb6487196a84aa27b2335c2fc49`

The third-party APK itself is not stored in this public repository.

## AIMB-G1 support evidence

Cyan's code contains `AIMB-G1` in its supported-glasses model list. This makes the Cyan protocol implementation directly relevant to the user's device family.

## BLE profile recovered from Cyan

```text
Service:
de5bf728-d711-4e47-af26-65e3012a5dc7

Notify:
de5bf729-d711-4e47-af26-65e3012a5dc7

Write:
de5bf72a-d711-4e47-af26-65e3012a5dc7
```

These UUIDs are **confirmed in Cyan and physically confirmed on the user's AIMB-G1** by the sanitized G1 test recorded in `docs/testing/results/2026-09-19_G1_PHYSICAL_DISCOVERY_PASS.md`.

## Control framing recovered from Cyan

Cyan's `LargeDataHandler.glassesControl(...)` uses command ID `0x41` and a frame layout:

```text
BC
41
LEN_LOW
LEN_HIGH
CRC_LOW
CRC_HIGH
PAYLOAD...
```

Length and CRC are little-endian.

### CRC

Cyan's CRC implementation is standard CRC-16/MODBUS over the payload only:

- initial CRC: `0xFFFF`
- polynomial: `0xA001`

Known check:

```text
payload: 02 01 01
CRC:     0x5010
frame:   BC 41 03 00 10 50 02 01 01
```

This resolves the older third-party worked-example discrepancy that claimed `0x5510`.

## Media-mode evidence

Cyan's PictureFragment contains these exact payloads:

```text
02 01 04 01  -> media import path associated with Wi-Fi P2P
02 01 04 02  -> media import path associated with AP Wi-Fi
```

Their complete frames are:

```text
P2P:
BC 41 04 00 93 5C 02 01 04 01

AP:
BC 41 04 00 D3 5D 02 01 04 02
```

These frames are documented for interoperability but are **not authorized for the first physical test**.

## Media transport evidence

Cyan contains:

- `media.config`
- `vf_list.txt`
- `log.list`
- `WifiP2pManager`
- stored glasses Wi-Fi name/password/IP fields
- local HTTP file download code
- `/files/`
- `/files/log/`
- OPUS audio support
- a `glass_album` local media metadata table

The observed media download implementation is local HTTP after BLE/Wi-Fi handoff.

## Authentication evidence

The traced media-import path is local:

```text
BLE control
→ Wi-Fi/P2P handoff
→ local glasses IP
→ local HTTP
```

No Cyan subscription token, cloud API key, signed nonce or account token was found being inserted into this traced media-import command path. This lowers the authentication risk but does not prove that all device initialization is unauthenticated.

## Other observed control payloads

Additional Cyan payloads were catalogued during static analysis. They are retained only as research provenance. Destructive/maintenance operations such as reset, restart, OTA or generic device-control features are explicitly outside the K Site Capture product scope.

## Current confidence levels

| Item | Evidence level |
|---|---|
| Cyan supports AIMB-G1 | confirmed in app code |
| BLE UUID family | confirmed in app code |
| frame magic/command/length/CRC layout | confirmed in app code |
| CRC-16/MODBUS | confirmed in app code |
| P2P/AP media payload bytes | confirmed in app code |
| local HTTP media retrieval | confirmed in app code |
| physical AIMB-G1 exposes expected UUIDs | **confirmed by G1 physical test** |
| exact response semantics | pending |
| initialization/time handshake requirement | pending |
| first real file download | pending |

## Safety rule

Protocol knowledge is stored separately from execution. Production code must use an explicit command allow-list and must not expose a generic raw BLE command console.


## Physical G1 confirmation — 2026-09-19

A read-only LE GATT connection physically confirmed the expected Cyan-family profile on the AIMB-G1:

- service `de5bf728-d711-4e47-af26-65e3012a5dc7`: present,
- notify `de5bf729-d711-4e47-af26-65e3012a5dc7`: present with NOTIFY,
- write `de5bf72a-d711-4e47-af26-65e3012a5dc7`: present with WRITE and WRITE_NO_RESPONSE.

Hardware revision `AM01SPG1_V1.4` was read from the standard Device Information Service. Firmware revision returned Android GATT status 133 during the sequential standard-field read.

The next protocol question is the response-channel behavior. G2 begins with notification subscription only and sends no proprietary control payload.


## Physical G2 response-channel confirmation — 2026-09-19

The first notification-only G2 probe subscribed successfully to the physically confirmed Cyan notify characteristic
`de5bf729-d711-4e47-af26-65e3012a5dc7` by writing only the standard CCCD enable-notification value.

No proprietary characteristic write was performed.

During a 30-second passive observation window, the AIMB-G1 emitted:

```text
BC 73 03 00 52 31 05 47 00
BC 73 08 00 01 07 01 01 00 00 00 01 00 01
BC 73 03 00 53 A1 05 46 00
```

These packets physically confirm that the existing envelope applies on the response channel as well:

```text
BC | command | len_le16 | crc_le16(payload) | payload
```

Validated payloads:
- command `0x73`, payload `05 47 00`, CRC `0x3152`,
- command `0x73`, payload `01 01 00 00 00 01 00 01`, CRC `0x0701`,
- command `0x73`, payload `05 46 00`, CRC `0xA153`.

Evidence level:
- response characteristic subscription: physically confirmed,
- spontaneous framed traffic: physically confirmed,
- response command `0x73`: physically observed,
- `0x73` payload semantics: pending,
- required initialization/time handshake before media-mode control: pending.

Do not infer payload meaning from byte patterns alone. Decode against Cyan evidence or passive correlation before authorizing the first proprietary control write.
