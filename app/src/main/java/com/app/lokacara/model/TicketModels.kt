package com.app.lokacara.model

import androidx.compose.runtime.Immutable

@Immutable
data class UpcomingEvent(
    val title: String,
    val date: String,
    val time: String,
    val location: String,
    val type: String,
    val imageRes: Int
)

@Immutable
data class HistoryEvent(
    val title: String,
    val date: String,
    val time: String,
    val location: String,
    val category: String,
    val isBlueBg: Boolean,
    val imageRes: Int
)