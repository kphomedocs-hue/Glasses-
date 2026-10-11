# v0.6.8.6 prebuild red-team checks
- Separate app package from v0.6.8.5; exact GitHub SHA/run/attempt embedded by Actions.
- No Wi-Fi Direct connect/cancel-connect/remove-group methods or branches; no HTTP, sockets, media, reset, AP or retry.
- Preflight group must be absent, and post-cleanup group must be absent twice.
- One ENTER and one EXIT on every reached terminal path. EXIT deferred while ENTER BLE write callback pending.
- Passive peer categories are mutually exclusive: missing, family prefix, full 12-hex bonded BLE suffix, other.
- Report only categories, not raw identifiers; 0x73 IDs are bounded to 24 per timing bucket.
- Need independent compile+lint+signature+manual review; **NO PHYSICAL AUTHORIZATION**.
