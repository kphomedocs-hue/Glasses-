#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
SRC="$ROOT/app/src/main"
JAVA="$SRC/java/com/parkarsite/g1responseprobe/MainActivity.java"
MANIFEST="$SRC/AndroidManifest.xml"
fail=0

check_absent() {
  local pat="$1" label="$2"
  if grep -RniE "$pat" "$SRC" >/tmp/kg1_g2_audit_hits 2>/dev/null; then
    echo "FAIL: $label"
    cat /tmp/kg1_g2_audit_hits
    fail=1
  else
    echo "PASS: $label absent"
  fi
}

check_absent 'writeCharacteristic' 'proprietary characteristic-write API'
check_absent 'de5bf72a-d711-4e47-af26-65e3012a5dc7' 'Cyan proprietary write UUID'
check_absent 'BluetoothLeScanner|startScan|BLUETOOTH_SCAN|ACCESS_FINE_LOCATION' 'BLE scanning/location surface'
check_absent 'createBond|removeBond|ACTION_PAIRING_REQUEST|BluetoothGattServer|openGattServer' 'pairing/GATT-server mutation API'
check_absent 'WifiManager|WifiP2pManager|ConnectivityManager|java\.net|okhttp|HttpURLConnection|Socket' 'Wi-Fi/network code'
check_absent '02[[:space:]]*01[[:space:]]*04[[:space:]]*0[12]|BC[[:space:]]*41' 'Cyan media/control command literals'
check_absent 'android\.permission\.INTERNET|ACCESS_WIFI_STATE|CHANGE_WIFI_STATE|NEARBY_WIFI_DEVICES' 'Internet/Wi-Fi permission'

grep -Fq 'de5bf729-d711-4e47-af26-65e3012a5dc7' "$JAVA"   && echo 'PASS: confirmed Cyan notify UUID is explicit'   || { echo 'FAIL: Cyan notify UUID missing'; fail=1; }

grep -Fq '00002902-0000-1000-8000-00805f9b34fb' "$JAVA"   && echo 'PASS: standard CCCD UUID is explicit'   || { echo 'FAIL: CCCD UUID missing'; fail=1; }

[[ "$(grep -Fc 'setCharacteristicNotification(' "$JAVA")" -eq 1 ]]   && echo 'PASS: exactly one local notification-enable API call'   || { echo 'FAIL: unexpected setCharacteristicNotification call count'; fail=1; }

[[ "$(grep -Fc 'writeDescriptor(' "$JAVA")" -eq 1 ]]   && echo 'PASS: exactly one descriptor-write API call'   || { echo 'FAIL: unexpected writeDescriptor call count'; fail=1; }

grep -Fq 'BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE' "$JAVA"   && echo 'PASS: descriptor payload is standard enable-notification value'   || { echo 'FAIL: standard notification descriptor value missing'; fail=1; }

grep -q 'android.permission.BLUETOOTH_CONNECT' "$MANIFEST"   && echo 'PASS: Bluetooth connect permission present'   || { echo 'FAIL: Bluetooth connect permission missing'; fail=1; }

exit "$fail"
