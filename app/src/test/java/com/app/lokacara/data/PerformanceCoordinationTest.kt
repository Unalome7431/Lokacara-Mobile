package com.app.lokacara.data

import com.app.lokacara.model.Event
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PerformanceCoordinationTest {

    @Test
    fun `latest request gate rejects stale generation`() {
        val gate = LatestRequestGate()
        val stale = gate.next()
        val latest = gate.next()

        assertFalse(gate.isLatest(stale))
        assertTrue(gate.isLatest(latest))
    }

    @Test
    fun `pagination merge keeps first item identity and appends unique ids`() {
        val first = event(1, "Pertama")
        val duplicate = event(1, "Duplikat")
        val second = event(2, "Kedua")

        val merged = mergeEventsById(listOf(first), listOf(duplicate, second))

        assertEquals(listOf(1L, 2L), merged.map(Event::id))
        assertEquals("Pertama", merged.first().title)
    }

    private fun event(id: Long, title: String) = Event(
        id = id,
        title = title,
        description = "",
        date = "",
        location = "",
        price = "Gratis",
        category = ""
    )
}
