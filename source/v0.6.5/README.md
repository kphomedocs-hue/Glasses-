# K G1 G6A Single-Capture Catalog Retention v0.6.5

## Why this diagnostic exists

The exact v0.6.4.2 build physically proved short-window no-capture stability:
- 10 images / 10 JPG remained stable;
- all 11 safe identities remained unchanged;
- verified exit and P2P cleanup completed;
- zero media-file GETs occurred.

The unresolved boundary is now capture-related. Earlier v0.6.3 observed an ambiguous 8→10 image change after one intended photo, while v0.6.2 had shown a post-transfer/restart catalog reversion.

v0.6.5 isolates **one physical capture and remote catalog retention before any media download**.

## State machine

1. **Baseline**
   - one BLE media-count query;
   - one P2P enter;
   - one catalog GET;
   - require full BLE/catalog parity;
   - retain opaque baseline identity sets in memory;
   - verified exit and P2P-group absence.

2. **Capture watch**
   - fresh BLE/notification session;
   - no P2P and no HTTP before capture visibility;
   - app reports **ARMED**;
   - user takes exactly one physical photo;
   - unchanged passive `0x73/0x01` inventory events may be ignored;
   - first changed inventory must be exactly +1 image with video/recording/config unchanged;
   - any +2/other change fails closed.

3. **Post-capture confirmation/catalog**
   - exactly one active `02 04` count confirmation;
   - require exact +1 image;
   - one P2P enter and one catalog GET;
   - require exactly one new JPG/safe identity;
   - require every baseline JPG/safe identity still present;
   - zero media GETs;
   - verified exit and P2P-group absence.

4. **Retention reconnect**
   - fixed 5000 ms reconnect delay;
   - fresh BLE/P2P session;
   - one inventory query;
   - one catalog GET;
   - require exact equality with the post-capture catalog and confirm the one new JPG is still present;
   - verified exit and cleanup.

## Exact successful operation totals

- media-count writes: 3
- P2P enter writes: 3
- transfer-exit writes: 3
- catalog GET requests: 3
- total HTTP GET requests: 3
- media-file GET requests: 0
- glasses mutation/deletion: 0

## Interpretation

- **PASS**: exactly one new JPG appears after exactly +1 capture visibility and survives a verified reconnect without any media download.
- **+2/other passive delta**: capture-side visibility ambiguity; stop before post-capture P2P.
- **+1 BLE but catalog delta is not exactly one JPG**: capture/catalog membership anomaly.
- **exact +1 catalog then new JPG disappears or membership changes after reconnect**: remote retention instability independent of media download.
- **exact +1 persists**: previous reversion becomes more likely related to the later transfer/download/restart path rather than ordinary idle or simple capture/reconnect behavior.

This diagnostic does not download, import, delete, rename, or otherwise mutate glasses media.
