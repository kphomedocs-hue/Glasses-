#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
SRC="$ROOT/app/src/main"
JAVA="$SRC/java/com/parkarsite/g1mediacountprobe/MainActivity.java"
MANIFEST="$SRC/AndroidManifest.xml"
fail=0

check_absent() {
  local pat="$1" label="$2"
  if grep -RniE "$pat" "$SRC" >/tmp/kg1_g3_audit_hits 2>/dev/null; then
    echo "FAIL: $label"
    cat /tmp/kg1_g3_audit_hits
    fail=1
  else
    echo "PASS: $label absent"
  fi
}

check_absent 'BluetoothLeScanner|startScan|BLUETOOTH_SCAN|ACCESS_FINE_LOCATION' 'BLE scanning/location surface'
check_absent 'createBond|removeBond|ACTION_PAIRING_REQUEST|BluetoothGattServer|openGattServer' 'pairing/GATT-server mutation API'
check_absent 'WifiManager|WifiP2pManager|ConnectivityManager|java\.net|okhttp|HttpURLConnection|Socket' 'Wi-Fi/network code'
check_absent '0x02[[:space:]]*,[[:space:]]*0x01[[:space:]]*,[[:space:]]*0x04[[:space:]]*,[[:space:]]*0x0[12]' 'media-mode payload'
check_absent 'frame\[1\][[:space:]]*=[[:space:]]*0x40' 'time-sync command'
check_absent 'android\.permission\.INTERNET|ACCESS_WIFI_STATE|CHANGE_WIFI_STATE|NEARBY_WIFI_DEVICES' 'Internet/Wi-Fi permission'

grep -Fq 'new byte[]{0x02, 0x04}' "$JAVA"   && echo 'PASS: exact media-count payload present'   || { echo 'FAIL: exact media-count payload missing'; fail=1; }

grep -Fq 'frame[1] = 0x41;' "$JAVA"   && echo 'PASS: allow-listed outer command is 0x41'   || { echo 'FAIL: command-0x41 framing missing'; fail=1; }

grep -Fq 'BC 41 02 00 01 13 02 04' "$ROOT/README.md"   && echo 'PASS: expected full query frame documented'   || { echo 'FAIL: expected full query frame missing'; fail=1; }

[[ "$(grep -Fc 'writeCharacteristic(' "$JAVA")" -eq 1 ]]   && echo 'PASS: exactly one characteristic-write API call site'   || { echo 'FAIL: characteristic-write call count is not exactly one'; fail=1; }

[[ "$(grep -Fc 'setCharacteristicNotification(' "$JAVA")" -eq 1 ]]   && echo 'PASS: exactly one local notification-enable API call'   || { echo 'FAIL: unexpected notification-enable call count'; fail=1; }

[[ "$(grep -Fc 'writeDescriptor(' "$JAVA")" -eq 1 ]]   && echo 'PASS: exactly one descriptor-write API call'   || { echo 'FAIL: unexpected descriptor-write call count'; fail=1; }

grep -Fq 'BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE' "$JAVA"   && echo 'PASS: CCCD payload is standard enable-notification value'   || { echo 'FAIL: standard CCCD enable value missing'; fail=1; }

grep -Fq 'commandWriteAttempted' "$JAVA"   && echo 'PASS: one-shot write guard present'   || { echo 'FAIL: one-shot write guard missing'; fail=1; }

grep -Fq 'No retry policy: TRUE' "$JAVA"   && echo 'PASS: report declares no proprietary-write retry'   || { echo 'FAIL: no-retry declaration missing'; fail=1; }

grep -q 'android.permission.BLUETOOTH_CONNECT' "$MANIFEST"   && echo 'PASS: Bluetooth connect permission present'   || { echo 'FAIL: Bluetooth connect permission missing'; fail=1; }

exit "$fail"
