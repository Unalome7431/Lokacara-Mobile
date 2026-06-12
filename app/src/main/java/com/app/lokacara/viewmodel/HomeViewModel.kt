package com.app.lokacara.viewmodel

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.lokacara.data.BookmarkManager
import com.app.lokacara.data.remote.ApiResult
import com.app.lokacara.data.remote.ImageUrlProvider
import com.app.lokacara.data.remote.dto.CategoryDto
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

    val selectedCategory = MutableStateFlow("Semua")

    val filteredEvents: StateFlow<List<Event>> = combine(
        _nearbyEvents, selectedCategory
    ) { events, category ->
        events.filter { event ->
            category == "Semua" || event.category == category
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadData()
        loadFilterData()
        autoDetectLocation()
    }

    private fun autoDetectLocation() {
        viewModelScope.launch {
            if (ContextCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                val locationManager = getApplication<Application>().getSystemService(Context.LOCATION_SERVICE) as LocationManager
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            }
        }
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

    fun updateCategory(category: String) {
        selectedCategory.value = category
    }
}
