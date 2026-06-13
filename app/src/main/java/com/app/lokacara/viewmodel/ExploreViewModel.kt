package com.app.lokacara.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.lokacara.data.AnalyticsTracker
import com.app.lokacara.data.BookmarkManager
import com.app.lokacara.data.remote.ApiResult
import com.app.lokacara.data.remote.ApiService
import com.app.lokacara.data.remote.ImageUrlProvider
import com.app.lokacara.data.remote.toEvent
import com.app.lokacara.data.remote.dto.CategoryDto
import com.app.lokacara.model.Event
import com.app.lokacara.repository.ExploreRepository
import com.app.lokacara.ui.components.SnackbarManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

enum class SortOption(val label: String) {
    TERBARU("Terbaru"),
    TERPOPULER("Terpopuler"),
    TERMURAH("Termurah")
}

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val repository: ExploreRepository,
    private val imageUrlProvider: ImageUrlProvider,
    private val bookmarkManager: BookmarkManager,
    private val apiService: ApiService,
    private val analytics: AnalyticsTracker
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

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isSearchExpanded = MutableStateFlow(false)
    val isSearchExpanded: StateFlow<Boolean> = _isSearchExpanded.asStateFlow()

    private val _eventName = MutableStateFlow("")
    val eventName: StateFlow<String> = _eventName.asStateFlow()

    private val _eventLocation = MutableStateFlow("")
    val eventLocation: StateFlow<String> = _eventLocation.asStateFlow()

    private val _eventCategory = MutableStateFlow("")
    val eventCategory: StateFlow<String> = _eventCategory.asStateFlow()

    private val _selectedCategoryChip = MutableStateFlow("Semua")
    val selectedCategoryChip: StateFlow<String> = _selectedCategoryChip.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.TERBARU)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    val filteredEvents: StateFlow<List<Event>> = combine(
        combine(_allEvents, _eventName) { events, name -> events to name },
        combine(_eventLocation, _eventCategory) { loc, cat -> loc to cat },
        combine(_selectedCategoryChip, _sortOption) { chip, sort -> chip to sort }
    ) { (events, name), (loc, cat), (chip, sort) ->
        val filtered = events.filter { event ->
            val matchName = name.isEmpty() || event.title.contains(name, ignoreCase = true)
            val matchLoc = loc.isEmpty() || event.location.contains(loc, ignoreCase = true)
            val matchCatText = cat.isEmpty() || event.category.contains(cat, ignoreCase = true)
            val matchChip = chip == "Semua" || event.category.equals(chip, ignoreCase = true)
            matchName && matchLoc && matchCatText && matchChip
        }
        when (sort) {
            SortOption.TERBARU -> filtered.sortedByDescending { parseDateMillis(it.date) }
            SortOption.TERPOPULER -> filtered.sortedByDescending { it.viewCount }
            SortOption.TERMURAH -> filtered.sortedBy { parsePrice(it.price) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun parseDateMillis(dateStr: String): Long {
        return try {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id-ID"))
            sdf.parse(dateStr)?.time ?: 0L
        } catch (_: Exception) { 0L }
    }

    private fun parsePrice(price: String): Int {
        return when {
            price == "Gratis" -> 0
            price.startsWith("Rp ") -> price.removePrefix("Rp ").replace(".", "").trim().toIntOrNull() ?: Int.MAX_VALUE
            else -> Int.MAX_VALUE
        }
    }

    private var searchJob: Job? = null
    private var loadMoreJob: Job? = null
    private var debounceJob: Job? = null

    init {
        analytics.logScreenView("Explore")
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
                is ApiResult.Error -> {
                    SnackbarManager.show("Gagal memuat kategori")
                }
            }
        }
        viewModelScope.launch {
            when (val result = repository.getLocations()) {
                is ApiResult.Success -> {
                    _locationSuggestions.value = result.data.data.map { it.name }
                }
                is ApiResult.Error -> {
                    SnackbarManager.show("Gagal memuat lokasi")
                }
            }
        }
    }

    fun toggleSearch() {
        _isSearchExpanded.value = !_isSearchExpanded.value
    }

    fun expandSearch() {
        _isSearchExpanded.value = true
    }

    fun collapseSearch() {
        _isSearchExpanded.value = false
    }

    fun updateEventName(value: String) {
        _eventName.value = value
        debounceSearch()
    }

    fun clearEventName() {
        _eventName.value = ""
        debounceSearch()
    }

    fun updateEventLocation(value: String) {
        _eventLocation.value = value
        debounceSearch()
    }

    fun clearEventLocation() {
        _eventLocation.value = ""
        debounceSearch()
    }

    fun updateEventCategory(value: String) {
        _eventCategory.value = value
        debounceSearch()
    }

    fun clearEventCategory() {
        _eventCategory.value = ""
        debounceSearch()
    }

    fun selectCategoryChip(category: String) {
        _selectedCategoryChip.value = category
        analytics.logEvent("category_chip_selected", mapOf("category" to category))
        searchEvents(_eventName.value)
    }

    fun selectSortOption(option: SortOption) {
        _sortOption.value = option
        analytics.logEvent("sort_selected", mapOf("option" to option.label))
        searchEvents(_eventName.value)
    }

    fun setInitialCategory(category: String) {
        if (category.isNotEmpty()) {
            _selectedCategoryChip.value = category
            searchEvents(_eventName.value)
        }
    }

    private fun debounceSearch() {
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch {
            delay(300)
            searchEvents(_eventName.value)
        }
    }

    private fun searchEvents(query: String) {
        searchJob?.cancel()
        loadMoreJob?.cancel()
        searchJob = viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            currentPage = 1
            hasMorePages = true

            val catId = _categories.value.find { it.name.equals(_selectedCategoryChip.value, ignoreCase = true) }?.id

            when (val result = repository.searchEvents(keyword = query.ifBlank { null }, categoryId = catId)) {
                is ApiResult.Success -> {
                    val events = result.data.data.map { it.toEvent(imageUrlProvider) }
                    _allEvents.value = events
                    hasMorePages = result.data.current_page < result.data.last_page
                    bookmarkJob?.cancel()
                    syncBookmarks()
                    analytics.logEvent("search_results", mapOf("count" to events.size.toString(), "query" to query))
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
        if (!hasMorePages || _isLoadingMore.value || _isLoading.value) return
        loadMoreJob?.cancel()
        loadMoreJob = viewModelScope.launch {
            _isLoadingMore.value = true
            currentPage++

            val catId = _categories.value.find { it.name.equals(_selectedCategoryChip.value, ignoreCase = true) }?.id

            when (val result = repository.searchEvents(keyword = _eventName.value.ifBlank { null }, categoryId = catId, page = currentPage)) {
                is ApiResult.Success -> {
                    val newEvents = result.data.data.map { it.toEvent(imageUrlProvider) }
                    _allEvents.value = _allEvents.value + newEvents
                    hasMorePages = result.data.current_page < result.data.last_page
                    bookmarkJob?.cancel()
                    syncBookmarks()
                }
                is ApiResult.Error -> {
                    currentPage--
                    _error.value = result.message
                }
            }
            _isLoadingMore.value = false
        }
    }

    fun toggleBookmark(eventId: String) {
        viewModelScope.launch {
            val bookmarkedIds = bookmarkManager.bookmarkedIds.first()
            val wasBookmarked = bookmarkedIds.contains(eventId)
            bookmarkManager.toggleBookmark(eventId)
            val idLong = eventId.toLongOrNull()
            if (idLong != null) {
                try {
                    if (wasBookmarked) apiService.removeBookmark(idLong) else apiService.addBookmark(idLong)
                } catch (_: Exception) {}
            }
            if (wasBookmarked) {
                SnackbarManager.show("Event dihapus dari bookmark")
                analytics.logEvent("bookmark_removed", mapOf("event_id" to eventId))
            } else {
                SnackbarManager.show("Event disimpan")
                analytics.logEvent("bookmark_added", mapOf("event_id" to eventId))
            }
        }
    }

    private var bookmarkJob: Job? = null

    private fun syncBookmarks() {
        bookmarkJob = viewModelScope.launch {
            bookmarkManager.bookmarkedIds.collect { bookmarkedIds ->
                _allEvents.value = _allEvents.value.map { event ->
                    val bookmarked = event.id.toString() in bookmarkedIds
                    if (event.isBookmarked != bookmarked) event.copy(isBookmarked = bookmarked) else event
                }
            }
        }
    }

    fun onSearchSubmit() {
        _isSearchExpanded.value = false
        searchEvents(_eventName.value)
        analytics.logEvent("search_submit", mapOf("query" to _eventName.value))
    }

    fun resetFilters() {
        _eventName.value = ""
        _eventLocation.value = ""
        _eventCategory.value = ""
        _selectedCategoryChip.value = "Semua"
        _isSearchExpanded.value = false
        searchEvents("")
        analytics.logEvent("filters_reset")
    }

    fun refresh() {
        searchEvents(_eventName.value)
        loadFilterData()
    }

    fun onEventClick(eventId: Long) {
        analytics.logClick("explore_event_card", mapOf("event_id" to eventId.toString()))
    }
}
