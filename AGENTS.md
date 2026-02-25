# Agents

See README.md for design intent and config file format.

## Build

```bash
JAVA_HOME=/opt/android-studio/jbr ./gradlew assembleDebug
```

Java is not on the system PATH — it must be referenced from the Android Studio
installation as above, otherwise the build fails immediately.

## Install & adb

`adb` is also not on the system PATH. It is bundled with Android Studio at:

```bash
/home/matt/Android/Sdk/platform-tools/adb
```

Install the debug APK:

```bash
/home/matt/Android/Sdk/platform-tools/adb install app/build/outputs/apk/debug/app-debug.apk
```
