package com.app.lokacara.viewmodel

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.lokacara.data.AnalyticsTracker
import com.app.lokacara.data.BookmarkManager
import com.app.lokacara.data.HomeCache
import com.app.lokacara.data.remote.ApiResult
import com.app.lokacara.data.remote.ApiService
import com.app.lokacara.data.remote.ImageUrlProvider
import com.app.lokacara.data.remote.dto.CategoryDto
import com.app.lokacara.data.remote.toEvent
import com.app.lokacara.model.Event
import com.app.lokacara.repository.HomeRepository
import com.app.lokacara.ui.components.SnackbarManager
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.*

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    private val repository: HomeRepository,
    private val bookmarkManager: BookmarkManager,
    private val imageUrlProvider: ImageUrlProvider,
    private val apiService: ApiService,
    private val analytics: AnalyticsTracker,
    private val cache: HomeCache
) : AndroidViewModel(application) {

    private val _categories = MutableStateFlow<List<CategoryDto>>(emptyList())
    val categoryNames: StateFlow<List<String>> = _categories.map { listOf("Semua") + it.map { dto -> dto.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("Semua"))

    // ── Loading & Error states (granular) ──
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _feedError = MutableStateFlow<String?>(null)
    val feedError: StateFlow<String?> = _feedError.asStateFlow()

    private val _categoryError = MutableStateFlow<String?>(null)
    val categoryError: StateFlow<String?> = _categoryError.asStateFlow()

    // ── Events ──
    private val _popularEvents = MutableStateFlow<List<Event>>(emptyList())
    val popularEvents: StateFlow<List<Event>> = _popularEvents.asStateFlow()

    private val _allEvents = MutableStateFlow<List<Event>>(emptyList())

    // ── Location ──
    val currentLocationName = MutableStateFlow("")
    val isLocationLoading = MutableStateFlow(false)
    val isLocationPickerVisible = MutableStateFlow(false)
    private val _currentLatLng = MutableStateFlow<Pair<Double, Double>?>(null)

    // ── Pagination ──
    private var currentPage = 1
    private var totalPages = 1
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()
    val hasMorePages: Boolean get() = currentPage < totalPages

    // ── Nearby events (Haversine) ──
    val nearbyEvents: StateFlow<List<Event>> = combine(
        _allEvents, _currentLatLng
    ) { events, latLng ->
        if (latLng == null) events.take(5)
        else events.filter { it.latitude != null && it.longitude != null }
            .sortedBy { haversine(latLng.first, latLng.second, it.latitude!!, it.longitude!!) }
            .take(5)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Category grouping ──
    val selectedCategory = MutableStateFlow("Semua")

    val groupedEvents: StateFlow<Map<String, List<Event>>> = combine(
        _allEvents, selectedCategory
    ) { events, category ->
        val filtered = if (category == "Semua") events else events.filter { it.category == category }
        filtered.groupBy { it.category.ifEmpty { "Lainnya" } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    init {
        analytics.logScreenView("Home")
        loadData()
        loadFilterData()
        autoDetectLocation()
    }

    // ── Haversine formula ──
    private fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return R * 2 * atan2(sqrt(a), sqrt(1 - a))
    }

    // ── Location ──
    private fun autoDetectLocation() {
        viewModelScope.launch {
            if (ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return@launch

            isLocationLoading.value = true

            try {
                val fusedClient = LocationServices.getFusedLocationProviderClient(getApplication())
                fusedClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) {
                        _currentLatLng.value = Pair(loc.latitude, loc.longitude)
                        viewModelScope.launch { resolveCityName(loc.latitude, loc.longitude) }
                    }
                }
            } catch (_: Exception) {}

            val location = try {
                val mgr = getApplication<Application>().getSystemService(Context.LOCATION_SERVICE) as LocationManager
                mgr.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: mgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            } catch (_: Exception) { null }

            if (location != null && _currentLatLng.value == null) {
                _currentLatLng.value = Pair(location.latitude, location.longitude)
                resolveCityName(location.latitude, location.longitude)
            }

            if (currentLocationName.value.isBlank()) {
                delay(10000)
                isLocationLoading.value = false
                if (currentLocationName.value.isBlank()) {
                    currentLocationName.value = ""
                }
            } else {
                isLocationLoading.value = false
            }
        }
    }

    private suspend fun resolveCityName(lat: Double, lng: Double) {
        val city = withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(getApplication())
                val addresses = geocoder.getFromLocation(lat, lng, 1) ?: emptyList()
                if (addresses.isNotEmpty()) addresses[0].locality ?: addresses[0].subAdminArea ?: addresses[0].adminArea else null
            } catch (_: Exception) { null }
        }
        if (!city.isNullOrBlank()) currentLocationName.value = city
    }

    fun showLocationPicker() {
        isLocationPickerVisible.value = true
    }

    fun dismissLocationPicker() {
        isLocationPickerVisible.value = false
    }

    fun setManualLocation(cityName: String, lat: Double, lng: Double) {
        _currentLatLng.value = Pair(lat, lng)
        currentLocationName.value = cityName
        isLocationPickerVisible.value = false
        analytics.logEvent("location_set_manual", mapOf("city" to cityName))
    }

    // ── Data loading with stale-while-revalidate ──
    private fun loadData() {
        viewModelScope.launch {
            // Show cached data immediately if available
            val cachedEvents = repository.getFeedEvents(forceRefresh = false)
            if (cachedEvents is ApiResult.Success && _allEvents.value.isEmpty()) {
                val events = cachedEvents.data.map { it.toEvent(imageUrlProvider) }
                if (events.isNotEmpty()) {
                    _popularEvents.value = events.sortedByDescending { it.viewCount }.take(3)
                    _allEvents.value = events
                }
                _isLoading.value = false
            }

            // If no cache and refreshing, show loading
            if (_allEvents.value.isEmpty() && cachedEvents !is ApiResult.Success) {
                _isLoading.value = true
            } else {
                _isRefreshing.value = true
            }

            _feedError.value = null
            currentPage = 1

            when (val result = repository.getFeedEvents(forceRefresh = true)) {
                is ApiResult.Success -> {
                    val events = result.data.map { it.toEvent(imageUrlProvider) }
                    if (events.isNotEmpty()) {
                        _popularEvents.value = events.sortedByDescending { it.viewCount }.take(3)
                        _allEvents.value = events
                        _feedError.value = null
                    }
                    analytics.logEvent("feed_loaded", mapOf("count" to events.size.toString()))
                }
                is ApiResult.Error -> {
                    if (_allEvents.value.isEmpty()) {
                        _feedError.value = result.message
                    } else {
                        SnackbarManager.show("Gagal memperbarui data")
                    }
                }
            }

            _isLoading.value = false
            _isRefreshing.value = false
            syncBookmarks()
        }
    }

    private fun loadFilterData() {
        viewModelScope.launch {
            _categoryError.value = null

            val cachedCats = repository.getCategories(forceRefresh = false)
            if (cachedCats is ApiResult.Success && _categories.value.isEmpty()) {
                _categories.value = cachedCats.data
            }

            when (val result = repository.getCategories(forceRefresh = true)) {
                is ApiResult.Success -> _categories.value = result.data
                is ApiResult.Error -> {
                    if (_categories.value.isEmpty()) {
                        _categoryError.value = result.message
                    }
                }
            }
        }
    }

    // ── Load more (pagination via search API) ──
    fun loadMore() {
        if (_isLoadingMore.value || !hasMorePages) return
        viewModelScope.launch {
            _isLoadingMore.value = true
            val nextPage = currentPage + 1
            when (val result = safeApiCall { apiService.searchEvents(page = nextPage) }) {
                is ApiResult.Success -> {
                    val newEvents = result.data.data.map { it.toEvent(imageUrlProvider) }
                    currentPage = result.data.current_page
                    totalPages = result.data.last_page
                    _allEvents.value = _allEvents.value + newEvents
                }
                is ApiResult.Error -> {
                    SnackbarManager.show("Gagal memuat lebih banyak event")
                }
            }
            _isLoadingMore.value = false
        }
    }

    private suspend fun <T> safeApiCall(call: suspend () -> T): ApiResult<T> {
        return try {
            ApiResult.Success(call())
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Terjadi kesalahan")
        }
    }

    fun refresh() {
        cache.invalidate()
        loadData()
        loadFilterData()
    }

    // ── Bookmark sync ──
    private var bookmarkJob: kotlinx.coroutines.Job? = null

    private fun syncBookmarks() {
        bookmarkJob?.cancel()
        bookmarkJob = viewModelScope.launch {
            bookmarkManager.bookmarkedIds.collect { bookmarkedIds ->
                val syncEvent: (Event) -> Event = { event ->
                    val bookmarked = event.id.toString() in bookmarkedIds
                    if (event.isBookmarked != bookmarked) event.copy(isBookmarked = bookmarked) else event
                }
                _popularEvents.value = _popularEvents.value.map(syncEvent)
                _allEvents.value = _allEvents.value.map(syncEvent)
            }
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

    fun onEventClick(event: Event) {
        analytics.logClick("event_card", mapOf("event_id" to event.id.toString(), "title" to event.title))
    }

    fun updateCategory(category: String) {
        selectedCategory.value = category
    }

}

