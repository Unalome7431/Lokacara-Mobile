package com.app.lokacara.data.remote.dto

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CertificateDtosTest {

    @Test
    fun `flat organizer certificate payload parses correctly`() {
        val moshi = Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
        val adapter = moshi.adapter(OrganizerCertificateStateResponse::class.java)

        val json = """
            {
              "event": {
                "id": 42,
                "title": "Lokakara Summit",
                "end_datetime": "2026-06-30 12:00:00"
              },
              "is_eligible": true,
              "has_template": true,
              "issued_count": 7,
              "last_issued_at": "2026-06-22T08:00:00Z",
              "status": "distributed",
              "layout": {
                "font_family": "Roboto",
                "font_color": "#000000",
                "font_size": "Medium",
                "x_pos": 50.0,
                "is_x_center": true,
                "y_pos": 60.0,
                "is_y_center": false,
                "max_width": 80.0,
                "max_height": 20.0
              }
            }
        """.trimIndent()

        val response = adapter.fromJson(json)

        assertNotNull(response)
        assertEquals(42L, response!!.event.id)
        assertTrue(response.is_eligible)
        assertEquals(7, response.issued_count)
        assertEquals("distributed", response.status)
        assertEquals("Roboto", response.layout.font_family)
        assertEquals(false, response.layout.is_y_center)
    }
}
