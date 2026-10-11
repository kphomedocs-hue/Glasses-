#!/usr/bin/env bash
set -euo pipefail
: "${RUNNER_TEMP:?}"
: "${GITHUB_WORKSPACE:?}"
: "${GITHUB_SHA:?}"
: "${GITHUB_RUN_ID:?}"
: "${GITHUB_RUN_ATTEMPT:?}"
cd "$GITHUB_WORKSPACE"
STAGE="$RUNNER_TEMP/g6a686_exact_bundle"
rm -rf "$STAGE"
mkdir -p "$STAGE/reports" "$STAGE/reviews"
APK="K_G1_G6A_Passive_Peer_Identity_Diagnostic_v0_6_8_6.apk"
SRCZIP="K_G1_G6A_Passive_Peer_Identity_Diagnostic_v0_6_8_6_source_exact.zip"
PKGZIP="K_G1_G6A_Passive_Peer_Identity_Diagnostic_v0_6_8_6_package.zip"
cp "$RUNNER_TEMP/g6a686/$APK" "$STAGE/$APK"
cp "$RUNNER_TEMP/g6a686/lint-results-debug.html" "$STAGE/lint-results-debug.html"
git cat-file -e "$GITHUB_SHA^{commit}"
git archive --format=zip --output="$STAGE/$SRCZIP" "$GITHUB_SHA" source/v0.6.8.6
(
  cd "$STAGE"
  sha256sum "$APK" > K_G1_G6A_Passive_Peer_Identity_Diagnostic_v0_6_8_6_APK_SHA256.txt
  sha256sum "$SRCZIP" > K_G1_G6A_Passive_Peer_Identity_Diagnostic_v0_6_8_6_source_SHA256.txt
)
APK_SHA="$(cut -d' ' -f1 "$STAGE/K_G1_G6A_Passive_Peer_Identity_Diagnostic_v0_6_8_6_APK_SHA256.txt")"
SRC_SHA="$(cut -d' ' -f1 "$STAGE/K_G1_G6A_Passive_Peer_Identity_Diagnostic_v0_6_8_6_source_SHA256.txt")"
cat > "$STAGE/BUILD_INFO.md" <<EOF2
# K G1 G6A Passive Peer Identity Diagnostic v0.6.8.6 — Exact Build
- Build commit: $GITHUB_SHA
- Build run: $GITHUB_RUN_ID
- Build attempt: $GITHUB_RUN_ATTEMPT
- Package ID: com.parkarsite.g6apeerdiag686
- versionCode: 1
- APK SHA-256: $APK_SHA
- Exact source ZIP SHA-256: $SRC_SHA
- Safety audit: PASS
- Red-team static audit: PASS
- Android compile: PASS
- Android Lint: PASS
- APK signature verification: PASS
- physical capture code: NONE
- Wi-Fi Direct discovery and read-only peer/group inspection: PRESENT; association code: ABSENT
- HTTP/catalog/media code: NONE
- persistent import/receipt code: NONE
- discovery starts before ENTER; no time-sync or inventory command
- credential values decoded/logged/persisted: NO
- ENTER writes allowed: exactly one; EXIT writes allowed: exactly one
- discovery bound: 16000 ms; connection bound: 15000 ms
- credential parsing/logging: NONE\n- WifiP2pManager connect, cancelConnect, removeGroup: NONE
- research-only build: NOT AUTHORIZED for physical use; G6A OPEN / G6B BLOCKED
EOF2
cat > "$STAGE/reports/README.md" <<EOF2
# Physical reports for exact v0.6.8.6 build
Only reports whose exported header matches:
- App version: 0.6.8.6
- Build commit: $GITHUB_SHA
- Build run: $GITHUB_RUN_ID
- Build attempt: $GITHUB_RUN_ATTEMPT
belong here.
EOF2
cat > "$STAGE/reviews/STATIC_AUDIT.md" <<EOF2
# Pre-physical static review
- Safety audit: PASS
- Red-team static audit: PASS
- Compile/lint: PASS
- APK signature verification: PASS
- bounded passive Wi-Fi Direct discovery/peer classification; NO connect/cancel/remove; no HTTP, media or receipt code
A separate manual state-machine/provenance review and explicit physical-test authorization are required.
EOF2
(
  cd "$STAGE"
  zip -q "$PKGZIP" "$APK" K_G1_G6A_Passive_Peer_Identity_Diagnostic_v0_6_8_6_APK_SHA256.txt "$SRCZIP" K_G1_G6A_Passive_Peer_Identity_Diagnostic_v0_6_8_6_source_SHA256.txt BUILD_INFO.md lint-results-debug.html reports/README.md reviews/STATIC_AUDIT.md
  sha256sum "$PKGZIP" > K_G1_G6A_Passive_Peer_Identity_Diagnostic_v0_6_8_6_package_SHA256.txt
)
git fetch origin main
git checkout -B main origin/main
DEST="releases/v0.6.8.6/builds/run-${GITHUB_RUN_ID}-attempt-${GITHUB_RUN_ATTEMPT}"
if [ -e "$DEST" ]; then echo "Refusing overwrite: $DEST"; exit 1; fi
mkdir -p "$DEST"
cp -R "$STAGE"/. "$DEST"/
MANIFEST="releases/v0.6.8.6/VERSION_MANIFEST.md"
if [ ! -f "$MANIFEST" ]; then
  mkdir -p "$(dirname "$MANIFEST")"
  cat > "$MANIFEST" <<'EOF2'
# v0.6.8.6 Version Manifest

- Version: v0.6.8.6
- Build role: bounded passive peer-category and early BLE event-ID diagnostic (NO CONNECT)
- Package ID: `com.parkarsite.g6apeerdiag686`
- Gate role: DIAGNOSTIC ONLY; cannot close G6A or unblock G6B
- Authoritative artifacts live under immutable `builds/run-<id>-attempt-<n>/` directories.

## Build instances
EOF2
fi
printf '\n### run-%s-attempt-%s\n- exact bundle: `builds/run-%s-attempt-%s/`\n- build commit: `%s`\n- APK SHA-256: `%s`\n- exact source ZIP SHA-256: `%s`\n- CI/static status: verified\n- physical status: NOT AUTHORIZED; not run\n'  "$GITHUB_RUN_ID" "$GITHUB_RUN_ATTEMPT" "$GITHUB_RUN_ID" "$GITHUB_RUN_ATTEMPT" "$GITHUB_SHA" "$APK_SHA" "$SRC_SHA" >> "$MANIFEST"
git config user.name "github-actions[bot]"
git config user.email "41898282+github-actions[bot]@users.noreply.github.com"
git add "releases/v0.6.8.6"
git commit -m "Archive immutable K G1 G6A Cyan P2P state diagnostic v0.6.8.6 build"
git push origin main
