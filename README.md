<div align="center">
  <img src="logo.svg" alt="vrsa" width="800"/>
</div>

# Very Reminder; Simple App (VRSA)

*Wordplay on "Very Simple Reminder App", in the style of a doge meme. Pronounced vur-sah.*

---

## Overview

An Android app that fires notifications at scheduled times. Configuration is
a plain-text file edited directly within the app (or with any external file
manager or text editor). There is no home screen, settings screen, or widget —
just an editor and a Save button.

---

## Behaviour

- **The app must be launched at least once** to schedule reminders.
  Until then, no notifications will fire.
- On first launch, the app requests notification permission (Android 13+) then
  opens the config file editor.
- The editor shows the raw contents of `reminders.txt`. Edit the file and tap
  **Save** — the app writes the file and reschedules all alarms immediately.
  A toast confirms how many reminders were scheduled.
- Each alarm fires at exactly the scheduled time, then automatically reschedules
  itself for the next occurrence of that reminder.
- Alarms are rescheduled automatically after a reboot — no user action required.

---

## Config File

**Location**

```
/sdcard/Android/data/com.vrsa.app/files/reminders.txt
```

`/sdcard/` is Android's standard symlink to the device's built-in user-accessible
storage (`/storage/emulated/0/`). This is part of the phone's internal flash
storage and is present on all normal Android devices — no SD card is required.

This directory is created automatically on first app launch. If the config file
does not exist, the app creates it with commented-out format examples.

The easiest way to edit it is within the app itself. It can also be edited
externally with any file manager or text editor that supports the path above —
re-open the app and tap Save to apply external changes.

**Format**

One reminder per line:

```
HH:MM  <days>  <label>
```

| Field   | Description                                                         |
|---------|---------------------------------------------------------------------|
| `HH:MM` | 24-hour time in `HH:MM` format (e.g. `08:00`, `22:30`)            |
| `days`  | `daily`, or a comma-separated list of day abbreviations (see below) |
| `label` | Free text shown in the notification body                            |

Fields are separated by one or more spaces or tabs.

**Day abbreviations**

`Mon` `Tue` `Wed` `Thu` `Fri` `Sat` `Sun` (case-insensitive)

**Comments and blank lines**

Lines starting with `#` and blank lines are ignored.

**Example**

```
# Weekday reminders
08:00  Mon,Tue,Wed,Thu,Fri  Take medication
12:30  daily                Drink water
22:30  Mon,Wed,Fri          Evening reminder

# Weekend
09:00  Sat,Sun              Morning walk
```

---

## Notifications

- Delivered as a standard Android notification (not a full-screen alarm).
- Title: `Reminder`
- Body: the label text from the config file.
- Priority: high (causes a heads-up notification on most devices).
- Auto-dismissed when tapped.

---

## Permissions

| Permission              | When requested              | Purpose                                        |
|-------------------------|-----------------------------|------------------------------------------------|
| `POST_NOTIFICATIONS`    | On first launch (Android 13+) | Required to show notifications               |
| `USE_EXACT_ALARM`       | Granted automatically (Android 13+) | Required to fire alarms at exact times |
| `RECEIVE_BOOT_COMPLETED`| Granted automatically       | Reschedule alarms after reboot                 |

No network, location, or other permissions are used.

---

## Testing

Add a reminder a couple of minutes in the future using the in-app editor, then
tap Save. The notification will appear at the specified time.

Alternatively, push a config file directly via adb:

```bash
# Write a reminder 2 minutes from now (adjust time as needed)
echo "HH:MM  daily  Test reminder" > /tmp/reminders.txt
adb push /tmp/reminders.txt /sdcard/Android/data/com.vrsa.app/files/reminders.txt
```

Then open the app and tap Save to schedule the alarm.

`adb` is not on the system PATH — see AGENTS.md for the full path.

---

## Limitations

- Reminders are recurring only. There is no one-time / dated reminder support.
- Malformed lines (wrong time format, unknown day names, missing label) are
  silently skipped.
- If the user denies notification permission, no notifications will be shown.
  Re-launch the app to be prompted again, or grant the permission manually in
  system settings.
- **Changes only take effect after tapping Save.** Editing `reminders.txt`
  externally has no effect until the app is opened and Save is tapped. Removed
  reminders will fire one final time at their next scheduled occurrence before
  stopping.
- On some devices (particularly Samsung, Xiaomi, and other OEM Android skins),
  aggressive battery management can interfere with exact alarms. If notifications
  stop arriving, go to Settings → Battery → Reminders and set it to
  "Unrestricted" or "Don't optimise". The exact path varies by device.
