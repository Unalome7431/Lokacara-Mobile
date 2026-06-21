package com.app.lokacara.repository

import android.content.Context
import android.net.Uri
import com.app.lokacara.data.remote.ApiResult
import com.app.lokacara.data.remote.ApiService
import com.app.lokacara.data.remote.dto.AttendeesResponse
import com.app.lokacara.data.remote.dto.CertificateTemplateUploadResponse
import com.app.lokacara.data.remote.dto.DistributeCertificatesRequest
import com.app.lokacara.data.remote.dto.MessageResponse
import com.app.lokacara.data.remote.safeApiCall
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CertificateRepository @Inject constructor(
    private val apiService: ApiService,
    @param:ApplicationContext private val context: Context
) {
    suspend fun getAttendees(eventId: Long, page: Int = 1): ApiResult<AttendeesResponse> {
        return safeApiCall { apiService.getAttendees(eventId, page) }
    }

    suspend fun uploadTemplate(
        eventId: Long,
        uri: Uri,
        fileName: String,
        mimeType: String
    ): ApiResult<CertificateTemplateUploadResponse> = withContext(Dispatchers.IO) {
        safeApiCall {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: error("File template tidak dapat dibuka")
            val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("template", fileName, requestBody)
            apiService.uploadCertificateTemplate(eventId, part)
        }
    }

    suspend fun distribute(
        eventId: Long,
        request: DistributeCertificatesRequest
    ): ApiResult<MessageResponse> {
        return safeApiCall { apiService.distributeCertificates(eventId, request) }
    }
}
