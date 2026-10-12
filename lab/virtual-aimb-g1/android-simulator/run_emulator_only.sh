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
    # Strict whitelist: only the synthetic phase and exception class, never raw logcat/data.
    grep -E '^INSTRUMENTATION_RESULT: (kind|status|error_kind|stage)=|^INSTRUMENTATION_CODE:' <<<"$OUTPUT" || true
    exit 1
  fi
  echo "VGL-EMU-0$round PASS: actual API36 emulator private receipts, recovery and blocked re-fetch"
done
echo 'VGL_EMULATOR 2/2 PASS: synthetic-only, separate Android processes; no user phone/glasses'

# Actual abrupt test-process termination at two durable-storage barriers.
# The process is forcibly terminated by Runtime.halt(73), not a caught exception.
# Distinguish expected termination from normal instrumentation PASS, timeout,
# or a caught instrumentation assertion/error (which reports status=FAIL).
run_hard_stop() {
  local mode="$1" label="$2" output code
  set +e
  output="$(timeout 120s adb -s "$SERIAL" shell am instrument -w -r -e vgl_mode "$mode" "$TEST_PKG/$RUNNER" 2>&1)"
  code=$?
  set -e
  # Android am instrument may print INSTRUMENTATION_CODE: 0 after a
  # Runtime.halt() process death. It is NOT a valid success signal here.
  # Demand NO normal status and independently verify the synced transaction
  # state in a fresh recovery instrumentation process immediately afterward.
  if [[ "$code" == "124" ]] ||
     grep -Fq 'INSTRUMENTATION_RESULT: status=FAIL' <<<"$output" ||
     grep -Fqx 'INSTRUMENTATION_RESULT: status=PASS' <<<"$output" ||
     grep -Fq 'INSTRUMENTATION_RESULT: error_kind=' <<<"$output"; then
    echo "ERROR: synthetic hard-stop did not terminate the process cleanly at $label"
    # No raw logcat, device paths, addresses or test media printed.
    grep -E '^INSTRUMENTATION_RESULT: (status|error_kind|stage)=|^INSTRUMENTATION_CODE:' <<<"$output" || true
    exit 1
  fi
  echo "VGL-EMU-CRASH-$label: instrumentation process terminated without normal result"
}
recover_hard_stop() {
  local mode="$1" label="$2" outcome="$3" output
  output="$(timeout 120s adb -s "$SERIAL" shell am instrument -w -r -e vgl_mode "$mode" "$TEST_PKG/$RUNNER")"
  if ! grep -Fqx 'INSTRUMENTATION_RESULT: status=PASS' <<<"$output" ||
     ! grep -Fqx "INSTRUMENTATION_RESULT: hard_stop=$outcome" <<<"$output" ||
     ! grep -Fqx 'INSTRUMENTATION_CODE: 0' <<<"$output"; then
    echo "ERROR: recovery after synthetic hard stop failed at $label"
    grep -E '^INSTRUMENTATION_RESULT: (kind|status|error_kind|stage|hard_stop)=|^INSTRUMENTATION_CODE:' <<<"$output" || true
    exit 1
  fi
  echo "VGL-EMU-CRASH-$label PASS: independent Android process recovers without source re-read"
}

run_hard_stop crash_after_fetch_mark 01
recover_hard_stop recover_after_fetch_mark 01 FETCH_MARK_UNCERTAIN_NO_REFETCH
run_hard_stop crash_after_verified_journal 02
recover_hard_stop recover_after_verified_journal 02 VERIFIED_LOCAL_RECOVERY_NO_REFETCH
run_hard_stop crash_mid_part_write 03
recover_hard_stop recover_mid_part_write 03 PARTIAL_BYTES_UNCERTAIN_NO_REFETCH
run_hard_stop crash_after_part_sync 04
recover_hard_stop recover_after_part_sync 04 SYNCED_PART_UNCERTAIN_NO_REFETCH
run_hard_stop crash_after_final_move 05
recover_hard_stop recover_after_final_move 05 MOVED_FINAL_LOCAL_RECOVERY
run_hard_stop crash_after_receipt 06
recover_hard_stop recover_after_receipt 06 RECEIPT_LOCAL_RECOVERY
echo 'VGL_EMULATOR_HARD_STOP 6/6 PASS: real Android JVM termination and local-only recovery at six boundaries'

# V4: same shared coordinator, completely synthetic but bitmap-decodable JPEG.
run_v4_mode(){
  local mode="$1" expected="$2" output
  output="$(timeout 120s adb -s "$SERIAL" shell am instrument -w -r -e vgl_mode "$mode" "$TEST_PKG/$RUNNER")"
  if ! grep -Fqx 'INSTRUMENTATION_RESULT: status=PASS' <<<"$output" ||
     ! grep -Fqx "INSTRUMENTATION_RESULT: shared=$expected" <<<"$output" ||
     ! grep -Fqx 'INSTRUMENTATION_CODE: 0' <<<"$output"; then
    echo "ERROR: V4 synthetic coordinator mode $mode failed"
    grep -E '^INSTRUMENTATION_RESULT: (status|stage|error_kind|shared)=|^INSTRUMENTATION_CODE:' <<<"$output" || true
    exit 1
  fi
  echo "V4-EMU $expected PASS"
}
run_v4_mode shared_first V4_SHARED_FULL_JPEG_COMMITTED
adb -s "$SERIAL" shell am force-stop "$PKG"
adb -s "$SERIAL" shell am force-stop "$TEST_PKG"
run_v4_mode shared_reopen V4_SHARED_REOPEN_NO_DUPLICATE
run_hard_stop shared_crash_verified V4
adb -s "$SERIAL" shell am force-stop "$PKG"
adb -s "$SERIAL" shell am force-stop "$TEST_PKG"
run_v4_mode shared_recover_verified V4_SHARED_HALT_RECOVERED_NO_REFETCH
echo 'V4-EMULATOR-SHARED PASS: one complete locally decoded synthetic JPEG; new coordinator persisted and recovered after actual test-process death'
