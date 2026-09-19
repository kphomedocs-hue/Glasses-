#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
SRC="$ROOT/app/src/main"
JAVA="$SRC/java/com/parkarsite/g1initprobe/MainActivity.java"
MANIFEST="$SRC/AndroidManifest.xml"
fail=0

check_absent() {
  local pat="$1" label="$2"
  if grep -RniE "$pat" "$SRC" >/tmp/kg1_g2c_audit_hits 2>/dev/null; then
    echo "FAIL: $label"
    cat /tmp/kg1_g2c_audit_hits
    fail=1
  else
    echo "PASS: $label absent"
  fi
}

check_absent 'BluetoothLeScanner|startScan|BLUETOOTH_SCAN|ACCESS_FINE_LOCATION' 'BLE scanning/location surface'
check_absent 'createBond|removeBond|ACTION_PAIRING_REQUEST|BluetoothGattServer|openGattServer' 'pairing/GATT-server mutation API'
check_absent 'WifiManager|WifiP2pManager|ConnectivityManager|java\.net|okhttp|HttpURLConnection|Socket' 'Wi-Fi/network code'
check_absent '02[[:space:]]*,?[[:space:]]*01[[:space:]]*,?[[:space:]]*04[[:space:]]*,?[[:space:]]*0[12]' 'media-mode payload'
check_absent 'frame\[1\][[:space:]]*=[[:space:]]*0x41|Command:[[:space:]]*0x41|command ID:[[:space:]]*0x41' 'glasses-control command'
check_absent 'android\.permission\.INTERNET|ACCESS_WIFI_STATE|CHANGE_WIFI_STATE|NEARBY_WIFI_DEVICES' 'Internet/Wi-Fi permission'

grep -Fq 'de5bf729-d711-4e47-af26-65e3012a5dc7' "$JAVA"   && echo 'PASS: confirmed Cyan notify UUID present'   || { echo 'FAIL: Cyan notify UUID missing'; fail=1; }

grep -Fq 'de5bf72a-d711-4e47-af26-65e3012a5dc7' "$JAVA"   && echo 'PASS: exact confirmed Cyan write UUID present'   || { echo 'FAIL: Cyan write UUID missing'; fail=1; }

grep -Fq 'frame[1] = 0x40;' "$JAVA"   && echo 'PASS: only allow-listed command framing is 0x40'   || { echo 'FAIL: command-0x40 frame construction missing'; fail=1; }

[[ "$(grep -Fc 'writeCharacteristic(' "$JAVA")" -eq 1 ]]   && echo 'PASS: exactly one characteristic-write API call site'   || { echo 'FAIL: characteristic-write call count is not exactly one'; fail=1; }

[[ "$(grep -Fc 'setCharacteristicNotification(' "$JAVA")" -eq 1 ]]   && echo 'PASS: exactly one local notification-enable API call'   || { echo 'FAIL: unexpected notification-enable call count'; fail=1; }

[[ "$(grep -Fc 'writeDescriptor(' "$JAVA")" -eq 1 ]]   && echo 'PASS: exactly one descriptor-write API call'   || { echo 'FAIL: unexpected descriptor-write call count'; fail=1; }

grep -Fq 'BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE' "$JAVA"   && echo 'PASS: CCCD payload is standard enable-notification value'   || { echo 'FAIL: standard CCCD enable value missing'; fail=1; }

grep -Fq 'commandWriteAttempted' "$JAVA"   && echo 'PASS: one-shot write guard present'   || { echo 'FAIL: one-shot write guard missing'; fail=1; }

grep -Fq 'No retry policy: TRUE' "$JAVA"   && echo 'PASS: report declares no proprietary-write retry'   || { echo 'FAIL: no-retry declaration missing'; fail=1; }

grep -q 'android.permission.BLUETOOTH_CONNECT' "$MANIFEST"   && echo 'PASS: Bluetooth connect permission present'   || { echo 'FAIL: Bluetooth connect permission missing'; fail=1; }

exit "$fail"
