package com.app.lokacara.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.lokacara.data.UserSessionManager
import com.app.lokacara.data.DraftManager
import com.app.lokacara.data.EventDraft
import com.app.lokacara.data.remote.ApiResult
import com.app.lokacara.data.remote.ApiService
import com.app.lokacara.data.remote.dto.CategoryDto
import com.app.lokacara.data.remote.safeApiCall
import com.app.lokacara.repository.ExploreRepository
import com.app.lokacara.ui.components.MapLocation
import com.app.lokacara.ui.components.SnackbarManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class CreateEventViewModel @Inject constructor(
    application: Application,
    private val apiService: ApiService,
    private val exploreRepository: ExploreRepository,
    private val userSessionManager: UserSessionManager,
    private val draftManager: DraftManager
) : AndroidViewModel(application) {

    val namaEvent = MutableStateFlow("")
    val penyelenggara = MutableStateFlow("")
    val waktuMulai = MutableStateFlow("")
    val waktuSelesai = MutableStateFlow("")
    val isOnline = MutableStateFlow(true)
    val aplikasiTempat = MutableStateFlow("")
    val alamat = MutableStateFlow("")
    val deskripsi = MutableStateFlow("")
    val kuota = MutableStateFlow(50)
    val posterUri = MutableStateFlow<Uri?>(null)

    val selectedCategoryId = MutableStateFlow<Int?>(null)

    private val _categories = MutableStateFlow<List<CategoryDto>>(emptyList())
    val categories: StateFlow<List<CategoryDto>> = _categories.asStateFlow()

    val selectedCategoryName: StateFlow<String> = selectedCategoryId.map { id ->
        _categories.value.find { it.id == id }?.name ?: ""
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _publishSuccess = MutableStateFlow(false)
    val publishSuccess: StateFlow<Boolean> = _publishSuccess.asStateFlow()

    private val _hasDraft = MutableStateFlow(false)
    val hasDraft: StateFlow<Boolean> = _hasDraft.asStateFlow()

    init {
        loadCategories()
        autoFillOrganizer()
        checkDraft()
    }

    private fun checkDraft() {
        viewModelScope.launch {
            draftManager.hasDraft.collect { _hasDraft.value = it }
        }
    }

    fun loadDraft() {
        viewModelScope.launch {
            val draft = draftManager.loadDraft() ?: return@launch
            namaEvent.value = draft.namaEvent
            penyelenggara.value = draft.penyelenggara
            waktuMulai.value = draft.waktuMulai
            waktuSelesai.value = draft.waktuSelesai
            isOnline.value = draft.isOnline
            aplikasiTempat.value = draft.aplikasiTempat
            alamat.value = draft.alamat
            deskripsi.value = draft.deskripsi
            kuota.value = draft.kuota
            selectedCategoryId.value = draft.selectedCategoryId
            latitude.value = draft.latitude
            longitude.value = draft.longitude
            SnackbarManager.show("Draf dimuat")
        }
    }

    fun saveDraft() {
        viewModelScope.launch {
            draftManager.saveDraft(
                EventDraft(
                    namaEvent = namaEvent.value,
                    penyelenggara = penyelenggara.value,
                    waktuMulai = waktuMulai.value,
                    waktuSelesai = waktuSelesai.value,
                    isOnline = isOnline.value,
                    aplikasiTempat = aplikasiTempat.value,
                    alamat = alamat.value,
                    deskripsi = deskripsi.value,
                    kuota = kuota.value,
                    selectedCategoryId = selectedCategoryId.value,
                    latitude = latitude.value,
                    longitude = longitude.value
                )
            )
            _hasDraft.value = true
            SnackbarManager.show("Draf tersimpan")
        }
    }

    fun clearDraft() {
        viewModelScope.launch {
            draftManager.deleteDraft()
            _hasDraft.value = false
        }
    }

    fun deleteDraft() {
        viewModelScope.launch {
            draftManager.deleteDraft()
            _hasDraft.value = false
            resetForm()
            SnackbarManager.show("Draf dihapus")
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            when (val result = exploreRepository.getCategories()) {
                is ApiResult.Success -> _categories.value = result.data
                else -> {}
            }
        }
    }

    private fun autoFillOrganizer() {
        viewModelScope.launch {
            val session = userSessionManager.userSession.first()
            if (session.name.isNotBlank()) {
                penyelenggara.value = session.name
            }
        }
    }

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

        val startDt = formatToApiDatetime(waktuMulai.value)
        val endDt = formatEndApiDatetime(waktuMulai.value, waktuSelesai.value)

        if (waktuMulai.value.isNotBlank() && waktuSelesai.value.isNotBlank()) {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            try {
                val startMillis = sdf.parse(startDt)?.time ?: 0L
                val endMillis = sdf.parse(endDt)?.time ?: 0L
                if (endMillis <= startMillis) {
                    _errorMessage.value = "Waktu selesai harus setelah waktu mulai"
                    return
                }
            } catch (_: Exception) {}
        }

        val latStr = latitude.value.trim()
        val lngStr = longitude.value.trim()
        if (latStr.isNotBlank() && lngStr.isBlank() || latStr.isBlank() && lngStr.isNotBlank()) {
            _errorMessage.value = "Latitude dan longitude harus diisi keduanya atau dikosongkan"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val type = if (isOnline.value) "online" else "offline"

            val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
            val descPart = desc.toRequestBody("text/plain".toMediaTypeOrNull())
            val typePart = type.toRequestBody("text/plain".toMediaTypeOrNull())
            val startPart = startDt.toRequestBody("text/plain".toMediaTypeOrNull())
            val endPart = endDt.toRequestBody("text/plain".toMediaTypeOrNull())

            val catPart = selectedCategoryId.value?.toString()
                ?.toRequestBody("text/plain".toMediaTypeOrNull())
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

            val latPart = if (latStr.isNotBlank())
                latStr.toRequestBody("text/plain".toMediaTypeOrNull()) else null
            val lngPart = if (lngStr.isNotBlank())
                lngStr.toRequestBody("text/plain".toMediaTypeOrNull()) else null

            val posterBody = posterUri.value?.let { uri ->
                val ctx = getApplication<Application>()
                withContext(Dispatchers.IO) {
                    val inputStream = ctx.contentResolver.openInputStream(uri) ?: return@withContext null
                    val bytes = inputStream.use { it.readBytes() }
                    val fileName = "poster_${System.currentTimeMillis()}.jpg"
                    val mediaType = ctx.contentResolver.getType(uri) ?: "image/jpeg"
                    MultipartBody.Part.createFormData("poster", fileName, bytes.toRequestBody(mediaType.toMediaTypeOrNull()))
                }
            }

            when (val result = safeApiCall {
                apiService.createEvent(
                    title = titlePart,
                    categoryId = catPart,
                    description = descPart,
                    type = typePart,
                    locationName = locPart,
                    address = addrPart,
                    latitude = latPart,
                    longitude = lngPart,
                    platformName = platPart,
                    link = linkPart,
                    startDatetime = startPart,
                    endDatetime = endPart,
                    capacity = capPart,
                    poster = posterBody
                )
            }) {
                is ApiResult.Success -> {
                    resetForm()
                    clearDraft()
                    _publishSuccess.value = true
                    SnackbarManager.show("Event berhasil diterbitkan")
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                    SnackbarManager.showError(result.message)
                }
            }

            _isLoading.value = false
        }
    }

    private fun formatToApiDatetime(input: String): String {
        if (input.isBlank()) {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            return sdf.format(java.util.Date())
        }
        return input
    }

    /* end start closer to +1hr if not set */
    private fun formatEndApiDatetime(startInput: String, endInput: String): String {
        if (startInput.isBlank() && endInput.isBlank()) {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            val cal = java.util.Calendar.getInstance()
            cal.time = java.util.Date()
            cal.add(java.util.Calendar.HOUR_OF_DAY, 1)
            return sdf.format(cal.time)
        }
        if (endInput.isBlank()) {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            val cal = java.util.Calendar.getInstance()
            cal.time = java.util.Date()
            cal.add(java.util.Calendar.HOUR_OF_DAY, 1)
            return sdf.format(cal.time)
        }
        return endInput
    }

    val latitude = MutableStateFlow("")
    val longitude = MutableStateFlow("")

    fun setLocationFromMap(location: MapLocation) {
        aplikasiTempat.value = location.name
        alamat.value = location.address
        latitude.value = location.latitude.toString()
        longitude.value = location.longitude.toString()
    }

    fun setDateTime(isStart: Boolean, date: String, time: String) {
        val formatted = "$date $time"
        if (isStart) {
            waktuMulai.value = formatted
        } else {
            waktuSelesai.value = formatted
        }
    }

    fun getDisplayDateTime(input: String): String {
        if (input.isBlank()) return ""
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            val date = sdf.parse(input) ?: return input
            val display = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID"))
            display.format(date)
        } catch (_: Exception) {
            input
        }
    }

    fun resetForm() {
        namaEvent.value = ""
        penyelenggara.value = ""
        waktuMulai.value = ""
        waktuSelesai.value = ""
        isOnline.value = true
        aplikasiTempat.value = ""
        alamat.value = ""
        deskripsi.value = ""
        kuota.value = 50
        posterUri.value = null
        selectedCategoryId.value = null
        latitude.value = ""
        longitude.value = ""
    }

    fun resetPublishSuccess() { _publishSuccess.value = false }
    fun clearError() { _errorMessage.value = null }
}
