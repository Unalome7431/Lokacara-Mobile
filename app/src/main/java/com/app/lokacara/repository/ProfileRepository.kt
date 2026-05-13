package com.app.lokacara.repository

import com.app.lokacara.R
import com.app.lokacara.model.CertificateData
import com.app.lokacara.model.Event
import com.app.lokacara.model.MyEventData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ProfileRepository {
    fun getMyEvents(): Flow<List<MyEventData>> = flow {
        // Simulate network delay
        delay(500)
        emit(
            listOf(
                MyEventData("Seminar Ai di Kota Surakarta", "25 April 2026", "Diikuti 100 orang", "Selesai", R.drawable.seminar_2)
            )
        )
    }

    fun getSavedEvents(): Flow<List<Event>> = flow {
        // Simulate network delay
        delay(500)
        emit(
            listOf(
                Event(
                    id = "1",
                    title = "Seminar Ai di Kota Surakarta",
                    description = "Acara ini dibuat untuk memenuhi tugas mata kul...",
                    date = "25 April 2026",
                    location = "Pura Mangkunegaran",
                    price = "Gratis",
                    imageRes = R.drawable.seminar_2,
                    category = "Seminar"
                ),
                Event(
                    id = "2",
                    title = "Seminar Ai di Kota Surakarta",
                    description = "Acara ini dibuat untuk memenuhi tugas mata kul...",
                    date = "25 April 2026",
                    location = "Pura Mangkunegaran",
                    price = "Gratis",
                    imageRes = R.drawable.seminar_3,
                    category = "Seminar"
                ),
                Event(
                    id = "3",
                    title = "Seminar Ai di Kota Surakarta",
                    description = "Acara ini dibuat untuk memenuhi tugas mata kul...",
                    date = "25 April 2026",
                    location = "Pura Mangkunegaran",
                    price = "Gratis",
                    imageRes = R.drawable.seminar_4,
                    category = "Seminar"
                )
            )
        )
    }

    fun getCertificates(): Flow<List<CertificateData>> = flow {
        // Simulate network delay
        delay(500)
        emit(
            listOf(
                CertificateData("Seminar Ai di Kota Surakarta", "25 April 2026", "15:00", "Pura Mangkunegaran", "Seminar", R.drawable.sertifcontoh),
                CertificateData("Seminar Ai di Kota Surakarta", "25 April 2026", "15:00", "Pura Mangkunegaran", "Seminar", R.drawable.sertifcontoh),
                CertificateData("Seminar Ai di Kota Surakarta", "25 April 2026", "15:00", "Pura Mangkunegaran", "Seminar", R.drawable.sertifcontoh)
            )
        )
    }
}
