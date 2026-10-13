#!/usr/bin/env bash
set -euo pipefail

# Single API36 emulator only. NEVER connect a phone/glasses or archive a test APK.
COUNT="$(adb devices | awk '$2=="device"{n++} END{print n+0}')"
SERIAL="$(adb devices | awk '$2=="device"{print $1}')"
if [[ "$COUNT" != 1 || "$SERIAL" != emulator-* ]]; then
  echo "FAIL: required exactly one Android emulator, no physical devices"; exit 1
fi
QEMU="$(adb -s "$SERIAL" shell getprop ro.kernel.qemu | tr -d '\r')"
API="$(adb -s "$SERIAL" shell getprop ro.build.version.sdk | tr -d '\r')"
REQUIRED_API="${K_SITE_EMULATOR_API:-36}"
if [[ "$REQUIRED_API" != 35 && "$REQUIRED_API" != 36 ]]; then
  echo "FAIL: only API35/API36 virtual execution is supported"; exit 1
fi
if [[ "$QEMU" != 1 || "$API" != "$REQUIRED_API" ]]; then
  echo "FAIL: not the exact requested API$REQUIRED_API emulator"; exit 1
fi

gradle --no-daemon :app:assembleDebug
APK="app/build/outputs/apk/debug/app-debug.apk"
test -s "$APK"
INFO="$("$ANDROID_HOME/build-tools/36.0.0/aapt" dump permissions "$APK")"
if grep -q 'uses-permission:' <<<"$INFO"; then
  echo "FAIL: preview APK unexpectedly requests permission"; exit 1
fi
PKG="com.parkarsite.g1.readonlypreview"
cleanup() {
  adb -s "$SERIAL" uninstall "$PKG.test" >/dev/null 2>&1 || true
  adb -s "$SERIAL" uninstall "$PKG" >/dev/null 2>&1 || true
}
trap cleanup EXIT
adb -s "$SERIAL" install -r -t "$APK" >/dev/null
adb -s "$SERIAL" shell pm clear "$PKG" >/dev/null
adb -s "$SERIAL" shell am start -W -n "$PKG/com.parkarsite.g1.preview.OfflinePreviewActivity" >/dev/null
sleep 3
adb -s "$SERIAL" shell uiautomator dump /sdcard/v4-offline-ui.xml >/dev/null
SCREEN="$(adb -s "$SERIAL" exec-out cat /sdcard/v4-offline-ui.xml)"
if ! grep -Fq 'Media transfer disabled' <<<"$SCREEN" ||
   ! grep -Fq 'Local Archive' <<<"$SCREEN" ||
   ! grep -Fq 'Read-only development preview' <<<"$SCREEN"; then
  echo "FAIL: mandatory visible on-screen original-preservation warning absent"
  exit 1
fi
echo "V4-SITE-CAPTURE-UI PASS: API$REQUIRED_API emulator visually exposes local archive and transfer-disabled warning"
echo "V4-SITE-CAPTURE-ISOLATION PASS: no permissions, test APK not uploaded, virtual device only"

# New production-facing local archive UI cases, synthesized in AndroidTest only.
# The installable app contains no seed endpoint; this separate test APK is not released.
gradle --no-daemon :app:assembleDebugAndroidTest
TEST_APK="app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk"
test -s "$TEST_APK"
TEST_INFO="$("$ANDROID_HOME/build-tools/36.0.0/aapt" dump permissions "$TEST_APK")"
if grep -q 'uses-permission:' <<<"$TEST_INFO"; then
  echo "FAIL: AndroidTest APK requested permissions"; exit 1
fi
adb -s "$SERIAL" install -r -t "$TEST_APK" >/dev/null
TEST_PKG="$PKG.test"
RUNNER="com.parkarsite.g1.preview.OfflineArchiveInstrumentation"

seed_fixture(){
  local mode="$1" expected="$2" output
  output="$(timeout 120s adb -s "$SERIAL" shell am instrument -w -r \
    -e preview_mode "$mode" "$TEST_PKG/$RUNNER")"
  if ! grep -Fqx 'INSTRUMENTATION_RESULT: status=PASS' <<<"$output" ||
     ! grep -Fqx "INSTRUMENTATION_RESULT: fixture=$expected" <<<"$output" ||
     ! grep -Fqx 'INSTRUMENTATION_CODE: 0' <<<"$output"; then
    echo "FAIL: local-only synthetic archive fixture $mode"
    grep -E '^INSTRUMENTATION_RESULT: (status|fixture|error_kind)=|^INSTRUMENTATION_CODE:' <<<"$output" || true
    exit 1
  fi
}
capture_scrolling_screen(){
  local first second
  adb -s "$SERIAL" shell uiautomator dump /sdcard/v4-offline-ui.xml >/dev/null
  first="$(adb -s "$SERIAL" exec-out cat /sdcard/v4-offline-ui.xml)"
  adb -s "$SERIAL" shell input swipe 530 1850 530 420 400
  adb -s "$SERIAL" shell uiautomator dump /sdcard/v4-offline-ui.xml >/dev/null
  second="$(adb -s "$SERIAL" exec-out cat /sdcard/v4-offline-ui.xml)"
  printf '%s\n%s\n' "$first" "$second"
}
check_archive_ui(){
  local status="$1" count="$2" extra="$3" output
  # Force-stop and restart to verify new Activity really reopens app-private receipts.
  adb -s "$SERIAL" shell am force-stop "$PKG"
  adb -s "$SERIAL" shell am force-stop "$TEST_PKG"
  adb -s "$SERIAL" shell am start -W -n "$PKG/com.parkarsite.g1.preview.OfflinePreviewActivity" >/dev/null
  sleep 2
  output="$(capture_scrolling_screen)"
  if ! grep -Fq 'Media transfer disabled' <<<"$output" ||
     ! grep -Fq "Locally verified files: $count" <<<"$output" ||
     ! grep -Fq "Archive status: $status" <<<"$output" ||
     ! grep -Fq "$extra" <<<"$output"; then
    echo "FAIL: expected safe offline archive UI not visible: $status"
    exit 1
  fi
  echo "V4-SITE-CAPTURE-ARCHIVE-UI $status PASS"
}
seed_fixture seed_complete COMPLETE_3
check_archive_ui LOCAL_ONLY_SAFE 3 'Uncertain imports: 0'
# Fresh process reboot without reseeding verifies persistence and non-duplication.
check_archive_ui LOCAL_ONLY_SAFE 3 'Manual reviews: 0'
seed_fixture seed_uncertain UNCERTAIN_1
check_archive_ui LOCAL_REVIEW_NO_REMOTE_RETRY 3 'Uncertain imports: 1'
seed_fixture corrupt_journal JOURNAL_CORRUPT
check_archive_ui LOCAL_LEDGER_UNVERIFIED 0 'Manual reviews: 1'
echo 'V4-SITE-CAPTURE-ARCHIVE-STATE 4/4 PASS: restart, mixed receipt counts, uncertain and corrupt journal fail-closed'
