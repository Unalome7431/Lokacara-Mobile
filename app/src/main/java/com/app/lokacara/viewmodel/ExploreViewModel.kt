package com.app.lokacara.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.lokacara.data.AnalyticsTracker
import com.app.lokacara.data.BookmarkSyncHelper
import com.app.lokacara.data.remote.ApiResult
import com.app.lokacara.data.remote.ApiService
import com.app.lokacara.data.remote.ImageUrlProvider
import com.app.lokacara.data.remote.toEvent
import com.app.lokacara.data.remote.dto.CategoryDto
import com.app.lokacara.model.Event
import com.app.lokacara.repository.ExploreRepository
import com.app.lokacara.ui.components.SnackbarManager
import coil.ImageLoader
import coil.request.ImageRequest
import coil.size.Precision
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

enum class DateFilter(val label: String) {
    SEMUA("Semua"),
    HARI_INI("Hari Ini"),
    BESOK("Besok"),
    MINGGU_INI("Minggu Ini"),
    BULAN_INI("Bulan Ini"),
    KUSTOM("Pilih Tanggal")
}

enum class PriceFilter(val label: String) {
    SEMUA("Semua"),
    GRATIS("Gratis"),
    DIBAWAH_50RB("< Rp50rb"),
    LIMA_PULUH_SERATUS("Rp50rb-Rp100rb"),
    DIATAS_100RB("> Rp100rb")
}

enum class SortOption(val label: String) {
    TERBARU("Terbaru"),
    TERPOPULER("Terpopuler"),
    TERMURAH("Termurah")
}

