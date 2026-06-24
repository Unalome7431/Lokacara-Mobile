package com.app.lokacara.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.lokacara.data.UserSessionManager
import com.app.lokacara.data.DraftManager
import com.app.lokacara.data.EventDraft
import com.app.lokacara.data.media.isRemoteUri
import com.app.lokacara.data.media.prepareMediaFromUri
import com.app.lokacara.data.media.toMultipartBodyPart
import com.app.lokacara.data.remote.ApiResult
import com.app.lokacara.data.remote.ApiService
import com.app.lokacara.data.remote.safeApiCall
import com.app.lokacara.data.remote.dto.CategoryDto
import com.app.lokacara.data.validation.Validators
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

    val eventIdToEdit = MutableStateFlow<Long?>(null)

    val namaEvent = MutableStateFlow("")
    val penyelenggara = MutableStateFlow("")
    val kontak = MutableStateFlow("")
    val waktuMulai = MutableStateFlow("")
    val waktuSelesai = MutableStateFlow("")
    val isOnline = MutableStateFlow(true)
    val isFreePrice = MutableStateFlow(true)
    val priceAmount = MutableStateFlow("")
    val aplikasiTempat = MutableStateFlow("")
    val alamat = MutableStateFlow("")
    val city = MutableStateFlow("")
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

    private val _fieldErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    val fieldErrors: StateFlow<Map<String, String>> = _fieldErrors.asStateFlow()

    private val _publishSuccess = MutableStateFlow(false)
    val publishSuccess: StateFlow<Boolean> = _publishSuccess.asStateFlow()

    private val _hasDraft = MutableStateFlow(false)
    val hasDraft: StateFlow<Boolean> = _hasDraft.asStateFlow()

    init {
        loadCategories()
        autoFillOrganizer()
        checkDraft()
    }

    fun loadEventForEditing(eventId: Long) {
        if (eventId <= 0L || eventIdToEdit.value == eventId) return

        viewModelScope.launch {
            _isLoading.value = true
            eventIdToEdit.value = eventId
            _errorMessage.value = null

            when (val result = safeApiCall { apiService.getEventDetail(eventId) }) {
                is ApiResult.Success -> {
                    val event = result.data.event
                    if (event == null) {
                        showError("Event tidak ditemukan")
                    } else {
                        namaEvent.value = event.title
                        deskripsi.value = event.description
                        val session = userSessionManager.userSession.first()
                        penyelenggara.value = event.organizer_name?.takeIf { it.isNotBlank() } ?: event.user?.name ?: session.name
                        kontak.value = event.contact?.takeIf { it.isNotBlank() }
                            ?: event.contact_phone?.takeIf { it.isNotBlank() }
                            ?: session.phone
                        kuota.value = event.capacity ?: 50
                        waktuMulai.value = event.start_datetime
                        waktuSelesai.value = event.end_datetime
                        selectedCategoryId.value = event.category_id

                        if (event.type == "offline") {
                            isOnline.value = false
                            latitude.value = event.latitude?.toString().orEmpty()
                            longitude.value = event.longitude?.toString().orEmpty()
                            alamat.value = event.address.orEmpty()
                            city.value = event.city.orEmpty()
                            aplikasiTempat.value = event.location_name.orEmpty()
                        } else {
                            isOnline.value = true
                            alamat.value = event.link.orEmpty()
                            aplikasiTempat.value = event.platform_name.orEmpty()
                            latitude.value = ""
                            longitude.value = ""
                            city.value = ""
                        }

                        val eventPrice = event.price ?: 0
                        if (eventPrice <= 0) {
                            isFreePrice.value = true
                            priceAmount.value = ""
                        } else {
                            isFreePrice.value = false
                            priceAmount.value = eventPrice.toString()
                        }

                        event.poster_url?.takeIf { it.isNotBlank() }?.let {
                            posterUri.value = Uri.parse(it)
                        }
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
            kontak.value = draft.kontak
            waktuMulai.value = draft.waktuMulai
            waktuSelesai.value = draft.waktuSelesai
            isOnline.value = draft.isOnline
            isFreePrice.value = draft.isFreePrice
            priceAmount.value = draft.priceAmount
            aplikasiTempat.value = draft.aplikasiTempat
            alamat.value = draft.alamat
            city.value = draft.city
            deskripsi.value = draft.deskripsi
            kuota.value = draft.kuota
            selectedCategoryId.value = draft.selectedCategoryId
            latitude.value = draft.latitude
            longitude.value = draft.longitude
            // Poster URI excluded from draft restore — content:// URIs expire
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
            if (session.phone.isNotBlank()) {
                kontak.value = session.phone
            }
        }
    }

    fun publish() {
        val eventIsOnline = isOnline.value
        val title = namaEvent.value.trim()
        val desc = deskripsi.value.trim()
        val organizerName = penyelenggara.value.trim()
        val contactText = kontak.value.trim()
        val venueOrPlatform = aplikasiTempat.value.trim()
        val addressOrLink = alamat.value.trim()
        val cityName = city.value.trim()
        val latStr = latitude.value.trim()
        val lngStr = longitude.value.trim()

        val errors = mutableMapOf<String, String>()
        if (title.isBlank()) errors["title"] = "Nama event harus diisi"
        if (title.length > 255) errors["title"] = "Nama event maksimal 255 karakter"
        if (organizerName.isBlank()) errors["organizer_name"] = "Penyelenggara harus diisi"
        if (contactText.isBlank()) errors["contact"] = "Kontak harus diisi"
        if (desc.isBlank()) errors["description"] = "Deskripsi event harus diisi"
        if (desc.length > 5000) errors["description"] = "Deskripsi event maksimal 5000 karakter"
        if (selectedCategoryId.value == null) errors["category"] = "Kategori event harus dipilih"
        if (waktuMulai.value.isBlank()) {
            errors["date"] = "Tanggal event harus diisi"
            errors["start_time"] = "Waktu mulai harus diisi"
        }
        if (waktuSelesai.value.isBlank()) errors["end_time"] = "Waktu selesai harus diisi"
        if (kuota.value <= 0 || kuota.value > 100_000) errors["capacity"] = "Kuota peserta harus di antara 1 sampai 100000"
        val priceValue = resolvePriceValue()
        if (priceValue == null) errors["price"] = "Harga event tidak valid"
        if (eventIsOnline) {
            if (venueOrPlatform.isBlank()) errors["platform"] = "Aplikasi/platform harus diisi"
            if (addressOrLink.isBlank()) errors["link"] = "Link event harus diisi"
        }
        if (latStr.isNotBlank() && lngStr.isBlank() || latStr.isBlank() && lngStr.isNotBlank()) {
            errors["location"] = "Latitude dan longitude harus diisi keduanya"
        }
        if (!eventIsOnline && (latStr.isBlank() || lngStr.isBlank())) {
            errors["location"] = "Pilih lokasi dari peta atau gunakan lokasi saat ini"
        }
        if (errors.isNotEmpty()) {
            val message = errors.values.first()
            _fieldErrors.value = errors
            showError(message)
            return
        }
        _fieldErrors.value = emptyMap()
        _errorMessage.value = null

        val startDateStr = waktuMulai.value.take(10)
        val startTimeStr = if (waktuMulai.value.length >= 16) waktuMulai.value.substring(11, 16) else ""
        val endTimeStr = if (waktuSelesai.value.length >= 16) waktuSelesai.value.substring(11, 16) else ""
        val pricePart = priceValue.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        if (waktuMulai.value.isNotBlank() && waktuSelesai.value.isNotBlank()) {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            try {
                val startMillis = sdf.parse(waktuMulai.value)?.time ?: 0L
                val endMillis = sdf.parse(waktuSelesai.value)?.time ?: 0L
                if (endMillis <= startMillis) {
                    _fieldErrors.value = mapOf("end_time" to "Waktu selesai harus setelah waktu mulai")
                    showError("Waktu selesai harus setelah waktu mulai")
                    return
                }
            } catch (_: Exception) {
                _fieldErrors.value = mapOf("start_time" to "Format tanggal tidak valid")
                showError("Format tanggal tidak valid")
                _isLoading.value = false
                return
            }
        }

        val offlineLocationName = if (!eventIsOnline) {
            venueOrPlatform.ifBlank { "Lokasi Event" }
        } else ""
        val offlineAddress = if (!eventIsOnline) {
            addressOrLink.ifBlank { coordinateFallbackAddress(latStr, lngStr) }
        } else ""
        if (!eventIsOnline && (offlineLocationName.isBlank() || offlineAddress.isBlank())) {
            _fieldErrors.value = mapOf("location" to "Detail lokasi offline belum lengkap")
            showError("Detail lokasi offline belum lengkap")
            return
        }
        if (!eventIsOnline) {
            aplikasiTempat.value = offlineLocationName
            alamat.value = offlineAddress
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val type = if (eventIsOnline) "online" else "offline"

            val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
            val descPart = desc.toRequestBody("text/plain".toMediaTypeOrNull())
            val typePart = type.toRequestBody("text/plain".toMediaTypeOrNull())
            val organizerPart = organizerName.toRequestBody("text/plain".toMediaTypeOrNull())
            val contactPart = contactText.toRequestBody("text/plain".toMediaTypeOrNull())
            val startDatePart = startDateStr.toRequestBody("text/plain".toMediaTypeOrNull())
            val startTimePart = startTimeStr.toRequestBody("text/plain".toMediaTypeOrNull())
            val endTimePart = endTimeStr.toRequestBody("text/plain".toMediaTypeOrNull())

            val catPart = selectedCategoryId.value?.toString()
                ?.toRequestBody("text/plain".toMediaTypeOrNull())
            val locPart = if (!eventIsOnline)
                offlineLocationName.toRequestBody("text/plain".toMediaTypeOrNull()) else null
            val addrPart = if (!eventIsOnline)
                offlineAddress.toRequestBody("text/plain".toMediaTypeOrNull()) else null
            val cityPart = if (!eventIsOnline)
                cityName.toRequestBody("text/plain".toMediaTypeOrNull()) else null
            val platPart = if (eventIsOnline)
                venueOrPlatform.toRequestBody("text/plain".toMediaTypeOrNull()) else null
            val linkPart = if (eventIsOnline)
                addressOrLink.toRequestBody("text/plain".toMediaTypeOrNull()) else null
            val capPart = kuota.value.takeIf { it > 0 }
                ?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())

            val latPart = if (latStr.isNotBlank())
                latStr.toRequestBody("text/plain".toMediaTypeOrNull()) else null
            val lngPart = if (lngStr.isNotBlank())
                lngStr.toRequestBody("text/plain".toMediaTypeOrNull()) else null

            val posterBody = posterUri.value?.let { uri ->
                if (isRemoteUri(uri)) {
                    null
                } else {
                    try {
                        val prepared = prepareMediaFromUri(
                            context = getApplication(),
                            uri = uri,
                            formDataName = "poster"
                        )
                        if (prepared.isFailure) {
                            showError(prepared.exceptionOrNull()?.message ?: "Poster tidak valid")
                            _isLoading.value = false
                            return@launch
                        }
                        prepared.getOrThrow().toMultipartBodyPart()
                    } catch (e: IllegalArgumentException) {
                        showError(e.message ?: "Poster tidak valid")
                        _isLoading.value = false
                        return@launch
                    }
                }
            }

            val currentEditId = eventIdToEdit.value
            val result = safeApiCall {
                if (currentEditId != null) {
                    apiService.updateEvent(
                        eventId = currentEditId,
                        title = titlePart,
                        categoryId = catPart,
                        description = descPart,
                        type = typePart,
                        organizerName = organizerPart,
                        contact = contactPart,
                        locationName = locPart,
                        address = addrPart,
                        city = cityPart,
                        latitude = latPart,
                        longitude = lngPart,
                        platformName = platPart,
                        link = linkPart,
                        startDate = startDatePart,
                        startTime = startTimePart,
                        endTime = endTimePart,
                        price = pricePart,
                        capacity = capPart,
                        poster = posterBody
                    )
                } else {
                    apiService.createEvent(
                        title = titlePart,
                        categoryId = catPart,
                        description = descPart,
                        type = typePart,
                        organizerName = organizerPart,
                        contact = contactPart,
                        locationName = locPart,
                        address = addrPart,
                        city = cityPart,
                        latitude = latPart,
                        longitude = lngPart,
                        platformName = platPart,
                        link = linkPart,
                        startDate = startDatePart,
                        startTime = startTimePart,
                        endTime = endTimePart,
                        price = pricePart,
                        capacity = capPart,
                        poster = posterBody
                    )
                }
            }

            when (result) {
                is ApiResult.Success -> {
                    resetForm()
                    clearDraft()
                    _publishSuccess.value = true
                    SnackbarManager.show(if (currentEditId != null) "Perubahan disimpan" else "Event berhasil diterbitkan")
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
            sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
            return sdf.format(java.util.Date())
        }
        return input
    }

    /* Keep end time one hour after start when it has to be inferred. */
    private fun formatEndApiDatetime(startInput: String, endInput: String): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        if (startInput.isBlank() && endInput.isBlank()) {
            val cal = java.util.Calendar.getInstance()
            cal.time = java.util.Date()
            cal.add(java.util.Calendar.HOUR_OF_DAY, 1)
            return sdf.format(cal.time)
        }
        if (endInput.isBlank()) {
            if (startInput.isNotBlank()) {
                try {
                    val startDate = sdf.parse(startInput) ?: throw IllegalArgumentException()
                    val cal = java.util.Calendar.getInstance()
                    cal.time = startDate
                    cal.add(java.util.Calendar.HOUR_OF_DAY, 1)
                    return sdf.format(cal.time)
                } catch (_: Exception) {}
            }
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
        val lat = location.latitude.toString()
        val lng = location.longitude.toString()
        aplikasiTempat.value = location.name.trim().ifBlank { "Lokasi Event" }
        alamat.value = location.address.trim().ifBlank { coordinateFallbackAddress(lat, lng) }
        city.value = location.city.trim().ifBlank { city.value }
        latitude.value = lat
        longitude.value = lng
    }

    fun setEventMode(online: Boolean) {
        if (isOnline.value == online) return
        isOnline.value = online
        aplikasiTempat.value = ""
        alamat.value = ""
        city.value = ""
        latitude.value = ""
        longitude.value = ""
        _errorMessage.value = null
    }

    private fun coordinateFallbackAddress(latStr: String, lngStr: String): String {
        val lat = latStr.toDoubleOrNull()
        val lng = lngStr.toDoubleOrNull()
        return if (lat != null && lng != null) {
            String.format(Locale.US, "Koordinat %.5f, %.5f", lat, lng)
        } else {
            ""
        }
    }

    fun setDateTime(isStart: Boolean, date: String, time: String) {
        val formatted = "$date $time"
        if (isStart) {
            waktuMulai.value = formatted
            if (waktuSelesai.value.isBlank() || !isEndAfterStart(formatted, waktuSelesai.value)) {
                waktuSelesai.value = addHours(formatted, 1)
            }
        } else {
            waktuSelesai.value = formatted
        }
    }

    fun setEventDate(date: String) {
        val startTime = apiTimePart(waktuMulai.value).ifBlank { DEFAULT_START_TIME }
        val endTime = apiTimePart(waktuSelesai.value).ifBlank { DEFAULT_END_TIME }
        val start = "$date $startTime"
        val end = "$date $endTime"

        waktuMulai.value = start
        waktuSelesai.value = if (isEndAfterStart(start, end)) end else addHours(start, 1)
        clearError()
    }

    fun setEventStartTime(time: String) {
        val date = selectedEventDate().ifBlank { todayDate() }
        setDateTime(isStart = true, date = date, time = time)
        clearError()
    }

    fun setEventEndTime(time: String) {
        val date = selectedEventDate().ifBlank { todayDate() }
        if (waktuMulai.value.isBlank()) {
            waktuMulai.value = "$date $DEFAULT_START_TIME"
        }
        waktuSelesai.value = "$date $time"
        clearError()
    }

    private fun addHours(input: String, hours: Int): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        return try {
            val date = sdf.parse(input) ?: return input
            val cal = java.util.Calendar.getInstance()
            cal.time = date
            cal.add(java.util.Calendar.HOUR_OF_DAY, hours)
            sdf.format(cal.time)
        } catch (_: Exception) {
            input
        }
    }

    private fun isEndAfterStart(startInput: String, endInput: String): Boolean {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        return try {
            val start = sdf.parse(startInput)?.time ?: return false
            val end = sdf.parse(endInput)?.time ?: return false
            end > start
        } catch (_: Exception) {
            false
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

    fun getDisplayDate(input: String): String {
        if (input.isBlank()) return ""
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = sdf.parse(input.take(10)) ?: return input.take(10)
            val display = SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("id-ID"))
            display.format(date)
        } catch (_: Exception) {
            input.take(10)
        }
    }

    fun getDisplayTime(input: String): String {
        return apiTimePart(input).take(5)
    }

    fun getTimePickerHour(input: String, defaultHour: Int): Int {
        return apiTimePart(input).take(2).toIntOrNull()?.coerceIn(0, 23) ?: defaultHour
    }

    fun getTimePickerMinute(input: String, defaultMinute: Int): Int {
        return apiTimePart(input).drop(3).take(2).toIntOrNull()?.coerceIn(0, 59) ?: defaultMinute
    }

    fun resetForm() {
        eventIdToEdit.value = null
        namaEvent.value = ""
        penyelenggara.value = ""
        kontak.value = ""
        waktuMulai.value = ""
        waktuSelesai.value = ""
        isOnline.value = true
        isFreePrice.value = true
        priceAmount.value = ""
        aplikasiTempat.value = ""
        alamat.value = ""
        city.value = ""
        deskripsi.value = ""
        kuota.value = 50
        posterUri.value = null
        selectedCategoryId.value = null
        latitude.value = ""
        longitude.value = ""
    }

    fun resetPublishSuccess() { _publishSuccess.value = false }
    fun clearError() {
        _errorMessage.value = null
        _fieldErrors.value = emptyMap()
    }

    private fun showError(message: String) {
        _errorMessage.value = message
        SnackbarManager.showError(message)
    }

    private fun currentDraft(): EventDraft = EventDraft(
        namaEvent = namaEvent.value,
        penyelenggara = penyelenggara.value,
        kontak = kontak.value,
        waktuMulai = waktuMulai.value,
        waktuSelesai = waktuSelesai.value,
        isOnline = isOnline.value,
        isFreePrice = isFreePrice.value,
        priceAmount = priceAmount.value,
        aplikasiTempat = aplikasiTempat.value,
        alamat = alamat.value,
        city = city.value,
        deskripsi = deskripsi.value,
        kuota = kuota.value,
        selectedCategoryId = selectedCategoryId.value,
        latitude = latitude.value,
        longitude = longitude.value,
        posterUriString = posterUri.value?.toString() ?: ""
    )

    private fun hasMeaningfulDraft(): Boolean {
        return namaEvent.value.isNotBlank() ||
            penyelenggara.value.isNotBlank() ||
            kontak.value.isNotBlank() ||
            waktuMulai.value.isNotBlank() ||
            waktuSelesai.value.isNotBlank() ||
            !isOnline.value ||
            aplikasiTempat.value.isNotBlank() ||
            alamat.value.isNotBlank() ||
            city.value.isNotBlank() ||
            deskripsi.value.isNotBlank() ||
            kuota.value != 50 ||
            !isFreePrice.value ||
            priceAmount.value.isNotBlank() ||
            selectedCategoryId.value != null ||
            latitude.value.isNotBlank() ||
            longitude.value.isNotBlank() ||
            posterUri.value != null
    }

    fun setPriceMode(isFree: Boolean) {
        isFreePrice.value = isFree
        _errorMessage.value = null
    }

    fun updatePriceAmount(rawValue: String) {
        priceAmount.value = rawValue.filter(Char::isDigit).take(9)
    }

    private fun resolvePriceValue(): Int? {
        return if (isFreePrice.value) {
            0
        } else {
            val digits = priceAmount.value.filter(Char::isDigit)
            if (digits.isBlank()) null else digits.toIntOrNull()?.takeIf { it >= 1 }
        }
    }

    private fun selectedEventDate(): String = waktuMulai.value.take(10).takeIf { it.length == 10 } ?: ""

    private fun apiTimePart(input: String): String {
        return if (input.length >= 19) input.substring(11, 19) else ""
    }

    private fun todayDate(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(java.util.Date())
    }

    private companion object {
        const val DEFAULT_START_TIME = "12:00:00"
        const val DEFAULT_END_TIME = "13:00:00"
    }
}
