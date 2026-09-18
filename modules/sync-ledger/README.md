# sync-ledger

Persistent import state.

Owns:
- remote identity,
- pending/downloading/complete/failed state,
- dedup across process restarts,
- crash recovery,
- retry metadata.

Production target: Room-backed implementation behind a pure interface.
