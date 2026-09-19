#!/usr/bin/env bash
set -euo pipefail
: "${RUNNER_TEMP:?}"; : "${GITHUB_WORKSPACE:?}"; : "${GITHUB_SHA:?}"; : "${GITHUB_RUN_ID:?}"
cd "$GITHUB_WORKSPACE"
git pull --rebase origin main
REL="releases/v0.5.6"
rm -rf "$REL"; mkdir -p "$REL"
python3 - <<'PY'
from pathlib import Path
import subprocess, zipfile
root=Path("source/v0.5.6")
out=Path("releases/v0.5.6/K_G1_Disposable_Photo_Probe_v0_5_6_source_v1.zip")
files=subprocess.check_output(["git","ls-files","-z",str(root)]).decode().split("\0")
with zipfile.ZipFile(out,"w",zipfile.ZIP_DEFLATED) as z:
    for f in sorted(x for x in files if x): z.write(f,Path(f).relative_to(root))
PY
cp "$RUNNER_TEMP/g56/K_G1_Disposable_Photo_Probe_v0_5_6.apk" "$REL/K_G1_Disposable_Photo_Probe_v0_5_6.apk"
cp "$RUNNER_TEMP/g56/lint-results-debug.html" "$REL/lint-results-debug.html"
sha256sum "$REL/K_G1_Disposable_Photo_Probe_v0_5_6.apk" > "$REL/K_G1_Disposable_Photo_Probe_v0_5_6_APK_SHA256.txt"
sha256sum "$REL/K_G1_Disposable_Photo_Probe_v0_5_6_source_v1.zip" > "$REL/K_G1_Disposable_Photo_Probe_v0_5_6_source_SHA256.txt"
cat > "$REL/BUILD_INFO.md" <<EOF
# K G1 Disposable Photo Probe v0.5.6 — Verified Repository Build
- Canonical build commit: $GITHUB_SHA
- Canonical build run: $GITHUB_RUN_ID
- Visibility-gated safety audit: PASS
- Android compile: PASS
- Android Lint: PASS
- APK signature verification: PASS
- Package ID: com.parkarsite.g1singlephotoprobe56
- Phase-B P2P blocked until exact +1 image inventory confirmation
- 60-second visibility watch
- one media GET maximum after exact single-JPG catalog delta
EOF
(cd "$REL" && zip -q -r "K_G1_Disposable_Photo_Probe_v0_5_6_package.zip" . -x "*package.zip" "*package_SHA256.txt")
sha256sum "$REL/K_G1_Disposable_Photo_Probe_v0_5_6_package.zip" > "$REL/K_G1_Disposable_Photo_Probe_v0_5_6_package_SHA256.txt"
git config user.name "github-actions[bot]"
git config user.email "41898282+github-actions[bot]@users.noreply.github.com"
git add "$REL"
if ! git diff --cached --quiet; then git commit -m "Archive verified K G1 Disposable Photo Probe v0.5.6 build"; git push; fi
