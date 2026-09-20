# K G1 G6A Persistent Import v0.6.0

First Android G6A candidate.

It preserves the physically proven G5.7 BLE/P2P/catalog selection flow and changes only the final handling of the exact-one new JPG:
- remote catalog line remains private and in memory;
- SHA-256 opaque identity is checked against the persistent ledger before media GET;
- exactly one HTTP media GET maximum;
- bytes stream directly into the G6A archive .part file;
- HTTP 200, byte cap and Content-Length are checked before source completion;
- JPEG SOI/EOI is validated by the persistence coordinator;
- final file is committed under app-private `g6a_imports/YYYY-MM-DD/NNNN.jpg`;
- synced opaque .source sidecar closes the post-move crash window;
- ledger is appended only after final-file commit;
- no glasses mutation/deletion.

This candidate still imports one new JPG maximum and has no background service, repeated polling, MP4 or OPUS transfer.
