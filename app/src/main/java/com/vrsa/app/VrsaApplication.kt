package com.vrsa.app

import android.app.Application
import com.vrsa.app.data.ConfigRepository
import com.vrsa.app.data.ReminderDao
import com.vrsa.app.data.VrsaDatabase
import com.vrsa.app.notifications.ReminderNotifier
import com.vrsa.app.scheduling.ReminderScheduler

/**
 * Manual dependency container — deliberate choice over Hilt for an app this
 * size: same testability, no annotation-processing build cost.
 */
class AppContainer(application: Application) {
    private val database = VrsaDatabase.build(application)
    val reminderDao: ReminderDao = database.reminderDao()
    val scheduler = ReminderScheduler(application)
    val notifier = ReminderNotifier(application)
    val repository = ConfigRepository(application, reminderDao, scheduler)
}

class VrsaApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }

    override fun onCreate() {
        super.onCreate()
        container.notifier.ensureChannel()
    }
}
