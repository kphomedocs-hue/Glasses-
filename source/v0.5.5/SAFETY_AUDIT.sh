#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
SRC="$ROOT/app/src/main"
JAVA="$SRC/java/com/parkarsite/g1capturevisibilityprobe55/MainActivity.java"
MANIFEST="$SRC/AndroidManifest.xml"
fail=0
bad(){ if grep -RniE "$1" "$SRC" >/tmp/g55_hits 2>/dev/null; then echo "FAIL: $2"; cat /tmp/g55_hits; fail=1; else echo "PASS: $2 absent"; fi; }

grep -Fq 'android.permission.BLUETOOTH_CONNECT' "$MANIFEST" || { echo "FAIL: BLUETOOTH_CONNECT missing"; fail=1; }
grep -Fq 'checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)==PackageManager.PERMISSION_GRANTED' "$JAVA" || { echo "FAIL: runtime BLUETOOTH_CONNECT permission gate missing"; fail=1; }
grep -Fq '@SuppressLint("MissingPermission")' "$JAVA" || { echo "FAIL: targeted lint annotation missing"; fail=1; }
bad 'android.permission.INTERNET' 'Internet permission'
bad 'android.permission.NEARBY_WIFI_DEVICES|android.permission.ACCESS_WIFI_STATE|android.permission.CHANGE_WIFI_STATE' 'Wi-Fi permissions'
bad 'WifiP2p|WifiManager|ConnectivityManager|NetworkRequest|NetworkCapabilities' 'Wi-Fi/network APIs'
bad 'HttpURLConnection|URLConnection|new URL\(|java\.net\.Socket|okhttp|AndroidNetworking' 'HTTP/socket implementation'
bad '/files/|media\.config|vf_list\.txt|log\.list' 'media/catalog endpoint'
bad 'FileOutputStream|Files\.write|RandomAccessFile|deleteFile|\.delete\(' 'file write/delete implementation'
bad '0x02[[:space:]]*,[[:space:]]*0x01[[:space:]]*,[[:space:]]*0x04' 'P2P/AP enter payload'
bad '0x02[[:space:]]*,[[:space:]]*0x01[[:space:]]*,[[:space:]]*0x09' 'transfer exit payload'
bad 'new byte\[\][[:space:]]*\{[[:space:]]*0x02[[:space:]]*,[[:space:]]*0x03' 'P2P IP query payload'
bad 'EditText|ACTION_VIEW' 'arbitrary command/URL input'
bad 'hexPreview|String\.format\([^\n]*%02X[^\n]*frame|raw frame' 'raw notification/frame logging'

grep -Fq 'new byte[]{0x02,0x04}' "$JAVA" || { echo "FAIL: sole media-count payload missing"; fail=1; }
grep -Fq 'if(queryWriteCount>=2)' "$JAVA" || { echo "FAIL: two-write cap missing"; fail=1; }
grep -Fq 'queryWriteCount++' "$JAVA" || { echo "FAIL: query write counter missing"; fail=1; }
grep -Fq 'POST_CAPTURE_OBSERVE_MS = 60000L' "$JAVA" || { echo "FAIL: bounded 60s watch missing"; fail=1; }
grep -Fq 'Arm 60s watch — then capture ONE photo' "$JAVA" || { echo "FAIL: arm-before-capture UI missing"; fail=1; }
grep -Fq 'stage=Stage.POST_CAPTURE_OBSERVE' "$JAVA" || { echo "FAIL: armed watch stage missing"; fail=1; }
grep -Fq 'watch73Count=0' "$JAVA" || { echo "FAIL: armed-window counter reset missing"; fail=1; }
grep -Fq 'if(stage==Stage.POST_CAPTURE_OBSERVE)watch73Count++' "$JAVA" || { echo "FAIL: armed-window event counter missing"; fail=1; }
grep -Fq 'parse73Inventory' "$JAVA" || { echo "FAIL: passive 0x73/0x01 parser missing"; fail=1; }
grep -Fq 'Raw notification frames logged: NO' "$JAVA" || { echo "FAIL: raw-frame redaction declaration missing"; fail=1; }
grep -Fq 'P2P/Wi-Fi/HTTP/media operations: 0' "$JAVA" || { echo "FAIL: zero-network declaration missing"; fail=1; }

write_sites="$(grep -Fo 'writeCharacteristic(localWriter)' "$JAVA" | wc -l | tr -d ' ')"
[[ "$write_sites" == "1" ]] || { echo "FAIL: expected one proprietary write site, found $write_sites"; fail=1; }

payload_sites="$(grep -Fo 'new byte[]{0x02,0x04}' "$JAVA" | wc -l | tr -d ' ')"
[[ "$payload_sites" == "1" ]] || { echo "FAIL: expected one proprietary payload literal, found $payload_sites"; fail=1; }

exit "$fail"
