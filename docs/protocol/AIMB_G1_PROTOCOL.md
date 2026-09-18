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

These UUIDs are **confirmed in Cyan** but still await first physical GATT confirmation on the user's AIMB-G1.

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
| physical AIMB-G1 exposes expected UUIDs | pending physical read-only test |
| exact response semantics | pending |
| initialization/time handshake requirement | pending |
| first real file download | pending |

## Safety rule

Protocol knowledge is stored separately from execution. Production code must use an explicit command allow-list and must not expose a generic raw BLE command console.
