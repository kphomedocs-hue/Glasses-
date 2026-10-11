# G6A v0.6.8.5 — Independent manual pre-physical review
Reviewed: 2026-10-11 (Asia/Kolkata)
Reviewer: ChatGPT independent source/artifact audit
Scope: EXACT diagnostic CI build only; NO production app promotion

## Exact provenance
- Source commit: `c38203f6e96044b7d36b6b6c12d1775079d750e9`
- GitHub Actions run: `38116572973`; attempt: `1`; job conclusion: SUCCESS
- Android package: `com.parkarsite.g6ap2pdiag685`; version: `0.6.8.5`
- Archived APK SHA-256: `49a134cc3e971faf1f9c3c5560cdbe4bf674b058ab58f64e1213469bdce9ddaf`
- Archived exact source ZIP SHA-256: `2d1f149a8f62ef3043bcb4d8b669ebb31a78d6aba14478c8526b8e518deb152c`
- GitHub Actions artifact: `11693359454`; immutable build folder: `releases/v0.6.8.5/builds/run-38116572973-attempt-1/`
- Independently downloaded the actual GitHub Actions artifact; recomputed and matched both APK and source-ZIP SHA-256 values.
- Checked the exact source ZIP against the previously reviewed MainActivity file and observed matching Git blob SHA; build identity commit/run embedded in `classes3.dex`.
- CI safety shell audit, red-team shell audit, 79 host-JVM policy assertions and 31 structural checks: PASS.
- Android compilation, lint, `apksigner verify`, artifact upload, immutable GitHub build archive: CI PASS.
- The lint HTML report contains **12 warnings**, not zero: seven text-internationalization suggestions and five target/API/dependency/backup/icon findings; no blocking compile/lint failure reported. These are not device-runtime proof.

## Manual state-machine review
- Target: exactly one bonded AIMB-G1-family BLE device; P2P peer must match paired BLE address suffix, not a generic device name. No arbitrary-peer fallback.
- Before ENTER: request read-only existing P2P group status; abort without group removal if a preexisting group is present.
- BLE GATT service, CCCD notification callback and discovery initiation precede the single ENTER command.
- Exactly one 0x41 / 02 01 04 01 ENTER write attempt; no time sync, inventory, reset, retry or AP mode.
- Discovery bound is 16s and connection-info observation bound is 15s; action/callback watchdogs are separate.
- Connection requires a single matched peer, WPS PBC, positive Android group-forming and group-membership evidence. No forced group-owner role.
- 0x73 IDs are logged only after the successful ENTER-write callback; raw frames, credentials, MACs, P2P addresses and IP addresses are not placed in the report.
- On every terminal path after an ENTER attempt: attempt one 0x41 / 02 01 09 EXIT; if ENTER is in flight, defer EXIT until its callback or a bounded watchdog, then attempt once. EXIT failures are reported, not silently passed.
- Terminal P2P cleanup stops discovery and cancels pending connect. It removes only a group attributed to the test by clean preflight, actual verified formation, exact selected-peer membership, and connect-request state.
- Two read-only post-cleanup group queries and a cleanup-issue flag gate positive conclusions. If cleanup is missing, unsuccessful or indeterminate, fail closed. No media/HTTP/sockets, imports, remote deletion, or captures.
- Manifest has NO INTERNET permission; backup disabled. Exact APK binary manifest contains required Nearby Wi-Fi/Bluetooth/Wi-Fi Direct permissions and no INTERNET string.

## Residual limitations / interpretation
1. The suffix-only peer matcher is stricter than Cyan's alternative exact stored-Wi-Fi-name match; failure to find the peer is *inconclusive*, not evidence of a glasses-side fault.
2. Connection-info polling is mainly event-driven; a missed Android connection broadcast can produce a false-negative/inconclusive outcome. Do not equate absence of a group report with proof the glasses stayed idle.
3. The passive 0x73 observation is 5s after verified association. Absence of 0x73/08 **within that bounded window** does not establish that 0x73/08 never occurs.
4. Android process death, Bluetooth loss, delayed Android group transitions or incomplete group attribution can prevent cleanup. The application must NOT remove any unproven/pre-existing group and must report failures or uncertainty. On cleanup failure, do not rerun before inspecting OS Wi-Fi Direct state.
5. On-device Wi-Fi Direct/BLE runtime behavior is **not proved by CI**; the physical run is the diagnostic needed to test it.

## Decision — SINGLE DIAGNOSTIC PHYSICAL CANDIDATE (not production)
**PASS WITH CONDITIONS** for **one supervised bounded physical diagnostic** using *only* the exact APK SHA and run above. This is an authorization to collect evidence, not a G6A PASS, G6A closure, production app promotion, or G6B unblocking.

Physical operator constraints:
- Use only the exact APK from this immutable build. The source/build revision is frozen for the one permitted trial.
- Force-stop Cyan Glasses and other glasses apps. Keep AIMB-G1 bonded; Bluetooth and Wi-Fi on. Do not unpair, reset, clear data or take photographs.
- Make sure Android Wi-Fi Direct has no active preexisting group; if uncertain, do not modify it, and allow the probe to fail-closed at read-only preflight.
- Grant required Bluetooth/Nearby-Wi-Fi permissions; keep app foregrounded; tap run **once**; do not retry in the same session or after a failure.
- Capture/export the entire sanitized terminal report. If it states CLEANUP FAILED/INDETERMINATE or an unexpected group remains, stop and inspect group state manually before further testing.
- Classify P2P formed + post-ENTER 0x73/08 as evidence of Cyan-style readiness, NOT import success. Any missed peer/group or missed 0x73/08 is bounded/inconclusive as described.
- Review the complete report before planning any subsequent diagnostic or resuming persistent G6A import.

**G6A: OPEN. G6B: BLOCKED.**
