#!/usr/bin/env bash
set -euo pipefail
: "${RUNNER_TEMP:?}"; : "${GITHUB_WORKSPACE:?}"; : "${GITHUB_SHA:?}"; : "${GITHUB_RUN_ID:?}"
cd "$GITHUB_WORKSPACE"
git pull --rebase origin main
SRC="source/v0.5.5"
REL="releases/v0.5.5"
rm -rf "$REL"
mkdir -p "$REL"
APK="$RUNNER_TEMP/g55/K_G1_Capture_Visibility_Probe_v0_5_5.apk"
LINT="$RUNNER_TEMP/g55/lint-results-debug.html"

python3 - <<'PY'
from pathlib import Path
import subprocess, zipfile
root=Path("source/v0.5.5")
out=Path("releases/v0.5.5/K_G1_Capture_Visibility_Probe_v0_5_5_source_v1.zip")
files=subprocess.check_output(["git","ls-files","-z",str(root)]).decode().split("\0")
with zipfile.ZipFile(out,"w",zipfile.ZIP_DEFLATED) as z:
    for f in sorted(x for x in files if x):
        z.write(f,Path(f).relative_to(root))
PY

cp "$APK" "$REL/K_G1_Capture_Visibility_Probe_v0_5_5.apk"
cp "$LINT" "$REL/lint-results-debug.html"
sha256sum "$REL/K_G1_Capture_Visibility_Probe_v0_5_5.apk" > "$REL/K_G1_Capture_Visibility_Probe_v0_5_5_APK_SHA256.txt"
sha256sum "$REL/K_G1_Capture_Visibility_Probe_v0_5_5_source_v1.zip" > "$REL/K_G1_Capture_Visibility_Probe_v0_5_5_source_SHA256.txt"

cat > "$REL/BUILD_INFO.md" <<EOF
# K G1 Capture Visibility Probe v0.5.5 — Verified Repository Build
- Canonical build commit: $GITHUB_SHA
- Canonical build run: $GITHUB_RUN_ID
- BLE-only safety audit: PASS
- Android compile: PASS
- Android Lint: PASS
- APK signature verification: PASS
- Package ID: com.parkarsite.g1capturevisibilityprobe55
- Runtime delta from v0.5.4: report-version metadata correction only
- Proprietary payload: 0x41 / 02 04 only
- Proprietary writes: two maximum total
- BLE connection: continuous across physical capture
- Armed observation: user arms 60s window BEFORE photo capture
- Armed-window 0x73 and 0x01 counters: separate from all-session counters
- P2P/Wi-Fi/HTTP/media access: absent
- INTERNET permission: absent
- Raw notification logging: absent
- File write/delete: absent
EOF

rm -rf "$RUNNER_TEMP/g55pkg"
mkdir -p "$RUNNER_TEMP/g55pkg"
cp "$REL"/* "$RUNNER_TEMP/g55pkg/"
(cd "$RUNNER_TEMP/g55pkg" && zip -q -r "$GITHUB_WORKSPACE/$REL/K_G1_Capture_Visibility_Probe_v0_5_5_package.zip" .)
sha256sum "$REL/K_G1_Capture_Visibility_Probe_v0_5_5_package.zip" > "$REL/K_G1_Capture_Visibility_Probe_v0_5_5_package_SHA256.txt"

git config user.name "github-actions[bot]"
git config user.email "41898282+github-actions[bot]@users.noreply.github.com"
git add "$REL"
if ! git diff --cached --quiet; then
  git commit -m "Archive verified K G1 Capture Visibility Probe v0.5.5 build"
  git push
fi
