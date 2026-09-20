# G6A persistence core

Pure-Java persistence layer for the first G6 implementation gate.

This module intentionally contains **no BLE, P2P, HTTP, Android UI, background service, polling, or glasses mutation code**.

It proves:
- opaque remote identity handling;
- deterministic daily numbering;
- one shared daily counter;
- durable part-file -> validation -> final commit;
- ledger commit only after final-file commit;
- duplicate prevention across restart;
- recovery that removes stale .part files and does not promote them.

Run:
```
bash source/g6a-core/run_selftest.sh
```


## Crash-window recovery hardening

A successful import writes a synced hidden `.source` sidecar containing only the opaque identity before the atomic/near-atomic final move. The durable ledger is still written only after the final file commit.

On restart, the ledger scans sidecars:
- final file + valid opaque sidecar -> recover the committed ledger entry;
- sidecar without a completed final file -> remove the stale sidecar;
- no remote filename/path is stored in the sidecar.

This closes the crash window between final-file move and ledger append without exposing the private remote path.


## G5.7 -> G6A integration contract

`SingleItemImportCoordinator` is the transport-neutral bridge for the next Android slice.

The proven G5.7 transport will provide:
- a private remote identity string held only in memory;
- the first-seen timestamp;
- a one-shot byte stream for the selected JPG.

The coordinator:
1. hashes the private identity to an opaque SHA-256 key;
2. checks the persistent ledger before requesting bytes;
3. streams into `.part`;
4. validates JPEG SOI/EOI and exact local byte count;
5. commits the final file;
6. commits the opaque ledger record;
7. returns an existing record without re-streaming when already imported.

No BLE/P2P/HTTP code is present in the coordinator.
