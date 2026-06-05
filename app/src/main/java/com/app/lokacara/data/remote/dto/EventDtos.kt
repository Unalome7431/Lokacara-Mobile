package com.app.lokacara.data.remote.dto

data class EventDto(
    val id: Long,
    val title: String,
    val description: String,
    val type: String,
    val location_name: String? = null,
    val address: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val platform_name: String? = null,
    val link: String? = null,
    val start_datetime: String,
    val end_datetime: String,
    val capacity: Int? = null,
    val view_count: Int = 0,
    val poster: String? = null,
    val category_id: Int? = null,
    val category: CategoryDto? = null,
    val user: EventUserDto? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)

data class CategoryDto(
    val id: Int,
    val name: String
)

data class EventUserDto(
    val id: Long,
    val name: String
)

data class EventListResponse(
    val data: List<EventDto>
)

data class PaginatedEventsResponse(
    val data: List<EventDto>,
    val current_page: Int,
    val last_page: Int,
    val per_page: Int,
    val total: Int
)

data class EventDetailResponse(
    val event: EventDto,
    val is_registered: Boolean = false
)

data class CreateEventResponse(
    val message: String,
    val event: EventDto
)
