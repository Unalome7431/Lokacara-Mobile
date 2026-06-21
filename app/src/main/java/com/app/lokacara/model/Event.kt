package com.app.lokacara.model

import androidx.compose.runtime.Immutable

@Immutable
data class Event(
    val id: Long,
    val title: String,
    val description: String,
    val date: String,
    val dateEpoch: Long = 0L,
    val location: String,
    val price: String,
    val imageUrl: String? = null,
    val category: String,
    val isBookmarked: Boolean = false,
    val penyelenggara: String = "",
    val address: String? = null,
    val platformName: String? = null,
    val link: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val viewCount: Int = 0,
    val type: String? = null,
    val startDatetime: String = "",
    val endDatetime: String? = null,
    val capacity: Int? = null,
    val status: String = "active",
    val kuota: Int = 100,
    val pendaftarCount: Int = 80,
    val hadirCount: Int = 40
)
