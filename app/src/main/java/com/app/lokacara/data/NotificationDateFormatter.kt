package com.app.lokacara.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object NotificationDateFormatter {
    private val dayNames = arrayOf("Minggu", "Senin", "Selasa", "Rabu", "Kamis", "Jumat", "Sabtu")
    private val monthNames = arrayOf(
        "Jan", "Feb", "Mar", "Apr", "Mei", "Jun",
        "Jul", "Agu", "Sep", "Okt", "Nov", "Des"
    )

    fun formatTime(isoDate: String?): String {
        if (isoDate.isNullOrBlank()) return ""
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = sdf.parse(isoDate) ?: return isoDate.substringAfter("T").take(5)
            val timeFormat = SimpleDateFormat("HH:mm", Locale.US)
            timeFormat.timeZone = TimeZone.getDefault()
            timeFormat.format(date)
        } catch (_: Exception) {
            isoDate.substringAfter("T").take(5)
        }
    }

    fun formatDateGroup(isoDate: String?): String {
        if (isoDate.isNullOrBlank()) return ""
        return try {
            val rawDate = isoDate.take(10)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = sdf.parse(rawDate) ?: return rawDate
            val cal = Calendar.getInstance().apply { time = date }
            val today = Calendar.getInstance()
            val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

            when {
                isSameDay(cal, today) -> "Hari ini"
                isSameDay(cal, yesterday) -> "Kemarin"
                cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) -> {
                    val dayName = dayNames[cal.get(Calendar.DAY_OF_WEEK) - 1]
                    val month = monthNames[cal.get(Calendar.MONTH)]
                    "$dayName, ${cal.get(Calendar.DAY_OF_MONTH)} $month"
                }
                else -> {
                    val dayName = dayNames[cal.get(Calendar.DAY_OF_WEEK) - 1]
                    val month = monthNames[cal.get(Calendar.MONTH)]
                    "$dayName, ${cal.get(Calendar.DAY_OF_MONTH)} $month ${cal.get(Calendar.YEAR)}"
                }
            }
        } catch (_: Exception) {
            isoDate.take(10)
        }
    }

    private fun isSameDay(c1: Calendar, c2: Calendar): Boolean {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
    }
}
