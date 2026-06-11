package com.app.lokacara.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.lokacara.data.remote.ApiResult
import com.app.lokacara.data.remote.ImageUrlProvider
import com.app.lokacara.data.remote.dto.CategoryDto
import com.app.lokacara.data.remote.dto.LocationDto
import com.app.lokacara.data.remote.toEvent
import com.app.lokacara.model.Event
import com.app.lokacara.repository.ExploreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val repository: ExploreRepository,
    private val imageUrlProvider: ImageUrlProvider
) : ViewModel() {

    private val _allEvents = MutableStateFlow<List<Event>>(emptyList())

    private val _locationSuggestions = MutableStateFlow<List<String>>(
        listOf("Surabaya", "Surakarta", "Jakarta", "Semarang", "Yogyakarta")
    )
    val locationSuggestions: StateFlow<List<String>> = _locationSuggestions.asStateFlow()

    private val _categorySuggestions = MutableStateFlow<List<String>>(
        listOf("Workshop", "Wanita", "Webinar", "Anime", "Musik", "Teknologi")
    )
    val categorySuggestions: StateFlow<List<String>> = _categorySuggestions.asStateFlow()

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
            val matchName = name.isEmpty() || event.title.contains(name, ignoreCase = true)
            val matchLoc = loc.isEmpty() || event.location.contains(loc, ignoreCase = true)
            val matchCatText = cat.isEmpty() || event.category.contains(cat, ignoreCase = true)
            val matchChip = chip == "Semua" || event.category.equals(chip, ignoreCase = true)
            matchName && matchLoc && matchCatText && matchChip
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private var searchJob: Job? = null

    init {
        searchEvents("")
        loadFilterData()
    }

    private fun loadFilterData() {
        viewModelScope.launch {
            when (val result = repository.getCategories()) {
                is ApiResult.Success -> {
                    _categorySuggestions.value = result.data.map { it.name }
                }
                else -> {}
            }
        }
        viewModelScope.launch {
            when (val result = repository.getLocations()) {
                is ApiResult.Success -> {
                    _locationSuggestions.value = result.data.data.map { it.name }
                }
                else -> {}
            }
        }
    }

    fun searchEvents(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = repository.searchEvents(keyword = query.ifBlank { null })) {
                is ApiResult.Success -> {
                    _allEvents.value = result.data.data.map { it.toEvent(imageUrlProvider) }
                }
                is ApiResult.Error -> {
                    _error.value = result.message
                    if (_allEvents.value.isEmpty()) {
                        _allEvents.value = emptyList()
                    }
                }
            }

            _isLoading.value = false
        }
    }

    fun searchWithDebounce(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            searchEvents(query)
        }
    }

    fun resetFilters() {
        eventName.value = ""
        eventLocation.value = ""
        eventCategory.value = ""
        selectedCategoryChip.value = "Semua"
        searchEvents("")
    }

    fun refresh() {
        searchEvents(eventName.value)
        loadFilterData()
    }
}
