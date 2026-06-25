package com.app.lokacara.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.TimeZone

class NotificationDateFormatterTest {
    @Test
    fun `formatTime converts utc iso timestamp to local time`() = withDefaultTimeZone("Asia/Jakarta") {
        assertEquals(
            "17:30 waktu lokal",
            NotificationDateFormatter.formatTime("2026-06-25T10:30:00Z")
        )
    }

    @Test
    fun `formatTime converts fractional utc timestamp to local time`() = withDefaultTimeZone("Asia/Jakarta") {
        assertEquals(
            "17:30 waktu lokal",
            NotificationDateFormatter.formatTime("2026-06-25T10:30:00.000000Z")
        )
    }

    @Test
    fun `formatTime treats timestamp without zone as utc and converts to local time`() =
        withDefaultTimeZone("Asia/Jakarta") {
            assertEquals(
                "17:30 waktu lokal",
                NotificationDateFormatter.formatTime("2026-06-25 10:30:00")
            )
        }

    private fun withDefaultTimeZone(zoneId: String, block: () -> Unit) {
        val original = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone(zoneId))
            block()
        } finally {
            TimeZone.setDefault(original)
        }
    }
}
