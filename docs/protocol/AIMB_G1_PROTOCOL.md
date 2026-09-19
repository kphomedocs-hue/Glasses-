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
| exact response semantics | **resolved for observed 0x01 / 0x05 events by exact Cyan trace** |
| initialization/time handshake requirement | **Cyan sends 0x40 time sync first, but does not use its callback as a gate** |
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


## G2B interpretation policy

The three observed `0x73` packets are valid physical protocol evidence, but their semantics must not be inferred from isolated byte values.

The next interpretation process is:

1. trace Cyan's receive path from BLE notification callback through frame validation/dispatch to the code that consumes command `0x73`;
2. record any explicit enum, state, subcommand or UI mapping supported by Cyan evidence;
3. independently correlate passive AIMB-G1 physical events with timestamped notifications;
4. require repeated correlations for a physical-event interpretation;
5. resolve disagreements in favor of the stronger/direct evidence and record uncertainty explicitly.

A future passive Event Correlator may write the standard CCCD only. It must not contain the proprietary Cyan write UUID or any characteristic-write API.

See `docs/testing/G2B_PASSIVE_CORRELATION_PLAN.md`.


## Exact G2B Cyan parser trace — 2026-09-19

Detailed evidence:
`docs/research/CYAN_EXACT_G2B_TRACE_2026-09-19.md`

### Command 0x73

Exact Cyan/Oudmon receive code treats `0x73` (decimal 115) as the persistent asynchronous device-data reporting channel.

The first payload byte at raw frame index 6 is the event type.

### Event 0x01

Exact `PictureFragment$MyDeviceNotifyListener` parses three little-endian 16-bit values, a config byte and an optional AP-only flag, then sums the three counts and updates the Album media-count UI.

The corresponding `GlassModelControlResponse` identifies the count ordering as image, video and record.

Physical G2 frame stable decode:
- imageCount: 1
- videoCount: 0
- recordCount: 1
- configFileType: 1
- onlySupportApImport: unavailable because the physical frame is one byte shorter than the current parser's newest schema.

### Event 0x05

Exact Cyan PictureFragment code consumes the third payload byte as the charging boolean. Both physical G2 frames therefore report charging=false.

The middle bytes `0x47` and `0x46` are consistent with the SDK convention for battery level, but the exact app branch's directly required fact is the charging flag.

### Initialization order

Exact post-service-discovery path:

```text
onServiceDiscovered
→ LargeDataHandler.initEnable
→ DeviceCmdInit.initDeviceSetting
→ DeviceCmdInit.init
→ syncTime (0x40)
→ syncDeviceInfo
→ syncDeviceSetting
```

`syncTime` is therefore Cyan's first queued proprietary command after notification setup.

Its callback is empty and later initialization is queued immediately; static evidence does not show time sync functioning as a required media-mode handshake.

### G2C rule

Before any `0x41` media/control payload, physically validate one Cyan-equivalent `0x40` time-sync command only.


## Physical G2C time-sync confirmation — 2026-09-19

A single Cyan-equivalent command-`0x40` time-sync write was physically tested.

Observed response:

```text
BC 40 01 00 BF 40 00
```

The response:
- uses command `0x40`,
- declares one payload byte,
- carries payload `00`,
- has CRC `0x40BF`,
- validates exactly using CRC-16/MODBUS over the payload.

Android also reported the characteristic-write callback as SUCCESS.

This physically confirms the controlled proprietary write/response path. The semantic meaning of the single response byte is not overstated.

### Next control-path probe

The exact Cyan app exposes a narrower read-style control request before any media-mode transition:

```text
glassesControl payload: 02 04
outer command: 0x41
purpose: media inventory/count query
```

Use that as G3 before any `02 01 04 01` P2P or `02 01 04 02` AP media-mode command.


## Physical G3 media-count confirmation — 2026-09-19

Query:

```text
BC 41 02 00 01 13 02 04
```

Physical response:

```text
BC 41 0B 00 2D 19 02 04 01 00 00 00 01 00 01 00 00
```

Exact current Cyan `GlassModelControlResponse.acceptData()` parses this as:
- dataType = 4,
- imageCount = 1,
- videoCount = 0,
- recordCount = 1,
- configFileType = 1,
- onlySupportApImport = false.

The final raw response byte is not consumed by the exact dataType-4 parser and remains semantically unspecified.

### Exact transport selection

Cyan `PictureFragment.requestPermissionLaunch$lambda$4` selects AP if:
- configFileType == 2, or
- HarmonyOS NEXT, or
- onlySupportApImport is true.

Otherwise it selects P2P.

The physical device state therefore selects P2P.

Exact fill-array payloads extracted from the current Cyan APK:
- `importAlbum()`: `02 01 04 01`,
- `importAlbumAp()`: `02 01 04 02`,
- `fileDownloadComplete()`: `02 01 09`.

The last command provides an exact-app rollback path for a bounded G3B transfer-mode lifecycle probe.


## G3B physical P2P lifecycle — 2026-09-19

P2P enter:
```text
0x41 / 02 01 04 01
```

The physical response confirms the transfer-credential schema:

```text
02 01 04 01
ssid_len_le16
password_len_le16
ssid_bytes
password_bytes
```

On this physical device:
- SSID length = 20,
- password length = 9.

Credential values are intentionally omitted from the public repository.

P2P exit:
```text
0x41 / 02 01 09
```

The exit command was accepted and produced a valid `0x41` response. Normal `0x73 / 0x01` media-inventory reporting resumed afterward.

G3B: **PASS**.


## Physical G4A2 P2P-IP confirmation — 2026-09-19

The verified v0.4.1 probe repeated the physically proven G4A Wi-Fi Direct association lifecycle and added no new proprietary query.

During the formed P2P group, a valid asynchronous `0x73` event with event ID `0x08` was observed. Using the verified G4A2 parser rule—event ID at raw frame index 6 and IPv4 octets at raw frame indices `[7..10]`—the glasses-side client address resolved to:

```text
192.168.49.176
```

The phone remained group owner at `192.168.49.1`.

Observed event IDs during the run:
- `0x0B`,
- `0x08`,
- `0x01`.

Safety result:
- proprietary writes: P2P enter once + transfer exit once only,
- `0x41 / 02 03` P2P-IP query: not used,
- HTTP/socket requests: 0,
- media operations: 0,
- credentials persisted/logged: no,
- exact peer match: yes,
- transfer exit: success.

G4A2 conclusion: **PASS**.

Evidence:
`docs/testing/results/2026-09-19_G4A2_P2P_IP_NOTIFY_PASS.md`

This physically satisfies the addressing prerequisite for G4B. It does not itself authorize media download or mutation; G4B remains a separate read-only local HTTP listing gate.
