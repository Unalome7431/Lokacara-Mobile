package com.app.lokacara.notifications

import com.app.lokacara.ui.navigation.Screen

data class AppNotificationPayload(
    val category: String,
    val target: NotificationTarget,
    val eventId: Long?,
    val title: String,
    val body: String
)

enum class NotificationTarget {
    EVENT_DETAIL,
    TICKETS,
    CERTIFICATES,
    NOTIFICATION;

    companion object {
        fun from(rawTarget: String?, category: String): NotificationTarget {
            return when (rawTarget?.trim()?.lowercase()) {
                "event_detail" -> EVENT_DETAIL
                "tickets" -> TICKETS
                "certificates" -> CERTIFICATES
                "notification" -> NOTIFICATION
                else -> defaultForCategory(category.trim().lowercase())
            }
        }

        private fun defaultForCategory(category: String): NotificationTarget {
            return when (category) {
                "registration_success", "attendance_checked_in" -> TICKETS
                "certificate_available" -> CERTIFICATES
                "event_reminder",
                "event_updated",
                "event_cancelled",
                "host_new_registration",
                "host_registration_cancelled",
                "event_capacity_warning",
                "bookmarked_event_reminder" -> EVENT_DETAIL
                else -> NOTIFICATION
            }
        }
    }
}

object AppNotificationPayloadParser {
    fun parse(data: Map<String, String>): AppNotificationPayload? {
        val category = data["category"]?.trim()?.lowercase()?.takeIf { it.isNotBlank() }
            ?: data["type"]?.trim()?.lowercase()?.takeIf { it == "event_reminder" }
            ?: return null
        return AppNotificationPayload(
            category = category,
            target = NotificationTarget.from(data["target"], category),
            eventId = data["event_id"]?.toLongOrNull()?.takeIf { it > 0 },
            title = data["title"]?.takeIf { it.isNotBlank() } ?: "Notifikasi Lokacara",
            body = data["body"]?.takeIf { it.isNotBlank() } ?: "Ada pembaruan baru untuk akunmu."
        )
    }
}

object NotificationRouteMapper {
    fun routeFor(payload: AppNotificationPayload): String {
        return routeFor(payload.target, payload.eventId)
    }

    fun routeFor(target: NotificationTarget, eventId: Long?): String {
        return when (target) {
            NotificationTarget.EVENT_DETAIL -> eventId
                ?.let { Screen.EventDetail.createRoute(it) }
                ?: Screen.Notification.route
            NotificationTarget.TICKETS -> Screen.Tickets.route
            NotificationTarget.CERTIFICATES -> Screen.Certificates.route
            NotificationTarget.NOTIFICATION -> Screen.Notification.route
        }
    }
}
