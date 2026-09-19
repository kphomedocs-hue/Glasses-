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

Status: **NEXT / PASSIVE ONLY**.

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

Exit criterion:
- `0x73` semantics and initialization requirements are sufficiently supported by static and/or repeated passive physical evidence to design one named G3 command, or the unresolved uncertainty is explicitly documented and G3 remains blocked.

## G3 — one allow-listed control command

Status: **BLOCKED pending G2B**.

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
