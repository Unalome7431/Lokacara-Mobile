package com.app.lokacara.viewmodel

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.lokacara.data.BookmarkManager
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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    application: Application,
    private val repository: HomeRepository,
    private val bookmarkManager: BookmarkManager,
    private val imageUrlProvider: ImageUrlProvider,
    private val apiService: ApiService
) : AndroidViewModel(application) {

    private val _categories = MutableStateFlow<List<CategoryDto>>(emptyList())
    val categoryNames: StateFlow<List<String>> = _categories.map { listOf("Semua") + it.map { dto -> dto.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("Semua"))

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _popularEvents = MutableStateFlow<List<Event>>(emptyList())
    val popularEvents: StateFlow<List<Event>> = _popularEvents.asStateFlow()

    private val _allEvents = MutableStateFlow<List<Event>>(emptyList())

    val currentLocationName = MutableStateFlow("")
    private val _currentLatLng = MutableStateFlow<Pair<Double, Double>?>(null)

    val nearbyEvents: StateFlow<List<Event>> = combine(
        _allEvents, _currentLatLng
    ) { events, latLng ->
        if (latLng == null) events.take(5)
        else events.sortedBy { event ->
            if (event.latitude != null && event.longitude != null) {
                val dx = event.latitude - latLng.first
                val dy = event.longitude - latLng.second
                dx * dx + dy * dy // squared distance approximation
            } else Double.MAX_VALUE
        }.take(5)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedCategory = MutableStateFlow("Semua")

    val groupedEvents: StateFlow<Map<String, List<Event>>> = combine(
        _allEvents, selectedCategory
    ) { events, category ->
        val filtered = if (category == "Semua") events else events.filter { it.category == category }
        filtered.groupBy { it.category.ifEmpty { "Lainnya" } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    init {
        loadData()
        loadFilterData()
        autoDetectLocation()
    }

    private fun autoDetectLocation() {
        viewModelScope.launch {
            if (ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return@launch

            // Try FusedLocationProvider first
            try {
                val fusedClient = LocationServices.getFusedLocationProviderClient(getApplication())
                fusedClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) {
                        _currentLatLng.value = Pair(loc.latitude, loc.longitude)
                        viewModelScope.launch { resolveCityName(loc.latitude, loc.longitude) }
                    }
                }
            } catch (_: Exception) {}

            // Fallback to LocationManager
            val location = try {
                val mgr = getApplication<Application>().getSystemService(Context.LOCATION_SERVICE) as LocationManager
                mgr.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: mgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            } catch (_: Exception) { null }

            if (location != null && _currentLatLng.value == null) {
                _currentLatLng.value = Pair(location.latitude, location.longitude)
                resolveCityName(location.latitude, location.longitude)
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

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = repository.getFeedEvents()) {
                is ApiResult.Success -> {
                    val events = result.data.data.map { it.toEvent(imageUrlProvider) }
                    if (events.isNotEmpty()) {
                        _popularEvents.value = events.take(3)
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

    private fun loadFilterData() {
        viewModelScope.launch {
            val catResult = repository.getCategories()
            if (catResult is ApiResult.Success) _categories.value = catResult.data
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

    fun updateCategory(category: String) {
        selectedCategory.value = category
    }
}
