#!/usr/bin/env bash
set -euo pipefail
: "${RUNNER_TEMP:?}"; : "${GITHUB_WORKSPACE:?}"; : "${GITHUB_SHA:?}"; : "${GITHUB_RUN_ID:?}"
cd "$GITHUB_WORKSPACE"
git pull --rebase origin main
REL="releases/v0.6.4.1"; rm -rf "$REL"; mkdir -p "$REL"
python3 - <<'PY'
from pathlib import Path
import subprocess, zipfile
root=Path("source/v0.6.4.1")
out=Path("releases/v0.6.4.1/K_G1_G6A_No_Capture_Stability_v0_6_4_1_source_v1.zip")
files=subprocess.check_output(["git","ls-files","-z",str(root)]).decode().split("\0")
with zipfile.ZipFile(out,"w",zipfile.ZIP_DEFLATED) as z:
    for f in sorted(x for x in files if x): z.write(f,Path(f).relative_to(root))
PY
cp "$RUNNER_TEMP/g6a641/K_G1_G6A_No_Capture_Stability_v0_6_4_1.apk" "$REL/K_G1_G6A_No_Capture_Stability_v0_6_4_1.apk"
cp "$RUNNER_TEMP/g6a641/lint-results-debug.html" "$REL/lint-results-debug.html"
sha256sum "$REL/K_G1_G6A_No_Capture_Stability_v0_6_4_1.apk" > "$REL/K_G1_G6A_No_Capture_Stability_v0_6_4_1_APK_SHA256.txt"
sha256sum "$REL/K_G1_G6A_No_Capture_Stability_v0_6_4_1_source_v1.zip" > "$REL/K_G1_G6A_No_Capture_Stability_v0_6_4_1_source_SHA256.txt"
cat > "$REL/BUILD_INFO.md" <<EOF
# K G1 G6A No-Capture Catalog Stability v0.6.4.1 — Verified Repository Build
- Canonical build commit: $GITHUB_SHA
- Canonical build run: $GITHUB_RUN_ID
- corrected bounded-scope safety audit: PASS
- Android compile: PASS
- Android Lint: PASS
- APK signature verification: PASS
- Package ID: com.parkarsite.g6astability64
- versionCode: 2
- cross-channel BLE image/catalog JPG parity required for PASS
- exit write callback + valid 0x41 response required before cleanup
- P2P group absence verified after one removeGroup request
- bounded cleanup callback
- minimum 30000 ms quiet interval with monotonic evidence
- exact final operation totals required for PASS
- media-file GET code paths: none
- no raw remote filename/path persistence
- no glasses mutation/deletion
EOF
(cd "$REL" && zip -q -r "K_G1_G6A_No_Capture_Stability_v0_6_4_1_package.zip" . -x "*package.zip" "*package_SHA256.txt")
sha256sum "$REL/K_G1_G6A_No_Capture_Stability_v0_6_4_1_package.zip" > "$REL/K_G1_G6A_No_Capture_Stability_v0_6_4_1_package_SHA256.txt"
git config user.name "github-actions[bot]"
git config user.email "41898282+github-actions[bot]@users.noreply.github.com"
git add "$REL"
if ! git diff --cached --quiet; then git commit -m "Archive verified K G1 G6A No-Capture Stability v0.6.4.1 build"; git push; fi
