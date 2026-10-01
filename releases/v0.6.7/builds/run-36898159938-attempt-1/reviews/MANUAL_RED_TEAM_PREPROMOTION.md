# Manual Red-Team Pre-Promotion Review — v0.6.7 run 36898159938 attempt 1

Date: 2026-10-01

## Exact build identity

- Version: v0.6.7
- Build commit: `cb90f25b0bea9cfe308a1d79d9f4235c7f2b21fe`
- Build run: `36898159938`
- Build attempt: `1`
- APK SHA-256: `b152a20f049d8d33e514b177d3abcb8a01dad00677a046ea58a4a8e5de9dd7ab`
- Exact source ZIP SHA-256: `f3ab19b2bdcd390319f3ee7e4659742cc3406417304fb7f29dbc2166ea164092`
- MainActivity blob: `4bb799124ad5963c26b0e66d91462f222c96b232`

## CI gates

PASS:
- safety audit;
- dedicated red-team static audit;
- Android compile;
- Android Lint;
- APK signature verification;
- immutable exact-build archive;
- artifact upload.

## Prior physical evidence incorporated

v0.6.4.2 physically proved short-window idle stability.

v0.6.5 physically proved:
- exact +1 physical capture;
- exactly one new JPG with no baseline loss;
- retention across a verified reconnect before any media GET.

v0.6.6 physically proved:
- one exact JPG GET completed and validated;
- immediately after that GET, a valid post-exit 0x73/0x01 inventory did not match the pre-GET snapshot;
- operator reports the same stopping condition in two physical runs;
- v0.6.6 stopped before fresh post-GET inventory/catalog classification.

## Critical v0.6.7 correction

Only this exact condition enables observational mismatch handling:
`stage==PRE_GET_RETENTION && mediaValidated && mediaGetCount==1`.

In that one branch:
- a valid post-exit 0x73/0x01 is accepted even if counts differ;
- exact image/video/recording/config/AP-only values are recorded;
- image delta vs the pre-GET snapshot is recorded;
- the event proves an immediate post-GET observation, not stable remote state;
- successful exit-write callback is still required;
- P2P-group absence is still required.

Outside that branch:
- post-exit inventory mismatch remains fail-closed.

## Fresh reconnect classification

After post-GET exit and verified group absence:
- a fresh BLE connection is established;
- an active inventory query is issued;
- a fresh P2P session is established;
- media.config is fetched once;
- fresh state is compared against the exact pre-GET/post-capture state.

Classification distinguishes:
1. transient exit-time transition with exact recovery;
2. downloaded new JPG absent after reconnect;
3. baseline JPG membership change;
4. persistent inventory/catalog count change;
5. identity-set change with counts otherwise stable;
6. no remote retention change.

The immediate exit event does not determine the final classification.

## Media GET boundary

PASS:
- exactly one media GET;
- exact new JPG selected from CURRENT pre-GET catalog by opaque identity;
- 32 MiB cap;
- HTTP 200 required;
- Content-Length consistency when supplied;
- JPEG SOI/EOI;
- temporary app-private cache only;
- temp deletion required;
- no persistent import;
- no persistent ledger;
- no glasses mutation/deletion.

## Successful bounded totals

- media-count queries: 4;
- P2P enter writes: 4;
- transfer-exit writes: 4;
- catalog GETs: 4;
- media-file GETs: 1;
- total HTTP GETs: 5.

## Decision

**PASS FOR ONE BOUNDED PHYSICAL v0.6.7 TEST.**

Only exact run `36898159938-attempt-1` is authorized.

Required physical report header:
- App version `0.6.7`;
- Build commit `cb90f25b0bea9cfe308a1d79d9f4235c7f2b21fe`;
- Build run `36898159938`;
- Build attempt `1`.

G6A remains open. G6B remains blocked.
