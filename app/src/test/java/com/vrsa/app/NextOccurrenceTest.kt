package com.vrsa.app

import com.vrsa.app.domain.nextOccurrence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class NextOccurrenceTest {

    private val zone = ZoneId.of("Europe/Amsterdam")

    // Wednesday 2026-07-15, 10:00
    private val wednesdayMorning =
        ZonedDateTime.of(2026, 7, 15, 10, 0, 0, 0, zone)

    private fun at(y: Int, m: Int, d: Int, h: Int, min: Int) =
        ZonedDateTime.of(y, m, d, h, min, 0, 0, zone)

    @Test
    fun `later today when time has not passed`() {
        val next = nextOccurrence(LocalTime.of(22, 30), setOf(DayOfWeek.WEDNESDAY), wednesdayMorning)
        assertEquals(at(2026, 7, 15, 22, 30), next)
    }

    @Test
    fun `next week when today's time already passed`() {
        val next = nextOccurrence(LocalTime.of(8, 0), setOf(DayOfWeek.WEDNESDAY), wednesdayMorning)
        assertEquals(at(2026, 7, 22, 8, 0), next)
    }

    @Test
    fun `skips to nearest selected day`() {
        val next = nextOccurrence(
            LocalTime.of(9, 0),
            setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY),
            wednesdayMorning,
        )
        assertEquals(at(2026, 7, 18, 9, 0), next)
    }

    @Test
    fun `exact boundary is not 'after' - fires next occurrence instead`() {
        val atTen = wednesdayMorning
        val next = nextOccurrence(LocalTime.of(10, 0), setOf(DayOfWeek.WEDNESDAY), atTen)
        assertEquals(at(2026, 7, 22, 10, 0), next)
    }

    @Test
    fun `daily reminder fires tomorrow when today's time passed`() {
        val next = nextOccurrence(LocalTime.of(8, 0), DayOfWeek.entries.toSet(), wednesdayMorning)
        assertEquals(at(2026, 7, 16, 8, 0), next)
    }

    @Test
    fun `empty day set yields null`() {
        assertNull(nextOccurrence(LocalTime.of(8, 0), emptySet(), wednesdayMorning))
    }

    @Test
    fun `spring forward gap resolves to shifted wall time`() {
        // In Europe/Amsterdam, 2026-03-29 02:00–03:00 does not exist.
        val saturdayBefore = ZonedDateTime.of(2026, 3, 28, 12, 0, 0, 0, zone)
        val next = nextOccurrence(LocalTime.of(2, 30), setOf(DayOfWeek.SUNDAY), saturdayBefore)
        // ZonedDateTime resolves the gap by shifting forward one hour.
        assertEquals(ZonedDateTime.of(2026, 3, 29, 3, 30, 0, 0, zone), next)
    }

    @Test
    fun `result is strictly in the future`() {
        val next = nextOccurrence(LocalTime.of(10, 0), DayOfWeek.entries.toSet(), wednesdayMorning)!!
        assert(next.isAfter(wednesdayMorning))
    }
}
