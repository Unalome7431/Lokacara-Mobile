package com.app.lokacara.model

data class UpcomingEvent(
    val title: String,
    val date: String,
    val time: String,
    val location: String,
    val type: String,
    val imageRes: Int
)

data class HistoryEvent(
    val title: String,
    val date: String,
    val time: String,
    val location: String,
    val category: String,
    val isBlueBg: Boolean,
    val imageRes: Int
)