package com.app.lokacara.repository

import com.app.lokacara.R
import com.app.lokacara.model.Event

class BookmarkRepository {
    fun getSavedEvents(): List<Event> {
        return listOf(
            Event("1", "Seminar Ai di Kota Surakarta", "Acara ini dibuat untuk memenuhi tugas mata kuliah kecerdasan buatan...", "25 April 2026", "Pura Mangkunegaran", "Gratis", R.drawable.seminar, "Teknologi", true),
            Event("2", "Sound of Solo Festival", "Konser musik tahunan yang menghadirkan musisi papan atas Indonesia...", "2 Mei 2026", "Benteng Vastenburg", "Rp 50.000", R.drawable.seminar_2, "Musik", true),
            Event("3", "Fullstack Workshop 2026", "Belajar membangun aplikasi modern dari zero ke hero bersama mentor expert...", "10 Mei 2026", "Solo Techno Park", "Gratis", R.drawable.seminar_3, "Teknologi", true)
        )
    }
}