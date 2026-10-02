# v0.6.8.3 Version Manifest

- Version: v0.6.8.3
- Build role: corrected safe short-0x41 response classifier after second red-team review revoked v0.6.8.2 before physical use
- Package ID: `com.parkarsite.g6acreddiag683`
- Gate role: **DIAGNOSTIC ONLY**; cannot close G6A or unblock G6B
- Physical status: **AUTHORIZED FOR ONE BOUNDED PHYSICAL DIAGNOSTIC RUN**

Purpose: determine whether the short valid `0x41` response observed in v0.6.8.1 is exact ENTER-command-shaped, ENTER-prefix-plus-status, or another safe short response.

Privacy/report-integrity rules:
- short payload hex may be rendered only during ENTER/LATE credential observation;
- only valid framed `0x41` payloads with declared payload length <8 bytes may be rendered;
- post-EXIT `0x41` payloads are never classified or logged;
- credential-capable payload bytes are never logged;
- SSID/password values are never decoded/logged/persisted.

### run-36968350504-attempt-1 — NOT PROMOTED
- exact bundle: `builds/run-36968350504-attempt-1/`
- build commit: `a4e70723b55763a4679deba20feea0ce51938ae3`
- APK SHA-256: `0e232c508f6d324573c9a16bb210636f7c6aa281a1d2da88ecc1452b8532bb82`
- exact source ZIP SHA-256: `659bf61c8fd5381d009dc4da5b93ed5e77c5c95bb54a6ae86fc017f2fd5d3e0e`
- CI/static status: verified
- physical status: not yet run

### run-36968598634-attempt-1 — NOT PROMOTED
- exact bundle: `builds/run-36968598634-attempt-1/`
- build commit: `6f45982f862997149ccd1e395d3a52d62164ca48`
- APK SHA-256: `23375cd95affcb24ac15f6f7079f57a048494de90aa18c3c76dbed746cea80dc`
- exact source ZIP SHA-256: `98045bdad5ee12608dd9e2439ee7a375c5d64240c23bfe95ea8b3da394770433`
- CI/static status: verified
- physical status: not yet run

### run-36968820818-attempt-1 — AUTHORIZED DIAGNOSTIC CANDIDATE
- exact bundle: `builds/run-36968820818-attempt-1/`
- build commit: `e282da324e8a77d56b4e3cdb6ce0b97711bd22ae`
- APK SHA-256: `2def2dbb77b260d9d9773349df99c6affacc026e7a1d8fdd44882f2b71edb6ec`
- exact source ZIP SHA-256: `1e179e4d488b7aaaec70aabf6ead252a244f3cd5cca06c0fa3bbcd47953c5003`
- CI/static status: verified
- physical status: not yet run


## Current and only authorized diagnostic candidate

- run: `36968820818`
- attempt: `1`
- build commit: `e282da324e8a77d56b4e3cdb6ce0b97711bd22ae`
- package: `com.parkarsite.g6acreddiag683`
- APK SHA-256: `2def2dbb77b260d9d9773349df99c6affacc026e7a1d8fdd44882f2b71edb6ec`
- exact source ZIP SHA-256: `1e179e4d488b7aaaec70aabf6ead252a244f3cd5cca06c0fa3bbcd47953c5003`
- exact bundle: `builds/run-36968820818-attempt-1/`

Manual red-team:
`builds/run-36968820818-attempt-1/reviews/MANUAL_RED_TEAM_PREPROMOTION.md`

This exact build supersedes and invalidates all earlier v0.6.8.2 and v0.6.8.3 diagnostic candidates for physical use.

Physical instruction:
- force-stop Cyan Glasses;
- keep AIMB-G1 powered and paired;
- do not take a photo;
- run once;
- return the complete report.

G6A remains OPEN. G6B remains BLOCKED.
