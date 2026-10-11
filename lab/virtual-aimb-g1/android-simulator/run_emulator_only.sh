#!/usr/bin/env bash
set -euo pipefail

# Safety gate: ONE standard Android emulator, NO physical ADB serial ever.
COUNT="$(adb devices | awk '$2=="device"{n++}END{print n+0}')"
SERIAL="$(adb devices | awk '$2=="device"{print $1}')"
if [[ "$COUNT" != "1" || "$SERIAL" != emulator-* ]]; then
  echo "ERROR: exactly one emulator and no connected physical device required"
  exit 1
fi
QEMU="$(adb -s "$SERIAL" shell getprop ro.kernel.qemu | tr -d '\r')"
API="$(adb -s "$SERIAL" shell getprop ro.build.version.sdk | tr -d '\r')"
if [[ "$QEMU" != "1" || "$API" != "36" ]]; then
  echo "ERROR: only API 36 virtual Android device permitted"
  exit 1
fi

# Two ephemeral APKs for the separate synthetic-only app/test on the virtual machine.
gradle --no-daemon :app:assembleDebug :app:assembleDebugAndroidTest
APP="app/build/outputs/apk/debug/app-debug.apk"
TEST="app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk"
test -s "$APP"
test -s "$TEST"
for file in "$APP" "$TEST"; do
  INFO="$("$ANDROID_HOME/build-tools/36.0.0/aapt" dump permissions "$file")"
  if grep -q 'uses-permission:' <<<"$INFO"; then
    echo "ERROR: simulator unexpectedly requests a hardware/network permission"
    exit 1
  fi
done

PKG="com.parkarsite.vglandroid.simulator"
TEST_PKG="com.parkarsite.vglandroid.simulator.test"
RUNNER="com.parkarsite.vglandroid.HarnessInstrumentation"
cleanup() {
  adb -s "$SERIAL" uninstall "$TEST_PKG" >/dev/null 2>&1 || true
  adb -s "$SERIAL" uninstall "$PKG" >/dev/null 2>&1 || true
}
trap cleanup EXIT
adb -s "$SERIAL" install -r -t "$APP" >/dev/null
adb -s "$SERIAL" install -r -t "$TEST" >/dev/null
adb -s "$SERIAL" shell pm clear "$PKG" >/dev/null

for round in 1 2; do
  if [[ "$round" == "2" ]]; then
    # Separate Android process; durable bytes remain in app-private storage.
    adb -s "$SERIAL" shell am force-stop "$PKG"
    adb -s "$SERIAL" shell am force-stop "$TEST_PKG"
  fi
  OUTPUT="$(adb -s "$SERIAL" shell am instrument -w -r "$TEST_PKG/$RUNNER")"
  if ! grep -Fqx 'INSTRUMENTATION_RESULT: status=PASS' <<<"$OUTPUT" ||
     ! grep -Fqx 'INSTRUMENTATION_RESULT: storage=PRIVATE_RECEIPT_REOPEN_OK' <<<"$OUTPUT" ||
     ! grep -Fqx 'INSTRUMENTATION_RESULT: interrupt=REMOTE_UNCERTAIN_AND_LOCAL_RECOVERY_OK' <<<"$OUTPUT" ||
     ! grep -Fqx 'INSTRUMENTATION_CODE: 0' <<<"$OUTPUT"; then
    echo "ERROR: emulator-only synthetic instrumentation failed in separate execution $round"
    # Do not print file paths or other raw Android output.
    exit 1
  fi
  echo "VGL-EMU-0$round PASS: actual API36 emulator private receipts, recovery and blocked re-fetch"
done
echo 'VGL_EMULATOR 2/2 PASS: synthetic-only, separate Android processes; no user phone/glasses'
