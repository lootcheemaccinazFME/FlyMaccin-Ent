#!/usr/bin/env bash
set -Eeuo pipefail

SRC="${1:-pocket-potna-v2.1.14}"
PACKAGE="${2:-com.flymaccin.pocketpotna}"
OUT="${3:-out/emulator}"
APK="${4:-${SRC}/app/build/outputs/apk/release/app-release.apk}"
BUILD_LABEL="${5:-canonical release}"
mkdir -p "$OUT"

collect_diagnostics() {
  set +e
  adb devices -l > "$OUT/adb-devices.txt" 2>&1
  adb shell getprop > "$OUT/getprop.txt" 2>&1
  adb shell dumpsys package "$PACKAGE" > "$OUT/package-dumpsys.txt" 2>&1
  adb shell dumpsys activity top > "$OUT/activity-top.txt" 2>&1
  adb shell dumpsys window windows > "$OUT/window-dumpsys.txt" 2>&1
  adb logcat -d -v threadtime > "$OUT/logcat.txt" 2>&1
  adb exec-out screencap -p > "$OUT/launch-screen.png" 2>/dev/null
  set -e
}
trap collect_diagnostics EXIT

echo "[emulator] Waiting for ADB device"
adb wait-for-device

echo "[emulator] Waiting for Android framework boot completion"
BOOTED=""
for _ in $(seq 1 180); do
  BOOTED="$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r' || true)"
  [[ "$BOOTED" == "1" ]] && break
  sleep 2
done
if [[ "$BOOTED" != "1" ]]; then
  echo "EMULATOR_FAIL: sys.boot_completed never reached 1"
  exit 20
fi

adb shell input keyevent 82 >/dev/null 2>&1 || true
adb shell settings put global window_animation_scale 0 || true
adb shell settings put global transition_animation_scale 0 || true
adb shell settings put global animator_duration_scale 0 || true

if [[ ! -s "$APK" ]]; then
  echo "EMULATOR_FAIL: APK missing: $APK"
  exit 21
fi

printf '%s\n' "apk_path=$APK" "build_label=$BUILD_LABEL" > "$OUT/apk-under-test.txt"

echo "[emulator] Installing $BUILD_LABEL APK"
adb uninstall "$PACKAGE" >/dev/null 2>&1 || true
INSTALL_OUTPUT="$(adb install --no-streaming -r "$APK" 2>&1)"
printf '%s\n' "$INSTALL_OUTPUT" | tee "$OUT/install.txt"
if ! grep -q '^Success$' <<<"$INSTALL_OUTPUT"; then
  echo "EMULATOR_FAIL: adb install did not report Success"
  exit 22
fi

if ! adb shell pm path "$PACKAGE" | tee "$OUT/pm-path.txt" | grep -q '^package:'; then
  echo "EMULATOR_FAIL: package manager cannot find $PACKAGE after install"
  exit 23
fi

ACTIVITY="$(adb shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.LAUNCHER "$PACKAGE" 2>/dev/null | tr -d '\r' | tail -n 1)"
printf '%s\n' "$ACTIVITY" > "$OUT/resolved-launcher.txt"
if [[ -z "$ACTIVITY" || "$ACTIVITY" != "$PACKAGE"/* ]]; then
  echo "EMULATOR_FAIL: no launcher activity resolved for $PACKAGE (got '$ACTIVITY')"
  exit 24
fi

echo "[emulator] Launching $ACTIVITY"
adb logcat -c
adb shell am force-stop "$PACKAGE"
START_OUTPUT="$(adb shell am start -W -n "$ACTIVITY" 2>&1)"
printf '%s\n' "$START_OUTPUT" | tee "$OUT/am-start.txt"
if grep -Eqi 'Error|Exception|Activity class .* does not exist|unable to resolve' <<<"$START_OUTPUT"; then
  echo "EMULATOR_FAIL: ActivityManager rejected launch"
  exit 25
fi

PID=""
for _ in $(seq 1 20); do
  PID="$(adb shell pidof "$PACKAGE" 2>/dev/null | tr -d '\r' || true)"
  [[ -n "$PID" ]] && break
  sleep 1
done
if [[ -z "$PID" ]]; then
  echo "EMULATOR_FAIL: app process never became live after launch"
  exit 26
fi
printf '%s\n' "$PID" > "$OUT/pid-initial.txt"

sleep 10
PID_AFTER="$(adb shell pidof "$PACKAGE" 2>/dev/null | tr -d '\r' || true)"
if [[ -z "$PID_AFTER" ]]; then
  echo "EMULATOR_FAIL: app process died during the 10-second startup survival window"
  exit 27
fi
printf '%s\n' "$PID_AFTER" > "$OUT/pid-after-10s.txt"

FOCUS="$(adb shell dumpsys window 2>/dev/null | grep -E 'mCurrentFocus|mFocusedApp' | grep "$PACKAGE" | head -1 || true)"
printf '%s\n' "$FOCUS" > "$OUT/focus.txt"

printf '%s\n' \
  'emulator_status=PASS' \
  "package=$PACKAGE" \
  "build_label=$BUILD_LABEL" \
  "apk_path=$APK" \
  "launcher_activity=$ACTIVITY" \
  "pid_initial=$PID" \
  "pid_after_10s=$PID_AFTER" \
  "focus_detected=$([[ -n "$FOCUS" ]] && echo yes || echo no)" \
  > "$OUT/EMULATOR_ACCEPTANCE.txt"

echo "EMULATOR_PASS: $PACKAGE installed, launched, and survived startup on the Android emulator ($BUILD_LABEL)"
