#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
SRC="$ROOT/app/src/main"
JAVA="$SRC/java/com/parkarsite/g6ap2pdiag685/MainActivity.java"
MANIFEST="$SRC/AndroidManifest.xml"
fail=0
HITS="$(mktemp)"
trap 'rm -f "$HITS"' EXIT
bad(){
  if grep -REn -- "$1" "$SRC" >"$HITS"; then
    echo "FAIL: $2"; cat "$HITS"; fail=1
  else
    rc=$?
    if [ "$rc" -eq 1 ]; then echo "PASS: $2 absent"; else echo "FAIL: $2 scan error ($rc)"; fail=1; fi
  fi
}
need(){ if grep -Fq -- "$1" "$2"; then echo "PASS: $3"; else echo "FAIL: $3"; fail=1; fi; }
bad 'android\.permission\.INTERNET' 'Internet permission'
bad 'HttpURLConnection|okhttp|java\.net\.Socket|ServerSocket|URL\(' 'HTTP/socket implementation'
bad 'media\.config|/files/|download|deleteFile|FileOutputStream' 'media/file access'
bad '0x02[[:space:]]*,[[:space:]]*0x01[[:space:]]*,[[:space:]]*0x04[[:space:]]*,[[:space:]]*0x02' 'AP-mode payload'
bad '0x02[[:space:]]*,[[:space:]]*0x01[[:space:]]*,[[:space:]]*0x0[Ff]' 'P2P reset payload'
bad 'groupOwnerIntent' 'forced group-owner intent'
bad 'parseTransferCredentials|credentialPassword|expectedP2pName|parseIpv4|groupOwnerAddress' 'credential/IP parsing or network-address reporting'
need 'new byte[]{0x02,0x01,0x04,0x01}' "$JAVA" 'one ENTER payload'
need 'new byte[]{0x02,0x01,0x09}' "$JAVA" 'one EXIT payload'
need 'P2P_DISCOVERY_TIMEOUT_MS = 16000L' "$JAVA" 'Cyan 16-second discovery bound'
need 'cfg.wps.setup=WpsInfo.PBC' "$JAVA" 'WPS PBC'
need 'requestGroupInfo' "$JAVA" 'group preflight and verification'
need 'cancelConnect' "$JAVA" 'pending-connect cleanup'
need 'android.permission.NEARBY_WIFI_DEVICES' "$MANIFEST" 'Nearby Wi-Fi permission'
need 'android.permission.CHANGE_WIFI_STATE' "$MANIFEST" 'Wi-Fi Direct permission'
exit "$fail"
