# v0.6.4.1 Version Manifest

- Version: v0.6.4.1
- Build role: corrected no-capture catalog-stability diagnostic
- Package ID: `com.parkarsite.g6astability64`
- versionCode: 2
- **Physical status: NOT RUN / RED-TEAM BLOCKED**
- **Use status: DO NOT INSTALL FOR PHYSICAL PROMOTION**

## Preserved build instances

### Initial verified build
- exact bundle: `builds/run-36808762256/`
- build commit: `fc97196fcce224e785cd9a86bed47c0c00b14f54`
- build run: `36808762256`
- APK SHA-256: `5726e2075e212e5cadbd7b60925c31057cc6f39ea23d6163f3ab60fca6051c21`
- physical test: none

### Later same-version rebuild
- build commit: `23ddb49c3ccf30d9a3241840079a194e5dab8c21`
- build run: `36809246969`
- version-root APK SHA-256: `168eb2f8a84a59496ab589a87702761388f69dc39d16b0d2f50c925c089cce3b`
- physical test: none
- provenance note: this rebuild demonstrated why mutable version-root APKs are no longer authoritative.

## Red-team review

`reviews/2026-10-01_RED_TEAM_RECHECK.md`

Material blockers include under-validated exit response, incomplete abort cleanup, case-insensitive “exact” peer matching, and release-provenance defects.

Next engineering version: v0.6.4.2 after corrections and a second red-team pass.
