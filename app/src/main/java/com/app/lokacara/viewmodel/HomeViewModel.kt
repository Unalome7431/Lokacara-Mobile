package com.app.lokacara.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.lokacara.data.BookmarkManager
import com.app.lokacara.data.remote.ApiResult
import com.app.lokacara.data.remote.ImageUrlProvider
import com.app.lokacara.data.remote.toEvent
import com.app.lokacara.model.Event
import com.app.lokacara.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    private val repository: HomeRepository,
    private val bookmarkManager: BookmarkManager,
    private val imageUrlProvider: ImageUrlProvider
) : AndroidViewModel(application) {

    val locations = repository.getLocations()
    val categories = repository.getCategories()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _allEvents = MutableStateFlow<List<Event>>(emptyList())

    private val _popularEvents = MutableStateFlow<List<Event>>(emptyList())
    val popularEvents: StateFlow<List<Event>> = _popularEvents.asStateFlow()

    private val _nearbyEvents = MutableStateFlow<List<Event>>(emptyList())
    val nearbyEvents: StateFlow<List<Event>> = _nearbyEvents.asStateFlow()

    val selectedLocation = MutableStateFlow("Solo")
    val selectedCategory = MutableStateFlow("Semua")

    val filteredEvents: StateFlow<List<Event>> = combine(
        _nearbyEvents, selectedCategory
    ) { events, category ->
        if (category == "Semua") events
        else events.filter { it.category == category }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = repository.getFeedEvents()) {
                is ApiResult.Success -> {
                    val events = result.data.data.map { it.toEvent(imageUrlProvider) }
                    if (events.isNotEmpty()) {
                        _popularEvents.value = events.take(3)
                        _nearbyEvents.value = events
                        _allEvents.value = events
                    }
                }
                is ApiResult.Error -> {
                    _error.value = result.message
                }
            }

            _isLoading.value = false
            syncBookmarks()
        }
    }

    fun refresh() {
        loadData()
    }

    private fun syncBookmarks() {
        viewModelScope.launch {
            bookmarkManager.bookmarkedIds.collect { bookmarkedIds ->
                val sync: (List<Event>) -> List<Event> = { list ->
                    list.map { it.copy(isBookmarked = it.id in bookmarkedIds) }
                }
                _nearbyEvents.value = sync(_nearbyEvents.value)
                _popularEvents.value = sync(_popularEvents.value)
            }
        }
    }

    fun updateLocation(location: String) {
        selectedLocation.value = location
    }

    fun updateCategory(category: String) {
        selectedCategory.value = category
    }

    fun toggleBookmark(eventId: String) {
        viewModelScope.launch {
            bookmarkManager.toggleBookmark(eventId)
        }
    }
}
