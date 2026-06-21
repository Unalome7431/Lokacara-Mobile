package com.app.lokacara.repository

import android.content.Context
import android.net.Uri
import com.app.lokacara.data.remote.ApiResult
import com.app.lokacara.data.remote.ApiService
import com.app.lokacara.data.remote.ImageUrlProvider
import com.app.lokacara.data.remote.dto.DashboardResponse
import com.app.lokacara.data.remote.dto.MessageResponse
import com.app.lokacara.data.remote.dto.ProfileResponse
import com.app.lokacara.data.remote.safeApiCall
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val apiService: ApiService,
    private val dashboardRepository: DashboardRepository,
    private val imageUrlProvider: ImageUrlProvider
) {
    suspend fun getProfile(): ApiResult<ProfileResponse> {
        return safeApiCall { apiService.getProfile() }
    }

    suspend fun getDashboard(forceRefresh: Boolean = false): ApiResult<DashboardResponse> {
        return dashboardRepository.getDashboard(forceRefresh)
    }

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

    suspend fun uploadAvatar(context: Context, imageUri: Uri): ApiResult<ProfileResponse> {
        val imagePart = try {
            withContext(Dispatchers.IO) {
            context.contentResolver.openAssetFileDescriptor(imageUri, "r")?.use { descriptor ->
                if (descriptor.length > 5_000_000) {
                    throw Exception("Ukuran foto maksimal 5 MB")
                }
            }
            val inputStream: InputStream = context.contentResolver.openInputStream(imageUri)
                ?: throw Exception("Cannot open image")
            val bytes = inputStream.use { it.readBytes() }
            val mimeType = context.contentResolver.getType(imageUri) ?: "image/jpeg"
            val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                MultipartBody.Part.createFormData("avatar", "avatar.jpg", requestBody)
            }
        } catch (exception: Exception) {
            return ApiResult.Error(exception.message ?: "Gagal membaca foto profil")
        }
        return safeApiCall { apiService.uploadAvatar(imagePart) }
    }
}
