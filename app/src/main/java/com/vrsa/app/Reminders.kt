package com.vrsa.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

data class Reminder(val time: LocalTime, val days: Set<DayOfWeek>, val label: String)

fun parseLine(line: String): Reminder? {
    val parts = line.trim().split(Regex("\\s+"), limit = 3)
    if (parts.size < 3) return null
    val time = runCatching { LocalTime.parse(parts[0]) }.getOrNull() ?: return null
    val days = parseDays(parts[1]) ?: return null
    return Reminder(time, days, parts[2])
}

fun parseDays(s: String): Set<DayOfWeek>? {
    if (s.lowercase() == "daily") return DayOfWeek.entries.toSet()
    val map = mapOf(
        "mon" to DayOfWeek.MONDAY, "tue" to DayOfWeek.TUESDAY,
        "wed" to DayOfWeek.WEDNESDAY, "thu" to DayOfWeek.THURSDAY,
        "fri" to DayOfWeek.FRIDAY, "sat" to DayOfWeek.SATURDAY,
        "sun" to DayOfWeek.SUNDAY
    )
    return s.split(",").map { map[it.lowercase()] ?: return null }.toSet()
}

fun scheduleAlarm(context: Context, reminder: Reminder, requestCode: Int) {
    val intent = Intent(context, AlarmReceiver::class.java).apply {
        putExtra("label", reminder.label)
        putExtra("hour", reminder.time.hour)
        putExtra("minute", reminder.time.minute)
        putExtra("days", reminder.days.map { it.ordinal }.toIntArray())
        putExtra("requestCode", requestCode)
    }
    val pi = PendingIntent.getBroadcast(
        context, requestCode, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    context.getSystemService(AlarmManager::class.java)
        .setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextOccurrenceMillis(reminder), pi)
}

fun nextOccurrenceMillis(reminder: Reminder): Long {
    val now = LocalDateTime.now()
    val todayAtTime = now.toLocalDate().atTime(reminder.time)
    if (now.dayOfWeek in reminder.days && todayAtTime.isAfter(now)) {
        return todayAtTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
    for (daysAhead in 1..7) {
        val candidate = now.toLocalDate().plusDays(daysAhead.toLong())
        if (candidate.dayOfWeek in reminder.days) {
            return candidate.atTime(reminder.time).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }
    }
    error("No next occurrence found for reminder: ${reminder.label}")
}
