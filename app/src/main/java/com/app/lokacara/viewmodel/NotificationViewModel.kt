package com.app.lokacara.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.lokacara.data.LatestRequestGate
import com.app.lokacara.data.filterNotifications
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
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val repository: NotificationRepository,
) : ViewModel() {
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    private val requestGate = LatestRequestGate()
    private var loadJob: Job? = null

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val selectedTab = MutableStateFlow(0)

    val filteredNotifications = combine(_notifications, selectedTab) { notifs, tabIndex ->
        filterNotifications(notifs, tabIndex)
    }.flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadNotifications()
    }

    private fun loadNotifications(force: Boolean = false) {
        if (!force && loadJob?.isActive == true) return
        if (force) loadJob?.cancel()
        val requestToken = requestGate.next()
        loadJob = viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val result = repository.getNotifications()) {
                is ApiResult.Success -> {
                    val notifications = withContext(Dispatchers.Default) {
                        result.data.data.map { dto ->
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
                            isRead = dto.is_read,
                            category = dto.category,
                            target = dto.target,
                            eventId = dto.event_id
                        )
                        }
                    }
                    if (!requestGate.isLatest(requestToken)) return@launch
                    _notifications.value = notifications
                }
                is ApiResult.Error -> {
                    if (!requestGate.isLatest(requestToken)) return@launch
                    _error.value = result.message
                }
            }
            if (requestGate.isLatest(requestToken)) _isLoading.value = false
        }
    }

    fun refresh() {
        loadNotifications(force = true)
    }
}
