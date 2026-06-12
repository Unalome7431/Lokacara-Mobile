package com.app.lokacara.repository

import com.app.lokacara.data.remote.ApiResult
import com.app.lokacara.data.remote.ApiService
import com.app.lokacara.data.remote.dto.DashboardResponse
import com.app.lokacara.data.remote.safeApiCall
import javax.inject.Inject

class DashboardRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getDashboard(): ApiResult<DashboardResponse> {
        return safeApiCall { apiService.getDashboard() }
    }
}
