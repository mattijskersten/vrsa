package com.vrsa.app

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import java.io.File
import java.time.DayOfWeek
import java.time.LocalTime

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val label = intent.getStringExtra("label") ?: return
        val hour = intent.getIntExtra("hour", -1)
        val minute = intent.getIntExtra("minute", -1)
        val dayOrdinals = intent.getIntArrayExtra("days") ?: return
        val requestCode = intent.getIntExtra("requestCode", 0)

        if (hour < 0 || minute < 0) return

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            val notification = NotificationCompat.Builder(context, "reminders")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Reminder")
                .setContentText(label)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()
            NotificationManagerCompat.from(context).notify(requestCode, notification)
        }

        // Reschedule for next occurrence only if reminder still exists in config
        val file = File(context.getExternalFilesDir(null), "reminders.txt")
        if (!file.exists()) return

        val days = dayOrdinals.map { DayOfWeek.of(it + 1) }.toSet()
        val reminder = Reminder(LocalTime.of(hour, minute), days, label)

        val stillExists = file.readLines()
            .filter { it.isNotBlank() && !it.startsWith("#") }
            .any { parseLine(it) == reminder }

        if (stillExists) {
            scheduleAlarm(context, reminder, requestCode)
        }
    }
}
