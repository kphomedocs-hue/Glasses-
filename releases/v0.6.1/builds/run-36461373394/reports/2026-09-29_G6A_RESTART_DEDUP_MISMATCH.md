# G6A v0.6.1 restart dedup physical result — MISMATCH

Date: 2026-09-29 (+05:30)

This result records the physical restart-dedup verification supplied from the v0.6.1 app.

## Verified physical facts

- App version: 0.6.1.
- Mode: read-only restart dedup verification.
- Persistent ledger entries at verification start: 1.
- Inventory: images=8, videos=0, recordings=1, configFileType=1.
- Catalog: 9 safe entries total; 8 JPG and 1 OPUS.
- Catalog HTTP status: 200.
- Current JPG entries checked in memory: 8.
- Opaque ledger matches in current catalog: 0.
- Media-file GET requests: 0.
- Inventory queries: 1.
- P2P enter writes: 1.
- Transfer-exit writes: 1.
- Catalog GET requests: 1.
- Glasses mutation/deletion: not implemented.
- Raw remote filename/path values were not logged or persisted.

Final app result:

`G6A RESULT: FAILED — No current catalog JPG matches the persistent opaque ledger.`

## Interpretation

The local persistent ledger survived restart, but the exact SHA-256 identity stored from the imported remote catalog entry did not match any current JPG catalog identity.

Source inspection confirmed that Part 1 and Part 2 both use the same `OpaqueIdentity.sha256(...)` implementation over the exact catalog-entry string. Therefore the mismatch is not explained by two different hashing algorithms.

The remaining hypotheses include:
1. remote catalog identity/path instability across transfer sessions;
2. a committed ledger item that is not the current catalog item expected by the test.

No conclusion between these hypotheses is permitted from v0.6.1 alone.

## Containment

- G6A is not passed.
- G6B remains blocked.
- Do not add glasses-side deletion/mutation.
- Do not loosen the zero-media-GET restart verification boundary.

## Follow-up

v0.6.2 adds a bounded privacy-safe identity-stability diagnostic using exact, basename-derived, lowercase-derived opaque hashes and nonsecret string-shape metadata. It still performs zero media GETs during restart verification.
