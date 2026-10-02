# v0.6.8.2 Version Manifest

- Version: v0.6.8.2
- Build role: safe short-0x41 response classifier after v0.6.8.1 observed one TOO_SHORT frame at +72 ms and no credential frame within 20 s
- Package ID: `com.parkarsite.g6acreddiag682`
- Gate role: **DIAGNOSTIC ONLY**; cannot close G6A or unblock G6B
- Physical status: **REVOKED BEFORE PHYSICAL USE — RED-TEAM REPORT-INTEGRITY FINDINGS**

Purpose: determine whether a valid short `0x41` response is exactly ENTER-command-shaped, ENTER-prefix-plus-status, or some other safe short response. Only payloads with declared length <8 bytes may be rendered as hex; under the preserved credential parser these are too short to contain credential values.

### run-36967449098-attempt-1 — REVOKED BEFORE PHYSICAL USE
- exact bundle: `builds/run-36967449098-attempt-1/`
- build commit: `863062eca47d857b1e74fe3f4662125d7f7e12d7`
- APK SHA-256: `74c2141d8d42fc65a28515f7f3e779aa9d1f8101fbcf4ba802eade7d168df892`
- exact source ZIP SHA-256: `40e2d49cbe6a62155202de113cbe43505728ff9ebe396e04fac842e625dbd3fe`
- CI/static status: verified
- physical status: not yet run

### run-36967445804-attempt-1 — NOT PROMOTED
- exact bundle: `builds/run-36967445804-attempt-1/`
- build commit: `db8f8957056c5e537898a7af648852d9044a46a1`
- APK SHA-256: `35e63365341a31cbb4f819514880fc9388d4b3192d7eac1400ad75ba1846bfc6`
- exact source ZIP SHA-256: `9cc0aa6b0e03db7dfad48dab266f9d146ac704d3ac6b90855626d19bec42e138`
- CI/static status: verified
- physical status: not yet run


## Current authorized diagnostic candidate

Only this exact build is authorized:
- run: `36967449098`
- attempt: `1`
- build commit: `863062eca47d857b1e74fe3f4662125d7f7e12d7`
- package: `com.parkarsite.g6acreddiag682`
- APK SHA-256: `74c2141d8d42fc65a28515f7f3e779aa9d1f8101fbcf4ba802eade7d168df892`
- exact source ZIP SHA-256: `40e2d49cbe6a62155202de113cbe43505728ff9ebe396e04fac842e625dbd3fe`
- exact bundle: `builds/run-36967449098-attempt-1/`

Manual red-team:
`builds/run-36967449098-attempt-1/reviews/MANUAL_RED_TEAM_PREPROMOTION.md`

Physical instruction:
- force-stop Cyan Glasses;
- keep AIMB-G1 powered and paired;
- do not take a photo;
- run once;
- return the full report.

G6A remains OPEN. G6B remains BLOCKED.


## Red-team recheck — revocation

A second adversarial review found two evidence-integrity issues before physical use:

1. `classifySafeShort41()` mutates short-frame counters and was called both during ENTER observation and on post-EXIT `0x41` frames. A post-EXIT frame could therefore contaminate counters intended to classify only the ENTER credential-observation window.

2. The final summary printed `Raw notification payload logged: NO` even though the build intentionally permits hex logging of valid `0x41` payloads whose declared payload length is <8 bytes. Credential-capable payloads were still protected, but the summary wording was inaccurate.

No physical report from v0.6.8.2 is accepted. Do not run this build.

Replacement diagnostic must:
- count/classify/log safe short payloads **only during ENTER/LATE credential observation**;
- never classify/log post-EXIT payload bytes;
- state accurately that credential-capable payloads are not logged while safe short payloads may be logged.

G6A remains OPEN. G6B remains BLOCKED.
