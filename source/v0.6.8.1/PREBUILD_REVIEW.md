# Prebuild review — v0.6.8.1 credential handshake diagnostic

Scope is intentionally smaller than v0.6.8:
- no capture watch;
- no Wi-Fi Direct APIs;
- no HTTP/URL APIs;
- no catalog GET;
- no media GET;
- no filesystem import;
- no receipt/ledger;
- no remote mutation/deletion.

Transport inherited from the physically successful v0.6.7/v0.6.8 path:
- Cyan service/notify/write UUIDs unchanged;
- CCCD notification enable sequence unchanged;
- media-count command unchanged;
- P2P-enter command unchanged;
- transfer-exit command unchanged;
- frame CRC/validation unchanged;
- credential parser acceptance boundary preserved structurally.

New diagnostic observation only:
- normal credential window = 10,000 ms;
- late passive observation window = additional 10,000 ms;
- valid and invalid notification counts;
- `0x41` credential-candidate structural classification;
- relative timing in milliseconds;
- SSID/password lengths only after a structurally valid credential frame;
- no credential value is decoded, logged, or persisted.

A valid credential within the normal window is classified NORMAL_VALID. A valid credential only in the late window is LATE_VALID. Otherwise the report distinguishes rejected `0x41` candidate activity, invalid-frame-only activity, or absence of a credential candidate.
