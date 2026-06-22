package com.app.lokacara.data

import com.app.lokacara.model.Event
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HostEventActionRulesTest {
    @Test
    fun `active future event can be cancelled`() {
        assertTrue(canHostCancelEvent(event(status = "active", start = 2_000), nowMillis = 1_000))
    }

    @Test
    fun `started or cancelled event cannot be cancelled`() {
        assertFalse(canHostCancelEvent(event(status = "active", start = 500), nowMillis = 1_000))
        assertFalse(canHostCancelEvent(event(status = "cancelled", start = 2_000), nowMillis = 1_000))
    }

    private fun event(status: String, start: Long) = Event(
        id = 1, title = "Event", description = "", date = "", dateEpoch = start,
        location = "", price = "Gratis", category = "", status = status
    )
}
