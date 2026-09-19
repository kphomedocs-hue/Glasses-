# Exact Cyan G5 single-file download trace — 2026-09-19

## Evidence input

Analysed package:
- Cyan Glasses `1.0.2.18_20260811`
- package `com.aitowe.aitoglasses`
- split export SHA-256 `1328b3c025f43c06b2a0674d4c17890ec4b76cb6487196a84aa27b2335c2fc49`
- extracted `base.apk` SHA-256 `1e700628d76fa4fa84047632e2ccce3673f1a01966fbf8c583985568f3aaaf64`

The third-party APK is not committed to the public repository.

## Physical prerequisite

G4B3 physically established the `configFileType=1` catalog as three safe relative entries:
- two `.jpg`;
- one `.opus`.

Actual catalog line values remain private and were not logged.

Evidence:
`docs/testing/results/2026-09-19_G4B3_CATALOG_LINE_PASS.md`

## Exact Cyan queue construction

For physical `configFileType=1`, `AlbumDepository.readPhotoFile`:
1. reads `media.config` with Kotlin `readLines()`;
2. preserves the returned list iteration order;
3. for each line constructs:
   `http://<glassDeviceWifiIP>/files/<line>`;
4. creates `PictureDownloadBean(path=url, fileName=line)`;
5. appends the bean with `BlockingDeque.putLast(...)`;
6. sets `totalFiles` from the line-list size.

No static evidence in this branch identifies the first or last line as the newest capture. Therefore G5 must not choose an existing entry merely by queue position.

## Exact Cyan media-file request

`AlbumDepository.downloadGlassFile()`:
- peeks the first queued `PictureDownloadBean`;
- reads `bean.getPath()` as the media URL;
- selects Cyan's local album directory and local filename;
- invokes:
  `AndroidNetworking.download(url, albumDirAbsolutePath, fileName)`;
- sets tag `download_file`;
- sets `Priority.MEDIUM`;
- builds the request;
- attaches a download-progress listener;
- calls `startDownload(DownloadListener)`.

No alternate media endpoint is selected in this method.

No custom request header, Range header, HEAD request, redirect policy override or explicit resume configuration is visible in the caller before `startDownload`.

## Cyan completion/error flow

On successful media download:
- Cyan processes the downloaded local file;
- removes/advances the queue;
- continues with the next queued item;
- when the queue is empty, it invokes the album download-complete callback.

`PictureFragment.fileDownloadComplete()` then follows the already recovered transfer-exit path and performs P2P/AP cleanup.

On error, Cyan maintains failure counters and can call its download routine again. That application-level retry behavior is **not** copied into the first G5 diagnostic.

## G5 safety decision

The first G5 must download exactly one **new disposable test photo**, not an arbitrary pre-existing catalog entry.

Because catalog queue order is not proven to be chronological, safe selection requires set-difference identification rather than first/last-entry guessing.

Approved design:
1. Phase A:
   - enter P2P once;
   - exact-peer association;
   - passive `0x73 / 0x08` IP;
   - GET `/files/media.config` once;
   - retain validated catalog lines **in memory only**;
   - log only counts/types;
   - exit transfer and remove P2P group.
2. User physically captures **one disposable JPG test photo** while transfer mode is off.
3. Phase B, after explicit user button:
   - re-enter P2P once;
   - repeat exact association/passive-IP path;
   - GET `/files/media.config` once;
   - compute new-entry set difference in memory;
   - require exactly one new safe relative entry and require extension `.jpg`;
   - construct only `http://<passive-IP>/files/<new-entry>`;
   - perform exactly one media GET;
   - no redirect and no retry;
   - stream to one app-private temporary file with a hard byte cap;
   - verify HTTP 200, non-empty result, cap not exceeded, Content-Length when present, and JPEG signature;
   - never log the remote line/filename/path;
   - exit transfer once and clean up.
4. Stop and review. No second media file.

If the catalog delta is zero, greater than one, unsafe, or not JPG, G5 must abort before any media GET.

## Prohibited in G5

- downloading any baseline/pre-existing catalog entry;
- queue-position guessing;
- OPUS/video download;
- more than one media GET;
- retry/resume/range behavior;
- redirects;
- arbitrary URL input;
- raw catalog logging;
- remote filename/path logging;
- response/media fingerprint logging;
- file deletion or mutation on the glasses;
- AP fallback;
- `02 03`;
- reset/restart/OTA.

This gate validates byte transfer only. Production naming, import ledger, multi-file sync and post-processing remain later gates.
