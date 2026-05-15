package com.app.lokacara.viewmodel

import androidx.lifecycle.ViewModel
import com.app.lokacara.model.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.app.lokacara.R

class EventDetailViewModel : ViewModel() {

    private val ev = Event(
        id = "1",
        title = "Judul Event Lorem Ipsum",
        description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
        date = "25 April 2026",
        location = "Pura Mangkunegaran",
        price = "Gratis",
        imageRes = R.drawable.seminar,
        category = "Teknologi",
        penyelenggara = "BEM Fasilkom UNS"
    )

    private val related = listOf(
        Event("2", "Sound of Solo Festival", "Konser musik tahunan", "2 Mei 2026", "Benteng Vastenburg", "Rp 50.000", R.drawable.seminar_2, "Musik", penyelenggara = "Dinas Pariwisata Solo"),
        Event("3", "Fullstack Workshop 2026", "Belajar membangun aplikasi modern", "10 Mei 2026", "Solo Techno Park", "Gratis", R.drawable.seminar_3, "Teknologi", penyelenggara = "Solo Techno Park Academy"),
        Event("p2", "Tech Summit 2026", "Konferensi teknologi terbesar", "20 April 2026", "Solo Convention Center", "Rp 150.000", R.drawable.seminar_3, "Teknologi", penyelenggara = "Tech Indonesia Foundation")
    )

    private val _event = MutableStateFlow(ev)
    val event: StateFlow<Event> = _event.asStateFlow()

    private val _relatedEvents = MutableStateFlow(related)
    val relatedEvents: StateFlow<List<Event>> = _relatedEvents.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
}
