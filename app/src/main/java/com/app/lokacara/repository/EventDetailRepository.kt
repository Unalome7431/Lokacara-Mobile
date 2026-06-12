package com.app.lokacara.repository

import com.app.lokacara.data.remote.ApiResult
import com.app.lokacara.data.remote.ApiService
import com.app.lokacara.data.remote.dto.EventDetailResponse
import com.app.lokacara.data.remote.safeApiCall
import javax.inject.Inject

class EventDetailRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun getEventDetail(eventId: Long): ApiResult<EventDetailResponse> {
        return safeApiCall { apiService.getEventDetail(eventId) }
    }
}
