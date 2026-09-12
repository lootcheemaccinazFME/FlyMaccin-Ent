#!/usr/bin/env bash
set -euo pipefail

APK="$GITHUB_WORKSPACE/MaccinAzzOS-Phase7-debug.apk"
PKG="$EXPECTED_PACKAGE"

adb install -r "$APK" | tee "$GITHUB_WORKSPACE/adb-install.txt"
grep -q 'Success' "$GITHUB_WORKSPACE/adb-install.txt"
adb logcat -c
adb shell am start -W -n "$PKG/.MainActivity" | tee "$GITHUB_WORKSPACE/launch-first.txt"
sleep 6
adb shell pidof "$PKG" | tee "$GITHUB_WORKSPACE/pid-first.txt"
test -s "$GITHUB_WORKSPACE/pid-first.txt"

dump_ui() {
  local out="$1"
  for i in $(seq 1 8); do
    adb shell uiautomator dump /sdcard/window.xml >/dev/null 2>&1 || true
    adb pull /sdcard/window.xml "$out" >/dev/null 2>&1 || true
    if [ -s "$out" ]; then return 0; fi
    sleep 1
  done
  return 1
}

tap_text() {
  local xml="$1"; shift
  local label="$*"
  local xy
  xy=$(python3 - "$xml" "$label" <<'PY'
import re, sys, xml.etree.ElementTree as ET
path, label = sys.argv[1], sys.argv[2]
root = ET.parse(path).getroot()
candidates=[]
for n in root.iter('node'):
    text=(n.attrib.get('text') or '').strip()
    desc=(n.attrib.get('content-desc') or '').strip()
    if label == text or label == desc or label in text or label in desc:
        m=re.match(r'\[(\d+),(\d+)\]\[(\d+),(\d+)\]', n.attrib.get('bounds',''))
        if m:
            x1,y1,x2,y2=map(int,m.groups())
            candidates.append(((x2-x1)*(y2-y1),(x1+x2)//2,(y1+y2)//2))
if not candidates:
    raise SystemExit(2)
_,x,y=max(candidates)
print(x,y)
PY
  )
  adb shell input tap $xy
  sleep 3
}

dump_ui "$GITHUB_WORKSPACE/ui-home.xml"
grep -Fq 'MACCIN AZZ GAME / OS' "$GITHUB_WORKSPACE/ui-home.xml"
tap_text "$GITHUB_WORKSPACE/ui-home.xml" 'Persona Operating Systems'
dump_ui "$GITHUB_WORKSPACE/ui-personas.xml"
grep -Fq 'MACCIN AZZ PERSONA OS' "$GITHUB_WORKSPACE/ui-personas.xml"
grep -Fq 'HAHAMaccin AzzOS' "$GITHUB_WORKSPACE/ui-personas.xml"
tap_text "$GITHUB_WORKSPACE/ui-personas.xml" 'HAHAMaccin AzzOS'
dump_ui "$GITHUB_WORKSPACE/ui-haha.xml"
grep -Fq 'HAHAMaccin AzzOS' "$GITHUB_WORKSPACE/ui-haha.xml"
grep -Fq 'Persona task' "$GITHUB_WORKSPACE/ui-haha.xml"
adb shell pidof "$PKG" > "$GITHUB_WORKSPACE/pid-persona.txt"
test -s "$GITHUB_WORKSPACE/pid-persona.txt"

adb shell input keyevent 4
sleep 2
adb shell input keyevent 4
sleep 3
dump_ui "$GITHUB_WORKSPACE/ui-home-before-reopen.xml"
grep -Fq 'MACCIN AZZ GAME / OS' "$GITHUB_WORKSPACE/ui-home-before-reopen.xml"

adb exec-out run-as "$PKG" ls -l databases | tee "$GITHUB_WORKSPACE/room-before.txt"
grep -Fq 'flymaccin_creator_os.db' "$GITHUB_WORKSPACE/room-before.txt"
adb exec-out run-as "$PKG" cat databases/flymaccin_creator_os.db > "$GITHUB_WORKSPACE/room-before.db"
test -s "$GITHUB_WORKSPACE/room-before.db"

adb shell am force-stop "$PKG"
sleep 2
adb shell am start -W -n "$PKG/.MainActivity" | tee "$GITHUB_WORKSPACE/launch-reopen.txt"
sleep 6
adb shell pidof "$PKG" | tee "$GITHUB_WORKSPACE/pid-reopen.txt"
test -s "$GITHUB_WORKSPACE/pid-reopen.txt"
dump_ui "$GITHUB_WORKSPACE/ui-reopen.xml"
grep -Fq 'MACCIN AZZ GAME / OS' "$GITHUB_WORKSPACE/ui-reopen.xml"
adb exec-out run-as "$PKG" ls -l databases | tee "$GITHUB_WORKSPACE/room-after.txt"
grep -Fq 'flymaccin_creator_os.db' "$GITHUB_WORKSPACE/room-after.txt"
adb exec-out run-as "$PKG" cat databases/flymaccin_creator_os.db > "$GITHUB_WORKSPACE/room-after.db"
test -s "$GITHUB_WORKSPACE/room-after.db"

adb shell input swipe 500 1400 500 500 250 || true
sleep 2
dump_ui "$GITHUB_WORKSPACE/ui-home-scrolled.xml"
tap_text "$GITHUB_WORKSPACE/ui-home-scrolled.xml" 'AI Runtime'
dump_ui "$GITHUB_WORKSPACE/ui-ai.xml"
grep -Eq 'AI Studio|Prompt Compiler|AI Runtime' "$GITHUB_WORKSPACE/ui-ai.xml"
adb shell pidof "$PKG" > "$GITHUB_WORKSPACE/pid-ai.txt"
test -s "$GITHUB_WORKSPACE/pid-ai.txt"

adb logcat -d -v threadtime > "$GITHUB_WORKSPACE/smoke-logcat.txt"
if grep -E 'FATAL EXCEPTION|Process: com\.flymaccin\.creatoros.*has died|Force finishing activity.*com\.flymaccin\.creatoros' "$GITHUB_WORKSPACE/smoke-logcat.txt"; then
  echo 'Fatal app crash detected during smoke test' >&2
  exit 1
fi

echo 'PASS: install, launch, Persona OS, HaHa workspace, Room persistence, reopen, and AI no-credential screen smoke checks' | tee "$GITHUB_WORKSPACE/smoke-summary.txt"
