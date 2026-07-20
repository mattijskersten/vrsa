package com.vrsa.app.scheduling

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.vrsa.app.data.Reminder
import com.vrsa.app.domain.nextOccurrence
import java.time.Clock
import java.time.ZonedDateTime

/**
 * Owns the mapping from reminders to AlarmManager alarms.
 *
 * Each reminder gets exactly one pending alarm for its next occurrence,
 * keyed by the reminder's database id, so edits and deletions can always
 * cancel the matching alarm — nothing fires for stale data.
 */
class ReminderScheduler(
    private val context: Context,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    /** True when the OS lets us schedule exact alarms (revocable on API 31–32). */
    fun canScheduleExact(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

    fun schedule(reminder: Reminder) {
        if (!reminder.enabled) {
            cancel(reminder.id)
            return
        }
        val next = nextOccurrence(reminder.time, reminder.days, ZonedDateTime.now(clock))
        if (next == null) {
            cancel(reminder.id)
            return
        }
        val triggerAt = next.toInstant().toEpochMilli()
        val pi = pendingIntent(reminder.id)
        if (canScheduleExact()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        } else {
            // Degrade gracefully rather than crash with SecurityException.
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
        Log.d(TAG, "Scheduled reminder ${reminder.id} at $next (exact=${canScheduleExact()})")
    }

    fun cancel(reminderId: Long) {
        alarmManager.cancel(pendingIntent(reminderId))
    }

    fun scheduleAll(reminders: List<Reminder>) {
        reminders.forEach(::schedule)
    }

    private fun pendingIntent(reminderId: Long): PendingIntent {
        val intent = Intent(context, ReminderAlarmReceiver::class.java)
            .putExtra(ReminderAlarmReceiver.EXTRA_REMINDER_ID, reminderId)
        return PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private companion object {
        const val TAG = "ReminderScheduler"
    }
}
