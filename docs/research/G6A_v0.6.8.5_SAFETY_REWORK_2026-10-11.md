# G6A v0.6.8.5 — corrected source review record

Date: 2026-10-11
Status: **SOURCE-ONLY CANDIDATE — NOT AN AUTHORIZED APK OR PHYSICAL RUN**
Gate state: **G6A OPEN / G6B BLOCKED**

## Evidence and scope

Starting material: uploaded `Glasses-g6a-next-files.zip` (2026-10-08
Cyan comparison and original v0.6.8.5 implementation). Prior red-team report:
`G6A_v0.6.8.5_RED_TEAM_BLOCKERS_2026-10-11.md` (7 blockers).
The current revision is a new source proposal. Preserve historical releases.

## Seven findings: source-level remediations

| Finding | Revision | Verification still required |
| --- | --- | --- |
| Incomplete group cleanup | One bounded terminal cleanup pipeline (stop discovery, cancel pending connect, inspect group, conditionally remove owned group, twice verify absence) | Android API integration + device test |
| Shared timeout cancels discovery | Independently named wall-clock deadlines for preflight/BLE/discovery/association/observation/EXIT/cleanup | Android scheduling / callback ordering |
| EXIT skipped on failures | Central terminal route attempts one EXIT after any attempted ENTER, or records transport/write impossibility | BLE callback races on device |
| Positive result despite retained group | Cleanup policy blocks positive classification without confirmed absence and successful EXIT | Physical cleanup proof |
| Unbounded preflight/cleanup callbacks | Distinct preflight, per-step and overall cleanup watchdogs | OS callback stalls |
| Unfenced asynchronous event reporting | `0x73` events included only after successful ENTER callback and before EXIT | Real notification ordering |
| Incomplete permission/lifecycle failure accounting | Precondition failure report, foreground-loss terminal request, one-session-only run | Android process kill cannot guarantee cleanup |

## Important sequencing clarification

The BLE connection and notify subscription now complete **before** the
16-second Wi-Fi Direct discovery window starts. Android peer discovery starts
**before** P2P ENTER. This reflects Cyan's crucial discovery-before-ENTER
property without incorrectly consuming the discovery deadline while GATT is
still connecting.

Group removal is permitted only if clean preflight succeeded, this app requested
association, and the resulting group contains the exact selected Wi-Fi Direct
peer. A group with unverified identity is never removed automatically;
classification becomes inconclusive and requires manual examination.

## Results available without Android SDK

- `SAFETY_AUDIT.sh`: PASS.
- `RED_TEAM_AUDIT.sh`: PASS.
- `STATE_MACHINE_AUDIT.sh`: PASS (host-JVM safety-policy permutations and
  structural checks for named timers, cleanup/EXIT paths and event fencing).
- `DiagnosticSafetyRulesTest.java`: 72 assertions PASS.

**Limitations:** Static checks do not prove that Android APIs are correctly
invoked, callbacks always fire, the app compiles against SDK 36, or Wi-Fi
Direct groups are removed on the physical device. A process kill during
cleanup is inherently not recoverable through Activity callbacks. This patch
has **no physical validation** and no build/signature provenance.

## Release safeguard

The v0.6.8.5 GitHub Action is changed to manual `workflow_dispatch` only.
Source push must not auto-promote a diagnostic APK. A distinct independent
manual red-team review and verified CI build/lint/signature/archive review are
required before authorizing one physically bounded diagnostic.

Do not repeat v0.6.8.4; do not start G6B; do not label G6A PASS.
