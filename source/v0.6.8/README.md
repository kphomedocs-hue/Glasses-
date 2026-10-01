# K G1 G6A Persistent Import + Restart Receipt v0.6.8

This build uses the physically proven behavior that a successful JPG GET consumes the exact downloaded JPG from AIMB-G1 remote inventory/catalog.

Phase 1:
- prove exact +1 capture and pre-GET retention;
- import exactly one JPG persistently;
- write through .part + fsync;
- validate JPEG;
- atomically commit numbered local file;
- commit durable opaque receipt;
- prove the remote catalog returned exactly to the baseline set after the consumed JPG disappeared.

Phase 2:
- copy the Phase 1 report;
- force-stop the app;
- reopen the same package;
- run local-only restart verification;
- prove the durable receipt and numbered JPG survived;
- prove zero download and zero duplicate creation.

Phase 2 is blocked in the same process that committed Phase 1.
