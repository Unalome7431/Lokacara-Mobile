package com.app.lokacara.repository

import android.content.Context
import android.net.Uri
import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.app.lokacara.data.remote.ApiResult
import com.app.lokacara.data.certificateDownloadFileName
import com.app.lokacara.data.remote.ApiService
import com.app.lokacara.data.remote.dto.AttendeesResponse
import com.app.lokacara.data.remote.dto.CertificateTemplateUploadResponse
import com.app.lokacara.data.remote.dto.DistributeCertificatesRequest
import com.app.lokacara.data.remote.dto.MessageResponse
import com.app.lokacara.data.remote.dto.OrganizerCertificateStateResponse
import com.app.lokacara.data.remote.safeApiCall
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CertificateRepository @Inject constructor(
    private val apiService: ApiService,
    private val dashboardRepository: DashboardRepository,
    @param:ApplicationContext private val context: Context
) {
    suspend fun getOrganizerState(eventId: Long): ApiResult<OrganizerCertificateStateResponse> {
        return safeApiCall { apiService.getOrganizerCertificateState(eventId) }
    }

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
        return safeApiCall { apiService.distributeCertificates(eventId, request) }.also {
            if (it is ApiResult.Success) dashboardRepository.invalidate()
        }
    }

    suspend fun cacheParticipantCertificate(
        eventId: Long,
        certificateId: String,
        forceRefresh: Boolean = false
    ): ApiResult<String> = withContext(Dispatchers.IO) {
        val cacheDir = File(context.cacheDir, "certificates").also(File::mkdirs)
        val target = File(cacheDir, "certificate_${certificateId.safeFilePart()}.jpg")
        if (!forceRefresh && target.exists() && target.length() > 0L) {
            return@withContext ApiResult.Success(target.absolutePath)
        }

        when (val result = safeApiCall { apiService.downloadCertificate(eventId) }) {
            is ApiResult.Error -> result
            is ApiResult.Success -> runCatching {
                val temp = File(cacheDir, "${target.name}.tmp")
                result.data.byteStream().use { input ->
                    FileOutputStream(temp).use(input::copyTo)
                }
                check(temp.length() > 0L) { "File sertifikat kosong" }
                if (target.exists()) target.delete()
                check(temp.renameTo(target)) { "Gagal menyimpan pratinjau sertifikat" }
                pruneCertificateCache(cacheDir, keep = 8)
                target.absolutePath
            }.fold(
                onSuccess = { ApiResult.Success(it) },
                onFailure = { ApiResult.Error(it.message ?: "Gagal menyimpan pratinjau sertifikat") }
            )
        }
    }

    suspend fun saveParticipantCertificate(
        eventId: Long,
        certificateId: String,
        eventTitle: String
    ): ApiResult<String> = withContext(Dispatchers.IO) {
        when (val cached = cacheParticipantCertificate(eventId, certificateId)) {
            is ApiResult.Error -> cached
            is ApiResult.Success -> runCatching {
                val source = File(cached.data)
                val fileName = certificateDownloadFileName(eventTitle)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val values = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                        put(MediaStore.Downloads.MIME_TYPE, "image/jpeg")
                        put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/Lokacara")
                        put(MediaStore.Downloads.IS_PENDING, 1)
                    }
                    val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                        ?: error("Penyimpanan unduhan tidak tersedia")
                    try {
                        context.contentResolver.openOutputStream(uri)?.use { output ->
                            source.inputStream().use { it.copyTo(output) }
                        } ?: error("File unduhan tidak dapat dibuka")
                        values.clear()
                        values.put(MediaStore.Downloads.IS_PENDING, 0)
                        context.contentResolver.update(uri, values, null, null)
                        uri.toString()
                    } catch (error: Throwable) {
                        context.contentResolver.delete(uri, null, null)
                        throw error
                    }
                } else {
                    val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                        ?.resolve("Lokacara")?.also(File::mkdirs)
                        ?: error("Penyimpanan unduhan tidak tersedia")
                    val target = File(dir, fileName)
                    source.inputStream().use { input -> target.outputStream().use(input::copyTo) }
                    target.absolutePath
                }
            }.fold(
                onSuccess = { ApiResult.Success(it) },
                onFailure = { ApiResult.Error(it.message ?: "Gagal mengunduh sertifikat") }
            )
        }
    }

    private fun pruneCertificateCache(directory: File, keep: Int) {
        directory.listFiles()?.filter { it.extension.equals("jpg", true) }
            ?.sortedByDescending(File::lastModified)
            ?.drop(keep)
            ?.forEach(File::delete)
    }

    private fun String.safeFilePart(): String =
        replace(Regex("[^a-zA-Z0-9_-]"), "_").ifBlank { "certificate" }
}
