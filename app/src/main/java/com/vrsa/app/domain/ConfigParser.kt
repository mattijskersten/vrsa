package com.vrsa.app.domain

import com.vrsa.app.data.Reminder
import com.vrsa.app.data.maskFromDaysOfWeek
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/**
 * Parser for the plain-text reminder config.
 *
 * Format, one reminder per line (blank lines and `#` comments ignored):
 *
 *     HH:MM  <daily|Mon,Tue,...>  <label>
 *
 * Unlike the v1 parser, malformed lines are collected as [ParseError]s with
 * line numbers and reasons instead of being silently dropped.
 */

data class ParseError(val lineNumber: Int, val line: String, val reason: String)

data class ParseResult(val reminders: List<Reminder>, val errors: List<ParseError>)

private val TIME_FORMAT = DateTimeFormatter.ofPattern("H:mm")

private val DAY_NAMES = mapOf(
    "mon" to DayOfWeek.MONDAY, "tue" to DayOfWeek.TUESDAY,
    "wed" to DayOfWeek.WEDNESDAY, "thu" to DayOfWeek.THURSDAY,
    "fri" to DayOfWeek.FRIDAY, "sat" to DayOfWeek.SATURDAY,
    "sun" to DayOfWeek.SUNDAY,
)

fun parseConfig(text: String): ParseResult {
    val reminders = mutableListOf<Reminder>()
    val errors = mutableListOf<ParseError>()

    text.lines().forEachIndexed { index, raw ->
        val line = raw.trim()
        if (line.isEmpty() || line.startsWith("#")) return@forEachIndexed

        fun error(reason: String) {
            errors += ParseError(index + 1, line, reason)
        }

        val parts = line.split(Regex("\\s+"), limit = 3)
        if (parts.size < 3) {
            error("expected: HH:MM  <days>  <label>")
            return@forEachIndexed
        }

        val time = try {
            LocalTime.parse(parts[0], TIME_FORMAT)
        } catch (_: DateTimeParseException) {
            error("'${parts[0]}' is not a valid time (use HH:MM, 24-hour)")
            return@forEachIndexed
        }

        val days = parseDays(parts[1]) ?: run {
            error("'${parts[1]}' is not 'daily' or a comma-separated list of Mon…Sun")
            return@forEachIndexed
        }

        reminders += Reminder(
            label = parts[2],
            hour = time.hour,
            minute = time.minute,
            daysMask = maskFromDaysOfWeek(days),
        )
    }
    return ParseResult(reminders, errors)
}

private fun parseDays(spec: String): Set<DayOfWeek>? {
    if (spec.equals("daily", ignoreCase = true)) return DayOfWeek.entries.toSet()
    val days = spec.split(",").map { DAY_NAMES[it.trim().lowercase()] ?: return null }
    return days.toSet()
}
