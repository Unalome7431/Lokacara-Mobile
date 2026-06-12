package com.app.lokacara.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.lokacara.data.remote.ApiResult
import com.app.lokacara.model.NotificationItem
import com.app.lokacara.model.NotificationType
import com.app.lokacara.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repository: NotificationRepository,
) : ViewModel() {
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val selectedTab = MutableStateFlow(0)

    val filteredNotifications = combine(_notifications, selectedTab) { notifs, tabIndex ->
        val type = if (tabIndex == 0) NotificationType.SOCIAL else NotificationType.SYSTEM
        notifs.filter { it.type == type }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = repository.getNotifications()) {
                is ApiResult.Success -> {
                    _notifications.value = result.data.data.map { dto ->
                        val type = when (dto.type) {
                            "social" -> NotificationType.SOCIAL
                            else -> NotificationType.SYSTEM
                        }
                        val rawDate = dto.created_at ?: ""
                        val dateDisplay = rawDate.take(10)
                        val timeDisplay = rawDate.substringAfter("T").take(8)
                        NotificationItem(
                            id = dto.id.toString(),
                            senderName = dto.sender_name ?: "Lokacara",
                            message = dto.message,
                            time = timeDisplay.ifEmpty { dateDisplay },
                            dateGroup = dateDisplay.ifEmpty { "Hari ini" },
                            type = type,
                            isRead = dto.is_read
                        )
                    }
                }
                is ApiResult.Error -> {
                    _error.value = result.message
                }
            }
            _isLoading.value = false
        }
    }

    fun refresh() {
        loadNotifications()
    }
}
