#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
SRC="$ROOT/app/src/main"
JAVA="$SRC/java/com/parkarsite/g1p2passociationprobe/MainActivity.java"
MANIFEST="$SRC/AndroidManifest.xml"
fail=0
bad(){ if grep -RniE "$1" "$SRC" >/tmp/g4a2_hits 2>/dev/null; then echo "FAIL: $2"; cat /tmp/g4a2_hits; fail=1; else echo "PASS: $2 absent"; fi; }
bad 'android\.permission\.INTERNET' 'Internet permission'
bad 'HttpURLConnection|okhttp|java\.net\.Socket|ServerSocket|URL\(' 'HTTP/socket implementation'
bad 'media\.config|/files/|download|deleteFile|FileOutputStream' 'media/file access'
bad '0x02[[:space:]]*,[[:space:]]*0x01[[:space:]]*,[[:space:]]*0x04[[:space:]]*,[[:space:]]*0x02' 'AP-mode payload'
bad 'new byte\[\][[:space:]]*\{[[:space:]]*0x02[[:space:]]*,[[:space:]]*0x03' 'P2P-IP query payload'
bad 'Arrays\.toString|toHex|bytesToHex' 'raw notification logging helper'
grep -Fq 'new byte[]{0x02,0x01,0x04,0x01}' "$JAVA" || { echo FAIL enter; fail=1; }
grep -Fq 'new byte[]{0x02,0x01,0x09}' "$JAVA" || { echo FAIL exit; fail=1; }
grep -Fq 'equalsIgnoreCase(expectedP2pName)' "$JAVA" || { echo FAIL exact-match; fail=1; }
grep -Fq 'groupOwnerIntent=0' "$JAVA" || { echo FAIL group-owner-intent; fail=1; }
grep -Fq 'cfg.wps.setup=WpsInfo.PBC' "$JAVA" || { echo FAIL WPS; fail=1; }
grep -Fq 'int eventId=data[6]&255' "$JAVA" || { echo FAIL event-id-parser; fail=1; }
grep -Fq 'parseIpv4(data,7)' "$JAVA" || { echo FAIL ipv4-index; fail=1; }
grep -Fq 'Async 0x73 event ID: 0x%02X' "$JAVA" || { echo FAIL sanitized-event-id-log; fail=1; }
grep -Fq 'Credential logging/persistence: DISABLED' "$JAVA" || { echo FAIL credential-redaction; fail=1; }
grep -Fq 'android.permission.NEARBY_WIFI_DEVICES' "$MANIFEST" || { echo FAIL nearby-wifi; fail=1; }
grep -Fq 'android.permission.CHANGE_WIFI_STATE' "$MANIFEST" || { echo FAIL change-wifi; fail=1; }
exit "$fail"
