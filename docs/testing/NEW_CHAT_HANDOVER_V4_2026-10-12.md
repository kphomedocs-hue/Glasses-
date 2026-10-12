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
