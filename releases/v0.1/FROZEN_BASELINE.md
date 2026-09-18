# Frozen Baseline — Discovery v0.1

Status: **FROZEN**

Canonical purpose:
- first physical AIMB-G1 read-only BLE/GATT discovery.

Canonical release files:
- `K_G1_Discovery_v0_1.apk`
- `K_G1_Discovery_v0_1_source_v4.zip`
- `K_G1_Discovery_v0_1_package.zip`
- QA/checksum files in this directory.

Frozen reference branch:

`frozen/discovery-v0.1-readonly`

Reference commit:

`aae1f8fc1d2afd0fd715505c5a3f460d1f69fa8b`

## Rule

Do not edit this version to add:
- BLE command writes,
- notification subscriptions,
- Wi-Fi/P2P/AP,
- HTTP media transfer,
- storage sync,
- reset/restart/OTA functions.

Any behavior beyond read-only discovery must go into a new gated version/module after the physical G1 report is reviewed.

## Evidence

- Source integrity: verified
- Android compile: PASS
- Android Lint: PASS
- APK signature verification: PASS
- Read-only safety audit: PASS
- Clean-room recovery: PASS

See:
- `../../docs/testing/GATE_ROADMAP.md`
- `../../docs/testing/results/2026-09-19_SOFTWARE_RECOVERY_GATE.md`
