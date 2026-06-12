package com.app.lokacara.data.remote.dto

data class QrTicketResponse(
    val event: EventDto,
    val registration: RegistrationDto
)

data class ScanRequest(
    val qr_token: String
)

data class ScanResponse(
    val message: String,
    val registration: RegistrationDto
)

data class AttendeesResponse(
    val event: EventDto,
    val attendees: PaginatedAttendees
)

data class PaginatedAttendees(
    val data: List<AttendeeDto>,
    val current_page: Int = 1,
    val last_page: Int = 1,
    val total: Int = 0
)

data class AttendeeDto(
    val id: Long,
    val user_id: Long,
    val event_id: Long,
    val status: String,
    val checked_in_at: String? = null,
    val user: AttendeeUserDto? = null
)

data class AttendeeUserDto(
    val id: Long,
    val name: String,
    val email: String? = null,
    val avatar_url: String? = null
)
