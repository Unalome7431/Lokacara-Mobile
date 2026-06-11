package com.app.lokacara.repository

import android.content.Context
import android.net.Uri
import com.app.lokacara.data.remote.ApiResult
import com.app.lokacara.data.remote.ApiService
import com.app.lokacara.data.remote.ImageUrlProvider
import com.app.lokacara.data.remote.dto.DashboardResponse
import com.app.lokacara.data.remote.dto.ProfileResponse
import com.app.lokacara.data.remote.safeApiCall
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
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

    suspend fun getDashboard(): ApiResult<DashboardResponse> {
        return dashboardRepository.getDashboard()
    }

    fun getPosterUrl(posterPath: String?): String? {
        return imageUrlProvider.posterUrl(posterPath)
    }

    suspend fun updateProfile(body: Map<String, String>): ApiResult<ProfileResponse> {
        return safeApiCall { apiService.updateProfile(body) }
    }

    suspend fun uploadAvatar(context: Context, imageUri: Uri): ApiResult<ProfileResponse> {
        return safeApiCall {
            val inputStream: InputStream = context.contentResolver.openInputStream(imageUri)
                ?: throw Exception("Cannot open image")
            val bytes = inputStream.use { it.readBytes() }
            val mimeType = context.contentResolver.getType(imageUri) ?: "image/jpeg"
            val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("avatar", "avatar.jpg", requestBody)
            apiService.uploadAvatar(imagePart)
        }
    }
}
