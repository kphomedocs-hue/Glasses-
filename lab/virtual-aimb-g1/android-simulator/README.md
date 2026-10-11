# AIMB-G1 Android simulator — SOURCE ONLY, no APK

Android SDK 36, Gradle 9.6, Java 17, independent test-only application ID. Uses **sourceSets paths to the same** VGL classes, real protocol/identity implementations and durable RecoveryImportStore already exercised by the 96 JVM scenarios. No copies of shared policy code.

This is *not* the production K G1 app. No HTTP client, no mock server, no network, Bluetooth, location, Wi-Fi Direct or Internet permissions; nothing can access the actual glasses. No physical commands, no source deletion, no external photos or recordings. A local in-memory marker-only JPG fixture is written to Android private no-backup storage through the actual RecoveryImportStore transaction code and checked on reopening. The synthetic Activity exercises the actual shared BLE-control state machine through fake callbacks, no real radios, plus the capture policy.

CI command (from this directory):

    gradle --no-daemon :app:compileDebugJavaWithJavac :app:compileDebugAndroidTestJavaWithJavac :app:processDebugMainManifest

The CI workflow does not assemble APKs, boot Android emulators, connect to glasses, or upload installation artifacts. The separately compiled instrumentation hook for twice-run local receipts is NOT YET EXECUTED. Android runtime and filesystem checks therefore remain PENDING until an emulator-only run is separately authorized.

Known Android runtime APIs here should be tested first on an Android 16/API 36 emulator (target and minimum 36 for this lab). Process kill, storage pressure and transport faults remain untested in Android. No production simulator-flavor/normal-flavor release hardening has been implemented.

The historical physical JPG GET consumed the remote original. **Real media GET is blocked. G6A OPEN; G6B BLOCKED.**
