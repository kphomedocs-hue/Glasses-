# Cyan G5 refresh/readiness parity trace — 2026-09-19

## Trigger

Physical G5 v0.5.0:
- Phase A catalog: 3 entries / 67 bytes;
- user captured exactly one disposable JPG after transfer exit;
- Phase B catalog: still 3 entries / 67 bytes;
- new-entry delta: 0;
- media-file GETs: 0.

Evidence:
`docs/testing/results/2026-09-19_G5_V0_5_0_SAFE_NO_DELTA.md`

## Finding 1 — Cyan reads media counts before import

Exact Cyan class:
`com.aitowe.aitoglasses.home.PictureFragment`

`loadDataData()` calls:
`readAlbumCounts()`

`readAlbumCounts()` invokes:
`LargeDataHandler.glassesControl(...)`

Recovered payload is the already physically proven G3 media inventory query:

```text
command: 0x41
payload: 02 04
frame: BC 41 02 00 01 13 02 04
```

Its callback reads:
- imageCount;
- videoCount;
- recordCount;
- configFileType;
- onlySupportApImport.

Physical G3 exact response/parser:
- raw byte 7: dataType;
- raw bytes 8..9: imageCount LE16;
- raw bytes 10..11: videoCount LE16;
- raw bytes 12..13: recordCount LE16;
- raw byte 14: configFileType;
- raw byte 15: onlySupportApImport;
- raw byte 16: parser-unconsumed trailing byte.

This query therefore belongs to Cyan's normal album-screen preparation path before the user initiates transfer.

## Finding 2 — Cyan delays catalog retrieval by exactly 1000 ms

Exact method:
`PictureFragment.downloadMediaConfig()`

After P2P/system readiness it obtains `glassDeviceWifiIP` and invokes:

`ThreadExtKt.ktxRunOnUiDelay(..., 0x03e8, PictureFragment$downloadMediaConfig$2)`

`0x03e8 = 1000 ms`.

The delayed lambda `PictureFragment$downloadMediaConfig$2.invoke(...)` then selects the catalog URL from `configFileType`.

For physical `configFileType=1`, it constructs:

```text
http://<glassDeviceWifiIP>/files/media.config
```

and calls:
`AlbumListViewModel.getPhotoTextFile(url, dcimPath, configFileName)`.

Therefore the 1000 ms delay is on the exact physical branch immediately before catalog download.

## v0.5.0 parity gap

v0.5.0:
- did not send `02 04` before entering P2P in each phase;
- started its catalog GET as soon as P2P + passive-IP prerequisites were available;
- did not reproduce Cyan's exact 1000 ms readiness delay.

The physical no-delta result is consistent with reading a manifest that had not yet been refreshed/settled, but static analysis alone cannot prove which omitted behavior is causal.

## Next candidate rule

The next G5 candidate may add **only**:
1. one `0x41 / 02 04` media-count query before P2P enter in each phase;
2. exact parsing of its count/config response;
3. a 1000 ms delay after P2P + passive-IP readiness before each catalog GET.

Everything else stays bounded:
- exact peer only;
- P2P enter once per phase;
- transfer exit once per phase;
- catalog GET once per phase;
- one media GET maximum;
- exact-one new safe relative JPG required before media GET;
- no redirect;
- no retry/resume/Range;
- no AP fallback;
- no `02 03`;
- no raw catalog/remote name/path logging;
- no glasses mutation.

The inventory query is diagnostic/read-only and was already physically validated at G3.  
The delay is exact Cyan parity, not an invented timing value.
