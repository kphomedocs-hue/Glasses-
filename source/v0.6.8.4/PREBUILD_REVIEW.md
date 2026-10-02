# Prebuild review — v0.6.8.4 one-0x40 initialization diagnostic

## Reason

v0.6.8.3 physically showed:
- media-count path works;
- P2P ENTER write/callback works;
- glasses promptly return a valid short non-credential `0x41` response;
- no credential-bearing frame follows within 20 seconds.

Exact Cyan static trace shows normal post-service-discovery initialization queues `0x40` time sync before later initialization/media actions. G2C already physically proved the Cyan-equivalent `0x40` write. Static trace does not establish time sync as a handshake gate, so this is a bounded hypothesis test only.

## Added behavior

One dynamic nine-byte Cyan-equivalent time payload:
- year/month/day/hour/minute/second in BCD;
- Cyan language code;
- Cyan timezone encoding;
- trailing mode/version byte `01`.

The app does not print or persist those payload values.

After the `0x40` characteristic write callback succeeds, the existing v0.6.8.3 media-count and ENTER path begins immediately. A `0x40` response is observed when it arrives before the media-count transition, but is not required as a gate and its payload is not logged.

## Preserved behavior

- same Cyan service/notify/write/CCCD UUIDs;
- media count `02 04`;
- ENTER `02 01 04 01`;
- EXIT `02 01 09`;
- CRC/frame validation;
- credential structural parser boundary;
- safe short-payload logging only for valid `0x41` payloads <8 bytes during ENTER/LATE observation;
- post-EXIT payload bytes never classified/logged;
- 10 s normal + 10 s passive late observation;
- one write per command, no retry.

## Negative scope

No:
- capture;
- Wi-Fi Direct;
- HTTP/URL;
- catalog/media GET;
- import/receipt/ledger;
- remote mutation/delete;
- credential-value logging/persistence.
