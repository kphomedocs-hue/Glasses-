# Exact Cyan G4B media-catalog trace — corrected 2026-09-19

## Evidence input

Analysed package:
- Cyan Glasses `1.0.2.18_20260811`
- package `com.aitowe.aitoglasses`
- split export SHA-256 `1328b3c025f43c06b2a0674d4c17890ec4b76cb6487196a84aa27b2335c2fc49`
- extracted `base.apk` SHA-256 `1e700628d76fa4fa84047632e2ccce3673f1a01966fbf8c583985568f3aaaf64`

The third-party APK is not committed to this public repository.

## Exact catalog URL branch

Bytecode for `PictureFragment$downloadMediaConfig$2.invoke(...)`:

1. If glasses-log mode is enabled:
   `http://<ip>/files/log/log.list`
2. Else read `UserConfig.getConfigFileType()`.
3. Compare against integer `2`.
4. If **configFileType == 2**:
   `http://<ip>:80/storage/sd0/C/DCIM/1/vf_list.txt`
5. Otherwise:
   `http://<ip>/files/media.config`

Physical G3 established:
- `configFileType = 1`;
- `onlySupportApImport = false`.

Therefore the exact physical catalog URL is:

```text
http://<glassDeviceWifiIP>/files/media.config
```

The G4A2/G4B physical glasses address was `192.168.49.176`, derived at runtime from passive `0x73 / 0x08`.

## Corrected exact parser branch

A deeper bytecode recheck of `AlbumDepository.readPhotoFile` corrects the earlier parser interpretation.

The method:
1. reads `UserConfig.getConfigFileType()`;
2. loads integer constant `2`;
3. branches on `configFileType != 2`.

### configFileType == 2

Cyan:
- constructs a local `File`;
- calls Kotlin `FilesKt.readText$default(...)`;
- passes the resulting String into `AlbumDepository$readPhotoFile$1`;
- obtains a Moshi adapter for `PtPFileModel`;
- calls `JsonAdapter.fromJson(...)`;
- consumes `PtPFileModel.getFile_list()`;
- for every `FileBean`, uses `FileBean.getF()` for later media-file URL construction.

This is the JSON branch.

### configFileType != 2

Cyan:
- constructs a local `File`;
- calls Kotlin `FilesKt.readLines$default(...)`;
- iterates the returned `List<String>`;
- for each line, constructs:
  `http://<glassDeviceWifiIP>/files/<line>`;
- if glasses-log mode is active, uses `/files/log/<line>` instead;
- creates `PictureDownloadBean(String url, String line)`;
- queues each bean in the media-download deque;
- sets the total-file count from the line-list size.

This is the line-oriented catalog branch.

Because the physical value is `configFileType=1`, **this AIMB-G1 uses the line-oriented `media.config` branch**.

## Physical confirmation

G4B2 v0.4.4 physically returned:
- HTTP 200;
- `Content-Type: text/plain`;
- 67 response bytes;
- valid UTF-8;
- no BOM;
- diagnostic line count: 4;
- not JSON before or after BOM/whitespace normalization.

This is consistent with the exact `readLines()` branch.

Evidence:
`docs/testing/results/2026-09-19_G4B2_RESPONSE_SHAPE_PASS.md`

## P2P routing

The exact P2P album path:
- forms the group through Cyan's Wi-Fi Direct helper;
- records P2P connection state;
- calls the catalog download path.

No explicit `ConnectivityManager.bindProcessToNetwork(...)` call appears in this P2P album path. Explicit process-network binding belongs to separate regular-Wi-Fi/AP helpers.

## G4B3 exact parity rule

The final catalog-listing verification may:
- repeat the proven P2P enter / exact-peer association / passive-IP / exit lifecycle;
- make exactly one GET to `/files/media.config`;
- split the bounded UTF-8 response using line semantics equivalent to Kotlin `readLines()`;
- reject/flag blank entries;
- report only:
  - exact non-empty entry count,
  - blank-entry count,
  - extension/type counts from each line,
  - whether entries are relative rather than absolute URLs,
  - whether unsafe path traversal tokens are present;
- never log any actual line, filename or path;
- make zero media-file GET requests.

It must not:
- switch to `vf_list.txt`;
- use the `configFileType=2` JSON parser;
- request a media file;
- follow redirects;
- retry;
- write/delete files;
- log credentials, raw body, filename/path values or response fingerprints;
- use `02 03`, AP fallback, reset/restart/OTA.

G5 remains blocked until G4B3 is physically reviewed.
