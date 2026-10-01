#!/usr/bin/env bash
set -euo pipefail
: "${RUNNER_TEMP:?}"
: "${GITHUB_WORKSPACE:?}"
: "${GITHUB_SHA:?}"
: "${GITHUB_RUN_ID:?}"
: "${GITHUB_RUN_ATTEMPT:?}"

cd "$GITHUB_WORKSPACE"
STAGE="$RUNNER_TEMP/g6a65_exact_bundle"
rm -rf "$STAGE"
mkdir -p "$STAGE/reports" "$STAGE/reviews"

APK="K_G1_G6A_Single_Capture_Retention_v0_6_5.apk"
SRCZIP="K_G1_G6A_Single_Capture_Retention_v0_6_5_source_exact.zip"
PKGZIP="K_G1_G6A_Single_Capture_Retention_v0_6_5_package.zip"

cp "$RUNNER_TEMP/g6a65/$APK" "$STAGE/$APK"
cp "$RUNNER_TEMP/g6a65/lint-results-debug.html" "$STAGE/lint-results-debug.html"

# Archive source directly from the immutable build commit.
git cat-file -e "$GITHUB_SHA^{commit}"
git archive --format=zip --output="$STAGE/$SRCZIP" "$GITHUB_SHA" source/v0.6.5

(
  cd "$STAGE"
  sha256sum "$APK" > K_G1_G6A_Single_Capture_Retention_v0_6_5_APK_SHA256.txt
  sha256sum "$SRCZIP" > K_G1_G6A_Single_Capture_Retention_v0_6_5_source_SHA256.txt
)
APK_SHA="$(cut -d' ' -f1 "$STAGE/K_G1_G6A_Single_Capture_Retention_v0_6_5_APK_SHA256.txt")"
SRC_SHA="$(cut -d' ' -f1 "$STAGE/K_G1_G6A_Single_Capture_Retention_v0_6_5_source_SHA256.txt")"

cat > "$STAGE/BUILD_INFO.md" <<EOF
# K G1 G6A Single-Capture Catalog Retention v0.6.5 — Exact Build
- Build commit: $GITHUB_SHA
- Build run: $GITHUB_RUN_ID
- Build attempt: $GITHUB_RUN_ATTEMPT
- Package ID: com.parkarsite.g6acapture65
- versionCode: 1
- APK SHA-256: $APK_SHA
- Exact source ZIP SHA-256: $SRC_SHA
- Source archive: git archive from exact GITHUB_SHA
- Safety audit: PASS
- Red-team static audit: PASS
- Android compile: PASS
- Android Lint: PASS
- APK signature verification: PASS
- Capture action issued by app: none
- Passive capture gate: exact +1 only
- Catalog snapshots: baseline + post-capture + retention
- Media-file GET code paths: none
- Exact peer: case-sensitive BLE-reported P2P name
- No raw remote filename/path persistence
- No glasses mutation/deletion
EOF

cat > "$STAGE/reports/README.md" <<EOF
# Physical reports for exact v0.6.5 build

Only reports whose exported header matches all of the following belong here:
- Build commit: $GITHUB_SHA
- Build run: $GITHUB_RUN_ID
- Build attempt: $GITHUB_RUN_ATTEMPT
EOF

cat > "$STAGE/reviews/STATIC_AUDIT.md" <<EOF
# Pre-physical static review

- Safety audit: PASS
- Red-team static audit: PASS
- Compile/lint: PASS
- APK signature verification: PASS

A separate manual state-machine/provenance red-team review is required before physical promotion.
EOF

(
  cd "$STAGE"
  zip -q "$PKGZIP" "$APK" K_G1_G6A_Single_Capture_Retention_v0_6_5_APK_SHA256.txt "$SRCZIP" K_G1_G6A_Single_Capture_Retention_v0_6_5_source_SHA256.txt BUILD_INFO.md lint-results-debug.html reports/README.md reviews/STATIC_AUDIT.md
  sha256sum "$PKGZIP" > K_G1_G6A_Single_Capture_Retention_v0_6_5_package_SHA256.txt
)

git fetch origin main
git checkout -B main origin/main
DEST="releases/v0.6.5/builds/run-${GITHUB_RUN_ID}-attempt-${GITHUB_RUN_ATTEMPT}"
if [ -e "$DEST" ]; then
  echo "Refusing to overwrite immutable build bundle: $DEST"
  exit 1
fi
mkdir -p "$DEST"
cp -R "$STAGE"/. "$DEST"/

MANIFEST="releases/v0.6.5/VERSION_MANIFEST.md"
if [ ! -f "$MANIFEST" ]; then
  mkdir -p "$(dirname "$MANIFEST")"
  cat > "$MANIFEST" <<'EOF'
# v0.6.5 Version Manifest

- Version: v0.6.5
- Build role: single-capture catalog retention with zero media downloads
- Package ID: `com.parkarsite.g6acapture65`
- Physical status: NO PHYSICAL CANDIDATE YET
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
git add "releases/v0.6.5"
git commit -m "Archive immutable K G1 G6A Single-Capture Retention v0.6.5 build"
git push origin main
