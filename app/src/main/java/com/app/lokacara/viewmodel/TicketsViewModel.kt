package com.app.lokacara.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.lokacara.data.remote.ApiResult
import com.app.lokacara.data.remote.ImageUrlProvider
import com.app.lokacara.data.remote.toHistoryEvent
import com.app.lokacara.data.remote.toUpcomingEvent
import com.app.lokacara.model.HistoryEvent
import com.app.lokacara.model.UpcomingEvent
import com.app.lokacara.repository.TicketsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TicketsViewModel @Inject constructor(
    application: Application,
    private val repository: TicketsRepository,
    private val imageUrlProvider: ImageUrlProvider
) : AndroidViewModel(application) {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _upcomingEvents = MutableStateFlow<List<UpcomingEvent>>(emptyList())
    val upcomingEvents: StateFlow<List<UpcomingEvent>> = _upcomingEvents.asStateFlow()

    private val _historyEvents = MutableStateFlow<List<HistoryEvent>>(emptyList())
    val historyEvents: StateFlow<List<HistoryEvent>> = _historyEvents.asStateFlow()

    private val _downloadedCertIds = MutableStateFlow<Set<String>>(emptySet())
    val downloadedCertIds: StateFlow<Set<String>> = _downloadedCertIds.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = repository.getDashboard()) {
                is ApiResult.Success -> {
                    val dashboard = result.data
                    val now = java.time.LocalDateTime.now().toString().take(10)

                    val upcoming = mutableListOf<UpcomingEvent>()
                    val history = mutableListOf<HistoryEvent>()

                    dashboard.joined_events.forEach { reg ->
                        val e = reg.event ?: return@forEach
                        val eventDate = e.start_datetime.take(10)
                        if (eventDate >= now) {
                            reg.toUpcomingEvent(imageUrlProvider)?.let { upcoming.add(it) }
                        } else {
                            reg.toHistoryEvent(imageUrlProvider)?.let { history.add(it) }
                        }
                    }

                    _upcomingEvents.value = upcoming
                    _historyEvents.value = history
                }
                is ApiResult.Error -> {
                    _error.value = result.message
                }
            }

            _isLoading.value = false
        }
    }

    fun markDownloaded(eventTitle: String) {
        _downloadedCertIds.value = _downloadedCertIds.value + eventTitle
    }

    fun downloadCertificate(event: HistoryEvent) {
        markDownloaded(event.title)
    }
}
