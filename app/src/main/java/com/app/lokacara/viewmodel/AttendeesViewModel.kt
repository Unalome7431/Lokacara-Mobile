package com.app.lokacara.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.lokacara.data.remote.ApiResult
import com.app.lokacara.data.remote.ApiService
import com.app.lokacara.data.remote.dto.AttendeeDto
import com.app.lokacara.data.remote.safeApiCall
import com.app.lokacara.ui.components.SnackbarManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AttendeesViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _attendees = MutableStateFlow<List<AttendeeDto>>(emptyList())
    val attendees: StateFlow<List<AttendeeDto>> = _attendees.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var currentEventId: Long = 0

    fun loadAttendees(eventId: Long) {
        currentEventId = eventId
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = safeApiCall { apiService.getAttendees(eventId) }) {
                is ApiResult.Success -> {
                    _attendees.value = result.data.attendees.data
                }
                is ApiResult.Error -> {
                    _error.value = result.message
                }
            }

            _isLoading.value = false
        }
    }

    fun toggleAttendance(registrationId: Long) {
        if (currentEventId == 0L) return
        viewModelScope.launch {
            when (val result = safeApiCall {
                apiService.toggleAttendance(currentEventId, registrationId)
            }) {
                is ApiResult.Success -> {
                    val updated = result.data.registration
                    _attendees.value = _attendees.value.map {
                        if (it.id == registrationId) it.copy(
                            status = updated.status,
                            checked_in_at = updated.checked_in_at
                        ) else it
                    }
                    SnackbarManager.show("Peserta berhasil check-in")
                }
                is ApiResult.Error -> {
                    _error.value = result.message
                }
            }
        }
    }

    fun sendReminders() {
        if (currentEventId == 0L) return
        viewModelScope.launch {
            _error.value = null
            when (val result = safeApiCall { apiService.sendReminders(currentEventId) }) {
                is ApiResult.Error -> {
                    _error.value = result.message
                }
                is ApiResult.Success -> { }
            }
        }
    }
}
