package com.app.lokacara.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class ReminderScheduleFormatterTest {
    @Test
    fun build_validStartDatetime_returnsExpectedOffsets() {
        val schedule = ReminderScheduleFormatter.build(
            startDatetime = "2026-06-15T10:30:00",
            nowMillis = 0L,
            locale = Locale.US
        )

        assertEquals(listOf("H-7", "H-3", "H-1", "Hari H"), schedule.map { it.label })
        assertTrue(schedule.all { it.scheduledAt.contains("10:30") })
        assertTrue(schedule.all { !it.hasPassed })
    }

    @Test
    fun build_futureNow_marksSchedulesAsPassed() {
        val schedule = ReminderScheduleFormatter.build(
            startDatetime = "2026-06-15T10:30:00",
            nowMillis = Long.MAX_VALUE,
            locale = Locale.US
        )

        assertTrue(schedule.all { it.hasPassed })
    }

    @Test
    fun build_invalidStartDatetime_returnsEmptyList() {
        val schedule = ReminderScheduleFormatter.build("not-a-date")

        assertTrue(schedule.isEmpty())
    }
}
