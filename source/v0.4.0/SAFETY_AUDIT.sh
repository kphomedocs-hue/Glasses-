#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
SRC="$ROOT/app/src/main"
JAVA="$SRC/java/com/parkarsite/g1p2passociationprobe/MainActivity.java"
MANIFEST="$SRC/AndroidManifest.xml"
fail=0
bad(){ if grep -RniE "$1" "$SRC" >/tmp/g4a_hits 2>/dev/null; then echo "FAIL: $2"; cat /tmp/g4a_hits; fail=1; else echo "PASS: $2 absent"; fi; }
bad 'android\.permission\.INTERNET' 'Internet permission'
bad 'HttpURLConnection|okhttp|java\.net\.Socket|ServerSocket|URL\(' 'HTTP/socket implementation'
bad 'media\.config|/files/|download|deleteFile|FileOutputStream' 'media/file access'
bad '0x02[[:space:]]*,[[:space:]]*0x01[[:space:]]*,[[:space:]]*0x04[[:space:]]*,[[:space:]]*0x02' 'AP-mode payload'
grep -Fq 'new byte[]{0x02,0x01,0x04,0x01}' "$JAVA" || { echo FAIL enter; fail=1; }
grep -Fq 'new byte[]{0x02,0x01,0x09}' "$JAVA" || { echo FAIL exit; fail=1; }
grep -Fq 'equalsIgnoreCase(expectedP2pName)' "$JAVA" || { echo FAIL exact-match; fail=1; }
grep -Fq 'groupOwnerIntent=0' "$JAVA" || { echo FAIL group-owner-intent; fail=1; }
grep -Fq 'cfg.wps.setup=WpsInfo.PBC' "$JAVA" || { echo FAIL WPS; fail=1; }
grep -Fq 'android.permission.NEARBY_WIFI_DEVICES' "$MANIFEST" || { echo FAIL nearby-wifi; fail=1; }
grep -Fq 'android.permission.CHANGE_WIFI_STATE' "$MANIFEST" || { echo FAIL change-wifi; fail=1; }
grep -Fq 'Credential logging/persistence: DISABLED' "$JAVA" || { echo FAIL credential-redaction; fail=1; }
exit "$fail"
