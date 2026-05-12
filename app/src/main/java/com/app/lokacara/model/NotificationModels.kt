package com.app.lokacara.model

enum class NotificationType { SOCIAL, SYSTEM }

data class NotificationItem(
    val id: String,
    val senderName: String = "",
    val message: String,
    val time: String,
    val dateGroup: String,
    val type: NotificationType,
    val isRead: Boolean,
    val iconRes: Int? = null
)