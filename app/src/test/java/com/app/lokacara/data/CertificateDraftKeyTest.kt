package com.app.lokacara.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class CertificateDraftKeyTest {
    @Test
    fun `draft key is stable and isolated by account and event`() {
        assertEquals("u7_e12", certificateDraftKey(7, 12))
        assertNotEquals(certificateDraftKey(7, 12), certificateDraftKey(8, 12))
        assertNotEquals(certificateDraftKey(7, 12), certificateDraftKey(7, 13))
    }
}
