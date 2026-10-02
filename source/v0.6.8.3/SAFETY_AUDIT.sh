#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
SRC="$ROOT/app/src/main"
JAVA="$SRC/java/com/parkarsite/g6acreddiag683/MainActivity.java"
GRADLE="$ROOT/app/build.gradle.kts"
fail=0
must(){ grep -Fq "$1" "$2" || { echo "FAIL: missing $3"; fail=1; }; }
bad(){ if grep -RniE "$1" "$SRC" >/tmp/g6a683_hits 2>/dev/null; then echo "FAIL: $2"; cat /tmp/g6a683_hits; fail=1; else echo "PASS: $2 absent"; fi; }

must 'applicationId = "com.parkarsite.g6acreddiag683"' "$GRADLE" 'fresh package'
must 'versionName = "0.6.8.3"' "$GRADLE" 'version'
must 'App version: 0.6.8.3' "$JAVA" 'runtime version'
must 'if(declared>=8||f.length!=declared+6)return "";' "$JAVA" 'safe short-payload logging guard'
must 'Post-exit 0x41 frame observed: IGNORED for exit confirmation; payload not classified/logged' "$JAVA" 'post-exit no-log rule'
must 'Credential-capable notification payload bytes logged: NO' "$JAVA" 'credential-capable payload privacy'
must 'Safe short (<8-byte) 0x41 payload bytes logged during ENTER/LATE observation:' "$JAVA" 'accurate short-payload summary'
must 'SHORT_ENTER_ECHO_SHAPE' "$JAVA" 'enter echo classification'
must 'SHORT_ENTER_PREFIX_PLUS_STATUS' "$JAVA" 'enter status classification'
must 'payloadHex=' "$JAVA" 'safe short payload evidence'
bad 'WifiP2p|HttpURLConnection|java\.net\.URL|new URL\(' 'Wi-Fi Direct / HTTP code'
bad 'android.permission.INTERNET|NEARBY_WIFI_DEVICES|ACCESS_FINE_LOCATION|ACCESS_COARSE_LOCATION' 'network/location permissions'
bad 'FileOutputStream|FileWriter|RandomAccessFile|Files\.write|SharedPreferences' 'persistent write code'
bad 'credentialPassword[[:space:]]*=|expectedP2pName[[:space:]]*=' 'credential value storage'
bad 'setRequestMethod|setRequestProperty|/files/' 'remote HTTP endpoint code'

echo "v0.6.8.3 safety audit complete"
exit "$fail"
