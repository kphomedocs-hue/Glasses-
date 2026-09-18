# Project Manifest

Authoritative inventory for the AIMB-G1 / K Site Capture project.

## Current physical-test baseline

| Item | Repository path | SHA-256 / status |
|---|---|---|
| Discovery v0.1 frozen source | `releases/v0.1/K_G1_Discovery_v0_1_source_v4.zip` | `84fb1b957290abdf2464a97b35b81f2d1e8e976ce9c067ecaf571c8c19f22c68` |
| Repository-built Discovery APK | `releases/v0.1/K_G1_Discovery_v0_1.apk` | `c32758f898b91041bc7e13a272096d2629836e4465542ee00c1fbd9764e7479a` |
| Repository package | `releases/v0.1/K_G1_Discovery_v0_1_package.zip` | `e6b23d5d0eaed6a07112f7f14343e011299bf706fe0e98f447a74d24f92c827a` |
| Expanded Discovery source | `source/v0.1/` | frozen |
| Build provenance | `releases/v0.1/BUILD_INFO.md` | verified |
| Lint report | `releases/v0.1/lint-results-debug.html` | archived |

## Earlier Discovery development artifacts

These hashes preserve provenance for the local build history. v4 is the frozen source baseline.

| Artifact | SHA-256 |
|---|---|
| `K_G1_Discovery_v0_1_source.zip` | `62c5e1a4518f24075957f2b6c36ddb1a3ff3c7fee210622d3843c95d18be615a` |
| `K_G1_Discovery_v0_1_source_v2.zip` | `a8da9da94da52c19edac69ea7e2a9a8190aeb9d98a1d26bb774f81ca0a18575e` |
| `K_G1_Discovery_v0_1_source_v3.zip` | `8f0f2ae4a956945cceb0736dddb83e3cf365c47aec6bbb47b3a4f71b0da420f9` |
| `K_G1_Discovery_v0_1_source_v4.zip` | `84fb1b957290abdf2464a97b35b81f2d1e8e976ce9c067ecaf571c8c19f22c68` |
| First local debug APK | `ab37790ad13028aa6e8f1e3d16b957c72f8038df0638e8e852b31488d9fc762a` |
| First local artifact ZIP | `ccd4680706c96356b1a3339300f48378e0c969d029a9d301cd61eb31e08ebcbb` |
| First local package ZIP | `608c2645470cd7ade65a8126242b04391333408fb125b51d391278ae8d3906fe` |

The repository-built APK is the canonical installable v0.1 artifact. Earlier debug APK/package bytes are provenance only.

## K Site Capture legacy lineage

| Version | Original ZIP SHA-256 | Role |
|---|---|---|
| v0.1 | `eecbd428a53977c7858b17010db1c646493f5ddf6cc2773ec78b8ac44fee0db6` | initial storage/transport proof |
| v0.2 | `204fa7942637c79e9da1889050503c230e69d9f3f29e88ba960a60a7497af1eb` | iterative preflight |
| v0.3 | `a306bb4a182007f3e9e2974a4611b54673334f94139bcea171aa8ffb587bacdd` | stronger preflight/test baseline |
| v0.4 | `2843b69ac743986d76a8c1d6821e88fc4978e10517a972184b4e32920b621a01` | best legacy media-engine reference |

v0.4 is being preserved under `archive/site-capture/v0.4/` in expanded, browsable form. Earlier versions are retained in this manifest for lineage and should never replace v0.4.

## Third-party Cyan input

The analysed Cyan export is intentionally **not committed** because this repository is public and the APK is third-party software.

- File: `Cyan Glasses-1.0.2.18_20260811-split-apks.zip`
- Size: 139,799,818 bytes
- SHA-256: `1328b3c025f43c06b2a0674d4c17890ec4b76cb6487196a84aa27b2335c2fc49`
- Package: `com.aitowe.aitoglasses`
- Version: `1.0.2.18_20260811`
- Version code: `85`

Only interoperability findings and provenance are stored in this public repository.

## Current gate

No active protocol write is authorized yet. The next action remains the read-only physical GATT discovery test using Discovery v0.1.
