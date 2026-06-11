package com.app.lokacara.viewmodel

import android.app.Application
import android.os.Build
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.lokacara.data.remote.ApiResult
import com.app.lokacara.data.remote.ApiService
import com.app.lokacara.data.remote.ImageUrlProvider
import com.app.lokacara.data.remote.safeApiCall
import com.app.lokacara.data.remote.toHistoryEvent
import com.app.lokacara.data.remote.toUpcomingEvent
import com.app.lokacara.model.HistoryEvent
import com.app.lokacara.model.UpcomingEvent
import com.app.lokacara.repository.TicketsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class TicketsViewModel @Inject constructor(
    application: Application,
    private val repository: TicketsRepository,
    private val apiService: ApiService,
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

    private val _downloadedCertIds = MutableStateFlow<Set<Long>>(emptySet())
    val downloadedCertIds: StateFlow<Set<Long>> = _downloadedCertIds.asStateFlow()

    init {
        loadDashboard()
    }

    fun refresh() {
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

    fun downloadCertificate(event: HistoryEvent) {
        if (event.id == 0L) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val res = safeApiCall { apiService.downloadCertificate(event.id) }
                when (res) {
                    is ApiResult.Success -> {
                        val body = res.data
                        val fileName = "certificate_${event.title.take(20).replace(Regex("[^a-zA-Z0-9]"), "_")}.jpg"
                        val file = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            val downloadsDir = getApplication<Application>().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                            downloadsDir?.let { File(it, fileName) }
                        } else {
                            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                            downloadsDir.mkdirs()
                            File(downloadsDir, fileName)
                        } ?: return@launch
                        FileOutputStream(file).use { outputStream ->
                            body.byteStream().use { inputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                        _downloadedCertIds.value = _downloadedCertIds.value + event.id
                    }
                    is ApiResult.Error -> {
                        _error.value = res.message
                    }
                }
            } catch (e: Exception) {
                _error.value = "Gagal mengunduh sertifikat"
            }
        }
    }
}
