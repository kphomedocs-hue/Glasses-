#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
REPO="$(cd "$ROOT/../../.." && pwd)"
OUT="$(mktemp -d)"
trap 'rm -rf "$OUT"' EXIT
javac --release 17 --add-modules jdk.httpserver -d "$OUT" \
  "$REPO/modules/protocol/src/main/java/com/parkarsite/g1/protocol/AimbG1Protocol.java" \
  "$REPO/source/g6a-identity-core/src/com/parkarsite/g6a/identity/PeerIdentityPolicy.java" \
  "$REPO/source/g6a-recovery-core/src/com/parkarsite/g6a/recovery/RecoveryImportStore.java" \
  "$REPO/lab/virtual-aimb-g1/src/com/parkarsite/g1/virtual/VirtualClock.java" \
  "$REPO/lab/virtual-aimb-g1/src/com/parkarsite/g1/virtual/FrameEvidence.java" \
  "$REPO/lab/virtual-aimb-g1/src/com/parkarsite/g1/virtual/VirtualGlassesCore.java" \
  "$REPO/lab/virtual-aimb-g1/src/com/parkarsite/g1/virtual/VirtualLabPolicies.java" \
  "$ROOT/src/com/parkarsite/g1/virtual/host/LoopbackHttpSelfTest.java" \
  "$ROOT/src/com/parkarsite/g1/virtual/host/HostCoreIntegrationSelfTest.java"
java --add-modules jdk.httpserver -cp "$OUT" com.parkarsite.g1.virtual.host.LoopbackHttpSelfTest
java --add-modules jdk.httpserver -cp "$OUT" com.parkarsite.g1.virtual.host.HostCoreIntegrationSelfTest
