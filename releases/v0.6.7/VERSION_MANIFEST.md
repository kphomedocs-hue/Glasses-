# v0.6.7 Version Manifest

- Version: v0.6.7
- Build role: observe/classify the reproduced post-GET inventory transition
- Package ID: `com.parkarsite.g6aobserver67`
- Authoritative artifacts live only under immutable `builds/run-<id>-attempt-<n>/` directories.

## Build instances

### run-36898143517-attempt-1
- exact bundle: `builds/run-36898143517-attempt-1/`
- build commit: `10d289309a4567a47104dc05f0313831c12287bc`
- APK SHA-256: `ac1023f283b80602badf117c08ba306a3b1dd74631efa8fccfa506d38b031856`
- exact source ZIP SHA-256: `0cd8618aaf704a5834be0113d295fdc458cca2042c353f1e996e8da8368f544a`
- CI/static status: verified
- physical status: not run
- promotion status: **NOT PROMOTED — concurrent duplicate build**

### run-36898159938-attempt-1 — CURRENT PHYSICAL CANDIDATE
- exact bundle: `builds/run-36898159938-attempt-1/`
- build commit: `cb90f25b0bea9cfe308a1d79d9f4235c7f2b21fe`
- APK SHA-256: `b152a20f049d8d33e514b177d3abcb8a01dad00677a046ea58a4a8e5de9dd7ab`
- exact source ZIP SHA-256: `f3ab19b2bdcd390319f3ee7e4659742cc3406417304fb7f29dbc2166ea164092`
- CI/static status: verified
- manual red-team review: PASS
- physical status: pending one bounded post-GET transition-observer run
- promotion status: **AUTHORIZED FOR PHYSICAL TESTING ONLY**

Manual review:
`builds/run-36898159938-attempt-1/reviews/MANUAL_RED_TEAM_PREPROMOTION.md`

## Current rule

Only run `36898159938-attempt-1` is authorized for the v0.6.7 physical test.

The report must identify exact version, build commit, run ID and attempt above.

G6A remains open. G6B remains blocked.
