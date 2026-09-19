#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
SRC="$ROOT/app/src/main"
JAVA="$SRC/java/com/parkarsite/g1singlephotoprobe51/MainActivity.java"
MANIFEST="$SRC/AndroidManifest.xml"
fail=0
bad(){ if grep -RniE "$1" "$SRC" >/tmp/g51_hits 2>/dev/null; then echo "FAIL: $2"; cat /tmp/g51_hits; fail=1; else echo "PASS: $2 absent"; fi; }

grep -Fq 'android.permission.INTERNET' "$MANIFEST" || { echo "FAIL: INTERNET missing"; fail=1; }
grep -Fq 'android:usesCleartextTraffic="true"' "$MANIFEST" || { echo "FAIL: cleartext local HTTP missing"; fail=1; }
grep -Fq 'android.permission.NEARBY_WIFI_DEVICES' "$MANIFEST" || { echo "FAIL: nearby Wi-Fi missing"; fail=1; }

bad 'java\.net\.Socket|ServerSocket|DatagramSocket|okhttp|AndroidNetworking' 'alternate socket/network stack'
bad 'setRequestMethod\("(POST|PUT|PATCH|DELETE|HEAD)"\)' 'non-GET HTTP method'
bad 'setInstanceFollowRedirects\(true\)|setFollowRedirects\(true\)' 'redirect enablement'
bad 'setRequestProperty\(' 'custom HTTP request headers / Range'
bad 'FileInputStream|RandomAccessFile|Files\.write|deleteFile|\.delete\(' 'unexpected local file mutation helper'
bad '/files/log/|vf_list\.txt|storage/sd0/C/DCIM/1' 'alternate catalog endpoint'
bad 'JSONObject|JSONArray|JSONTokener|PtPFileModel|file_list' 'wrong JSON catalog branch'
bad '0x02[[:space:]]*,[[:space:]]*0x01[[:space:]]*,[[:space:]]*0x04[[:space:]]*,[[:space:]]*0x02' 'AP-mode payload'
bad 'new byte\[\][[:space:]]*\{[[:space:]]*0x02[[:space:]]*,[[:space:]]*0x03' 'P2P-IP query payload'
bad 'EditText|ACTION_VIEW' 'arbitrary URL/user endpoint input'
bad 'MessageDigest|SHA-256|sha256\(' 'catalog/media fingerprint logging'
bad 'append\([^\n]*\+[[:space:]]*(candidate|safeCandidate|trimmed|line)([^A-Za-z0-9_]|$)' 'private catalog/media value logging'
bad 'append\([^\n]*getAbsolutePath|append\([^\n]*localFile|append\([^\n]*getPath' 'local path logging'
bad 'Thread\.sleep|while[[:space:]]*\(true\).*GET|for[[:space:]]*\([^\n]*retry' 'retry loop'

grep -Fq 'new byte[]{0x02,0x04}' "$JAVA" || { echo "FAIL: media-count payload missing"; fail=1; }
grep -Fq 'new byte[]{0x02,0x01,0x04,0x01}' "$JAVA" || { echo "FAIL: P2P enter missing"; fail=1; }
grep -Fq 'new byte[]{0x02,0x01,0x09}' "$JAVA" || { echo "FAIL: transfer exit missing"; fail=1; }
grep -Fq 'phase=Phase.COUNT_SENT' "$JAVA" || { echo "FAIL: count phase missing"; fail=1; }
grep -Fq 'countWriteCount++' "$JAVA" || { echo "FAIL: count write bound missing"; fail=1; }
grep -Fq 'currentConfigFileType!=1||currentOnlySupportApImport' "$JAVA" || { echo "FAIL: P2P branch guard missing"; fail=1; }
grep -Fq 'private static final long CATALOG_READY_DELAY_MS = 1000L;' "$JAVA" || { echo "FAIL: exact 1000 ms delay constant missing"; fail=1; }
grep -Fq 'handler.postDelayed(catalogDelayTask,CATALOG_READY_DELAY_MS)' "$JAVA" || { echo "FAIL: exact catalog delay invocation missing"; fail=1; }
grep -Fq 'phase=Phase.CATALOG_DELAY' "$JAVA" || { echo "FAIL: catalog delay phase missing"; fail=1; }
grep -Fq 'equalsIgnoreCase(expectedP2pName)' "$JAVA" || { echo "FAIL: exact peer match missing"; fail=1; }
grep -Fq 'cfg.wps.setup=WpsInfo.PBC' "$JAVA" || { echo "FAIL: WPS PBC missing"; fail=1; }
grep -Fq 'parseIpv4(data,7)' "$JAVA" || { echo "FAIL: passive IP parser missing"; fail=1; }
grep -Fq 'private static final String CATALOG_PATH = "/files/media.config";' "$JAVA" || { echo "FAIL: exact catalog path missing"; fail=1; }
grep -Fq 'private static final String MEDIA_PREFIX = "/files/";' "$JAVA" || { echo "FAIL: exact media prefix missing"; fail=1; }
grep -Fq 'private static final int MEDIA_MAX_BYTES = 33554432;' "$JAVA" || { echo "FAIL: 32 MiB media cap missing"; fail=1; }
grep -Fq 'deltaCount!=1' "$JAVA" || { echo "FAIL: exact-one delta guard missing"; fail=1; }
grep -Fq 'summary.safeEntries.containsAll(baselineCatalog)' "$JAVA" || { echo "FAIL: baseline containment guard missing"; fail=1; }
grep -Fq '".jpg".equals(extensionOnly(candidate))' "$JAVA" || { echo "FAIL: JPG-only guard missing"; fail=1; }
grep -Fq 'isStrictMediaRelativePath(candidate)' "$JAVA" || { echo "FAIL: strict candidate guard missing"; fail=1; }
grep -Fq 'getCacheDir()' "$JAVA" || { echo "FAIL: app-private cache destination missing"; fail=1; }
grep -Fq 'new FileOutputStream(file)' "$JAVA" || { echo "FAIL: streaming output missing"; fail=1; }
grep -Fq 'm.first[0]==0xFF&&m.first[1]==0xD8&&m.first[2]==0xFF' "$JAVA" || { echo "FAIL: JPEG SOI check missing"; fail=1; }
grep -Fq 'prev==0xFF&&last==0xD9' "$JAVA" || { echo "FAIL: JPEG EOI check missing"; fail=1; }

url_count="$(grep -Fo 'new URL(' "$JAVA" | wc -l | tr -d ' ')"
[[ "$url_count" == "2" ]] || { echo "FAIL: expected exactly two URL constructor sites, found $url_count"; fail=1; }
get_count="$(grep -Fo 'setRequestMethod("GET")' "$JAVA" | wc -l | tr -d ' ')"
[[ "$get_count" == "2" ]] || { echo "FAIL: expected exactly two GET call sites, found $get_count"; fail=1; }
fos_count="$(grep -Fo 'new FileOutputStream(' "$JAVA" | wc -l | tr -d ' ')"
[[ "$fos_count" == "1" ]] || { echo "FAIL: expected exactly one file-output site, found $fos_count"; fail=1; }
write_count="$(grep -Eo 'writeFrame\((x|gatt),frame41\(' "$JAVA" | wc -l | tr -d ' ')"
[[ "$write_count" == "3" ]] || { echo "FAIL: expected exactly 3 proprietary write call sites (count/enter/exit), found $write_count"; fail=1; }

exit "$fail"
