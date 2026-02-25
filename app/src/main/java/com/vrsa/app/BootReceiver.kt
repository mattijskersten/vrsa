package com.vrsa.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.io.File

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val file = File(context.getExternalFilesDir(null), "reminders.txt")
        if (!file.exists()) return
        file.readLines()
            .filter { it.isNotBlank() && !it.startsWith("#") }
            .forEach { line ->
                val reminder = parseLine(line) ?: return@forEach
                scheduleAlarm(context, reminder, line.hashCode())
            }
    }
}
