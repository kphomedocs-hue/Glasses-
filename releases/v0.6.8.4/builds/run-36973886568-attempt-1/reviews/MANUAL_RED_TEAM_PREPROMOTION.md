# Manual red-team pre-promotion review — v0.6.8.4

## Exact build

- Version: `0.6.8.4`
- Build commit: `506b3f311be7e2dd8a6cd5ce8f7bf36615f95e25`
- Build run: `36973886568`
- Build attempt: `1`
- Package: `com.parkarsite.g6ainitdiag684`
- Runtime source blob: `ec1c03c83c5a71a5fc0efd4db4836817d4c61ecf`
- APK SHA-256: `06184f7c634ccb940fa64989c035a03ed96cf43835d0c413aea3a43931fae7e1`
- Exact source ZIP SHA-256: `16a139b81cc05caadd2f05b93d74bea4b4be46ddc1e17f4c84af7c006f112789`

## Automated and artifact-level gates

- safety audit: PASS;
- strengthened red-team static audit: PASS;
- Android compile: PASS;
- Android Lint: PASS;
- APK signature verification in CI: PASS;
- immutable exact-build archive: PASS;
- downloaded APK re-hash: exact match;
- downloaded exact-source ZIP re-hash: exact match;
- safety audit re-run from downloaded archived source: PASS;
- red-team audit re-run from downloaded archived source: PASS.

APK string inspection:
- app package code is in classes2/classes3;
- no WifiP2p references occur in those app-package DEX files;
- no HttpURLConnection, java/net/URL, /files/ endpoint, INTERNET permission, NEARBY_WIFI_DEVICES, fine/coarse location permission, or expectedP2pName reference occurs in the app-package DEX files;
- generic framework/desugaring DEX files outside the app package contain platform type names including WifiP2p/java.net.URL; these are not referenced by the app source or app-package DEX.

## Exact G2C 0x40 parity

Compared field-by-field with the physically proven G2C v0.2.1 implementation:
- `Calendar.getInstance()`: same;
- +1 second adjustment: same;
- year BCD using year % 2000: same;
- month/day/hour/minute/second BCD: same;
- Cyan language lookup: all 30 mappings identical;
- timezone offset divisor: same;
- timezone normalization `(hours + 24) % 24`: same;
- timezone encoding `normalized * 2 + 1`: same;
- trailing byte `0x01`: same;
- outer command: `0x40`;
- Modbus CRC framing semantics: same.

The generated time payload values are neither printed nor persisted.

## Transport parity against v0.6.8.3

Unchanged:
- Cyan service UUID;
- Cyan notify UUID;
- Cyan write UUID;
- CCCD UUID;
- media-count payload `02 04`;
- P2P-enter payload `02 01 04 01`;
- transfer-exit payload `02 01 09`;
- credential structural parser boundary;
- safe short-frame logging boundary;
- post-EXIT no-payload-log rule;
- 10-second normal credential window;
- additional 10-second passive late window.

## State-machine review

Bounded states:
`IDLE -> TIME_SENT -> COUNT_SENT -> ENTER_SENT -> [LATE_OBSERVE] -> EXIT_SENT -> COMPLETE`.

Rules:
1. notification subscription must succeed;
2. exactly one dynamic `0x40` time-sync write is attempted;
3. no retry is implemented;
4. media-count cannot be sent until the `0x40` BLE write callback succeeds;
5. a `0x40` response is not required as a gate;
6. exactly one media-count write;
7. exactly one P2P-enter write;
8. no second ENTER in the late window;
9. exactly one transfer-exit write;
10. no Wi-Fi Direct or HTTP path exists.

A valid `0x40`/asynchronous notification that arrives while still in TIME_SENT may be counted by command only; its payload is not logged.

## Prior-short-status comparison

The previous physically observed safe five-byte short status is used only as an exact comparison token during ENTER/LATE observation.

Classification safeguards:
- same-prior-status result requires every observed `0x41` to match that exact safe short status;
- other-safe-short-only cannot absorb a mixture containing the prior status;
- mixed response shapes or longer rejected `0x41` activity produce an explicit mixed classification;
- credential-capable payload bytes are never rendered.

A valid credential after the initialization sequence is reported as observed after `0x40`; the diagnostic itself does not claim causal proof from a single success.

## Negative scope

No:
- photo/capture path;
- Android Wi-Fi Direct API use;
- HTTP/URL/media/catalog access;
- persistent import;
- receipt/ledger;
- remote delete/mutation;
- credential value storage/logging;
- retry loop.

## Build-history disposition

- run `36973860827`, commit `9773de...`: **NOT PROMOTED** because it predates the corrected archive report-header template.
- run `36973886568`, commit `506b3f...`: **AUTHORIZED**.

Any later automatic build is non-authoritative unless separately reviewed and promoted.

## Decision

**AUTHORIZED FOR ONE BOUNDED PHYSICAL DIAGNOSTIC RUN.**

Physical instruction:
- force-stop Cyan Glasses;
- keep AIMB-G1 powered and paired;
- do not take a photo;
- run the exact v0.6.8.4 diagnostic once;
- return the complete report.

This diagnostic cannot close G6A or unblock G6B by itself.
