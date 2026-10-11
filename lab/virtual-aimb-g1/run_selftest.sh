#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$ROOT/../.." && pwd)"
OUT="$(mktemp -d)"
trap 'rm -rf "$OUT"' EXIT
javac --release 17 -d "$OUT" \
  "$REPO/modules/protocol/src/main/java/com/parkarsite/g1/protocol/AimbG1Protocol.java" \
  "$REPO/source/g6a-identity-core/src/com/parkarsite/g6a/identity/PeerIdentityPolicy.java" \
  "$ROOT/src/com/parkarsite/g1/virtual/VirtualClock.java" \
  "$ROOT/src/com/parkarsite/g1/virtual/FrameEvidence.java" \
  "$ROOT/src/com/parkarsite/g1/virtual/VirtualGlassesCore.java" \
  "$ROOT/src/com/parkarsite/g1/virtual/VirtualLabPolicies.java" \
  "$REPO/source/g6a-recovery-core/src/com/parkarsite/g6a/recovery/RecoveryImportStore.java" \
  "$ROOT/selftest/com/parkarsite/g1/virtual/VirtualScenarioSelfTest.java" \
  "$ROOT/selftest/com/parkarsite/g1/virtual/VirtualRedTeamSelfTest.java" \
  "$ROOT/selftest/com/parkarsite/g1/virtual/VirtualRemainingScenarioSelfTest.java"
java -cp "$OUT" com.parkarsite.g1.virtual.VirtualScenarioSelfTest
java -cp "$OUT" com.parkarsite.g1.virtual.VirtualRedTeamSelfTest
java -cp "$OUT" com.parkarsite.g1.virtual.VirtualRemainingScenarioSelfTest
