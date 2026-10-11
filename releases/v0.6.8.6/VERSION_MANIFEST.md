# v0.6.8.6 Version Manifest

- Version: v0.6.8.6
- Build role: bounded passive peer-category and early BLE event-ID diagnostic (NO CONNECT)
- Package ID: `com.parkarsite.g6apeerdiag686`
- Gate role: DIAGNOSTIC ONLY; cannot close G6A or unblock G6B
- Authoritative artifacts live under immutable `builds/run-<id>-attempt-<n>/` directories.

## Build instances

### run-38118060511-attempt-1
- exact bundle: `builds/run-38118060511-attempt-1/`
- build commit: `90c2acb4f8736acf6ff024bfa3ba660ead09dad9`
- APK SHA-256: `d05ce2a23d25d409fc583c3830b9e574577586f2eb2f21e830895d9eece099d0`
- exact source ZIP SHA-256: `9202118df6eb15a5628fdf0d199c909bc0b8ca47b01e02b93396bdc02422473b`
- CI/static status: verified
- physical status: NOT AUTHORIZED; not run

## CURRENT OVERRIDE — 2026-10-11 v0.6.8.6 passive diagnostic exact-build prepromotion

**Reason for v0.6.8.6:** The one-time v0.6.8.5 physical run on 2026-10-11 showed one Wi-Fi Direct peer but no suffix match; it never attempted association. Historical G4A/G4A2 used an exact credential-derived P2P name. Current short 0x41 response does not provide that name. v0.6.8.5 also discarded the CRC-valid early 0x73 event ID while ENTER write callback was pending.

**Frozen passive diagnostic source/build**:
- Specification: `docs/research/G6A_V0_6_8_6_PASSIVE_PEER_IDENTITY_SPEC_2026-10-11.md`.
- Source: `source/v0.6.8.6/`.
- Exact source commit: `90c2acb4f8736acf6ff024bfa3ba660ead09dad9`.
- GitHub Actions: run `38118060511`, attempt `1`, **SUCCESS**.
- Exact immutable archive: `releases/v0.6.8.6/builds/run-38118060511-attempt-1/`.
- APK SHA-256: `d05ce2a23d25d409fc583c3830b9e574577586f2eb2f21e830895d9eece099d0`.
- Source ZIP SHA-256: `9202118df6eb15a5628fdf0d199c909bc0b8ca47b01e02b93396bdc02422473b`.
- Independent exact-build manual review: `releases/v0.6.8.6/builds/run-38118060511-attempt-1/reviews/MANUAL_RED_TEAM_PREPROMOTION.md`.
- Safety/red-team/host-JVM, compile/lint, signature, archive and artifact checks: PASS. Lint has 12 nonblocking warnings. Two harmless build-info wording defects recorded in manual review.

**Physical decision:** Conditional PASS for **ONE supervised passive diagnostic run only** of the exact APK above. No Wi-Fi Direct `connect`, `cancelConnect` or `removeGroup`, and no media, HTTP, capture, reset or retry. It performs one bounded discoverPeers + ENTER, reports only safe peer-name categories and CRC-valid 0x73 event ID timing buckets, then one EXIT and verified read-only cleanup.

The user must not repeat any failed/indeterminate run, must preserve the full terminal report, and must not interpret a single peer or model prefix as proof of glasses identity. Do not perform any production import until the new diagnostic result is reviewed.

**G6A remains OPEN. G6B remains BLOCKED.**

