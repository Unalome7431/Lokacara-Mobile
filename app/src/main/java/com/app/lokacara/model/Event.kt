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

    // --- TAMBAHAN UNTUK MOCK DATA ANALITIK ---
    val kuota: Int = 100,          // Default 100
    val pendaftarCount: Int = 80,  // Default 80
    val hadirCount: Int = 40       // Default 40
)