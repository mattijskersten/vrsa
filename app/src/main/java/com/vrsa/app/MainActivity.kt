package com.vrsa.app

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import java.io.File

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
        } else {
            scheduleAlarms()
            finish()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        scheduleAlarms()
        finish()
    }

    private fun scheduleAlarms() {
        createConfigFile()
        val file = File(getExternalFilesDir(null), "reminders.txt")
        file.readLines()
            .filter { it.isNotBlank() && !it.startsWith("#") }
            .forEach { line ->
                val reminder = parseLine(line) ?: return@forEach
                scheduleAlarm(this, reminder, line.hashCode())
            }
    }

    private fun createConfigFile() {
        val file = File(getExternalFilesDir(null), "reminders.txt")
        if (!file.exists()) {
            file.writeText(
                "# Reminders config file\n" +
                "# One reminder per line: HH:MM  <days>  <label>\n" +
                "# Days: daily  OR  Mon,Tue,Wed,Thu,Fri,Sat,Sun (comma-separated)\n" +
                "#\n" +
                "# Examples:\n" +
                "# 08:00  daily                Morning alarm\n" +
                "# 09:00  Mon,Tue,Wed,Thu,Fri  Weekday reminder\n" +
                "# 22:30  Fri,Sat              Weekend late reminder\n"
            )
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel("reminders", "Reminders", NotificationManager.IMPORTANCE_HIGH)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
