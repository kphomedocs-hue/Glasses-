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
- physical status: Phase 1 BLOCKED at baseline P2P credential handshake after three reproduced attempts on 2026-10-02

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


## Handover pointer

Authoritative next-chat resume instructions:
`/00_NEXT_CHAT_HANDOVER.md`

The physical candidate remains exactly run `36903742122-attempt-1`; this pointer does not change source, APK, or physical authorization.


## 2026-10-02 physical blocker update

The exact authorized v0.6.8 build was attempted three times. All three reproduced:
- successful GATT/notify setup;
- successful media-count response at 11 images / 0 videos / 1 recording;
- successful P2P-enter write start and write callback;
- no accepted credential handshake inside the existing 10-second window;
- zero catalog/media HTTP;
- zero persistent import/receipt.

Evidence:
`builds/run-36903742122-attempt-1/reports/2026-10-02_G6A_V0_6_8_P2P_CREDENTIAL_HANDSHAKE_FAILURE_REPRODUCED_3X.md`

Do not keep retrying v0.6.8 unchanged.

Next authorized physical action is the separate diagnostic package v0.6.8.1, exact run `36965814011`, whose purpose is only to classify the credential-notification failure.

G6A remains OPEN. G6B remains BLOCKED.
