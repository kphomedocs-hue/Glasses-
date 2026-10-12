# Site Capture App

Future production application.

Legacy reference implementation:
- `archive/site-capture/v0.4/`

Target behavior:
- glasses act as capture source,
- phone silently imports new media,
- one chronological daily counter across supported media,
- preserve capture timestamps,
- never overwrite originals,
- robust dedup/retry/integrity verification,
- minimal UI.

Development should consume modular components under `modules/` rather than reviving the legacy monolithic transport directly.


## 2026-10-12 V4 source-only offline milestone
`src/main/java/com/parkarsite/g1/sitecapture/SiteCaptureOfflineCore.java` is a production-facing **local-only** backend for durable mixed-media receipt recovery and review. It accepts an app-private filesystem root and has **no BLE, Wi-Fi Direct, HTTP or remote media GET capability**. Default real sync/connect/read methods are false.

`android-preview/` contains an isolated API36 **read-only UI source prototype** that displays local verified counts and warnings using `getNoBackupFilesDir()`. It intentionally has no permissions or remote-connection controls. It was **compiled only** — no installable APK was built or signed and no emulator runtime/screenshot result is claimed. Host JVM test run 38284068924 PASS (16 assertions); Android compile-only 38284338093 PASS; full report `docs/testing/results/2026-10-12_V4_SITE_CAPTURE_PRODUCTION_OFFLINE_UI_SCAFFOLD.md`.

### Hard product blockers
- Real JPG GET+EXIT was associated with disappearance of the exact original on the glasses; no original-preserving import exists in proven evidence.
- Real Wi-Fi Direct peer identity is unknown; BLE name/suffix matches alone do not authorize connection.
- Android live BLE/P2P/HTTP adapters and verified vendor media fidelity do not yet exist in the new shared shipping controller.
- The existing fixture-grant path in the shared coordinator must be excluded and independently audited in any future signed release.

**Do not install this preview on a real phone as a substitute for the final app. No live transfer is authorized. G6A OPEN; G6B BLOCKED.**


### UI preview later validation / security hardening (same date)
The initial source-only milestone above was extended. The final API36 emulator run [38285067667](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38285067667) **PASS** confirms that the Android UI actually displays its transfer-disabled warning and local archive title on the virtual device. Final compile-only [38285067603](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38285067603) **PASS** confirms there are no transfer-coordinator classes compiled into the preview and no permissions/APK produced in that compilation. An ephemeral zero-permission preview APK was built **only inside the emulator UI workflow**, not published. Neither the preview nor its test APK is approved for a physical phone.
