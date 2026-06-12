package com.app.lokacara.data.remote

import com.app.lokacara.data.UserSessionManager
import com.app.lokacara.data.remote.dto.RefreshTokenResponse
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenRefreshHelper @Inject constructor(
    private val sessionManager: UserSessionManager,
    private val moshi: Moshi
) {

    companion object {
        private const val BASE_URL = "https://lokacara.my.id/"
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    fun refreshToken(): String? {
        return runBlocking {
            val currentToken = sessionManager.getAccessToken()
            if (currentToken.isEmpty()) return@runBlocking null

            try {
                val request = Request.Builder()
                    .url("${BASE_URL}api/auth/refresh")
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer $currentToken")
                    .post("{}".toRequestBody("application/json".toMediaType()))
                    .build()

                val response = client.newCall(request).execute()
                val body = response.body?.string()
                response.close()

                if (!response.isSuccessful || body == null) return@runBlocking null

                val refreshResponse = moshi.adapter(RefreshTokenResponse::class.java)
                    .fromJson(body) ?: return@runBlocking null

                val newToken = refreshResponse.token
                val session = sessionManager.userSession.first()
                sessionManager.saveAuth(
                    token = newToken,
                    userId = session.userId,
                    name = session.name,
                    email = session.email,
                    role = session.userRole
                )
                sessionManager.saveUserSession(
                    name = session.name,
                    email = session.email,
                    phone = session.phone,
                    location = session.location
                )

                newToken
            } catch (e: Exception) {
                null
            }
        }
    }
}
