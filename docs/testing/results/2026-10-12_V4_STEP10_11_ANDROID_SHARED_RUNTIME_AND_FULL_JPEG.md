# V4 Steps 10 and 11 — shared coordinator API36 emulator runtime + synthetic JPEG decode

Date: 2026-10-12. **PASSED IN ANDROID EMULATOR SCOPE ONLY.** Not a physical glasses/phone, vendor codec-parity or source-original retention test.

## Exact source and CI
All four final workflows ran against the **same source SHA** `46c242cd183a116521a3d81477903dddcbef3695`:
- [Android API36 emulator run 38280843057](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38280843057): **SUCCESS**.
- [Android source compile-only run 38280843127](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38280843127): **SUCCESS**.
- [Offline 96 and existing host suite 38280843183](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38280843183): **SUCCESS**.
- [Repository hygiene run 38280843017](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38280843017): **SUCCESS**.

### Raw emulator log evidence
```text
V4-EMU V4_SHARED_FULL_JPEG_COMMITTED PASS
V4-EMU V4_SHARED_REOPEN_NO_DUPLICATE PASS
VGL-EMU-CRASH-V4: instrumentation process terminated without normal result
V4-EMU V4_SHARED_HALT_RECOVERED_NO_REFETCH PASS
V4-EMULATOR-SHARED PASS: one complete locally decoded synthetic JPEG; new coordinator persisted and recovered after actual test-process death
```

Legacy pre-existing `VGL_EMULATOR 2/2` and `VGL_EMULATOR_HARD_STOP 6/6` also regressed successfully as part of the existing runner; **do not add these again as new test counts**.

## Actual new Android execution
- The **same production-oriented** `SharedTransferCoordinator` class from `source/g1-shared-workflow/src` runs inside the API36 virtual device, with an Android-instrumentation-only fixture permit in `app/src/androidTest/java/com/parkarsite/g1/workflow/SharedCoordinatorInstrumentationProbe.java`.
- Synthetic baseline and one-new-JPG catalogs are checked by the shared coordinator, along with fake peer/group/IP evidence; all these values are injected and **not authentication of hardware**.
- Synthetic JPEG is created **inside Android** using a 32×24 ARGB_8888 raster and `Bitmap.compress(JPEG,90,...)`. A real `BitmapFactory.decodeByteArray` successfully decodes it with expected width/height before import. It is neither a seven-byte marker-only fixture nor an AIMB-G1 capture.
- The shared coordinator's host-test-only import path delegates to the actual `RecoveryImportStore`: the instrumentation verifies `FETCH_MAY_HAVE_STARTED` journal was made before Source callback, writes synthetic bytes once, then validates fsync/atomic file and receipt. The **stored** JPG is decoded again on Android, after the durable commit.
- One verified synthetic EXIT + group-absence tuple closes the observation. It is test data; no real BLE/P2P/HTTP operations occur.
- The test force-stops the package and the next independent instrumentation process validates file+receipt using `recover()` and `isCommittedAndIntact()`, decodes stored JPEG again, and verifies the public/default `SharedTransferCoordinator` constructor still blocks media Source with zero invocation.
- A distinct crash mode executes the **new shared coordinator**, not the earlier isolated storage-only harness, and deliberately executes `Runtime.halt(73)` at `RecoveryImportStore.Barrier.AFTER_VERIFIED_JOURNAL`. It emits no normal PASS/FAIL. A fresh subsequent Android process promotes already VERIFIED local bytes/receipt and decodes the recovered JPG. No Source redownload is attempted or performed.
- One API36 Google APIs x86_64 emulator only; both ephemeral app+instrumentation APKs have **zero requested permissions**, are installed **only in CI virtual device**, removed by runner cleanup, and not uploaded as releases.

## Failed predecessor and correction
The first newly modified API36 source compile run [38280700416](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38280700416) failed because the new AndroidTest code called `Files.readString(Path)`, which is not available in the Android API target's core-library surface. This was an instrumentation-source compatibility issue, not proof of runtime failure. Replaced it with a 4096-byte-bounded `Files.readAllBytes` and UTF-8 conversion in commit `46c242cd183a116521a3d81477903dddcbef3695`. All final compile/runtime/offline/hygiene workflows then passed. The first emulator run on the uncorrected source was not promoted.

## Red-team limits and blockers
1. **Test-only authorization is not production permission.** `HostFixturePermit` is package-private, and this instrumentation's implementation is kept in `androidTest` rather than the application's `main` source. The shipping constructor always blocks media. Step 13 must still audit that no fixture capability is packaged in any final signed production APK; package-private access by itself is not a sufficient release guarantee.
2. The fake BLE, peer, group, IP, inventory and EXIT values are not independent vendor or Android Wi-Fi Direct attestations. No real glasses were touched.
3. Decoder success proves **only this generated raster JPG** decodes on API36. It does not prove EXIF parity, glasses JPEG encoding, orientation, MP4/OPUS compatibility, full fidelity or actual capture timestamps.
4. The crash test proves real Android instrumentation JVM termination at one VERIFIED-barrier of the **new shared coordinator**. It is not power-loss or a complete replication of the prior six storage barrier tests under the new coordinator.
5. `RecoveryImportStore` still allocates only JPG daily files; persistent shared JPG/MP4/OPUS numbering/migration is **not implemented in this milestone** (V4 Step 12).
6. A successful real JPG HTTP GET + EXIT was previously associated with disappearance of the exact original on the glasses; no preserving media read has been established and **no real GET is allowed**. Current actual Wi-Fi Direct peer identity also remains unresolved.

## Gate decision
**V4 Step 10: PASS** — one new same-coordinator Android synthetic end-to-end runtime + fresh-process durable reopen + one verified-journal JVM abrupt termination/recovery.
**V4 Step 11: PASS IN GENERATED-JPEG DECODE SCOPE ONLY** — complete Android-generated JPEG encoded, decoded before and after durable local commit and recovery. Vendor fidelity/metadata parity remains unverified.
**Step 12 onward: NOT PROMOTED by this test.**
**G6A OPEN; G6B BLOCKED.** No final production APK or physical media transfer authorized.
