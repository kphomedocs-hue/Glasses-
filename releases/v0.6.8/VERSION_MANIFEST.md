# v0.6.8 Version Manifest

- Version: v0.6.8
- Build role: persistent local import + durable receipt + restart/no-duplicate proof
- Package ID: `com.parkarsite.g6apersist68`
- Authoritative artifacts live under immutable `builds/run-<id>-attempt-<n>/` directories.

## Pre-candidate CI history

- run `36902477543` — NOT PROMOTED; red-team audit script had an unbound-variable typo; compile skipped.
- run `36902556896` — NOT PROMOTED; same pre-fix audit generation.
- run `36902661741` — NOT PROMOTED; runtime CI passed, but this read-only workflow did not create the required immutable release folder.
- run `36903533618` — NOT PROMOTED; runtime CI passed, immutable archive step failed because workflow token lacked repository-write permission.

## Current physical candidate

### run-36903742122-attempt-1 — AUTHORIZED FOR PHYSICAL TESTING ONLY

- exact bundle: `builds/run-36903742122-attempt-1/`
- build commit: `cf92a44563352ed339d8dd74a825483c7dba39e4`
- build run: `36903742122`
- build attempt: `1`
- APK SHA-256: `3f94a240030c10162c0025853846f6ea29860de2ed36874742cddbb2a61d4b27`
- exact source ZIP SHA-256: `0dd1535b280d383e102a338eb2a9fc5b7106438bea3eb106e56808b9b6e12d54`
- CI/static: PASS
- compile/lint: PASS
- signature: PASS
- immutable archive: PASS
- manual pre-promotion review: PASS
- physical status: pending two-phase v0.6.8 test

Manual review:
`builds/run-36903742122-attempt-1/reviews/MANUAL_RED_TEAM_PREPROMOTION.md`

## Physical evidence rule

Only reports whose header identifies:
- App version `0.6.8`
- Build commit `cf92a44563352ed339d8dd74a825483c7dba39e4`
- Build run `36903742122`
- Build attempt `1`

belong to this physical candidate.

Phase 1 must pass before Phase 2 is attempted. Phase 2 must use the same installed package/data after a real force-stop/reopen; do not uninstall or clear app data.

G6A remains open until both reports are reviewed. G6B remains blocked.
