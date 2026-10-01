# v0.6.5 Version Manifest

- Version: v0.6.5
- Build role: single-capture catalog retention with zero media downloads
- Package ID: `com.parkarsite.g6acapture65`
- Authoritative artifacts live only under immutable `builds/run-<id>-attempt-<n>/` directories.

## Build instances

### run-36893051068-attempt-1
- build commit: `45338d203d178e11152966e8989baab30124c2a5`
- safety/red-team/compile/lint/signature: PASS
- archive: FAILED due concurrent non-fast-forward push race
- authoritative build bundle: none
- physical status: not run
- promotion status: **NOT PROMOTED**

### run-36893102299-attempt-1 — CURRENT PHYSICAL CANDIDATE
- exact bundle: `builds/run-36893102299-attempt-1/`
- build commit: `598e9aed94546bff2c9b018114932902aca67e05`
- APK SHA-256: `985edbcc5a8a7b035cd50c71771c7a51b9f166c9d6a827972b2f4b13423c477f`
- exact source ZIP SHA-256: `40af18c38038f03ab2ec7745b3dd3efbf50f72757fff41f67d1e6d751efbf1e1`
- CI/static status: verified
- manual red-team review: PASS
- physical status: **PHYSICAL PASS — exact +1 JPG persisted across verified reconnect with zero media downloads**
- promotion status: completed diagnostic; do not reuse for a different question

Manual review:
`builds/run-36893102299-attempt-1/reviews/MANUAL_RED_TEAM_PREPROMOTION.md`

## Current rule

Physical report:
`builds/run-36893102299-attempt-1/reports/2026-10-01_G6A_SINGLE_CAPTURE_RETENTION_PHYSICAL_PASS.md`

This exact build has completed its authorized diagnostic. It proves exact +1 capture/catalog membership and pre-download retention across a verified reconnect. G6A remains open and G6B remains blocked.
