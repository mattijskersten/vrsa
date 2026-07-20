package com.vrsa.app.domain

import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZonedDateTime

/**
 * Pure scheduling math, kept free of Android dependencies so it is unit-testable.
 *
 * Returns the next moment strictly after [from] at which a reminder with the
 * given [time] and [days] should fire, or null when [days] is empty.
 * All arithmetic is zone-aware, so DST transitions resolve the way a wall
 * clock would (a 02:30 reminder on a spring-forward day fires at the shifted
 * wall time chosen by the zone rules rather than being dropped).
 */
fun nextOccurrence(time: LocalTime, days: Set<DayOfWeek>, from: ZonedDateTime): ZonedDateTime? {
    if (days.isEmpty()) return null
    for (daysAhead in 0..7L) {
        val date = from.toLocalDate().plusDays(daysAhead)
        if (date.dayOfWeek !in days) continue
        val candidate = ZonedDateTime.of(date, time, from.zone)
        if (candidate.isAfter(from)) return candidate
    }
    return null // unreachable when days is non-empty; keeps the contract explicit
}
