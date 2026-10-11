#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
JAVA="$ROOT/app/src/main/java/com/parkarsite/g6apeerdiag686/MainActivity.java"
RULE="$ROOT/app/src/main/java/com/parkarsite/g6apeerdiag686/DiagnosticSafetyRules.java"
OUT="$(mktemp -d)"
trap 'rm -rf "$OUT"' EXIT
javac -d "$OUT" "$RULE" "$ROOT/tests/DiagnosticSafetyRulesTest.java"
java -cp "$OUT" com.parkarsite.g6apeerdiag686.DiagnosticSafetyRulesTest
python3 - "$JAVA" <<'PY'
from pathlib import Path
import sys,re
s=Path(sys.argv[1]).read_text()
checks={
"single source ENTER":s.count('new byte[]{0x02,0x01,0x04,0x01}')==1,
"single source EXIT":s.count('new byte[]{0x02,0x01,0x09}')==1,
"identity categories only":'DiagnosticSafetyRules.classifyPeerName(' in s,
"bounded peer snapshots":'if(peerSnapshots<MAX_PEER_SNAPSHOTS)' in s,
"bounded event buckets":'MAX_EVENTS_PER_BUCKET=24' in s,
"pre ENTER event IDs":'preEnterEvents.add(eventId)' in s,
"pending ENTER event IDs":'pendingEnterEvents.add(eventId)' in s,
"post ENTER event IDs":'postEnterEvents.add(eventId)' in s,
"event payload omitted":'append("Raw' not in s,
"discovery deadline":'armTimer("DISCOVERY",P2P_DISCOVERY_TIMEOUT_MS' in s,
"BLE deadline":'armTimer("BLE",CONNECT_TIMEOUT_MS' in s,
"preflight deadline":'armTimer("PREFLIGHT",PREFLIGHT_TIMEOUT_MS' in s,
"overall cleanup deadline":'armTimer("CLEANUP_TOTAL",CLEANUP_TOTAL_TIMEOUT_MS' in s,
"cleanup callback watchdog":'armTimer("CLEANUP_STEP",CLEANUP_STEP_TIMEOUT_MS' in s,
"EXIT deferred until BLE write settles":'pendingWrites.contains(BleWrite.ENTER)' in s,
"EXIT callback watchdog":'armTimer("EXIT",EXIT_CALLBACK_TIMEOUT_MS' in s,
"central terminal EXIT":'if(enterWriteAttempted)sendExitOnce();else startCleanup();' in s,
"fail closed cleanup":'DiagnosticSafetyRules.cleanTerminalClassification(' in s,
"both group queries":'VERIFY_GROUP_FIRST' in s and 'VERIFY_GROUP_SECOND' in s,
"no association":'WifiP2pConfig' not in s and 'p2pManager.connect(' not in s,
"no cancellation":'p2pManager.cancelConnect(' not in s,
"no group removal":'p2pManager.removeGroup(' not in s,
"no media":'HttpURLConnection' not in s and 'FileOutputStream' not in s,
"single-session UI":'runButton.setEnabled(false);reportView.setText(report.toString());' in s,
"build provenance":all(x in s for x in ('BuildConfig.BUILD_COMMIT','BuildConfig.BUILD_RUN','BuildConfig.BUILD_ATTEMPT')),
"foreground abort":'requestTerminal("Activity left foreground;' in s,
}
for name,passed in checks.items():print(("PASS" if passed else "FAIL")+": "+name)
if not all(checks.values()):raise SystemExit(1)
PY
