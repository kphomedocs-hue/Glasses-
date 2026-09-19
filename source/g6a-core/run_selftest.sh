#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
OUT="$ROOT/.selftest-build"
rm -rf "$OUT"
mkdir -p "$OUT"
find "$ROOT/src" "$ROOT/selftest" -name '*.java' -print0 | xargs -0 javac -source 17 -target 17 -d "$OUT"
java -cp "$OUT" com.parkarsite.g6a.CoreSelfTest
rm -rf "$OUT"
