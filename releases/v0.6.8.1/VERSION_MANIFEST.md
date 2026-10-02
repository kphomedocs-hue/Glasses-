# v0.6.8.1 Version Manifest

- Version: v0.6.8.1
- Build role: bounded P2P credential-handshake diagnostic after three reproducible v0.6.8 baseline failures
- Package ID: `com.parkarsite.g6acreddiag681`
- Gate role: **DIAGNOSTIC ONLY**; cannot close G6A or unblock G6B
- Physical status: **NO PHYSICAL CANDIDATE YET**
- Authoritative artifacts will live under immutable `builds/run-<id>-attempt-<n>/` directories.

## Diagnostic question

Distinguish among:
1. no valid `0x41` credential candidate across 20 seconds;
2. valid `0x41` activity structurally rejected by the unchanged credential boundary;
3. credential accepted within the original 10-second window;
4. credential accepted only during an additional 10-second passive late-observation window.

The build must not contain Wi-Fi Direct, HTTP/catalog/media, capture, persistent-import, or receipt code.

### run-36965814011-attempt-1
- exact bundle: `builds/run-36965814011-attempt-1/`
- build commit: `beceb27754f491fcae6a53433ad4dd4fcfadc22b`
- APK SHA-256: `01ba35d297c6d48dce31242185032d8d37ced39856419e827cc9c9013e4246dd`
- exact source ZIP SHA-256: `f3d65c6ca0bf2b8d815f5e98e62a8f8420730b350a41b6505e2072537cedd8c2`
- CI/static status: verified
- physical status: not yet run
