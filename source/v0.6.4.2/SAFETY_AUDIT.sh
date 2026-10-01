#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
SRC="$ROOT/app/src/main"
JAVA="$SRC/java/com/parkarsite/g6astability642/MainActivity.java"
MANIFEST="$SRC/AndroidManifest.xml"
GRADLE="$ROOT/app/build.gradle.kts"
fail=0
must(){ grep -Fq "$1" "$2" || { echo "FAIL: missing $3"; fail=1; }; }
bad(){ if grep -RniE "$1" "$SRC" >/tmp/g6a642_hits 2>/dev/null; then echo "FAIL: $2"; cat /tmp/g6a642_hits; fail=1; else echo "PASS: $2 absent"; fi; }

must 'applicationId = "com.parkarsite.g6astability642"' "$GRADLE" 'fresh package ID'
must 'versionName = "0.6.4.2"' "$GRADLE" 'version'
must 'BUILD_COMMIT' "$GRADLE" 'build commit field'
must 'BUILD_RUN' "$GRADLE" 'build run field'
must 'BUILD_ATTEMPT' "$GRADLE" 'build attempt field'
must 'android.permission.INTERNET' "$MANIFEST" 'INTERNET permission'
must 'android.permission.NEARBY_WIFI_DEVICES' "$MANIFEST" 'Nearby Wi-Fi permission'
must 'Instruction: DO NOT TAKE ANY PHOTO during the entire run' "$JAVA" 'no-capture instruction'
must 'Media-file GET allowed: 0' "$JAVA" 'zero media GET boundary'
must 'Per-file identity/hash tokens in report: DISABLED' "$JAVA" 'no per-file token reporting'
must 'd.deviceName.equals(expectedP2pName)' "$JAVA" 'case-sensitive exact peer'
must 'Post-exit 0x73/0x01 confirmation: COMPLETE' "$JAVA" 'post-exit evidence'
must 'eventId==0x01&&phase==Phase.EXIT_SENT&&exitWriteCallbackSucceeded' "$JAVA" 'post-exit event gating'
must 'Post-exit 0x41 frame observed: IGNORED for exit confirmation' "$JAVA" 'generic 0x41 ignored'
must 'private void abortRun(String reason)' "$JAVA" 'central abort'
must 'Abort recovery: attempting the single allow-listed transfer-exit write.' "$JAVA" 'abort exit recovery'
must 'P2P group present after cleanup request:' "$JAVA" 'group absence readback'
must 'if(!line.equals(line.trim()))' "$JAVA" 'exact-line whitespace rejection'
must 'if(!"NONE".equals(out.bom))return out;' "$JAVA" 'BOM rejection'
must 'Snapshot A BLE/catalog media-count parity:' "$JAVA" 'full snapshot A parity'
must 'Snapshot B BLE/catalog full media parity:' "$JAVA" 'full snapshot B parity'
must 'countWriteCount==2&&enterWriteCount==2&&exitWriteCount==2&&catalogGetCount==2&&mediaGetCount==0&&httpRequestCount==2' "$JAVA" 'exact operation totals'
must 'Build commit: "+BuildConfig.BUILD_COMMIT' "$JAVA" 'report build commit'
must 'Build run: "+BuildConfig.BUILD_RUN' "$JAVA" 'report build run'
must 'Build attempt: "+BuildConfig.BUILD_ATTEMPT' "$JAVA" 'report build attempt'

bad 'MEDIA_PREFIX|startMediaGet|importNewJpg|FileOutputStream|RandomAccessFile' 'media download/import path'
bad 'equalsIgnoreCase\(expectedP2pName\)' 'case-insensitive P2P peer matching'
bad 'substring\(0,12\)|JPG identity tokens|Current exact-ID tokens' 'per-file truncated identity tokens'
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
echo "v0.6.4.2 safety audit complete"
exit "$fail"
