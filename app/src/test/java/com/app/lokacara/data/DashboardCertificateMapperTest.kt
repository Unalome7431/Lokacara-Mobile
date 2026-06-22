package com.app.lokacara.data

import com.app.lokacara.data.remote.dto.CertificateDto
import com.app.lokacara.data.remote.dto.CertificateEventDto
import com.app.lokacara.data.remote.dto.CertificateRegistrationDto
import com.app.lokacara.data.remote.dto.DashboardResponse
import org.junit.Assert.assertEquals
import org.junit.Test

class DashboardCertificateMapperTest {
    @Test
    fun `dashboard certificates retain event ids and count`() {
        val response = DashboardResponse(certificates = listOf(certificate(1, 10), certificate(2, 20)))
        val mapped = mapDashboardCertificates(response)
        assertEquals(2, mapped.size)
        assertEquals(listOf(10L, 20L), mapped.map { it.eventId })
    }

    @Test
    fun `download filename is sanitized`() {
        assertEquals("sertifikat_Event_Solo_2026.jpg", certificateDownloadFileName("Event Solo 2026"))
    }

    private fun certificate(id: Long, eventId: Long) = CertificateDto(
        id = id,
        event_registration = CertificateRegistrationDto(
            id = id,
            event = CertificateEventDto(id = eventId, title = "Event $eventId")
        )
    )
}
