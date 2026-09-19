#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
SRC="$ROOT/app/src/main"
JAVA="$SRC/java/com/parkarsite/g1mediacatalogprobe/MainActivity.java"
MANIFEST="$SRC/AndroidManifest.xml"
fail=0
bad(){ if grep -RniE "$1" "$SRC" >/tmp/g4b_hits 2>/dev/null; then echo "FAIL: $2"; cat /tmp/g4b_hits; fail=1; else echo "PASS: $2 absent"; fi; }

grep -Fq 'android.permission.INTERNET' "$MANIFEST" || { echo "FAIL: INTERNET permission missing"; fail=1; }
grep -Fq 'android:usesCleartextTraffic="true"' "$MANIFEST" || { echo "FAIL: cleartext local HTTP not explicitly enabled"; fail=1; }
grep -Fq 'android.permission.NEARBY_WIFI_DEVICES' "$MANIFEST" || { echo "FAIL: nearby Wi-Fi permission missing"; fail=1; }

bad 'java\.net\.Socket|ServerSocket|DatagramSocket|okhttp|AndroidNetworking' 'alternate socket/network stack'
bad 'setRequestMethod\("(POST|PUT|PATCH|DELETE|HEAD)"\)' 'non-GET HTTP method'
bad 'setInstanceFollowRedirects\(true\)|setFollowRedirects\(true\)' 'HTTP redirect enablement'
bad 'FileOutputStream|RandomAccessFile|Files\.write|deleteFile|\.delete\(' 'file write/delete implementation'
bad '/files/log/|vf_list\.txt|storage/sd0/C/DCIM/1' 'alternate catalog endpoint'
bad '0x02[[:space:]]*,[[:space:]]*0x01[[:space:]]*,[[:space:]]*0x04[[:space:]]*,[[:space:]]*0x02' 'AP-mode payload'
bad 'new byte\[\][[:space:]]*\{[[:space:]]*0x02[[:space:]]*,[[:space:]]*0x03' 'P2P-IP query payload'
bad 'EditText|ACTION_VIEW' 'arbitrary URL/user endpoint input'
bad 'Arrays\.toString|toHex|bytesToHex' 'raw notification logging helper'

grep -Fq 'new byte[]{0x02,0x01,0x04,0x01}' "$JAVA" || { echo "FAIL: P2P enter missing"; fail=1; }
grep -Fq 'new byte[]{0x02,0x01,0x09}' "$JAVA" || { echo "FAIL: transfer exit missing"; fail=1; }
grep -Fq 'equalsIgnoreCase(expectedP2pName)' "$JAVA" || { echo "FAIL: exact peer match missing"; fail=1; }
grep -Fq 'groupOwnerIntent=0' "$JAVA" || { echo "FAIL: groupOwnerIntent missing"; fail=1; }
grep -Fq 'cfg.wps.setup=WpsInfo.PBC' "$JAVA" || { echo "FAIL: WPS PBC missing"; fail=1; }
grep -Fq 'int eventId=data[6]&255' "$JAVA" || { echo "FAIL: event ID parser missing"; fail=1; }
grep -Fq 'parseIpv4(data,7)' "$JAVA" || { echo "FAIL: passive IPv4 parser missing"; fail=1; }
grep -Fq 'private static final String CATALOG_PATH = "/files/media.config";' "$JAVA" || { echo "FAIL: exact catalog path missing"; fail=1; }
grep -Fq 'private static final int CATALOG_MAX_BYTES = 65536;' "$JAVA" || { echo "FAIL: catalog size cap missing"; fail=1; }
grep -Fq 'setRequestMethod("GET")' "$JAVA" || { echo "FAIL: GET missing"; fail=1; }
grep -Fq 'setInstanceFollowRedirects(false)' "$JAVA" || { echo "FAIL: redirects not disabled"; fail=1; }
grep -Fq 'a==192&&b==168&&c==49&&d>=2&&d<=254' "$JAVA" || { echo "FAIL: confirmed P2P subnet guard missing"; fail=1; }
grep -Fq 'Filename/path values logged: NO' "$JAVA" || { echo "FAIL: filename/path redaction declaration missing"; fail=1; }
grep -Fq 'Media-file GET requests: 0' "$JAVA" || { echo "FAIL: media-file GET declaration missing"; fail=1; }

url_count="$(grep -Fo 'new URL(' "$JAVA" | wc -l | tr -d ' ')"
[[ "$url_count" == "1" ]] || { echo "FAIL: expected exactly one URL constructor, found $url_count"; fail=1; }
get_count="$(grep -Fo 'setRequestMethod("GET")' "$JAVA" | wc -l | tr -d ' ')"
[[ "$get_count" == "1" ]] || { echo "FAIL: expected exactly one GET site, found $get_count"; fail=1; }

exit "$fail"
