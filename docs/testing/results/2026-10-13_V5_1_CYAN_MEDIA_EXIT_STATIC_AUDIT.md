# V5.1 forensic audit evidence — Cyan APK media GET / EXIT
**Date:** 2026-10-13 · **STATIC ONLY** · **targeted original-DEX audit PASS 27/27** · original preservation safety: **NO-GO**.

## Reproduction input and trust boundary

Owner-supplied Cyan Glasses 1.0.2.18_20260811 `base.apk`, independently SHA-256 checked:
```text
1e700628d76fa4fa84047632e2ccce3673f1a01966fbf8c583985568f3aaaf64
```
The six extracted DEX files were byte-for-byte identical to their corresponding `base.apk` ZIP entries; original binary contents were used to parse Android DEX method and callsite tables, not simulated or guessed from prose. Input APK/DEX copied **only to private ephemeral analysis storage**, absent from GitHub. Public report contains only method identifiers, safe code-unit PCs, checksum evidence, and test outcomes; private APK, raw catalog, addresses and media are not released.

## Verification counter — 27/27 PASS

| Part | Check set | Passed |
|---|---|---|
| 01 Provenance | SHA-256 equals original export manifest | 1/1 |
| 01 Provenance | `classes.dex` through `classes6.dex` byte-for-byte equal to embedded entries in original base APK | 6/6 |
| 02 Downloader and queue | exact `AndroidNetworking.download` at code-unit PC `0x00d8`; exact `ANRequest.startDownload` at `0x00fd`; successful `BlockingDeque.removeFirst` at `0x0027`; executor `execute` at `0x003e`; async local processor at `0x000d`; next queue invocation at `0x0007`; empty queue calls listener `fileDownloadComplete` at `0x0030` | 7/7 |
| 03 EXIT and teardown | `PictureFragment.fileDownloadComplete` invokes `LargeDataHandler.glassesControl` at `0x001e`; exact `fill-array-data` is 3 bytes `02 01 09`; UI runnable calls Wi-Fi `cancelP2pConnection` at `0x0044`, AP disconnect at `0x004a`; EXIT response callback has no calls | 5/5 |
| 04 Error/retry and scope | error counter setter at `0x0018`; reentry at `0x0054`; phone queue clear at `0x0076`; UI error cleanup calls P2P teardown at `0x0056`; processing path invokes `GFileUtilKt.deleteFile(String)` at `0x006d`; processing path contains no direct `glassesControl`; `GFileUtilKt.deleteFile(String)` reaches `java.io.File.delete`; inspected UI error handler does not call `glassesControl` directly | 8/8 |
| **Total** | Direct original-DEX/provenance assertions | **27/27 PASS** |

An initial offline audit harness assertion failed due an **overloaded `invoke` method selector** and then a test predicate that matched `setDownloadErrorCount` but not its generated `access$setDownloadErrorCount$p` bridge. The harness was corrected to select the typed `invoke(AlbumDepository$downloadGlassFile$2)` overload and match actual resolved accessors. **The final 27/27 was computed only after correcting the harness; earlier failing checks are not represented as passed.** No app source files were changed in this static inspection.

## Independent evidence recheck

- Prior Cyan static note: `docs/research/CYAN_EXACT_G5_TRACE_2026-09-19.md` — the main download method, queue and completion path are consistent with original DEX.
- Remote-retention no-GET control: `releases/v0.6.5/builds/run-36893102299-attempt-1/reports/2026-10-01_G6A_SINGLE_CAPTURE_RETENTION_PHYSICAL_PASS.md` — zero media-file GET, new JPG persisted across EXIT/cleanup/fresh reconnect.
- Source catalogue-loss positive: `releases/v0.6.7/builds/run-36898159938-attempt-1/reports/2026-10-01_G6A_V0_6_7_POST_GET_REMOTE_CONSUMPTION_PHYSICAL_PROOF.md` — one successful JPG GET+EXIT, exact one catalog JPG missing after post-session reconnect, baseline stable.
- Bytecode recheck: `docs/research/V5_1_CYAN_MEDIA_DOWNLOAD_EXIT_FORENSICS_2026-10-13.md`, including exact call paths, local deletion helper and response callback.
- Independent negative-hypothesis review: `docs/research/V5_1_CYAN_MEDIA_EXIT_RED_TEAM_2026-10-13.md`, 17 challenges and safety decisions.

## Interpretation controls

PASS means **the audited original DEX methods contain exactly the described calls**. It does NOT mean GET is non-destructive, that all vendor firmware/endpoints are understood, that HTTP GET caused the loss at a precise instant, or that original media survives transfer. No new practical non-consumptive alternative has been established.

**Physical G6A OPEN / G6B BLOCKED. C3 preservation NO-GO.** No radio communication, Android APK build, BLE control/EXIT command execution, HTTP GET or media import occurred during V5.1.
