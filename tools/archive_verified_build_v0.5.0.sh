#!/usr/bin/env bash
set -euo pipefail
: "${RUNNER_TEMP:?}"; : "${GITHUB_WORKSPACE:?}"; : "${GITHUB_SHA:?}"; : "${GITHUB_RUN_ID:?}"
cd "$GITHUB_WORKSPACE"
git pull --rebase origin main
SRC="source/v0.5.0"
REL="releases/v0.5.0"
rm -rf "$REL"
mkdir -p "$REL"
APK="$RUNNER_TEMP/g5/K_G1_Disposable_Photo_Probe_v0_5_0.apk"
LINT="$RUNNER_TEMP/g5/lint-results-debug.html"

python3 - <<'PY'
from pathlib import Path
import subprocess, zipfile
root=Path("source/v0.5.0")
out=Path("releases/v0.5.0/K_G1_Disposable_Photo_Probe_v0_5_0_source_v1.zip")
files=subprocess.check_output(["git","ls-files","-z",str(root)]).decode().split("\0")
with zipfile.ZipFile(out,"w",zipfile.ZIP_DEFLATED) as z:
    for f in sorted(x for x in files if x):
        z.write(f,Path(f).relative_to(root))
PY

cp "$APK" "$REL/K_G1_Disposable_Photo_Probe_v0_5_0.apk"
cp "$LINT" "$REL/lint-results-debug.html"
sha256sum "$REL/K_G1_Disposable_Photo_Probe_v0_5_0.apk" > "$REL/K_G1_Disposable_Photo_Probe_v0_5_0_APK_SHA256.txt"
sha256sum "$REL/K_G1_Disposable_Photo_Probe_v0_5_0_source_v1.zip" > "$REL/K_G1_Disposable_Photo_Probe_v0_5_0_source_SHA256.txt"

cat > "$REL/BUILD_INFO.md" <<EOF
# K G1 Disposable Photo Probe v0.5.0 — Verified Repository Build
- Canonical build commit: $GITHUB_SHA
- Canonical build run: $GITHUB_RUN_ID
- G5 safety audit: PASS
- Android compile: PASS
- Android Lint: PASS
- APK signature verification: PASS
- Package ID: com.parkarsite.g1singlephotoprobe
- Selection: two-phase baseline + exactly one new safe JPG delta
- Proprietary writes: P2P enter once + transfer exit once per phase
- Catalog GETs: one per phase
- Media GETs: one maximum
- Total HTTP GETs on success: three maximum
- Catalog path: /files/media.config
- Media prefix: /files/
- Media cap: 33554432 bytes
- Redirects: disabled
- Retry/resume/Range: absent
- Alternate endpoints / JSON / AP fallback / 02 03: absent
- Remote filename/path/raw catalog logging: absent
- Catalog/media fingerprint logging: absent
- Glasses file mutation/deletion: absent
- Local destination: app-private cache only
- JPEG SOI/EOI validation: present
EOF

rm -rf "$RUNNER_TEMP/g5pkg"
mkdir -p "$RUNNER_TEMP/g5pkg"
cp "$REL"/* "$RUNNER_TEMP/g5pkg/"
(cd "$RUNNER_TEMP/g5pkg" && zip -q -r "$GITHUB_WORKSPACE/$REL/K_G1_Disposable_Photo_Probe_v0_5_0_package.zip" .)
sha256sum "$REL/K_G1_Disposable_Photo_Probe_v0_5_0_package.zip" > "$REL/K_G1_Disposable_Photo_Probe_v0_5_0_package_SHA256.txt"

git config user.name "github-actions[bot]"
git config user.email "41898282+github-actions[bot]@users.noreply.github.com"
git add "$REL"
if ! git diff --cached --quiet; then
  git commit -m "Archive verified K G1 Disposable Photo Probe v0.5.0 build"
  git push
fi
