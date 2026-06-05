package com.app.lokacara.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.lokacara.data.remote.ApiResult
import com.app.lokacara.data.remote.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class CreateEventViewModel @Inject constructor(
    application: Application,
    private val apiService: ApiService
) : AndroidViewModel(application) {

    val namaEvent = MutableStateFlow("")
    val kategori = MutableStateFlow("")
    val penyelenggara = MutableStateFlow("")
    val waktuMulai = MutableStateFlow("")
    val waktuSelesai = MutableStateFlow("")
    val isOnline = MutableStateFlow(true)
    val aplikasiTempat = MutableStateFlow("")
    val alamat = MutableStateFlow("")
    val deskripsi = MutableStateFlow("")
    val kuota = MutableStateFlow(50)
    val posterUri = MutableStateFlow<Uri?>(null)

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _publishSuccess = MutableStateFlow(false)
    val publishSuccess: StateFlow<Boolean> = _publishSuccess.asStateFlow()

    fun publish() {
        val title = namaEvent.value.trim()
        if (title.isBlank()) {
            _errorMessage.value = "Nama event harus diisi"
            return
        }
        val desc = deskripsi.value.trim()
        if (desc.isBlank()) {
            _errorMessage.value = "Deskripsi event harus diisi"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val startDt = formatToApiDatetime(waktuMulai.value)
            val endDt = formatToApiDatetime(waktuSelesai.value)
            val type = if (isOnline.value) "online" else "offline"

            val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
            val descPart = desc.toRequestBody("text/plain".toMediaTypeOrNull())
            val typePart = type.toRequestBody("text/plain".toMediaTypeOrNull())
            val startPart = startDt.toRequestBody("text/plain".toMediaTypeOrNull())
            val endPart = endDt.toRequestBody("text/plain".toMediaTypeOrNull())

            val catPart = kategori.value.trim().ifBlank { null }
                ?.let { it.toRequestBody("text/plain".toMediaTypeOrNull()) }
            val locPart = if (!isOnline.value && aplikasiTempat.value.isNotBlank())
                aplikasiTempat.value.trim().toRequestBody("text/plain".toMediaTypeOrNull()) else null
            val addrPart = if (!isOnline.value && alamat.value.isNotBlank())
                alamat.value.trim().toRequestBody("text/plain".toMediaTypeOrNull()) else null
            val platPart = if (isOnline.value && aplikasiTempat.value.isNotBlank())
                aplikasiTempat.value.trim().toRequestBody("text/plain".toMediaTypeOrNull()) else null
            val linkPart = if (isOnline.value && alamat.value.isNotBlank())
                alamat.value.trim().toRequestBody("text/plain".toMediaTypeOrNull()) else null
            val capPart = kuota.value.takeIf { it > 0 }
                ?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())

            val posterBody = posterUri.value?.let { uri ->
                val ctx = getApplication<Application>()
                val inputStream = ctx.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes() ?: return@let null
                inputStream.close()
                val fileName = "poster_${System.currentTimeMillis()}.jpg"
                val mediaType = ctx.contentResolver.getType(uri) ?: "image/jpeg"
                MultipartBody.Part.createFormData("poster", fileName, bytes.toRequestBody(mediaType.toMediaTypeOrNull()))
            }

            try {
                val response = apiService.createEvent(
                    title = titlePart,
                    categoryId = catPart,
                    description = descPart,
                    type = typePart,
                    locationName = locPart,
                    address = addrPart,
                    latitude = null,
                    longitude = null,
                    platformName = platPart,
                    link = linkPart,
                    startDatetime = startPart,
                    endDatetime = endPart,
                    capacity = capPart,
                    poster = posterBody ?: MultipartBody.Part.createFormData("poster", "")
                )
                _publishSuccess.value = true
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Gagal membuat event"
            }

            _isLoading.value = false
        }
    }

    private fun formatToApiDatetime(input: String): String {
        if (input.isBlank()) {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            return sdf.format(Date())
        }
        return input
    }

    fun resetPublishSuccess() { _publishSuccess.value = false }
    fun clearError() { _errorMessage.value = null }
}
