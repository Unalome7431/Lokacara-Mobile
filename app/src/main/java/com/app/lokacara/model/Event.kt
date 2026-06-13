package com.app.lokacara.model

import androidx.compose.runtime.Immutable

@Immutable
data class Event(
    val id: Long,
    val title: String,
    val description: String,
    val date: String,
    val location: String,
    val price: String,
    val imageUrl: String? = null,
    val category: String,
    val isBookmarked: Boolean = false,
    val penyelenggara: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val viewCount: Int = 0,
    val type: String? = null,
    val startDatetime: String = "",
    val endDatetime: String? = null,
    val capacity: Int? = null
)