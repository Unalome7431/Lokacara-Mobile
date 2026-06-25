package com.app.lokacara.data

import com.app.lokacara.data.remote.dto.AttendeeDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class OrganizerPerformanceRegressionTest {

    @Test
    fun `attendee pagination deduplicates registration ids`() {
        val first = attendee(1, "registered")
        val second = attendee(2, "registered")

        val result = mergeAttendeesById(listOf(first), listOf(first.copy(status = "present"), second))

        assertEquals(listOf(1L, 2L), result.map(AttendeeDto::id))
        assertEquals("registered", result.first().status)
    }

    @Test
    fun `attendee status replacement preserves unaffected row identity`() {
        val first = attendee(1, "registered")
        val second = attendee(2, "registered")

        val result = replaceAttendeeById(listOf(first, second), first.copy(status = "present"))

        assertEquals("present", result.first().status)
        assertSame(second, result.last())
    }

    @Test
    fun `create event readiness includes organizer contact checks`() {
        val completed = completedEventRequirements(
            hasName = true,
            hasCategory = true,
            hasOrganizer = true,
            hasContact = true,
            hasSchedule = true,
            hasLocation = false,
            hasDescription = true,
            hasPrice = true,
            hasValidCapacity = true
        )

        assertEquals(8, completed)
    }

    private fun attendee(id: Long, status: String) = AttendeeDto(
        id = id,
        user_id = id,
        event_id = 10,
        status = status
    )
}
