package com.app.lokacara.repository

import android.content.Context
import android.net.Uri
import com.app.lokacara.data.media.MediaConstraints
import com.app.lokacara.data.media.prepareMediaFromUri
import com.app.lokacara.data.media.toMultipartBodyPart
import com.app.lokacara.data.remote.ApiResult
import com.app.lokacara.data.remote.ApiService
import com.app.lokacara.data.remote.ImageUrlProvider
import com.app.lokacara.data.remote.dto.DashboardResponse
import com.app.lokacara.data.remote.dto.MessageResponse
import com.app.lokacara.data.remote.dto.ProfileResponse
import com.app.lokacara.data.remote.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val apiService: ApiService,
    private val dashboardRepository: DashboardRepository,
    private val imageUrlProvider: ImageUrlProvider
) {
    val dashboard = dashboardRepository.dashboard

    suspend fun getProfile(): ApiResult<ProfileResponse> {
        return safeApiCall { apiService.getProfile() }
    }

    suspend fun getDashboard(forceRefresh: Boolean = false): ApiResult<DashboardResponse> {
        return dashboardRepository.getDashboard(forceRefresh)
    }

    fun invalidateDashboard() = dashboardRepository.invalidate()

    suspend fun cancelEvent(eventId: Long): ApiResult<MessageResponse> {
        dashboardRepository.invalidate()
        return safeApiCall { apiService.cancelEvent(eventId) }
    }

    fun getPosterUrl(posterPath: String?): String? {
        return imageUrlProvider.posterUrl(posterPath)
    }

    suspend fun updateProfile(body: Map<String, String>): ApiResult<ProfileResponse> {
        return safeApiCall { apiService.updateProfile(body) }
    }

    private val avatarConstraints = MediaConstraints(
        maxBytes = MAX_SOURCE_AVATAR_BYTES,
        maxDimensionPx = MAX_AVATAR_DIMENSION.toFloat(),
        jpegQuality = AVATAR_JPEG_QUALITY
    )

    suspend fun uploadAvatar(context: Context, imageUri: Uri): ApiResult<ProfileResponse> {
        val prepared = prepareMediaFromUri(context, imageUri, "avatar", avatarConstraints)
        val imagePart = when {
            prepared.isSuccess -> prepared.getOrThrow().toMultipartBodyPart()
            else -> {
                val message = prepared.exceptionOrNull()?.message ?: "Gagal membaca foto profil"
                return ApiResult.Error(message)
            }
        }
        return safeApiCall { apiService.uploadAvatar(imagePart) }
    }

    private companion object {
        const val MAX_SOURCE_AVATAR_BYTES = 15_000_000L
        const val MAX_AVATAR_DIMENSION = 720
        const val AVATAR_JPEG_QUALITY = 82
    }
}
