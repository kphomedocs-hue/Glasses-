#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
SRC="$ROOT/app/src/main"
JAVA="$SRC/java/com/parkarsite/g6aobserver67/MainActivity.java"
GRADLE="$ROOT/app/build.gradle.kts"
MANIFEST="$SRC/AndroidManifest.xml"
fail=0
must(){ grep -Fq "$1" "$2" || { echo "FAIL: missing $3"; fail=1; }; }
bad(){ if grep -RniE "$1" "$SRC" >/tmp/g6a67_hits 2>/dev/null; then echo "FAIL: $2"; cat /tmp/g6a67_hits; fail=1; else echo "PASS: $2 absent"; fi; }

must 'applicationId = "com.parkarsite.g6aobserver67"' "$GRADLE" 'fresh package ID'
must 'versionName = "0.6.7"' "$GRADLE" 'version'
must 'BUILD_COMMIT' "$GRADLE" 'build commit'
must 'BUILD_RUN' "$GRADLE" 'build run'
must 'BUILD_ATTEMPT' "$GRADLE" 'build attempt'
must 'android.permission.INTERNET' "$MANIFEST" 'INTERNET permission'

must 'boolean observationalPostGetExit=stage==Stage.PRE_GET_RETENTION&&mediaValidated&&mediaGetCount==1;' "$JAVA" 'strict post-GET observation gate'
must 'Post-GET exit inventory observation mode: TRUE' "$JAVA" 'observation marker'
must 'Post-GET exit inventory images:' "$JAVA" 'exact observed image count logging'
must 'Post-GET exit inventory videos:' "$JAVA" 'exact observed video count logging'
must 'Post-GET exit inventory recordings:' "$JAVA" 'exact observed recording count logging'
must 'Post-GET exit config file type:' "$JAVA" 'exact observed config logging'
must 'Post-GET exit image delta vs pre-GET snapshot:' "$JAVA" 'exit delta logging'
must 'outside the post-GET observation stage; exit separation not proven.' "$JAVA" 'all other mismatch remains fatal'
must 'postGetExitInventoryObserved' "$JAVA" 'post-GET observation state'
must 'Fresh reconnect inventory equals pre-GET/post-capture counts:' "$JAVA" 'fresh inventory classification'
must 'Downloaded new JPG identity still present after fresh reconnect:' "$JAVA" 'fresh exact JPG presence'
must 'TRANSIENT EXIT-TIME INVENTORY TRANSITION' "$JAVA" 'transient classification'
must 'PERSISTENT CHANGE — DOWNLOADED NEW JPG ABSENT' "$JAVA" 'persistent deletion classification'
must 'countWriteCount==4&&enterWriteCount==4&&exitWriteCount==4&&catalogGetCount==4&&mediaGetCount==1&&httpRequestCount==5' "$JAVA" 'exact operation totals'

must 'if(mediaGetCount!=0)' "$JAVA" 'single media GET guard'
must 'if(!preGetRetentionExact)' "$JAVA" 'pre-GET retention gate'
must 'OpaqueIdentity.sha256(candidate).equals(newJpgHash)' "$JAVA" 'exact media identity gate'
must 'MEDIA_MAX_BYTES=33554432' "$JAVA" 'media cap'
must 'Persistent import created: NO' "$JAVA" 'no persistent import'
must 'Persistent ledger updated: NO' "$JAVA" 'no ledger'

bad 'equalsIgnoreCase\(expectedP2pName\)' 'case-insensitive peer match'
bad 'setInstanceFollowRedirects\(true\)|setFollowRedirects\(true\)' 'redirect enablement'
bad 'setRequestProperty\(' 'custom HTTP headers / Range'
bad 'SharedPreferences|RoomDatabase|SQLiteDatabase|MediaStore' 'persistent state APIs'
bad 'setRequestMethod\("(POST|PUT|PATCH|DELETE|HEAD)"\)' 'non-GET HTTP'
bad '/files/log/|vf_list\.txt|storage/sd0/C/DCIM/1' 'alternate endpoint'

url_count="$(grep -Fo 'new URL(' "$JAVA" | wc -l | tr -d ' ')"
[[ "$url_count" == "2" ]] || { echo "FAIL: expected 2 URL constructors, found $url_count"; fail=1; }
get_count="$(grep -Fo 'setRequestMethod("GET")' "$JAVA" | wc -l | tr -d ' ')"
[[ "$get_count" == "2" ]] || { echo "FAIL: expected 2 GET callsites, found $get_count"; fail=1; }

echo "v0.6.7 safety audit complete"
exit "$fail"
