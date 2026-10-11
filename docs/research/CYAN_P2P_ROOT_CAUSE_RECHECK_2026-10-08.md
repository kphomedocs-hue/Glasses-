# Cyan P2P root-cause recheck — 2026-10-08

## Scope and evidence

This note records the completed static comparison requested after the repeated
v0.6.8.4 short-response result. It does not authorize a new APK or physical
test.

Primary evidence:

- GitHub `main` at `ac19c16fd1e7cac8dc3b1821c65ff0630308d64c`;
- frozen K sources `v0.3.1`, `v0.6.7`, and `v0.6.8.4`;
- Cyan Glasses `1.0.2.18_20260811` split package SHA-256
  `1328b3c025f43c06b2a0674d4c17890ec4b76cb6487196a84aa27b2335c2fc49`;
- extracted Cyan `base.apk` SHA-256
  `1e700628d76fa4fa84047632e2ccce3673f1a01966fbf8c583985568f3aaaf64`;
- v0.6.8.4 physical reports under
  `releases/v0.6.8.4/builds/run-36973886568-attempt-1/reports/`.

No third-party APK or bulk decompiled source is committed here.

## Exact implementation comparison

| Path | Sequence | Advancement condition |
| --- | --- | --- |
| K v0.3.1 | subscribe -> ENTER -> observe BLE for 8 s -> EXIT | successful ENTER write callback only; no Android P2P |
| K v0.6.7 | register receiver -> inventory -> ENTER -> parse credentials -> discovery -> exact peer -> group -> `0x73/08` | ENTER callback plus credential frame before discovery |
| K v0.6.8.4 | subscribe -> `0x40` callback -> inventory -> ENTER -> observe credentials for 20 s -> EXIT | diagnostic observation only; zero Android P2P |
| Cyan | register receiver -> start discovery -> ENTER -> exact peer/group; separately wait for `0x73/08` | Android P2P connection **and** passive `0x73/08` before download |

Important correction: v0.3.1 application logic did not parse credentials or
prove P2P readiness. Its separate physical report is the evidence that a long,
CRC-valid credential-bearing `0x41` was historically emitted. That run also
observed two valid `0x73/0x0B` events; their semantics remain unconfirmed.

## Short `0x41` interpretation

The repeated five-byte payload is:

```text
89 01 8A 00 00
```

With Cyan's full-frame indexing, `GlassModelControlResponse` reads:

- `dataType = 0x01`;
- `glassWorkType = 0x8A`.

`0x8A` is outside the work types accepted by the examined Cyan parser, and the
album callback only handles `dataType == 1 && glassWorkType == 4`. Therefore the
short frame is neither a credential frame nor a Cyan-recognized successful
work-type-4 response. The meanings of `0x89` and `0x8A` remain unknown. They
must not be labelled success, rejection, busy, or status without physical or
primary-source proof.

## Initialization and work-state findings

Cyan queues these post-connect operations:

1. `0x40` time sync;
2. `0x43` device-info read;
3. `0x42` battery read;
4. `0x47` feature-support read;
5. `0x51` volume read;
6. `0x41 / 02 04` media-count/config read.

If feature support advertises current-work-type support, Cyan also sends the
read-only `0x41 / 01 0A` query and publishes the returned current type. No
examined callback uses these reads to reset state or gate P2P ENTER. Historical
v0.3.1 direct-entry success also rules them out as universal prerequisites.

Confirmed lifecycle semantics:

- `0x41 / 02 01 09`: sent by Cyan after all downloads complete; not
  pre-transfer preparation;
- `0x41 / 02 01 0F`: conditional recovery after the 16-second discovery
  timeout (one retry) or after `0x73/09` with `FF 02`; not normal preparation;
- Cyan timeout recovery resets/reinitializes P2P and rediscovers, but does not
  resend ENTER in that recovery method.

## Root-cause status

The credential gate is the strongest identified application-state-machine
mismatch, but it is not yet the physically confirmed root cause. v0.6.8.4 did
not perform Android P2P discovery and therefore cannot distinguish:

- P2P active without a credential frame; from
- P2P not activated by the current ENTER exchange.

## Smallest bounded discriminator — specification only

Preconditions before ENTER:

1. Wi-Fi Direct enabled and required Android permissions granted;
2. read-only group query confirms no pre-existing P2P group; otherwise abort;
3. the Cyan peer predicate derived in memory: case-insensitive equality with
   the stored glasses Wi-Fi name **or** a P2P device name ending in the paired
   BLE address with separators removed;
4. receiver registered and all notification/event counters cleared;
5. peer discovery started.

Bounded test:

1. send `0x41 / 02 01 04 01` exactly once;
2. during Cyan's 16-second discovery phase, request peers and require exactly
   one peer satisfying that Cyan predicate; abort as ambiguous if more than one
   peer satisfies it;
3. connect only to that peer using WPS PBC;
4. use a separate explicitly bounded connection phase and request connection
   information;
5. count only CRC-valid post-ENTER `0x73` event IDs, including `0x0B`, `0x08`,
   and `0x09`, without raw payload logging;
6. after every path where ENTER started, attempt `0x41 / 02 01 09` exactly once;
7. stop discovery, cancel pending connection, remove only a group created by
   the test, verify group absence, unregister the receiver, and close BLE/P2P;
8. report cleanup failure separately from the primary classification.

Forbidden:

- HTTP, sockets, catalog/media GET, capture, import, receipt, or deletion;
- reset, retry, AP mode, or arbitrary peer connection;
- logging credentials, Bluetooth addresses, derived peer identity, peer
  addresses, filenames/paths, or raw frames.

## Future implementation boundary — not an authorization to build

The smallest safe code baseline is the frozen v0.4.1 P2P-association probe. It
already has no Internet permission, no HTTP/socket/media implementation, one
ENTER, one EXIT, WPS-PBC association, and a physically passed P2P group path.

The allowed delta is limited to:

1. start peer discovery before ENTER instead of after credential parsing;
2. remove the credential response as the discovery gate;
3. replace the credential-derived name matcher with the Cyan peer predicate
   above;
4. add read-only group preflight and post-cleanup group verification;
5. cancel a pending P2P connection during cleanup and record cleanup separately;
6. bound discovery at 16 seconds, with a separate connection-information bound;
7. remove v0.4.1's forced `groupOwnerIntent = 0`: Cyan supplies only the
   matched device address and WPS-PBC setup.

It must retain v0.4.1's no-Internet manifest and must not add time sync,
inventory reads, HTTP, media access, capture, reset, retry, AP mode, or an
arbitrary-peer path. The report may state group formation and group-owner role
as booleans, but must not emit any network address. This describes a later
reviewable implementation only; it does not authorize an APK or physical run.

## Interpretation

| Observation | Defensible conclusion |
| --- | --- |
| one peer satisfies the Cyan predicate after ENTER | expected P2P advertisement observed; causation by ENTER is not yet isolated |
| exact peer associates and group forms | Android P2P works despite the short `0x41` |
| group forms and post-ENTER `0x73/08` arrives | full Cyan-style P2P readiness; K credential gate is confirmed wrong for this path |
| post-ENTER `0x73/09 FF 02` | a separate one-reset recovery test becomes justified |
| no peer/group and no qualifying error | inconclusive; not proof of glasses-side failure |
| group forms without `0x73/08` | P2P activation proven; missing BLE readiness cause remains unresolved |

## Gate effect

- No APK is authorized by this document.
- Do not repeat v0.6.8.4.
- G6A remains **OPEN**.
- G6B remains **BLOCKED**.
