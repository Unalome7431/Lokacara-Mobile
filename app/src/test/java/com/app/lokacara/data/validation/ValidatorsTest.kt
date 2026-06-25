package com.app.lokacara.data.validation

import org.junit.Assert.*
import org.junit.Test

class ValidatorsTest {

    @Test
    fun `isValidEmail returns true for valid emails`() {
        assertTrue(Validators.isValidEmail("user@example.com"))
        assertTrue(Validators.isValidEmail("a.b@c.co"))
        assertTrue(Validators.isValidEmail("test+tag@domain.id"))
    }

    @Test
    fun `isValidEmail returns false for invalid emails`() {
        assertFalse(Validators.isValidEmail(""))
        assertFalse(Validators.isValidEmail("notanemail"))
        assertFalse(Validators.isValidEmail("@domain.com"))
        assertFalse(Validators.isValidEmail("user@"))
    }

    @Test
    fun `validateEmail returns error for blank email`() {
        val error = Validators.validateEmail("")
        assertNotNull(error)
        assertTrue(error!!.contains("harus diisi"))
    }

    @Test
    fun `validateEmail returns error for invalid format`() {
        val error = Validators.validateEmail("bad")
        assertNotNull(error)
        assertTrue(error!!.contains("Format"))
    }

    @Test
    fun `validateEmail returns null for valid email`() {
        assertNull(Validators.validateEmail("user@example.com"))
    }

    @Test
    fun `validateRequired returns error for blank value`() {
        assertNotNull(Validators.validateRequired("", "Nama"))
        assertNotNull(Validators.validateRequired("   ", "Judul"))
    }

    @Test
    fun `validateRequired returns null for non-blank value`() {
        assertNull(Validators.validateRequired("hello", "Judul"))
    }

    @Test
    fun `validatePassword returns error for short password`() {
        val error = Validators.validatePassword("abc")
        assertNotNull(error)
        assertTrue(error!!.contains("minimal"))
    }

    @Test
    fun `validatePassword returns null for valid password`() {
        assertNull(Validators.validatePassword("password1"))
        assertNull(Validators.validatePassword("123456"))
    }

    @Test
    fun `validatePasswordConfirmation detects mismatch`() {
        val error = Validators.validatePasswordConfirmation("pass1", "pass2")
        assertNotNull(error)
        assertTrue(error!!.contains("tidak sama"))
    }

    @Test
    fun `validatePasswordConfirmation returns null for match`() {
        assertNull(Validators.validatePasswordConfirmation("pass1", "pass1"))
    }

    @Test
    fun `validateTextLength returns error when exceeding limit`() {
        val error = Validators.validateTextLength("a".repeat(256), 255, "Judul")
        assertNotNull(error)
        assertTrue(error!!.contains("255"))
    }

    @Test
    fun `validateTextLength returns null when within limit`() {
        assertNull(Validators.validateTextLength("hello", 255, "Judul"))
    }

    @Test
    fun `validateCapacity rejects out of range values`() {
        assertNotNull(Validators.validateCapacity(0))
        assertNotNull(Validators.validateCapacity(100_001))
    }

    @Test
    fun `validateCapacity accepts in range values`() {
        assertNull(Validators.validateCapacity(1))
        assertNull(Validators.validateCapacity(50))
        assertNull(Validators.validateCapacity(100_000))
    }

    @Test
    fun `validatePrice rejects negative values`() {
        assertNotNull(Validators.validatePrice(-1))
    }

    @Test
    fun `validatePrice accepts zero and positive values`() {
        assertNull(Validators.validatePrice(0))
        assertNull(Validators.validatePrice(1000))
    }

    @Test
    fun `validateSchedule returns error when end before start`() {
        val error = Validators.validateSchedule("2025-06-01 10:00:00", "2025-06-01 09:00:00")
        assertNotNull(error)
        assertTrue(error!!.contains("setelah"))
    }

    @Test
    fun `validateSchedule returns null when end after start`() {
        assertNull(Validators.validateSchedule("2025-06-01 10:00:00", "2025-06-01 11:00:00"))
    }

    @Test
    fun `validateSchedule handles invalid format`() {
        assertNotNull(Validators.validateSchedule("invalid", "invalid"))
    }

    @Test
    fun `validateFileSize returns error for oversized files`() {
        assertNotNull(Validators.validateFileSize(11_000_000L))
    }

    @Test
    fun `validateFileSize returns null for acceptable sizes`() {
        assertNull(Validators.validateFileSize(5_000_000L))
        assertNull(Validators.validateFileSize(10_000_000L))
    }

    @Test
    fun `validateLocation returns error when both coordinates blank`() {
        assertNotNull(Validators.validateLocation("", ""))
    }

    @Test
    fun `validateLocation returns error when only one coordinate provided`() {
        assertNotNull(Validators.validateLocation("-6.2", ""))
        assertNotNull(Validators.validateLocation("", "106.8"))
    }

    @Test
    fun `validateLocation returns null when both coordinates provided`() {
        assertNull(Validators.validateLocation("-6.2", "106.8"))
    }

    @Test
    fun `isSyntheticEmail detects placeholder emails`() {
        assertTrue("user@placeholder.local".isSyntheticEmail())
        assertTrue("test@PLACEHOLDER.LOCAL".isSyntheticEmail())
    }

    @Test
    fun `isSyntheticEmail returns false for real emails`() {
        assertFalse("user@gmail.com".isSyntheticEmail())
    }

    @Test
    fun `isDisplayableEmail returns false for synthetic emails`() {
        assertFalse("user@placeholder.local".isDisplayableEmail())
    }

    @Test
    fun `isDisplayableEmail returns true for real emails`() {
        assertTrue("user@gmail.com".isDisplayableEmail())
    }

    @Test
    fun `toDisplayEmail returns empty for synthetic email`() {
        assertEquals("", "user@placeholder.local".toDisplayEmail())
    }
}
