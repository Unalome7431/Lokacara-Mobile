package com.app.lokacara.viewmodel

import android.app.Application
import com.app.lokacara.data.PushTokenManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.lokacara.data.SettingsManager
import com.app.lokacara.data.UserSessionManager
import com.app.lokacara.data.remote.ApiResult
import com.app.lokacara.data.remote.ApiService
import com.app.lokacara.data.remote.safeApiCall
import com.app.lokacara.ui.components.SnackbarManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    application: Application,
    private val settingsManager: SettingsManager,
    private val userSessionManager: UserSessionManager,
    private val pushTokenManager: PushTokenManager,
    private val apiService: ApiService
) : AndroidViewModel(application) {

    val notificationsEnabled: StateFlow<Boolean> = settingsManager.notificationsEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting.asStateFlow()

    private val _deleteError = MutableStateFlow<String?>(null)
    val deleteError: StateFlow<String?> = _deleteError.asStateFlow()

    private val _deleteSuccess = MutableStateFlow(false)
    val deleteSuccess: StateFlow<Boolean> = _deleteSuccess.asStateFlow()

    val isGoogleAuth: StateFlow<Boolean> = userSessionManager.userSession
        .map { it.authProvider == "google" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val hasLocalPassword: StateFlow<Boolean> = userSessionManager.userSession
        .map { it.hasLocalPassword }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val previous = notificationsEnabled.value
            settingsManager.setNotificationsEnabled(enabled)
            val synced = try {
                apiService.updateSettings(mapOf("notifications_enabled" to enabled))
                true
            } catch (_: Exception) {
                false
            }
            if (!synced) {
                settingsManager.setNotificationsEnabled(previous)
                SnackbarManager.showError("Gagal menyinkronkan pengaturan")
                return@launch
            }
            SnackbarManager.show(if (enabled) "Notifikasi diaktifkan" else "Notifikasi dinonaktifkan")
        }
    }

    fun deleteAccount(password: String) {
        if (password.isBlank()) {
            _deleteError.value = "Password harus diisi"
            return
        }
        viewModelScope.launch {
            _isDeleting.value = true
            _deleteError.value = null
            when (val result = safeApiCall { apiService.deleteAccount(mapOf("password" to password)) }) {
                is ApiResult.Success -> {
                    _deleteSuccess.value = true
                    SnackbarManager.show("Akun berhasil dihapus")
                }
                is ApiResult.Error -> {
                    _deleteError.value = result.message
                }
            }
            _isDeleting.value = false
        }
    }

    fun deleteGoogleAccount(googleToken: String) {
        if (googleToken.isBlank()) {
            _deleteError.value = "Token Google tidak valid"
            return
        }
        viewModelScope.launch {
            _isDeleting.value = true
            _deleteError.value = null
            when (val result = safeApiCall { apiService.deleteAccount(mapOf("google_token" to googleToken)) }) {
                is ApiResult.Success -> {
                    _deleteSuccess.value = true
                    SnackbarManager.show("Akun berhasil dihapus")
                }
                is ApiResult.Error -> {
                    _deleteError.value = result.message
                }
            }
            _isDeleting.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            pushTokenManager.unregisterLastSyncedToken()
            try { apiService.logout() } catch (_: Exception) {}
            userSessionManager.logout()
            SnackbarManager.show("Anda telah logout")
        }
    }

    fun resetDeleteSuccess() { _deleteSuccess.value = false }
    fun clearDeleteError() { _deleteError.value = null }
}
