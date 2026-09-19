# AIMB-G1 Gate Roadmap

Progress is evidence-gated, not version-number driven.

## G0 — software/recovery readiness

Status: **PASS**.

Required evidence:
- frozen Discovery source hash verified,
- read-only safety audit passes,
- clean Android compile/lint,
- APK signature verifies,
- legacy v0.4 pure-Java regression tests pass,
- project rebuilds from a clean repository checkout.

Evidence:
- Cold Recovery Gate run `35387241734`: PASS.
- Hardened Cold Recovery run `35387536088`: PASS.
- Core Module Regression run `35387408775`: PASS.
- Expanded Core Module Regression run `35387812463`: PASS.
- frozen reference branch: `frozen/discovery-v0.1-readonly`.

## G1 — physical GATT confirmation

Status: **PASS — 2026-09-19**.

Confirmed:
- physical AIMB-G1 LE GATT connection,
- complete service/characteristic/property enumeration,
- Cyan service `de5bf728-...`,
- Cyan notify `de5bf729-...`,
- Cyan write `de5bf72a-...`,
- standard Device Information hardware revision `AM01SPG1_V1.4`.

Evidence:
`docs/testing/results/2026-09-19_G1_PHYSICAL_DISCOVERY_PASS.md`

## G2 — response-channel confirmation

Status: **PASS — 2026-09-19**.

Confirmed:
- standard CCCD notification subscription succeeds on `de5bf729-...`,
- no proprietary write is required merely to receive spontaneous traffic,
- three valid spontaneous `0x73` frames were received,
- frames validate against the known little-endian length + CRC-16/MODBUS envelope.

Evidence:
`docs/testing/results/2026-09-19_G2_NOTIFICATION_ONLY_PASS.md`

## G2B — response-semantic correlation

Status: **PASS — exact Cyan static trace**.

Purpose:
- decode the meaning of observed `0x73` traffic,
- decide whether initialization/time behavior is required before control,
- avoid guessing and avoid active protocol writes.

Method:
1. targeted Cyan parser tracing;
2. passive event correlation using timestamped physical actions and notification frames;
3. repeated observations before assigning a semantic label.

Allowed:
- connect to exactly one bonded AIMB-G1-family device,
- discover the confirmed service,
- subscribe to the confirmed notify characteristic,
- standard CCCD enable-notification write,
- timestamp incoming frames,
- user-generated event markers,
- CRC/length validation,
- sanitized reporting.

Prohibited:
- proprietary characteristic write,
- `de5bf72a-...` control write,
- initialization/time command,
- media-mode command,
- Wi-Fi/P2P/AP,
- HTTP/media transfer,
- pairing/unpairing/reset,
- OTA/firmware operation,
- generic raw BLE command console.

Plan:
`docs/testing/G2B_PASSIVE_CORRELATION_PLAN.md`

Exit criterion: **met by exact Cyan trace**.

Evidence:
`docs/research/CYAN_EXACT_G2B_TRACE_2026-09-19.md`

Resolved:
- observed `0x01` event = media inventory/count/config report,
- observed `0x05` event = battery/charging-family report,
- Cyan's first queued post-discovery proprietary command = `0x40` syncTime,
- time-sync callback is not used as a handshake gate.

Passive event correlation remains available as a fallback for future unknown event types.

## G2C — one-command initialization parity

Status: **PASS — 2026-09-19**.

Candidate: K G1 Init Probe v0.2.1. GitHub Actions run `35416416013`: PASS.

Action:
- connect and subscribe to the confirmed response path,
- construct Cyan-equivalent dynamic `0x40` time-sync payload,
- send exactly one `0x40` frame,
- record any `0x40` response and spontaneous `0x73` reports,
- disconnect.

Prohibited:
- `0x41` glassesControl,
- P2P/AP media payload,
- Wi-Fi/P2P/AP activation,
- HTTP/media transfer,
- reset/restart/OTA/firmware action,
- generic write console.

