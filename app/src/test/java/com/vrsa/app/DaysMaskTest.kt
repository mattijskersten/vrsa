package com.vrsa.app

import com.vrsa.app.data.daysOfWeekFromMask
import com.vrsa.app.data.maskFromDaysOfWeek
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek

class DaysMaskTest {

    @Test
    fun `round trips every subset boundary case`() {
        val cases = listOf(
            emptySet(),
            setOf(DayOfWeek.MONDAY),
            setOf(DayOfWeek.SUNDAY),
            setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
            DayOfWeek.entries.toSet(),
        )
        for (days in cases) {
            assertEquals(days, daysOfWeekFromMask(maskFromDaysOfWeek(days)))
        }
    }

    @Test
    fun `mask uses bit 0 for Monday through bit 6 for Sunday`() {
        assertEquals(0b0000001, maskFromDaysOfWeek(setOf(DayOfWeek.MONDAY)))
        assertEquals(0b1000000, maskFromDaysOfWeek(setOf(DayOfWeek.SUNDAY)))
        assertEquals(0b1111111, maskFromDaysOfWeek(DayOfWeek.entries.toSet()))
    }
}
