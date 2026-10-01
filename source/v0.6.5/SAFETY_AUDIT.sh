#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
SRC="$ROOT/app/src/main"
JAVA="$SRC/java/com/parkarsite/g6acapture65/MainActivity.java"
MANIFEST="$SRC/AndroidManifest.xml"
GRADLE="$ROOT/app/build.gradle.kts"
fail=0
must(){ grep -Fq "$1" "$2" || { echo "FAIL: missing $3"; fail=1; }; }
bad(){ if grep -RniE "$1" "$SRC" >/tmp/g6a65_hits 2>/dev/null; then echo "FAIL: $2"; cat /tmp/g6a65_hits; fail=1; else echo "PASS: $2 absent"; fi; }

must 'applicationId = "com.parkarsite.g6acapture65"' "$GRADLE" 'fresh v0.6.5 package'
must 'versionName = "0.6.5"' "$GRADLE" 'version'
must 'BUILD_COMMIT' "$GRADLE" 'build commit field'
must 'BUILD_RUN' "$GRADLE" 'build run field'
must 'BUILD_ATTEMPT' "$GRADLE" 'build attempt field'
must 'android.permission.INTERNET' "$MANIFEST" 'INTERNET permission'
must 'android.permission.NEARBY_WIFI_DEVICES' "$MANIFEST" 'Nearby Wi-Fi permission'

must 'Instruction: DO NOT TAKE A PHOTO until the app explicitly reports ARMED' "$JAVA" 'pre-arm capture prohibition'
must 'Instruction: TAKE EXACTLY ONE PHOTO NOW' "$JAVA" 'single physical capture instruction'
must 'P2P/HTTP operations before +1 confirmation: 0' "$JAVA" 'pre-capture network boundary marker'
must 'Phase.CAPTURE_WATCH' "$JAVA" 'capture-watch state'
must 'inv.imageCount!=baselineImageCount+1' "$JAVA" 'exact +1 passive capture gate'
must 'Ambiguous capture visibility:' "$JAVA" 'ambiguous delta fail-closed'
must 'Active post-capture inventory confirms exact +1 image:' "$JAVA" 'active +1 confirmation'
must 'Exact single new JPG catalog delta:' "$JAVA" 'post-capture catalog gate'
must 'New JPG identity still present:' "$JAVA" 'retention new-item check'
must 'countWriteCount==3&&enterWriteCount==3&&exitWriteCount==3&&catalogGetCount==3&&mediaGetCount==0&&httpRequestCount==3' "$JAVA" 'exact final operation totals'
must 'Media-file GET allowed: 0' "$JAVA" 'zero media GET boundary'
must 'Per-file identity/hash tokens in report: DISABLED' "$JAVA" 'no per-file token reporting'
must 'd.deviceName.equals(expectedP2pName)' "$JAVA" 'case-sensitive exact peer'
must 'Post-exit 0x41 frame observed: IGNORED for exit confirmation' "$JAVA" 'generic exit 0x41 ignored'
must 'Post-exit 0x73/0x01 confirmation: COMPLETE' "$JAVA" 'post-exit evidence'
must 'if(!line.equals(line.trim()))' "$JAVA" 'whitespace identity rejection'
must 'if(!"NONE".equals(out.bom))return out;' "$JAVA" 'BOM rejection'
must 'Build commit: "+BuildConfig.BUILD_COMMIT' "$JAVA" 'runtime build commit'
must 'Build run: "+BuildConfig.BUILD_RUN' "$JAVA" 'runtime build run'
must 'Build attempt: "+BuildConfig.BUILD_ATTEMPT' "$JAVA" 'runtime build attempt'

bad 'MEDIA_PREFIX|startMediaGet|importNewJpg|FileOutputStream|RandomAccessFile' 'media download/import path'
bad 'equalsIgnoreCase\(expectedP2pName\)' 'case-insensitive P2P peer matching'
bad 'substring\(0,12\)|identity tokens \(12-hex|Current exact-ID tokens' 'per-file deterministic token output'
bad 'setRequestMethod\("(POST|PUT|PATCH|DELETE|HEAD)"\)' 'non-GET HTTP method'
bad 'setInstanceFollowRedirects\(true\)|setFollowRedirects\(true\)' 'redirect enablement'
bad 'setRequestProperty\(' 'custom HTTP headers / Range'
bad '/files/log/|vf_list\.txt|storage/sd0/C/DCIM/1' 'alternate catalog endpoint'
bad '0x02[[:space:]]*,[[:space:]]*0x01[[:space:]]*,[[:space:]]*0x04[[:space:]]*,[[:space:]]*0x02' 'AP-mode payload'
bad 'new byte\[\][[:space:]]*\{[[:space:]]*0x02[[:space:]]*,[[:space:]]*0x03' 'P2P-IP query payload'
bad 'java\.net\.Socket|ServerSocket|DatagramSocket|okhttp|AndroidNetworking' 'alternate network stack'
bad 'EditText|ACTION_VIEW' 'arbitrary URL/user endpoint input'

url_count="$(grep -Fo 'new URL(' "$JAVA" | wc -l | tr -d ' ')"
[[ "$url_count" == "1" ]] || { echo "FAIL: expected one URL constructor site, found $url_count"; fail=1; }
get_count="$(grep -Fo 'setRequestMethod("GET")' "$JAVA" | wc -l | tr -d ' ')"
[[ "$get_count" == "1" ]] || { echo "FAIL: expected one GET call site, found $get_count"; fail=1; }
catalog_path_count="$(grep -Fo '"/files/media.config"' "$JAVA" | wc -l | tr -d ' ')"
[[ "$catalog_path_count" == "1" ]] || { echo "FAIL: expected one catalog path literal, found $catalog_path_count"; fail=1; }

echo "v0.6.5 safety audit complete"
exit "$fail"
