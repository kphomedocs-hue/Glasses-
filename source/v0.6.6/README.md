# K G1 G6A Single-JPG GET Retention v0.6.6

## Evidence entering this diagnostic

Physically proven before v0.6.6:
- v0.6.4.2: short-window no-capture catalog stability;
- v0.6.5: one physical capture produced exact +1 image / exact +1 JPG;
- all baseline identities remained;
- the one new JPG survived verified exit/group cleanup and a fresh reconnect;
- zero media-file downloads were used in those proofs.

The next unresolved boundary is whether **one media-file GET itself** changes remote catalog retention semantics.

## State machine

1. Baseline inventory + catalog.
2. Verified exit + P2P-group absence.
3. Fresh BLE capture watch; user takes exactly one physical photo only after ARMED.
4. Passive exact +1 required; +2/other change fails closed.
5. Active +1 confirmation.
6. Post-capture catalog must show exactly one new JPG and no baseline loss.
7. Verified exit + cleanup.
8. Fresh **pre-GET retention** reconnect must show exact post-capture catalog identity retention.
9. Only after that proof, resolve the exact new JPG from the current catalog by its in-memory opaque identity.
10. Perform exactly **one GET** of that JPG.
11. Validate HTTP 200, 32 MiB cap, Content-Length consistency when supplied, JPEG SOI/EOI.
12. Store only in app-private cache temporarily; delete immediately after validation.
13. No persistent import and no ledger update.
14. Verified exit + cleanup.
15. Fresh **post-GET retention** reconnect.
16. Compare inventory/catalog identity sets against the pre-GET/post-capture state.
17. Verified final exit + cleanup.

## Successful operation totals

- media-count queries: 4
- P2P enters: 4
- transfer exits: 4
- catalog GETs: 4
- media-file GETs: 1
- total HTTP GETs: 5
- glasses mutation/deletion: 0

## Interpretation

- PASS: exact new JPG remains remotely present after one GET; full catalog remains unchanged.
- New JPG absent only after GET: media transfer itself is implicated.
- Baseline membership changes after GET: transfer-associated remote catalog mutation/anomaly.
- GET succeeds and retention stays exact: remaining unresolved boundary moves to persistent local import/ledger/restart/dedup handling.

No remote filename/path or credentials are logged/persisted.
