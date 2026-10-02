# Manual pre-promotion red-team review — v0.6.8.1

## Exact build

- Version: `0.6.8.1`
- Build commit: `beceb27754f491fcae6a53433ad4dd4fcfadc22b`
- Build run: `36965814011`
- Build attempt: `1`
- Package: `com.parkarsite.g6acreddiag681`
- Runtime source blob: `fe068170e70b8c6b5be3ab764a6a26816c0a2493`
- APK SHA-256: `01ba35d297c6d48dce31242185032d8d37ced39856419e827cc9c9013e4246dd`
- Exact source ZIP SHA-256: `f3d65c6ca0bf2b8d815f5e98e62a8f8420730b350a41b6505e2072537cedd8c2`

CI/static gates:
- safety audit: PASS;
- red-team static audit: PASS;
- Android compile: PASS;
- Android Lint: PASS;
- APK signature verification: PASS;
- immutable exact-build archive: PASS.

## Transport parity against authorized v0.6.8

The diagnostic preserves the exact confirmed transport constants:
- Cyan service UUID: unchanged;
- Cyan notify UUID: unchanged;
- Cyan write UUID: unchanged;
- CCCD UUID: unchanged;
- media-count payload: `02 04`;
- P2P-enter payload: `02 01 04 01`;
- transfer-exit payload: `02 01 09`.

Frame generation, CRC16, and full-frame CRC/length validation are semantically identical to v0.6.8.

The media-count parser preserves the same minimum lengths, `02 04` prefix, little-endian image/video/recording counts, config-file type, and AP-only flag interpretation.

## Credential acceptance-boundary review

Authorized v0.6.8 accepts a credential frame only when:
1. the full Cyan frame has already passed CRC/length validation;
2. total frame length is at least 14 bytes;
3. declared payload length is at least 8 bytes;
4. payload prefix is exactly `02 01 04 01`;
5. SSID length and password length are both positive;
6. the embedded SSID + password lengths remain within the frame.

v0.6.8.1 uses those same structural conditions.

Privacy is stricter in the diagnostic:
- it does not construct/store the SSID string;
- it does not construct/store the password string;
- it reports only structural result, timing, and lengths after a structurally valid frame;
- raw notification payload bytes are never reported or persisted.

Because the original v0.6.8 parser constructs non-empty Java strings only after positive non-zero byte lengths have already been established, omitting string construction does not broaden the structural acceptance boundary being diagnosed.

## State-machine review

The diagnostic is intentionally smaller than the G6A transfer app:
1. one bonded-target selection;
2. one GATT connection + Cyan notification subscription;
3. exactly one media-count write, guarded against retry;
4. exactly one P2P-enter write, guarded against retry;
5. original 10,000 ms credential window;
6. if no accepted credential appears, one additional 10,000 ms **passive** observation window with no second enter write;
7. exactly one allow-listed transfer-exit write, guarded against retry;
8. bounded exit observation.

A credential arriving during the original window is classified `NORMAL_VALID`.
A credential first arriving during the extra passive window is classified `LATE_VALID`.
Otherwise the report distinguishes:
- valid `0x41` activity rejected by credential structure;
- invalid-frame activity without valid `0x41`;
- no valid `0x41` credential candidate.

The extra 10-second passive window deliberately keeps the glasses in the entered transfer state longer than v0.6.8, but it sends no extra enter command and performs no Android Wi-Fi Direct connection. The window is bounded and followed by the one allow-listed exit command.

## Negative-scope review

Exact source contains:
- no physical-capture path;
- no Android Wi-Fi Direct discovery/connection APIs;
- no HTTP/URL code;
- no catalog endpoint;
- no media GET;
- no persistent media import;
- no receipt/ledger;
- no remote delete/mutation command;
- no raw Bluetooth address logging;
- no SSID/password value logging or persistence.

The package is separate from v0.6.8, so it cannot alter the v0.6.8 app-private import/receipt state.

## Decision

**AUTHORIZED FOR ONE BOUNDED PHYSICAL DIAGNOSTIC RUN.**

This is diagnostic evidence only. It cannot close G6A and cannot unblock G6B.

Do not take a photo during this diagnostic.
