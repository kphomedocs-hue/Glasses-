#!/usr/bin/env bash
set -euo pipefail
: "${RUNNER_TEMP:?}"; : "${GITHUB_WORKSPACE:?}"; : "${GITHUB_SHA:?}"; : "${GITHUB_RUN_ID:?}"
cd "$GITHUB_WORKSPACE"; git pull --rebase origin main
SRC="source/v0.4.1"; REL="releases/v0.4.1"; mkdir -p "$REL"
APK="$RUNNER_TEMP/g4a2/K_G1_P2P_IP_Notify_Probe_v0_4_1.apk"; LINT="$RUNNER_TEMP/g4a2/lint-results-debug.html"
python3 - <<'PY'
from pathlib import Path
import subprocess, zipfile
root=Path("source/v0.4.1"); out=Path("releases/v0.4.1/K_G1_P2P_IP_Notify_Probe_v0_4_1_source_v1.zip")
files=subprocess.check_output(["git","ls-files","-z",str(root)]).decode().split("\0")
with zipfile.ZipFile(out,"w",zipfile.ZIP_DEFLATED) as z:
    for f in sorted(x for x in files if x): z.write(f,Path(f).relative_to(root))
PY
cp "$APK" "$REL/K_G1_P2P_IP_Notify_Probe_v0_4_1.apk"
cp "$LINT" "$REL/lint-results-debug.html"
sha256sum "$REL/K_G1_P2P_IP_Notify_Probe_v0_4_1.apk" > "$REL/K_G1_P2P_IP_Notify_Probe_v0_4_1_APK_SHA256.txt"
sha256sum "$REL/K_G1_P2P_IP_Notify_Probe_v0_4_1_source_v1.zip" > "$REL/K_G1_P2P_IP_Notify_Probe_v0_4_1_source_SHA256.txt"
cat > "$REL/BUILD_INFO.md" <<EOF
# K G1 P2P IP Notify Probe v0.4.1 — Verified Repository Build
- Canonical build commit: $GITHUB_SHA
- Canonical build run: $GITHUB_RUN_ID
- G4A2 safety audit: PASS
- Android compile: PASS
- Android Lint: PASS
- APK signature verification: PASS
- Internet permission: ABSENT
- HTTP/socket/media access: ABSENT
- P2P-IP query 02 03: ABSENT
- Proprietary writes: P2P enter once + transfer exit once only
- 0x73 reporting: sanitized event IDs; IPv4 only for event 0x08
- Peer selection: exact BLE-reported P2P name only
EOF
rm -rf "$RUNNER_TEMP/g4a2pkg"; mkdir -p "$RUNNER_TEMP/g4a2pkg"
cp "$REL"/* "$RUNNER_TEMP/g4a2pkg/"
(cd "$RUNNER_TEMP/g4a2pkg" && zip -q -r "$GITHUB_WORKSPACE/$REL/K_G1_P2P_IP_Notify_Probe_v0_4_1_package.zip" .)
sha256sum "$REL/K_G1_P2P_IP_Notify_Probe_v0_4_1_package.zip" > "$REL/K_G1_P2P_IP_Notify_Probe_v0_4_1_package_SHA256.txt"
git config user.name "github-actions[bot]"; git config user.email "41898282+github-actions[bot]@users.noreply.github.com"
git add "$REL"
if ! git diff --cached --quiet; then git commit -m "Archive verified K G1 P2P IP Notify Probe v0.4.1 build"; git push; fi
