package com.vrsa.app

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.io.File
import java.util.concurrent.TimeUnit

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
        } else {
            scheduleWork()
            finish()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        scheduleWork()
        finish()
    }

    private fun scheduleWork() {
        createConfigFile()
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "reminders",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
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
