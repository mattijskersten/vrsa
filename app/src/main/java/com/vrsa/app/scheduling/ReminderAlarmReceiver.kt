package com.vrsa.app.scheduling

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.vrsa.app.VrsaApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Fires when a reminder's alarm goes off. The intent carries only the
 * reminder id; current state (label, enabled, schedule) is read from the
 * database, so a reminder edited or deleted after scheduling never produces
 * a stale notification.
 */
class ReminderAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
        if (reminderId < 0) return

        val container = (context.applicationContext as VrsaApplication).container
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val reminder = container.reminderDao.getById(reminderId) ?: return@launch
                if (!reminder.enabled) return@launch
                container.notifier.showReminder(reminder)
                container.scheduler.schedule(reminder)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_REMINDER_ID = "reminder_id"
    }
}
