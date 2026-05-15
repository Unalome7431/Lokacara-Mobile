package com.app.lokacara.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.lokacara.model.Event
import com.app.lokacara.repository.HomeRepository
import kotlinx.coroutines.flow.*

class HomeViewModel : ViewModel() {
    private val repository = HomeRepository()

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
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        loadData()
    }

    private fun loadData() {
        _isLoading.value = true
        _popularEvents.value = repository.getPopularEvents()
        _allNearbyEvents.value = repository.getNearbyEvents()
        _isLoading.value = false
    }

    fun updateLocation(location: String) {
        selectedLocation.value = location
    }

    fun updateCategory(category: String) {
        selectedCategory.value = category
    }

    fun toggleBookmark(eventId: String) {
        _allNearbyEvents.value = _allNearbyEvents.value.map { event ->
            if (event.id == eventId) {
                event.copy(isBookmarked = !event.isBookmarked)
            } else {
                event
            }
        }

    }
}