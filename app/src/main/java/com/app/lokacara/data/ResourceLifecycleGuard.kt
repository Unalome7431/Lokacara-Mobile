package com.app.lokacara.data

import java.util.concurrent.atomic.AtomicBoolean

class ResourceLifecycleGuard {
    private val active = AtomicBoolean(true)

    fun runIfActive(action: () -> Unit): Boolean {
        if (!active.get()) return false
        action()
        return true
    }

    fun close() {
        active.set(false)
    }
}
