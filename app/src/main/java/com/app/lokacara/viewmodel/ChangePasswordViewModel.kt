package com.app.lokacara.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChangePasswordViewModel : ViewModel() {

    val oldPassword = MutableStateFlow("")
    val newPassword = MutableStateFlow("")
    val confirmPassword = MutableStateFlow("")

    val oldPasswordVisible = MutableStateFlow(false)
    val newPasswordVisible = MutableStateFlow(false)
    val confirmPasswordVisible = MutableStateFlow(false)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _changeSuccess = MutableStateFlow(false)
    val changeSuccess: StateFlow<Boolean> = _changeSuccess.asStateFlow()

    fun changePassword() {
        when {
            oldPassword.value.isBlank() -> {
                _errorMessage.value = "Kata sandi lama harus diisi"
                return
            }
            newPassword.value.isBlank() -> {
                _errorMessage.value = "Kata sandi baru harus diisi"
                return
            }
            newPassword.value.length < 6 -> {
                _errorMessage.value = "Kata sandi baru minimal 6 karakter"
                return
            }
            newPassword.value != confirmPassword.value -> {
                _errorMessage.value = "Konfirmasi kata sandi tidak cocok"
                return
            }
        }
        viewModelScope.launch {
            _isLoading.value = true
            delay(500)
            _isLoading.value = false
            _changeSuccess.value = true
        }
    }

    fun resetChangeSuccess() { _changeSuccess.value = false }
    fun clearError() { _errorMessage.value = null }
}
