# v0.6.8.3 Version Manifest

- Version: v0.6.8.3
- Build role: corrected safe short-0x41 response classifier after second red-team review revoked v0.6.8.2 before physical use
- Package ID: `com.parkarsite.g6acreddiag683`
- Gate role: **DIAGNOSTIC ONLY**; cannot close G6A or unblock G6B
- Physical status: **NO PHYSICAL CANDIDATE YET**

Purpose: determine whether the short valid `0x41` response observed in v0.6.8.1 is exact ENTER-command-shaped, ENTER-prefix-plus-status, or another safe short response.

Privacy/report-integrity rules:
- short payload hex may be rendered only during ENTER/LATE credential observation;
- only valid framed `0x41` payloads with declared payload length <8 bytes may be rendered;
- post-EXIT `0x41` payloads are never classified or logged;
- credential-capable payload bytes are never logged;
- SSID/password values are never decoded/logged/persisted.
