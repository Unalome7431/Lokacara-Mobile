package com.app.lokacara.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.lokacara.data.remote.ApiResult
import com.app.lokacara.data.remote.ApiService
import com.app.lokacara.data.remote.dto.ScanRequest
import com.app.lokacara.data.remote.dto.ScanResponse
import com.app.lokacara.data.remote.safeApiCall
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QrScanViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    val qrToken = MutableStateFlow("")

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _result = MutableStateFlow<ScanResponse?>(null)
    val result: StateFlow<ScanResponse?> = _result.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var currentEventId: Long = 0

    fun setEventId(eventId: Long) {
        currentEventId = eventId
    }

    fun scan() {
        val token = qrToken.value.trim()
        if (token.isEmpty()) {
            _error.value = "Masukkan kode QR"
            return
        }
        if (currentEventId == 0L) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _result.value = null

            when (val res = safeApiCall { apiService.scanQr(currentEventId, ScanRequest(token)) }) {
                is ApiResult.Success -> {
                    _result.value = res.data
                }
                is ApiResult.Error -> {
                    _error.value = res.message
                }
            }

            _isLoading.value = false
        }
    }

    fun reset() {
        qrToken.value = ""
        _result.value = null
        _error.value = null
    }
}
