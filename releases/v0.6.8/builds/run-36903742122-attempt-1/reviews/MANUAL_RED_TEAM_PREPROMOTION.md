# Manual pre-promotion review — v0.6.8

Exact build:
- commit: `cf92a44563352ed339d8dd74a825483c7dba39e4`
- run: `36903742122`
- attempt: `1`
- package: `com.parkarsite.g6apersist68`
- APK SHA-256: `3f94a240030c10162c0025853846f6ea29860de2ed36874742cddbb2a61d4b27`
- source ZIP SHA-256: `0dd1535b280d383e102a338eb2a9fc5b7106438bea3eb106e56808b9b6e12d54`
- MainActivity blob: `8b9b5fd1552a71e5d782031e9737680ea0078443`

All CI gates passed: safety, red-team, compile, lint, signature, immutable archive, artifact upload.

Phase 1 review PASS:
- exact +1 capture and pre-transfer retention remain required;
- one persistent JPG import only;
- app-private partial file is synced before validation;
- JPEG is validated before final commit;
- opaque receipt is durably committed only after final local file exists;
- fresh remote state must return exactly to the original baseline after the transferred JPG is consumed;
- successful totals remain 4 inventory / 4 enter / 4 exit / 4 catalog reads / 1 media read / 5 HTTP reads.

Phase 2 review PASS:
- same-process verification is blocked;
- after a real app-process restart, receipt and local JPG are loaded from disk;
- exactly one committed local media file is required;
- byte count and JPEG are revalidated;
- verification performs zero HTTP, zero media reads, and zero BLE/P2P operations;
- no duplicate file is created.

Known nonblocking issue:
- one failure-only generic message retains an old diagnostic label. Success headers and v0.6.8 build identity are correct; behavior is unaffected.

Decision: **AUTHORIZED FOR ONE TWO-PHASE PHYSICAL TEST.**

G6A remains open until both Phase 1 and Phase 2 reports are reviewed. G6B remains blocked.