Exit criterion:
- one benign Cyan-parity proprietary write succeeds or fails in a documented way and response behavior is understood.

## G3 — one allow-listed media-inventory query

Status: **PASS — 2026-09-19**.

Candidate: K G1 Media Count Probe v0.3. GitHub Actions run `35420261264`: PASS.

Use the exact Cyan `glassesControl` media-count request:

```text
outer command: 0x41
payload: 02 04
```

Purpose:
- validate the `0x41` request/response path,
- read media inventory/count information,
- avoid intentionally entering P2P/AP media mode.

Allowed:
- existing confirmed LE connection and notify subscription,
- exactly one `0x41` write with payload `02 04`,
- capture and decode the matching response,
- passive `0x73` observation,
- disconnect.

Prohibited:
- `02 01 04 01` P2P media-mode command,
- `02 01 04 02` AP media-mode command,
- Wi-Fi/P2P/AP activation,
- HTTP/media transfer,
- delete/modify/reset/restart/OTA,
- arbitrary command input or retry loop.

Exit criterion:
- the single media-count query response is physically captured and parsed.

## G3B — bounded P2P transfer-mode lifecycle

Status: **PASS — 2026-09-19**.

Candidate: K G1 P2P Lifecycle Probe v0.3.1. GitHub Actions run `35421429624`: PASS.

Exact Cyan physical prerequisites now support the P2P route:
- configFileType = 1,
- onlySupportApImport = false.

Approved lifecycle:
1. connect + subscribe,
2. send `0x41` payload `02 01 04 01` exactly once,
3. observe BLE responses,
4. send `0x41` payload `02 01 09` exactly once as the exact Cyan exit-transfer command,
5. observe BLE responses,
6. disconnect.

G3B deliberately does **not** use Android Wi-Fi/P2P APIs or HTTP.

Exit criterion:
- enter-transfer and exit-transfer behavior are both physically captured and frame-validated.

## G4A — phone-side P2P discovery/association

Status: **PASS — 2026-09-19**.

Purpose:
- validate Android Wi-Fi Direct discovery/association against the physically confirmed glasses P2P mode,
- establish the local network path without issuing an HTTP request.

Allowed:
- enter P2P transfer mode with the confirmed `02 01 04 01` payload,
- parse transfer credentials in memory without logging or persisting them,
- use Android `WifiP2pManager` to discover/connect to the corresponding peer,
- inspect connection/group information and local/group-owner addresses,
- exit transfer mode with `02 01 09`,
- disconnect/remove temporary phone-side P2P state.

Prohibited:
- Internet access,
- HTTP/socket requests,
- `media.config` access,
- file/media listing,
- downloads,
- file modification/deletion,
- AP-mode command,
- arbitrary BLE/network command input.

Exit criterion:
- glasses P2P network is discovered/associated from the phone and local connection metadata is captured without accessing media.

## G4A2 — passive P2P-IP notification capture

Status: **PASS — 2026-09-19**.

Purpose:
- resolve the glasses-side P2P client IP without adding any new proprietary command.

Method:
- repeat the physically proven G4A association path,
- expose only sanitized `0x73` event identifiers,
- if event `0x08` appears, parse IPv4 bytes `[7..10]` in memory,
- do not log credentials, peer addresses, or unrelated raw payloads,
- exit transfer mode and clean up exactly as G4A.

Allowed proprietary writes:
1. P2P enter `0x41 / 02 01 04 01`, once;
2. transfer exit `0x41 / 02 01 09`, once.

No additional query is allowed in G4A2.

Physical result:
- valid `0x73 / 0x08` observed during the proven P2P association lifecycle,
- phone group owner: `192.168.49.1`,
- glasses client IP: `192.168.49.176`,
- observed event IDs: `0x0B`, `0x08`, `0x01`,
- HTTP/socket/media operations: 0,
- `0x41 / 02 03` query: not used,
- transfer exit: successful,
- cleanup anomaly `removeGroup` reason 2 remained non-blocking.

