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
            if (bookmarkedIds.isEmpty()) {
                _savedEvents.value = emptyList()
                _isLoading.value = false
                return@launch
            }

            safeApiCall { apiService.getFeedEvents() }.let { result ->
                when (result) {
                    is ApiResult.Success -> {
                        val allEvents = result.data.data
                        _savedEvents.value = allEvents
                            .filter { it.id.toString() in bookmarkedIds }
                            .map { it.toEvent(imageUrlProvider) }
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
            bookmarkManager.toggleBookmark(eventId)
            _savedEvents.value = _savedEvents.value.filter {
                bookmarkManager.bookmarkedIds.first().contains(it.id)
            }
        }
    }
}
