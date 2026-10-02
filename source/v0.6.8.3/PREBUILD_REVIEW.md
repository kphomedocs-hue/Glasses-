# Prebuild review — v0.6.8.3 corrected short-response diagnostic

This revision exists because the second red-team pass revoked v0.6.8.2 before physical use.

Corrections:
1. safe short-frame classification/counters are used only while phase is ENTER_SENT or LATE_OBSERVE;
2. post-EXIT `0x41` frames are acknowledged only as ignored, with no payload classification/logging and no short-response counter mutation;
3. summary wording now states:
   - credential-capable notification payload bytes logged: NO;
   - safe short (<8-byte) 0x41 payload bytes logged during ENTER/LATE observation: true/false.

Inherited unchanged:
- Cyan UUIDs;
- media count `02 04`;
- P2P ENTER `02 01 04 01`;
- EXIT `02 01 09`;
- CRC/frame validation;
- credential structural boundary;
- one count write, one ENTER write, one EXIT write;
- 10 s normal + 10 s passive late observation;
- no capture, Wi-Fi Direct, HTTP/catalog/media, import, or receipt logic.

Safe short logging boundary:
- full Cyan frame must already validate;
- command must be `0x41`;
- declared payload length must be <8;
- actual frame length must equal declared payload + 6-byte header.

Under the preserved parser, credential values cannot begin until after the first 8 payload bytes.
