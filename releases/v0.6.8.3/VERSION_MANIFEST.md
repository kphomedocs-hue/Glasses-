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

### run-36968350504-attempt-1
- exact bundle: `builds/run-36968350504-attempt-1/`
- build commit: `a4e70723b55763a4679deba20feea0ce51938ae3`
- APK SHA-256: `0e232c508f6d324573c9a16bb210636f7c6aa281a1d2da88ecc1452b8532bb82`
- exact source ZIP SHA-256: `659bf61c8fd5381d009dc4da5b93ed5e77c5c95bb54a6ae86fc017f2fd5d3e0e`
- CI/static status: verified
- physical status: not yet run

### run-36968598634-attempt-1
- exact bundle: `builds/run-36968598634-attempt-1/`
- build commit: `6f45982f862997149ccd1e395d3a52d62164ca48`
- APK SHA-256: `23375cd95affcb24ac15f6f7079f57a048494de90aa18c3c76dbed746cea80dc`
- exact source ZIP SHA-256: `98045bdad5ee12608dd9e2439ee7a375c5d64240c23bfe95ea8b3da394770433`
- CI/static status: verified
- physical status: not yet run
