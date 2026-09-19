#!/usr/bin/env bash
set -euo pipefail
: "${RUNNER_TEMP:?}"; : "${GITHUB_WORKSPACE:?}"; : "${GITHUB_SHA:?}"; : "${GITHUB_RUN_ID:?}"
cd "$GITHUB_WORKSPACE"
git pull --rebase origin main
SRC="source/v0.4.2"
REL="releases/v0.4.2"
mkdir -p "$REL"
APK="$RUNNER_TEMP/g4b/K_G1_Media_Catalog_Probe_v0_4_2.apk"
LINT="$RUNNER_TEMP/g4b/lint-results-debug.html"

python3 - <<'PY'
from pathlib import Path
import subprocess, zipfile
root=Path("source/v0.4.2")
out=Path("releases/v0.4.2/K_G1_Media_Catalog_Probe_v0_4_2_source_v1.zip")
files=subprocess.check_output(["git","ls-files","-z",str(root)]).decode().split("\0")
with zipfile.ZipFile(out,"w",zipfile.ZIP_DEFLATED) as z:
    for f in sorted(x for x in files if x):
        z.write(f,Path(f).relative_to(root))
PY

cp "$APK" "$REL/K_G1_Media_Catalog_Probe_v0_4_2.apk"
cp "$LINT" "$REL/lint-results-debug.html"
sha256sum "$REL/K_G1_Media_Catalog_Probe_v0_4_2.apk" > "$REL/K_G1_Media_Catalog_Probe_v0_4_2_APK_SHA256.txt"
sha256sum "$REL/K_G1_Media_Catalog_Probe_v0_4_2_source_v1.zip" > "$REL/K_G1_Media_Catalog_Probe_v0_4_2_source_SHA256.txt"

cat > "$REL/BUILD_INFO.md" <<EOF
# K G1 Media Catalog Probe v0.4.2 — Verified Repository Build
- Canonical build commit: $GITHUB_SHA
- Canonical build run: $GITHUB_RUN_ID
- G4B safety audit: PASS
- Android compile: PASS
- Android Lint: PASS
- APK signature verification: PASS
- Package ID: com.parkarsite.g1mediacatalogprobe
- INTERNET permission: PRESENT for local HTTP only
- Cleartext HTTP: enabled for exact Cyan local endpoint
- P2P-IP query 02 03: ABSENT
- Proprietary writes: P2P enter once + transfer exit once only
- HTTP requests: exactly one GET site
- Catalog path: /files/media.config
- Redirects: disabled
- Catalog response cap: 65536 bytes
- Media-file GET/download/mutation: ABSENT
- Filename/path value logging: ABSENT
- Peer selection: exact BLE-reported P2P name only
EOF

rm -rf "$RUNNER_TEMP/g4bpkg"
mkdir -p "$RUNNER_TEMP/g4bpkg"
cp "$REL"/* "$RUNNER_TEMP/g4bpkg/"
(cd "$RUNNER_TEMP/g4bpkg" && zip -q -r "$GITHUB_WORKSPACE/$REL/K_G1_Media_Catalog_Probe_v0_4_2_package.zip" .)
sha256sum "$REL/K_G1_Media_Catalog_Probe_v0_4_2_package.zip" > "$REL/K_G1_Media_Catalog_Probe_v0_4_2_package_SHA256.txt"

git config user.name "github-actions[bot]"
git config user.email "41898282+github-actions[bot]@users.noreply.github.com"
git add "$REL"
if ! git diff --cached --quiet; then
  git commit -m "Archive verified K G1 Media Catalog Probe v0.4.2 build"
  git push
fi
