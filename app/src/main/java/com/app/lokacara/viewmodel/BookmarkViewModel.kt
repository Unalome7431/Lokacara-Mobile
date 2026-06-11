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

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _savedEvents = MutableStateFlow<List<Event>>(emptyList())
    val savedEvents: StateFlow<List<Event>> = _savedEvents.asStateFlow()

    init {
        loadBookmarkedEvents()
    }

    private fun loadBookmarkedEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val bookmarkedIds = bookmarkManager.bookmarkedIds.first()

            safeApiCall { apiService.getBookmarks() }.let { result ->
                when (result) {
                    is ApiResult.Success -> {
                        val bookmarkedEvents = result.data.data.map { it.toEvent(imageUrlProvider) }
                        _savedEvents.value = bookmarkedEvents.map { event ->
                            event.copy(isBookmarked = event.id.toString() in bookmarkedIds)
                        }
                    }
                    is ApiResult.Error -> {
                        _error.value = result.message
                    }
                }
            }

            _isLoading.value = false
        }
    }

    fun toggleBookmark(eventId: String) {
        viewModelScope.launch {
            val currentlyBookmarked = _savedEvents.value.any { it.id.toString() == eventId }
            if (currentlyBookmarked) {
                _savedEvents.value = _savedEvents.value.filter { it.id.toString() != eventId }
            }
            bookmarkManager.toggleBookmark(eventId)
        }
    }

    fun refresh() {
        loadBookmarkedEvents()
    }
}
