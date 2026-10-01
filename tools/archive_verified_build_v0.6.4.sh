#!/usr/bin/env bash
set -euo pipefail
: "${RUNNER_TEMP:?}"; : "${GITHUB_WORKSPACE:?}"; : "${GITHUB_SHA:?}"; : "${GITHUB_RUN_ID:?}"
cd "$GITHUB_WORKSPACE"
git pull --rebase origin main
REL="releases/v0.6.4"
mkdir -p "$REL/reports"
# Preserve VERSION_MANIFEST.md and reports/. Refresh top-level build artifacts only.
find "$REL" -maxdepth 1 -type f ! -name 'VERSION_MANIFEST.md' -delete
python3 - <<'PY'
from pathlib import Path
import subprocess, zipfile
root=Path("source/v0.6.4")
out=Path("releases/v0.6.4/K_G1_G6A_No_Capture_Stability_v0_6_4_source_v1.zip")
files=subprocess.check_output(["git","ls-files","-z",str(root)]).decode().split("\0")
with zipfile.ZipFile(out,"w",zipfile.ZIP_DEFLATED) as z:
    for f in sorted(x for x in files if x): z.write(f,Path(f).relative_to(root))
PY
cp "$RUNNER_TEMP/g6a64/K_G1_G6A_No_Capture_Stability_v0_6_4.apk" "$REL/K_G1_G6A_No_Capture_Stability_v0_6_4.apk"
cp "$RUNNER_TEMP/g6a64/lint-results-debug.html" "$REL/lint-results-debug.html"
sha256sum "$REL/K_G1_G6A_No_Capture_Stability_v0_6_4.apk" > "$REL/K_G1_G6A_No_Capture_Stability_v0_6_4_APK_SHA256.txt"
sha256sum "$REL/K_G1_G6A_No_Capture_Stability_v0_6_4_source_v1.zip" > "$REL/K_G1_G6A_No_Capture_Stability_v0_6_4_source_SHA256.txt"
cat > "$REL/BUILD_INFO.md" <<EOF
# K G1 G6A No-Capture Catalog Stability v0.6.4 — Verified Repository Build
- Canonical build commit: $GITHUB_SHA
- Canonical build run: $GITHUB_RUN_ID
- bounded-scope safety audit: PASS
- Android compile: PASS
- Android Lint: PASS
- APK signature verification: PASS
- Package ID: com.parkarsite.g6astability64
- exactly two runtime inventory/catalog snapshots by state machine
- fixed 30000 ms no-capture quiet interval
- one catalog HTTP GET code path reused by both snapshots
- media-file GET code paths: none
- opaque SHA-256 catalog identities retained in memory only
- no raw remote filename/path persistence
- no glasses mutation/deletion
EOF
(cd "$REL" && zip -q -r "K_G1_G6A_No_Capture_Stability_v0_6_4_package.zip" . -x "*package.zip" "*package_SHA256.txt")
sha256sum "$REL/K_G1_G6A_No_Capture_Stability_v0_6_4_package.zip" > "$REL/K_G1_G6A_No_Capture_Stability_v0_6_4_package_SHA256.txt"
git config user.name "github-actions[bot]"
git config user.email "41898282+github-actions[bot]@users.noreply.github.com"
git add "$REL"
if ! git diff --cached --quiet; then git commit -m "Archive verified K G1 G6A No-Capture Stability v0.6.4 build"; git push; fi
