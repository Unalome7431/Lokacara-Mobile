package com.app.lokacara.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.lokacara.data.UserSessionManager
import com.app.lokacara.data.remote.ApiResult
import com.app.lokacara.data.remote.ApiService
import com.app.lokacara.data.remote.ImageUrlProvider
import com.app.lokacara.data.remote.safeApiCall
import com.app.lokacara.data.remote.toEvent
import com.app.lokacara.model.Event
import com.app.lokacara.repository.EventDetailRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    private val repository: EventDetailRepository,
    private val apiService: ApiService,
    private val imageUrlProvider: ImageUrlProvider,
    private val userSessionManager: UserSessionManager
) : ViewModel() {

    private var currentEventId: Long = 0
    private var hostUserId: Long = 0

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

    private val _isHost = MutableStateFlow(false)
    val isHost: StateFlow<Boolean> = _isHost.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isJoining = MutableStateFlow(false)
    val isJoining: StateFlow<Boolean> = _isJoining.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun loadEvent(eventId: Long) {
        currentEventId = eventId
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            val currentUserId = userSessionManager.userSession.first().userId.toLong()

            when (val result = repository.getEventDetail(eventId)) {
                is ApiResult.Success -> {
                    val detail = result.data
                    _event.value = detail.event.toEvent(imageUrlProvider)
                    _isRegistered.value = detail.is_registered
                    hostUserId = detail.event.user?.id ?: 0L
                    _isHost.value = hostUserId > 0 && hostUserId == currentUserId
                    if (detail.is_registered) loadQrTicket()
                }
                is ApiResult.Error -> {
                    _error.value = result.message
                }
            }

            _isLoading.value = false
        }
    }

    fun joinEvent() {
        if (currentEventId == 0L) return
        viewModelScope.launch {
            _isJoining.value = true
            _error.value = null
            _successMessage.value = null

            safeApiCall { apiService.joinEvent(currentEventId) }.let { result ->
                when (result) {
                    is ApiResult.Success -> {
                        _isRegistered.value = true
                        _successMessage.value = "Berhasil mendaftar event!"
                        loadQrTicket()
                    }
                    is ApiResult.Error -> {
                        _error.value = result.message
                    }
                }
            }

            _isJoining.value = false
        }
    }

    fun leaveEvent() {
        if (currentEventId == 0L) return
        viewModelScope.launch {
            _isJoining.value = true
            _error.value = null
            _successMessage.value = null

            safeApiCall { apiService.leaveEvent(currentEventId) }.let { result ->
                when (result) {
                    is ApiResult.Success -> {
                        _isRegistered.value = false
                        _successMessage.value = "Berhasil membatalkan pendaftaran"
                    }
                    is ApiResult.Error -> {
                        _error.value = result.message
                    }
                }
            }

            _isJoining.value = false
        }
    }

    fun loadQrTicket() {
        if (currentEventId == 0L) return
        viewModelScope.launch {
            when (safeApiCall { apiService.getQrTicket(currentEventId) }) {
                is ApiResult.Success -> { }
                is ApiResult.Error -> { }
            }
        }
    }

    fun sendReminders() {
        if (currentEventId == 0L) return
        viewModelScope.launch {
            _error.value = null
            _successMessage.value = null
            safeApiCall { apiService.sendReminders(currentEventId) }.let { result ->
                when (result) {
                    is ApiResult.Success -> {
                        _successMessage.value = "Pengingat sedang dikirim"
                    }
                    is ApiResult.Error -> {
                        _error.value = result.message
                    }
                }
            }
        }
    }

    fun clearMessages() {
        _error.value = null
        _successMessage.value = null
    }
}
