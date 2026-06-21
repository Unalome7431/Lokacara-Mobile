package com.app.lokacara.data

import com.app.lokacara.model.CertificateData
import com.app.lokacara.model.NotificationItem
import com.app.lokacara.model.NotificationType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class DashboardUiCollectionsTest {

    @Test
    fun `notification filtering retains source item identity`() {
        val social = notification("1", NotificationType.SOCIAL)
        val system = notification("2", NotificationType.SYSTEM)

        val filtered = filterNotifications(listOf(social, system), tabIndex = 0)

        assertEquals(1, filtered.size)
        assertSame(social, filtered.single())
    }

    @Test
    fun `certificate update changes only matching item`() {
        val first = certificate("1")
        val second = certificate("2")

        val updated = markCertificateDownloaded(listOf(first, second), "1", "/certificate.jpg")

        assertEquals("/certificate.jpg", updated.first().filePath)
        assertSame(second, updated.last())
    }

    private fun notification(id: String, type: NotificationType) = NotificationItem(
        id = id,
        message = "Pesan",
        time = "10:00",
        dateGroup = "Hari ini",
        type = type,
        isRead = false
    )

    private fun certificate(id: String) = CertificateData(
        id = id,
        title = "Sertifikat",
        date = "",
        time = "",
        location = "",
        category = ""
    )
}
