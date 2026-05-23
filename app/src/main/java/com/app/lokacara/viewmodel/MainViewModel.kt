package com.app.lokacara.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.lokacara.data.SettingsManager
import com.app.lokacara.ui.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsManager = SettingsManager(application)

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            val isOnboardingCompleted = settingsManager.isOnboardingCompleted.first()
            val token = settingsManager.authToken.first()

            if (!isOnboardingCompleted) {
                _startDestination.value = Screen.Onboarding.route
            } else if (token.isNullOrEmpty()) {
                _startDestination.value = Screen.Login.route
            } else {
                _startDestination.value = "main_container"
            }
        }
    }
}