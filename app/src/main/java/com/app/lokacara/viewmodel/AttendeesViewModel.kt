package com.app.lokacara.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.lokacara.data.remote.ApiResult
import com.app.lokacara.data.remote.ApiService
import com.app.lokacara.data.remote.dto.AttendeeDto
import com.app.lokacara.data.remote.dto.EventDto
import com.app.lokacara.data.remote.safeApiCall
import com.app.lokacara.data.mergeAttendeesById
import com.app.lokacara.data.replaceAttendeeById
import com.app.lokacara.ui.components.SnackbarManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import javax.inject.Inject

@HiltViewModel
class AttendeesViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _attendees = MutableStateFlow<List<AttendeeDto>>(emptyList())
    val attendees: StateFlow<List<AttendeeDto>> = _attendees.asStateFlow()

    private val _event = MutableStateFlow<EventDto?>(null)
    val event: StateFlow<EventDto?> = _event.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _isReminderSending = MutableStateFlow(false)
    val isReminderSending: StateFlow<Boolean> = _isReminderSending.asStateFlow()

    private val _togglingIds = MutableStateFlow<Set<Long>>(emptySet())
    val togglingIds: StateFlow<Set<Long>> = _togglingIds.asStateFlow()

    private val _totalCount = MutableStateFlow(0)
    val totalCount: StateFlow<Int> = _totalCount.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var currentEventId: Long = 0
    private var currentPage = 1
    private var hasMorePages = true
    private var loadJob: Job? = null
    private var loadMoreJob: Job? = null

    fun loadAttendees(eventId: Long, refresh: Boolean = false) {
        currentEventId = eventId
        loadJob?.cancel()
        loadMoreJob?.cancel()
        _isLoadingMore.value = false
        loadJob = viewModelScope.launch {
            currentPage = 1
            hasMorePages = true
            if (refresh) _isRefreshing.value = true else _isLoading.value = true
            _error.value = null

            when (val result = safeApiCall { apiService.getAttendees(eventId) }) {
                is ApiResult.Success -> {
                    _event.value = result.data.event
                    _attendees.value = result.data.attendees.data
                    _totalCount.value = result.data.attendees.total
                    hasMorePages = result.data.attendees.current_page < result.data.attendees.last_page
                }
                is ApiResult.Error -> {
                    _error.value = result.message
                    SnackbarManager.showError(result.message)
                }
            }

            _isLoading.value = false
            _isRefreshing.value = false
        }
    }

    fun refresh() {
        if (currentEventId == 0L) return
        loadAttendees(currentEventId, refresh = true)
    }

    fun loadNextPage() {
        if (currentEventId == 0L || !hasMorePages || _isLoadingMore.value || _isLoading.value || loadMoreJob?.isActive == true) return
        _isLoadingMore.value = true
        loadMoreJob = viewModelScope.launch {
            _error.value = null
            val nextPage = currentPage + 1
            when (val result = safeApiCall { apiService.getAttendees(currentEventId, nextPage) }) {
                is ApiResult.Success -> {
                    _event.value = result.data.event
                    currentPage = result.data.attendees.current_page
                    hasMorePages = result.data.attendees.current_page < result.data.attendees.last_page
                    _totalCount.value = result.data.attendees.total
                    _attendees.value = mergeAttendeesById(_attendees.value, result.data.attendees.data)
                }
                is ApiResult.Error -> {
                    _error.value = result.message
                    SnackbarManager.showError(result.message)
                }
            }
            _isLoadingMore.value = false
        }
    }

    fun toggleAttendance(registrationId: Long) {
        if (currentEventId == 0L) return
        viewModelScope.launch {
            _togglingIds.value = _togglingIds.value + registrationId
            when (val result = safeApiCall {
                apiService.toggleAttendance(currentEventId, registrationId)
            }) {
                is ApiResult.Success -> {
                    val updated = result.data.registration
                    val replacement = _attendees.value.firstOrNull { it.id == registrationId }
                        ?.copy(status = updated.status, checked_in_at = updated.checked_in_at)
                    if (replacement != null) {
                        _attendees.value = replaceAttendeeById(_attendees.value, replacement)
                    }
                    SnackbarManager.show(if (updated.status == "present") "Peserta ditandai hadir" else "Status peserta diperbarui")
                }
                is ApiResult.Error -> {
                    _error.value = result.message
                    SnackbarManager.showError(result.message)
                }
            }
            _togglingIds.value = _togglingIds.value - registrationId
        }
    }

    fun sendReminders() {
        if (currentEventId == 0L || _isReminderSending.value) return
        viewModelScope.launch {
            _isReminderSending.value = true
            _error.value = null
            when (val result = safeApiCall { apiService.sendReminders(currentEventId) }) {
                is ApiResult.Error -> {
                    _error.value = result.message
                    SnackbarManager.showError(result.message)
                }
                is ApiResult.Success -> {
                    SnackbarManager.show("Pengingat sedang dikirim")
                }
            }
            _isReminderSending.value = false
        }
    }
}
