# G6A v0.6.8.5 — prebuild provenance recheck (11 October 2026)

## Decision
The source package is ready for **GitHub synchronization and independent Android CI compile/lint**, but **not** authorized for physical device testing, APK promotion, G6A closure, or G6B initiation.

## Evidence
- Source package contains the bounded Cyan-style P2P association discriminator without HTTP, media download, imports, or proprietary reset commands.
- Checks performed locally against the exact package after patching:
  - `SAFETY_AUDIT.sh`: PASS
  - `RED_TEAM_AUDIT.sh`: PASS
  - `STATE_MACHINE_AUDIT.sh`: PASS (73 host-JVM policy assertions plus 26 source structural checks)
- Android SDK/Gradle are not available in the local review runtime; **Android compilation, lint, APK signing and device testing were not performed**.
- GitHub `main` remains the durable authority. No source was pushed by this review environment.

## Newly identified issue and correction
The previous source exported an app version but not the exact commit/run/attempt demanded by `tools/archive_verified_build_v0.6.8.5.sh` for physical reports. A physical report could not be uniquely matched to an immutable build. This is a **blocking provenance issue**.

Corrected source:
1. `app/build.gradle.kts` enables BuildConfig and injects `GITHUB_SHA`, `GITHUB_RUN_ID`, and `GITHUB_RUN_ATTEMPT` at compile time.
2. Both the normal diagnostic report and permission/precondition abort reports include the four identity lines: App version, Build commit, Build run, Build attempt.
3. Local builds embed `LOCAL_UNVERIFIED` and must never be treated as an authorized physical artifact.
4. The structural audit now asserts that all three metadata fields are referenced by the report path.

## Remaining external gates
1. Push exact source to GitHub (verify no nested folder and do not overwrite repository handover, checkpoint, or gate roadmap).
2. Run the manual-only GitHub Actions workflow on the reviewed source commit; record SHA, CI run ID, attempt, artifact hashes, lint and signing results.
3. Independently review runtime callback/timeout/cleanup implementation and immutable exact-build archive.
4. Record a separate **explicit physical authorization decision** before testing any APK.

## Gate status
**G6A OPEN; G6B BLOCKED.** No APK candidate is authorized by this document.

## Second independent safety correction — group ownership
During manual review, `DiagnosticSafetyRules.mayRemoveGroup(...)` accepted a
`formedByTest` argument but failed to enforce it. Previously, a clean preflight,
connection request, and matching peer identity could authorize deletion of a
group formed by another actor during the test. The new predicate explicitly
requires `formedByTest` **as well as** the previous checks. A new regression
assertion forbids removal when that condition is false. If group attribution
remains inconclusive, cleanup intentionally does not delete it and the terminal
classification fails closed.
