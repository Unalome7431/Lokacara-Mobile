package com.app.lokacara.data.remote.dto

data class NotificationListResponse(
    val data: List<NotificationItemDto>,
    val unread_count: Int = 0
)

data class NotificationItemDto(
    val id: Long,
    val sender_name: String? = null,
    val message: String,
    val type: String,
    val category: String? = null,
    val target: String? = null,
    val event_id: Long? = null,
    val is_read: Boolean = false,
    val created_at: String? = null
)
