package com.app.lokacara.viewmodel

import android.app.Application
import android.os.Build
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.lokacara.data.UserSessionManager
import com.app.lokacara.data.LatestRequestGate
import com.app.lokacara.data.remote.ApiResult
import com.app.lokacara.data.remote.ApiService
import com.app.lokacara.data.remote.BoundedImagePrefetcher
import com.app.lokacara.data.remote.ImageUrlProvider
import com.app.lokacara.data.remote.safeApiCall
import com.app.lokacara.data.remote.toHistoryEvent
import com.app.lokacara.data.remote.toUpcomingEvent
import com.app.lokacara.model.HistoryEvent
import com.app.lokacara.model.UpcomingEvent
import com.app.lokacara.ui.components.SnackbarManager
import com.app.lokacara.repository.TicketsRepository
import com.app.lokacara.repository.CertificateRepository
import coil.ImageLoader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TicketsViewModel @Inject constructor(
    application: Application,
    private val repository: TicketsRepository,
    private val certificateRepository: CertificateRepository,
    private val apiService: ApiService,
    private val imageUrlProvider: ImageUrlProvider,
    private val userSessionManager: UserSessionManager,
    private val imageLoader: ImageLoader
) : AndroidViewModel(application) {

    private val imagePrefetcher = BoundedImagePrefetcher(
        context = application,
        imageLoader = imageLoader,
        maxRequests = 5
    )
    private val dashboardGate = LatestRequestGate()
    private var dashboardJob: Job? = null

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isAuthChecked = MutableStateFlow(false)
    val isAuthChecked: StateFlow<Boolean> = _isAuthChecked.asStateFlow()

    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _upcomingEvents = MutableStateFlow<List<UpcomingEvent>>(emptyList())
    val upcomingEvents: StateFlow<List<UpcomingEvent>> = _upcomingEvents.asStateFlow()

    private val _todayEvents = MutableStateFlow<List<UpcomingEvent>>(emptyList())
    val todayEvents: StateFlow<List<UpcomingEvent>> = _todayEvents.asStateFlow()

    private val _historyEvents = MutableStateFlow<List<HistoryEvent>>(emptyList())
    val historyEvents: StateFlow<List<HistoryEvent>> = _historyEvents.asStateFlow()

    private val _downloadedCertIds = MutableStateFlow<Set<Long>>(emptySet())
    val downloadedCertIds: StateFlow<Set<Long>> = _downloadedCertIds.asStateFlow()

    private val _certificatePreviews = MutableStateFlow<Map<Long, String>>(emptyMap())
    val certificatePreviews: StateFlow<Map<Long, String>> = _certificatePreviews.asStateFlow()

    init {
        viewModelScope.launch {
            userSessionManager.userSession.collect { session ->
                val loggedIn = session.isLoggedIn
                _isLoggedIn.value = loggedIn
                _isAuthChecked.value = true
                _userName.value = session.name
                if (loggedIn) {
                    if (_upcomingEvents.value.isEmpty() && _historyEvents.value.isEmpty()) {
                        loadDashboard()
                    }
                } else {
                    dashboardJob?.cancel()
                    _userName.value = ""
                    _todayEvents.value = emptyList()
                    _upcomingEvents.value = emptyList()
                    _historyEvents.value = emptyList()
                    _error.value = null
                    _isLoading.value = false
                }
            }
        }
    }

    fun refresh() {
        loadDashboard(force = true)
    }

    private fun loadDashboard(force: Boolean = false) {
        if (!_isLoggedIn.value) return
        if (!force && dashboardJob?.isActive == true) return
        if (force) dashboardJob?.cancel()
        val requestToken = dashboardGate.next()
        dashboardJob = viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = repository.getDashboard(forceRefresh = force)) {
                is ApiResult.Success -> {
                    val (today, upcoming, history) = withContext(Dispatchers.Default) {
                        val nowMillis = System.currentTimeMillis()
                        val todayItems = mutableListOf<UpcomingEvent>()
                        val upcomingItems = mutableListOf<UpcomingEvent>()
                        val historyItems = mutableListOf<HistoryEvent>()

                        result.data.joined_events.forEach { reg ->
                            val event = reg.event ?: return@forEach
                            val startMillis = parseEventTimeMillis(event.start_datetime)
                            val endMillis = parseEventTimeMillis(event.end_datetime).takeIf { it > 0 } ?: startMillis
                            if (endMillis < nowMillis) {
                                reg.toHistoryEvent(imageUrlProvider)?.let { historyEvent ->
                                    historyItems.add(
                                        historyEvent.copy(
                                            date = formatTicketDate(event.start_datetime),
                                            time = formatTicketTime(event.start_datetime),
                                            location = readableLocation(event.location_name, event.platform_name, event.type)
                                        )
                                    )
                                }
                            } else {
                                reg.toUpcomingEvent(imageUrlProvider)?.let { upcomingEvent ->
                                    val uiEvent = upcomingEvent.copy(
                                        date = formatTicketDate(event.start_datetime),
                                        time = formatTicketTime(event.start_datetime),
                                        location = readableLocation(event.location_name, event.platform_name, event.type)
                                    )
                                    if (isSameDay(startMillis, nowMillis) || isSameDay(endMillis, nowMillis)) {
                                        todayItems.add(uiEvent)
                                    } else {
                                        upcomingItems.add(uiEvent)
                                    }
                                }
                            }
                        }
                        Triple(
                            todayItems.sortedBy { parseDisplayDateMillis(it.date, it.time) },
                            upcomingItems.sortedBy { parseDisplayDateMillis(it.date, it.time) },
                            historyItems.sortedByDescending { parseDisplayDateMillis(it.date, it.time) }
                        )
                    }
                    if (!dashboardGate.isLatest(requestToken)) return@launch
                    _todayEvents.value = today
                    _upcomingEvents.value = upcoming
                    _historyEvents.value = history
                    prefetchTicketImages(today + upcoming + history)
                    loadCertificatePreviews(history)
                }
                is ApiResult.Error -> {
                    if (!dashboardGate.isLatest(requestToken)) return@launch
                    _error.value = result.message
                }
            }

            if (dashboardGate.isLatest(requestToken)) _isLoading.value = false
        }
    }

    private fun prefetchTicketImages(events: List<Any>) {
        val urls = events.asSequence()
            .mapNotNull { event ->
                when (event) {
                    is UpcomingEvent -> event.imageUrl
                    is HistoryEvent -> event.imageUrl
                    else -> null
                }?.takeIf(String::isNotBlank)
            }
            .distinct()
            .take(6)
            .toList()

        imagePrefetcher.replace(urls, sizePx = 320)
    }

    fun downloadCertificate(event: HistoryEvent) {
        if (event.id == 0L) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val res = certificateRepository.saveParticipantCertificate(
                    event.id,
                    event.id.toString(),
                    event.title
                )
                when (res) {
                    is ApiResult.Success -> {
                        _downloadedCertIds.value = _downloadedCertIds.value + event.id
                        SnackbarManager.show("Sertifikat berhasil diunduh")
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

    private fun loadCertificatePreviews(historyEvents: List<HistoryEvent>) {
        viewModelScope.launch(Dispatchers.IO) {
            for (event in historyEvents) {
                if (event.id == 0L) continue
                val certId = event.id.toString()
                when (val result = certificateRepository.cacheParticipantCertificate(event.id, certId)) {
                    is ApiResult.Success -> {
                        _certificatePreviews.value = _certificatePreviews.value + (event.id to result.data)
                    }
                    is ApiResult.Error -> { }
                }
            }
        }
    }

    private fun readableLocation(locationName: String?, platformName: String?, type: String): String {
        return locationName?.takeIf { it.isNotBlank() }
            ?: platformName?.takeIf { it.isNotBlank() }
            ?: if (type.equals("online", ignoreCase = true)) "Online" else "Lokasi belum tersedia"
    }

    private fun parseEventTimeMillis(input: String): Long {
        val value = input.trim()
        if (value.isBlank()) return 0L
        val patterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd"
        )
        return patterns.firstNotNullOfOrNull { pattern ->
            runCatching {
                SimpleDateFormat(pattern, Locale.US).parse(value)?.time
            }.getOrNull()
        } ?: 0L
    }

    private fun formatTicketDate(input: String): String {
        val millis = parseEventTimeMillis(input)
        if (millis == 0L) return input.take(10)
        return SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id-ID")).format(Date(millis))
    }

    private fun formatTicketTime(input: String): String {
        val millis = parseEventTimeMillis(input)
        if (millis == 0L) {
            return input.substringAfter("T", input).substringAfter(" ").take(5)
        }
        return SimpleDateFormat("HH:mm", Locale.US).format(Date(millis))
    }

    private fun parseDisplayDateMillis(date: String, time: String): Long {
        val value = "$date $time"
        return runCatching {
            SimpleDateFormat("dd MMM yyyy HH:mm", Locale.forLanguageTag("id-ID")).parse(value)?.time
        }.getOrNull() ?: 0L
    }

    private fun isSameDay(firstMillis: Long, secondMillis: Long): Boolean {
        if (firstMillis <= 0L || secondMillis <= 0L) return false
        val first = java.util.Calendar.getInstance().apply { timeInMillis = firstMillis }
        val second = java.util.Calendar.getInstance().apply { timeInMillis = secondMillis }
        return first.get(java.util.Calendar.YEAR) == second.get(java.util.Calendar.YEAR) &&
            first.get(java.util.Calendar.DAY_OF_YEAR) == second.get(java.util.Calendar.DAY_OF_YEAR)
    }

    override fun onCleared() {
        dashboardJob?.cancel()
        imagePrefetcher.clear()
        super.onCleared()
    }
}
