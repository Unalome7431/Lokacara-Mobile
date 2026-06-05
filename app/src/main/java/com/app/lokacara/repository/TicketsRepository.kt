package com.app.lokacara.repository

import com.app.lokacara.data.remote.ApiResult
import com.app.lokacara.data.remote.ApiService
import com.app.lokacara.data.remote.dto.DashboardResponse
import com.app.lokacara.data.remote.safeApiCall
import javax.inject.Inject

class TicketsRepository @Inject constructor(
    private val dashboardRepository: DashboardRepository
) {
    suspend fun getDashboard(): ApiResult<DashboardResponse> {
        return dashboardRepository.getDashboard()
    }
}
