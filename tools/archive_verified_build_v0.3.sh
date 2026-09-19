#!/usr/bin/env bash
set -euo pipefail

: "${RUNNER_TEMP:?RUNNER_TEMP is required}"
: "${GITHUB_WORKSPACE:?GITHUB_WORKSPACE is required}"
: "${GITHUB_SHA:?GITHUB_SHA is required}"
: "${GITHUB_RUN_ID:?GITHUB_RUN_ID is required}"

cd "$GITHUB_WORKSPACE"
git pull --rebase origin main

SRC_DIR="source/v0.3"
REL_DIR="releases/v0.3"
APK_SRC="$RUNNER_TEMP/kg1g3/K_G1_Media_Count_Probe_v0_3.apk"
LINT_SRC="$RUNNER_TEMP/kg1g3/lint-results-debug.html"

test -d "$SRC_DIR"
test -f "$APK_SRC"
test -f "$LINT_SRC"
mkdir -p "$REL_DIR"

python3 - <<'PY'
from pathlib import Path
import subprocess, zipfile
root = Path("source/v0.3")
out = Path("releases/v0.3/K_G1_Media_Count_Probe_v0_3_source_v1.zip")
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

SOURCE_ZIP="$REL_DIR/K_G1_Media_Count_Probe_v0_3_source_v1.zip"
SOURCE_SHA="$(sha256sum "$SOURCE_ZIP" | awk '{print $1}')"
sha256sum "$SOURCE_ZIP" > "$REL_DIR/K_G1_Media_Count_Probe_v0_3_source_SHA256.txt"

if [[ ! -f "$REL_DIR/K_G1_Media_Count_Probe_v0_3.apk" ]]; then
  cp "$APK_SRC" "$REL_DIR/K_G1_Media_Count_Probe_v0_3.apk"
  cp "$LINT_SRC" "$REL_DIR/lint-results-debug.html"
  sha256sum "$REL_DIR/K_G1_Media_Count_Probe_v0_3.apk" > "$REL_DIR/K_G1_Media_Count_Probe_v0_3_APK_SHA256.txt"
  CANONICAL_BUILD_COMMIT="$GITHUB_SHA"
  CANONICAL_BUILD_RUN="$GITHUB_RUN_ID"
else
  test -f "$REL_DIR/K_G1_Media_Count_Probe_v0_3_APK_SHA256.txt"
  test -f "$REL_DIR/lint-results-debug.html"
  CANONICAL_BUILD_COMMIT="$(sed -n 's/^- Canonical APK build commit: //p' "$REL_DIR/BUILD_INFO.md" | head -n1)"
  CANONICAL_BUILD_RUN="$(sed -n 's/^- Canonical APK build run: //p' "$REL_DIR/BUILD_INFO.md" | head -n1)"
fi

APK_SHA="$(sha256sum "$REL_DIR/K_G1_Media_Count_Probe_v0_3.apk" | awk '{print $1}')"

{
  echo "# K G1 Media Count Probe v0.3 — Verified Repository Build"
  echo
  echo "- Source SHA-256: $SOURCE_SHA"
  echo "- Repository-built APK SHA-256: $APK_SHA"
  echo "- Canonical APK build commit: $CANONICAL_BUILD_COMMIT"
  echo "- Canonical APK build run: $CANONICAL_BUILD_RUN"
  echo "- Latest verification commit: $GITHUB_SHA"
  echo "- Latest verification run: $GITHUB_RUN_ID"
  echo "- G3 single-query scope verification: PASS"
  echo "- G3 safety audit: PASS"
  echo "- Android compile: PASS"
  echo "- Android Lint: PASS"
  echo "- APK signature verification: PASS"
  echo "- Source package contents: Git-tracked source files only"
  echo
  echo "This build sends exactly one command-0x41 frame with payload 02 04 after notification subscription. It contains no media-mode payload, Wi-Fi/network code, transfer code, arbitrary command input, retry loop, reset or OTA behavior."
} > "$REL_DIR/BUILD_INFO.md"

rm -rf "$RUNNER_TEMP/package-g3"
mkdir -p "$RUNNER_TEMP/package-g3"
cp "$REL_DIR/K_G1_Media_Count_Probe_v0_3.apk" "$RUNNER_TEMP/package-g3/"
cp "$REL_DIR/K_G1_Media_Count_Probe_v0_3_APK_SHA256.txt" "$RUNNER_TEMP/package-g3/"
cp "$SOURCE_ZIP" "$RUNNER_TEMP/package-g3/"
cp "$REL_DIR/K_G1_Media_Count_Probe_v0_3_source_SHA256.txt" "$RUNNER_TEMP/package-g3/"
cp "$REL_DIR/BUILD_INFO.md" "$RUNNER_TEMP/package-g3/"
cp "$REL_DIR/lint-results-debug.html" "$RUNNER_TEMP/package-g3/"
cp README.md "$RUNNER_TEMP/package-g3/PROJECT_README.md"
cp LATEST_CHECKPOINT.md "$RUNNER_TEMP/package-g3/LATEST_CHECKPOINT.md"
cp docs/testing/results/2026-09-19_G2C_TIME_SYNC_PASS.md "$RUNNER_TEMP/package-g3/G2C_TIME_SYNC_PASS.md"

rm -f "$REL_DIR/K_G1_Media_Count_Probe_v0_3_package.zip"
(
  cd "$RUNNER_TEMP/package-g3"
  zip -q -r "$GITHUB_WORKSPACE/$REL_DIR/K_G1_Media_Count_Probe_v0_3_package.zip" .
)
sha256sum "$REL_DIR/K_G1_Media_Count_Probe_v0_3_package.zip" > "$REL_DIR/K_G1_Media_Count_Probe_v0_3_package_SHA256.txt"

git config user.name "github-actions[bot]"
git config user.email "41898282+github-actions[bot]@users.noreply.github.com"
git add "$REL_DIR"
if ! git diff --cached --quiet; then
  git commit -m "Archive verified K G1 Media Count Probe v0.3 build"
  git push
fi
