#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
SRC="$ROOT/app/src/main"
JAVA="$SRC/java/com/parkarsite/g6acreddiag682/MainActivity.java"
GRADLE="$ROOT/app/build.gradle.kts"
fail=0
must(){ grep -Fq "$1" "$2" || { echo "FAIL: missing $3"; fail=1; }; }
bad(){ if grep -RniE "$1" "$SRC" >/tmp/g6a682_hits 2>/dev/null; then echo "FAIL: $2"; cat /tmp/g6a682_hits; fail=1; else echo "PASS: $2 absent"; fi; }

must 'applicationId = "com.parkarsite.g6acreddiag682"' "$GRADLE" 'fresh package'
must 'versionName = "0.6.8.2"' "$GRADLE" 'version'
must 'App version: 0.6.8.2' "$JAVA" 'runtime version'
must 'if(declared>=8||f.length!=declared+6)return "";' "$JAVA" 'safe short-payload logging guard'
must 'SHORT_ENTER_ECHO_SHAPE' "$JAVA" 'enter echo-shape classification'
must 'SHORT_ENTER_PREFIX_PLUS_STATUS' "$JAVA" 'enter status classification'
must 'payloadHex=' "$JAVA" 'safe short payload evidence'
must 'Credential values decoded/logged/persisted: NO' "$JAVA" 'credential privacy'
must 'Android Wi-Fi Direct API operations: 0' "$JAVA" 'zero wifi'
must 'HTTP requests: 0' "$JAVA" 'zero http'
must 'Media-file GET requests: 0' "$JAVA" 'zero media'
bad 'WifiP2p|HttpURLConnection|java\.net\.URL|new URL\(' 'Wi-Fi Direct / HTTP code'
bad 'android.permission.INTERNET|NEARBY_WIFI_DEVICES|ACCESS_FINE_LOCATION|ACCESS_COARSE_LOCATION' 'network/location permissions'
bad 'FileOutputStream|FileWriter|RandomAccessFile|Files\.write|SharedPreferences' 'persistent write code'
bad 'credentialPassword[[:space:]]*=|expectedP2pName[[:space:]]*=' 'credential value storage'
bad 'setRequestMethod|setRequestProperty|/files/' 'remote HTTP endpoint code'

echo "v0.6.8.2 safety audit complete"
exit "$fail"
