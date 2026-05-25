package com.app.lokacara.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.app.lokacara.data.SettingsManager
import com.app.lokacara.data.UserSessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsManager = SettingsManager(application)
    private val userSessionManager = UserSessionManager(application)
    
    val isLoggedIn: Flow<Boolean> = userSessionManager.userSession.map { it.isLoggedIn }
    val isOnboardingCompleted: Flow<Boolean> = settingsManager.isOnboardingCompleted
}
