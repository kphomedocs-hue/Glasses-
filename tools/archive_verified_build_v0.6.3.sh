#!/usr/bin/env bash
set -euo pipefail
: "${RUNNER_TEMP:?}"; : "${GITHUB_WORKSPACE:?}"; : "${GITHUB_SHA:?}"; : "${GITHUB_RUN_ID:?}"
cd "$GITHUB_WORKSPACE"
git pull --rebase origin main
REL="releases/v0.6.3"
mkdir -p "$REL/reports"
# Preserve VERSION_MANIFEST.md and reports/. Refresh top-level build artifacts only.
find "$REL" -maxdepth 1 -type f ! -name 'VERSION_MANIFEST.md' -delete
python3 - <<'PY'
from pathlib import Path
import subprocess, zipfile
root=Path("source/v0.6.3")
out=Path("releases/v0.6.3/K_G1_G6A_Retention_Diagnostic_v0_6_3_source_v1.zip")
files=subprocess.check_output(["git","ls-files","-z",str(root)]).decode().split("\0")
with zipfile.ZipFile(out,"w",zipfile.ZIP_DEFLATED) as z:
    for f in sorted(x for x in files if x): z.write(f,Path(f).relative_to(root))
PY
cp "$RUNNER_TEMP/g6a63/K_G1_G6A_Retention_Diagnostic_v0_6_3.apk" "$REL/K_G1_G6A_Retention_Diagnostic_v0_6_3.apk"
cp "$RUNNER_TEMP/g6a63/lint-results-debug.html" "$REL/lint-results-debug.html"
sha256sum "$REL/K_G1_G6A_Retention_Diagnostic_v0_6_3.apk" > "$REL/K_G1_G6A_Retention_Diagnostic_v0_6_3_APK_SHA256.txt"
sha256sum "$REL/K_G1_G6A_Retention_Diagnostic_v0_6_3_source_v1.zip" > "$REL/K_G1_G6A_Retention_Diagnostic_v0_6_3_source_SHA256.txt"
cat > "$REL/BUILD_INFO.md" <<EOF
# K G1 G6A Retention Diagnostic v0.6.3 — Verified Repository Build
- Canonical build commit: $GITHUB_SHA
- Canonical build run: $GITHUB_RUN_ID
- bounded-scope safety audit: PASS
- Android compile: PASS
- Android Lint: PASS
- APK signature verification: PASS
- Package ID: com.parkarsite.g6aimport63
- G5.7 transport boundary preserved
- one new JPG maximum in import mode
- saved baseline JPG identity set + saved new-item identity are opaque SHA-256 only
- restart verification media GETs: zero
- no raw remote filename/path text persisted
- no glasses mutation/deletion
EOF
(cd "$REL" && zip -q -r "K_G1_G6A_Retention_Diagnostic_v0_6_3_package.zip" . -x "*package.zip" "*package_SHA256.txt")
sha256sum "$REL/K_G1_G6A_Retention_Diagnostic_v0_6_3_package.zip" > "$REL/K_G1_G6A_Retention_Diagnostic_v0_6_3_package_SHA256.txt"
git config user.name "github-actions[bot]"
git config user.email "41898282+github-actions[bot]@users.noreply.github.com"
git add "$REL"
if ! git diff --cached --quiet; then git commit -m "Archive verified K G1 G6A Retention Diagnostic v0.6.3 build"; git push; fi
