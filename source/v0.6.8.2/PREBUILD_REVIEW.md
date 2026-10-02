# Prebuild review — v0.6.8.2 safe short-response diagnostic

Scope remains transport-only:
- no capture;
- no Android Wi-Fi Direct;
- no HTTP/catalog/media;
- no persistent import or receipt.

Inherited unchanged from v0.6.8.1:
- Cyan UUIDs;
- `02 04` media count;
- `02 01 04 01` ENTER;
- `02 01 09` EXIT;
- CRC/frame validation;
- credential structural acceptance boundary;
- 10 s normal + 10 s passive late window;
- one write per command, no retry.

New observation:
- only for a **valid framed 0x41 whose declared payload length is <8 bytes**, report total frame length, declared payload length, and that entire short payload in hex;
- classify exact `02 01 04 01` as `SHORT_ENTER_ECHO_SHAPE`;
- classify payloads beginning `02 01 04 01` with 1–3 extra bytes as `SHORT_ENTER_PREFIX_PLUS_STATUS`;
- otherwise classify `OTHER_SAFE_SHORT_0x41`.

The preserved credential parser needs at least 8 payload bytes before any credential lengths can even be fully represented, so this short-payload logging boundary cannot expose SSID/password bytes.
