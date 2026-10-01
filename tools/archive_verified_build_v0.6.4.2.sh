#!/usr/bin/env bash
set -euo pipefail
: "${RUNNER_TEMP:?}"
: "${GITHUB_WORKSPACE:?}"
: "${GITHUB_SHA:?}"
: "${GITHUB_RUN_ID:?}"
: "${GITHUB_RUN_ATTEMPT:?}"

cd "$GITHUB_WORKSPACE"
STAGE="$RUNNER_TEMP/g6a642_exact_bundle"
rm -rf "$STAGE"
mkdir -p "$STAGE/reports" "$STAGE/reviews"

APK="K_G1_G6A_No_Capture_Stability_v0_6_4_2.apk"
SRCZIP="K_G1_G6A_No_Capture_Stability_v0_6_4_2_source_exact.zip"
PKGZIP="K_G1_G6A_No_Capture_Stability_v0_6_4_2_package.zip"

cp "$RUNNER_TEMP/g6a642/$APK" "$STAGE/$APK"
cp "$RUNNER_TEMP/g6a642/lint-results-debug.html" "$STAGE/lint-results-debug.html"

# Exact source provenance: archive directly from the immutable build commit.
git cat-file -e "$GITHUB_SHA^{commit}"
git archive --format=zip --output="$STAGE/$SRCZIP" "$GITHUB_SHA" source/v0.6.4.2

(
  cd "$STAGE"
  sha256sum "$APK" > K_G1_G6A_No_Capture_Stability_v0_6_4_2_APK_SHA256.txt
  sha256sum "$SRCZIP" > K_G1_G6A_No_Capture_Stability_v0_6_4_2_source_SHA256.txt
)

APK_SHA="$(cut -d' ' -f1 "$STAGE/K_G1_G6A_No_Capture_Stability_v0_6_4_2_APK_SHA256.txt")"
SRC_SHA="$(cut -d' ' -f1 "$STAGE/K_G1_G6A_No_Capture_Stability_v0_6_4_2_source_SHA256.txt")"

cat > "$STAGE/BUILD_INFO.md" <<EOF
# K G1 G6A No-Capture Catalog Stability v0.6.4.2 — Exact Build
- Build commit: $GITHUB_SHA
- Build run: $GITHUB_RUN_ID
- Build attempt: $GITHUB_RUN_ATTEMPT
- Package ID: com.parkarsite.g6astability642
- versionCode: 1
- APK SHA-256: $APK_SHA
- Exact source ZIP SHA-256: $SRC_SHA
- Source archive command: git archive from exact GITHUB_SHA
- Safety audit: PASS
- Red-team static audit: PASS
- Android compile: PASS
- Android Lint: PASS
- APK signature verification: PASS
- P2P enter handoff: Android write callback + valid credential response required\n- media-file GET code paths: none
- post-exit confirmation: matching 0x73/0x01 after exit write callback
- exact case-sensitive BLE-reported P2P peer name only
- no per-file identity/hash tokens in report
- no raw remote filename/path persistence
- no glasses mutation/deletion
EOF

cat > "$STAGE/reports/README.md" <<EOF
# Physical reports for exact build

Only physical reports whose exported header identifies:
- Build commit: $GITHUB_SHA
- Build run: $GITHUB_RUN_ID
- Build attempt: $GITHUB_RUN_ATTEMPT

belong in this folder.
EOF

cat > "$STAGE/reviews/STATIC_AUDIT.md" <<EOF
# Pre-physical static review

- Safety audit: PASS
- Red-team static audit: PASS
- Compile/lint: PASS
- APK signature verification: PASS

A separate manual state-machine red-team review is still required before physical promotion.
EOF

(
  cd "$STAGE"
  zip -q "$PKGZIP" "$APK" K_G1_G6A_No_Capture_Stability_v0_6_4_2_APK_SHA256.txt "$SRCZIP" K_G1_G6A_No_Capture_Stability_v0_6_4_2_source_SHA256.txt BUILD_INFO.md lint-results-debug.html reports/README.md reviews/STATIC_AUDIT.md
  sha256sum "$PKGZIP" > K_G1_G6A_No_Capture_Stability_v0_6_4_2_package_SHA256.txt
)

# Only after all exact-build artifacts are staged outside the repo do we move to current main.
git fetch origin main
git checkout -B main origin/main

DEST="releases/v0.6.4.2/builds/run-${GITHUB_RUN_ID}-attempt-${GITHUB_RUN_ATTEMPT}"
if [ -e "$DEST" ]; then
  echo "Refusing to overwrite immutable build bundle: $DEST"
  exit 1
fi
mkdir -p "$DEST"
cp -R "$STAGE"/. "$DEST"/

MANIFEST="releases/v0.6.4.2/VERSION_MANIFEST.md"
if [ ! -f "$MANIFEST" ]; then
  mkdir -p "$(dirname "$MANIFEST")"
  cat > "$MANIFEST" <<'EOF'
# v0.6.4.2 Version Manifest

- Version: v0.6.4.2
- Build role: red-team-corrected no-capture catalog-stability diagnostic
- Package ID: `com.parkarsite.g6astability642`
- Physical status: pending manual red-team review and then physical test.
- Authoritative artifacts live only under immutable `builds/run-<id>-attempt-<n>/` directories.

## Build instances
EOF
fi

printf '\n### run-%s-attempt-%s\n- exact bundle: `builds/run-%s-attempt-%s/`\n- build commit: `%s`\n- APK SHA-256: `%s`\n- exact source ZIP SHA-256: `%s`\n- CI/static status: verified\n- physical status: not yet run\n' \
  "$GITHUB_RUN_ID" "$GITHUB_RUN_ATTEMPT" \
  "$GITHUB_RUN_ID" "$GITHUB_RUN_ATTEMPT" \
  "$GITHUB_SHA" "$APK_SHA" "$SRC_SHA" >> "$MANIFEST"

git config user.name "github-actions[bot]"
git config user.email "41898282+github-actions[bot]@users.noreply.github.com"
git add "releases/v0.6.4.2"
git commit -m "Archive immutable K G1 G6A No-Capture Stability v0.6.4.2 build"
git push origin main
