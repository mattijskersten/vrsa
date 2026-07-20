# VRSA — Very Reminder; Simple App

A small, reliable Android app for recurring weekly reminders, configured
through a plain-text file edited directly in the app. This is the
professional rewrite of the original prototype: same minimalist product —
an editor and a Save button, nothing else — rebuilt on a modern stack with
the scheduling correctness issues fixed.

## Behaviour

- The app shows the raw contents of `reminders.txt` in a monospace editor.
  Edit and tap **Save** — the file is written and all alarms are rebuilt.
  A snackbar confirms how many reminders were scheduled.
- Lines that can't be parsed are listed in-app with their line number and
  reason (v1 silently dropped them).
- The file can also be edited externally (file manager, adb); changes are
  **applied automatically the next time the app opens** — no Save needed
  (v1 required a manual re-save, and removed reminders fired one last time).
- Notifications fire at exactly the scheduled time and reschedule themselves.
  Alarms survive reboots, app updates, timezone changes, and clock changes.
- In-app warnings (with one-tap fixes) when notification permission or the
  exact-alarm capability is missing.

## Config file

```
/sdcard/Android/data/com.vrsa.app/files/reminders.txt
```

One reminder per line; blank lines and `#` comments are ignored:

```
HH:MM  <days>  <label>
```

- `HH:MM` — 24-hour time (`8:00` and `08:00` both accepted)
- `days` — `daily`, or a comma-separated list of `Mon` `Tue` `Wed` `Thu`
  `Fri` `Sat` `Sun` (case-insensitive)
- `label` — free text shown in the notification

## Architecture

Single-module MVVM app, manual dependency injection (`AppContainer` in
`VrsaApplication` — Hilt would be overhead at this size).

| Layer | Contents |
|---|---|
| `domain/` | `parseConfig()` (text → reminders + per-line errors) and `nextOccurrence()` (pure, zone-aware scheduling math incl. DST gaps). Both Android-free and unit-tested. |
| `data/` | Room entity/DAO/database — the *runtime mirror* of the text file — and `ConfigRepository`, the single write path: apply text → write file, rebuild DB rows, cancel stale alarms, schedule the new set. |
| `scheduling/` | `ReminderScheduler` (one alarm per reminder, keyed by DB id), `ReminderAlarmReceiver` (fires, re-reads state from DB, reschedules), `SystemEventReceiver` (boot / update / time / timezone → reschedule all). |
| `notifications/` | Channel setup and notification building. |
| `ui/` | Compose editor screen + `EditorViewModel`. |

Why a database behind a text file: alarm intents carry only a stable row id,
so the receiver can check current state at fire time. Every Save cancels the
previous alarm set before scheduling the new one — the two v1 bugs
(`line.hashCode()` identity collisions, removed reminders firing once more)
are structurally impossible.

## Build

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew assembleDebug
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew testDebugUnitTest
```

Toolchain: Gradle 8.11.1, AGP 8.7.3, Kotlin 2.0.21, compile/target SDK 35,
min SDK 26.

Install:

```bash
~/Android/Sdk/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Upgrading from v1

Install over the old app (same application id). The existing `reminders.txt`
is preserved by the update and applied automatically on first launch.

## Testing

Unit tests cover the config parser (`ConfigParserTest`), the scheduling math
(`NextOccurrenceTest`, incl. the strictly-after boundary and a DST
spring-forward gap), and day-bitmask persistence (`DaysMaskTest`).

Manual smoke test: add a reminder a couple of minutes ahead, Save, wait for
the heads-up notification; or push a file via adb and just reopen the app.

## Known limitations

- Weekly recurring reminders only — no one-off dated reminders, no snooze.
- OEM battery managers (Samsung/Xiaomi etc.) can still suppress alarms; set
  the app to "Unrestricted" battery usage if notifications stop.
