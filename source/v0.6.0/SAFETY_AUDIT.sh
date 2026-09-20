#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
SRC="$ROOT/app/src/main"
JAVA="$SRC/java/com/parkarsite/g6aimport60/MainActivity.java"
CORE="$SRC/java/com/parkarsite/g6a"
MANIFEST="$SRC/AndroidManifest.xml"
fail=0
bad(){ if grep -RniE "$1" "$SRC" >/tmp/g6a_hits 2>/dev/null; then echo "FAIL: $2"; cat /tmp/g6a_hits; fail=1; else echo "PASS: $2 absent"; fi; }

grep -Fq 'android.permission.INTERNET' "$MANIFEST" || { echo "FAIL: INTERNET missing"; fail=1; }
grep -Fq 'android.permission.NEARBY_WIFI_DEVICES' "$MANIFEST" || { echo "FAIL: nearby Wi-Fi missing"; fail=1; }
bad 'java\.net\.Socket|ServerSocket|DatagramSocket|okhttp|AndroidNetworking' 'alternate socket/network stack'
bad 'setRequestMethod\("(POST|PUT|PATCH|DELETE|HEAD)"\)' 'non-GET HTTP method'
bad 'setInstanceFollowRedirects\(true\)|setFollowRedirects\(true\)' 'redirect enablement'
bad 'setRequestProperty\(' 'custom HTTP request headers / Range'
bad '/files/log/|vf_list\.txt|storage/sd0/C/DCIM/1' 'alternate catalog endpoint'
bad '0x02[[:space:]]*,[[:space:]]*0x01[[:space:]]*,[[:space:]]*0x04[[:space:]]*,[[:space:]]*0x02' 'AP-mode payload'
bad 'new byte\[\][[:space:]]*\{[[:space:]]*0x02[[:space:]]*,[[:space:]]*0x03' 'P2P-IP query payload'
bad 'EditText|ACTION_VIEW' 'arbitrary URL/user endpoint input'

grep -Fq 'new File(getFilesDir(),"g6a_imports")' "$JAVA" || { echo "FAIL: app-private persistent root missing"; fail=1; }
grep -Fq 'OpaqueIdentity.sha256(candidate)' "$JAVA" || { echo "FAIL: opaque dedup precheck missing"; fail=1; }
grep -Fq 'importLedger.contains(opaqueId)' "$JAVA" || { echo "FAIL: persistent dedup guard missing"; fail=1; }
grep -Fq 'importCoordinator.importNewJpg' "$JAVA" || { echo "FAIL: coordinator integration missing"; fail=1; }
grep -Fq 'Ledger entry delta: +1' "$JAVA" || { echo "FAIL: ledger commit evidence missing"; fail=1; }
grep -Fq 'Remote filename/path persisted: NO' "$JAVA" || { echo "FAIL: privacy marker missing"; fail=1; }
grep -Fq 'new byte[]{0x02,0x04}' "$JAVA" || { echo "FAIL: media-count payload missing"; fail=1; }
grep -Fq 'new byte[]{0x02,0x01,0x04,0x01}' "$JAVA" || { echo "FAIL: P2P enter missing"; fail=1; }
grep -Fq 'new byte[]{0x02,0x01,0x09}' "$JAVA" || { echo "FAIL: transfer exit missing"; fail=1; }

url_count="$(grep -Fo 'new URL(' "$JAVA" | wc -l | tr -d ' ')"
[[ "$url_count" == "2" ]] || { echo "FAIL: expected exactly two URL constructor sites, found $url_count"; fail=1; }
get_count="$(grep -Fo 'setRequestMethod("GET")' "$JAVA" | wc -l | tr -d ' ')"
[[ "$get_count" == "2" ]] || { echo "FAIL: expected exactly two GET call sites, found $get_count"; fail=1; }
media_prefix_count="$(grep -Fo 'MEDIA_PREFIX+candidate' "$JAVA" | wc -l | tr -d ' ')"
[[ "$media_prefix_count" == "1" ]] || { echo "FAIL: expected exactly one media URL construction, found $media_prefix_count"; fail=1; }

grep -RniE 'http://|https://' "$CORE" && { echo "FAIL: persistence core must remain transport-free"; fail=1; } || true
grep -RniE 'Bluetooth|WifiP2p|HttpURLConnection|android\.' "$CORE" && { echo "FAIL: persistence core contains transport/android dependency"; fail=1; } || true

exit "$fail"
