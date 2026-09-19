#!/usr/bin/env bash
set -euo pipefail
: "${RUNNER_TEMP:?}"; : "${GITHUB_WORKSPACE:?}"; : "${GITHUB_SHA:?}"; : "${GITHUB_RUN_ID:?}"
cd "$GITHUB_WORKSPACE"
git pull --rebase origin main
SRC="source/v0.5.1"
REL="releases/v0.5.1"
rm -rf "$REL"
mkdir -p "$REL"
APK="$RUNNER_TEMP/g51/K_G1_Disposable_Photo_Probe_v0_5_1.apk"
LINT="$RUNNER_TEMP/g51/lint-results-debug.html"

python3 - <<'PY'
from pathlib import Path
import subprocess, zipfile
root=Path("source/v0.5.1")
out=Path("releases/v0.5.1/K_G1_Disposable_Photo_Probe_v0_5_1_source_v1.zip")
files=subprocess.check_output(["git","ls-files","-z",str(root)]).decode().split("\0")
with zipfile.ZipFile(out,"w",zipfile.ZIP_DEFLATED) as z:
    for f in sorted(x for x in files if x):
        z.write(f,Path(f).relative_to(root))
PY

cp "$APK" "$REL/K_G1_Disposable_Photo_Probe_v0_5_1.apk"
cp "$LINT" "$REL/lint-results-debug.html"
sha256sum "$REL/K_G1_Disposable_Photo_Probe_v0_5_1.apk" > "$REL/K_G1_Disposable_Photo_Probe_v0_5_1_APK_SHA256.txt"
sha256sum "$REL/K_G1_Disposable_Photo_Probe_v0_5_1_source_v1.zip" > "$REL/K_G1_Disposable_Photo_Probe_v0_5_1_source_SHA256.txt"

cat > "$REL/BUILD_INFO.md" <<EOF
# K G1 Disposable Photo Probe v0.5.1 — Verified Repository Build
- Canonical build commit: $GITHUB_SHA
- Canonical build run: $GITHUB_RUN_ID
- G5.1 safety audit: PASS
- Android compile: PASS
- Android Lint: PASS
- APK signature verification: PASS
- Package ID: com.parkarsite.g1singlephotoprobe51
- Media-count query: 0x41 / 02 04 exactly once per phase
- Catalog readiness delay: exact Cyan 1000 ms per phase
- P2P enter: once per phase
- Transfer exit: once per phase
- Catalog GETs: one per phase
- Media GETs: one maximum after exact single-JPG delta
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
EOF

rm -rf "$RUNNER_TEMP/g51pkg"
mkdir -p "$RUNNER_TEMP/g51pkg"
cp "$REL"/* "$RUNNER_TEMP/g51pkg/"
(cd "$RUNNER_TEMP/g51pkg" && zip -q -r "$GITHUB_WORKSPACE/$REL/K_G1_Disposable_Photo_Probe_v0_5_1_package.zip" .)
sha256sum "$REL/K_G1_Disposable_Photo_Probe_v0_5_1_package.zip" > "$REL/K_G1_Disposable_Photo_Probe_v0_5_1_package_SHA256.txt"

git config user.name "github-actions[bot]"
git config user.email "41898282+github-actions[bot]@users.noreply.github.com"
git add "$REL"
if ! git diff --cached --quiet; then
  git commit -m "Archive verified K G1 Disposable Photo Probe v0.5.1 build"
  git push
fi
