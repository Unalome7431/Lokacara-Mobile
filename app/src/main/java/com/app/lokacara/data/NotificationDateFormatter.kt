package com.app.lokacara.data

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.Locale

object NotificationDateFormatter {
    private val dayNames = arrayOf("Minggu", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu")
    private val monthNames = arrayOf(
        "Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
        "Jul", "Agu", "Sep", "Okt", "Nov", "Des"
    )
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.US)
    private val dateTimeFormatters = listOf(
        flexibleFormatter("yyyy-MM-dd'T'HH:mm:ss"),
        flexibleFormatter("yyyy-MM-dd HH:mm:ss")
    )
    private val fallbackTimeRegex = Regex("""\b\d{2}:\d{2}\b""")

    fun formatTime(isoDate: String?): String {
        if (isoDate.isNullOrBlank()) return ""
        val localTime = parseInstant(isoDate)
            ?.atZone(ZoneId.systemDefault())
            ?.toLocalTime()
            ?.format(timeFormatter)
        return localTime?.let { "$it waktu lokal" } ?: fallbackTime(isoDate)
    }

    fun formatDateGroup(isoDate: String?): String {
        if (isoDate.isNullOrBlank()) return ""
        val date = parseInstant(isoDate)
            ?.atZone(ZoneId.systemDefault())
            ?.toLocalDate()
            ?: return isoDate.take(10)
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        return when {
            date == today -> "Hari ini"
            date == yesterday -> "Kemarin"
            date.year == today.year -> {
                val dayName = dayNames[date.dayOfWeek.value % 7]
                val month = monthNames[date.monthValue - 1]
                "$dayName, ${date.dayOfMonth} $month"
            }
            else -> {
                val dayName = dayNames[date.dayOfWeek.value % 7]
                val month = monthNames[date.monthValue - 1]
                "$dayName, ${date.dayOfMonth} $month ${date.year}"
            }
        }
    }

    private fun parseInstant(rawValue: String): Instant? {
        val value = rawValue.trim()
        parseWithZone(value)?.let { return it }

        val normalized = value.substringBefore('+').substringBefore('Z').take(26)
        return dateTimeFormatters.firstNotNullOfOrNull { formatter ->
            runCatching {
                LocalDateTime.parse(normalized, formatter).atZone(ZoneId.of("UTC")).toInstant()
            }.getOrNull()
        }
    }

    private fun parseWithZone(value: String): Instant? {
        return runCatching { Instant.parse(value) }.getOrNull()
            ?: runCatching { OffsetDateTime.parse(value).toInstant() }.getOrNull()
    }

    private fun flexibleFormatter(pattern: String): DateTimeFormatter {
        return DateTimeFormatterBuilder()
            .appendPattern(pattern)
            .optionalStart()
            .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
            .optionalEnd()
            .toFormatter(Locale.US)
    }

    private fun fallbackTime(value: String): String {
        return fallbackTimeRegex.find(value)?.value ?: value.substringAfter("T").take(5)
    }
}
