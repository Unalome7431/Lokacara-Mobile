package com.app.lokacara.data

import com.app.lokacara.model.UpcomingEvent

fun normalizeUpcomingEvents(
    events: List<UpcomingEvent>,
    nowMillis: Long = System.currentTimeMillis(),
    limit: Int = 4
): List<UpcomingEvent> = events.asSequence()
    .filter { it.id > 0L && it.status.equals("active", true) && it.startEpoch > nowMillis }
    .distinctBy(UpcomingEvent::id)
    .sortedBy(UpcomingEvent::startEpoch)
    .take(limit)
    .toList()
