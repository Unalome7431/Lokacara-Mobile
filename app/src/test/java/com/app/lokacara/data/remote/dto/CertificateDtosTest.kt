package com.app.lokacara.data.remote.dto

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CertificateDtosTest {

    @Test
    fun `layout config maps every distribute API field`() {
        val request = CertificateLayoutConfig(
            fontFamily = "Playfair",
            fontColor = "#1E3A8A",
            fontSize = "Large",
            xPosition = 42.5f,
            isXCentered = false,
            yPosition = 60f,
            isYCentered = true
        ).toDistributeRequest("temp/certificate-template.png")

        assertEquals("temp/certificate-template.png", request.template_path)
        assertEquals("Playfair", request.font_family)
        assertEquals("#1E3A8A", request.font_color)
        assertEquals("Large", request.font_size)
        assertEquals(42.5f, request.x_pos)
        assertFalse(request.is_x_center)
        assertEquals(60f, request.y_pos)
        assertTrue(request.is_y_center)
    }
}
