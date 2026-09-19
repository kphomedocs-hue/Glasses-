# K G1 Catalog Shape Probe v0.4.4 — G4B2

Purpose: explain the v0.4.2 physical `JSONException` without changing the endpoint or widening network scope.

## Why G4B2 exists

Physical v0.4.2 proved:
- P2P association succeeded,
- passive `0x73 / 0x08` IP resolution succeeded,
- exactly one GET was attempted to `/files/media.config`,
- the request progressed past the HTTP-status and response-size checks,
- the bounded body was read,
- Android `JSONObject` parsing then threw `JSONException`,
- no media-file request occurred,
- transfer exit succeeded.

Exact Cyan bytecode independently shows that for the current `configFileType=1` branch, `AlbumDepository.readPhotoFile` reads the downloaded file as whole text and passes that text directly to a Moshi `PtPFileModel` adapter.

So the endpoint remains fixed. The unresolved question is the physical response text's encoding/top-level representation or a parser-compatibility detail.

## G4B2 safety boundary

Same single-request network boundary as v0.4.2:
- exact P2P enter once,
- exact BLE-reported peer only,
- passive `0x73 / 0x08` IP only,
- phone-group-owner topology required,
- confirmed `192.168.49.0/24` subnet guard,
- exactly one GET to `/files/media.config`,
- no redirects,
- 4-second connect/read timeouts,
- 65,536-byte response cap,
- no retry,
- exact transfer exit once.

New diagnostic output is structural only. A pre-physical privacy recheck removed the v0.4.3 full-response SHA-256 field because the catalog itself contains private media metadata and a persistent fingerprint is unnecessary.

New diagnostic output is structural only:
- response byte count,
- sanitized Content-Type / Content-Encoding,
- strict UTF-8 validity,
- BOM type,
- line count,
- first/last non-whitespace token class,
- raw vs BOM-normalized JSON top-level type,
- root-key count,
- whether `file_list` exists and its value type,
- item count,
- presence of known compact keys `c,e,f,h,s,t,w`,
- unknown-key count,
- extension counts derived from `f`.

Still forbidden:
- logging raw body text,
- logging filename/path values,
- media-file GET/download,
- file-system writes/deletes,
- arbitrary URL input,
- `02 03` IP query,
- AP-mode fallback,
- reset/restart/OTA.

## Install note

v0.4.4 uses a fresh package ID `com.parkarsite.g1catalogshapeprobe4` so it installs separately from v0.4.2/v0.4.3 despite GitHub debug signing.
