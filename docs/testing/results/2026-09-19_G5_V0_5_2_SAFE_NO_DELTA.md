# G5.2 v0.5.2 Physical Result — SAFE NO-DELTA STOP

Date: 2026-09-19
Reported generation time: 2026-09-19T15:50:49+0530
Gate: G5.2 disposable JPG download with exact Cyan inventory + catalog-readiness parity
Result: **SAFE STOP — device inventory and catalog both unchanged; zero media GETs**

## Version-label note

The physically run APK is the G5.2 build/package:
- package ID: `com.parkarsite.g1singlephotoprobe52`
- G5.2 count write/response handshake is present in the report;
- G5.2 gate text is present.

One source report literal incorrectly prints:
`App version: 0.5.1`

The actual Android `versionName`, UI title and package identify the build as v0.5.2. This is a report-label defect only and does not change protocol behavior.

## Phase A

Inventory refresh:
- command `0x41 / 02 04`;
- write start: success;
- valid response;
- image count: 2;
- video count: 0;
- recording count: 1;
- configFileType: 1;
- onlySupportApImport: false;
- Android BLE write callback: success;
- write/response barrier completed before P2P enter.

Transfer/catalog:
- P2P association succeeded;
- phone group owner: `192.168.49.1`;
- passive glasses IPv4: `192.168.49.176`;
- exact Cyan 1000 ms readiness delay applied;
- one GET `/files/media.config`;
- HTTP 200;
- 67 bytes;
- three safe relative entries;
- extensions: `.jpg=2, .opus=1`;
- zero media GETs;
- transfer exit succeeded.

## Phase B after one user-confirmed disposable photo

Second inventory refresh:
- command `0x41 / 02 04`;
- write callback: success;
- valid response;
- image count: 2;
- video count: 0;
- recording count: 1;
- inventory deltas: image 0 / video 0 / recording 0.

Second transfer/catalog:
- exact P2P association succeeded;
- passive glasses IPv4 resolved;
- exact Cyan 1000 ms readiness delay applied;
- one GET `/files/media.config`;
- HTTP 200;
- response still exactly 67 bytes;
- catalog still exactly three safe entries;
- extension summary still `.jpg=2, .opus=1`;
- complete baseline still present;
- new catalog entries: 0.

The exact-one-new-JPG guard therefore stopped before media transfer.

Totals:
- media-count queries: 2;
- P2P enter writes: 2;
- transfer-exit writes: 2;
- catalog GETs: 2;
- media-file GETs: 0;
- total HTTP GETs: 2.

## Interpretation

This is not an HTTP/media-download failure.

The newly captured photo was not visible to either of the two independent read paths used immediately afterward:
1. BLE media inventory `02 04`;
2. HTTP `/files/media.config`.

The direct device inventory remaining unchanged is the stronger signal: the issue is upstream of catalog parsing/download selection.

Historical note:
- G3 earlier reported image count 1;
- G4B3 already showed two JPG catalog entries;
- G5.2 Phase A reports image count 2.

Therefore the device count and line catalog are not guaranteed to become mutually consistent immediately. This supports an asynchronous/deferred media-index visibility hypothesis, but does not prove the just-captured file was committed.

## Next gate

Do not repeat the same download flow yet.

Next diagnostic should isolate capture visibility with no Wi-Fi/HTTP:
1. connect BLE and subscribe to the confirmed notify channel;
2. one `02 04` baseline inventory query;
3. remain BLE-connected while transfer mode is OFF;
4. user captures exactly one photo;
5. observe only sanitized `0x73` event IDs and, if event `0x01` appears, parse only media-count/config fields;
6. after a bounded observation period, issue exactly one `02 04` recheck;
7. disconnect and review.

No P2P, HTTP, media GET, file mutation, AP fallback or `02 03`.

G6 remains blocked.
