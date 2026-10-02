# Manual pre-promotion red-team review — v0.6.8.2

## Exact build

- Version: `0.6.8.2`
- Build commit: `863062eca47d857b1e74fe3f4662125d7f7e12d7`
- Build run: `36967449098`
- Build attempt: `1`
- Package: `com.parkarsite.g6acreddiag682`
- Runtime source blob: `f44f972fa66734effab2214e0a63e7ee3ab43188`
- APK SHA-256: `74c2141d8d42fc65a28515f7f3e779aa9d1f8101fbcf4ba802eade7d168df892`
- Exact source ZIP SHA-256: `40e2d49cbe6a62155202de113cbe43505728ff9ebe396e04fac842e625dbd3fe`

## Automated gates

- safety audit: PASS;
- red-team static audit: PASS;
- Android compile: PASS;
- Android Lint: PASS;
- APK signature verification: PASS;
- immutable exact-build archive: PASS.

## Transport parity

Compared with exact authorized v0.6.8.1:
- Cyan service UUID unchanged;
- Cyan notify UUID unchanged;
- Cyan write UUID unchanged;
- CCCD UUID unchanged;
- media-count payload remains `02 04`;
- P2P-enter payload remains `02 01 04 01`;
- transfer-exit payload remains `02 01 09`;
- no-retry guards remain;
- normal credential window remains 10,000 ms;
- passive late-observation window remains 10,000 ms;
- credential structural acceptance boundary remains unchanged.

## New observation boundary

v0.6.8.2 may render payload hex only when all of these are true:
1. the full Cyan frame already passed frame-length and CRC validation;
2. command is `0x41`;
3. declared payload length is **strictly less than 8 bytes**;
4. actual frame length equals declared payload length + 6-byte Cyan header.

This is narrower than the minimum credential structure. The preserved credential parser requires:
- four-byte `02 01 04 01` prefix;
- two-byte SSID length;
- two-byte password length;
before any SSID/password value bytes can begin.

Therefore a payload shorter than 8 bytes cannot contain an SSID/password value under the protocol boundary being diagnosed.

Safe short-response classifications:
- exact payload `02 01 04 01` -> `SHORT_ENTER_ECHO_SHAPE`;
- payload begins `02 01 04 01` and contains 1–3 additional bytes -> `SHORT_ENTER_PREFIX_PLUS_STATUS`;
- any other valid payload shorter than 8 bytes -> `OTHER_SAFE_SHORT_0x41`.

The diagnostic does not render payload bytes from credential-capable frames (declared payload length >=8).

## Negative-scope review

Exact source contains:
- no capture path;
- no Android Wi-Fi Direct API;
- no HTTP/URL code;
- no catalog/media endpoint;
- no media GET;
- no persistent import;
- no receipt/ledger;
- no remote delete/mutation command;
- no SSID/password value storage;
- no raw credential-bearing payload logging;
- no Bluetooth address logging.

The package is separate from v0.6.8 and v0.6.8.1, so it cannot alter their app-private state.

## Duplicate CI run handling

Run `36967445804` was produced from commit `db8f8957056c5e537898a7af648852d9044a46a1` before the archive report-header template was corrected. It is **NOT PROMOTED**.

Only run `36967449098`, attempt 1, commit `863062eca47d857b1e74fe3f4662125d7f7e12d7` is authorized.

## Decision

**AUTHORIZED FOR ONE BOUNDED PHYSICAL DIAGNOSTIC RUN.**

Instruction:
- force-stop Cyan Glasses;
- keep AIMB-G1 powered and paired;
- do not take a photo;
- run once;
- preserve and return the complete report.

This build is diagnostic evidence only. It cannot close G6A and cannot unblock G6B.
