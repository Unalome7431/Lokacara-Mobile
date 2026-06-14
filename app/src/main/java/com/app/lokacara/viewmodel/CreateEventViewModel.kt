package com.app.lokacara.viewmodel

import android.app.Application
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.lokacara.data.DraftManager
import com.app.lokacara.data.EventDraft
import com.app.lokacara.data.UserSessionManager
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
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class CreateEventViewModel @Inject constructor(
    application: Application,
    private val apiService: ApiService,
    private val exploreRepository: ExploreRepository,
    private val userSessionManager: UserSessionManager,
    private val draftManager: DraftManager
) : AndroidViewModel(application) {

    val eventIdToEdit = MutableStateFlow<Long?>(null)

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

    val latitude = MutableStateFlow("")
    val longitude = MutableStateFlow("")

    init {
        loadCategories()
        autoFillOrganizer()
        checkDraft()
    }

    fun loadEventForEditing(eventId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            eventIdToEdit.value = eventId
            _errorMessage.value = null

            val result = safeApiCall { apiService.getEventDetail(eventId) }
            when (result) {
                is ApiResult.Success<*> -> {
                    val detail = result.data as? com.app.lokacara.data.remote.dto.EventDetailResponse
                    val event = detail?.event ?: return@launch
                    namaEvent.value = event.title
                    deskripsi.value = event.description

                    val sessionName = userSessionManager.userSession.first().name
                    penyelenggara.value = event.user?.name ?: sessionName
                    kuota.value = event.capacity ?: 50
                    waktuMulai.value = event.start_datetime
                    waktuSelesai.value = event.end_datetime

                    selectedCategoryId.value = event.category_id

                    if (event.type == "offline") {
                        isOnline.value = false
                        latitude.value = event.latitude?.toString() ?: ""
                        longitude.value = event.longitude?.toString() ?: ""
                        alamat.value = event.address ?: ""
                        aplikasiTempat.value = event.location_name ?: ""
                    } else {
                        isOnline.value = true
                        alamat.value = event.link ?: ""
                        aplikasiTempat.value = event.platform_name ?: ""
                    }

                    if (!event.poster_url.isNullOrEmpty()) {
                        posterUri.value = Uri.parse(event.poster_url!!)
                    }
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                    SnackbarManager.showError(result.message)
                }
            }
            _isLoading.value = false
        }
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
            draftManager.saveDraft(currentDraft())
            _hasDraft.value = true
            SnackbarManager.show("Draf tersimpan")
        }
    }

    fun saveDraftAndExit(onExit: () -> Unit) {
        viewModelScope.launch {
            if (hasMeaningfulDraft()) {
                draftManager.saveDraft(currentDraft())
                _hasDraft.value = true
                SnackbarManager.show("Draf tersimpan")
            }
            onExit()
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
        val desc = deskripsi.value.trim()

        if (title.isBlank() || desc.isBlank() || selectedCategoryId.value == null || waktuMulai.value.isBlank()) {
            _errorMessage.value = "Harap lengkapi semua field yang wajib"
            return
        }

        val startDt = formatToApiDatetime(waktuMulai.value)
        val endDt = formatEndApiDatetime(waktuMulai.value, waktuSelesai.value)
        val latStr = latitude.value.trim()
        val lngStr = longitude.value.trim()

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val type = if (isOnline.value) "online" else "offline"
            val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
            val descPart = desc.toRequestBody("text/plain".toMediaTypeOrNull())
            val typePart = type.toRequestBody("text/plain".toMediaTypeOrNull())
            val startPart = startDt.toRequestBody("text/plain".toMediaTypeOrNull())
            val endPart = endDt.toRequestBody("text/plain".toMediaTypeOrNull())

            val catPart = selectedCategoryId.value?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
            val locPart = if (!isOnline.value && aplikasiTempat.value.isNotBlank()) aplikasiTempat.value.trim().toRequestBody("text/plain".toMediaTypeOrNull()) else null
            val addrPart = if (!isOnline.value && alamat.value.isNotBlank()) alamat.value.trim().toRequestBody("text/plain".toMediaTypeOrNull()) else null
            val platPart = if (isOnline.value && aplikasiTempat.value.isNotBlank()) aplikasiTempat.value.trim().toRequestBody("text/plain".toMediaTypeOrNull()) else null
            val linkPart = if (isOnline.value && alamat.value.isNotBlank()) alamat.value.trim().toRequestBody("text/plain".toMediaTypeOrNull()) else null
            val capPart = kuota.value.takeIf { it > 0 }?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())

            val latPart = if (latStr.isNotBlank()) latStr.toRequestBody("text/plain".toMediaTypeOrNull()) else null
            val lngPart = if (lngStr.isNotBlank()) lngStr.toRequestBody("text/plain".toMediaTypeOrNull()) else null

            val posterBody = try {
                posterUri.value?.let { uri ->
                    if (uri.scheme == "http" || uri.scheme == "https") {
                        null
                    } else {
                        val ctx = getApplication<Application>()
                        withContext(Dispatchers.IO) {
                            val inputStream = ctx.contentResolver.openInputStream(uri) ?: throw IllegalArgumentException("Gagal membuka poster")
                            var bytes = inputStream.use { it.readBytes() }
                            if (bytes.size > 300_000) {
                                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                val maxDim = 1600f
                                val scale = minOf(maxDim / bitmap.width, maxDim / bitmap.height, 1f)
                                val scaled = if (scale < 1f) android.graphics.Bitmap.createScaledBitmap(bitmap, (bitmap.width * scale).toInt(), (bitmap.height * scale).toInt(), true) else bitmap
                                val out = ByteArrayOutputStream()
                                scaled.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, out)
                                bytes = out.toByteArray()
                            }
                            val originalType = ctx.contentResolver.getType(uri) ?: "image/jpeg"
                            MultipartBody.Part.createFormData("poster", "poster_${System.currentTimeMillis()}.jpg", bytes.toRequestBody(originalType.toMediaTypeOrNull()))
                        }
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Poster tidak valid"
                _isLoading.value = false
                return@launch
            }

            val currentEditId = eventIdToEdit.value

            val result = safeApiCall {
                if (currentEditId != null) {
                    apiService.updateEvent(
                        eventId = currentEditId, title = titlePart, categoryId = catPart, description = descPart,
                        type = typePart, locationName = locPart, address = addrPart, latitude = latPart,
                        longitude = lngPart, platformName = platPart, link = linkPart, startDatetime = startPart,
                        endDatetime = endPart, capacity = capPart, poster = posterBody
                    )
                } else {
                    apiService.createEvent(
                        title = titlePart, categoryId = catPart, description = descPart, type = typePart,
                        locationName = locPart, address = addrPart, latitude = latPart, longitude = lngPart,
                        platformName = platPart, link = linkPart, startDatetime = startPart,
                        endDatetime = endPart, capacity = capPart, poster = posterBody
                    )
                }
            }

            when (result) {
                is ApiResult.Success -> {
                    resetForm()
                    clearDraft()
                    _publishSuccess.value = true
                    SnackbarManager.show(if (currentEditId != null) "Perubahan disimpan" else "Event diterbitkan")
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
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
            return sdf.format(Date())
        }
        return input
    }

    private fun formatEndApiDatetime(startInput: String, endInput: String): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
        if (endInput.isBlank()) {
            val cal = Calendar.getInstance().apply { time = Date(); add(Calendar.HOUR_OF_DAY, 1) }
            return sdf.format(cal.time)
        }
        return endInput
    }

    fun setLocationFromMap(location: MapLocation) {
        aplikasiTempat.value = location.name
        alamat.value = location.address
        latitude.value = location.latitude.toString()
        longitude.value = location.longitude.toString()
    }

    fun setDateTime(isStart: Boolean, date: String, time: String) {
        val formatted = "$date $time"
        if (isStart) waktuMulai.value = formatted else waktuSelesai.value = formatted
    }

    fun getDisplayDateTime(input: String): String {
        if (input.isBlank()) return ""
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            val date = sdf.parse(input) ?: return input
            SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID")).format(date)
        } catch (_: Exception) { input }
    }

    fun resetForm() {
        eventIdToEdit.value = null
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

    private fun currentDraft() = EventDraft(
        namaEvent = namaEvent.value, penyelenggara = penyelenggara.value, waktuMulai = waktuMulai.value,
        waktuSelesai = waktuSelesai.value, isOnline = isOnline.value, aplikasiTempat = aplikasiTempat.value,
        alamat = alamat.value, deskripsi = deskripsi.value, kuota = kuota.value, selectedCategoryId = selectedCategoryId.value,
        latitude = latitude.value, longitude = longitude.value, posterUriString = posterUri.value?.toString() ?: ""
    )

    private fun hasMeaningfulDraft() = namaEvent.value.isNotBlank() || waktuMulai.value.isNotBlank() || deskripsi.value.isNotBlank() || posterUri.value != null
}