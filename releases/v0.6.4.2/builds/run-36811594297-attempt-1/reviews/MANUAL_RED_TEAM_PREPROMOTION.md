# Manual Red-Team Pre-Promotion Review — run 36811594297 attempt 1

Date: 2026-10-01

## Provenance

PASS:
- immutable build folder exists;
- VERSION_MANIFEST values are populated correctly;
- BUILD_INFO identifies build commit/run/attempt;
- source ZIP was generated from exact GITHUB_SHA;
- APK/source hashes are recorded.

## Runtime/state-machine review

PASS:
- generic 0x41 does not prove exit;
- exit confirmation requires post-callback matching 0x73/0x01;
- centralized abort path performs bounded exit/cleanup;
- exact case-sensitive peer name;
- no per-file identity tokens;
- exact-line catalog rejection;
- full BLE/catalog media parity.

FAIL:
- valid transfer credentials can trigger startP2pDiscovery() while the app is still waiting for the Android characteristic-write callback for the P2P-enter command.
- therefore the enter handoff is not yet a complete dual-condition handshake.

## Decision

**NOT PROMOTED. DO NOT PHYSICALLY RUN THIS BUILD.**

Required fix: discovery must begin only after BOTH:
1. P2P-enter Android write callback SUCCESS; and
2. valid transfer-credential response.
