# K G1 Disposable Photo Probe v0.5.0 — G5

Purpose: validate exactly one disposable JPG media download without guessing which existing catalog entry is newest.

## Proven prerequisites

- G4A exact Wi-Fi Direct association: PASS.
- G4A2 passive glasses IP: PASS.
- G4B3 exact configFileType=1 line-list catalog: PASS.
- Current physical catalog: 3 safe relative entries, classified as 2 JPG + 1 OPUS.
- Exact Cyan media URL form: `http://<glassDeviceWifiIP>/files/<catalog-line>`.
- Exact Cyan caller uses `AndroidNetworking.download(url, albumDir, filename)`.

See:
- `docs/testing/results/2026-09-19_G4B3_CATALOG_LINE_PASS.md`
- `docs/research/CYAN_EXACT_G5_TRACE_2026-09-19.md`

## Two-phase selection

### Phase A — baseline

1. Enter P2P exactly once.
2. Associate only to the exact BLE-reported peer.
3. Resolve glasses IP only from passive `0x73 / 0x08`.
4. GET `/files/media.config` exactly once.
5. Validate every catalog entry and retain values in memory only.
6. Log counts/types only.
7. Exit transfer mode exactly once and clean up P2P.

The app then stops and asks the user to capture exactly one disposable JPG with the glasses.

### Phase B — post-capture

1. Re-enter P2P exactly once.
2. Repeat exact-peer association and passive-IP resolution.
3. GET `/files/media.config` exactly once.
4. Require the complete baseline to remain present.
5. Compute the in-memory set difference.
6. Require exactly one new entry.
7. Require that entry to be a strict-safe relative path and extension `.jpg`.
8. GET only `http://<passive-IP>/files/<that-new-entry>` exactly once.
9. Stream directly to one app-private cache file.
10. Verify:
   - HTTP 200,
   - non-empty body,
   - maximum 32 MiB,
   - Content-Length equality when provided,
   - JPEG SOI signature `FF D8 FF`,
   - JPEG EOI signature `FF D9`.
11. Exit transfer mode exactly once and stop.

## Strict path guard

The new entry is rejected if it:
- is empty or over 240 characters,
- contains a URL scheme,
- starts with `/` or `\`,
- contains `..` traversal,
- contains control characters,
- contains `?`, `#`, `%`, `:` or backslash,
- contains empty path segments or `.` segments,
- contains characters outside letters, digits, underscore, hyphen, dot and forward slash.

## Hard limits

Across a successful two-phase run:
- proprietary P2P-enter writes: 2 maximum,
- proprietary transfer-exit writes: 2 maximum,
- catalog GETs: 2 maximum,
- media-file GETs: 1 maximum,
- total HTTP GETs: 3 maximum,
- media response: 32 MiB maximum,
- redirects: disabled,
- retry/resume/Range: absent.

## Privacy

Never logged or persisted:
- SSID/password values,
- Bluetooth/peer addresses,
- raw catalog body,
- catalog line values,
- remote filenames/paths,
- remote media URL,
- media fingerprint/hash,
- local cache path.

The baseline catalog exists only in memory and is cleared when the run finishes or the Activity is destroyed.

## Not implemented

- old-file/queue-position guessing,
- OPUS/video download,
- multi-file sync,
- production naming,
- import ledger,
- media sharing/export,
- glasses deletion/mutation,
- AP fallback,
- `02 03`,
- reset/restart/OTA.
