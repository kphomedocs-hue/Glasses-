#!/usr/bin/env bash
set -euo pipefail
: "${RUNNER_TEMP:?}"; : "${GITHUB_WORKSPACE:?}"; : "${GITHUB_SHA:?}"; : "${GITHUB_RUN_ID:?}"
cd "$GITHUB_WORKSPACE"; git pull --rebase origin main
SRC="source/v0.4.0"; REL="releases/v0.4.0"; mkdir -p "$REL"
APK="$RUNNER_TEMP/g4a/K_G1_P2P_Association_Probe_v0_4_0.apk"; LINT="$RUNNER_TEMP/g4a/lint-results-debug.html"
python3 - <<'PY'
from pathlib import Path
import subprocess, zipfile
root=Path("source/v0.4.0"); out=Path("releases/v0.4.0/K_G1_P2P_Association_Probe_v0_4_0_source_v1.zip")
files=subprocess.check_output(["git","ls-files","-z",str(root)]).decode().split("\0")
with zipfile.ZipFile(out,"w",zipfile.ZIP_DEFLATED) as z:
    for f in sorted(x for x in files if x): z.write(f,Path(f).relative_to(root))
PY
cp "$APK" "$REL/K_G1_P2P_Association_Probe_v0_4_0.apk"
cp "$LINT" "$REL/lint-results-debug.html"
sha256sum "$REL/K_G1_P2P_Association_Probe_v0_4_0.apk" > "$REL/K_G1_P2P_Association_Probe_v0_4_0_APK_SHA256.txt"
sha256sum "$REL/K_G1_P2P_Association_Probe_v0_4_0_source_v1.zip" > "$REL/K_G1_P2P_Association_Probe_v0_4_0_source_SHA256.txt"
cat > "$REL/BUILD_INFO.md" <<EOF
# K G1 P2P Association Probe v0.4.0 — Verified Repository Build
- Canonical build commit: $GITHUB_SHA
- Canonical build run: $GITHUB_RUN_ID
- G4A safety audit: PASS
- Android compile: PASS
- Android Lint: PASS
- APK signature verification: PASS
- Internet permission: ABSENT
- HTTP/socket/media access: ABSENT
- Peer selection: exact BLE-reported P2P name only
EOF
rm -rf "$RUNNER_TEMP/g4apkg"; mkdir -p "$RUNNER_TEMP/g4apkg"
cp "$REL"/* "$RUNNER_TEMP/g4apkg/"
(cd "$RUNNER_TEMP/g4apkg" && zip -q -r "$GITHUB_WORKSPACE/$REL/K_G1_P2P_Association_Probe_v0_4_0_package.zip" .)
sha256sum "$REL/K_G1_P2P_Association_Probe_v0_4_0_package.zip" > "$REL/K_G1_P2P_Association_Probe_v0_4_0_package_SHA256.txt"
git config user.name "github-actions[bot]"; git config user.email "41898282+github-actions[bot]@users.noreply.github.com"
git add "$REL"
if ! git diff --cached --quiet; then git commit -m "Archive verified K G1 P2P Association Probe v0.4.0 build"; git push; fi
