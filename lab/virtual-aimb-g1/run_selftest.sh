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
  "$ROOT/selftest/com/parkarsite/g1/virtual/VirtualRemainingScenarioSelfTest.java" \
  "$ROOT/selftest/com/parkarsite/g1/virtual/VirtualPhase2RedTeamSelfTest.java"
java -cp "$OUT" com.parkarsite.g1.virtual.VirtualScenarioSelfTest | tee "$OUT/scenarios.log"
java -cp "$OUT" com.parkarsite.g1.virtual.VirtualRemainingScenarioSelfTest | tee -a "$OUT/scenarios.log"
java -cp "$OUT" com.parkarsite.g1.virtual.VirtualRedTeamSelfTest
java -cp "$OUT" com.parkarsite.g1.virtual.VirtualPhase2RedTeamSelfTest
# Explicitly verify manifest completeness, uniqueness and exact status phrase.
test "$(grep -Ec '^VGL-[0-9]{2}-[0-9]{2} OFFLINE_ASSERTIONS_PASS
 "$OUT/scenarios.log")" -eq 96
test -z "$(grep '^VGL-' "$OUT/scenarios.log" | sort | uniq -d)"
for pack in $(seq 1 12); do
  for case_number in $(seq 1 8); do
    printf -v case_id 'VGL-%02d-%02d' "$pack" "$case_number"
    grep -Fqx "$case_id OFFLINE_ASSERTIONS_PASS" "$OUT/scenarios.log"
  done
done
echo 'MANIFEST PASS: exactly 96 uniquely executed offline subset IDs; both red-team suites complete'
