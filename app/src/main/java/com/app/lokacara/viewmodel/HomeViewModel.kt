package com.app.lokacara.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.lokacara.data.BookmarkManager
import com.app.lokacara.model.Event
import com.app.lokacara.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    private val repository: HomeRepository,
    private val bookmarkManager: BookmarkManager,
) : AndroidViewModel(application) {

    val locations = repository.getLocations()
    val categories = repository.getCategories()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _allNearbyEvents = MutableStateFlow<List<Event>>(emptyList())

    private val _popularEvents = MutableStateFlow<List<Event>>(emptyList())
    val popularEvents: StateFlow<List<Event>> = _popularEvents.asStateFlow()

    val selectedLocation = MutableStateFlow("Solo")
    val selectedCategory = MutableStateFlow("Semua")

    val filteredEvents: StateFlow<List<Event>> = combine(
        _allNearbyEvents, selectedCategory
    ) { events, category ->
        if (category == "Semua") {
            events
        } else {
            events.filter { it.category == category }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadData()
    }

    private fun loadData() {
        _isLoading.value = true
        _popularEvents.value = repository.getPopularEvents()
        _allNearbyEvents.value = repository.getNearbyEvents()
        _isLoading.value = false
        syncBookmarks()
    }

    private fun syncBookmarks() {
        viewModelScope.launch {
            bookmarkManager.bookmarkedIds.collect { bookmarkedIds ->
                _allNearbyEvents.value = _allNearbyEvents.value.map { event ->
                    event.copy(isBookmarked = event.id in bookmarkedIds)
                }
                _popularEvents.value = _popularEvents.value.map { event ->
                    event.copy(isBookmarked = event.id in bookmarkedIds)
                }
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