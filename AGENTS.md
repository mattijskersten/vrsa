# Agents

See README.md for architecture and design decisions.

## Build

Java is not on the default toolchain path Gradle expects — set `JAVA_HOME`
explicitly:

```bash
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew assembleDebug
JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 ./gradlew testDebugUnitTest
```

The Android SDK lives at `/home/matt/Android/Sdk` (see `local.properties`).

## adb

`adb` is not on PATH:

```bash
/home/matt/Android/Sdk/platform-tools/adb install app/build/outputs/apk/debug/app-debug.apk
```

## Conventions

- All user-visible text goes in `res/values/strings.xml`.
- The text file is the user-facing source of truth; the Room DB is its
  runtime mirror. All mutations go through `ConfigRepository.apply()` so
  file, database, and AlarmManager can never drift — never call the DAO
  directly from UI code.
- `domain/` (`ConfigParser.kt`, `NextOccurrence.kt`) must stay free of
  Android imports (it's the unit-tested core).
