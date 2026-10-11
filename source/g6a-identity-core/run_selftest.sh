#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$ROOT/../.." && pwd)"
OUT="$(mktemp -d)"
trap 'rm -rf "$OUT"' EXIT
LEGACY_685="$REPO/source/v0.6.8.5/app/src/main/java/com/parkarsite/g6ap2pdiag685/DiagnosticSafetyRules.java"
LEGACY_686="$REPO/source/v0.6.8.6/app/src/main/java/com/parkarsite/g6apeerdiag686/DiagnosticSafetyRules.java"
test -f "$LEGACY_685"
test -f "$LEGACY_686"
find "$ROOT/src" "$ROOT/selftest" -name '*.java' -print0 |
  xargs -0 javac --release 17 -d "$OUT" "$LEGACY_685" "$LEGACY_686"
java -cp "$OUT" com.parkarsite.g6a.identity.PeerIdentitySelfTest
java -cp "$OUT" com.parkarsite.g6ap2pdiag685.LegacyV685ComparisonSelfTest
java -cp "$OUT" com.parkarsite.g6apeerdiag686.LegacyV686ComparisonSelfTest
