package com.app.lokacara.data.remote

import com.app.lokacara.model.Event
import com.app.lokacara.data.remote.dto.EventDto
import com.app.lokacara.data.remote.dto.RegistrationDto
import com.app.lokacara.model.HistoryEvent
import com.app.lokacara.model.UpcomingEvent
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date
import java.util.concurrent.TimeUnit

private fun parseDateEpoch(dateStr: String): Long {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.parse(dateStr.take(10))?.time ?: 0L
    } catch (_: Exception) { 0L }
}

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

fun formatRibuan(amount: Int): String {
    val s = amount.toString()
    val sb = StringBuilder()
    var count = 0
    for (i in s.length - 1 downTo 0) {
        sb.append(s[i])
        count++
        if (count % 3 == 0 && i > 0) sb.append('.')
    }
    return sb.reverse().toString()
}

fun formatViewCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format(Locale.US, "%.1fjt", count / 1_000_000.0)
        count >= 1_000 -> String.format(Locale.US, "%.1frb", count / 1_000.0)
        else -> count.toString()
    }
}

fun countdownLabel(startDatetime: String): String? {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val eventDate = sdf.parse(startDatetime.replace("T", " ").take(19))
        if (eventDate == null) return null
        val now = Date()
        val diff = eventDate.time - now.time
        val daysLeft = TimeUnit.MILLISECONDS.toDays(diff)
        when {
            diff < 0 -> "Sedang berlangsung"
            daysLeft == 0L -> "Hari ini"
            daysLeft == 1L -> "Besok"
            daysLeft <= 7L -> "$daysLeft hari lagi"
            daysLeft <= 30L -> "${daysLeft / 7} minggu lagi"
            else -> null
        }
    } catch (_: Exception) {
        null
    }
}

fun EventDto.toEvent(imageUrlProvider: ImageUrlProvider): Event {
    val categoryName = category?.name ?: "Lainnya"
    val penyelenggara = organizer_name?.takeIf { it.isNotBlank() } ?: user?.name ?: "Penyelenggara"
    val location = location_name ?: platform_name ?: ""
    val dateDisplay = formatDate(start_datetime)
    val epoch = parseDateEpoch(start_datetime)

    return Event(
        id = id,
        title = title,
        description = description,
        date = dateDisplay,
        dateEpoch = epoch,
        location = location,
        price = when {
            price == null || price == 0 -> "Gratis"
            else -> "Rp ${formatRibuan(price)}"
        },
        imageUrl = poster_url ?: imageUrlProvider.posterUrl(poster),
        category = categoryName,
        isBookmarked = false,
        penyelenggara = penyelenggara,
        address = address,
        city = city,
        platformName = platform_name,
        link = link,
        latitude = latitude,
        longitude = longitude,
        viewCount = view_count,
        type = type.ifEmpty { null },
        startDatetime = start_datetime,
        endDatetime = end_datetime.ifEmpty { null },
        capacity = capacity,
        status = status?.ifBlank { null } ?: "active",
        kuota = capacity ?: 100
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
        qrToken = qr_token,
        startEpoch = parseDateEpoch(e.start_datetime),
        status = e.status ?: "active"
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
