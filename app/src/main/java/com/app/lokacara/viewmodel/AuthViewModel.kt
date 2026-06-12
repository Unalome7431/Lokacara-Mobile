package com.app.lokacara.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.lokacara.data.SettingsManager
import com.app.lokacara.data.UserSessionManager
import com.app.lokacara.data.remote.ApiResult
import com.app.lokacara.data.remote.dto.AuthResponse
import com.app.lokacara.repository.AuthRepository
import com.app.lokacara.ui.components.SnackbarManager
import dagger.hilt.android.lifecycle.HiltViewModel
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

    val name = MutableStateFlow("")
    val email = MutableStateFlow("")
    val password = MutableStateFlow("")
    val confirmPassword = MutableStateFlow("")
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
        if (email.value.isBlank()) { _errorMessage.value = "Email harus diisi"; return }
        val emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$".toRegex()
        if (!emailRegex.matches(email.value.trim())) {
            _errorMessage.value = "Format email tidak valid"
            return
        }
        if (password.value.isBlank()) { _errorMessage.value = "Kata sandi harus diisi"; return }
        viewModelScope.launch {
            _errorMessage.value = null
            _isLoading.value = true
            when (val result = repository.login(email.value.trim(), password.value)) {
                is ApiResult.Success -> {
                    if (saveAuthenticatedSession(result.data)) {
                        settingsManager.setOnboardingCompleted()
                        _loginSuccess.value = true
                        SnackbarManager.show("Login berhasil")
                    }
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                    SnackbarManager.showError(result.message)
                }
            }
            _isLoading.value = false
        }
    }

    fun register() {
        if (name.value.isBlank()) { _errorMessage.value = "Nama harus diisi"; return }
        if (email.value.isBlank()) { _errorMessage.value = "Email harus diisi"; return }
        val emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$".toRegex()
        if (!emailRegex.matches(email.value.trim())) {
            _errorMessage.value = "Format email tidak valid"
            return
        }
        if (password.value.length < 6) { _errorMessage.value = "Kata sandi minimal 6 karakter"; return }
        if (password.value != confirmPassword.value) { _errorMessage.value = "Password dan konfirmasi password tidak sama"; return }
        if (!isChecked.value) { _errorMessage.value = "Anda harus menyetujui syarat & ketentuan"; return }
        viewModelScope.launch {
            _errorMessage.value = null
            _isLoading.value = true
            when (val result = repository.register(name.value.trim(), email.value.trim(), password.value)) {
                is ApiResult.Success -> {
                    _registerSuccess.value = true
                    SnackbarManager.show("Akun berhasil dibuat")
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                }
            }
            _isLoading.value = false
        }
    }

    private val _forgotPasswordLoading = MutableStateFlow(false)
    val forgotPasswordLoading: StateFlow<Boolean> = _forgotPasswordLoading.asStateFlow()

    private val _forgotPasswordSuccess = MutableStateFlow(false)
    val forgotPasswordSuccess: StateFlow<Boolean> = _forgotPasswordSuccess.asStateFlow()

    private val _forgotPasswordError = MutableStateFlow<String?>(null)
    val forgotPasswordError: StateFlow<String?> = _forgotPasswordError.asStateFlow()

    fun forgotPassword(email: String) {
        if (email.isBlank()) {
            _forgotPasswordError.value = "Email harus diisi"
            return
        }
        val emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$".toRegex()
        if (!emailRegex.matches(email.trim())) {
            _forgotPasswordError.value = "Format email tidak valid"
            return
        }
        viewModelScope.launch {
            _forgotPasswordLoading.value = true
            _forgotPasswordError.value = null
            when (val result = repository.forgotPassword(email.trim())) {
                is ApiResult.Success -> {
                    _forgotPasswordSuccess.value = true
                    SnackbarManager.show("Link reset password telah dikirim ke email Anda")
                }
                is ApiResult.Error -> {
                    _forgotPasswordError.value = result.message
                }
            }
            _forgotPasswordLoading.value = false
        }
    }

    fun loginWithGoogle(idToken: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            when (val result = repository.loginWithGoogle(idToken)) {
                is ApiResult.Success -> {
                    if (saveAuthenticatedSession(result.data)) {
                        settingsManager.setOnboardingCompleted()
                        _loginSuccess.value = true
                        SnackbarManager.show("Login berhasil")
                    }
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                    SnackbarManager.showError(result.message)
                }
            }
            _isLoading.value = false
        }
    }

    fun resetForgotPasswordSuccess() { _forgotPasswordSuccess.value = false }
    fun resetLoginSuccess() { _loginSuccess.value = false }
    fun resetRegisterSuccess() { _registerSuccess.value = false }
    fun clearError() { _errorMessage.value = null }

    private suspend fun saveAuthenticatedSession(auth: AuthResponse): Boolean {
        val token = auth.token?.takeIf { it.isNotBlank() }
        val user = auth.user
        if (token == null || user == null || user.id <= 0L) {
            val message = "Respons login tidak valid"
            _errorMessage.value = message
            SnackbarManager.showError(message)
            return false
        }

        userSessionManager.saveAuth(
            token = token,
            userId = user.id,
            name = user.name,
            email = user.email,
            role = user.role
        )
        return true
    }

    fun resetForm() {
        name.value = ""
        email.value = ""
        password.value = ""
        confirmPassword.value = ""
        isChecked.value = false
    }
}
