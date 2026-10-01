# v0.6.6 Version Manifest

- Version: v0.6.6
- Build role: isolate exactly one JPG GET and test remote retention
- Package ID: `com.parkarsite.g6aget66`
- Authoritative artifacts live only under immutable `builds/run-<id>-attempt-<n>/` directories.

## Build instances

### run-36895263627-attempt-1
- exact bundle: `builds/run-36895263627-attempt-1/`
- build commit: `60f2fc2fdfd3f1393add8d9159ab2f5659b2c27c`
- APK SHA-256: `2e1188fea3a6d82b7cbb923b768b2d30c42e60fe3d722f6988a1abbca49f46ef`
- exact source ZIP SHA-256: `b9b88a09036a0c031f8800fcc45c952b7836e6f3c9ff251ea4c19636b3f1bd00`
- CI/static status: verified
- manual review: FAILED — PRE-GET post-cleanup fallback could dead-end in CLEANUP
- physical status: not run
- promotion status: **NOT PROMOTED**

Review:
`builds/run-36895263627-attempt-1/reviews/MANUAL_RED_TEAM_NOT_PROMOTED.md`

### run-36895589975-attempt-1 — CURRENT PHYSICAL CANDIDATE
- exact bundle: `builds/run-36895589975-attempt-1/`
- build commit: `1ea64b26f311e6b2f18be536e1548766dabe8aa9`
- APK SHA-256: `29920782f16e5ef5785ef62d835622b3dbff0cb1b53f0f5848306f13cf76343d`
- exact source ZIP SHA-256: `c07333baa71377d0fc8b7c8bd5b7f661946fdfef949883585c332269cd347674`
- CI/static status: verified
- manual red-team review: PASS
- physical status: pending one bounded single-JPG GET retention run
- promotion status: **AUTHORIZED FOR PHYSICAL TESTING ONLY**

Manual review:
`builds/run-36895589975-attempt-1/reviews/MANUAL_RED_TEAM_PREPROMOTION.md`

## Current rule

Only run `36895589975-attempt-1` is authorized for the next v0.6.6 physical test.

The physical report must identify exact version, build commit, build run, and attempt.

G6A remains open. G6B remains blocked.
