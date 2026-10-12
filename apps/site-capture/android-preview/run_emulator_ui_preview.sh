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
if [[ "$QEMU" != 1 || "$API" != 36 ]]; then
  echo "FAIL: not a verified API36 emulator"; exit 1
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
echo "V4-SITE-CAPTURE-UI PASS: API36 emulator visually exposes local archive and transfer-disabled warning"
echo "V4-SITE-CAPTURE-ISOLATION PASS: no permissions, test APK not uploaded, virtual device only"
