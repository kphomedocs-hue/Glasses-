# AIMB-G1 Gate Roadmap

Progress is evidence-gated, not version-number driven.

## G0 — software/recovery readiness

Required:
- frozen Discovery source hash verified,
- read-only safety audit passes,
- clean Android compile/lint,
- APK signature verifies,
- legacy v0.4 pure-Java regression tests pass,
- project rebuilds from a clean repository checkout.

Status: pending cold-recovery workflow result.

## G1 — physical GATT confirmation

Action:
- scan,
- connect,
- enumerate services/characteristics/properties,
- read selected standard Device Information fields,
- disconnect.

Prohibited:
- proprietary writes,
- notification descriptor writes,
- Wi-Fi/media commands.

Exit criterion:
- actual physical profile reviewed.

## G2 — response-channel confirmation

Only after G1.

Action:
- enable only the evidence-backed response/notification path,
- observe connection/initialization behavior,
- determine whether time/init handshake is required.

No media-mode command yet unless initialization semantics are understood.

## G3 — one allow-listed media-mode command

Only after G2.

Action:
- send exactly one reviewed, named media-mode command,
- capture raw/parsed response,
- stop.

No automatic download.

## G4 — local network + read-only media listing

Action:
- establish only the glasses local P2P/AP network,
- discover the actual local endpoint,
- read `media.config` / equivalent catalog,
- display filenames/metadata only.

No delete/modify operations.

## G5 — one disposable media download

Action:
- download one newly captured disposable photo,
- stream to disk,
- verify length/integrity,
- preserve timestamp,
- save as the first daily numbered file.

Exit criterion:
- byte-complete file opens correctly and dedup/retry behavior is understood.

## G6 — automatic sync

Action:
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
- repeated captures with same timestamp,
- permission revocation/regrant,
- battery/background behavior.

No destructive maintenance commands are part of this roadmap.
