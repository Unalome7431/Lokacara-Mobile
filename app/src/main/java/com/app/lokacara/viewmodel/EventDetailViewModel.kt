package com.app.lokacara.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.lokacara.data.remote.ApiResult
import com.app.lokacara.data.remote.ImageUrlProvider
import com.app.lokacara.data.remote.toEvent
import com.app.lokacara.model.Event
import com.app.lokacara.repository.EventDetailRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    private val repository: EventDetailRepository,
    private val imageUrlProvider: ImageUrlProvider
) : ViewModel() {

    private val _event = MutableStateFlow(
        Event(
            id = "", title = "Memuat...", description = "",
            date = "", location = "", price = "",
            imageUrl = null, category = "", isBookmarked = false,
            penyelenggara = ""
        )
    )
    val event: StateFlow<Event> = _event.asStateFlow()

    private val _isRegistered = MutableStateFlow(false)
    val isRegistered: StateFlow<Boolean> = _isRegistered.asStateFlow()

    private val _relatedEvents = MutableStateFlow<List<Event>>(emptyList())
    val relatedEvents: StateFlow<List<Event>> = _relatedEvents.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadEvent(eventId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = repository.getEventDetail(eventId)) {
                is ApiResult.Success -> {
                    val detail = result.data
                    _event.value = detail.event.toEvent(imageUrlProvider)
                    _isRegistered.value = detail.is_registered
                }
                is ApiResult.Error -> {
                    _error.value = result.message
                }
            }

            _isLoading.value = false
        }
    }
}
