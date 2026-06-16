package com.app.lokacara.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class ReminderScheduleItem(
    val label: String,
    val scheduledAt: String,
    val hasPassed: Boolean
)

object ReminderScheduleFormatter {
    private val reminderOffsets = listOf(
        7 to "H-7",
        3 to "H-3",
        1 to "H-1",
        0 to "Hari H"
    )

    fun build(
        startDatetime: String,
        nowMillis: Long = System.currentTimeMillis(),
        locale: Locale = Locale.forLanguageTag("id-ID")
    ): List<ReminderScheduleItem> {
        val eventStart = parseEventStart(startDatetime) ?: return emptyList()
        val output = SimpleDateFormat("dd MMM yyyy, HH:mm", locale)

        return reminderOffsets.map { (daysBefore, label) ->
            val scheduled = Calendar.getInstance().apply {
                time = eventStart
                add(Calendar.DAY_OF_YEAR, -daysBefore)
            }
            ReminderScheduleItem(
                label = label,
                scheduledAt = output.format(scheduled.time),
                hasPassed = scheduled.timeInMillis <= nowMillis
            )
        }
    }

    private fun parseEventStart(startDatetime: String): Date? {
        val normalized = startDatetime.trim().replace("T", " ").take(19)
        if (normalized.length < 16) return null

        val patterns = listOf("yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm")
        return patterns.firstNotNullOfOrNull { pattern ->
            runCatching {
                SimpleDateFormat(pattern, Locale.US).apply {
                    isLenient = false
                }.parse(normalized)
            }.getOrNull()
        }
    }
}
