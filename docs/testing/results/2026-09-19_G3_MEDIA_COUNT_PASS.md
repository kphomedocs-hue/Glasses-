# AIMB-G1 G3 Media Count Query — PASS

Date: 2026-09-19

## Test metadata

- App: K G1 Media Count Probe v0.3
- APK SHA-256: `31191dc90dac56e9aced7db5a6f6b4c65b969f10ec6d673199ee94c6a2f204e9`
- Gate: G3 one-command media inventory/count query
- Allowed proprietary command: `0x41` with payload `02 04` only
- Maximum proprietary characteristic writes: 1
- Media-mode commands: not implemented
- Wi-Fi/network: not implemented
- Public record: sanitized

## Query

```text
BC 41 02 00 01 13 02 04
```

Android write start: SUCCESS  
Android write callback: SUCCESS  
Retry count: 0

## Response

```text
BC 41 0B 00 2D 19 02 04 01 00 00 00 01 00 01 00 00
```

Frame validation:
- command: `0x41`,
- payload length: 11,
- CRC from frame: `0x192D`,
- CRC-16/MODBUS: PASS.

Payload:

```text
02 04 01 00 00 00 01 00 01 00 00
```

## Exact Cyan parser decode

The exact current Cyan `GlassModelControlResponse.acceptData()` parses a dataType-4 response as:

- raw byte 7: dataType,
- raw bytes 8..9: imageCount LE16,
- raw bytes 10..11: videoCount LE16,
- raw bytes 12..13: recordCount LE16,
- raw byte 14: configFileType,
- raw byte 15: onlySupportApImport flag,
- raw byte 16: not consumed by this parser branch.

Decoded physical response:

| Field | Value |
|---|---:|
| dataType | 4 |
| imageCount | 1 |
| videoCount | 0 |
| recordCount | 1 |
| configFileType | 1 |
| onlySupportApImport | false |
| trailing unparsed byte | 0 |

This matches the stable media-count/config fields previously observed through spontaneous `0x73` inventory reporting.

## Exact Cyan transport-selection logic

The exact Cyan `PictureFragment.requestPermissionLaunch$lambda$4` chooses AP import when:
- `configFileType == 2`, or
- the phone is HarmonyOS NEXT, or
- `onlySupportApImport == true`.

Otherwise it chooses P2P import.

For this physical response:
- configFileType = 1,
- onlySupportApImport = false,
- test platform is normal Android,

therefore the exact Cyan path selects **P2P import**.

## Exact Cyan enter/exit payloads

Recovered directly from the exact app's fill-array payloads:

`PictureFragment.importAlbum()`:
```text
02 01 04 01
```

`PictureFragment.importAlbumAp()`:
```text
02 01 04 02
```

`PictureFragment.fileDownloadComplete()`:
```text
02 01 09
```

The last payload is the app's explicit exit-transfer-mode command after file transfer completes.

## Conclusion

**G3 PASS.**

The read-style `0x41` query path is physically confirmed, media inventory is decoded, and Cyan's exact transport-selection logic identifies P2P as the appropriate media transport for this device state.

## Next gate

G3B should validate only the transfer-mode lifecycle:

1. subscribe to notifications,
2. send exactly one P2P enter payload `02 01 04 01`,
3. observe raw responses,
4. send exactly one Cyan exit-transfer payload `02 01 09`,
5. observe raw responses,
6. disconnect.

G3B must contain no Wi-Fi/P2P Android API, no HTTP/media transfer, and no file operation.
