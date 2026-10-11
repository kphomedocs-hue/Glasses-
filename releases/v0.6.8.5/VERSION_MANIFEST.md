# v0.6.8.5 Version Manifest

- Version: v0.6.8.5
- Build role: bounded one-Cyan-style Wi-Fi Direct association discriminator
- Package ID: `com.parkarsite.g6ap2pdiag685`
- Gate role: DIAGNOSTIC ONLY; cannot close G6A or unblock G6B
- Authoritative artifacts live under immutable `builds/run-<id>-attempt-<n>/` directories.

## Build instances

### run-38116572973-attempt-1
- exact bundle: `builds/run-38116572973-attempt-1/`
- build commit: `c38203f6e96044b7d36b6b6c12d1775079d750e9`
- APK SHA-256: `49a134cc3e971faf1f9c3c5560cdbe4bf674b058ab58f64e1213469bdce9ddaf`
- exact source ZIP SHA-256: `2d1f149a8f62ef3043bcb4d8b669ebb31a78d6aba14478c8526b8e518deb152c`
- CI/static status: verified
- physical status: not yet run

## CURRENT OVERRIDE — 2026-10-11 v0.6.8.5 exact-build manual review and bounded physical authorization

The one-time v0.6.8.5 diagnostic CI candidate is now **authorized for ONE supervised bounded PHYSICAL DIAGNOSTIC ONLY** after an independent exact-build manual review. This supersedes earlier generic "not authorized" wording in source-level prebuild documentation; it does not authorize a production APK or G6A closure.

- Exact build source commit: `c38203f6e96044b7d36b6b6c12d1775079d750e9`.
- GitHub Actions run: `38116572973`, attempt `1`; CI conclusion: **SUCCESS**.
- Exact immutable bundle: `releases/v0.6.8.5/builds/run-38116572973-attempt-1/`.
- APK SHA-256: `49a134cc3e971faf1f9c3c5560cdbe4bf674b058ab58f64e1213469bdce9ddaf`.
- Exact source ZIP SHA-256: `2d1f149a8f62ef3043bcb4d8b669ebb31a78d6aba14478c8526b8e518deb152c`.
- Full independent decision and limitations: `releases/v0.6.8.5/builds/run-38116572973-attempt-1/reviews/MANUAL_RED_TEAM_PREPROMOTION.md`.
- The build is safety-audit, host-JVM regression, compile, lint, signature, and immutable-archive verified. The lint report contains 12 non-blocking warnings.
- One run ONLY. Force-stop Cyan; keep AIMB-G1 paired; Bluetooth/Wi-Fi on; no capture, no reset, no repeat, no network transfer.
- Important limitations: suffix-only Wi-Fi peer matching; largely broadcast-driven connection-state observation; 5s post-group BLE event window. A negative/inconclusive outcome is NOT proof the glasses cannot enter P2P.
- Stop if cleanup reports failed/indeterminate, preserve report, and inspect Wi-Fi Direct group state before any further attempt.
- Do not resume persistent import until this diagnostic's full physical report is reviewed. Never treat CI success as physical PASS.

**G6A OPEN. G6B BLOCKED.**
