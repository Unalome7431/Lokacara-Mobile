package com.app.lokacara.data.remote

import com.app.lokacara.model.Event
import com.app.lokacara.data.remote.dto.EventDto
import com.app.lokacara.data.remote.dto.RegistrationDto
import com.app.lokacara.model.HistoryEvent
import com.app.lokacara.model.UpcomingEvent
import java.text.SimpleDateFormat
import java.util.Locale

private fun formatDate(dateStr: String): String {
    return try {
        val inputSdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val date = inputSdf.parse(dateStr.take(10)) ?: return dateStr.take(10)
        val outputSdf = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id-ID"))
        outputSdf.format(date)
    } catch (_: Exception) {
        dateStr.take(10)
    }
}

fun EventDto.toEvent(imageUrlProvider: ImageUrlProvider): Event {
    val categoryName = category?.name ?: "Lainnya"
    val penyelenggara = user?.name ?: "Penyelenggara"
    val location = location_name ?: platform_name ?: ""
    val dateDisplay = formatDate(start_datetime)

    return Event(
        id = id,
        title = title,
        description = description,
        date = dateDisplay,
        location = location,
        price = when {
            price == null || price == 0 -> "Gratis"
            else -> "Rp $price"
        },
        imageUrl = poster_url ?: imageUrlProvider.posterUrl(poster),
        category = categoryName,
        isBookmarked = false,
        penyelenggara = penyelenggara,
        latitude = latitude,
        longitude = longitude,
        viewCount = view_count
    )
}

fun RegistrationDto.toUpcomingEvent(imageUrlProvider: ImageUrlProvider): UpcomingEvent? {
    val e = event ?: return null
    val location = e.location_name ?: e.platform_name ?: ""
    return UpcomingEvent(
        id = e.id,
        title = e.title,
        date = e.start_datetime.take(10),
        time = e.start_datetime.substringAfter("T").take(8),
        location = location,
        type = e.type,
        imageUrl = e.poster_url ?: imageUrlProvider.posterUrl(e.poster),
        qrToken = qr_token
    )
}

fun RegistrationDto.toHistoryEvent(imageUrlProvider: ImageUrlProvider): HistoryEvent? {
    val e = event ?: return null
    val location = e.location_name ?: e.platform_name ?: ""
    return HistoryEvent(
        id = e.id,
        title = e.title,
        date = e.start_datetime.take(10),
        time = e.start_datetime.substringAfter("T").take(8),
        location = location,
        category = e.category?.name ?: "Lainnya",
        isBlueBg = false,
        imageUrl = e.poster_url ?: imageUrlProvider.posterUrl(e.poster)
    )
}
