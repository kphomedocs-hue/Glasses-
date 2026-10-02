#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
SRC="$ROOT/app/src/main"
JAVA="$SRC/java/com/parkarsite/g6ainitdiag684/MainActivity.java"
GRADLE="$ROOT/app/build.gradle.kts"
fail=0
must(){ grep -Fq "$1" "$2" || { echo "FAIL: missing $3"; fail=1; }; }
bad(){ if grep -RniE "$1" "$SRC" >/tmp/g6a684_hits 2>/dev/null; then echo "FAIL: $2"; cat /tmp/g6a684_hits; fail=1; else echo "PASS: $2 absent"; fi; }

must 'applicationId = "com.parkarsite.g6ainitdiag684"' "$GRADLE" 'fresh package'
must 'versionName = "0.6.8.4"' "$GRADLE" 'version'
must 'App version: 0.6.8.4' "$JAVA" 'runtime version'
must 'private enum Phase { IDLE, TIME_SENT, COUNT_SENT, ENTER_SENT, LATE_OBSERVE, EXIT_SENT, COMPLETE }' "$JAVA" 'time phase'
must 'if(timeWriteAttempted)return;' "$JAVA" 'time no-retry guard'
must 'Time-sync writes allowed: EXACTLY ONE' "$JAVA" 'time write contract'
must 'Time-sync response required as gate: NO' "$JAVA" 'non-gating response contract'
must 'Payload: dynamic 9-byte Cyan-equivalent time/language/timezone payload; values not logged' "$JAVA" 'time privacy contract'
must 'buildCyanTimePayload()' "$JAVA" 'time builder'
must 'frame40(buildCyanTimePayload())' "$JAVA" '0x40 framing'
must 'new byte[]{0x02,0x04}' "$JAVA" 'media count command'
must 'new byte[]{0x02,0x01,0x04,0x01}' "$JAVA" 'enter command'
must 'new byte[]{0x02,0x01,0x09}' "$JAVA" 'exit command'
must 'Credential-capable notification payload bytes logged: NO' "$JAVA" 'credential payload privacy'
must 'Android Wi-Fi Direct API operations: 0' "$JAVA" 'zero wifi'
must 'HTTP requests: 0' "$JAVA" 'zero http'
must 'Physical captures requested by app: 0' "$JAVA" 'zero capture'

bad 'WifiP2p|HttpURLConnection|java\.net\.URL|new URL\(' 'Wi-Fi Direct / HTTP code'
bad 'android.permission.INTERNET|NEARBY_WIFI_DEVICES|ACCESS_FINE_LOCATION|ACCESS_COARSE_LOCATION' 'network/location permissions'
bad 'FileOutputStream|FileWriter|RandomAccessFile|Files\.write|SharedPreferences' 'persistent write code'
bad 'credentialPassword[[:space:]]*=|expectedP2pName[[:space:]]*=' 'credential value storage'
bad 'setRequestMethod|setRequestProperty|/files/' 'remote HTTP endpoint code'

echo "v0.6.8.4 safety audit complete"
exit "$fail"
