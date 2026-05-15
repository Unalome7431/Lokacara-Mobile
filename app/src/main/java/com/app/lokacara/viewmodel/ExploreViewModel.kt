package com.app.lokacara.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.lokacara.model.Event
import com.app.lokacara.repository.ExploreRepository
import kotlinx.coroutines.flow.*

class ExploreViewModel : ViewModel() {
    private val repository = ExploreRepository()

    private val _allEvents = MutableStateFlow<List<Event>>(emptyList())
    val locationSuggestions = repository.getLocations()
    val categorySuggestions = repository.getCategories()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val isSearchExpanded = MutableStateFlow(false)
    val eventName = MutableStateFlow("")
    val eventLocation = MutableStateFlow("")
    val eventCategory = MutableStateFlow("")
    val selectedCategoryChip = MutableStateFlow("Semua")

    val filteredEvents: StateFlow<List<Event>> = combine(
        _allEvents, eventName, eventLocation, eventCategory, selectedCategoryChip
    ) { events, name, loc, cat, chip ->
        events.filter { event ->
            val matchName = event.title.contains(name, ignoreCase = true)
            val matchLoc = loc.isEmpty() || event.location.contains(loc, ignoreCase = true)
            val matchCatText = cat.isEmpty() || event.category.contains(cat, ignoreCase = true)
            val matchChip = chip == "Semua" || event.category.equals(chip, ignoreCase = true)

            matchName && matchLoc && matchCatText && matchChip
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        _isLoading.value = true
        _allEvents.value = repository.getEvents()
        _isLoading.value = false
    }

    fun resetFilters() {
        eventName.value = ""
        eventLocation.value = ""
        eventCategory.value = ""
        selectedCategoryChip.value = "Semua"
    }
}