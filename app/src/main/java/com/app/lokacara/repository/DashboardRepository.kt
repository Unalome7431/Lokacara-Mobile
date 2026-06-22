package com.app.lokacara.repository

import com.app.lokacara.data.remote.ApiResult
import com.app.lokacara.data.remote.ApiService
import com.app.lokacara.data.remote.dto.DashboardResponse
import com.app.lokacara.data.remote.safeApiCall
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepository @Inject constructor(
    private val apiService: ApiService
) {
    private val requestMutex = Mutex()
    @Volatile private var cachedResponse: DashboardResponse? = null
    @Volatile private var cachedAtMillis = 0L
    private val _dashboard = MutableStateFlow<DashboardResponse?>(null)
    val dashboard: StateFlow<DashboardResponse?> = _dashboard.asStateFlow()

    suspend fun getDashboard(forceRefresh: Boolean = false): ApiResult<DashboardResponse> {
        cachedValue(forceRefresh)?.let { return ApiResult.Success(it) }
        return requestMutex.withLock {
            cachedValue(forceRefresh)?.let { return@withLock ApiResult.Success(it) }
            safeApiCall { apiService.getDashboard() }.also { result ->
                if (result is ApiResult.Success) {
                    cachedResponse = result.data
                    cachedAtMillis = System.currentTimeMillis()
                    _dashboard.value = result.data
                }
            }
        }
    }

    fun invalidate() {
        cachedAtMillis = 0L
    }

    private fun cachedValue(forceRefresh: Boolean): DashboardResponse? {
        if (forceRefresh || System.currentTimeMillis() - cachedAtMillis > CACHE_TTL_MS) return null
        return cachedResponse
    }

    private companion object {
        const val CACHE_TTL_MS = 30_000L
    }
}
