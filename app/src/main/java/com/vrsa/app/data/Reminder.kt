package com.vrsa.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.DayOfWeek
import java.time.LocalTime

/**
 * A recurring weekly reminder. [daysMask] is a bitmask with bit 0 = Monday …
 * bit 6 = Sunday, so day membership survives persistence without string parsing.
 */
@Entity(tableName = "reminders")
data class Reminder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val hour: Int,
    val minute: Int,
    val daysMask: Int,
    val enabled: Boolean = true,
) {
    val time: LocalTime get() = LocalTime.of(hour, minute)
    val days: Set<DayOfWeek> get() = daysOfWeekFromMask(daysMask)
}

fun maskFromDaysOfWeek(days: Set<DayOfWeek>): Int =
    days.fold(0) { mask, day -> mask or (1 shl (day.value - 1)) }

fun daysOfWeekFromMask(mask: Int): Set<DayOfWeek> =
    DayOfWeek.entries.filterTo(sortedSetOf()) { mask and (1 shl (it.value - 1)) != 0 }

val EVERY_DAY: Set<DayOfWeek> = DayOfWeek.entries.toSet()
