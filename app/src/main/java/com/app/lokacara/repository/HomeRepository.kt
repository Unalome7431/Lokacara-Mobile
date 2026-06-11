package com.app.lokacara.repository

import com.app.lokacara.data.remote.ApiResult
import com.app.lokacara.data.remote.ApiService
import com.app.lokacara.data.remote.ImageUrlProvider
import com.app.lokacara.data.remote.dto.CategoryDto
import com.app.lokacara.data.remote.dto.EventListResponse
import com.app.lokacara.data.remote.dto.LocationListResponse
import com.app.lokacara.data.remote.safeApiCall
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val apiService: ApiService,
    private val imageUrlProvider: ImageUrlProvider
) {

    suspend fun getFeedEvents(): ApiResult<EventListResponse> {
        return safeApiCall { apiService.getFeedEvents() }
    }

    suspend fun getCategories(): ApiResult<List<CategoryDto>> {
        return safeApiCall { apiService.getCategories() }
    }

    suspend fun getLocations(): ApiResult<LocationListResponse> {
        return safeApiCall { apiService.getLocations() }
    }
}
