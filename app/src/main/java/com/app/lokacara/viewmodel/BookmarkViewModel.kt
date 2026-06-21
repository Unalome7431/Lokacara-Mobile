package com.app.lokacara.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.lokacara.data.BookmarkManager
import com.app.lokacara.data.remote.ApiResult
import com.app.lokacara.data.remote.ApiService
import com.app.lokacara.data.remote.ImageUrlProvider
import com.app.lokacara.data.remote.safeApiCall
import com.app.lokacara.data.remote.toEvent
import com.app.lokacara.model.Event
import com.app.lokacara.ui.components.SnackbarManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BookmarkViewModel @Inject constructor(
    application: Application,
    private val bookmarkManager: BookmarkManager,
    private val apiService: ApiService,
    private val imageUrlProvider: ImageUrlProvider
) : AndroidViewModel(application) {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _savedEvents = MutableStateFlow<List<Event>>(emptyList())
    val savedEvents: StateFlow<List<Event>> = _savedEvents.asStateFlow()

    init {
        loadBookmarkedEvents()
    }

    private fun loadBookmarkedEvents() {
        viewModelScope.launch {
            _isLoading.value = true

            val bookmarkedIds = bookmarkManager.bookmarkedIds.first()

            if (bookmarkedIds.isEmpty()) {
                _savedEvents.value = emptyList()
                _isLoading.value = false
                return@launch
            }

            // Try server bookmarks first — returns events that server knows about
            val loaded = mutableListOf<Event>()
            val fromServer = safeApiCall { apiService.getBookmarks() }
            if (fromServer is ApiResult.Success) {
                val serverEvents = withContext(Dispatchers.Default) {
                    fromServer.data.data.map { it.toEvent(imageUrlProvider).copy(isBookmarked = true) }
                }
                loaded.addAll(serverEvents)
            }

            // Fetch remaining events by individual IDs (for locally-bookmarked events not known to server)
            val loadedIds = loaded.map { it.id }.toSet()
            val missingIds = bookmarkedIds
                .mapNotNull { it.toLongOrNull() }
                .filter { it !in loadedIds }

            if (missingIds.isNotEmpty()) {
                withContext(Dispatchers.IO) {
                    missingIds.forEach { id ->
                        when (val detail = safeApiCall { apiService.getEventDetail(id) }) {
                            is ApiResult.Success -> {
                                detail.data.event?.let { dto ->
                                    val event = dto.toEvent(imageUrlProvider).copy(isBookmarked = true)
                                    loaded.add(event)
                                }
                            }
                            is ApiResult.Error -> { /* event not found or inaccessible */ }
                        }
                    }
                }
            }

            _savedEvents.value = loaded
            _isLoading.value = false
        }
    }

    fun toggleBookmark(eventId: String) {
        viewModelScope.launch {
            val currentIds = bookmarkManager.bookmarkedIds.first()
            val isRemoving = eventId in currentIds

            bookmarkManager.toggleBookmark(eventId)

            if (isRemoving) {
                val removed = _savedEvents.value.find { it.id.toString() == eventId }
                _savedEvents.value = _savedEvents.value.filter { it.id.toString() != eventId }
                val idLong = eventId.toLongOrNull()
                if (idLong != null) {
                    try {
                        apiService.removeBookmark(idLong)
                    } catch (_: Exception) {
                        if (removed != null) {
                            _savedEvents.value = _savedEvents.value + removed
                        }
                        bookmarkManager.toggleBookmark(eventId)
                        SnackbarManager.showError("Gagal menghapus bookmark")
                    }
                }
            } else {
                val idLong = eventId.toLongOrNull()
                if (idLong != null) {
                    try {
                        apiService.addBookmark(idLong)
                    } catch (_: Exception) {
                        bookmarkManager.toggleBookmark(eventId)
                        SnackbarManager.showError("Gagal menyimpan bookmark")
                    }
                }
            }
        }
    }

    fun refresh() {
        loadBookmarkedEvents()
    }
}
