package com.app.lokacara.data

import com.app.lokacara.data.remote.dto.DashboardResponse
import com.app.lokacara.model.CertificateData

fun mapDashboardCertificates(dashboard: DashboardResponse): List<CertificateData> =
    dashboard.certificates.map { certificate ->
        CertificateData(
            id = certificate.id.toString(),
            eventId = certificate.event_registration?.event?.id ?: 0L,
            title = certificate.event_registration?.event?.title ?: "Sertifikat",
            date = certificate.issued_at?.take(10) ?: "",
            time = "",
            location = "",
            category = ""
        )
    }

fun certificateDownloadFileName(eventTitle: String): String {
    val safeTitle = eventTitle.replace(Regex("[^a-zA-Z0-9_-]"), "_").ifBlank { "event" }.take(40)
    return "sertifikat_$safeTitle.jpg"
}
