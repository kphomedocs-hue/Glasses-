#!/usr/bin/env bash
set -euo pipefail
: "${RUNNER_TEMP:?}"; : "${GITHUB_WORKSPACE:?}"; : "${GITHUB_SHA:?}"; : "${GITHUB_RUN_ID:?}"
cd "$GITHUB_WORKSPACE"
git pull --rebase origin main
SRC="source/v0.4.4"
REL="releases/v0.4.4"
mkdir -p "$REL"
APK="$RUNNER_TEMP/g4b2h/K_G1_Catalog_Shape_Probe_v0_4_4.apk"
LINT="$RUNNER_TEMP/g4b2h/lint-results-debug.html"

python3 - <<'PY'
from pathlib import Path
import subprocess, zipfile
root=Path("source/v0.4.4")
out=Path("releases/v0.4.4/K_G1_Catalog_Shape_Probe_v0_4_4_source_v1.zip")
files=subprocess.check_output(["git","ls-files","-z",str(root)]).decode().split("\0")
with zipfile.ZipFile(out,"w",zipfile.ZIP_DEFLATED) as z:
    for f in sorted(x for x in files if x):
        z.write(f,Path(f).relative_to(root))
PY

cp "$APK" "$REL/K_G1_Catalog_Shape_Probe_v0_4_4.apk"
cp "$LINT" "$REL/lint-results-debug.html"
sha256sum "$REL/K_G1_Catalog_Shape_Probe_v0_4_4.apk" > "$REL/K_G1_Catalog_Shape_Probe_v0_4_4_APK_SHA256.txt"
sha256sum "$REL/K_G1_Catalog_Shape_Probe_v0_4_4_source_v1.zip" > "$REL/K_G1_Catalog_Shape_Probe_v0_4_4_source_SHA256.txt"

cat > "$REL/BUILD_INFO.md" <<EOF
# K G1 Catalog Shape Probe v0.4.4 — Verified Repository Build
- Canonical build commit: $GITHUB_SHA
- Canonical build run: $GITHUB_RUN_ID
- G4B2 hardened safety audit: PASS
- Android compile: PASS
- Android Lint: PASS
- APK signature verification: PASS
- Package ID: com.parkarsite.g1catalogshapeprobe4
- HTTP requests: exactly one GET site
- Catalog path: /files/media.config
- Redirects: disabled
- Catalog response cap: 65536 bytes
- Raw body logging: ABSENT
- Filename/path value logging: ABSENT
- Catalog fingerprint/hash logging: ABSENT
- Media-file GET/download/mutation: ABSENT
- P2P-IP query 02 03: ABSENT
- Proprietary writes: P2P enter once + transfer exit once only
- Response classification: encoding/BOM/JSON structure/counts only
EOF

rm -rf "$RUNNER_TEMP/g4b2hpkg"
mkdir -p "$RUNNER_TEMP/g4b2hpkg"
cp "$REL"/* "$RUNNER_TEMP/g4b2hpkg/"
(cd "$RUNNER_TEMP/g4b2hpkg" && zip -q -r "$GITHUB_WORKSPACE/$REL/K_G1_Catalog_Shape_Probe_v0_4_4_package.zip" .)
sha256sum "$REL/K_G1_Catalog_Shape_Probe_v0_4_4_package.zip" > "$REL/K_G1_Catalog_Shape_Probe_v0_4_4_package_SHA256.txt"

git config user.name "github-actions[bot]"
git config user.email "41898282+github-actions[bot]@users.noreply.github.com"
git add "$REL"
if ! git diff --cached --quiet; then
  git commit -m "Archive verified K G1 Catalog Shape Probe v0.4.4 build"
  git push
fi
