package com.app.lokacara.model

import androidx.compose.runtime.Immutable

@Immutable
data class UpcomingEvent(
    val id: Long = 0,
    val title: String,
    val date: String,
    val time: String,
    val location: String,
    val type: String,
    val imageUrl: String? = null,
    val qrToken: String? = null,
    val startEpoch: Long = 0L,
    val status: String = "active"
)

@Immutable
data class HistoryEvent(
    val id: Long = 0,
    val title: String,
    val date: String,
    val time: String,
    val location: String,
    val category: String,
    val isBlueBg: Boolean,
    val imageUrl: String? = null
)
