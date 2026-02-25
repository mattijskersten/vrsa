package com.vrsa.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime

class ReminderWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        val file = File(applicationContext.getExternalFilesDir(null), "reminders.txt")
        if (!file.exists()) return Result.success()

        val prefs = applicationContext.getSharedPreferences("fired", Context.MODE_PRIVATE)
        val today = LocalDate.now()
        val now = LocalTime.now()

        file.readLines()
            .filter { it.isNotBlank() && !it.startsWith("#") }
            .forEach { line ->
                val reminder = parseLine(line) ?: return@forEach
                if (isDue(reminder, today, now) && !firedToday(prefs, line, today)) {
                    if (postNotification(reminder.label, line.hashCode())) {
                        markFired(prefs, line, today)
                    }
                }
            }

        return Result.success()
    }

    private data class Reminder(val time: LocalTime, val days: Set<DayOfWeek>, val label: String)

    private fun parseLine(line: String): Reminder? {
        val parts = line.trim().split(Regex("\\s+"), limit = 3)
        if (parts.size < 3) return null
        val time = runCatching { LocalTime.parse(parts[0]) }.getOrNull() ?: return null
        val days = parseDays(parts[1]) ?: return null
        return Reminder(time, days, parts[2])
    }

    private fun parseDays(s: String): Set<DayOfWeek>? {
        if (s.lowercase() == "daily") return DayOfWeek.entries.toSet()
        val map = mapOf(
            "mon" to DayOfWeek.MONDAY, "tue" to DayOfWeek.TUESDAY,
            "wed" to DayOfWeek.WEDNESDAY, "thu" to DayOfWeek.THURSDAY,
            "fri" to DayOfWeek.FRIDAY, "sat" to DayOfWeek.SATURDAY,
            "sun" to DayOfWeek.SUNDAY
        )
        return s.split(",").map { map[it.lowercase()] ?: return null }.toSet()
    }

    private fun isDue(r: Reminder, today: LocalDate, now: LocalTime) =
        today.dayOfWeek in r.days && now >= r.time

    private fun firedToday(prefs: android.content.SharedPreferences, line: String, today: LocalDate) =
        prefs.getString(line.hashCode().toString(), null) == today.toString()

    private fun markFired(prefs: android.content.SharedPreferences, line: String, today: LocalDate) =
        prefs.edit().putString(line.hashCode().toString(), today.toString()).apply()

    private fun postNotification(label: String, id: Int): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            applicationContext.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return false

        val notification = NotificationCompat.Builder(applicationContext, "reminders")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Reminder")
            .setContentText(label)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(applicationContext).notify(id, notification)
        return true
    }
}
