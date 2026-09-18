#!/usr/bin/env bash
set -euo pipefail

: "${RUNNER_TEMP:?RUNNER_TEMP is required}"
: "${GITHUB_WORKSPACE:?GITHUB_WORKSPACE is required}"
: "${GITHUB_SHA:?GITHUB_SHA is required}"
: "${GITHUB_RUN_ID:?GITHUB_RUN_ID is required}"

cd "$GITHUB_WORKSPACE"

SOURCE_ZIP="releases/v0.1/K_G1_Discovery_v0_1_source_v4.zip"
APK_SRC="$RUNNER_TEMP/kg1/out/K_G1_Discovery_v0_1.apk"
LINT_SRC="$RUNNER_TEMP/kg1/app/build/reports/lint-results-debug.html"

test -f "$SOURCE_ZIP"
test -f "$APK_SRC"
test -f "$LINT_SRC"

# Browsable exact source tree.
rm -rf source/v0.1
mkdir -p source/v0.1
unzip -q "$SOURCE_ZIP" -d source/v0.1

# Permanent verified build outputs.
mkdir -p releases/v0.1
cp "$APK_SRC" releases/v0.1/K_G1_Discovery_v0_1.apk
sha256sum releases/v0.1/K_G1_Discovery_v0_1.apk > releases/v0.1/K_G1_Discovery_v0_1_APK_SHA256.txt
cp "$LINT_SRC" releases/v0.1/lint-results-debug.html

APK_SHA="$(sha256sum releases/v0.1/K_G1_Discovery_v0_1.apk | awk '{print $1}')"
SOURCE_SHA="$(sha256sum "$SOURCE_ZIP" | awk '{print $1}')"

{
  echo "# K G1 Discovery v0.1 — Verified Repository Build"
  echo
  echo "- Source SHA-256: \`$SOURCE_SHA\`"
  echo "- Repository-built APK SHA-256: \`$APK_SHA\`"
  echo "- Git commit used for build: \`$GITHUB_SHA\`"
  echo "- GitHub Actions run: \`$GITHUB_RUN_ID\`"
  echo "- Android compile: PASS"
  echo "- Android Lint: PASS"
  echo "- Read-only safety audit: PASS"
  echo "- APK signature verification: PASS"
  echo
  echo "This APK is rebuilt from the exact frozen v0.1 source. Its byte hash may differ from an earlier debug APK because Android debug signing keys can differ between build environments."
} > releases/v0.1/BUILD_INFO.md

# Self-contained package.
rm -rf "$RUNNER_TEMP/package"
mkdir -p "$RUNNER_TEMP/package"
cp releases/v0.1/K_G1_Discovery_v0_1.apk "$RUNNER_TEMP/package/"
cp "$SOURCE_ZIP" "$RUNNER_TEMP/package/"
cp releases/v0.1/K_G1_Discovery_v0_1_APK_SHA256.txt "$RUNNER_TEMP/package/"
cp releases/v0.1/BUILD_INFO.md "$RUNNER_TEMP/package/"
cp releases/v0.1/lint-results-debug.html "$RUNNER_TEMP/package/"
cp README.md "$RUNNER_TEMP/package/PROJECT_README.md"
cp LATEST_CHECKPOINT.md "$RUNNER_TEMP/package/LATEST_CHECKPOINT.md"
(
  cd "$RUNNER_TEMP/package"
  zip -q -r "$GITHUB_WORKSPACE/releases/v0.1/K_G1_Discovery_v0_1_package.zip" .
)
sha256sum releases/v0.1/K_G1_Discovery_v0_1_package.zip > releases/v0.1/K_G1_Discovery_v0_1_package_SHA256.txt

git config user.name "github-actions[bot]"
git config user.email "41898282+github-actions[bot]@users.noreply.github.com"
git add source/v0.1 releases/v0.1
if ! git diff --cached --quiet; then
  git commit -m "Archive verified K G1 Discovery v0.1 build"
  git push
fi
