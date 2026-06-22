package com.app.lokacara.model

import androidx.compose.runtime.Immutable

@Immutable
data class MyEventData(
    val title: String,
    val date: String,
    val attendees: String,
    val status: String,
    val imageUrl: String? = null
)

@Immutable
data class CertificateData(
    val id: String,
    val eventId: Long = 0L,
    val title: String,
    val date: String,
    val time: String,
    val location: String,
    val category: String,
    val imageUrl: String? = null,
    val filePath: String? = null,
    val isPreviewLoading: Boolean = false,
    val isDownloading: Boolean = false,
    val errorMessage: String? = null
)

@Immutable
data class UserProfile(
    val name: String,
    val email: String,
    val phone: String,
    val location: String,
    val profileImageUrl: String? = null
)
