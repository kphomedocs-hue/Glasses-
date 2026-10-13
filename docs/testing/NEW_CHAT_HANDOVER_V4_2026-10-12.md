# LATEST V5.1 STATIC CYAN FORENSICS OVERRIDE — 2026-10-13 READ FIRST
**V5.1 COMPLETE for narrowly inspected original APK/DEX media-download, local processing, error/retry and EXIT call path.** User-supplied original Cyan APK SHA-256 `1e700628d76fa4fa84047632e2ccce3673f1a01966fbf8c583985568f3aaaf64` verified; all six extracted DEX files byte-identical to APK entries; **27/27 private offline original-bytecode assertions PASS**. Detailed report `docs/research/V5_1_CYAN_MEDIA_DOWNLOAD_EXIT_FORENSICS_2026-10-13.md`; 17-point independent red-team `docs/research/V5_1_CYAN_MEDIA_EXIT_RED_TEAM_2026-10-13.md`; exact assertions and provenance `docs/testing/results/2026-10-13_V5_1_CYAN_MEDIA_EXIT_STATIC_AUDIT.md`. No proprietary binary committed.

Critical distinction: `AlbumDepository$downloadGlassFile$2.onDownloadComplete` advances a PHONE queue. `GFileUtilKt.deleteFile(String)` found in local postprocessing uses `java.io.File.delete` on PHONE. Empty queue triggers BLE `02 01 09` EXIT and P2P/AP teardown; its specific response callback is empty, so acknowledgement-before-cleanup was NOT proved. `onError` retries media downloads; do NOT copy. The **actual firmware** may still consume at GET/EXIT/another asynchronous transition; v0.6.7 physical exact JPG disappeared while v0.6.5 no-media-GET controls retained JPG. **No preserving transfer found/proven. G6A OPEN; G6B BLOCKED; C3 NO-GO.** Never authorize a real media GET or physical test on this static result.

Next V5.2: strictly offline causal evidence matrix, manufacturer-supported preservation source request and a scoped validation decision. Do not re-run V5.1 or replace real DEX with mocked symbol searches. Verify GitHub HEAD and hygiene before proceeding.

---

# LATEST CONTINUATION — 2026-10-13 READ FIRST
The K Site Capture zero-permission **read-only preview** now displays verified app-private synthetic JPG/MP4/OPUS counts, survives an Activity restart and warns on interrupted imports and corrupted local journals. **API36 UI 4/4 PASS** run `38340827600` and **Android15/API35 UI 4/4 PASS** run `38341334991`. Android16/API36 regression `38341308157` PASS. Preview minSdk lowered to 35 after API35 emulator verification. New details `docs/testing/results/2026-10-13_V4_SITE_CAPTURE_POPULATED_ARCHIVE_ANDROID_RUNTIME.md`, `docs/testing/results/2026-10-13_V4_SITE_CAPTURE_API35_API36_COMPATIBILITY.md` and final section of `LATEST_CHECKPOINT.md`.

**DO NOT mistake app-private synthetic UI tests for real AIMB-G1 syncing.** Read-only preview has no compiled transfer coordinator, no radio/network/GET permissions, no real peer identity, no real media transferred and no signed production release. Frozen v0.6.7 evidence indicates exact fetched JPG disappeared after GET+EXIT, so original-preserving transfer remains unproved and blocked. **G6A OPEN, G6B BLOCKED.**

---

# NEWEST V4 POST-PLAN OVERRIDE — VERIFIED LOCAL-ONLY ANDROID PREVIEW
2026-10-12: `apps/site-capture/` now contains a **production-facing offline/local-only facade** and `android-preview/` read-only UI, compiled with zero permissions and no media transport. New host tests [38284068924](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38284068924) PASS (16 assertions), final Android API36 source compile [38285067603](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38285067603) PASS (no installable APK, and no shared transfer-coordinator/fixture-grant classes compiled), final one-emulator visible UI test [38285067667](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38285067667) PASS (temporary virtual-device APK, visible “Media transfer disabled,” no physical installation). See `docs/testing/results/2026-10-12_V4_SITE_CAPTURE_PRODUCTION_OFFLINE_UI_SCAFFOLD.md`, `docs/research/G6A_ORIGINAL_PRESERVATION_VENDOR_EVIDENCE_REQUEST_2026-10-12.md` and END of `LATEST_CHECKPOINT.md`.

