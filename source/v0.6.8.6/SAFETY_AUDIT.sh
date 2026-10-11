#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
SRC="$ROOT/app/src/main"
JAVA="$SRC/java/com/parkarsite/g6apeerdiag686/MainActivity.java"
MANIFEST="$SRC/AndroidManifest.xml"
RULE="$SRC/java/com/parkarsite/g6apeerdiag686/DiagnosticSafetyRules.java"
fail=0
HITS="$(mktemp)"
trap 'rm -f "$HITS"' EXIT
bad() {
 if grep -REn -- "$1" "$SRC" >"$HITS"; then echo "FAIL: $2"; cat "$HITS"; fail=1
 else rc=$?; if [ "$rc" -eq 1 ]; then echo "PASS: $2 absent"; else echo "FAIL: $2 scan error"; fail=1; fi; fi
}
need() { if grep -Fq -- "$1" "$2"; then echo "PASS: $3"; else echo "FAIL: $3"; fail=1; fi; }
bad 'android.permission.INTERNET' 'INTERNET permission'
bad 'HttpURLConnection|okhttp|java.net.Socket|ServerSocket|java.net.URL|FileOutputStream|media.config|/files/' 'network/media APIs'
bad 'p2pManager.connect\(|p2pManager.cancelConnect\(|p2pManager.removeGroup\(|WifiP2pConfig|groupOwnerIntent|WpsInfo' 'P2P mutation APIs'
bad 'new byte\[\]\{0x02,0x01,0x04,0x02\}|new byte\[\]\{0x02,0x01,0x0[Ff]' 'AP/reset payloads'
bad 'parseTransferCredentials|credentialPassword|expectedP2pName|parseIpv4|groupOwnerAddress' 'credential/IP parsing'
need 'new byte[]{0x02,0x01,0x04,0x01}' "$JAVA" 'exact one ENTER payload'
need 'new byte[]{0x02,0x01,0x09}' "$JAVA" 'exact one EXIT payload'
need 'P2P_DISCOVERY_TIMEOUT_MS = 16000L' "$JAVA" '16-second discovery'
need 'requestGroupInfo' "$JAVA" 'read-only group checks'
need 'stopPeerDiscovery' "$JAVA" 'discovery cleanup'
need 'DiagnosticSafetyRules.classifyPeerName' "$JAVA" 'in-memory categories'
need 'MAX_EVENTS_PER_BUCKET=24' "$JAVA" 'event-ID bound'
need 'pendingEnterEvents.add(eventId)' "$JAVA" 'early 0x73 event type captured'
need 'android.permission.NEARBY_WIFI_DEVICES' "$MANIFEST" 'required nearby permission'
need 'android:allowBackup="false"' "$MANIFEST" 'no Android backup'
need 'fullSuffixMatch' "$RULE" 'no generic name-based association'
exit "$fail"
