package com.app.lokacara.model

data class MyEventData(
    val title: String,
    val date: String,
    val attendees: String,
    val status: String,
    val imageRes: Int
)

data class CertificateData(
    val title: String,
    val date: String,
    val time: String,
    val location: String,
    val category: String,
    val imageRes: Int
)
