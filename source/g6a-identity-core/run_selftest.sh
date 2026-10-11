#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
OUT="$(mktemp -d)"
trap 'rm -rf "$OUT"' EXIT
find "$ROOT/src" "$ROOT/selftest" -name '*.java' -print0 | xargs -0 javac --release 17 -d "$OUT"
java -cp "$OUT" com.parkarsite.g6a.identity.PeerIdentitySelfTest
