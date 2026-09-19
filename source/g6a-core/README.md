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
