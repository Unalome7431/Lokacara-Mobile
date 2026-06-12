package com.app.lokacara.repository

import com.app.lokacara.data.remote.ApiResult
import com.app.lokacara.data.remote.ApiService
import com.app.lokacara.data.remote.dto.NotificationListResponse
import com.app.lokacara.data.remote.safeApiCall
import javax.inject.Inject

class NotificationRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getNotifications(): ApiResult<NotificationListResponse> {
        return safeApiCall { apiService.getNotifications() }
    }
}
