package com.app.lokacara.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.lokacara.data.PushTokenManager
import com.app.lokacara.data.SettingsManager
import com.app.lokacara.data.UserSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val settingsManager: SettingsManager,
    private val userSessionManager: UserSessionManager,
    private val pushTokenManager: PushTokenManager,
) : AndroidViewModel(application) {
    
    val isLoggedIn: Flow<Boolean> = userSessionManager.userSession
        .map { it.isLoggedIn }
        .catch { e ->
            Log.e("MainViewModel", "Error reading user session", e)
            emit(false)
        }

    val isOnboardingCompleted: Flow<Boolean> = settingsManager.isOnboardingCompleted
        .catch { e ->
            Log.e("MainViewModel", "Error reading onboarding", e)
            emit(false)
        }

    val notificationsEnabled: Flow<Boolean> = settingsManager.notificationsEnabled
        .catch { e ->
            Log.e("MainViewModel", "Error reading notification setting", e)
            emit(true)
        }

    fun syncPushToken() {
        viewModelScope.launch {
            pushTokenManager.syncCurrentTokenIfAuthenticated()
        }
    }
}
