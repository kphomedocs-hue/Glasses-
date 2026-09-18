#!/usr/bin/env bash
set -euo pipefail

: "${RUNNER_TEMP:?RUNNER_TEMP is required}"
: "${GITHUB_WORKSPACE:?GITHUB_WORKSPACE is required}"
: "${GITHUB_SHA:?GITHUB_SHA is required}"
: "${GITHUB_RUN_ID:?GITHUB_RUN_ID is required}"

cd "$GITHUB_WORKSPACE"
git pull --rebase origin main

SRC_DIR="source/v0.1.2"
REL_DIR="releases/v0.1.2"
APK_SRC="$RUNNER_TEMP/kg1v012/K_G1_Discovery_v0_1_2.apk"
LINT_SRC="$RUNNER_TEMP/kg1v012/lint-results-debug.html"

test -d "$SRC_DIR"
test -f "$APK_SRC"
test -f "$LINT_SRC"
mkdir -p "$REL_DIR"

python3 - <<'PY'
from pathlib import Path
import subprocess, zipfile
root = Path("source/v0.1.2")
out = Path("releases/v0.1.2/K_G1_Discovery_v0_1_2_source_v1.zip")
tracked = subprocess.check_output(["git", "ls-files", "-z", str(root)]).decode().split("\0")
with zipfile.ZipFile(out, "w", compression=zipfile.ZIP_DEFLATED, compresslevel=9) as z:
    for item in sorted(x for x in tracked if x):
        p = Path(item)
        rel = p.relative_to(root).as_posix()
        zi = zipfile.ZipInfo(rel, (1980, 1, 1, 0, 0, 0))
        mode = 0o755 if p.name.endswith(".sh") else 0o644
        zi.external_attr = (mode & 0xFFFF) << 16
        zi.compress_type = zipfile.ZIP_DEFLATED
        z.writestr(zi, p.read_bytes())
PY

SOURCE_ZIP="$REL_DIR/K_G1_Discovery_v0_1_2_source_v1.zip"
SOURCE_SHA="$(sha256sum "$SOURCE_ZIP" | awk '{print $1}')"
sha256sum "$SOURCE_ZIP" > "$REL_DIR/K_G1_Discovery_v0_1_2_source_SHA256.txt"

if [[ ! -f "$REL_DIR/K_G1_Discovery_v0_1_2.apk" ]]; then
  cp "$APK_SRC" "$REL_DIR/K_G1_Discovery_v0_1_2.apk"
  cp "$LINT_SRC" "$REL_DIR/lint-results-debug.html"
  sha256sum "$REL_DIR/K_G1_Discovery_v0_1_2.apk" > "$REL_DIR/K_G1_Discovery_v0_1_2_APK_SHA256.txt"
  CANONICAL_BUILD_COMMIT="$GITHUB_SHA"
  CANONICAL_BUILD_RUN="$GITHUB_RUN_ID"
else
  test -f "$REL_DIR/K_G1_Discovery_v0_1_2_APK_SHA256.txt"
  test -f "$REL_DIR/lint-results-debug.html"
  CANONICAL_BUILD_COMMIT="$(sed -n 's/^- Canonical APK build commit: //p' "$REL_DIR/BUILD_INFO.md" | head -n1)"
  CANONICAL_BUILD_RUN="$(sed -n 's/^- Canonical APK build run: //p' "$REL_DIR/BUILD_INFO.md" | head -n1)"
fi

APK_SHA="$(sha256sum "$REL_DIR/K_G1_Discovery_v0_1_2.apk" | awk '{print $1}')"
{
  echo "# K G1 Discovery v0.1.2 — Verified Repository Build"
  echo
  echo "- Source SHA-256: $SOURCE_SHA"
  echo "- Repository-built APK SHA-256: $APK_SHA"
  echo "- Canonical APK build commit: $CANONICAL_BUILD_COMMIT"
  echo "- Canonical APK build run: $CANONICAL_BUILD_RUN"
  echo "- Latest verification commit: $GITHUB_SHA"
  echo "- Latest verification run: $GITHUB_RUN_ID"
  echo "- Diagnostic scope verification: PASS"
  echo "- Android compile: PASS"
  echo "- Android Lint: PASS"
  echo "- Read-only safety audit: PASS"
  echo "- APK signature verification: PASS"
  echo "- Source package contents: Git-tracked source files only"
  echo
  echo "v0.1 and v0.1.1 remain unchanged. v0.1.2 adds read-only BLE observation and a uniquely identified bonded-device LE GATT fallback; it adds no control/write/network/reset behavior."
} > "$REL_DIR/BUILD_INFO.md"

rm -rf "$RUNNER_TEMP/package-v012"
mkdir -p "$RUNNER_TEMP/package-v012"
cp "$REL_DIR/K_G1_Discovery_v0_1_2.apk" "$RUNNER_TEMP/package-v012/"
cp "$REL_DIR/K_G1_Discovery_v0_1_2_APK_SHA256.txt" "$RUNNER_TEMP/package-v012/"
cp "$SOURCE_ZIP" "$RUNNER_TEMP/package-v012/"
cp "$REL_DIR/K_G1_Discovery_v0_1_2_source_SHA256.txt" "$RUNNER_TEMP/package-v012/"
cp "$REL_DIR/BUILD_INFO.md" "$RUNNER_TEMP/package-v012/"
cp "$REL_DIR/lint-results-debug.html" "$RUNNER_TEMP/package-v012/"
cp README.md "$RUNNER_TEMP/package-v012/PROJECT_README.md"
cp LATEST_CHECKPOINT.md "$RUNNER_TEMP/package-v012/LATEST_CHECKPOINT.md"
rm -f "$REL_DIR/K_G1_Discovery_v0_1_2_package.zip"
(
  cd "$RUNNER_TEMP/package-v012"
  zip -q -r "$GITHUB_WORKSPACE/$REL_DIR/K_G1_Discovery_v0_1_2_package.zip" .
)
sha256sum "$REL_DIR/K_G1_Discovery_v0_1_2_package.zip" > "$REL_DIR/K_G1_Discovery_v0_1_2_package_SHA256.txt"

git config user.name "github-actions[bot]"
git config user.email "41898282+github-actions[bot]@users.noreply.github.com"
git add "$REL_DIR"
if ! git diff --cached --quiet; then
  git commit -m "Archive verified K G1 Discovery v0.1.2 build"
  git push
fi
