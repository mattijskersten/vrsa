package com.vrsa.app.data

import android.content.Context
import com.vrsa.app.domain.ParseResult
import com.vrsa.app.domain.parseConfig
import com.vrsa.app.scheduling.ReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Owns the plain-text config file and keeps the runtime state (Room + alarms)
 * in sync with it.
 *
 * The text file is the user-facing source of truth; the database is the
 * runtime mirror the alarm receiver reads at fire time. [apply] rebuilds the
 * mirror atomically: cancel every previously scheduled alarm, replace the
 * rows, schedule the new set — so removed reminders never fire again (a v1
 * bug) and edits take effect immediately.
 */
class ConfigRepository(
    context: Context,
    private val dao: ReminderDao,
    private val scheduler: ReminderScheduler,
) {
    /** Same externally-accessible location as v1, so file managers can edit it. */
    val configFile: File = File(context.getExternalFilesDir(null), "reminders.txt")

    suspend fun loadText(): String = withContext(Dispatchers.IO) {
        if (!configFile.exists()) {
            configFile.parentFile?.mkdirs()
            configFile.writeText(DEFAULT_TEMPLATE)
        }
        configFile.readText()
    }

    /** Writes [text] to the config file and syncs database + alarms to it. */
    suspend fun apply(text: String): ParseResult = withContext(Dispatchers.IO) {
        configFile.parentFile?.mkdirs()
        configFile.writeText(text)

        val result = parseConfig(text)
        dao.getAll().forEach { scheduler.cancel(it.id) }
        dao.clearAll()
        result.reminders.forEach { reminder ->
            val id = dao.upsert(reminder)
            scheduler.schedule(reminder.copy(id = id))
        }
        result
    }

    suspend fun rescheduleAll() = withContext(Dispatchers.IO) {
        scheduler.scheduleAll(dao.getAll())
    }

    private companion object {
        val DEFAULT_TEMPLATE = """
            # Reminders config file
            # One reminder per line: HH:MM  <days>  <label>
            # Days: daily  OR  Mon,Tue,Wed,Thu,Fri,Sat,Sun (comma-separated)
            #
            # Examples:
            # 08:00  daily                Morning alarm
            # 09:00  Mon,Tue,Wed,Thu,Fri  Weekday reminder
            # 22:30  Fri,Sat              Weekend late reminder
        """.trimIndent() + "\n"
    }
}
