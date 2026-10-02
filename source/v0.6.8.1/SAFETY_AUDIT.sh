#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
SRC="$ROOT/app/src/main"
JAVA="$SRC/java/com/parkarsite/g6acreddiag681/MainActivity.java"
GRADLE="$ROOT/app/build.gradle.kts"
MANIFEST="$SRC/AndroidManifest.xml"
fail=0
must(){ grep -Fq "$1" "$2" || { echo "FAIL: missing $3"; fail=1; }; }
bad(){ if grep -RniE "$1" "$SRC" >/tmp/g6a681_hits 2>/dev/null; then echo "FAIL: $2"; cat /tmp/g6a681_hits; fail=1; else echo "PASS: $2 absent"; fi; }

must 'applicationId = "com.parkarsite.g6acreddiag681"' "$GRADLE" 'fresh package'
must 'versionName = "0.6.8.1"' "$GRADLE" 'version'
must 'App version: 0.6.8.1' "$JAVA" 'runtime report version'
must 'NORMAL_CREDENTIAL_WINDOW_MS=10000L' "$JAVA" 'normal 10-second window'
must 'LATE_OBSERVE_WINDOW_MS=10000L' "$JAVA" 'bounded late window'
must 'new byte[]{0x02,0x04}' "$JAVA" 'media count command'
must 'new byte[]{0x02,0x01,0x04,0x01}' "$JAVA" 'enter command'
must 'new byte[]{0x02,0x01,0x09}' "$JAVA" 'exit command'
must 'Credential values decoded/logged/persisted: NO' "$JAVA" 'credential privacy report'
must 'Android Wi-Fi Direct API operations: 0' "$JAVA" 'zero wifi report'
must 'HTTP requests: 0' "$JAVA" 'zero http report'
must 'Media-file GET requests: 0' "$JAVA" 'zero media report'
must 'Physical captures requested by app: 0' "$JAVA" 'zero capture report'

bad 'WifiP2p|HttpURLConnection|java\.net\.URL|new URL\(' 'Wi-Fi Direct / HTTP code'
bad 'android.permission.INTERNET|NEARBY_WIFI_DEVICES|ACCESS_FINE_LOCATION|ACCESS_COARSE_LOCATION' 'unneeded network/location permissions'
bad 'FileOutputStream|FileWriter|RandomAccessFile|Files\.write|SharedPreferences' 'persistent write code'
bad 'Arrays\.toString\(data\)|Base64|credentialPassword[[:space:]]*=|expectedP2pName[[:space:]]*=' 'credential/raw payload exposure'
bad 'setRequestMethod|setRequestProperty|/files/' 'remote HTTP endpoint code'

echo "v0.6.8.1 safety audit complete"
exit "$fail"
