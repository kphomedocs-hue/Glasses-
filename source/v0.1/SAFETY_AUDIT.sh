#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
SRC="$ROOT/app/src/main"
fail=0
check_absent() {
  local pat="$1" label="$2"
  if grep -RniE "$pat" "$SRC" >/tmp/kg1_audit_hits 2>/dev/null; then
    echo "FAIL: $label"
    cat /tmp/kg1_audit_hits
    fail=1
  else
    echo "PASS: $label absent"
  fi
}
check_absent 'writeCharacteristic|writeDescriptor|setCharacteristicNotification' 'BLE write/notification API'
check_absent 'WifiManager|WifiP2pManager|ConnectivityManager|java\.net|okhttp|HttpURLConnection|Socket' 'Wi-Fi/network code'
check_absent 'BC[[:space:]]*41|02[[:space:]]*01[[:space:]]*04[[:space:]]*01' 'Cyan control/media command literals'
check_absent 'android\.permission\.INTERNET|ACCESS_WIFI_STATE|CHANGE_WIFI_STATE|NEARBY_WIFI_DEVICES' 'Internet/Wi-Fi permission'
if grep -q 'android.permission.BLUETOOTH_SCAN' "$SRC/AndroidManifest.xml" && grep -q 'android.permission.BLUETOOTH_CONNECT' "$SRC/AndroidManifest.xml"; then
  echo 'PASS: required modern Bluetooth permissions present'
else
  echo 'FAIL: required Bluetooth permissions missing'; fail=1
fi
exit "$fail"
