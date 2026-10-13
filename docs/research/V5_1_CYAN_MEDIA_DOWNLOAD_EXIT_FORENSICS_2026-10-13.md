# V5.1 — Cyan Glasses media GET, local processing, queue completion and BLE EXIT
**2026-10-13 · independent targeted ORIGINAL-DEX static recheck · no glasses access or HTTP requests**

## Executive finding and immutable safety decision

**27/27 direct-bytecode/provenance assertions PASS**, after verifying the exact user-supplied 85-code Cyan Glasses 1.0.2.18_20260811 `base.apk` and its six embedded DEX files. This is a genuinely new inspection of the **original bytes**, not just a retelling of the retained 2026-09-19 research note.

The inspected normal P2P media flow is:
```text
read configFileType=1 catalogue
  -> construct queue of relative /files/ URLs (previous G4B research)
  -> AlbumDepository.downloadGlassFile
       -> AndroidNetworking.download(url, LOCAL_DIRECTORY, LOCAL_NAME)
       -> startDownload(DownloadListener)
  -> DownloadListener.onDownloadComplete
       -> for normal media: removeFirst() from PHONE BlockingDeque
       -> dispatch LOCAL processDownloadedFile on ExecutorService
       -> callback schedules downloadGlassFile again
  -> on now-empty PHONE queue: WifiFilesDownloadListener.fileDownloadComplete()
  -> PictureFragment.fileDownloadComplete()
       -> glassesControl(byte[] {0x02,0x01,0x09}, response callback)
       -> UI cleanup; P2P init/cancel or AP disconnect branches
```

**No source-preservation guarantee was recovered.** The physical v0.6.7 experiment proved that the exact fetched JPG disappeared from the remote inventory/catalog after a complete GET+EXIT cycle. The static flow **does not** establish if firmware commits that transition during GET, completion, EXIT, asynchronous state handling, or a combination. **NO GO for real media GET / syncing release.**

## Part V5.1-A — Original binary provenance VERIFIED

The files were privately read from the owner's previously uploaded Cyan APK/DEX assets in the Project Library. They have **not** been copied into this public repo or attached as a distributable artifact.

| Verification | Evidence |
|---|---|
| Original app package | `com.aitowe.aitoglasses`, Cyan Glasses 1.0.2.18_20260811, versionCode 85 (retained original export manifest) |
| `base.apk` SHA-256 | `1e700628d76fa4fa84047632e2ccce3673f1a01966fbf8c583985568f3aaaf64` — computed afresh from private original bytes and agrees with historical manifest |
| DEX archive entries | `classes.dex`, `classes2.dex` to `classes6.dex`: **six out of six full-byte comparisons matched** against embedded DEX entries in the verified base APK |
| Parser basis | DEX header string/type/prototype/method/class-data/code-item tables parsed directly; invoke targets resolved via method indexes. Offsets below are **DEX 16-bit code-unit offsets**, *not* file offsets or line numbers |
| Static audit | 27 of 27 targeted assertions passed (one APK SHA, six embedded DEX equality checks, twenty targeted branch/call/helper assertions). Audit run on private local bytes; no binaries were published |

Independently calculated DEX SHA-256 digests for source custody (not needed for device association):
- `classes.dex`: `96462cbb62d5af6f2d9152384f6b1c053a82ae6d386523a546738f492554b891`
- `classes2.dex`: `ace454296a80e540a35fbc49157e543d2b82cf6d2d66ee23a712001023c717d6`
- `classes3.dex`: `bd3bf24b405e39aaaca7ca23d2a83d307b82e1be576dbcc20071b1b9c5e4fcd1`
- `classes4.dex`: `196adf42fb041386c8d0c31a7f47dfd45b959e3ef27866d22aa187407c2f6761`
- `classes5.dex`: `f391e3d3ca404e08a0a19e50447d397c2ba94f6c875621c6bc0113695942d30a`
- `classes6.dex`: `c3c4e0487d1a569779339578b251a786db1158687af456d7dfd5c050256affef`

## Part V5.1-B — Exact downloader branch VERIFIED

**DEX:** `classes4.dex`; `Lcom/aitowe/aitoglasses/depository/AlbumDepository;->downloadGlassFile()V`.

| Offset | Resolved bytecode call/effect | Classification |
|---|---|---|
| `0x001e–0x0030` | On empty `fileQueue`, `BlockingDeque.isEmpty()` followed by `WifiFilesDownloadListener.fileDownloadComplete()` | Direct bytecode, PHONE queue |
| `0x0052–0x005a` | `failCount` guard and `BlockingDeque.removeFirst()` before selecting next bean | Direct bytecode, PHONE queue skip |
| `0x0062–0x0064` | `BlockingDeque.peekFirst()` | Direct bytecode, PHONE queue |
| `0x00a0–0x00af` | Conditional `configFileType` handling and `PictureDownloadBean.setFileName(...)` | Direct bytecode, PHONE filename |
| `0x00c8–0x00d8` | `PictureDownloadBean.getPath()`, `GFileUtilKt.getAlbumDirFile()`, `File.getAbsolutePath()`, `PictureDownloadBean.getFileName()`, then `AndroidNetworking.download(url,localDir,localName)` | Direct call, conventional media download |
| `0x00dc–0x00fd` | `setTag`, `Priority.MEDIUM`, `build()`, `setDownloadProgressListener(...)`, `startDownload(DownloadListener)` | Direct call |