**IMPORTANT: THIS IS NOT THE FINAL MEDIA-SYNC APP.** Real original-on-glasses preservation remains unproven (v0.6.7 successful JPG GET+EXIT removed exact remote catalog identity); actual Wi-Fi Direct peer correlation and live Android BLE/P2P/HTTP production adapters remain unverified. Signed production G6A release remains NO-GO. `G6A OPEN; G6B BLOCKED`. Do not claim final completion or enable real GET.

---

# LATEST 2026-10-12 V4 EXECUTION OVERRIDE — READ BEFORE HISTORICAL HANDOVER
**V4 Steps 01–04 static audit/spec documented; Steps 05–06 shared Java/default-deny foundation implemented; Steps 07–08 host-integrated synthetic PASS; Step 09 API36 source compile PASS; Steps 10–11 actual emulator same-controller/full decoded synthetic JPG/restart/VERIFIED process crash PASS; Step 12 mixed JPG/MP4/OPUS local numbering/legacy guard host 38 assertions + API36 emulator crash/recovery PASS. Step 13 release red-team source audit documented but FINAL APK CERTIFICATION BLOCKED. Step 14 reconciliation completed with C1 HOLD, C2 BLOCKED, C3 NO-GO, C4 BLOCKED. G6A OPEN; G6B BLOCKED.**
Read the most recent additions at the END of `LATEST_CHECKPOINT.md` and `docs/testing/GATE_ROADMAP.md`. Exact reports:
- `docs/testing/results/2026-10-12_V4_STEP10_11_ANDROID_SHARED_RUNTIME_AND_FULL_JPEG.md` — emulator run 38280843057 SUCCESS.
- `docs/testing/results/2026-10-12_V4_STEP12_MIXED_DURABLE_HOST_ANDROID.md` — host 38282545306 SUCCESS (38 assertions), emulator 38282672292 SUCCESS, no actual vendor MP4/OPUS decoder parity.
- `docs/testing/V4_STEP13_RELEASE_ISOLATION_RED_TEAM_2026-10-12.md`
- `docs/testing/V4_STEP14_SOFTWARE_HARDWARE_CONVERGENCE_DECISION_2026-10-12.md`.
**DO NOT restart the old 14-step list from Step 01 or repeat the prior six emulator crash tests for counts.** Further software work: production controller integration of typed real platform evidence under default-OFF connection/media, conservative mixed-media local store integration and vendor format research. Physical preservation issue: a successful real JPG GET followed by EXIT previously made the exact original disappear; no preserving GET method verified. Real current P2P peer identity unverified. NO physical glasses or media GET and NO physical-test APK without separately scoped authorization.

---

# AIMB-G1 / K G1 — concise next-chat handover (2026-10-12)

**Source of truth:** https://github.com/kphomedocs-hue/Glasses- `main`.
**Read first:** `00_NEXT_CHAT_HANDOVER.md`, `LATEST_CHECKPOINT.md`, `docs/testing/GATE_ROADMAP.md`, `docs/testing/V4_RISK_FIRST_PLAN_2026-10-12.md`, this file. The GitHub history may advance; always verify current HEAD before acting.

## Objective
Build a minimal Android app replacing Cyan media transfer for AIMB-G1 glasses, saving JPG/MP4/OPUS locally with chronological daily numbering, crash recovery and dedup, **while retaining originals on glasses**.

## What is VERIFIED, and what is NOT
- Historical physical scoped gates G0–G5.7 passed (BLE, notifications, selected protocol responses, past Wi-Fi Direct association, catalog and **one** JPG transfer).
- Virtual lab: **96/96 offline synthetic safety subsets**, **7/7 host-local HTTP faults**, **10/10 integrated host core→catalog→HTTP→receipt cases** PASS. Host CI: https://github.com/kphomedocs-hue/Glasses-/actions/runs/38169936818.
- Android 16/API36 isolated zero-permission simulator: ordinary private-storage/reopen **2/2 PASS**, plus **6/6 real JVM abrupt termination and recovery barriers** PASS, using fresh instrumentation processes. Emulator CI: https://github.com/kphomedocs-hue/Glasses-/actions/runs/38169936791. Six boundaries: AFTER_FETCH_MARK, AFTER_VERIFIED_JOURNAL, MID_PART_WRITE, AFTER_PART_SYNC, AFTER_FINAL_MOVE, AFTER_RECEIPT. Synthetic seven-byte JPG markers are NOT full decoded photos; these tests are not real phone power-loss or live glasses tests.
- **G6A OPEN, G6B BLOCKED**. No current real P2P target authenticated. Cyan has a raw-colon Bluetooth-address suffix fallback that old K matcher overlooked, but ONE OTHER peer in previous report is still unidentified. Short `0x41` response semantics unknown.
- Physical v0.6.5 no-GET retains a new JPG; v0.6.7 single successful JPG GET plus EXIT/reconnect made exact new JPG disappear from remote catalog. The precise removal instant (GET versus exit) is not proven. **Original retention is NOT solved.** No physical media GET/automatic import is authorized.

