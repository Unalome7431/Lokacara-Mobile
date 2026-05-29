package com.app.lokacara.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.app.lokacara.data.SettingsManager
import com.app.lokacara.data.UserSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val settingsManager: SettingsManager,
    private val userSessionManager: UserSessionManager,
) : AndroidViewModel(application) {
    
    val isLoggedIn: Flow<Boolean> = userSessionManager.userSession.map { it.isLoggedIn }
    val isOnboardingCompleted: Flow<Boolean> = settingsManager.isOnboardingCompleted
}
