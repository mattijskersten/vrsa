# Very Reminder; Simple App (VRSA) — Specification

*Wordplay on "Very Simple Reminder App", in the style of a doge meme. Pronounced vur-sah.*

---

## Overview

A headless Android app that reads a plain-text config file and fires
notifications at scheduled times. There is no UI. Configuration is done
entirely by editing a text file with any file manager or text editor app.

---

## Behaviour

- **The app must be launched at least once** to schedule the background job.
  Until then, no notifications will fire.
- On first launch, the app requests notification permission (Android 13+),
  schedules a background job, then closes. There is no visible UI.
- On subsequent launches the app closes immediately (the background job is
  already running).
- A background job runs approximately every 15 minutes. Each run:
  1. Reads the config file.
  2. For each reminder line, checks whether it is due.
  3. Fires a notification for any reminder that is due and has not already
     fired today.
- Reminders fire at most once per day per line.
- Notifications may arrive up to 15 minutes after the scheduled time.
- The background job persists across reboots without any user action.

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
does not exist, the app creates it with commented-out format examples. Edit it
with any Android file manager or text editor that supports the path above.

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

| Permission           | When requested        | Purpose                          |
|----------------------|-----------------------|----------------------------------|
| `POST_NOTIFICATIONS` | On first launch (Android 13+) | Required to show notifications |

No network, location, or other permissions are used.

---

## Testing

WorkManager's 15-minute polling interval makes manual testing slow. Use these
adb commands to trigger the worker immediately after installing:

Force-run the WorkManager job:
```bash
adb shell cmd jobscheduler run -f com.vrsa.app 0
```

Check WorkManager diagnostics:
```bash
adb shell am broadcast -a androidx.work.diagnostics.REQUEST_DIAGNOSTICS_INFO -p com.vrsa.app
```

`adb` is not on the system PATH — see AGENTS.md for the full path.

---

## Limitations

- Reminders are recurring only. There is no one-time / dated reminder support.
- Notifications may arrive up to ~15 minutes late due to the polling interval.
- If the config file does not exist, the app creates it with format examples as comments.
- Malformed lines (wrong time format, unknown day names, missing label) are
  silently skipped.
- If the user denies notification permission, no notifications will be shown.
  Re-launch the app to be prompted again, or grant the permission manually in
  system settings.
- The app does not watch the file for changes. Edits take effect at the next
  poll cycle (within 15 minutes).
- On some devices (particularly Samsung, Xiaomi, and other OEM Android skins),
  aggressive battery management can silently prevent background jobs from running.
  If notifications stop arriving, go to Settings → Battery → Reminders and set
  it to "Unrestricted" or "Don't optimise". The exact path varies by device.
