package com.app.lokacara.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.app.lokacara.model.Event
import com.app.lokacara.repository.EventDetailRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EventDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: EventDetailRepository = EventDetailRepository()
) : ViewModel() {

    private val eventId: String = savedStateHandle.get<String>("eventId") ?: ""

    private val _event = MutableStateFlow<Event?>(null)
    val event: StateFlow<Event?> = _event.asStateFlow()

    private val _relatedEvents = MutableStateFlow<List<Event>>(emptyList())
    val relatedEvents: StateFlow<List<Event>> = _relatedEvents.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        _isLoading.value = true
        val result = repository.getEventById(eventId)
        if (result != null) {
            _event.value = result
            _relatedEvents.value = repository.getRelatedEvents(result.category, eventId)
        } else {
            _error.value = "Event tidak ditemukan"
        }
        _isLoading.value = false
    }

    companion object {
        fun factory(eventId: String): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return EventDetailViewModel(
                        savedStateHandle = SavedStateHandle(mapOf("eventId" to eventId))
                    ) as T
                }
            }
    }
}
