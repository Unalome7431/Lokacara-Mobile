package com.app.lokacara.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.lokacara.data.validation.Validators
import com.app.lokacara.data.validation.isValidEmail
import com.app.lokacara.data.validation.isSyntheticEmail
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

    private val _loginFieldErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    val loginFieldErrors: StateFlow<Map<String, String>> = _loginFieldErrors.asStateFlow()

    private val _registerFieldErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    val registerFieldErrors: StateFlow<Map<String, String>> = _registerFieldErrors.asStateFlow()

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess.asStateFlow()

    private val _registerSuccess = MutableStateFlow(false)
    val registerSuccess: StateFlow<Boolean> = _registerSuccess.asStateFlow()

    fun login() {
        if (_isLoading.value) return
        val errors = validateLogin()
        if (errors.isNotEmpty()) {
            val message = errors.values.first()
            _loginFieldErrors.value = errors
            _errorMessage.value = message
            SnackbarManager.showError(message)
            return
        }
        viewModelScope.launch {
            _errorMessage.value = null
            _loginFieldErrors.value = emptyMap()
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
        if (_isLoading.value) return
        val errors = validateRegister()
        if (errors.isNotEmpty()) {
            val message = errors.values.first()
            _registerFieldErrors.value = errors
            _errorMessage.value = message
            SnackbarManager.showError(message)
            return
        }
        viewModelScope.launch {
            _errorMessage.value = null
            _registerFieldErrors.value = emptyMap()
            _isLoading.value = true
            when (val result = repository.register(name.value.trim(), email.value.trim(), password.value)) {
                is ApiResult.Success -> {
                    _registerSuccess.value = true
                    SnackbarManager.show("Akun berhasil dibuat")
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                    SnackbarManager.showError(result.message)
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

    private val _changePasswordSuccess = MutableStateFlow(false)
    val changePasswordSuccess: StateFlow<Boolean> = _changePasswordSuccess.asStateFlow()

    val oldPassword = MutableStateFlow("")
    val newPassword = MutableStateFlow("")

    fun changePassword() {
        if (oldPassword.value.isBlank()) { showError("Kata sandi lama harus diisi"); return }
        if (newPassword.value.length < 6) { showError("Kata sandi baru minimal 6 karakter"); return }
        if (newPassword.value != confirmPassword.value) { showError("Password baru dan konfirmasi tidak sama"); return }
        viewModelScope.launch {
            _errorMessage.value = null
            _isLoading.value = true
            when (val result = repository.changePassword(oldPassword.value, newPassword.value, confirmPassword.value)) {
                is ApiResult.Success -> {
                    _changePasswordSuccess.value = true
                    SnackbarManager.show("Kata sandi berhasil diubah")
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                    SnackbarManager.showError(result.message)
                }
            }
            _isLoading.value = false
        }
    }

    fun resetChangePasswordSuccess() { _changePasswordSuccess.value = false }

    fun forgotPassword(email: String) {
        if (email.isBlank()) {
            _forgotPasswordError.value = "Email harus diisi"
            SnackbarManager.showError("Email harus diisi")
            return
        }
        val emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$".toRegex()
        if (!emailRegex.matches(email.trim())) {
            _forgotPasswordError.value = "Format email tidak valid"
            SnackbarManager.showError("Format email tidak valid")
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

    fun loginWithGoogle(idToken: String, fallbackEmail: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            when (val result = repository.loginWithGoogle(idToken)) {
                is ApiResult.Success -> {
                    if (saveAuthenticatedSession(result.data, fallbackEmail, provider = "google")) {
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
    fun clearError() {
        _errorMessage.value = null
        _loginFieldErrors.value = emptyMap()
        _registerFieldErrors.value = emptyMap()
    }

    fun clearLoginFieldError(field: String) {
        _errorMessage.value = null
        _loginFieldErrors.value = _loginFieldErrors.value - field
    }

    fun clearRegisterFieldError(field: String) {
        _errorMessage.value = null
        _registerFieldErrors.value = _registerFieldErrors.value - field
    }

    private fun validateLogin(): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        Validators.validateEmail(email.value)?.let { errors["email"] = it }
        if (password.value.isBlank()) errors["password"] = "Kata sandi harus diisi"
        return errors
    }

    private fun validateRegister(): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        Validators.validateName(name.value)?.let { errors["name"] = it }
        Validators.validateEmail(email.value)?.let { errors["email"] = it }
        Validators.validatePassword(password.value)?.let { errors["password"] = it }
        Validators.validatePasswordConfirmation(password.value, confirmPassword.value)?.let { errors["confirmPassword"] = it }
        if (!isChecked.value) errors["agreement"] = "Setujui syarat dan kebijakan privasi untuk daftar"
        return errors
    }

    private suspend fun saveAuthenticatedSession(auth: AuthResponse, fallbackEmail: String? = null, provider: String = "email"): Boolean {
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
            email = resolveSessionEmail(user.email, fallbackEmail),
            role = user.role,
            provider = provider
        )
        return true
    }

    private fun resolveSessionEmail(apiEmail: String, fallbackEmail: String?): String {
        val fallback = fallbackEmail?.trim().orEmpty()
        return when {
            apiEmail.isSyntheticEmail() && fallback.isValidEmail() && !fallback.isSyntheticEmail() -> fallback
            else -> apiEmail
        }
    }

    private fun showError(message: String) {
        _errorMessage.value = message
        SnackbarManager.showError(message)
    }

    fun resetForm() {
        name.value = ""
        email.value = ""
        password.value = ""
        confirmPassword.value = ""
        isChecked.value = false
        clearError()
    }
}
