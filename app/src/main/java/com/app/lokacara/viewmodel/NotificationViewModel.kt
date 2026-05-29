package com.app.lokacara.viewmodel

import androidx.lifecycle.ViewModel
import com.app.lokacara.model.NotificationItem
import com.app.lokacara.model.NotificationType
import com.app.lokacara.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repository: NotificationRepository,
) : ViewModel() {
    private val _notifications = MutableStateFlow(repository.getNotifications())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val selectedTab = MutableStateFlow(0)

    val filteredNotifications = selectedTab.map { tabIndex ->
        val type = if (tabIndex == 0) NotificationType.SOCIAL else NotificationType.SYSTEM
        _notifications.value.filter { it.type == type }
    }
}