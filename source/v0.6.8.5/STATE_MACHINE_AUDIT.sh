#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
JAVA="$ROOT/app/src/main/java/com/parkarsite/g6ap2pdiag685/MainActivity.java"
RULE="$ROOT/app/src/main/java/com/parkarsite/g6ap2pdiag685/DiagnosticSafetyRules.java"
OUT="$(mktemp -d)"
trap 'rm -rf "$OUT"' EXIT
javac -d "$OUT" "$RULE" "$ROOT/tests/DiagnosticSafetyRulesTest.java"
java -cp "$OUT" com.parkarsite.g6ap2pdiag685.DiagnosticSafetyRulesTest
python3 - "$JAVA" <<'PY'
from pathlib import Path
import re,sys
s=Path(sys.argv[1]).read_text()
requirements={
 'distinct discovery deadline': 'armTimer("DISCOVERY",P2P_DISCOVERY_TIMEOUT_MS' in s,
 'distinct association deadline': 'armTimer("ASSOCIATION",P2P_CONNECTION_TIMEOUT_MS' in s,
 'distinct BLE deadline': 'armTimer("BLE",CONNECT_TIMEOUT_MS' in s,
 'distinct preflight deadline': 'armTimer("PREFLIGHT",PREFLIGHT_TIMEOUT_MS' in s,
 'distinct overall cleanup deadline': 'armTimer("CLEANUP_TOTAL",CLEANUP_TOTAL_TIMEOUT_MS' in s,
 'independent callback watchdog': 'armTimer("CLEANUP_STEP",CLEANUP_STEP_TIMEOUT_MS' in s,
 'EXIT terminal route': 'if(enterWriteAttempted)sendExitOnce();else startCleanup();' in s,
 'one EXIT guard': 'if(exitWriteAttempted){startCleanup();return;}' in s,
 'all terminal results after cleanup': 'private void finishReport()' in s and 'private void requestTerminal(String reason)' in s,
 'cleanup group ownership policy': 'DiagnosticSafetyRules.mayRemoveGroup(' in s,
 'group ownership requires formed-by-test': 'groupCreatedByTest,connectRequested,groupContainsTarget(group)' in s,
 'fail-closed terminal classification': 'DiagnosticSafetyRules.cleanTerminalClassification(' in s,
 'post-ENTER callback event fence': 'if(!enterCallbackSucceeded){pendingEnter73Count++;return;}' in s,
 'no shared mutable timeout': 'private Runnable timeout;' not in s and 'void schedule(' not in s,
 'preexisting group never removed': 'if(preexistingGroup){append("Pre-existing group preserved' in s,
 'two group verification steps': 'VERIFY_GROUP_FIRST' in s and 'VERIFY_GROUP_SECOND' in s,
 'activity stop terminal cleanup': 'onStop()' in s and 'requestTerminal("Activity left foreground' in s,
 'discovery request cleanup regardless of callback': 'if(!discoveryRequested||' in s,
 'no direct fail bypass': not re.search(r'\bfail\(',s),
 'no Wi-Fi target identity reporting': 'append("Peer address' not in s and 'append("Bluetooth address' not in s,
 'single-session physical diagnostic': 'runButton.setEnabled(false);reportView.setText(report.toString());' in s,
 'preflight then BLE': 'startBleConnection(pendingTarget);' in s,
 'notifications then discovery': 'cancelTimer("BLE");startP2pDiscovery();' in s,
 'discovery then ENTER': 'append("discoverPeers: SUCCESS");sendEnter(gatt);' in s,
 'permission denial report': 'private void abortBeforeRun(String reason)' in s,
 'CI build provenance in report': 'appendBuildIdentity();' in s and 'BuildConfig.BUILD_COMMIT' in s and 'BuildConfig.BUILD_RUN' in s and 'BuildConfig.BUILD_ATTEMPT' in s,
}
for name,ok in requirements.items(): print(('PASS' if ok else 'FAIL')+': '+name)
if not all(requirements.values()):raise SystemExit(1)
PY
