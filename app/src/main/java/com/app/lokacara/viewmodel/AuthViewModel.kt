package com.app.lokacara.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.lokacara.data.SettingsManager
import com.app.lokacara.data.UserSessionManager
import com.app.lokacara.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    application: Application,
    private val repository: AuthRepository,
    private val userSessionManager: UserSessionManager,
    private val settingsManager: SettingsManager,
) : AndroidViewModel(application) {

    val email = MutableStateFlow("")
    val password = MutableStateFlow("")
    val isChecked = MutableStateFlow(false)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess.asStateFlow()

    private val _registerSuccess = MutableStateFlow(false)
    val registerSuccess: StateFlow<Boolean> = _registerSuccess.asStateFlow()

    fun login() {
        viewModelScope.launch {
            _errorMessage.value = null
            _isLoading.value = true
            delay(500)
            val result = repository.login(email.value.trim(), password.value)
            _isLoading.value = false
            val profile = result.getOrNull()
            if (profile != null) {
                userSessionManager.saveUserSession(
                    name = profile.name,
                    email = profile.email,
                    phone = profile.phone,
                    location = profile.location
                )
                settingsManager.setOnboardingCompleted()
                _loginSuccess.value = true
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Gagal masuk"
            }
        }
    }

    fun register() {
        viewModelScope.launch {
            _errorMessage.value = null
            _isLoading.value = true
            delay(500)
            val result = repository.register(email.value.trim(), password.value)
            _isLoading.value = false
            val profile = result.getOrNull()
            if (profile != null) {
                userSessionManager.saveUserSession(
                    name = profile.name,
                    email = profile.email,
                    phone = profile.phone,
                    location = profile.location
                )
                settingsManager.setOnboardingCompleted()
                _registerSuccess.value = true
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Gagal mendaftar"
            }
        }
    }

    fun resetLoginSuccess() { _loginSuccess.value = false }
    fun resetRegisterSuccess() { _registerSuccess.value = false }
    fun clearError() { _errorMessage.value = null }
}
