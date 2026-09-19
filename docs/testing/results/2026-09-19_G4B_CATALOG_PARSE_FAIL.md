# G4B Physical Catalog Read — network/read success, JSON parse failure

Date: 2026-09-19  
App: K G1 Media Catalog Probe v0.4.2  
Gate: G4B one read-only local media catalog GET  
Result: **G4B NOT PASSED — response parsing unresolved**

## Successful physical observations

The verified v0.4.2 probe:
- selected exactly one bonded AIMB-G1-family device,
- connected over the confirmed Cyan BLE profile,
- entered P2P transfer mode once,
- received valid transfer credentials without logging them,
- discovered exactly one peer and matched the exact BLE-reported name,
- formed the P2P group,
- confirmed the phone as group owner at `192.168.49.1`,
- passively observed `0x73 / 0x08`,
- resolved the glasses client IP as `192.168.49.176`,
- attempted exactly one GET to `/files/media.config`,
- performed zero media-file GET requests,
- exited transfer mode successfully,
- observed normal `0x73 / 0x01` reporting after exit,
- logged/persisted no credentials and no filename/path values.

## Failure point

The report ended with:

```text
G4B ERROR: Catalog GET/parse failed: JSONException
```

Review of the verified v0.4.2 source narrows this further:
1. a non-200 response would have produced `Catalog HTTP status <n>`;
2. an oversized declared response would have produced `Catalog Content-Length exceeds safety cap`;
3. an oversized streamed body would have produced `IOException`;
4. the reported `JSONException` therefore occurred after the request had passed the HTTP-status check and after the bounded body was read into memory.

So the local network/HTTP read path is established, but the exact physical response text is not yet structurally understood by the diagnostic.

## Static Cyan correction

Exact Cyan bytecode shows that `AlbumDepository.readPhotoFile` checks `configFileType`. In the current branch it:
- constructs a local `File`,
- calls Kotlin `FilesKt.readText$default(...)` to read the whole downloaded file as text,
- passes that text into the `readPhotoFile$1` lambda,
- obtains a Moshi adapter for `PtPFileModel`,
- calls `JsonAdapter.fromJson(jsonString)`,
- then consumes `PtPFileModel.getFile_list()`.

This confirms the endpoint/whole-text path but does **not** justify assuming Android `JSONObject` will accept the raw physical bytes unchanged.

Possible compatibility causes include BOM/encoding or another top-level representation; none is assumed without physical evidence.

## Next gate

**G4B2 — one identical GET with safe response-shape characterization only.**

No second endpoint, no retry, no media file, and no G5 action are authorized.
