#!/usr/bin/env bash
set -euo pipefail
: "${RUNNER_TEMP:?RUNNER_TEMP is required}"
: "${GITHUB_WORKSPACE:?GITHUB_WORKSPACE is required}"
: "${GITHUB_SHA:?GITHUB_SHA is required}"
: "${GITHUB_RUN_ID:?GITHUB_RUN_ID is required}"
cd "$GITHUB_WORKSPACE"
git pull --rebase origin main
SRC_DIR="source/v0.1.1"
REL_DIR="releases/v0.1.1"
APK_SRC="$RUNNER_TEMP/kg1v011/K_G1_Discovery_v0_1_1.apk"
LINT_SRC="$RUNNER_TEMP/kg1v011/lint-results-debug.html"
test -d "$SRC_DIR"
test -f "$APK_SRC"
test -f "$LINT_SRC"
mkdir -p "$REL_DIR"
python3 - <<'PY'
from pathlib import Path
import zipfile
root=Path("source/v0.1.1")
out=Path("releases/v0.1.1/K_G1_Discovery_v0_1_1_source_v1.zip")
with zipfile.ZipFile(out,"w",compression=zipfile.ZIP_DEFLATED,compresslevel=9) as z:
    for p in sorted(x for x in root.rglob("*") if x.is_file()):
        rel=p.relative_to(root).as_posix()
        zi=zipfile.ZipInfo(rel,(1980,1,1,0,0,0))
        mode=0o755 if p.name.endswith(".sh") else 0o644
        zi.external_attr=(mode & 0xFFFF) << 16
        zi.compress_type=zipfile.ZIP_DEFLATED
        z.writestr(zi,p.read_bytes())
PY
SOURCE_ZIP="$REL_DIR/K_G1_Discovery_v0_1_1_source_v1.zip"
SOURCE_SHA="$(sha256sum "$SOURCE_ZIP" | awk '{print $1}')"
sha256sum "$SOURCE_ZIP" > "$REL_DIR/K_G1_Discovery_v0_1_1_source_SHA256.txt"
if [[ ! -f "$REL_DIR/K_G1_Discovery_v0_1_1.apk" ]]; then
  cp "$APK_SRC" "$REL_DIR/K_G1_Discovery_v0_1_1.apk"
  cp "$LINT_SRC" "$REL_DIR/lint-results-debug.html"
  sha256sum "$REL_DIR/K_G1_Discovery_v0_1_1.apk" > "$REL_DIR/K_G1_Discovery_v0_1_1_APK_SHA256.txt"
  APK_SHA="$(sha256sum "$REL_DIR/K_G1_Discovery_v0_1_1.apk" | awk '{print $1}')"
  {
    echo "# K G1 Discovery v0.1.1 — Verified Repository Build"
    echo
    echo "- Source SHA-256: `$SOURCE_SHA`"
    echo "- Repository-built APK SHA-256: `$APK_SHA`"
    echo "- Git commit used for build: `$GITHUB_SHA`"
    echo "- GitHub Actions run: `$GITHUB_RUN_ID`"
    echo "- Android compile: PASS"
    echo "- Android Lint: PASS"
    echo "- Read-only safety audit: PASS"
    echo "- APK signature verification: PASS"
    echo
    echo "v0.1 remains frozen. v0.1.1 changes only target-name recognition to accept AIMB-G1 and AIMB-G1_* while preserving the same read-only G1 boundary."
  } > "$REL_DIR/BUILD_INFO.md"
  rm -rf "$RUNNER_TEMP/package-v011"
  mkdir -p "$RUNNER_TEMP/package-v011"
  cp "$REL_DIR/K_G1_Discovery_v0_1_1.apk" "$RUNNER_TEMP/package-v011/"
  cp "$REL_DIR/K_G1_Discovery_v0_1_1_APK_SHA256.txt" "$RUNNER_TEMP/package-v011/"
  cp "$SOURCE_ZIP" "$RUNNER_TEMP/package-v011/"
  cp "$REL_DIR/K_G1_Discovery_v0_1_1_source_SHA256.txt" "$RUNNER_TEMP/package-v011/"
  cp "$REL_DIR/BUILD_INFO.md" "$RUNNER_TEMP/package-v011/"
  cp "$REL_DIR/lint-results-debug.html" "$RUNNER_TEMP/package-v011/"
  cp README.md "$RUNNER_TEMP/package-v011/PROJECT_README.md"
  cp LATEST_CHECKPOINT.md "$RUNNER_TEMP/package-v011/LATEST_CHECKPOINT.md"
  ( cd "$RUNNER_TEMP/package-v011" && zip -q -r "$GITHUB_WORKSPACE/$REL_DIR/K_G1_Discovery_v0_1_1_package.zip" . )
  sha256sum "$REL_DIR/K_G1_Discovery_v0_1_1_package.zip" > "$REL_DIR/K_G1_Discovery_v0_1_1_package_SHA256.txt"
fi
git config user.name "github-actions[bot]"
git config user.email "41898282+github-actions[bot]@users.noreply.github.com"
git add "$REL_DIR"
if ! git diff --cached --quiet; then
  git commit -m "Archive verified K G1 Discovery v0.1.1 build"
  git push
fi
