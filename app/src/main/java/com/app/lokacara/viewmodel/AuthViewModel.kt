package com.app.lokacara.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.lokacara.data.SettingsManager // Import SettingsManager
import com.app.lokacara.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// 1. Ubah menjadi AndroidViewModel agar bisa membaca Context/Application
class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AuthRepository()

    // 2. Inisialisasi SettingsManager di sini
    private val settingsManager = SettingsManager(application)

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
                onSuccess = {
                    // 3. SEBELUM loginSuccess diubah ke true, SIMPAN dulu tokennya ke DataStore
                    viewModelScope.launch {
                        // Kita simpan token dummy dulu untuk uji coba lokal
                        settingsManager.saveAuthSession("token_dummy_lokacara_123", 1, "Daffa Arrivo")

                        // Pastikan status onboarding juga ditandai sudah selesai
                        settingsManager.setOnboardingCompleted()

                        // Baru setelah sukses tersimpan, pindah halaman
                        _loginSuccess.value = true
                    }
                },
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