# Exact Cyan G4B media-catalog trace — 2026-09-19

## Evidence input

Analysed package:
- Cyan Glasses `1.0.2.18_20260811`
- package `com.aitowe.aitoglasses`
- split export SHA-256 `1328b3c025f43c06b2a0674d4c17890ec4b76cb6487196a84aa27b2335c2fc49`
- extracted `base.apk` SHA-256 `1e700628d76fa4fa84047632e2ccce3673f1a01966fbf8c583985568f3aaaf64`

The third-party APK is not committed to this public repository.

## Exact catalog-selection branch

`PictureFragment` initializes:
- `configFileName = "media.config"`
- `configFileNameT2 = "vf_list.txt"`
- `logFileName = "log.list"`

Exact `PictureFragment$downloadMediaConfig$2.invoke(...)` behavior:

1. If glasses-log mode is enabled:
   `http://<ip>/files/log/log.list`
2. Else if `configFileType == 2`:
   `http://<ip>:80/storage/sd0/C/DCIM/1/vf_list.txt`
3. Else:
   `http://<ip>/files/media.config`

Physical G3 established:
- `configFileType = 1`
- `onlySupportApImport = false`

Therefore the exact Cyan catalog URL for the physically observed AIMB-G1 state is:

```text
http://<glassDeviceWifiIP>/files/media.config
```

With the G4A2 physical address this resolves to the local endpoint:

```text
http://192.168.49.176/files/media.config
```

The address is runtime-derived in the diagnostic; it is not hard-coded into the app.

## Exact response model

Cyan downloads the catalog text and parses it through Moshi into:

```text
PtPFileModel(file_list = List<FileBean>)
```

`PtPFileModel` has the root property:
- `file_list`

`FileBean` has properties:
- `c` — String
- `e` — String
- `f` — String
- `h` — int
- `s` — String
- `t` — String
- `w` — int

For each catalog item, Cyan uses `FileBean.getF()` to construct the later media URL:

```text
http://<glassDeviceWifiIP>:80/<f>
```

That later media-file GET is outside G4B.

## Cyan catalog transfer implementation

Cyan's `AlbumDepository.getPhotoTextFile(...)` delegates to `AndroidNetworking.download(url, dirPath, fileName)`, then reads and parses the downloaded text file.

The current G4B diagnostic deliberately narrows this behavior:
- one in-memory GET only,
- no file-system write,
- no media-file request,
- no retry,
- no redirect,
- bounded response size,
- sanitized structural reporting only.

This is stricter than Cyan while preserving the read-only interoperability question.

## P2P routing / process binding

Exact P2P album flow:
- `WifiP2pManagerSingleton` forms the P2P group,
- `PictureFragment.onConnected(...)` records connection state,
- `PictureFragment.downloadMediaConfig()` proceeds to the catalog request.

No explicit `ConnectivityManager.bindProcessToNetwork(...)` call is present in this P2P album path.

Explicit process-network binding found in the APK belongs to separate regular-Wi-Fi/AP helper paths such as:
- `WifiConnector`
- `TempWifiHelper`
- `DisconnectCallbackHolder`

Therefore G4B mirrors the exact P2P path and does not add process binding.

## G4B implementation rule

The first physical G4B diagnostic may:
- use the already verified P2P enter/association/passive-IP/exit lifecycle,
- require the phone-group-owner topology already observed in G4A/G4A2,
- accept only a `192.168.49.0/24` glasses address resolved from `0x73 / 0x08`,
- issue exactly one GET to `/files/media.config`,
- parse only the JSON structure in memory,
- report counts, key names, and extension counts without filename/path values.

It must not:
- query `0x41 / 02 03`,
- request any media file,
- follow redirects,
- use arbitrary URLs,
- write/delete files,
- use AP mode,
- mutate the glasses,
- retry the HTTP request.
