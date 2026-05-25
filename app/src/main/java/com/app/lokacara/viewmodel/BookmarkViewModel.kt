package com.app.lokacara.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.lokacara.data.BookmarkManager
import com.app.lokacara.model.Event
import com.app.lokacara.repository.BookmarkRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BookmarkViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = BookmarkRepository()
    private val bookmarkManager = BookmarkManager(application)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _savedEvents = MutableStateFlow(repository.getSavedEvents())
    val savedEvents: StateFlow<List<Event>> = _savedEvents.asStateFlow()

    init {
        syncBookmarks()
    }

    private fun syncBookmarks() {
        viewModelScope.launch {
            val bookmarkedIds = bookmarkManager.bookmarkedIds.first()
            _savedEvents.value = _savedEvents.value.map { event ->
                event.copy(isBookmarked = event.id in bookmarkedIds)
            }
        }
    }

    fun toggleBookmark(eventId: String) {
        viewModelScope.launch {
            bookmarkManager.toggleBookmark(eventId)
            val bookmarkedIds = bookmarkManager.bookmarkedIds.first()
            _savedEvents.value = _savedEvents.value.map { event ->
                if (event.id == eventId) {
                    event.copy(isBookmarked = event.id in bookmarkedIds)
                } else {
                    event
                }
            }
        }
    }
}