# Manual red-team pre-promotion review — v0.6.8.3

## Exact build

- Version: `0.6.8.3`
- Build commit: `e282da324e8a77d56b4e3cdb6ce0b97711bd22ae`
- Build run: `36968820818`
- Build attempt: `1`
- Package: `com.parkarsite.g6acreddiag683`
- Runtime source blob: `48779142eacbe46d78b0f162e55a0e09f402acad`
- APK SHA-256: `2def2dbb77b260d9d9773349df99c6affacc026e7a1d8fdd44882f2b71edb6ec`
- Exact source ZIP SHA-256: `1e179e4d488b7aaaec70aabf6ead252a244f3cd5cca06c0fa3bbcd47953c5003`

## Artifact-level verification

The downloaded GitHub Actions artifact was independently re-hashed:
- APK hash exactly matched the immutable BUILD_INFO value;
- source ZIP hash exactly matched the immutable BUILD_INFO value;
- APK contains an APK Signing Block.

The exact archived source was extracted and both bundled audits were re-run:
- SAFETY_AUDIT.sh: PASS;
- RED_TEAM_AUDIT.sh: PASS.

The compiled app DEX containing `com.parkarsite.g6acreddiag683` was scanned for forbidden app-scope references. No app-scope references were found for:
- WifiP2p;
- HttpURLConnection;
- /files/ endpoints;
- INTERNET / NEARBY_WIFI_DEVICES / fine/coarse location permissions;
- credentialPassword value storage;
- expectedP2pName value storage.

## Transport parity

Compared against exact authorized v0.6.8.1 diagnostic:
- Cyan service UUID unchanged;
- Cyan notify UUID unchanged;
- Cyan write UUID unchanged;
- CCCD UUID unchanged;
- media-count command remains `02 04`;
- P2P ENTER remains `02 01 04 01`;
- transfer EXIT remains `02 01 09`;
- normal credential window remains 10,000 ms;
- passive late window remains 10,000 ms.

## Report-integrity recheck

The two v0.6.8.2 findings are fixed:
1. `classifySafeShort41(data)` is called only during ENTER_SENT/LATE_OBSERVE.
2. Post-EXIT `0x41` frames are reported only as ignored; their payload is not classified/logged and they cannot mutate ENTER-observation short-response counters.
3. The inaccurate `Raw notification payload logged: NO` summary no longer exists.
4. The report now states:
   - `Credential-capable notification payload bytes logged: NO`;
   - whether safe short (<8-byte) `0x41` payload bytes were logged during ENTER/LATE observation.

A further edge-case review also fixed mixed short-response classification:
- `SHORT_ENTER_ECHO_SHAPE_ONLY` is emitted only when every observed `0x41` belongs to that exact short shape;
- `SHORT_ENTER_PREFIX_STATUS_ONLY` is emitted only when every observed `0x41` belongs to that shape;
- `OTHER_SAFE_SHORT_0x41_ONLY` is emitted only when every observed `0x41` belongs to that category;
- mixed short shapes and/or longer rejected `0x41` activity produce `MIXED_SHORT_OR_REJECTED_0x41_ACTIVITY`.

## Privacy boundary

Safe payload hex rendering requires:
- valid Cyan frame length/CRC;
- command `0x41`;
- declared payload length strictly <8 bytes;
- actual frame length = declared payload + 6.

Under the preserved credential parser, the first 8 payload bytes are required for:
- `02 01 04 01` prefix;
- 2-byte SSID length;
- 2-byte password length.

Therefore credential value bytes cannot begin in a payload shorter than 8 bytes.

Credential-capable payload bytes (declared payload >=8) are never rendered.

## Negative scope

Exact source contains:
- no physical-capture path;
- no Android Wi-Fi Direct API;
- no HTTP/URL code;
- no catalog endpoint;
- no media GET;
- no persistent media import;
- no receipt/ledger;
- no remote delete/mutation command;
- no SSID/password value storage;
- no Bluetooth address logging.

## Build-history disposition

NOT PROMOTED:
- v0.6.8.2 run `36967445804`;
- v0.6.8.2 run `36967449098` — revoked before physical use after report-integrity review;
- v0.6.8.3 run `36968350504`;
- v0.6.8.3 run `36968598634`.

Only v0.6.8.3 run `36968820818`, attempt 1, commit `e282da324e8a77d56b4e3cdb6ce0b97711bd22ae` is accepted.

## Decision

**AUTHORIZED FOR ONE BOUNDED PHYSICAL DIAGNOSTIC RUN.**

Instruction:
- force-stop Cyan Glasses;
- keep AIMB-G1 powered and paired;
- do not take a photo;
- run once;
- return the complete report.

This is diagnostic evidence only. It cannot close G6A and cannot unblock G6B.
