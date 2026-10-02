# v0.6.8.2 Version Manifest

- Version: v0.6.8.2
- Build role: safe short-0x41 response classifier after v0.6.8.1 observed one TOO_SHORT frame at +72 ms and no credential frame within 20 s
- Package ID: `com.parkarsite.g6acreddiag682`
- Gate role: **DIAGNOSTIC ONLY**; cannot close G6A or unblock G6B
- Physical status: **NO PHYSICAL CANDIDATE YET**

Purpose: determine whether a valid short `0x41` response is exactly ENTER-command-shaped, ENTER-prefix-plus-status, or some other safe short response. Only payloads with declared length <8 bytes may be rendered as hex; under the preserved credential parser these are too short to contain credential values.
