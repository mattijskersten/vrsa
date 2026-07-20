package com.vrsa.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminders")
    suspend fun getAll(): List<Reminder>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getById(id: Long): Reminder?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(reminder: Reminder): Long

    @Query("DELETE FROM reminders")
    suspend fun clearAll()
}
