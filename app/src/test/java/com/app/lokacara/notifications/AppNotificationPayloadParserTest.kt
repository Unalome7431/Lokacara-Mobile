package com.app.lokacara.notifications

import com.app.lokacara.ui.navigation.Screen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppNotificationPayloadParserTest {
    @Test
    fun parse_genericPayload_returnsCategoryTargetAndContent() {
        val payload = AppNotificationPayloadParser.parse(
            mapOf(
                "category" to "registration_success",
                "target" to "tickets",
                "event_id" to "42",
                "title" to "Pendaftaran berhasil",
                "body" to "Kamu berhasil terdaftar."
            )
        )

        assertEquals("registration_success", payload?.category)
        assertEquals(NotificationTarget.TICKETS, payload?.target)
        assertEquals(42L, payload?.eventId)
        assertEquals("Pendaftaran berhasil", payload?.title)
        assertEquals("Kamu berhasil terdaftar.", payload?.body)
    }

    @Test
    fun parse_legacyReminderPayload_mapsToEventDetail() {
        val payload = AppNotificationPayloadParser.parse(
            mapOf(
                "type" to "event_reminder",
                "event_id" to "42",
                "title" to "Reminder",
                "body" to "Event starts soon"
            )
        )

        assertEquals("event_reminder", payload?.category)
        assertEquals(NotificationTarget.EVENT_DETAIL, payload?.target)
        assertEquals(Screen.EventDetail.createRoute(42L), payload?.let { NotificationRouteMapper.routeFor(it) })
    }

    @Test
    fun parse_missingCategoryAndType_returnsNull() {
        val payload = AppNotificationPayloadParser.parse(
            mapOf("event_id" to "42")
        )

        assertNull(payload)
    }

    @Test
    fun parse_nonReminderTypeWithoutCategory_returnsNull() {
        val payload = AppNotificationPayloadParser.parse(
            mapOf("type" to "system", "event_id" to "42")
        )

        assertNull(payload)
    }

    @Test
    fun routeFor_eventDetailWithoutEventId_fallsBackToNotification() {
        val payload = AppNotificationPayload(
            category = "event_updated",
            target = NotificationTarget.EVENT_DETAIL,
            eventId = null,
            title = "Event berubah",
            body = "Cek detail event."
        )

        assertEquals(Screen.Notification.route, NotificationRouteMapper.routeFor(payload))
    }

    @Test
    fun parse_unknownTarget_fallsBackByCategory() {
        val payload = AppNotificationPayloadParser.parse(
            mapOf(
                "category" to "certificate_available",
                "target" to "something_else"
            )
        )

        assertEquals(NotificationTarget.CERTIFICATES, payload?.target)
        assertEquals(Screen.Certificates.route, payload?.let { NotificationRouteMapper.routeFor(it) })
    }
}
