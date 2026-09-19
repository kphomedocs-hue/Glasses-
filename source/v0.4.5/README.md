# K G1 Catalog Line Probe v0.4.5 — G4B3

Purpose: mirror the exact Cyan `configFileType=1` catalog parser without exposing catalog values or requesting media.

## Exact corrected Cyan behavior

The physically observed AIMB-G1 reports `configFileType=1`.

Exact Cyan bytecode shows:
- `configFileType == 2` → `vf_list.txt` + Kotlin `readText()` + Moshi JSON;
- `configFileType != 2` → `/files/media.config` + Kotlin `readLines()`;
- each line is later used as `http://<glassDeviceWifiIP>/files/<line>`.

G4B2 v0.4.4 physically confirmed that `/files/media.config` returns HTTP 200, `text/plain`, valid UTF-8, no BOM and a line-oriented response.

## G4B3 boundary

Allowed:
- proven P2P enter once,
- exact BLE-reported peer only,
- passive `0x73 / 0x08` IP only,
- phone-group-owner topology,
- confirmed `192.168.49.0/24` subnet guard,
- exactly one GET to `/files/media.config`,
- no redirects,
- 4-second connect/read timeouts,
- 65,536-byte response cap,
- no retry,
- line parsing in memory with Java `BufferedReader.readLine()`, matching Cyan/Kotlin line semantics,
- sanitized report fields only:
  - exact line-list entry count,
  - non-empty count,
  - blank/whitespace count,
  - relative-safe count,
  - scheme/absolute-URL count,
  - leading-slash count,
  - traversal-token count,
  - control-character count,
  - extension counts.

Forbidden:
- actual catalog line values,
- filenames/paths,
- raw response logging,
- response fingerprints/hashes,
- media-file GET/download,
- alternate endpoint,
- `vf_list.txt`,
- JSON fallback,
- file writes/deletes,
- `02 03` IP query,
- AP fallback,
- reset/restart/OTA.

## Physical exit criterion

The line-list is considered validated only if:
- UTF-8 is valid,
- at least one non-empty entry exists,
- entry count is within 256,
- no blank entries,
- no scheme/absolute URLs,
- no leading slash,
- no `..` traversal segment,
- no control characters,
- all non-empty entries satisfy the relative-safe test.

Even on success, G5 remains blocked until the sanitized report is reviewed.
