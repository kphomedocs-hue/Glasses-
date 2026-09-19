# K G1 Media Catalog Probe v0.4.2 — G4B

Purpose: validate one read-only local media-catalog request after the already proven AIMB-G1 P2P association and passive glasses-IP resolution.

## Exact Cyan evidence used

For physical `configFileType = 1`, Cyan constructs:

```text
http://<glassDeviceWifiIP>/files/media.config
```

The downloaded text is parsed as a JSON object whose root contains `file_list`. Each item is represented by Cyan's `FileBean(c,e,f,h,s,t,w)`; Cyan uses `f` as the remote media path.

See:
`docs/research/CYAN_EXACT_G4B_TRACE_2026-09-19.md`

## G4B safety boundary

Allowed:
- exact confirmed BLE P2P enter command `0x41 / 02 01 04 01`, once,
- in-memory transfer credential parsing,
- exact BLE-reported Wi-Fi Direct peer matching,
- WPS PBC with `groupOwnerIntent=0`,
- passive `0x73 / 0x08` IPv4 parsing,
- only the physically confirmed `192.168.49.0/24` P2P subnet,
- exactly one HTTP GET to `/files/media.config`,
- no redirects,
- 4 s connect/read timeouts,
- maximum 65,536 response bytes,
- in-memory JSON parsing only,
- sanitized count/key/extension reporting,
- exact transfer exit `0x41 / 02 01 09`, once,
- cleanup/disconnect.

Forbidden:
- `0x41 / 02 03` IP query,
- any third proprietary BLE write,
- AP-mode payload,
- arbitrary URL/host/path input,
- redirects,
- POST/PUT/PATCH/DELETE,
- media-file GET,
- file download/write/delete/mutation,
- logging actual catalog filenames or paths,
- logging SSID/password or peer address,
- reset/restart/OTA/firmware operations.

## Install note

v0.4.2 uses a new package ID, `com.parkarsite.g1mediacatalogprobe`, so it does not depend on the temporary debug signing identity of the earlier v0.4.0/v0.4.1 probes.

## Physical test boundary

Do not run until the repository build, safety audit, lint, APK signature verification, and repository hygiene all pass. Then run once and return the complete sanitized report.
