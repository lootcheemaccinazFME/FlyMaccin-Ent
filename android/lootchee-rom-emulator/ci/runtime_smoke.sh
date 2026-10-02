#!/usr/bin/env bash
set -euo pipefail
APK="android/lootchee-rom-emulator/app/build/outputs/apk/debug/app-debug.apk"
adb install -r "$APK"
adb shell am force-stop com.flymaccin.lootcheerom
adb shell am start -W -n com.flymaccin.lootcheerom/.MainActivity | tee /tmp/launch.txt
sleep 8
PID="$(adb shell pidof com.flymaccin.lootcheerom | tr -d '\r')"
test -n "$PID"
adb shell dumpsys activity activities > /tmp/activities.txt
grep -q 'com.flymaccin.lootcheerom/.MainActivity' /tmp/activities.txt
adb logcat -d -t 500 > /tmp/logcat.txt
if grep -E 'FATAL EXCEPTION.*com.flymaccin.lootcheerom|Process: com.flymaccin.lootcheerom.*FATAL' /tmp/logcat.txt; then exit 1; fi
adb exec-out screencap -p > /tmp/fme-home.png
echo "FME_RUNTIME_INSTALL_LAUNCH=PASS" | tee /tmp/runtime.txt
