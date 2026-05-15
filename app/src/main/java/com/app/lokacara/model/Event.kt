package com.app.lokacara.model

data class Event(
    val id: String,
    val title: String,
    val description: String,
    val date: String,
    val location: String,
    val price: String,
    val imageRes: Int,
    val category: String,
    val isBookmarked: Boolean = false,
    val penyelenggara: String = ""
)