## Important architecture gap
- `VirtualGlassesCore` has empty catalog/media/store ports and default `connectAuthorized()==false`, `mediaGetAuthorized()==false`.
- `AndroidHarnessController` exercises local `RecoveryImportStore` separately with fake bytes.
- Host-only `fakeOnlyDownload` bypass is deliberately limited to synthetic localhost fixtures; NEVER transplant into production.
- Full production shipping controller, Android live BLE/P2P/HTTP adapters, authentic JPEG/MP4/OPUS fidelity and persistent mixed-media daily numbering remain unproven.

## The ACTUAL next plan (source-of-truth V4, not duplicate old HTTP-first list)
The former conversational 12-step Android-HTTP-first plan was superseded by **`docs/testing/V4_RISK_FIRST_PLAN_2026-10-12.md`**. Its 14 small steps are **planned, NOT implemented**:
1. **01 S1:** audit shipping versus simulator and enumerate interfaces. **START HERE.**
2. **02 H1:** document GET+EXIT remote-original loss timeline precisely, including unknown causal boundary.
3. **03 H2:** rank real non-consumptive alternatives from primary sources; do not try speculative HEAD, Range, AP or unapproved commands.
4. **04 H3:** specify distinct exact/normalized/raw-colon/generic/OTHER passive peer identity buckets + pre/post ENTER timing; not a new diagnostic APK.
5. **05 S1:** typed shared controller/transport/store/clock interfaces without active radio/network.
6. **06 S2:** default-off transfer authority; preserve-original hard block, synthetic injection cannot enable production.
7. **07 S3:** one integrated source-to-store host JVM scenario using shared controller.
8. **08 S3:** adversarial timeout, ambiguous peer/+2, no-IP, malformed catalog, no duplicate GET regressions.
9. **09 S4:** compile same shared controller in current zero-permission API36 simulator.
10. **10 S4:** emulator runtime shared-orchestration and crash recovery with in-memory fake bytes.
11. **11 S5:** generate and actually decode a complete synthetic JPG on Android.
12. **12 S6:** durable mixed-media JPG/MP4/OPUS daily numbering, timezone rollover, dedup and migration.
13. **13 S7:** red-team isolation, permission allowlist, source parity and build provenance.
14. **14 C1:** reconcile exact software coverage with unresolved physical blockers and checkpoint.

H1–H3 (02–04) can run in parallel with software S1. These steps do not by themselves authorize a physical app or media GET.

## Execution rules
1. Inspect actual GitHub `main` before modifying; preserve frozen `source/v*`, release manifests/immutable APKs, prior reports and source provenance.
2. Work in tiny commits, run appropriate Java/Android GitHub Actions, verify conclusions and save exact run IDs/commits and sanitized red-team findings.
3. Public repo: NEVER upload Cyan APK/DEX, bonded MACs, SSIDs/passwords, actual owner media, or unsanitized diagnostics.
4. Virtual software PASS does not close physical G6A/G6B. Never convert name matches or injected peer booleans to real authentication.
5. Preserve-original requirement blocks any real JPG GET; do not invent non-consumptive behavior. No real phone/glasses test, physical app install, remote GET or final production APK without separately scoped authorization.
6. Use the existing isolated simulator for synthetic tests. Don't add `INTERNET` to the proven zero-permission app merely to duplicate existing HTTP tests.

## First command for next chat
'Read current GitHub main and the V4 risk-first plan. Execute **Step 01 shipping-versus-simulator source audit** and **Step 02 evidence-bounded original-preservation static review**, update GitHub with reports, verify CI/hygiene, and then identify smallest safe Step 05 implementation. Do not rebuild old tested HTTP examples or access physical glasses.'

**State:** V3C PASS in simulation; V4 14-step revised plan PROPOSED; G6A OPEN; G6B BLOCKED.
