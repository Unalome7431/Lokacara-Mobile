package com.app.lokacara.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.lokacara.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

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
            result.fold(
                onSuccess = { _loginSuccess.value = true },
                onFailure = { _errorMessage.value = it.message ?: "Gagal masuk" }
            )
        }
    }

    fun register() {
        viewModelScope.launch {
            _errorMessage.value = null
            _isLoading.value = true
            delay(500)
            val result = repository.register(email.value.trim(), password.value)
            _isLoading.value = false
            result.fold(
                onSuccess = { _registerSuccess.value = true },
                onFailure = { _errorMessage.value = it.message ?: "Gagal mendaftar" }
            )
        }
    }

    fun resetLoginSuccess() { _loginSuccess.value = false }
    fun resetRegisterSuccess() { _registerSuccess.value = false }
    fun clearError() { _errorMessage.value = null }
}
