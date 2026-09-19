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

Status: **VERIFIED BUILD READY FOR PHYSICAL TEST**.

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

## G3B — one allow-listed media-mode command

Status: **BLOCKED pending G3**.

Only after G2B review:
- send exactly one reviewed, named, precomputed command,
- capture raw/parsed response,
- stop.

No generic command console and no automatic download.

## G4 — local network + read-only media listing

Only after G3.

- establish only the glasses' local P2P/AP path,
- discover the actual local endpoint,
- read `media.config` or equivalent catalog,
- display filenames/metadata only,
- no delete/modify operation.

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
