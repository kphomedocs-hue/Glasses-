#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
SRC="$ROOT/app/src/main"
JAVA="$SRC/java/com/parkarsite/g6astability64/MainActivity.java"
MANIFEST="$SRC/AndroidManifest.xml"
fail=0
must(){ grep -Fq "$1" "$2" || { echo "FAIL: missing $3"; fail=1; }; }
bad(){ if grep -RniE "$1" "$SRC" >/tmp/g6a64_hits 2>/dev/null; then echo "FAIL: $2"; cat /tmp/g6a64_hits; fail=1; else echo "PASS: $2 absent"; fi; }

must 'android.permission.INTERNET' "$MANIFEST" 'INTERNET permission'
must 'android.permission.NEARBY_WIFI_DEVICES' "$MANIFEST" 'Nearby Wi-Fi permission'
must 'private static final long STABILITY_QUIET_MS=30000L;' "$JAVA" '30-second quiet interval'
must 'Instruction: DO NOT TAKE ANY PHOTO during the entire run' "$JAVA" 'no-capture instruction'
must 'Media-file GET allowed: 0' "$JAVA" 'zero-media boundary'
must 'Catalog identity comparison: OPAQUE SHA-256 IN MEMORY ONLY' "$JAVA" 'opaque comparison boundary'
must 'JPG opaque identity sets exactly equal:' "$JAVA" 'JPG set comparison'
must 'Full safe catalog opaque identity sets exactly equal:' "$JAVA" 'full set comparison'
must 'G6A NO-CAPTURE STABILITY DIAGNOSTIC:' "$JAVA" 'diagnostic classification'
must 'new byte[]{0x02,0x04}' "$JAVA" 'media-count payload'
must 'new byte[]{0x02,0x01,0x04,0x01}' "$JAVA" 'P2P enter payload'
must 'new byte[]{0x02,0x01,0x09}' "$JAVA" 'transfer exit payload'

bad 'MEDIA_PREFIX|startMediaGet|importNewJpg|FileOutputStream|RandomAccessFile' 'media download/import path'
bad 'setRequestMethod\("(POST|PUT|PATCH|DELETE|HEAD)"\)' 'non-GET HTTP method'
bad 'setInstanceFollowRedirects\(true\)|setFollowRedirects\(true\)' 'redirect enablement'
bad 'setRequestProperty\(' 'custom HTTP headers / Range'
bad '/files/log/|vf_list\.txt|storage/sd0/C/DCIM/1' 'alternate catalog endpoint'
bad '0x02[[:space:]]*,[[:space:]]*0x01[[:space:]]*,[[:space:]]*0x04[[:space:]]*,[[:space:]]*0x02' 'AP-mode payload'
bad 'new byte\[\][[:space:]]*\{[[:space:]]*0x02[[:space:]]*,[[:space:]]*0x03' 'P2P-IP query payload'
bad 'java\.net\.Socket|ServerSocket|DatagramSocket|okhttp|AndroidNetworking' 'alternate network stack'
bad 'EditText|ACTION_VIEW' 'arbitrary URL/user endpoint input'

url_count="$(grep -Fo 'new URL(' "$JAVA" | wc -l | tr -d ' ')"
[[ "$url_count" == "1" ]] || { echo "FAIL: expected exactly one URL constructor site, found $url_count"; fail=1; }
get_count="$(grep -Fo 'setRequestMethod("GET")' "$JAVA" | wc -l | tr -d ' ')"
[[ "$get_count" == "1" ]] || { echo "FAIL: expected exactly one GET call site, found $get_count"; fail=1; }
catalog_path_count="$(grep -Fo '"/files/media.config"' "$JAVA" | wc -l | tr -d ' ')"
[[ "$catalog_path_count" == "1" ]] || { echo "FAIL: expected exactly one catalog path literal, found $catalog_path_count"; fail=1; }

echo "v0.6.4 safety audit complete"
exit "$fail"
