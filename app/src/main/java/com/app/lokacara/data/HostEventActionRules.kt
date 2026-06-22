package com.app.lokacara.data

import com.app.lokacara.model.Event

fun canHostCancelEvent(event: Event, nowMillis: Long = System.currentTimeMillis()): Boolean {
    return event.status.equals("active", ignoreCase = true) &&
        event.dateEpoch > nowMillis
}
