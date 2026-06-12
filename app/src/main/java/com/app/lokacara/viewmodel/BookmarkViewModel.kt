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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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

            // Fetch from feed and filter by local bookmarked IDs
            when (val result = safeApiCall { apiService.getFeedEvents() }) {
                is ApiResult.Success -> {
                    _savedEvents.value = result.data.data
                        .filter { it.id.toString() in bookmarkedIds }
                        .map { it.toEvent(imageUrlProvider).copy(isBookmarked = true) }
                }
                is ApiResult.Error -> {
                    _savedEvents.value = emptyList()
                }
            }

            _isLoading.value = false
        }
    }

    fun toggleBookmark(eventId: String) {
        viewModelScope.launch {
            val currentIds = bookmarkManager.bookmarkedIds.first()
            val isRemoving = eventId in currentIds

            if (isRemoving) {
                _savedEvents.value = _savedEvents.value.filter { it.id.toString() != eventId }
            }
            bookmarkManager.toggleBookmark(eventId)
            val idLong = eventId.toLongOrNull()
            if (idLong != null) {
                val synced = try {
                    if (isRemoving) apiService.removeBookmark(idLong) else apiService.addBookmark(idLong)
                    true
                } catch (_: Exception) {
                    false
                }
                if (!synced) {
                    bookmarkManager.toggleBookmark(eventId)
                    loadBookmarkedEvents()
                    SnackbarManager.showError("Gagal menyinkronkan bookmark")
                }
            }
        }
    }

    fun refresh() {
        loadBookmarkedEvents()
    }
}