Evidence:
`docs/testing/results/2026-09-19_G4A2_P2P_IP_NOTIFY_PASS.md`

Verified candidate: K G1 P2P IP Notify Probe v0.4.1. APK SHA-256 `3c9104c34fbf06fb06631a09c67cac9eab3e2a626078abcc14401cf09a2cddf3`; build run `35429585616` PASS.

## G4B — read-only local media listing

Status: **PHYSICAL LOCAL HTTP/BODY READ REACHED; JSON PARSER UNRESOLVED**.

Physical v0.4.2 result:
- proven P2P lifecycle and phone-group-owner topology repeated successfully,
- passive glasses IP resolved again,
- exactly one GET attempted to `/files/media.config`,
- request progressed past the HTTP-status check,
- bounded response body was read,
- diagnostic Android `JSONObject` parse threw `JSONException`,
- media-file GET requests: 0,
- transfer exit: success.

Because v0.4.2 reports the exception only after the status and bounded-read stages, the local network/read path is physically established. The exact physical response representation is not yet sufficiently characterized.

Evidence:
`docs/testing/results/2026-09-19_G4B_CATALOG_PARSE_FAIL.md`

Exact Cyan bytecode correction:
- current `configFileType=1` path still selects `/files/media.config`,
- `AlbumDepository.readPhotoFile` reads the whole downloaded file using Kotlin `readText`,
- that string is passed to Moshi's `PtPFileModel` adapter,
- therefore v0.4.2's `JSONObject` incompatibility does not justify changing the endpoint.

## G4B2 — safe response-shape characterization

Status: **HARDENED VERIFIED BUILD READY — physical test pending**.

v0.4.3 was superseded before physical use after a privacy recheck found that it would report a SHA-256 fingerprint of the entire private catalog response. That fingerprint was unnecessary for parser diagnosis.

Approved candidate: **K G1 Catalog Shape Probe v0.4.4**.
- APK SHA-256: `8d61807f8edf3a49a0d172a73695cc1a1422852a1b284b033e00cc58711d06c0`
- canonical build commit: `38483c269fbfdd987f0c9a3a9f5671a48e094d68`
- build run: `35433077797` — PASS
- archive commit: `892c82e9101cd8812468b770955b4fcd3d944062`
- source-commit Repository Hygiene: PASS
- hardened safety audit / compile / lint / APK signature: PASS

Physical boundary:
- identical proven P2P enter/association/passive-IP/exit lifecycle,
- same one GET to `/files/media.config`,
- no redirect and no retry,
- 65,536-byte cap,
- no media-file GET,
- no raw-body logging,
- no filename/path value logging,
- no full-response hash/fingerprint logging,
- no file write/delete/mutation,
- structural output only: response byte count, sanitized Content-Type/Encoding, strict UTF-8 status, BOM, line count, token classes, raw/normalized JSON type, root-key count, `file_list` type/count, known protocol-key presence, unknown-key count and extension counts.

Exit criterion:
- physically characterize the response enough to explain the v0.4.2 parse failure and determine the exact safe parser rule. Do not advance to G5 until the report is reviewed.

Evidence:
`docs/research/CYAN_EXACT_G4B_TRACE_2026-09-19.md`

## G5 — one disposable media download

- download one newly captured disposable test file,
- stream to disk,
- verify declared length/integrity,
- preserve capture timestamp,
- save using the numbering policy.

## G6 — automatic sync

- sync new JPG/MP4/OPUS,
- one daily chronological counter,
- persistent ledger,
- retry/recovery,
- integrity checks,
- minimal production UI.

## G7 — hardening

- long videos,
- weak/failed network transitions,
- process kill/reboot recovery,
- day rollover,
- storage-full handling,
- duplicate catalog entries,
- repeated captures with the same timestamp,
- permission revocation/regrant,
- battery/background behavior.

No destructive maintenance commands are part of this roadmap.
