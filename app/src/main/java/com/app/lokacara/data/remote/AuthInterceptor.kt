package com.app.lokacara.data.remote

import com.app.lokacara.data.UserSessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val sessionManager: UserSessionManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { sessionManager.getAccessToken() }

        val request = chain.request().newBuilder()
            .header("Accept", "application/json")

        if (token.isNotEmpty()) {
            request.header("Authorization", "Bearer $token")
        }

        return chain.proceed(request.build())
    }
}
