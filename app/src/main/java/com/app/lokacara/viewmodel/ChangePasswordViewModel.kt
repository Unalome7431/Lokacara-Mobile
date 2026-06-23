package com.app.lokacara.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class ChangePasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

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
                showError("Kata sandi lama harus diisi")
                return
            }
            newPassword.value.isBlank() -> {
                showError("Kata sandi baru harus diisi")
                return
            }
            newPassword.value.length < 6 -> {
                showError("Kata sandi baru minimal 6 karakter")
                return
            }
            newPassword.value != confirmPassword.value -> {
                showError("Konfirmasi kata sandi tidak cocok")
                return
            }
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            when (val result = authRepository.changePassword(
                oldPassword = oldPassword.value,
                newPassword = newPassword.value,
                newPasswordConfirmation = confirmPassword.value
            )) {
                is ApiResult.Success -> {
                    _changeSuccess.value = true
                    SnackbarManager.show("Password berhasil diubah")
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                    SnackbarManager.showError(result.message)
                }
            }
            _isLoading.value = false
        }
    }

    fun resetChangeSuccess() { _changeSuccess.value = false }
    fun clearError() { _errorMessage.value = null }

    private fun showError(message: String) {
        _errorMessage.value = message
        SnackbarManager.showError(message)
    }
}
