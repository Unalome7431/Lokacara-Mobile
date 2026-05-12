package com.app.lokacara.repository

import com.app.lokacara.R
import com.app.lokacara.model.Event

class ExploreRepository {
    fun getEvents(): List<Event> {
        return listOf(
            Event("1", "Seminar Ai di Kota Surakarta", "Acara ini dibuat untuk memenuhi tugas mata kuliah...", "25 April 2026", "Pura Mangkunegaran", "Gratis", R.drawable.seminar, "Teknologi"),
            Event("2", "Sound of Solo Festival", "Konser musik tahunan...", "2 Mei 2026", "Benteng Vastenburg", "Rp 50.000", R.drawable.seminar_2, "Musik"),
            Event("3", "Fullstack Workshop 2026", "Belajar membangun aplikasi modern...", "10 Mei 2026", "Solo Techno Park", "Gratis", R.drawable.seminar_3, "Teknologi"),
            Event("4", "Cosplay Anime Matsuri", "Festival jejepangan terbesar...", "15 Mei 2026", "The Park Mall", "Rp 35.000", R.drawable.seminar, "Anime")
        )
    }

    fun getLocations() = listOf("Surabaya", "Surakarta", "Jakarta", "Semarang", "Yogyakarta")
    fun getCategories() = listOf("Workshop", "Wanita", "Webinar", "Anime", "Musik", "Teknologi")
}