enum class ErrorType {
    NETWORK,
    SERVER,
    NO_RESULT
}

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val repository: ExploreRepository,
    private val imageUrlProvider: ImageUrlProvider,
    private val bookmarkSyncHelper: BookmarkSyncHelper,
    private val apiService: ApiService,
    private val analytics: AnalyticsTracker,
    private val imageLoader: ImageLoader,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _allEvents = MutableStateFlow<List<Event>>(emptyList())

    private val _locationSuggestions = MutableStateFlow<List<String>>(emptyList())
    val locationSuggestions: StateFlow<List<String>> = _locationSuggestions.asStateFlow()

    private val _categorySuggestions = MutableStateFlow<List<String>>(emptyList())
    val categorySuggestions: StateFlow<List<String>> = _categorySuggestions.asStateFlow()

    private val _categories = MutableStateFlow<List<CategoryDto>>(emptyList())

    private var currentPage = 1
    private var hasMorePages = true

    private val _isLoading = MutableStateFlow(true)
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

    private val _dateFilter = MutableStateFlow(DateFilter.SEMUA)
    val dateFilter: StateFlow<DateFilter> = _dateFilter.asStateFlow()

    private val _priceFilter = MutableStateFlow(PriceFilter.SEMUA)
    val priceFilter: StateFlow<PriceFilter> = _priceFilter.asStateFlow()

    private val _isGridView = MutableStateFlow(false)
    val isGridView: StateFlow<Boolean> = _isGridView.asStateFlow()

    private val _errorType = MutableStateFlow<ErrorType?>(null)
    val errorType: StateFlow<ErrorType?> = _errorType.asStateFlow()

    private val _searchHistory = MutableStateFlow<List<String>>(emptyList())
    val searchHistory: StateFlow<List<String>> = _searchHistory.asStateFlow()

    private val _showDatePicker = MutableStateFlow(false)
    val showDatePicker: StateFlow<Boolean> = _showDatePicker.asStateFlow()

    private val _customDateRange = MutableStateFlow<Pair<Long, Long>?>(null)

    val filteredEvents: StateFlow<List<Event>> = combine(
        combine(_allEvents, _eventName) { events, name -> events to name },
        combine(_eventLocation, _eventCategory) { loc, cat -> loc to cat },
        combine(_selectedCategoryChip, _sortOption) { chip, sort -> chip to sort },
        combine(_dateFilter, _priceFilter) { date, price -> date to price },
        _customDateRange
    ) { (events, name), (loc, cat), (chip, sort), (dateFilter, priceFilter), customRange ->
        val now = java.util.Calendar.getInstance()
        val todayStart = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0); set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
        val tomorrowEnd = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 23); set(java.util.Calendar.MINUTE, 59); set(java.util.Calendar.SECOND, 59)
            add(java.util.Calendar.DAY_OF_MONTH, 1)
        }.timeInMillis
        val weekEnd = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.SATURDAY)
            set(java.util.Calendar.HOUR_OF_DAY, 23); set(java.util.Calendar.MINUTE, 59); set(java.util.Calendar.SECOND, 59)
            if (before(java.util.Calendar.getInstance())) add(java.util.Calendar.WEEK_OF_MONTH, 1)
        }.timeInMillis
        val monthEnd = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.DAY_OF_MONTH, getActualMaximum(java.util.Calendar.DAY_OF_MONTH))
            set(java.util.Calendar.HOUR_OF_DAY, 23); set(java.util.Calendar.MINUTE, 59); set(java.util.Calendar.SECOND, 59)
        }.timeInMillis

        val filtered = events.filter { event ->
            val matchName = name.isEmpty() || event.title.contains(name, ignoreCase = true)
            val matchLoc = loc.isEmpty() || event.location.contains(loc, ignoreCase = true)
            val matchCatText = cat.isEmpty() || event.category.contains(cat, ignoreCase = true)
            val matchChip = chip == "Semua" || event.category.equals(chip, ignoreCase = true)
            val matchDate = when (dateFilter) {
                DateFilter.SEMUA -> true
                DateFilter.HARI_INI -> event.dateEpoch in todayStart..tomorrowEnd
                DateFilter.BESOK -> event.dateEpoch in tomorrowEnd..tomorrowEnd + 86400000L
                DateFilter.MINGGU_INI -> event.dateEpoch in todayStart..weekEnd
                DateFilter.BULAN_INI -> event.dateEpoch in todayStart..monthEnd
                DateFilter.KUSTOM -> {
                    val range = customRange
                    if (range != null) event.dateEpoch in range.first..range.second else true
                }
            }
            val matchPrice = when (priceFilter) {
                PriceFilter.SEMUA -> true
                PriceFilter.GRATIS -> event.price == "Gratis"
                PriceFilter.DIBAWAH_50RB -> parsePrice(event.price) in 1..49999
                PriceFilter.LIMA_PULUH_SERATUS -> parsePrice(event.price) in 50000..100000
                PriceFilter.DIATAS_100RB -> parsePrice(event.price) > 100000
            }
            matchName && matchLoc && matchCatText && matchChip && matchDate && matchPrice
        }
        when (sort) {
            SortOption.TERBARU -> filtered.sortedByDescending { it.dateEpoch }
            SortOption.TERPOPULER -> filtered.sortedByDescending { it.viewCount }
            SortOption.TERMURAH -> filtered.sortedBy { parsePrice(it.price) }
        }
    }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun parsePrice(price: String): Int {
        return when {
            price == "Gratis" -> 0
            price.startsWith("Rp ") -> price.removePrefix("Rp ").replace(".", "").trim().toIntOrNull() ?: Int.MAX_VALUE
            else -> Int.MAX_VALUE
        }
    }

    private var searchJob: Job? = null
    private var loadMoreJob: Job? = null
    private val prefetchedImageUrls = mutableSetOf<String>()

    init {
        analytics.logScreenView("Explore")
        resetFilters()
        loadFilterData()
    }

    private fun loadFilterData() {
        viewModelScope.launch {
            when (val result = repository.getCategories()) {
                is ApiResult.Success -> {
                    _categories.value = result.data
                    _categorySuggestions.value = result.data.map { it.name }
                    if (_selectedCategoryChip.value != DEFAULT_CATEGORY) {
                        searchEvents(_eventName.value)
                    }
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
    }

    fun clearEventName() {
        _eventName.value = ""
    }

    fun updateEventLocation(value: String) {
        _eventLocation.value = value
    }

    fun clearEventLocation() {
        _eventLocation.value = ""
    }

    fun updateEventCategory(value: String) {
        _eventCategory.value = value
    }

    fun clearEventCategory() {
        _eventCategory.value = ""
    }

    fun selectDateFilter(filter: DateFilter) {
        if (filter == DateFilter.KUSTOM) {
            _showDatePicker.value = true
            return
        }
        _dateFilter.value = filter
        _customDateRange.value = null
        analytics.logEvent("date_filter_selected", mapOf("filter" to filter.label))
    }

    fun dismissDatePicker() {
        _showDatePicker.value = false
    }

    fun setCustomDate(dateMillis: Long) {
        val cal = java.util.Calendar.getInstance()
        cal.timeInMillis = dateMillis
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
        cal.set(java.util.Calendar.MINUTE, 0)
        cal.set(java.util.Calendar.SECOND, 0)
        cal.set(java.util.Calendar.MILLISECOND, 0)
        val startOfDay = cal.timeInMillis
        cal.set(java.util.Calendar.HOUR_OF_DAY, 23)
        cal.set(java.util.Calendar.MINUTE, 59)
        cal.set(java.util.Calendar.SECOND, 59)
        val endOfDay = cal.timeInMillis
        _customDateRange.value = Pair(startOfDay, endOfDay)
        _dateFilter.value = DateFilter.KUSTOM
        _showDatePicker.value = false
        analytics.logEvent("date_filter_selected", mapOf("filter" to "custom"))
    }

    fun selectPriceFilter(filter: PriceFilter) {
        _priceFilter.value = filter
        analytics.logEvent("price_filter_selected", mapOf("filter" to filter.label))
    }

    fun toggleGridView() {
        _isGridView.value = !_isGridView.value
        analytics.logEvent("grid_view_toggled", mapOf("enabled" to _isGridView.value.toString()))
    }

    fun selectCategoryChip(category: String) {
        _selectedCategoryChip.value = category
        analytics.logEvent("category_chip_selected", mapOf("category" to category))
    }

    fun selectSortOption(option: SortOption) {
        _sortOption.value = option
        analytics.logEvent("sort_selected", mapOf("option" to option.label))
    }

    fun setInitialCategory(category: String) {
        val targetCategory = category.trim().ifEmpty { DEFAULT_CATEGORY }
        val alreadyDefault = targetCategory == DEFAULT_CATEGORY &&
                _eventName.value.isEmpty() &&
                _eventLocation.value.isEmpty() &&
                _eventCategory.value.isEmpty() &&
                _selectedCategoryChip.value == DEFAULT_CATEGORY &&
                _dateFilter.value == DateFilter.SEMUA &&
                _priceFilter.value == PriceFilter.SEMUA &&
                !_isSearchExpanded.value

        if (alreadyDefault) return

        _eventName.value = ""
        _eventLocation.value = ""
        _eventCategory.value = ""
        _selectedCategoryChip.value = targetCategory
        _dateFilter.value = DateFilter.SEMUA
        _priceFilter.value = PriceFilter.SEMUA
        _customDateRange.value = null
        _isSearchExpanded.value = false
        searchEvents("")
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
                    applyEvents(events)
                    hasMorePages = result.data.current_page < result.data.last_page
                    bookmarkSyncHelper.cancel()
                    bookmarkSyncHelper.syncBookmarks(viewModelScope, _allEvents)
                    analytics.logEvent("search_results", mapOf("count" to events.size.toString(), "query" to query))
                }
                is ApiResult.Error -> {
                    _error.value = result.message
                    _errorType.value = if (result.message.contains("jaringan") || result.message.contains("koneksi")) ErrorType.NETWORK else ErrorType.SERVER
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
                    applyEvents(_allEvents.value + newEvents)
                    hasMorePages = result.data.current_page < result.data.last_page
                    bookmarkSyncHelper.cancel()
                    bookmarkSyncHelper.syncBookmarks(viewModelScope, _allEvents)
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
        bookmarkSyncHelper.toggleBookmark(viewModelScope, eventId)
    }

    fun onSearchSubmit() {
        _isSearchExpanded.value = false
        val query = _eventName.value.trim()
        if (query.isNotEmpty() && !_searchHistory.value.any { it.equals(query, ignoreCase = true) }) {
            _searchHistory.value = (listOf(query) + _searchHistory.value).take(10)
        }
        searchEvents(query)
        analytics.logEvent("search_submit", mapOf("query" to query))
    }

    fun clearSearchHistory() {
        _searchHistory.value = emptyList()
    }

    fun resetFilters() {
        _eventName.value = ""
        _eventLocation.value = ""
        _eventCategory.value = ""
        _selectedCategoryChip.value = DEFAULT_CATEGORY
        _dateFilter.value = DateFilter.SEMUA
        _priceFilter.value = PriceFilter.SEMUA
        _customDateRange.value = null
        _isSearchExpanded.value = false
        searchEvents("")
        analytics.logEvent("filters_reset")
    }

    private fun applyEvents(events: List<Event>) {
        _allEvents.value = events
        prefetchExploreImages(events)
    }

    private fun prefetchExploreImages(events: List<Event>) {
        val urls = events.asSequence()
            .mapNotNull { it.imageUrl?.takeIf(String::isNotBlank) }
            .distinct()
            .take(8)
            .toList()

        if (urls.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            urls.forEach { imageUrl ->
                val shouldPrefetch = synchronized(prefetchedImageUrls) {
                    prefetchedImageUrls.add(imageUrl)
                }
                if (!shouldPrefetch) return@forEach

                imageLoader.enqueue(
                    ImageRequest.Builder(appContext)
                        .data(imageUrl)
                        .size(500)
                        .precision(Precision.INEXACT)
                        .crossfade(false)
                        .build()
                )
            }
        }
    }

    fun refresh() {
        searchEvents(_eventName.value)
        loadFilterData()
    }

    fun onEventClick(eventId: Long) {
        analytics.logClick("explore_event_card", mapOf("event_id" to eventId.toString()))
    }

    private companion object {
        const val DEFAULT_CATEGORY = "Semua"
    }
}
