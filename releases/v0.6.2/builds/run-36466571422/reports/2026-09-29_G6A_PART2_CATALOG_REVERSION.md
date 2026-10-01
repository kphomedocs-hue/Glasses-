# G6A v0.6.2 physical Part 2 — RESTART CATALOG REVERSION / DEDUP MISMATCH

Date: 2026-09-29 (+05:30)

This records the physical Part-2 restart identity diagnostic from the same v0.6.2 installation that passed Part 1.

## Part-1 reference

Part 1 ended with:
- images=9, videos=0, recordings=1;
- catalog entries=10;
- JPG entries=9;
- committed persistent local file: `2026-09-29/0001.jpg`;
- ledger entries=1;
- committed exact-ID token: `4c28b1090be7`.

The Part-1 baseline before capture was:
- images=8, videos=0, recordings=1;
- catalog entries=9;
- JPG entries=8;
- OPUS entries=1.

## Part-2 observed state after force-stop/reopen

Restart verification reported:
- app version=0.6.2;
- ledger entries at verification start=1;
- inventory images=8, videos=0, recordings=1;
- catalog entries=9;
- JPG entries=8;
- OPUS entries=1;
- exact ledger matches=0;
- diagnostic capsule available=YES;
- committed exact-ID token=`4c28b1090be7`;
- basename-derived matches=0;
- lowercase-exact matches=0;
- identity-shape matches=8;
- media-file GET requests=0;
- catalog GET requests=1;
- P2P enter writes=1;
- transfer-exit writes=1;
- glasses mutation/deletion command=0.

The app classified the result as:
`REMOTE IDENTITY STRING CHANGED WHILE NONSECRET SHAPE STAYED STABLE`

and failed restart dedup because no exact current catalog JPG matched the committed ledger.

## Stronger interpretation from the combined Part-1/Part-2 evidence

The most important physical fact is that the remote inventory/catalog count reverted exactly to the Part-1 pre-capture baseline:
- Part-1 baseline: 8 JPG / 9 total entries;
- Part-1 after capture/import: 9 JPG / 10 total entries;
- Part-2 restart: 8 JPG / 9 total entries.

Therefore the v0.6.2 evidence does **not** justify concluding only that the remote identity string was renamed.

What is proven:
1. the local persistent import and ledger survived restart;
2. the exact/basename/lowercase identity of the committed remote item is absent from the current catalog;
3. one JPG is also absent compared with the post-capture state;
4. the remote catalog count returned to the exact pre-capture count;
5. zero media redownloads occurred during verification.

What remains unresolved:
- whether the successfully transferred new JPG was removed from the glasses after transfer;
- whether another JPG disappeared while the new item was renamed/replaced;
- at what point any disappearance occurs (media GET, transfer exit, later firmware cleanup, or another glasses-side behavior).

The `Identity-shape matches: 8` signal is non-discriminating because all eight current JPG entries can share the same character-length/component-count shape. It must not be treated as evidence that the committed item itself merely changed string value.

## Gate decision

- G6A persistent local import: physically proven.
- G6A restart-safe remote-identity dedup: NOT proven.
- G6A remote post-transfer retention semantics: unresolved.
- G6B: BLOCKED.

## Required next diagnostic

Persist a privacy-safe opaque hash set of the entire Part-1 baseline catalog, plus the exact new-item opaque identity. On restart, compare the current catalog hash set with:
1. the saved baseline set; and
2. the saved new-item identity.

This can determine whether the remote catalog reverted exactly to baseline without logging filenames or downloading media during restart verification.

Do not add glasses deletion/mutation.
