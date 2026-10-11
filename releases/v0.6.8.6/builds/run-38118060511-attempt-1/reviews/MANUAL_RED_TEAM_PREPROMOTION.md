# G6A v0.6.8.6 — Independent exact-build manual red-team review
Date: 2026-10-11; gate scope: bounded **passive** peer-identity and early 0x73 event discriminator only.

## Exact provenance
- Build source commit: `90c2acb4f8736acf6ff024bfa3ba660ead09dad9`.
- GitHub Actions run `38118060511`, attempt `1`: all steps SUCCESS.
- Application ID: `com.parkarsite.g6apeerdiag686`, version `0.6.8.6`.
- APK SHA-256: `d05ce2a23d25d409fc583c3830b9e574577586f2eb2f21e830895d9eece099d0`.
- Exact source ZIP SHA-256: `9202118df6eb15a5628fdf0d199c909bc0b8ca47b01e02b93396bdc02422473b`.
- Actual GitHub Actions artifact downloaded and these two hashes independently recalculated: MATCH.
- APK DEX strings contain exact commit, run, version and package identity; no `WifiP2pConfig`, `removeGroup`, `cancelConnect`, `requestConnectionInfo`, `HttpURLConnection`, or `media.config` method/symbol strings. Android-supported classes may contain unrelated filesystem references; absence of an app-owned file-output operation is confirmed from exact source, not asserted for every runtime-library DEX byte.
- Safety shell audit, independent red-team audit, host-JVM policy tests, state-machine structural checks, Gradle assembleDebug, Android Lint, apksigner verify, immutable archive, GitHub artifact upload: PASS.
- Android Lint report: 12 warnings, none failed the CI lint step. Warnings include platform/dependency/backup/icon and UI text-localization items; these do not establish real-device correctness.

## Source code and state-machine review
1. Select exactly one bonded AIMB-G1 family device; never log its full name suffix or BLE address.
2. Require pre-existing Wi-Fi Direct group absence via a read-only platform query; abort if a group exists.
3. BLE GATT and notifications before one bounded `discoverPeers`, followed by precisely one `0x41/02 01 04 01` ENTER. No initialization, inventory, reset, AP, retry, arbitrary BLE console, HTTP, file, capture or media access.
4. `requestPeers` reads names **in memory**, then stores/reports only bounded mutually exclusive categories (missing, family-prefix, full 12-hex paired-address suffix, other). Eight reported snapshot maximum; no raw name, SSID, address, hash/token or per-peer output. An observed generic-prefix peer NEVER causes a connection.
5. Code includes NO Wi-Fi Direct `connect()`, `cancelConnect()`, `removeGroup()`, `WifiP2pConfig`, `WpsInfo` or connection-info/group-formation request. One visible peer does not establish target identity.
6. CRC-valid BLE 0x73 event IDs only are stored in bounded lists (maximum 24 per bucket) for pre-ENTER, ENTER-write-callback-pending and post-ENTER-callback, with overflow counters. No 0x73 payload fields are exposed.
7. A central terminal routine requests one 0x41/02 01 09 EXIT when ENTER was attempted; if ENTER is in flight it defers EXIT for a bounded time, then attempts once and flags failure/uncertainty. EXIT success is determined by write callback, not generic 0x41 response.
8. Cleanup independently deadlines discovery stop and group queries; it NEVER removes any group. Two read-only post-cleanup verifications and EXIT success are required for a clean result. A group present/unknown results in fail-closed report.
9. No successful observation may mark G6A PASS: classification is INCONCLUSIVE even if a suffix-class peer appears, because no association/media import is attempted.

## Discovered non-blocking documentation issues
- `BUILD_INFO.md` carries a stale line claiming a `15000 ms connection bound`. The exact v0.6.8.6 source contains no connection phase. Treat the line as stale metadata, not as a performed connection.
- `BUILD_INFO.md` includes a literal escaped `\n` between two credential-related information clauses. This does not affect compiled logic or artifact identity.
- The older source-snapshot `PHYSICAL_TEST.md` says not yet authorized. This is historically correct as of source freeze; the **later dated immutable manual review plus version manifest** decides the exact physical candidate.

## Residual limitations
- First-party Cyan's stored exact Wi-Fi name remains unavailable to this K diagnostic. Peer category counts are not device authentication.
- Peer reports can reflect cached Android peers or missed broadcasts, and are not proof of visibility/invisibility at the glasses.
- A pending-write 0x73 event may be unrelated to ENTER; timing order does not establish causation. No credential bytes are collected.
- Android process death, missing BLE callback and group-query timing can prevent deterministic cleanup, although this diagnostic never initiates a Wi-Fi Direct connection.
- First physical run must be interpreted as a bounded hypothesis discriminator, not a production functional gate.

## Decision — CONDITIONAL PASS FOR ONE SUPERVISED PASSIVE PHYSICAL DIAGNOSTIC ONLY
Exact build `38118060511`, attempt `1`, hash above is authorized for **one** operator-triggered passive diagnostic, provided:
- Force-stop Cyan Glasses; keep AIMB-G1 paired with Bluetooth and Wi-Fi enabled; no photo/capture/reset/unpair/retry.
- Check there is no pre-existing Wi-Fi Direct group; if unsure, do not change it. The app will also perform its read-only preflight.
- Install only the exact APK; grant Bluetooth/Nearby Wi-Fi permissions; leave app foregrounded; tap the run button once.
- Do not rerun if a permission failure, group warning or cleanup failure/indeterminate status occurs.
- Return the full sanitized report. Evaluate raw counts and event IDs only as hypotheses; never connect automatically to the sole observed peer.
- Do not resume G6A media-import testing until the report is reviewed.

**No production APK promotion. G6A OPEN. G6B BLOCKED.**
