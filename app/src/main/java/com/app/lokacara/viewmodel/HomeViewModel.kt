package com.app.lokacara.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.lokacara.data.BookmarkManager
import com.app.lokacara.data.remote.ApiResult
import com.app.lokacara.data.remote.ImageUrlProvider
import com.app.lokacara.data.remote.dto.CategoryDto
import com.app.lokacara.data.remote.dto.LocationDto
import com.app.lokacara.data.remote.toEvent
import com.app.lokacara.model.Event
import com.app.lokacara.repository.HomeRepository
import com.app.lokacara.ui.components.SnackbarManager
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

    private val _locations = MutableStateFlow<List<LocationDto>>(emptyList())
    val locationNames: StateFlow<List<String>> = _locations.map { it.map { dto -> dto.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _categories = MutableStateFlow<List<CategoryDto>>(emptyList())
    val categoryNames: StateFlow<List<String>> = _categories.map { listOf("Semua") + it.map { dto -> dto.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("Semua", "Musik", "Teknologi", "Anime", "Hobi"))

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

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
        loadFilterData()
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

    private fun loadFilterData() {
        viewModelScope.launch {
            val catResult = repository.getCategories()
            if (catResult is ApiResult.Success) _categories.value = catResult.data

            val locResult = repository.getLocations()
            if (locResult is ApiResult.Success) _locations.value = locResult.data.data
        }
    }

    fun refresh() {
        loadData()
        loadFilterData()
    }

    private var bookmarkJob: kotlinx.coroutines.Job? = null

    private fun syncBookmarks() {
        bookmarkJob?.cancel()
        bookmarkJob = viewModelScope.launch {
            bookmarkManager.bookmarkedIds.collect { bookmarkedIds ->
                _nearbyEvents.value = _nearbyEvents.value.map { event ->
                    val bookmarked = event.id.toString() in bookmarkedIds
                    if (event.isBookmarked != bookmarked) event.copy(isBookmarked = bookmarked) else event
                }
                _popularEvents.value = _popularEvents.value.map { event ->
                    val bookmarked = event.id.toString() in bookmarkedIds
                    if (event.isBookmarked != bookmarked) event.copy(isBookmarked = bookmarked) else event
                }
            }
        }
    }

    fun toggleBookmark(eventId: String) {
        viewModelScope.launch {
            val wasBookmarked = bookmarkManager.bookmarkedIds.first().contains(eventId)
            bookmarkManager.toggleBookmark(eventId)
            if (wasBookmarked) {
                SnackbarManager.show("Event dihapus dari bookmark")
            } else {
                SnackbarManager.show("Event disimpan")
            }
        }
    }

    fun updateLocation(location: String) {
        selectedLocation.value = location
    }

    fun updateCategory(category: String) {
        selectedCategory.value = category
    }
}
