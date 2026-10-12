# V4 Step 14 — software-versus-hardware convergence decision
Date: 2026-10-12. **DOCUMENTED HOLD, not a completed production app or physical gate closure.**

## Fourteen-step risk-first status
| Step | Scope evidence | Disposition |
|---|---|---|
| 01 | Shipping vs virtual/simulator source audit | Audit filed |
| 02 | Original JPG no-GET vs GET+EXIT physical evidence | Static review filed; causal instant unproven |
| 03 | Non-consumptive alternatives ranked by evidence | Static review filed; NO verified preserving GET |
| 04 | Passive peer classification and test requirements | Specification filed; no fresh real peer identity |
| 05 | Typed pure-Java shared contracts, observation and source/store coordinator | Host/Android-source compile verified; not a live shipping controller |
| 06 | Default-off real transfer authorization | Verified source/host/emulator synthetic; final APK release guard still pending |
| 07 | Integrated shared coordinator exact single JPG -> actual durable local receipt | Host synthetic PASS, GitHub run 38277694218 |
| 08 | Integrated adversarial peer/catalog/timeout/replay cases | Host synthetic PASS, 43 new assertions |
| 09 | Shared Java controller compiled in permission-free API36 Android simulator | PASS, GitHub run 38278073484 |
| 10 | SAME shared coordinator in API36 emulator end-to-end and VERIFIED crash recovery | PASS in synthetic Android scope, run 38280843057 |
| 11 | Fully decoded synthetic JPEG (Android Bitmap compress/decode and durable readback) | PASS synthetic 32x24 raster; vendor fidelity/EXIF NOT proven |
| 12 | Mixed JPG/MP4/OPUS durable daily naming, timezone and read-only legacy protection | Host 38 assertions PASS (38282545306), final API36 revised-source mixed-media runtime + MP4 halt/recover PASS (38282672292) |
| 13 | Red-team release isolation | SOURCE AUDIT FILED; final production signed APK / DEX isolation NOT CERTIFIED |
| 14 | Reconcile V4 software coverage with hardware evidence and next gate | This documented **HOLD** decision |

## Verified software claims, without upgrading scope
- Historical frozen physical G0–G5.7 bounded milestones verified, no new physical gate promotion.
- Previously 96/96 synthetic offline subsets, 7/7 separate host HTTP faults, 10/10 host core→HTTP→receipt tests; prior API36 six process-stop barriers are separate storage-only checks, not new full-controller hardware evidence.
- New host common-coordinator: exactly one synthetic JPG selected and journalled before source callback, validated local receipt, no second Source on restart or after ambiguity.
- New API36 emulator common-coordinator: one full generated, bitmap-decodable JPG, private file receipt, fresh-process reopen, new coordinator VERIFIED journal process halt and local-only recovery.
- New independent mixed-media local store: shared JPG/MP4/OPUS ordinal across a first-seen day, midnight rollover in Asia/Kolkata, independently retained capture timestamp, one-shot write barrier, durable receipt and no automatic retry; legacy tentative JPG ordinals and old/orphan receipts block unsafe reuse. Host tested. MP4/OPUS are container-signature fixtures, not decoder-verified vendor media.
- No user phone or physical glasses used; all new Android APKs existed only as temporary emulator artifacts with no app/test permissions and no release upload.

## Remaining blockers to the intended final minimal Android app
**HARD PHYSICAL PRESERVATION BLOCKER:** The tested successful JPG GET plus EXIT is associated with remote catalog disappearance of that exact original while baseline images remain. It is unproven whether GET, acknowledgement or exit performs the removal. No credible preserving read has yet been established. The explicit requirement **keep originals on glasses** forbids any new real media GET, automatic import or final sync promotion.

**HARD PEER BLOCKER:** The actual contemporary AIMB-G1 Wi-Fi Direct peer, selected group member, bound Android Network and device-correlated IP remain unidentified/unverified; raw-colon/normalized/generic/OTHER string matches do not authenticate a device. Short `0x41` credential semantics remain unknown.

**PRODUCTION INTEGRATION BLOCKER:** New shared coordinator has no Android real BLE/P2P/HTTP adapters; the Android simulator is deliberately zero-permission and is not a shipping app. Mixed-media store has not been wired into an authentic live-media coordinator, and no vendor JPG metadata, MP4 container or OPUS codec fidelity is proven.

**RELEASE BLOCKER:** Existing instrumentation fixture permit is injected solely in test sources, but the common controller still has a package-private synthetic permit API. The eventual production build must independently prove no host/instrumentation bypass, debug UI, test endpoint, inadvertent permission, raw secret, unapproved GET or device-specific data is packaged; no final signed APK/DEX exists yet to audit.

## Decisions
- **C1: HOLD / PARTIAL SOFTWARE-ONLY PROOF.** The specific synthetic host, Android, storage, and decoder gates above have scoped PASS; required production BLE/P2P/HTTP integration and mixed-media fidelity remain absent. Do not label C1 production acceptance PASS.
- **C2: BLOCKED pending independently authorized real peer/cleanup test.**
- **C3: NO-GO to automatic media transfer pending compelling original-preservation evidence or explicit user requirement change.**
- **C4: BLOCKED — no final release APK, app installation or real media GET.**
- G6A **OPEN**. G6B **BLOCKED**.

## Safe next technical work
1. Static vendor/Cyan downloader and EXIT call-site audit for evidence-backed non-consumptive media retrieval; never invent methods or issue exploratory GET.
2. Integrate the same default-off shared coordinator with well-scoped real platform evidence interfaces **without enabling real connection/download**.
3. Extend local-only decoding/shape/metadata and migration tests on host/isolated emulator, avoiding unnecessary reruns of earlier simulation subsets.
4. Only after owner separately authorizes a bounded physical passive observation protocol, verify current peer identity; no media GET.
5. No final syncing APK until C2 and C3 are independently supported.

All historical source/release artifacts must remain immutable.


## Final Step 12 verification addendum
`docs/testing/results/2026-10-12_V4_STEP12_MIXED_DURABLE_HOST_ANDROID.md` records the updated conservative legacy collision guards. Final host test [38282545306](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38282545306) SUCCESS 38 assertions; exact fully revised source API36 emulator [38282672292](https://github.com/kphomedocs-hue/Glasses-/actions/runs/38282672292) SUCCESS with `V4_MIXED_JPG_MP4_OPUS_NUMBERED`, `V4_MIXED_REOPEN_NO_DUPLICATES` and `V4_MIXED_MP4_RECOVERED_NO_RETRY` PASS. This upgrades Step 12 **in synthetic local-store scope only** and does not alter C1–C4 HOLD decisions.
