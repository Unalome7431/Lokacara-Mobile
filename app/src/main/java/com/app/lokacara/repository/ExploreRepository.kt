package com.app.lokacara.repository

import com.app.lokacara.data.remote.ApiResult
import com.app.lokacara.data.remote.ApiService
import com.app.lokacara.data.remote.dto.PaginatedEventsResponse
import com.app.lokacara.data.remote.safeApiCall
import javax.inject.Inject

class ExploreRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun searchEvents(
        keyword: String? = null,
        categoryId: Int? = null,
        page: Int = 1
    ): ApiResult<PaginatedEventsResponse> {
        return safeApiCall {
            apiService.searchEvents(keyword = keyword, categoryId = categoryId, page = page)
        }
    }

    fun getLocations() = listOf("Surabaya", "Surakarta", "Jakarta", "Semarang", "Yogyakarta")
    fun getCategories() = listOf("Workshop", "Wanita", "Webinar", "Anime", "Musik", "Teknologi")
}
