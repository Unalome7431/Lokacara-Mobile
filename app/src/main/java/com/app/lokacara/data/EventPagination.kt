package com.app.lokacara.data

import com.app.lokacara.model.Event

fun mergeEventsById(existing: List<Event>, incoming: List<Event>): List<Event> {
    return (existing + incoming).distinctBy(Event::id)
}
