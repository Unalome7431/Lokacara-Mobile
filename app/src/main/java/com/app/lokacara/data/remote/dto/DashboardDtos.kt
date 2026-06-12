package com.app.lokacara.data.remote.dto

data class DashboardResponse(
    val joined_events: List<RegistrationDto> = emptyList(),
    val hosted_events: List<EventDto> = emptyList(),
    val certificates: List<CertificateDto> = emptyList()
)

data class RegistrationDto(
    val id: Long,
    val user_id: Long,
    val event_id: Long,
    val qr_token: String? = null,
    val status: String = "confirmed",
    val checked_in_at: String? = null,
    val registered_at: String? = null,
    val event: EventDto? = null
)

data class CertificateDto(
    val id: Long,
    val registration_id: Long? = null,
    val file_url: String? = null,
    val issued_at: String? = null,
    val event_registration: CertificateRegistrationDto? = null
)

data class CertificateRegistrationDto(
    val id: Long,
    val event: CertificateEventDto? = null
)

data class CertificateEventDto(
    val id: Long,
    val title: String? = null
)
