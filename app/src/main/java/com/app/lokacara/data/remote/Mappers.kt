package com.app.lokacara.data.remote

import com.app.lokacara.model.Event
import com.app.lokacara.data.remote.dto.EventDto
import com.app.lokacara.data.remote.dto.RegistrationDto
import com.app.lokacara.model.HistoryEvent
import com.app.lokacara.model.UpcomingEvent

fun EventDto.toEvent(imageUrlProvider: ImageUrlProvider): Event {
    val categoryName = category?.name ?: "Lainnya"
    val penyelenggara = user?.name ?: "Penyelenggara"
    val location = location_name ?: platform_name ?: ""
    val dateDisplay = start_datetime.take(10) // "2026-06-17"

    return Event(
        id = id.toString(),
        title = title,
        description = description,
        date = dateDisplay,
        location = location,
        price = "Gratis",
        imageUrl = imageUrlProvider.posterUrl(poster),
        category = categoryName,
        isBookmarked = false,
        penyelenggara = penyelenggara
    )
}

fun RegistrationDto.toUpcomingEvent(imageUrlProvider: ImageUrlProvider): UpcomingEvent? {
    val e = event ?: return null
    val location = e.location_name ?: e.platform_name ?: ""
    return UpcomingEvent(
        title = e.title,
        date = e.start_datetime.take(10),
        time = e.start_datetime.substringAfter("T").take(8),
        location = location,
        type = e.type,
        imageUrl = imageUrlProvider.posterUrl(e.poster)
    )
}

fun RegistrationDto.toHistoryEvent(imageUrlProvider: ImageUrlProvider): HistoryEvent? {
    val e = event ?: return null
    val location = e.location_name ?: e.platform_name ?: ""
    return HistoryEvent(
        title = e.title,
        date = e.start_datetime.take(10),
        time = e.start_datetime.substringAfter("T").take(8),
        location = location,
        category = e.category?.name ?: "Lainnya",
        isBlueBg = false,
        imageUrl = imageUrlProvider.posterUrl(e.poster)
    )
}
