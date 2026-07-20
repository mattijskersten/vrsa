#!/usr/bin/env bash
# One-time port of reminders from the old VRSA (com.vrsa.app) to VRSA 2
# (com.vrsa.app2), over adb.
#
# Since VRSA 2 keeps the same reminders.txt format and auto-applies the file
# on launch, the port is a straight file copy. Prerequisites: the connected
# phone has the old app's reminders.txt, and VRSA 2 is installed.
set -euo pipefail

ADB="${ADB:-$HOME/Android/Sdk/platform-tools/adb}"
OLD=/sdcard/Android/data/com.vrsa.app/files/reminders.txt
NEW=/sdcard/Android/data/com.vrsa.app2/files/reminders.txt
WORK="$(mktemp -d)"
trap 'rm -rf "$WORK"' EXIT

echo "==> Pulling old config"
# Plain pull works on devices where adbd has storage access; fall back to
# run-as (works when the old debug app is installed) if it doesn't.
"$ADB" pull "$OLD" "$WORK/reminders.txt" >/dev/null 2>&1 ||
    "$ADB" shell run-as com.vrsa.app cat "/storage/emulated/0/Android/data/com.vrsa.app/files/reminders.txt" \
        > "$WORK/reminders.txt"
[ -s "$WORK/reminders.txt" ] || { echo "ERROR: could not read old reminders.txt"; exit 1; }
echo "    $(grep -cvE '^\s*(#|$)' "$WORK/reminders.txt" || true) reminder line(s) found"

echo "==> Launching VRSA 2 once so its files directory exists"
"$ADB" shell am start -n com.vrsa.app2/com.vrsa.app.ui.MainActivity >/dev/null
sleep 3

echo "==> Copying config to VRSA 2 and relaunching so it applies"
"$ADB" push "$WORK/reminders.txt" "$NEW" >/dev/null
"$ADB" shell am force-stop com.vrsa.app2
"$ADB" shell am start -n com.vrsa.app2/com.vrsa.app.ui.MainActivity >/dev/null
sleep 3

echo "==> Alarms now scheduled by VRSA 2:"
"$ADB" shell "dumpsys alarm | grep -c 'com.vrsa.app2/com.vrsa.app.scheduling.ReminderAlarmReceiver'" || true
echo "Done. Check the editor on the phone — any unparseable lines are listed in-app."
