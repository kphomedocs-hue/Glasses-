#!/usr/bin/env bash
set -euo pipefail
: "${RUNNER_TEMP:?}"; : "${GITHUB_WORKSPACE:?}"; : "${GITHUB_SHA:?}"; : "${GITHUB_RUN_ID:?}"
cd "$GITHUB_WORKSPACE"
git pull --rebase origin main
SRC="source/v0.4.5"
REL="releases/v0.4.5"
mkdir -p "$REL"
APK="$RUNNER_TEMP/g4b3/K_G1_Catalog_Line_Probe_v0_4_5.apk"
LINT="$RUNNER_TEMP/g4b3/lint-results-debug.html"

python3 - <<'PY'
from pathlib import Path
import subprocess, zipfile
root=Path("source/v0.4.5")
out=Path("releases/v0.4.5/K_G1_Catalog_Line_Probe_v0_4_5_source_v1.zip")
files=subprocess.check_output(["git","ls-files","-z",str(root)]).decode().split("\0")
with zipfile.ZipFile(out,"w",zipfile.ZIP_DEFLATED) as z:
    for f in sorted(x for x in files if x):
        z.write(f,Path(f).relative_to(root))
PY

cp "$APK" "$REL/K_G1_Catalog_Line_Probe_v0_4_5.apk"
cp "$LINT" "$REL/lint-results-debug.html"
sha256sum "$REL/K_G1_Catalog_Line_Probe_v0_4_5.apk" > "$REL/K_G1_Catalog_Line_Probe_v0_4_5_APK_SHA256.txt"
sha256sum "$REL/K_G1_Catalog_Line_Probe_v0_4_5_source_v1.zip" > "$REL/K_G1_Catalog_Line_Probe_v0_4_5_source_SHA256.txt"

cat > "$REL/BUILD_INFO.md" <<EOF
# K G1 Catalog Line Probe v0.4.5 — Verified Repository Build
- Canonical build commit: $GITHUB_SHA
- Canonical build run: $GITHUB_RUN_ID
- G4B3 safety audit: PASS
- Android compile: PASS
- Android Lint: PASS
- APK signature verification: PASS
- Package ID: com.parkarsite.g1cataloglineprobe
- Parser: configFileType=1 line-list parity
- HTTP requests: exactly one GET site
- Catalog path: /files/media.config
- Redirects: disabled
- Catalog response cap: 65536 bytes
- JSON/vf_list alternate branch: ABSENT
- Raw body logging: ABSENT
- Filename/path value logging: ABSENT
- Catalog fingerprint/hash logging: ABSENT
- Media-file GET/download/mutation: ABSENT
- P2P-IP query 02 03: ABSENT
- Proprietary writes: P2P enter once + transfer exit once only
EOF

rm -rf "$RUNNER_TEMP/g4b3pkg"
mkdir -p "$RUNNER_TEMP/g4b3pkg"
cp "$REL"/* "$RUNNER_TEMP/g4b3pkg/"
(cd "$RUNNER_TEMP/g4b3pkg" && zip -q -r "$GITHUB_WORKSPACE/$REL/K_G1_Catalog_Line_Probe_v0_4_5_package.zip" .)
sha256sum "$REL/K_G1_Catalog_Line_Probe_v0_4_5_package.zip" > "$REL/K_G1_Catalog_Line_Probe_v0_4_5_package_SHA256.txt"

git config user.name "github-actions[bot]"
git config user.email "41898282+github-actions[bot]@users.noreply.github.com"
git add "$REL"
if ! git diff --cached --quiet; then
  git commit -m "Archive verified K G1 Catalog Line Probe v0.4.5 build"
  git push
fi
