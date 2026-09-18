#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT="$ROOT/.selftest-build"
rm -rf "$OUT" && mkdir -p "$OUT"
javac -d "$OUT" \
  "$ROOT/app/src/main/java/com/parkarsite/g1capture/g1/G1Transport.java" \
  "$ROOT/app/src/main/java/com/parkarsite/g1capture/g1/RemoteMedia.java" \
  "$ROOT/app/src/main/java/com/parkarsite/g1capture/storage/NumberingPolicy.java" \
  "$ROOT/app/src/main/java/com/parkarsite/g1capture/storage/FileImportLedger.java" \
  "$ROOT/app/src/main/java/com/parkarsite/g1capture/storage/FileMediaArchive.java" \
  "$ROOT/app/src/main/java/com/parkarsite/g1capture/core/ProbeRunner.java" \
  "$ROOT/app/src/main/java/com/parkarsite/g1capture/research/HeyCyanCandidateProtocol.java" \
  "$ROOT/core-selftest/com/parkarsite/g1capture/selftest/CoreSelfTest.java"
java -cp "$OUT" com.parkarsite.g1capture.selftest.CoreSelfTest
rm -rf "$OUT"
