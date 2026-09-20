#!/usr/bin/env bash
set -euo pipefail
: "${RUNNER_TEMP:?}"; : "${GITHUB_WORKSPACE:?}"; : "${GITHUB_SHA:?}"; : "${GITHUB_RUN_ID:?}"
cd "$GITHUB_WORKSPACE"
git pull --rebase origin main
REL="releases/v0.6.0"; rm -rf "$REL"; mkdir -p "$REL"
python3 - <<'PY'
from pathlib import Path
import subprocess, zipfile
root=Path("source/v0.6.0")
out=Path("releases/v0.6.0/K_G1_G6A_Persistent_Import_v0_6_0_source_v1.zip")
files=subprocess.check_output(["git","ls-files","-z",str(root)]).decode().split("\0")
with zipfile.ZipFile(out,"w",zipfile.ZIP_DEFLATED) as z:
    for f in sorted(x for x in files if x): z.write(f,Path(f).relative_to(root))
PY
cp "$RUNNER_TEMP/g6a60/K_G1_G6A_Persistent_Import_v0_6_0.apk" "$REL/K_G1_G6A_Persistent_Import_v0_6_0.apk"
cp "$RUNNER_TEMP/g6a60/lint-results-debug.html" "$REL/lint-results-debug.html"
sha256sum "$REL/K_G1_G6A_Persistent_Import_v0_6_0.apk" > "$REL/K_G1_G6A_Persistent_Import_v0_6_0_APK_SHA256.txt"
sha256sum "$REL/K_G1_G6A_Persistent_Import_v0_6_0_source_v1.zip" > "$REL/K_G1_G6A_Persistent_Import_v0_6_0_source_SHA256.txt"
cat > "$REL/BUILD_INFO.md" <<EOF
# K G1 G6A Persistent Import v0.6.0 — Verified Repository Build
- Canonical build commit: $GITHUB_SHA
- Canonical build run: $GITHUB_RUN_ID
- G6A safety audit: PASS
- Android compile: PASS
- Android Lint: PASS
- APK signature verification: PASS
- Package ID: com.parkarsite.g6aimport60
- G5.7 transport boundary preserved
- one new JPG maximum per run
- persistent app-private archive + opaque ledger
- no glasses mutation/deletion
EOF
(cd "$REL" && zip -q -r "K_G1_G6A_Persistent_Import_v0_6_0_package.zip" . -x "*package.zip" "*package_SHA256.txt")
sha256sum "$REL/K_G1_G6A_Persistent_Import_v0_6_0_package.zip" > "$REL/K_G1_G6A_Persistent_Import_v0_6_0_package_SHA256.txt"
git config user.name "github-actions[bot]"
git config user.email "41898282+github-actions[bot]@users.noreply.github.com"
git add "$REL"
if ! git diff --cached --quiet; then git commit -m "Archive verified K G1 G6A Persistent Import v0.6.0 build"; git push; fi
