package com.app.lokacara.data.result

import com.app.lokacara.data.remote.ApiResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ErrorMappingTest {

    @Test
    fun `httpCodeToMessage maps known codes correctly`() {
        assertEquals(AppErrorMessages.SERVER_400, httpCodeToMessage(400))
        assertEquals(AppErrorMessages.SERVER_401, httpCodeToMessage(401))
        assertEquals(AppErrorMessages.SERVER_403, httpCodeToMessage(403))
        assertEquals(AppErrorMessages.SERVER_404, httpCodeToMessage(404))
        assertEquals(AppErrorMessages.SERVER_413, httpCodeToMessage(413))
        assertEquals(AppErrorMessages.SERVER_422, httpCodeToMessage(422))
        assertEquals(AppErrorMessages.SERVER_429, httpCodeToMessage(429))
        assertEquals(AppErrorMessages.SERVER_500, httpCodeToMessage(500))
    }

    @Test
    fun `httpCodeToMessage returns unknown for unmapped codes`() {
        assertEquals(AppErrorMessages.SERVER_UNKNOWN, httpCodeToMessage(418))
        assertEquals(AppErrorMessages.SERVER_UNKNOWN, httpCodeToMessage(999))
    }

    @Test
    fun `toUserMessage returns body message when present`() {
        val error = ApiResult.Error("Email sudah terdaftar", code = 422)
        assertEquals("Email sudah terdaftar", error.toUserMessage())
    }

    @Test
    fun `toUserMessage falls back to code mapping when message is blank`() {
        val error = ApiResult.Error("", code = 404)
        assertEquals(AppErrorMessages.SERVER_404, error.toUserMessage())
    }

    @Test
    fun `toUserMessage falls back to default when no code and no message`() {
        val error = ApiResult.Error("", code = null)
        assertEquals(AppErrorMessages.SERVER_UNKNOWN, error.toUserMessage())
    }

    @Test
    fun `toMessageWithFallback uses fallback when both message and code are empty`() {
        val error = ApiResult.Error("", code = null)
        assertEquals("Custom fallback", error.toMessageWithFallback("Custom fallback"))
    }

    @Test
    fun `network error message is preserved`() {
        val error = ApiResult.Error(AppErrorMessages.NETWORK, code = null)
        assertTrue(error.toUserMessage().contains("koneksi"))
    }

    @Test
    fun `toUserMessage with custom default overrides`() {
        val error = ApiResult.Error("", code = null)
        assertEquals("Override default", error.toUserMessage("Override default"))
    }
}
