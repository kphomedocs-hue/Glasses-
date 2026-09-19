#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
SRC="$ROOT/app/src/main"
JAVA="$SRC/java/com/parkarsite/g1p2plifecycleprobe/MainActivity.java"
MANIFEST="$SRC/AndroidManifest.xml"
fail=0

check_absent() {
  local pat="$1" label="$2"
  if grep -RniE "$pat" "$SRC" >/tmp/kg1_g3b_audit_hits 2>/dev/null; then
    echo "FAIL: $label"
    cat /tmp/kg1_g3b_audit_hits
    fail=1
  else
    echo "PASS: $label absent"
  fi
}

check_absent 'BluetoothLeScanner|startScan|BLUETOOTH_SCAN|ACCESS_FINE_LOCATION' 'BLE scanning/location surface'
check_absent 'createBond|removeBond|ACTION_PAIRING_REQUEST|BluetoothGattServer|openGattServer' 'pairing/GATT-server mutation API'
check_absent 'WifiManager|WifiP2pManager|ConnectivityManager|java\.net|okhttp|HttpURLConnection|Socket' 'phone Wi-Fi/network code'
check_absent '0x02[[:space:]]*,[[:space:]]*0x01[[:space:]]*,[[:space:]]*0x04[[:space:]]*,[[:space:]]*0x02' 'AP-mode payload'
check_absent 'android\.permission\.INTERNET|ACCESS_WIFI_STATE|CHANGE_WIFI_STATE|NEARBY_WIFI_DEVICES' 'Internet/Wi-Fi permission'
check_absent 'delete|removeFile|FileOutputStream|ContentResolver' 'file mutation surface'

grep -Fq 'new byte[]{0x02, 0x01, 0x04, 0x01}' "$JAVA"   && echo 'PASS: exact Cyan P2P-enter payload present'   || { echo 'FAIL: P2P-enter payload missing'; fail=1; }

grep -Fq 'new byte[]{0x02, 0x01, 0x09}' "$JAVA"   && echo 'PASS: exact Cyan exit-transfer payload present'   || { echo 'FAIL: exit-transfer payload missing'; fail=1; }

grep -Fq 'BC 41 04 00 93 5C 02 01 04 01' "$ROOT/README.md"   && echo 'PASS: P2P-enter frame documented'   || { echo 'FAIL: P2P-enter frame missing'; fail=1; }

grep -Fq 'BC 41 03 00 11 96 02 01 09' "$ROOT/README.md"   && echo 'PASS: exit-transfer frame documented'   || { echo 'FAIL: exit-transfer frame missing'; fail=1; }

[[ "$(grep -Fc 'writeCharacteristic(' "$JAVA")" -eq 1 ]]   && echo 'PASS: one centralized characteristic-write API call site'   || { echo 'FAIL: characteristic-write API call count is not exactly one'; fail=1; }

[[ "$(grep -Fc 'setCharacteristicNotification(' "$JAVA")" -eq 1 ]]   && echo 'PASS: exactly one local notification-enable API call'   || { echo 'FAIL: unexpected notification-enable call count'; fail=1; }

[[ "$(grep -Fc 'writeDescriptor(' "$JAVA")" -eq 1 ]]   && echo 'PASS: exactly one descriptor-write API call'   || { echo 'FAIL: unexpected descriptor-write call count'; fail=1; }

grep -Fq 'enterWriteAttempted' "$JAVA"   && grep -Fq 'exitWriteAttempted' "$JAVA"   && echo 'PASS: explicit two-stage one-shot guards present'   || { echo 'FAIL: lifecycle write guards missing'; fail=1; }

grep -Fq 'No retry policy: TRUE' "$JAVA"   && echo 'PASS: no-retry declaration present'   || { echo 'FAIL: no-retry declaration missing'; fail=1; }

grep -q 'android.permission.BLUETOOTH_CONNECT' "$MANIFEST"   && echo 'PASS: Bluetooth connect permission present'   || { echo 'FAIL: Bluetooth connect permission missing'; fail=1; }

exit "$fail"
