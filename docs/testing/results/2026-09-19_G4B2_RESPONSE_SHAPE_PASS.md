# G4B2 Physical Catalog Response-Shape Result — PASS

Date: 2026-09-19  
App: K G1 Catalog Shape Probe v0.4.4  
Gate: G4B2 one read-only local media catalog GET + safe response classification  
Result: **PASS — physical response shape established**

## Physical observations

The hardened v0.4.4 probe:
- selected exactly one bonded AIMB-G1-family target;
- connected over the confirmed Cyan BLE profile;
- entered P2P transfer mode once;
- received valid transfer credentials without logging/persisting them;
- matched the exact BLE-reported Wi-Fi Direct peer;
- formed a P2P group with the phone as group owner;
- passively resolved the glasses IPv4 from `0x73 / 0x08`;
- made exactly one GET to `/files/media.config`;
- followed no redirects;
- made no retry;
- performed zero media-file GET requests;
- exited transfer mode successfully;
- logged no raw catalog body, filename/path values, credentials, peer address or catalog fingerprint.

Network observations:
- phone/group owner: `192.168.49.1`;
- glasses client: `192.168.49.176`;
- HTTP status: `200`;
- Content-Type: `text/plain`;
- Content-Encoding: none;
- response bytes: 67;
- strict UTF-8: valid;
- BOM: none;
- diagnostic line count: 4;
- first non-whitespace token class: other ASCII;
- last non-whitespace token class: other ASCII;
- raw JSON parse: fail;
- BOM-normalized JSON parse: fail.

Observed asynchronous event IDs:
- `0x0B`;
- `0x08`;
- `0x01`.

Cleanup retained the known non-blocking Android Wi-Fi Direct `removeGroup` reason 2 anomaly.

## Corrected exact Cyan interpretation

A bytecode-level recheck of the exact Cyan APK resolves the parser branch:

In `AlbumDepository.readPhotoFile`:
- Cyan reads `UserConfig.getConfigFileType()`;
- it compares that value with integer `2`;
- **only when configFileType == 2** does it call Kotlin `FilesKt.readText$default(...)` and pass the resulting text to a Moshi `PtPFileModel` adapter;
- **when configFileType != 2** it calls Kotlin `FilesKt.readLines$default(...)`.

The physical G3 value is `configFileType = 1`, so this AIMB-G1 follows the **line-oriented branch**, not the JSON branch.

The exact `PictureFragment$downloadMediaConfig$2` bytecode independently confirms:
- `configFileType == 2` → `:80/storage/sd0/C/DCIM/1/vf_list.txt`;
- otherwise → `/files/media.config`.

The same line-oriented branch then iterates every line from `media.config` and constructs:

```text
http://<glassDeviceWifiIP>/files/<line>
```

Each line is passed with that URL into Cyan's `PictureDownloadBean(String, String)` queue.

Therefore the physical 67-byte, four-line, UTF-8 `text/plain` response is consistent with the exact Cyan `configFileType=1` catalog path.

## Conclusion

G4B2 exit criterion is met: the physical catalog response format is now understood.

The earlier v0.4.2 `JSONException` was caused by our diagnostic incorrectly applying the `configFileType=2` JSON parser to the physical `configFileType=1` line-oriented catalog.

No alternative endpoint, decryption, decompression or transformation is required by Cyan for this branch.

Next gate before any media download: **G4B3 exact line-list parser parity**. It may repeat the same single GET and report only sanitized entry counts and extension/type counts; it must not log any actual line/filename/path value.

G5 remains blocked.
