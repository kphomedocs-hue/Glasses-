# v0.6.4.2 Version Manifest

- Version: v0.6.4.2
- Build role: red-team-corrected no-capture catalog-stability diagnostic
- Package ID: `com.parkarsite.g6astability642`
- Authoritative artifacts live only under immutable `builds/run-<id>-attempt-<n>/` directories.

## Build instances

### run-36811380575-attempt-1
- exact bundle: `builds/run-36811380575-attempt-1/`
- build commit: `342ae660640a73211c4479c273372341ebe0d725`
- APK SHA-256: `102302236dde6de57c4f9ec78f0bee016695fb0f600178f7ed6e43251bee80c7`
- exact source ZIP SHA-256: `1110c49b8c915050c2a1132634e3fba53b5d232e28548e667f6f0b4fe3ad7394`
- CI/static status: verified
- manual review: FAILED — manifest generator quoting defect
- physical status: not run
- promotion status: **NOT PROMOTED**

### run-36811594297-attempt-1
- exact bundle: `builds/run-36811594297-attempt-1/`
- build commit: `6703eb4ab76032e60af927110bae54c47b9fa574`
- APK SHA-256: `3f00ebb6d7dfdd47b8b6a068c2efb4adedd5f937c0e0b7aed9065013849a4ea8`
- exact source ZIP SHA-256: `f13ad2c146818ac81acdb0f205e6267aa64af7c3d5141506a018ca5f20ef9bfd`
- CI/static status: verified
- manual review: FAILED — P2P discovery could start before enter write-callback confirmation
- physical status: not run
- promotion status: **NOT PROMOTED**

### run-36811812916-attempt-1 — CURRENT PHYSICAL CANDIDATE
- exact bundle: `builds/run-36811812916-attempt-1/`
- build commit: `e97bfc4ac66d6de49007296fdfff311df129801d`
- APK SHA-256: `550d4935f17ed6250feda603a9d2963e79aaa716dd20ed59be00124e97f4ae99`
- exact source ZIP SHA-256: `5a353681d94957025b6c9c9e4db2c4699ea16e36713cc562caec80fac73b567d`
- CI/static status: verified
- manual red-team review: PASS
- physical status: pending one bounded no-capture run
- promotion status: **AUTHORIZED FOR PHYSICAL TESTING ONLY**

Manual review:
`builds/run-36811812916-attempt-1/reviews/MANUAL_RED_TEAM_PREPROMOTION.md`

## Current rule

Only run `36811812916-attempt-1` is authorized for the next physical test. A report belongs to this build only if its exported header contains the exact build commit, run ID, and attempt above.

G6A remains open. G6B remains blocked.