The **traced caller** contains no `HEAD`, `Range`, custom header, proprietary `keepOriginal` flag or alternate single-media endpoint configuration. This does **not** prove no such capability exists anywhere in firmware or the bundled HTTP library.

An important forensic/privacy note: the original Cyan caller constructs XLog messages containing the source URL and local filename. Those values were NOT extracted or committed. Our implementation must not copy that logging style.

## Part V5.1-C — Successful download callback, phone-local processing and queue exit VERIFIED

**DEX:** `classes4.dex`;
`Lcom/aitowe/aitoglasses/depository/AlbumDepository$downloadGlassFile$2;`.

- `onDownloadComplete()V`:
  - `0x0008–0x0016`: treats the `.log` suffix as a separate branch which directly schedules another `downloadGlassFile`; it is **not** the normal JPG flow.
  - `0x0017–0x001e`: reset download error/fail counters for ordinary media.
  - `0x0021–0x0027`: obtains the phone-side `fileQueue` and calls `BlockingDeque.removeFirst()`. **This is not a remote delete.**
  - `0x002f–0x003e`: posts a `Runnable` to the phone-side `albumImportExecutor`.
- `onDownloadComplete$lambda$0(...)`:
  - `0x000d`: calls `AlbumDepository.processDownloadedFile(PictureDownloadBean)`.
  - `0x0012–0x0033`: schedules a follow-up runnable on the UI thread, including exception-handling paths.
- `AlbumDepository$downloadGlassFile$2$onDownloadComplete$1$1.invoke(...)`:
  - `0x0007`: schedules `AlbumDepository.downloadGlassFile()` again.
- When the phone queue then becomes empty, `AlbumDepository.downloadGlassFile()` directly calls `WifiFilesDownloadListener.fileDownloadComplete()` at `0x0030`.

**Ordering established only at app callback/invocation level:** callback success → phone deque advancement → local file processing task → next queue request → empty-queue callback. Actual cross-thread execution/completion and device firmware behavior are not resolved by this static trace.

### Resolve the misleading `deleteFile` symbol

`AlbumDepository.processDownloadedFile(...)` (classes4.dex) uses `File.getAbsolutePath()` at `0x0069` and `GFileUtilKt.deleteFile(String)` at `0x006d`, around a local file-size guard. That helper in **classes.dex** is:
`Lcom/aitowe/aitoglasses/all/utils/GFileUtilKt;->deleteFile(Ljava/lang/String;)Z`; it constructs a `java.io.File`, checks `exists()/isFile()` and invokes **`java.io.File.delete()Z` at `0x0011`**. The sibling `deleteFile(java.io.File)` also invokes `File.delete()`.

**Conclusion:** this identified `deleteFile` operation acts on a **phone-local downloaded file**, not on glasses-side HTTP storage. No `glassesControl` invocation was found in the inspected `processDownloadedFile` method. This disambiguates one deceptively named deletion call; it does **not** prove all firmware-side deletion pathways absent.

## Part V5.1-D — Exact EXIT / P2P teardown and failure path VERIFIED

**DEX:** `classes4.dex`;
`Lcom/aitowe/aitoglasses/home/PictureFragment;->fileDownloadComplete()V`.

- `0x0006–0x000c`: set completion state and post UI runnable `PictureFragment$fileDownloadComplete$1`.
- `0x0014–0x001e`: constructs an exact three-byte array from `fill-array-data` and calls `LargeDataHandler.glassesControl([B, ILargeDataResponse)` at `0x001e`. **Exact bytes: `02 01 09`.** This is the previously documented BLE transfer EXIT (not a remote delete command).
- The response callback `PictureFragment.fileDownloadComplete$lambda$21(I,GlassModelControlResponse)` has **no calls and only returns**. No acknowledgement-based success gate is evident *in this specific callback*.
- `PictureFragment$fileDownloadComplete$1.invoke` calls `WifiP2pManagerSingleton.initP2P()` at `0x003b`, then `cancelP2pConnection()` at `0x0044` on its P2P branch; AP branch calls `TempWifiHelper.disconnect()` at `0x004a`. That P2P helper's own `cancelP2pConnection()` invokes Android `WifiP2pManager.cancelConnect()` and `removeGroup()`. These are Android phone Wi-Fi Direct operations, not an HTTP delete request.
- The UI P2P cleanup runnable was *scheduled before* the BLE EXIT invocation in `fileDownloadComplete()`; asynchronous thread scheduling prevents asserting which happens first in real wall-clock time. **Do not claim EXIT acknowledgement precedes group teardown.**

