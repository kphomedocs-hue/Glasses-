# GitHub Desktop synchronization — G6A v0.6.8.5 corrected source

This ZIP contains **repository-relative paths**, not a nested `Glasses-` folder.
It includes only new paths verified missing from GitHub `main` on 11 October
2026 plus a new review document. It deliberately excludes the three root
history files (`00_NEXT_CHAT_HANDOVER.md`, `LATEST_CHECKPOINT.md`,
`docs/testing/GATE_ROADMAP.md`) to prevent unreviewed overwrites. Their
current `main` contents remain authoritative. The same status is recorded in
the new `docs/research/G6A_v0.6.8.5_SAFETY_REWORK_2026-10-11.md`.

**Using GitHub Desktop on Windows**

1. Open the existing local repository `Glasses-` in GitHub Desktop and run
   Fetch origin, then pull the latest `main`. Resolve any local changes before
   copying these files.
2. Extract this ZIP **into the root of the existing `Glasses-` repository**,
   preserving subfolders. There should be exactly one `Glasses-` directory,
   not `Glasses-/Glasses-/`.
3. Verify that GitHub Desktop shows only these paths as new or modified:
   `source/v0.6.8.5/`, `.github/workflows/build-g6a-cyan-p2p-state-diagnostic-v0.6.8.5.yml`,
   `tools/archive_verified_build_v0.6.8.5.sh`, and the two
   `docs/research/` documents. Do not commit APKs or credentials.
4. Commit with message `Research: harden G6A v0.6.8.5 bounded P2P diagnostic (no APK approval)`
   and push the commit to GitHub.
5. Verify uploaded source against the local files before any manual CI dispatch.
   The workflow will **not** auto-build on push because it is manual-only.
6. Do **not** install a new APK or physically test until a separate manual
   red-team review plus CI build/lint/signature checks authorize the exact run.

Local prebuild checks (Linux / Git Bash with Java available):

```
cd source/v0.6.8.5
bash SAFETY_AUDIT.sh
bash RED_TEAM_AUDIT.sh
bash STATE_MACHINE_AUDIT.sh
```

G6A remains OPEN. G6B remains BLOCKED.

**Provenance-fix addendum (11 October 2026):** Source package now contains
build-time `GITHUB_SHA`, `GITHUB_RUN_ID`, and `GITHUB_RUN_ATTEMPT` in the app's
exported report header. Do not use any prior ZIP of v0.6.8.5 for the next build.

**Independent safety recheck (11 October 2026):** Fixed group-removal policy
to require actual observed group formation by this test. Final local checks:
73 host-JVM policy assertions and 26 structural checks passed. This package
supersedes earlier v0.6.8.5 ZIPs. CI compile/lint, signature, immutable build
archive and physical authorization are still pending.
