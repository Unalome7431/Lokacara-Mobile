package com.app.lokacara.viewmodel

import androidx.lifecycle.ViewModel
import com.app.lokacara.model.HistoryEvent
import com.app.lokacara.model.UpcomingEvent
import com.app.lokacara.repository.TicketsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TicketsViewModel : ViewModel() {

    private val repository = TicketsRepository()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _upcomingEvents = MutableStateFlow<List<UpcomingEvent>>(emptyList())
    val upcomingEvents: StateFlow<List<UpcomingEvent>> = _upcomingEvents.asStateFlow()

    private val _historyEvents = MutableStateFlow<List<HistoryEvent>>(emptyList())
    val historyEvents: StateFlow<List<HistoryEvent>> = _historyEvents.asStateFlow()

    init {
        loadEvents()
    }

    private fun loadEvents() {
        _isLoading.value = true
        _upcomingEvents.value = repository.getUpcomingEvents()
        _historyEvents.value = repository.getHistoryEvents()
        _isLoading.value = false
    }
}