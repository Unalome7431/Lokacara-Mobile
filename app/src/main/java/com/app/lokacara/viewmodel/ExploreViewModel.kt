package com.app.lokacara.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.lokacara.data.BookmarkManager
import com.app.lokacara.data.remote.ApiResult
import com.app.lokacara.data.remote.ApiService
import com.app.lokacara.data.remote.ImageUrlProvider
import com.app.lokacara.data.remote.dto.CategoryDto
import com.app.lokacara.data.remote.dto.LocationDto
import com.app.lokacara.data.remote.toEvent
import com.app.lokacara.model.Event
import com.app.lokacara.repository.ExploreRepository
import com.app.lokacara.ui.components.SnackbarManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val repository: ExploreRepository,
    private val imageUrlProvider: ImageUrlProvider,
    private val bookmarkManager: BookmarkManager,
    private val apiService: ApiService
) : ViewModel() {

    private val _allEvents = MutableStateFlow<List<Event>>(emptyList())

    private val _locationSuggestions = MutableStateFlow<List<String>>(emptyList())
    val locationSuggestions: StateFlow<List<String>> = _locationSuggestions.asStateFlow()

    private val _categorySuggestions = MutableStateFlow<List<String>>(emptyList())
    val categorySuggestions: StateFlow<List<String>> = _categorySuggestions.asStateFlow()

    private val _categories = MutableStateFlow<List<CategoryDto>>(emptyList())

    private var currentPage = 1
    private var hasMorePages = true

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
                    _categories.value = result.data
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
            currentPage = 1
            hasMorePages = true

            val catId = _categories.value.find { it.name.equals(selectedCategoryChip.value, ignoreCase = true) }?.id

            when (val result = repository.searchEvents(keyword = query.ifBlank { null }, categoryId = catId)) {
                is ApiResult.Success -> {
                    val events = result.data.data.map { it.toEvent(imageUrlProvider) }
                    _allEvents.value = events
                    hasMorePages = result.data.current_page < result.data.last_page
                    syncBookmarks()
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

    fun loadNextPage() {
        if (!hasMorePages || _isLoading.value) return
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _isLoading.value = true
            currentPage++

            val catId = _categories.value.find { it.name.equals(selectedCategoryChip.value, ignoreCase = true) }?.id

            when (val result = repository.searchEvents(keyword = eventName.value.ifBlank { null }, categoryId = catId, page = currentPage)) {
                is ApiResult.Success -> {
                    val newEvents = result.data.data.map { it.toEvent(imageUrlProvider) }
                    _allEvents.value = _allEvents.value + newEvents
                    hasMorePages = result.data.current_page < result.data.last_page
                    syncBookmarks()
                }
                is ApiResult.Error -> {
                    currentPage--
                    _error.value = result.message
                }
            }
            _isLoading.value = false
        }
    }

    fun toggleBookmark(eventId: String) {
        viewModelScope.launch {
            val wasBookmarked = bookmarkManager.bookmarkedIds.first().contains(eventId)
            bookmarkManager.toggleBookmark(eventId)
            val idLong = eventId.toLongOrNull()
            if (idLong != null) {
                if (wasBookmarked) {
                    try { apiService.removeBookmark(idLong) } catch (_: Exception) {}
                } else {
                    try { apiService.addBookmark(idLong) } catch (_: Exception) {}
                }
            }
            if (wasBookmarked) {
                SnackbarManager.show("Event dihapus dari bookmark")
            } else {
                SnackbarManager.show("Event disimpan")
            }
        }
    }

    private var bookmarkJob: Job? = null

    private fun syncBookmarks() {
        bookmarkJob?.cancel()
        bookmarkJob = viewModelScope.launch {
            bookmarkManager.bookmarkedIds.collect { bookmarkedIds ->
                _allEvents.value = _allEvents.value.map { event ->
                    val bookmarked = event.id.toString() in bookmarkedIds
                    if (event.isBookmarked != bookmarked) event.copy(isBookmarked = bookmarked) else event
                }
            }
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