**Error and retries, also directly rechecked:**
- `AlbumDepository$downloadGlassFile$2.onError(ANError)`: increments fail/error counters; notifies the error listener; at `0x0054` can invoke `downloadGlassFile()` again while a bounded retry condition holds, else at `0x0076` clears the **phone deque**. The method's `const/4` threshold and `if-gt` branch indicate a threshold of **2**, allowing reentry at counts 1 or 2 and clearing at a higher count (under normal starting state). **This retry policy is not safe to copy to potentially consumptive media reads.**
- `PictureFragment.fileDownloadError(I,I)` updates UI/error state. Its UI handler `PictureFragment$fileDownloadError$2.invoke` invokes `AlbumDepository.cleanFileQueue()`, `WifiP2pManagerSingleton.cancelP2pConnection()` (or AP disconnect), and updates UI. The specific inspected error methods contain **no direct BLE `glassesControl(02 01 09)` call**. That is a scoped source-fact, **not** a universal claim that firmware never exits on an error.
- The error callback may cause both a downloader reentry and UI cleanup scheduling. Without runtime ordering, the net effect cannot be proven safe for interrupted/consumptive downloads.

## Contrast with strongest physical controlled experiment

| Control | Physical evidence, frozen in `releases/` | What it proves |
|---|---|---|
| No media-file GET | v0.6.5: post-capture exactly one new JPG still present after verified transfer EXIT, group teardown and fresh 5-second reconnect | A normal idle/capture/catalog/EXIT-only cycle **did not** remove this JPG |
| Exactly one successful JPG GET + EXIT | v0.6.7: one new selected JPG returned HTTP 200 (599645 bytes). On post-EXIT and fresh reconnect, image inventory was lower by one and the exact downloaded identity absent; all previous JPGs retained | The tested completed transfer lifecycle is **associated with remote catalogue consumption** |
| Cyan app bytecode | The downloaded local file is processed, a **local queue** is dequeued, then EXIT is sent | Disproves *only* the hypothesis that the observed Java `BlockingDeque.removeFirst` or `java.io.File.delete` is a remote deletion command |

Sources:
- `releases/v0.6.5/builds/run-36893102299-attempt-1/reports/2026-10-01_G6A_SINGLE_CAPTURE_RETENTION_PHYSICAL_PASS.md`
- `releases/v0.6.7/builds/run-36898159938-attempt-1/reports/2026-10-01_G6A_V0_6_7_POST_GET_REMOTE_CONSUMPTION_PHYSICAL_PROOF.md`
- `docs/research/CYAN_EXACT_G5_TRACE_2026-09-19.md` (historical account independently rechecked in part)
- `docs/research/G6A_STEP2_NONCONSUMPTIVE_MEDIA_PATH_AUDIT_2026-10-11.md`

## V5.1 verified/unknown matrix

**Verified (direct original-bytecode):**
- exact app identity and all six extracted DEX bytes match package;
- ordinary local media URL downloaded with `AndroidNetworking.download`;
- phone deque removal on media callback success; asynchronous local processing;
- empty phone queue completes import and triggers BLE EXIT `02 01 09`;
- P2P phone cleanup, without acknowledgement gating in the inspected EXIT response callback;
- one named `deleteFile` helper deletes a phone-local `java.io.File`;
- error path may retry the media downloader and schedules phone queue/network teardown.

**Unknown / not proved:**
- vendor firmware's HTTP GET side effects, HTTP server code, whether bytes are deleted or only catalogue entry hidden;
- whether GET, ACK, EXIT, timing, or another transition individually causes removal;
- whether MP4/OPUS are similarly consumptive;
- whether another officially supported original-preserving endpoint/mode exists;
- whether the current paired glasses Wi-Fi peer can be authenticated;
- whether hidden third-party library defaults materially alter a request (only the app's exact caller was inspected for custom headers/methods);
- any claim that a new physical request is safe.

## V5.1 stop condition

**STATIC FORENSIC INVESTIGATION: COMPLETE for these exact, bounded methods.** Not a full closed-world proof over all Cyan/firmware code. The original preservation problem is **still unresolved** and **G6A OPEN / G6B BLOCKED**. No APK was built, no live radios used, no media HTTP request sent, no manufacturer contacted. See companion independent `docs/research/V5_1_CYAN_MEDIA_EXIT_RED_TEAM_2026-10-13.md` and result `docs/testing/results/2026-10-13_V5_1_CYAN_MEDIA_EXIT_STATIC_AUDIT.md`.
