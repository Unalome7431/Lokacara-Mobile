package com.app.lokacara.repository

import com.app.lokacara.data.HomeCache
import com.app.lokacara.data.remote.ApiResult
import com.app.lokacara.data.remote.ApiService
import com.app.lokacara.data.remote.ImageUrlProvider
import com.app.lokacara.data.remote.dto.CategoryDto
import com.app.lokacara.data.remote.dto.EventDto
import com.app.lokacara.data.remote.safeApiCall
import javax.inject.Inject

class HomeRepository @Inject constructor(
    private val apiService: ApiService,
    private val imageUrlProvider: ImageUrlProvider,
    private val cache: HomeCache
) {

    suspend fun getFeedEvents(forceRefresh: Boolean = false): ApiResult<List<EventDto>> {
        if (!forceRefresh && !cache.isStale) {
            val cached = cache.events
            if (cached != null) return ApiResult.Success(cached)
        }
        val result = safeApiCall { apiService.getFeedEvents().data }
        if (result is ApiResult.Success) {
            cache.putEvents(result.data)
        }
        return result
    }

    suspend fun getCategories(forceRefresh: Boolean = false): ApiResult<List<CategoryDto>> {
        if (!forceRefresh && !cache.isStale) {
            val cached = cache.categories
            if (cached != null) return ApiResult.Success(cached)
        }
        val result = safeApiCall { apiService.getCategories().data }
        if (result is ApiResult.Success) {
            cache.putCategories(result.data)
        }
        return result
    }
}
