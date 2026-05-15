package com.app.lokacara.repository

import com.app.lokacara.R
import com.app.lokacara.model.Event

class HomeRepository {

    fun getLocations(): List<String> = listOf("Solo", "Yogyakarta", "Semarang", "Jakarta")

    fun getCategories(): List<String> = listOf("Semua", "Musik", "Teknologi", "Anime", "Hobi")

    fun getPopularEvents(): List<Event> {
        return listOf(
            Event("p1", "Yogyakarta Concert", "Rabu 14 April 2026 | 13.00", "Yogyakarta Arena", "", "", R.drawable.candi, "Musik", penyelenggara = "Jogja Creative Hub"),
            Event("p2", "Tech Summit 2026", "Senin 20 April 2026 | 09.00", "Solo Convention Center", "", "", R.drawable.seminar_3, "Teknologi", penyelenggara = "Tech Indonesia Foundation")
        )
    }

    fun getNearbyEvents(): List<Event> {
        return listOf(
            Event("1", "Seminar Ai di Kota Surakarta", "Acara ini dibuat untuk memenuhi tugas mata kuliah kecerdasan buatan...", "25 April 2026", "Pura Mangkunegaran", "Gratis", R.drawable.seminar, "Teknologi", penyelenggara = "BEM Fasilkom UNS"),
            Event("2", "Sound of Solo Festival", "Konser musik tahunan yang menghadirkan musisi papan atas Indonesia...", "2 Mei 2026", "Benteng Vastenburg", "Rp 50.000", R.drawable.seminar_2, "Musik", penyelenggara = "Dinas Pariwisata Solo"),
            Event("3", "Fullstack Workshop 2026", "Belajar membangun aplikasi modern dari zero ke hero bersama mentor expert...", "10 Mei 2026", "Solo Techno Park", "Gratis", R.drawable.seminar_3, "Teknologi", penyelenggara = "Solo Techno Park Academy")
        )
    }
}