# Software Recovery Gate Result

Date: 2026-09-19

## Cold Recovery Gate

GitHub Actions run: `35387241734`

Result: **PASS**

Passed from a fresh checkout:
- repository artifact SHA verification,
- frozen Discovery source verification,
- Discovery read-only safety audit,
- Android SDK/toolchain setup,
- Discovery v0.1 clean rebuild,
- Android Lint,
- APK signature verification,
- Site Capture v0.4 pure-Java regression suite,
- legacy Python/XML diagnostic-tool validation,
- v0.4 RealAimbG1Transport inertness check.

This proves the current project can be recovered from GitHub without relying on the previous ChatGPT workspace.

## Core Module Regression Gate

GitHub Actions run: `35387408775`

Result: **PASS**

Passed:
- CRC-16/MODBUS vector `02 01 01 -> 0x5010`,
- exact P2P frame vector,
- exact AP frame vector,
- media numbering across JPG/MP4/OPUS,
- Asia/Kolkata midnight rollover,
- unsafe media extension rejection,
- private/local numeric IPv4 policy,
- public IP/domain rejection,
- module dependency guards.

## Frozen reference

Branch:

`frozen/discovery-v0.1-readonly`

Frozen from commit:

`aae1f8fc1d2afd0fd715505c5a3f460d1f69fa8b`

The branch is a recovery reference. The canonical release artifacts remain under `releases/v0.1/`.

## Next gate

G1 — physical read-only AIMB-G1 GATT discovery.
