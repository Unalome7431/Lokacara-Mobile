package com.app.lokacara.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.lokacara.data.BookmarkSyncHelper
import com.app.lokacara.data.UserSessionManager
import com.app.lokacara.data.remote.ApiResult
import com.app.lokacara.data.remote.ApiService
import com.app.lokacara.data.remote.ImageUrlProvider
import com.app.lokacara.data.remote.safeApiCall
import com.app.lokacara.data.remote.toEvent
import com.app.lokacara.model.Event
import com.app.lokacara.repository.EventDetailRepository
import com.app.lokacara.ui.components.SnackbarManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class EventDetailAction {
    JOIN,
    LEAVE,
    REMINDER,
    CANCEL
}

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    private val repository: EventDetailRepository,
    private val apiService: ApiService,
    private val imageUrlProvider: ImageUrlProvider,
    private val userSessionManager: UserSessionManager,
    private val bookmarkSyncHelper: BookmarkSyncHelper
) : ViewModel() {

    private var currentEventId: Long = 0
    private var hostUserId: Long = 0L

    private val _event = MutableStateFlow(
        Event(
            id = 0L, title = "Memuat...", description = "",
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

    private val _isReminderSending = MutableStateFlow(false)
    val isReminderSending: StateFlow<Boolean> = _isReminderSending.asStateFlow()

    private val _isCancelling = MutableStateFlow(false)
    val isCancelling: StateFlow<Boolean> = _isCancelling.asStateFlow()

    private val _isQrLoading = MutableStateFlow(false)
    val isQrLoading: StateFlow<Boolean> = _isQrLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _actionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = _actionError.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _lastAction = MutableStateFlow<EventDetailAction?>(null)
    val lastAction: StateFlow<EventDetailAction?> = _lastAction.asStateFlow()

    private val _qrToken = MutableStateFlow<String?>(null)
    val qrToken: StateFlow<String?> = _qrToken.asStateFlow()

    fun loadEvent(eventId: Long) {
        currentEventId = eventId
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val currentUserId = userSessionManager.userSession.first().userId

                when (val result = repository.getEventDetail(eventId)) {
                    is ApiResult.Success -> {
                        val detail = result.data
                        val eventDto = detail.event
                        if (eventDto != null) {
                            _event.value = eventDto.toEvent(imageUrlProvider)
                            hostUserId = eventDto.user?.id ?: 0L
                            _isHost.value = hostUserId > 0 && hostUserId == currentUserId
                            _isRegistered.value = detail.is_registered
                            bookmarkSyncHelper.syncBookmark(viewModelScope, _event)
                            if (detail.is_registered) loadQrTicket()
                        } else {
                            _error.value = "Event tidak ditemukan"
                        }
                    }
                    is ApiResult.Error -> {
                        _error.value = result.message
                    }
                }
            } catch (e: Exception) {
                _error.value = "Gagal memuat detail event"
            }
            _isLoading.value = false
        }
    }

    fun joinEvent() {
        if (currentEventId == 0L || _isJoining.value) return
        viewModelScope.launch {
            _isJoining.value = true
            _actionError.value = null
            _successMessage.value = null
            _lastAction.value = null
            try {
                safeApiCall { apiService.joinEvent(currentEventId) }.let { result ->
                when (result) {
                    is ApiResult.Success -> {
                        _isRegistered.value = true
                        _successMessage.value = "Berhasil mendaftar event!"
                        _lastAction.value = EventDetailAction.JOIN
                        SnackbarManager.show("Berhasil mendaftar event")
                        loadQrTicket()
                    }
                    is ApiResult.Error -> {
                        _actionError.value = result.message
                        SnackbarManager.showError(result.message)
                    }
                }
            }

            } catch (e: Exception) {
                    _actionError.value = "Gagal bergabung event"
                    SnackbarManager.showError("Gagal bergabung event")
                }
            _isJoining.value = false
        }
    }

    fun leaveEvent() {
        if (currentEventId == 0L || _isJoining.value) return
        viewModelScope.launch {
            _isJoining.value = true
            _actionError.value = null
            _successMessage.value = null
            _lastAction.value = null
            try {
                safeApiCall { apiService.leaveEvent(currentEventId) }.let { result ->
                when (result) {
                    is ApiResult.Success -> {
                        _isRegistered.value = false
                        _qrToken.value = null
                        _successMessage.value = "Berhasil membatalkan pendaftaran"
                        _lastAction.value = EventDetailAction.LEAVE
                        SnackbarManager.show("Berhasil keluar dari event")
                    }
                    is ApiResult.Error -> {
                        _actionError.value = result.message
                        SnackbarManager.showError(result.message)
                    }
                }
            }

            } catch (e: Exception) {
                    _actionError.value = "Gagal membatalkan pendaftaran"
                    SnackbarManager.showError("Gagal membatalkan pendaftaran")
                }
            _isJoining.value = false
        }
    }

    fun loadQrTicket() {
        if (currentEventId == 0L) return
        viewModelScope.launch {
            _isQrLoading.value = true
            when (val result = safeApiCall { apiService.getQrTicket(currentEventId) }) {
                is ApiResult.Success -> {
                    _qrToken.value = result.data.registration.qr_token
                }
                is ApiResult.Error -> {
                    _actionError.value = result.message
                }
            }
            _isQrLoading.value = false
        }
    }

    fun toggleBookmark() {
        val eventId = _event.value.id
        if (eventId == 0L) return
        bookmarkSyncHelper.toggleBookmark(viewModelScope, eventId.toString())
    }

    fun sendReminders() {
        if (currentEventId == 0L || _isReminderSending.value) return
        viewModelScope.launch {
            _isReminderSending.value = true
            _actionError.value = null
            _successMessage.value = null
            _lastAction.value = null
            safeApiCall { apiService.sendReminders(currentEventId) }.let { result ->
                when (result) {
                    is ApiResult.Success -> {
                        _successMessage.value = "Pengingat sedang dikirim"
                        _lastAction.value = EventDetailAction.REMINDER
                        SnackbarManager.show("Pengingat sedang dikirim")
                    }
                    is ApiResult.Error -> {
                        _actionError.value = result.message
                        SnackbarManager.showError(result.message)
                    }
                }
            }
            _isReminderSending.value = false
        }
    }

    fun cancelEvent() {
        if (currentEventId == 0L || !_isHost.value || _isCancelling.value) return
        viewModelScope.launch {
            _isCancelling.value = true
            _actionError.value = null
            _successMessage.value = null
            _lastAction.value = null
            when (val result = repository.cancelEvent(currentEventId)) {
                is ApiResult.Success -> {
                    _successMessage.value = result.data.message.ifBlank { "Event berhasil dibatalkan" }
                    _lastAction.value = EventDetailAction.CANCEL
                    SnackbarManager.show("Event berhasil dibatalkan")
                    loadEvent(currentEventId)
                }
                is ApiResult.Error -> {
                    _actionError.value = result.message
                    SnackbarManager.showError(result.message)
                }
            }
            _isCancelling.value = false
        }
    }

    fun clearMessages() {
        _actionError.value = null
        _successMessage.value = null
        _lastAction.value = null
    }

    override fun onCleared() {
        bookmarkSyncHelper.cancel()
        super.onCleared()
    }
}
