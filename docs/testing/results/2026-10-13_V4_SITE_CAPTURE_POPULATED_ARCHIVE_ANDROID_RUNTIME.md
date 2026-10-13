# K Site Capture — populated local archive / recovery warnings in real API36 Android UI

Date: 2026-10-13. **PASS on a GitHub-hosted Android 16/API36 emulator, synthetic app-private media only.** No physical glasses/owner handset, radios, HTTP, media GET or released APK.

## Exact evidence
- Source commit tested: `76c80dcdcb5a6abc6b6558df75406a396309b095`.
- Android emulator workflow [38340827600](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38340827600): **SUCCESS**.
- Matching source-only compile [38340827614](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38340827614): **SUCCESS**.
- Matching production-facing JVM facade [38340827615](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38340827615): **SUCCESS**.
- Matching repository hygiene [38340827599](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38340827599): **SUCCESS**.

### Raw emulator milestones
```text
V4-SITE-CAPTURE-UI PASS: API36 emulator visually exposes local archive and transfer-disabled warning
V4-SITE-CAPTURE-ISOLATION PASS: no permissions, test APK not uploaded, virtual device only
V4-SITE-CAPTURE-ARCHIVE-UI LOCAL_ONLY_SAFE PASS
V4-SITE-CAPTURE-ARCHIVE-UI LOCAL_ONLY_SAFE PASS
V4-SITE-CAPTURE-ARCHIVE-UI LOCAL_REVIEW_NO_REMOTE_RETRY PASS
V4-SITE-CAPTURE-ARCHIVE-UI LOCAL_LEDGER_UNVERIFIED PASS
V4-SITE-CAPTURE-ARCHIVE-STATE 4/4 PASS: restart, mixed receipt counts, uncertain and corrupt journal fail-closed
```

## Actual executed workflow (new evidence, not a repeat of old storage-only tests)
1. `OfflineArchiveInstrumentation` in Android `app/src/androidTest/java` creates three **synthetic** private media records: a complete, bitmap-decodable 24×16 JPEG; shape-only ISO-BMFF MP4 (`ftyp`+`mdat`); shape-only Ogg/OpusHead. It writes these through the exact same `MixedMediaRecoveryStore` source compiled into the read-only preview.
2. Original **production main application** `OfflinePreviewActivity`, without any fixture endpoint, opens the app-private store and renders `Locally verified files: 3`, `Uncertain imports: 0`, `Archive status: LOCAL_ONLY_SAFE`. Actual Android `uiautomator` on-screen dump with scrolling checks the text.
3. The app and instrumentation process are force-stopped, then the app Activity is separately restarted **without reseeding**. The UI still shows three verified media and zero manual review; a second fetch is never invoked by the app.
4. Test-only instrumentation prepares a fourth OPUS record, durably marks `FETCH_MAY_HAVE_STARTED`, then injects a controlled exception **before invoking any Source bytes**. After a fresh Activity restart, UI shows three verified files, one uncertain record and `Archive status: LOCAL_REVIEW_NO_REMOTE_RETRY`.
5. Test-only instrumentation corrupts one known synthetic JPG transaction file. On a fresh Activity restart, the production facade fails closed and the UI shows `LOCAL_LEDGER_UNVERIFIED`, zero **claimed** verified files and a manual-review warning; no incorrect recovery success.
6. Both virtual-device app and separate instrumentation APK requested **zero permissions**, were installed only on the GitHub emulator and are removed in trap cleanup. Test-only fixture code is not placed in the app's main source tree. Source-compile workflow still excludes `SharedTransferCoordinator`, `SharedObservationController`, `SharedTransferContracts` from the preview's Java compilation; no media GET or BLE/P2P actions are possible from this preview.

## Red-team limits
- `LOCAL_LEDGER_UNVERIFIED` indicates unreadable metadata, not that pre-existing local files were deleted; the UI intentionally declines to count them as verified until review.
- The three media fixtures are **not real glasses files**. MP4/OPUS shapes are neither decoder/playback tests nor proof of actual vendor metadata fidelity.
- The Android preview still targets a separate proof-of-concept app package. The shipping sync app's actual BLE, Wi-Fi Direct, HTTP, peer authentication and originals-preservation behaviors remain unimplemented/unverified.
- The screen test reads accessibility hierarchy text rather than capturing/analyzing screenshots; it establishes presence/visibility of warnings and counts, not full visual design approval.
- These tests do **not** establish whole-device power-loss durability. Prior Android instrumentation crash-recovery proof remains separately scoped.
- No signed, published, or physically installed APK is approved by this milestone.

## Gate
**Populated archive UI / restart / unknown-remote / corrupt local ledger: 4 of 4 PASS in synthetic API36 emulator scope.**
**Real G6A OPEN; G6B BLOCKED. C1 HOLD, C2 BLOCKED, C3 NO-GO, C4 BLOCKED.**
