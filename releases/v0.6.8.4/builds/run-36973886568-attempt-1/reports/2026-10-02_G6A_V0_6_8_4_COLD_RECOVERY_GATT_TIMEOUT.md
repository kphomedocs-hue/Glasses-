# G6A v0.6.8.4 cold-recovery rerun — GATT transport timeout

Generated: 2026-10-02T17:05:02+0530

## Exact build
- App version: 0.6.8.4
- Build commit: 506b3f311be7e2dd8a6cd5ce8f7bf36615f95e25
- Build run: 36973886568
- Build attempt: 1
- Package: com.parkarsite.g6ainitdiag684

## Physical result
- bonded devices total: 7
- AIMB-G1-family bonded matches: 1
- bonded target identity resolved successfully
- GATT connection did not reach CONNECTED within the existing 30-second bound
- diagnostic classification: PRECONDITION_OR_TRANSPORT_FAILURE

No protocol stage was reached:
- 0x40 writes: 0
- media-count writes: 0
- P2P-enter writes: 0
- transfer-exit writes: 0
- Wi-Fi Direct operations: 0
- HTTP/catalog/media requests: 0
- physical captures requested: 0

## Interpretation
This run does not retest the P2P credential behavior. It failed earlier, at BLE GATT transport establishment.

The bonded-record check proves Android still has exactly one AIMB-G1-family pairing record. It does not prove the glasses were BLE-connectable/advertising at the time of the attempt.

Do not build another protocol variant. Recover the BLE transport first.

## Next transport-only recovery action
- force-stop Cyan Glasses and v0.6.8.4;
- turn phone Bluetooth OFF briefly;
- fully power AIMB-G1 OFF;
- wait;
- turn phone Bluetooth ON;
- power AIMB-G1 ON;
- allow Bluetooth to settle before opening any app;
- do not unpair, clear app data, open Cyan, or take a photo;
- run the same exact v0.6.8.4 once.

Because this failed run reached zero proprietary writes, the next run is a transport-precondition retry, not a repeated protocol experiment.

G6A remains OPEN. G6B remains BLOCKED.
