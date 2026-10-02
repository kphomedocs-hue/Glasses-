# v0.6.8.1 Version Manifest

- Version: v0.6.8.1
- Build role: bounded P2P credential-handshake diagnostic after three reproducible v0.6.8 baseline failures
- Package ID: `com.parkarsite.g6acreddiag681`
- Gate role: **DIAGNOSTIC ONLY**; cannot close G6A or unblock G6B
- Physical status: **PHYSICAL DIAGNOSTIC COMPLETE — SHORT 0x41 RESPONSE ONLY; NO CREDENTIAL FRAME WITHIN 20 s**
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


## Current authorized diagnostic candidate

Only this exact build is authorized:
- run: `36965814011`
- attempt: `1`
- build commit: `beceb27754f491fcae6a53433ad4dd4fcfadc22b`
- package: `com.parkarsite.g6acreddiag681`
- APK SHA-256: `01ba35d297c6d48dce31242185032d8d37ced39856419e827cc9c9013e4246dd`
- exact source ZIP SHA-256: `f3d65c6ca0bf2b8d815f5e98e62a8f8420730b350a41b6505e2072537cedd8c2`
- exact bundle: `builds/run-36965814011-attempt-1/`

Manual red-team:
`builds/run-36965814011-attempt-1/reviews/MANUAL_RED_TEAM_PREPROMOTION.md`

Physical instruction:
- force-stop Cyan Glasses;
- keep AIMB-G1 powered and paired;
- **do not take a photo**;
- run the diagnostic once;
- preserve the complete report.

G6A remains OPEN. G6B remains BLOCKED.


## Physical result — 2026-10-02T10:29:19+0530

Exact authorized build ran once.

Result:
- baseline inventory: 11 images / 0 videos / 1 recording;
- P2P-enter write: SUCCESS;
- enter write callback: SUCCESS at +88 ms;
- one valid `0x41` notification at +72 ms;
- that frame was `TOO_SHORT` to contain the credential structure;
- no structurally valid credential frame in the original 10-second window;
- no structurally valid credential frame in the additional 10-second passive window;
- zero Android Wi-Fi Direct operations;
- zero HTTP/catalog/media GET;
- zero capture/import/receipt activity.

Evidence:
`builds/run-36965814011-attempt-1/reports/2026-10-02_G6A_V0_6_8_1_CREDENTIAL_DIAGNOSTIC_PHYSICAL_RESULT.md`

Interpretation:
A normal credential-bearing frame was not rejected for prefix/length/bounds reasons; it was not observed. The remaining ambiguity is the exact meaning of the short `0x41` response. The next diagnostic may expose only safe metadata from frames whose declared payload length is <8 bytes, which by the preserved credential parser cannot contain credential values.

G6A remains OPEN. G6B remains BLOCKED.
