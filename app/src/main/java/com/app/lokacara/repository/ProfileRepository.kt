package com.app.lokacara.repository

import com.app.lokacara.data.remote.ApiResult
import com.app.lokacara.data.remote.ApiService
import com.app.lokacara.data.remote.ImageUrlProvider
import com.app.lokacara.data.remote.dto.DashboardResponse
import com.app.lokacara.data.remote.dto.ProfileResponse
import com.app.lokacara.data.remote.safeApiCall
import javax.inject.Inject

class ProfileRepository @Inject constructor(
    private val apiService: ApiService,
    private val dashboardRepository: DashboardRepository,
    private val imageUrlProvider: ImageUrlProvider
) {
    suspend fun getProfile(): ApiResult<ProfileResponse> {
        return safeApiCall { apiService.getProfile() }
    }

    suspend fun getDashboard(): ApiResult<DashboardResponse> {
        return dashboardRepository.getDashboard()
    }

    fun getPosterUrl(posterPath: String?): String? {
        return imageUrlProvider.posterUrl(posterPath)
    }
}
