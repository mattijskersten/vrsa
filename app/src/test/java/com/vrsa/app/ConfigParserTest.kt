package com.vrsa.app

import com.vrsa.app.data.maskFromDaysOfWeek
import com.vrsa.app.domain.parseConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek

class ConfigParserTest {

    @Test
    fun `parses valid lines, skipping comments and blanks`() {
        val result = parseConfig(
            """
            # comment
            08:00  daily                Take medication

            22:30  Mon,Wed,Fri          Evening reminder
            """.trimIndent()
        )
        assertTrue(result.errors.isEmpty())
        assertEquals(2, result.reminders.size)

        val first = result.reminders[0]
        assertEquals(8, first.hour)
        assertEquals(0, first.minute)
        assertEquals(maskFromDaysOfWeek(DayOfWeek.entries.toSet()), first.daysMask)
        assertEquals("Take medication", first.label)

        val second = result.reminders[1]
        assertEquals(
            maskFromDaysOfWeek(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)),
            second.daysMask,
        )
    }

    @Test
    fun `label keeps internal whitespace and fields split on tabs`() {
        val result = parseConfig("09:15\tSat,Sun\tMorning walk in the park")
        assertEquals("Morning walk in the park", result.reminders.single().label)
    }

    @Test
    fun `accepts single-digit hour and case-insensitive days`() {
        val result = parseConfig("8:05  mOn,SUN  x")
        assertTrue(result.errors.isEmpty())
        assertEquals(8, result.reminders.single().hour)
        assertEquals(5, result.reminders.single().minute)
    }

    @Test
    fun `reports malformed lines with line numbers instead of dropping them`() {
        val result = parseConfig(
            """
            08:00  daily  ok
            25:00  daily  bad time
            08:00  Mon,Funday  bad day
            08:00  missing-label
            """.trimIndent()
        )
        assertEquals(1, result.reminders.size)
        assertEquals(listOf(2, 3, 4), result.errors.map { it.lineNumber })
        assertTrue(result.errors[0].reason.contains("25:00"))
        assertTrue(result.errors[1].reason.contains("Mon,Funday"))
    }

    @Test
    fun `empty input parses to nothing`() {
        val result = parseConfig("")
        assertTrue(result.reminders.isEmpty())
        assertTrue(result.errors.isEmpty())
    }
}
