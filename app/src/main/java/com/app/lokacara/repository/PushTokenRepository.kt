package com.app.lokacara.repository

import com.app.lokacara.data.remote.ApiResult
import com.app.lokacara.data.remote.ApiService
import com.app.lokacara.data.remote.dto.DeletePushTokenRequest
import com.app.lokacara.data.remote.dto.MessageResponse
import com.app.lokacara.data.remote.dto.PushTokenRequest
import com.app.lokacara.data.remote.safeApiCall
import javax.inject.Inject

class PushTokenRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun register(token: String): ApiResult<MessageResponse> {
        return safeApiCall { apiService.registerPushToken(PushTokenRequest(token = token)) }
    }

    suspend fun unregister(token: String): ApiResult<MessageResponse> {
        return safeApiCall { apiService.unregisterPushToken(DeletePushTokenRequest(token = token)) }
    }
}
