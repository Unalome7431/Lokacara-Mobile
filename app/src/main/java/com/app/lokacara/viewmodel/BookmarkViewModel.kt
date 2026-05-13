package com.app.lokacara.viewmodel

import androidx.lifecycle.ViewModel
import com.app.lokacara.model.Event
import com.app.lokacara.repository.BookmarkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BookmarkViewModel : ViewModel() {
    private val repository = BookmarkRepository()

    private val _savedEvents = MutableStateFlow(repository.getSavedEvents())
    val savedEvents: StateFlow<List<Event>> = _savedEvents.asStateFlow()

    fun toggleBookmark(eventId: String) {
        _savedEvents.value = _savedEvents.value.map { event ->
            if (event.id == eventId) {
                event.copy(isBookmarked = !event.isBookmarked)
            } else {
                event
            }
        }
    }
}