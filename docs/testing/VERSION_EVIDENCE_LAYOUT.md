# Version Evidence Layout

This repository keeps every APK build and the reports produced by that exact build together.

## Canonical layout

```
source/vX.Y.Z/                              # source family for the version
releases/vX.Y.Z/                            # version container
  VERSION_MANIFEST.md                       # version/build index only
  builds/
    run-<github-run-id>/                     # immutable exact build
      BUILD_INFO.md
      <APK>
      <APK/SOURCE/PACKAGE SHA256 files>
      <source zip>
      <package zip>
      lint-results-debug.html
      reports/
        <physical reports from this exact APK>
      reviews/
        <red-team/static reviews of this exact build>
```

Legacy top-level release artifacts may remain for history, but once a version has more than one build they are **not authoritative**.

## Mandatory rules

1. Every app version gets its own `source/vX.Y.Z/` and `releases/vX.Y.Z/` container.
2. Every CI build is immutable and is stored under `releases/vX.Y.Z/builds/run-<run-id>/`.
3. A build directory is never overwritten, deleted, or reused by another run.
4. Physical reports belong under the exact build directory that generated them:
   `releases/vX.Y.Z/builds/run-<run-id>/reports/`.
5. A version-level `reports/` folder may exist only as a mirror/index for convenience; it is not sufficient provenance when multiple builds exist.
6. `VERSION_MANIFEST.md` lists all known builds for that version, their hashes/status, and identifies which build was physically tested.
7. A physical report must never be reassigned to a later rebuild merely because the app version string is the same.
8. Build/archive scripts must never replace an existing APK/build bundle.
9. Source archives must be created from the exact build commit (`GITHUB_SHA`), not from a mutable branch state after `git pull`.
10. Build artifacts should be staged outside the repository before syncing `main` for the archival commit.
11. Changing archive tooling must not silently replace a previously verified APK under the same build identity.
12. Superseded/failed builds remain preserved for provenance.
13. `LATEST_CHECKPOINT.md` is current-only. Long historical checkpoints belong under `docs/history/`.
14. Privacy boundaries remain unchanged: no credentials or raw remote filename/path values in stored reports.
15. Deterministic per-file hash tokens should not be emitted in reports unless essential; prefer counts and set-difference summaries.

## Historical evidence repair

Immutable exact-build bundles have been created for:
- v0.6.1 / run `36461373394`;
- v0.6.2 / run `36466571422`;
- v0.6.3 / run `36585496838` — this contains the exact APK associated with the physical +2 report;
- v0.6.4.1 / run `36808762256` — preserved for provenance, but red-team blocked from physical use.

This layout is mandatory for future AIMB-G1 builds.
