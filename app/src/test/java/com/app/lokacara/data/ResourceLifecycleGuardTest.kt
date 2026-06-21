package com.app.lokacara.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ResourceLifecycleGuardTest {
    @Test
    fun `callback is suppressed after resource closes`() {
        val guard = ResourceLifecycleGuard()
        var callbackCount = 0

        assertTrue(guard.runIfActive { callbackCount++ })
        guard.close()

        assertFalse(guard.runIfActive { callbackCount++ })
        assertTrue(callbackCount == 1)
    }
}
