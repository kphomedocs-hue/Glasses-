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

## Exact read/parse path

Decompilation-level bytecode inspection of `AlbumDepository.readPhotoFile` confirms two parsing branches keyed by `configFileType`.

For the current physical branch, Cyan:
- constructs a `java.io.File`,
- calls Kotlin `FilesKt.readText$default(...)`,
- passes the entire resulting text string into `AlbumDepository$readPhotoFile$1`,
- obtains Moshi from `MoshiUtils`,
- obtains a `JsonAdapter` for `PtPFileModel`,
- calls `JsonAdapter.fromJson(jsonString)`,
- then calls `PtPFileModel.getFile_list()`.

The alternate branch uses `FilesKt.readLines$default(...)` and is associated with the other configuration path.

## Exact response model expected by Cyan

The generated Moshi model is:

```text
PtPFileModel(file_list = List<FileBean>)
```

`FileBean` exposes compact properties:
- `c` — String
- `e` — String
- `f` — String
- `h` — int
- `s` — String
- `t` — String
- `w` — int

For each parsed item, Cyan uses `FileBean.getF()` to construct the later media URL:

```text
http://<glassDeviceWifiIP>:80/<f>
```

That later media-file GET is outside G4B.

## Physical v0.4.2 correction

The first physical G4B probe performed the exact endpoint GET once and then failed with:

```text
Catalog GET/parse failed: JSONException
```

Because the verified v0.4.2 control flow checks HTTP status and response-size limits before invoking Android `JSONObject`, this failure proves:
- the request reached the expected HTTP path,
- the response body was read within the 65,536-byte cap,
- the failure occurred at the diagnostic's JSON-parser compatibility layer.

It does **not** invalidate the endpoint.

It does invalidate the stronger earlier assumption that the physical response bytes can be passed directly to Android `JSONObject` with no normalization or parser compatibility handling.

Potential causes such as BOM/encoding or a different top-level representation remain hypotheses until physically characterized.

Evidence:
`docs/testing/results/2026-09-19_G4B_CATALOG_PARSE_FAIL.md`

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

Therefore the G4B diagnostic continues to mirror the exact P2P path and does not add process binding.

## G4B2 implementation rule

The next physical diagnostic keeps the exact same one-request boundary:
- proven P2P enter/association/passive-IP/exit lifecycle,
- phone-group-owner topology,
- only a `192.168.49.0/24` runtime IP from `0x73 / 0x08`,
- exactly one GET to `/files/media.config`,
- no redirects,
- no retry,
- bounded in-memory body only.

It may report only safe structural facts:
- response byte count and SHA-256,
- sanitized Content-Type / Content-Encoding,
- strict UTF-8 validity,
- BOM type,
- line count,
- top-level token class,
- raw vs BOM-normalized JSON type,
- root-key count,
- `file_list` presence/type,
- catalog item count,
- presence of known compact keys `c,e,f,h,s,t,w`,
- unknown-key count,
- extension counts derived from `f`.

It must not log the raw body or any filename/path value and must not request any media file.
