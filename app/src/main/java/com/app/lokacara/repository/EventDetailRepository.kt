package com.app.lokacara.repository

import com.app.lokacara.R
import com.app.lokacara.model.Event

class EventDetailRepository {

    private val allEvents = listOf(
        Event("1", "Seminar Ai di Kota Surakarta", "Acara ini dibuat untuk memenuhi tugas mata kuliah kecerdasan buatan. Seminar ini akan membahas perkembangan AI terkini, termasuk machine learning, deep learning, dan implementasi AI di dunia industri. Peserta akan mendapatkan sertifikat dan materi eksklusif dari para pembicara ahli di bidangnya.", "25 April 2026", "Pura Mangkunegaran", "Gratis", R.drawable.seminar, "Teknologi", penyelenggara = "BEM Fasilkom UNS"),
        Event("2", "Sound of Solo Festival", "Konser musik tahunan yang menghadirkan musisi papan atas Indonesia. Nikmati pengalaman musik yang tak terlupakan dengan ribuan penonton dari berbagai daerah. Festival ini juga menghadirkan bazar kuliner dan pameran seni.", "2 Mei 2026", "Benteng Vastenburg", "Rp 50.000", R.drawable.seminar_2, "Musik", penyelenggara = "Dinas Pariwisata Solo"),
        Event("3", "Fullstack Workshop 2026", "Belajar membangun aplikasi modern dari zero ke hero bersama mentor expert. Workshop intensif selama 3 hari mencakup frontend, backend, database, dan deployment. Cocok untuk pemula maupun developer yang ingin upgrade skill.", "10 Mei 2026", "Solo Techno Park", "Gratis", R.drawable.seminar_3, "Teknologi", penyelenggara = "Solo Techno Park Academy"),
        Event("4", "Cosplay Anime Matsuri", "Festival jejepangan terbesar di Jawa Tengah dengan berbagai lomba cosplay, pameran manga, dan pertunjukan budaya Jepang. Tersedia juga booth merchandise eksklusif dan meet & greet dengan cosplayer terkenal.", "15 Mei 2026", "The Park Mall", "Rp 35.000", R.drawable.seminar, "Anime", penyelenggara = "Komunitas Developer Indonesia"),
        Event("p1", "Yogyakarta Concert", "Konser spektakuler di kota pelajar bersama musisi indie ternama. Acara ini akan dimeriahkan oleh puluhan penampil dari berbagai genre musik. Jangan lewatkan experience seru bersama teman-temanmu.", "14 April 2026", "Yogyakarta Arena", "Rp 75.000", R.drawable.candi, "Musik", penyelenggara = "Jogja Creative Hub"),
        Event("p2", "Tech Summit 2026", "Konferensi teknologi terbesar yang menghubungkan inovator, startup, dan investor. Bahas tren terbaru AI, blockchain, IoT dan networking dengan profesional industri.", "20 April 2026", "Solo Convention Center", "Rp 150.000", R.drawable.seminar_3, "Teknologi", penyelenggara = "Tech Indonesia Foundation")
    )

    fun getEventById(id: String): Event? {
        return allEvents.find { it.id == id }
    }

    fun getRelatedEvents(category: String, excludeId: String): List<Event> {
        return allEvents
            .filter { it.category == category && it.id != excludeId }
            .take(4)
    }
}
