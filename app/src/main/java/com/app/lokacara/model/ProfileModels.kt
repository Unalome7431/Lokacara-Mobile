package com.app.lokacara.model

import androidx.compose.runtime.Immutable

@Immutable
data class MyEventData(
    val title: String,
    val date: String,
    val attendees: String,
    val status: String,
    val imageRes: Int
)

@Immutable
data class CertificateData(
    val title: String,
    val date: String,
    val time: String,
    val location: String,
    val category: String,
    val imageRes: Int
)

@Immutable
data class UserProfile(
    val name: String,
    val email: String,
    val phone: String,
    val location: String,
    val profileImageRes: Int? = null
)
