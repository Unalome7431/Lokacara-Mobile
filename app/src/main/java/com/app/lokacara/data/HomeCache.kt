package com.app.lokacara.data

import com.app.lokacara.data.remote.dto.CategoryDto
import com.app.lokacara.data.remote.dto.EventDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeCache @Inject constructor() {

    private var cachedEvents: List<EventDto>? = null
    private var cachedCategories: List<CategoryDto>? = null
    private var cacheTimestamp: Long = 0L

    val events: List<EventDto>? get() = cachedEvents
    val categories: List<CategoryDto>? get() = cachedCategories
    val isStale: Boolean get() = System.currentTimeMillis() - cacheTimestamp > 30_000L

    fun putEvents(events: List<EventDto>) {
        cachedEvents = events
        cacheTimestamp = System.currentTimeMillis()
    }

    fun putCategories(categories: List<CategoryDto>) {
        cachedCategories = categories
        cacheTimestamp = System.currentTimeMillis()
    }

    fun invalidate() {
        cachedEvents = null
        cachedCategories = null
        cacheTimestamp = 0L
    }
}
