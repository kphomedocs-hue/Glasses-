#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
SRC="$ROOT/app/src/main"
JAVA="$SRC/java/com/parkarsite/g6aget66/MainActivity.java"
MANIFEST="$SRC/AndroidManifest.xml"
GRADLE="$ROOT/app/build.gradle.kts"
fail=0
must(){ grep -Fq "$1" "$2" || { echo "FAIL: missing $3"; fail=1; }; }
bad(){ if grep -RniE "$1" "$SRC" >/tmp/g6a66_hits 2>/dev/null; then echo "FAIL: $2"; cat /tmp/g6a66_hits; fail=1; else echo "PASS: $2 absent"; fi; }

must 'applicationId = "com.parkarsite.g6aget66"' "$GRADLE" 'fresh package ID'
must 'versionName = "0.6.6"' "$GRADLE" 'version'
must 'BUILD_COMMIT' "$GRADLE" 'build commit field'
must 'BUILD_RUN' "$GRADLE" 'build run field'
must 'BUILD_ATTEMPT' "$GRADLE" 'build attempt field'
must 'android.permission.INTERNET' "$MANIFEST" 'INTERNET permission'
must 'android.permission.NEARBY_WIFI_DEVICES' "$MANIFEST" 'Nearby Wi-Fi permission'

must 'Media GETs allowed: EXACTLY ONE, only after pre-GET retention is proven' "$JAVA" 'one media GET boundary'
must 'stage!=Stage.PRE_GET_RETENTION||phase!=Phase.CATALOG_GET' "$JAVA" 'media GET stage guard'
must 'if(mediaGetCount!=0)' "$JAVA" 'single media GET guard'
must 'if(!preGetRetentionExact)' "$JAVA" 'pre-GET retention gate'
must 'OpaqueIdentity.sha256(candidate).equals(newJpgHash)' "$JAVA" 'exact new JPG identity guard'
must 'MEDIA_MAX_BYTES=33554432' "$JAVA" '32 MiB media cap'
must 'JPEG SOI signature: PASS' "$JAVA" 'JPEG SOI validation'
must 'JPEG EOI signature: PASS' "$JAVA" 'JPEG EOI validation'
must 'Temporary file cleanup after validation: PASS' "$JAVA" 'temp cleanup proof'
must 'Persistent import created: NO' "$JAVA" 'no persistent import'
must 'Persistent ledger updated: NO' "$JAVA" 'no ledger update'
must 'Downloaded new JPG identity still present remotely:' "$JAVA" 'post-GET retention check'
must 'countWriteCount==4&&enterWriteCount==4&&exitWriteCount==4&&catalogGetCount==4&&mediaGetCount==1&&httpRequestCount==5' "$JAVA" 'exact operation totals'
must 'd.deviceName.equals(expectedP2pName)' "$JAVA" 'case-sensitive peer match'
must 'Post-exit 0x41 frame observed: IGNORED for exit confirmation' "$JAVA" 'generic 0x41 ignored'
must 'Post-exit 0x73/0x01 confirmation: COMPLETE' "$JAVA" 'post-exit proof'
must 'if(!line.equals(line.trim()))' "$JAVA" 'whitespace identity rejection'
must 'if(!"NONE".equals(out.bom))return out;' "$JAVA" 'BOM rejection'

bad 'equalsIgnoreCase\(expectedP2pName\)' 'case-insensitive P2P peer match'
bad 'setRequestMethod\("(POST|PUT|PATCH|DELETE|HEAD)"\)' 'non-GET HTTP method'
bad 'setInstanceFollowRedirects\(true\)|setFollowRedirects\(true\)' 'redirect enablement'
bad 'setRequestProperty\(' 'custom HTTP headers / Range'
bad '/files/log/|vf_list\.txt|storage/sd0/C/DCIM/1' 'alternate endpoint'
bad 'SharedPreferences|RoomDatabase|SQLiteDatabase|MediaStore|DocumentsContract' 'persistent import/ledger APIs'
bad 'substring\(0,12\)|identity tokens \(12-hex|Current exact-ID tokens' 'per-file deterministic token output'

url_count="$(grep -Fo 'new URL(' "$JAVA" | wc -l | tr -d ' ')"
[[ "$url_count" == "2" ]] || { echo "FAIL: expected exactly 2 URL constructor sites (catalog + media), found $url_count"; fail=1; }
get_count="$(grep -Fo 'setRequestMethod("GET")' "$JAVA" | wc -l | tr -d ' ')"
[[ "$get_count" == "2" ]] || { echo "FAIL: expected exactly 2 GET callsites (catalog + media), found $get_count"; fail=1; }
media_prefix_count="$(grep -Fo 'MEDIA_PREFIX+candidate' "$JAVA" | wc -l | tr -d ' ')"
[[ "$media_prefix_count" == "1" ]] || { echo "FAIL: expected exactly one media URL construction path, found $media_prefix_count"; fail=1; }

echo "v0.6.6 safety audit complete"
exit "$fail"
