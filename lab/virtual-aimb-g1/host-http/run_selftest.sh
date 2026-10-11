#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$ROOT/../../.." && pwd)"
OUT="$(mktemp -d)"
trap 'rm -rf "$OUT"' EXIT
javac --release 17 --add-modules jdk.httpserver -d "$OUT" \
  "$REPO/source/g6a-recovery-core/src/com/parkarsite/g6a/recovery/RecoveryImportStore.java" \
  "$REPO/lab/virtual-aimb-g1/src/com/parkarsite/g1/virtual/VirtualLabPolicies.java" \
  "$ROOT/src/com/parkarsite/g1/virtual/host/LoopbackHttpSelfTest.java"
java --add-modules jdk.httpserver -cp "$OUT" com.parkarsite.g1.virtual.host.LoopbackHttpSelfTest
