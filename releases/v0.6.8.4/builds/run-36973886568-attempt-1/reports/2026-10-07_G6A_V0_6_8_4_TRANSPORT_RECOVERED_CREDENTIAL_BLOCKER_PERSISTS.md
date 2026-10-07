# G6A v0.6.8.4 — transport recovered, credential blocker persists

Generated: 2026-10-07T21:06:11+0530

## Exact build
- App version: 0.6.8.4
- Build commit: 506b3f311be7e2dd8a6cd5ce8f7bf36615f95e25
- Build run: 36973886568
- Build attempt: 1
- Package: com.parkarsite.g6ainitdiag684

## Physical result

BLE transport recovery succeeded:
- bonded devices total: 7;
- AIMB-G1-family bonded matches: 1;
- GATT connected;
- Cyan service/notify/write present;
- notification subscription succeeded.

0x40 initialization:
- exactly one write;
- one valid 0x40 response frame observed;
- write callback succeeded.

Media-count:
- exactly one query;
- valid response;
- inventory: 0 images / 0 videos / 0 recordings;
- configFileType=1;
- onlySupportApImport=false.

P2P ENTER:
- exactly one 02 01 04 01 write;
- write callback succeeded;
- one valid framed short 0x41 response at +75 ms;
- safe short response matched the same previously observed v0.6.8.3 response exactly;
- no credential-bearing frame in the original 10-second window;
- no credential-bearing frame in the additional 10-second passive window.

EXIT:
- one 02 01 09 write;
- write callback succeeded;
- post-exit 0x41 ignored without payload classification/logging;
- no matching post-exit 0x73/0x01 observed.

Negative scope:
- Wi-Fi Direct operations: 0;
- HTTP/catalog/media requests: 0;
- physical captures requested: 0;
- credential values logged/persisted: NO.

## Conclusion

1. The temporary GATT transport failure is resolved.
2. The credential-handshake blocker persists after a true recovery cycle and across a five-day interval.
3. The same short non-credential 0x41 response persists whether the media inventory is populated (earlier 11/0/1) or empty (current 0/0/0).
4. The blocker also persists whether a 0x40 response is observed or not.
5. Therefore the evidence no longer supports a transient BLE/GATT state or a missing single 0x40 initialization write as the cause.

Historical controls remain important:
- G3B v0.3.1 physically succeeded with direct subscribe -> P2P ENTER -> credentials -> EXIT and no media-count/time-sync prerequisite.
- v0.6.7 physically completed four P2P-enter cycles on 2026-10-01.

The next root-cause test should reproduce the historically successful G3B entry sequence exactly, with current hardened privacy/reporting, before introducing any new proprietary command.

G6A remains OPEN. G6B remains BLOCKED.
