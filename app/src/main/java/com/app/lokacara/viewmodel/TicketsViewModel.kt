package com.app.lokacara.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.lokacara.data.FileStorageManager
import com.app.lokacara.model.HistoryEvent
import com.app.lokacara.model.UpcomingEvent
import com.app.lokacara.repository.TicketsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TicketsViewModel @Inject constructor(
    application: Application,
    private val repository: TicketsRepository,
    private val fileStorageManager: FileStorageManager,
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
        loadEvents()
    }

    private fun loadEvents() {
        _isLoading.value = true
        _upcomingEvents.value = repository.getUpcomingEvents()
        _historyEvents.value = repository.getHistoryEvents()
        _isLoading.value = false
    }

    fun downloadCertificate(event: HistoryEvent) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Gunakan nama file yang lebih aman (menghapus karakter aneh)
                val safeTitle = event.title.take(15).replace(Regex("[^a-zA-Z0-9]"), "_")
                val fileName = "cert_${safeTitle}_${System.currentTimeMillis()}.png"
                
                val path = fileStorageManager.saveCertificate(event.imageRes, fileName)
                
                if (path != null) {
                    _downloadedCertIds.value = _downloadedCertIds.value + event.title
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}