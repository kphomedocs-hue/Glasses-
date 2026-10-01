# K G1 G6A Post-GET Inventory Transition Observer v0.6.7

## Why this diagnostic exists

v0.6.6 physically reproduced the same boundary twice according to the operator:
- exact +1 capture path remained valid;
- pre-GET reconnect retained the new JPG exactly;
- one JPG GET completed and validated;
- immediately after GET, a valid post-exit `0x73/0x01` inventory no longer matched the pre-GET snapshot;
- v0.6.6 then stopped before a fresh post-GET inventory/catalog read.

v0.6.7 keeps the proven transport/capture/GET path and changes only the observation rule after GET.

## Critical rule change

For baseline, post-capture and all non-post-GET exits:
- post-exit inventory MUST still match the current snapshot.

Only after:
- pre-GET retention is proven;
- exactly one media GET succeeded;
- JPEG validation succeeded;

the first valid post-exit `0x73/0x01` is accepted as **experimental observation** even if counts changed.

The app records:
- image count;
- video count;
- recording count;
- config type;
- AP-only flag;
- image delta vs pre-GET snapshot.

It then still requires:
- successful exit write callback;
- verified P2P group absence;
- fresh BLE reconnect;
- fresh active inventory query;
- fresh P2P/catalog read;
- exact identity-set comparison against pre-GET state.

## Classification

The fresh post-GET state will be classified as one of:

1. **Transient exit-time inventory transition**
   - exit-time counts changed;
   - fresh inventory/catalog returned to the exact pre-GET state.

2. **Persistent downloaded-JPG absence**
   - the exact downloaded JPG identity is missing after fresh reconnect.

3. **Persistent baseline membership change**
   - one or more baseline JPG identities are missing.

4. **Persistent count change**
   - inventory/catalog media counts differ after fresh reconnect.

5. **Persistent identity change**
   - counts match but safe/JPG identity sets differ.

6. **No remote retention change**
   - exit-time inventory also matched and fresh state is exact.

## Successful bounded totals

- media-count queries: 4
- P2P enters: 4
- transfer exits: 4
- catalog GETs: 4
- media-file GETs: 1
- total HTTP GETs: 5
- glasses mutation/deletion: 0
- persistent import/ledger: none

The purpose is observation/classification, not forcing a PASS outcome.
