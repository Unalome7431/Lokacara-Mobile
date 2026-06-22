package com.app.lokacara.data

import com.app.lokacara.model.UpcomingEvent
import org.junit.Assert.assertEquals
import org.junit.Test

class UpcomingEventCollectionsTest {
    @Test
    fun `upcoming events remove invalid duplicates and sort nearest first`() {
        val events = listOf(
            event(2, 3_000), event(1, 2_000), event(1, 2_000),
            event(3, 500), event(4, 4_000, "cancelled"), event(5, 5_000)
        )
        assertEquals(listOf(1L, 2L, 5L), normalizeUpcomingEvents(events, nowMillis = 1_000).map { it.id })
    }

    @Test
    fun `upcoming presentation is limited to hero plus three items`() {
        val events = (1L..6L).map { event(it, 1_000 + it) }
        assertEquals(4, normalizeUpcomingEvents(events, nowMillis = 0).size)
    }

    private fun event(id: Long, start: Long, status: String = "active") = UpcomingEvent(
        id = id, title = "Event $id", date = "", time = "", location = "",
        type = "offline", startEpoch = start, status = status
    )
}
