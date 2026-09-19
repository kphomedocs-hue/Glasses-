# Exact Cyan G2B Static Trace — 2026-09-19

## Scope

This note records interoperability findings from the user's exact Cyan Glasses installation package.

Input provenance:
- application: Cyan Glasses
- package: `com.aitowe.aitoglasses`
- version: `1.0.2.18_20260811`
- version code: `85`
- exported split package SHA-256: `1328b3c025f43c06b2a0674d4c17890ec4b76cb6487196a84aa27b2335c2fc49`
- extracted `base.apk` SHA-256: `1e700628d76fa4fa84047632e2ccce3673f1a01966fbf8c583985568f3aaaf64`

The third-party APK and decompiled bulk source are not committed to this public repository.

## Receive-path trace

Exact classes/methods traced:

```text
com.oudmon.ble.base.bluetooth.LargeDataParser.parseData(byte[])
com.oudmon.ble.base.communication.LargeDataHandler.initEnable()
com.aitowe.aitoglasses.home.PictureFragment$MyDeviceNotifyListener.parseData(...)
```

`LargeDataParser.parseData` validates the `0xBC` framed stream, creates the command-specific response bean, and dispatches command `0x73` through the persistent/out-device listener map.

`LargeDataHandler.initEnable()`:
- enables the serial-port notification request,
- registers the device-notify listener under decimal command key `115` = `0x73`.

This directly confirms that `0x73` is the asynchronous device-data reporting channel used by Cyan.

## Exact event-type location

Cyan's `PictureFragment$MyDeviceNotifyListener.parseData` reads:

```text
eventType = loadData[6]
```

Because `loadData` contains the complete framed packet, index 6 is the first payload byte after:

```text
BC | command | len_lo | len_hi | crc_lo | crc_hi
```

## Event 0x01 — media inventory/count/config report

For `loadData[6] == 0x01`, exact Cyan code parses:

```text
loadData[7..8]   -> little-endian 16-bit count A
loadData[9..10]  -> little-endian 16-bit count B
loadData[11..12] -> little-endian 16-bit count C
loadData[13]     -> configFileType
loadData[14]     -> onlySupportApImport flag
```

It then adds the three counts and updates the Picture/Album UI count.

The ordering matches the separate exact `GlassModelControlResponse` media-count response fields:
- imageCount,
- videoCount,
- recordCount,
- configFileType,
- onlySupportApImport.

Therefore the physically observed G2 packet:

```text
BC 73 08 00 01 07 01 01 00 00 00 01 00 01
```

decodes through the stable portion of that schema as:

```text
event type       0x01
image count      1
video count      0
record count     1
configFileType   1
```

Compatibility note: the physical AIMB-G1 frame has an 8-byte payload and therefore stops at raw index 13. The current Cyan parser also attempts to read raw index 14 for `onlySupportApImport`. This indicates a firmware/schema-length variant. Do not fabricate the missing flag; treat it as unavailable for this physical frame.

## Event 0x05 — battery/charging-family report

For `loadData[6] == 0x05`, exact Cyan PictureFragment code consumes `loadData[8]` as a boolean charging flag.

The two physical frames:

```text
BC 73 03 00 52 31 05 47 00
BC 73 03 00 53 A1 05 46 00
```

therefore directly establish:
- event family: battery/charging status,
- charging flag: `0` in both frames.

The intermediate values `0x47` and `0x46` are consistent with the Oudmon SDK sample convention in which `loadData[7]` is battery level, but the exact Cyan PictureFragment branch itself only needs the charging flag. The repository should distinguish that stronger exact-app fact from the supporting SDK interpretation.

## Post-service-discovery initialization order

Exact Cyan connection path:

```text
MyBluetoothReceiver.onServiceDiscovered()
  -> LargeDataHandler.initEnable()
  -> initCmd()
  -> DeviceCmdInit.initDeviceSetting()
  -> DeviceCmdInit.init()
```

`DeviceCmdInit.init()` queues, in this order:

1. `LargeDataHandler.syncTime(...)`
2. `LargeDataHandler.syncDeviceInfo(...)`
3. `syncDeviceSetting()`

The first proprietary control command Cyan queues after notification setup is therefore **time synchronization**.

## Exact time-sync command

`LargeDataHandler.syncTime(...)` uses command ID:

```text
0x40
```

with a `SyncTime` payload of 9 bytes. The payload is constructed from:
- phone calendar year/month/day/hour/minute/second in BCD,
- language,
- encoded timezone,
- trailing mode/version byte.

It is framed through the same `BC | command | len | CRC | payload` envelope.

Important behavioral finding:
- Cyan's `init$lambda$0` time-sync callback is empty,
- Cyan immediately queues later initialization calls rather than waiting for or inspecting the time-sync response.

Conclusion: **time sync is part of Cyan's normal post-connect initialization, but the static evidence does not show it acting as a required handshake gate for media mode.**

## Media path reconfirmed in exact APK

Exact `PictureFragment` payloads:

```text
importAlbum()     -> 02 01 04 01   (P2P-associated path)
importAlbumAp()   -> 02 01 04 02   (AP-associated path)
readAlbumCounts() -> 02 04
```

These are sent using `LargeDataHandler.glassesControl` / command `0x41`.

## G2B conclusion

**PASS by exact static trace.**

The originally planned passive event-correlator build is not required before the next gate because the exact Cyan APK has resolved the event semantics needed for progression.

Resolved:
- `0x73` asynchronous report channel: confirmed,
- `0x01`: media inventory/count/config report,
- `0x05`: battery/charging-family report,
- Cyan post-connect initialization order: confirmed,
- `0x40` time sync is first queued proprietary command,
- time-sync callback is not used as a handshake gate.

Remaining caution:
- physical `0x01` packet is one byte shorter than the current Cyan parser's newest schema; the optional AP-only flag is unavailable in that frame.

## Recommended next gate

Do not jump directly to media mode.

Use **G2C — one-command initialization parity**:
- connect,
- subscribe to the confirmed notify path,
- send exactly one dynamically constructed Cyan-equivalent `0x40` time-sync frame,
- capture any `0x40` response and spontaneous `0x73` traffic,
- send no `0x41` control/media command,
- disconnect.

This is a smaller behavioral step than entering P2P/AP media mode and mirrors Cyan's first normal post-service-discovery command.
