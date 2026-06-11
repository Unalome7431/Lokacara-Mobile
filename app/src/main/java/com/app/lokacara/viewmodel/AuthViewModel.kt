package com.app.lokacara.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.lokacara.data.SettingsManager
import com.app.lokacara.data.UserSessionManager
import com.app.lokacara.data.remote.ApiResult
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
        if (password.value.isBlank()) { _errorMessage.value = "Kata sandi harus diisi"; return }
        viewModelScope.launch {
            _errorMessage.value = null
            _isLoading.value = true
            when (val result = repository.login(email.value.trim(), password.value)) {
                is ApiResult.Success -> {
                    val auth = result.data
                    val user = auth.user
                    val userId = user?.id ?: 0L
                    userSessionManager.saveAuth(
                        token = auth.token ?: "",
                        userId = userId,
                        name = user?.name ?: "",
                        email = user?.email ?: "",
                        role = user?.role ?: ""
                    )
                    settingsManager.setOnboardingCompleted()
                    _loginSuccess.value = true
                    SnackbarManager.show("Login berhasil")
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
        if (password.value.length < 6) { _errorMessage.value = "Kata sandi minimal 6 karakter"; return }
        if (!isChecked.value) { _errorMessage.value = "Anda harus menyetujui syarat & ketentuan"; return }
        viewModelScope.launch {
            _errorMessage.value = null
            _isLoading.value = true
            when (val result = repository.register(name.value.trim(), email.value.trim(), password.value)) {
                is ApiResult.Success -> {
                    val auth = result.data
                    val user = auth.user
                    val userId = user?.id ?: 0L
                    userSessionManager.saveAuth(
                        token = auth.token ?: "",
                        userId = userId,
                        name = user?.name ?: "",
                        email = user?.email ?: "",
                        role = user?.role ?: ""
                    )
                    settingsManager.setOnboardingCompleted()
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
                    SnackbarManager.showError(result.message)
                }
            }
            _forgotPasswordLoading.value = false
        }
    }

    fun resetForgotPasswordSuccess() { _forgotPasswordSuccess.value = false }
    fun resetLoginSuccess() { _loginSuccess.value = false }
    fun resetRegisterSuccess() { _registerSuccess.value = false }
    fun clearError() { _errorMessage.value = null }
}
