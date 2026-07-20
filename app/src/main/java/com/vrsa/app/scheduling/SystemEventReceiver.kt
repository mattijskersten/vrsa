package com.vrsa.app.scheduling

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.vrsa.app.VrsaApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Re-schedules every enabled reminder after events that invalidate pending
 * alarms: reboot, app update, and wall-clock or timezone changes.
 */
class SystemEventReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in HANDLED_ACTIONS) return

        val container = (context.applicationContext as VrsaApplication).container
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                container.scheduler.scheduleAll(container.reminderDao.getAll())
            } finally {
                pendingResult.finish()
            }
        }
    }

    private companion object {
        val HANDLED_ACTIONS = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
        )
    }
}
