# Version Evidence Layout

This repository keeps every APK build and the reports produced by that exact version together.

## Canonical layout

```
source/vX.Y.Z/                  # source for that version
releases/vX.Y.Z/                # version evidence bundle
  VERSION_MANIFEST.md
  BUILD_INFO.md
  <APK>
  <APK/SOURCE/PACKAGE SHA256 files>
  <source zip>
  <package zip>
  lint-results-debug.html
  reports/
    README.md
    <physical reports from this exact APK>
```

## Rules

1. Every new app version gets its own `source/vX.Y.Z/` and `releases/vX.Y.Z/` folders.
2. Physical reports produced by an installed APK are stored under that exact version's `releases/vX.Y.Z/reports/` folder.
3. Reports must never be moved into another version folder even if a later version interprets them.
4. A report may also be mirrored/indexed under `docs/testing/results/` for chronological research history, but the version-local copy is the primary evidence bundle.
5. `VERSION_MANIFEST.md` identifies the build, status, build provenance, APK hash where available, and associated reports.
6. Build/archive scripts must preserve an existing `reports/` directory and version manifest. Rebuilding a release must never delete physical evidence.
7. Superseded or failed builds remain in their own version folders for provenance and are never overwritten by a newer APK.
8. Do not attach a report to a version unless its provenance is sufficiently established.
9. Privacy boundaries remain unchanged: no credentials or raw remote filename/path values in stored reports.

This layout is mandatory for future AIMB-G1 builds.
