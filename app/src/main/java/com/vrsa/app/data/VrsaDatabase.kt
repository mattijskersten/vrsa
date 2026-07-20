package com.vrsa.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Reminder::class], version = 1, exportSchema = false)
abstract class VrsaDatabase : RoomDatabase() {
    abstract fun reminderDao(): ReminderDao

    companion object {
        fun build(context: Context): VrsaDatabase =
            Room.databaseBuilder(context, VrsaDatabase::class.java, "vrsa.db").build()
    }
}
