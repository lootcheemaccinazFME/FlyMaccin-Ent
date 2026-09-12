#!/usr/bin/env sh
set -eu

PACKAGE='com.flymaccin.pocketpotna'
APK='demonic-ai-studio-hut/app/build/outputs/apk/debug/app-debug.apk'
REPORT='emulator-smoke-report.txt'

: > "$REPORT"
echo "PACKAGE=$PACKAGE" | tee -a "$REPORT"
echo "APK_SHA256=$(sha256sum "$APK" | awk '{print $1}')" | tee -a "$REPORT"

adb wait-for-device

i=1
while [ "$i" -le 60 ]; do
  STATE="$(adb get-state 2>/dev/null || true)"
  BOOT="$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r' || true)"
  if [ "$STATE" = "device" ] && [ "$BOOT" = "1" ]; then
    break
  fi
  sleep 2
  i=$((i + 1))
done

test "$(adb get-state)" = "device"
test "$(adb shell getprop sys.boot_completed | tr -d '\r')" = "1"
adb devices -l | tee adb-devices.txt

adb install -r "$APK" | tee -a "$REPORT"
PACKAGE_PATH="$(adb shell pm path "$PACKAGE" | tr -d '\r')"
echo "$PACKAGE_PATH" | tee -a "$REPORT"
test -n "$PACKAGE_PATH"

adb shell dumpsys package "$PACKAGE" | grep -E 'versionName=|versionCode=|supportsPictureInPicture|resizeMode' | head -8 | tee -a "$REPORT" || true

adb logcat -c
adb shell monkey -p "$PACKAGE" -c android.intent.category.LAUNCHER 1 | tee -a "$REPORT"
sleep 10
PID="$(adb shell pidof "$PACKAGE" | tr -d '\r')"
echo "PID_AFTER_FIRST_LAUNCH=$PID" | tee -a "$REPORT"
test -n "$PID"
adb exec-out screencap -p > visual-home-first.png
adb logcat -d > logcat.txt
if grep -A30 -B10 -E "Process: ${PACKAGE}|FATAL EXCEPTION" logcat.txt | grep -q "$PACKAGE"; then
  echo 'FAIL: app-specific fatal exception detected.' | tee -a "$REPORT"
  exit 1
fi

adb shell am force-stop "$PACKAGE"
sleep 2
adb shell monkey -p "$PACKAGE" -c android.intent.category.LAUNCHER 1 | tee -a "$REPORT"
sleep 6
PID2="$(adb shell pidof "$PACKAGE" | tr -d '\r')"
echo "PID_AFTER_RELAUNCH=$PID2" | tee -a "$REPORT"
test -n "$PID2"
adb exec-out screencap -p > visual-home-relaunch.png
adb logcat -d > logcat-final.txt
if grep -A30 -B10 -E "Process: ${PACKAGE}|FATAL EXCEPTION" logcat-final.txt | grep -q "$PACKAGE"; then
  echo 'FAIL: app-specific fatal exception detected after relaunch.' | tee -a "$REPORT"
  exit 1
fi

adb shell dumpsys activity activities > activity-final.txt
adb shell dumpsys package "$PACKAGE" > package-final.txt
echo 'PASS: install, launch, visual capture, process survival, crash scan, force-stop, and relaunch.' | tee -a "$REPORT"
