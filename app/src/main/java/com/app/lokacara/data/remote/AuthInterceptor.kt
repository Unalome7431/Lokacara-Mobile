package com.app.lokacara.data.remote

import com.app.lokacara.data.UserSessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val sessionManager: UserSessionManager,
    private val tokenRefreshHelper: TokenRefreshHelper
) : Interceptor {
    private val refreshLock = Any()
    private var isRefreshing = false

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { sessionManager.getAccessToken() }

        val request = chain.request().newBuilder()
            .header("Accept", "application/json")

        if (token.isNotEmpty()) {
            request.header("Authorization", "Bearer $token")
        }

        val response = chain.proceed(request.build())

        if (response.code == 401 && token.isNotEmpty()) {
            synchronized(refreshLock) {
                if (!isRefreshing) {
                    isRefreshing = true
                    try {
                        val newToken = tokenRefreshHelper.refreshToken()
                        if (newToken != null) {
                            response.close()
                            val retryRequest = chain.request().newBuilder()
                                .header("Accept", "application/json")
                                .header("Authorization", "Bearer $newToken")
                                .build()
                            return chain.proceed(retryRequest)
                        }
                    } finally {
                        isRefreshing = false
                    }
                }
            }
        }

        return response
    }
}
