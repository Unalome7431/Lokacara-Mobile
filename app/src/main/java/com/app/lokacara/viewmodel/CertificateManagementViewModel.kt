package com.app.lokacara.viewmodel

import android.app.Application
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.lokacara.data.remote.ApiResult
import com.app.lokacara.data.remote.dto.CertificateLayoutConfig
import com.app.lokacara.repository.CertificateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

data class CertificateManagementUiState(
    val eventTitle: String = "",
    val selectedUri: Uri? = null,
    val selectedFileName: String = "",
    val selectedMimeType: String = "",
    val selectedFileSize: Long = 0L,
    val templatePath: String? = null,
    val fontFamily: String = "Roboto",
    val fontSize: String = "Medium",
    val fontColor: String = "#000000",
    val xPosition: Float = 50f,
    val isXCentered: Boolean = true,
    val yPosition: Float = 50f,
    val isYCentered: Boolean = true,
    val presentAttendeeCount: Int = 0,
    val isEventFinished: Boolean = false,
    val isLoadingEligibility: Boolean = true,
    val isUploading: Boolean = false,
    val isDistributing: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null
) {
    val canUpload: Boolean
        get() = selectedUri != null && !isUploading && !isDistributing

    val canDistribute: Boolean
        get() = !templatePath.isNullOrBlank() && isEventFinished &&
            presentAttendeeCount > 0 && !isLoadingEligibility && !isUploading && !isDistributing
}

@HiltViewModel
class CertificateManagementViewModel @Inject constructor(
    application: Application,
    private val repository: CertificateRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(CertificateManagementUiState())
    val uiState: StateFlow<CertificateManagementUiState> = _uiState.asStateFlow()

    private var currentEventId: Long = 0L

    fun initialize(eventId: Long) {
        if (eventId <= 0L || currentEventId == eventId) return
        currentEventId = eventId
        loadEligibility()
    }

    private fun loadEligibility() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingEligibility = true, errorMessage = null)
            when (val firstResult = repository.getAttendees(currentEventId)) {
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoadingEligibility = false,
                        errorMessage = firstResult.message
                    )
                }
                is ApiResult.Success -> {
                    val response = firstResult.data
                    var presentCount = response.attendees.data.count { it.status.equals("present", true) }
                    var loadError: String? = null
                    for (page in 2..response.attendees.last_page) {
                        when (val pageResult = repository.getAttendees(currentEventId, page)) {
                            is ApiResult.Success -> presentCount += pageResult.data.attendees.data.count {
                                it.status.equals("present", true)
                            }
                            is ApiResult.Error -> {
                                loadError = pageResult.message
                                break
                            }
                        }
                    }
                    _uiState.value = _uiState.value.copy(
                        eventTitle = response.event.title,
                        presentAttendeeCount = presentCount,
                        isEventFinished = hasEventFinished(response.event.end_datetime),
                        isLoadingEligibility = false,
                        errorMessage = loadError
                    )
                }
            }
        }
    }

    fun selectTemplate(uri: Uri?) {
        if (uri == null) return
        viewModelScope.launch(Dispatchers.IO) {
            val resolver = getApplication<Application>().contentResolver
            val mimeType = resolver.getType(uri).orEmpty().lowercase(Locale.US)
            var fileName = "template"
            var fileSize = -1L
            resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (nameIndex >= 0) fileName = cursor.getString(nameIndex) ?: fileName
                        if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) fileSize = cursor.getLong(sizeIndex)
                    }
                }
            if (fileSize < 0) {
                fileSize = resolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: -1L
            }

            val extension = fileName.substringAfterLast('.', "").lowercase(Locale.US)
            val validType = mimeType in ACCEPTED_MIME_TYPES || extension in ACCEPTED_EXTENSIONS
            val resolvedMimeType = when {
                mimeType in ACCEPTED_MIME_TYPES -> mimeType
                extension == "png" -> "image/png"
                else -> "image/jpeg"
            }
            val error = when {
                !validType -> "Gunakan file gambar berformat JPG, JPEG, atau PNG."
                fileSize < 0 -> "Ukuran file tidak dapat dibaca. Pilih file lain."
                fileSize > MAX_TEMPLATE_SIZE -> "Ukuran template maksimal 5 MB."
                else -> null
            }

            _uiState.value = if (error == null) {
                _uiState.value.copy(
                    selectedUri = uri,
                    selectedFileName = fileName,
                    selectedMimeType = resolvedMimeType,
                    selectedFileSize = fileSize,
                    templatePath = null,
                    successMessage = null,
                    errorMessage = null
                )
            } else {
                _uiState.value.copy(errorMessage = error, successMessage = null)
            }
        }
    }

    fun uploadTemplate() {
        val state = _uiState.value
        val uri = state.selectedUri ?: return showError("Pilih template sertifikat terlebih dahulu.")
        if (!state.canUpload || currentEventId == 0L) return
        viewModelScope.launch {
            _uiState.value = state.copy(isUploading = true, errorMessage = null, successMessage = null)
            when (val result = repository.uploadTemplate(
                currentEventId,
                uri,
                state.selectedFileName,
                state.selectedMimeType
            )) {
                is ApiResult.Success -> _uiState.value = _uiState.value.copy(
                    isUploading = false,
                    templatePath = result.data.template_path,
                    successMessage = "Template berhasil diunggah."
                )
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    isUploading = false,
                    errorMessage = result.message
                )
            }
        }
    }

    fun distributeCertificates() {
        val state = _uiState.value
        val templatePath = state.templatePath
            ?: return showError("Unggah template terlebih dahulu sebelum mengirim sertifikat.")
        if (!state.isEventFinished) return showError("Sertifikat hanya dapat dikirim setelah event selesai.")
        if (state.presentAttendeeCount <= 0) return showError("Belum ada peserta hadir yang dapat menerima sertifikat.")
        if (!state.canDistribute || currentEventId == 0L) return

        viewModelScope.launch {
            _uiState.value = state.copy(isDistributing = true, errorMessage = null, successMessage = null)
            val request = CertificateLayoutConfig(
                fontFamily = state.fontFamily,
                fontColor = state.fontColor,
                fontSize = state.fontSize,
                xPosition = state.xPosition,
                isXCentered = state.isXCentered,
                yPosition = state.yPosition,
                isYCentered = state.isYCentered
            ).toDistributeRequest(templatePath)
            when (val result = repository.distribute(currentEventId, request)) {
                is ApiResult.Success -> _uiState.value = _uiState.value.copy(
                    isDistributing = false,
                    successMessage = "Pembuatan dan pengiriman sertifikat telah dimulai."
                )
                is ApiResult.Error -> _uiState.value = _uiState.value.copy(
                    isDistributing = false,
                    errorMessage = result.message
                )
            }
        }
    }

    fun setFontFamily(value: String) = updateState { copy(fontFamily = value) }
    fun setFontSize(value: String) = updateState { copy(fontSize = value) }
    fun setFontColor(value: String) {
        val normalized = value.trim().uppercase(Locale.US)
        if (HEX_COLOR.matches(normalized)) updateState { copy(fontColor = normalized, errorMessage = null) }
        else showError("Gunakan kode warna heksadesimal, misalnya #000000.")
    }
    fun setXCentered(value: Boolean) = updateState { copy(isXCentered = value) }
    fun setYCentered(value: Boolean) = updateState { copy(isYCentered = value) }
    fun setXPosition(value: Float) = updateState { copy(xPosition = value.coerceIn(0f, 100f)) }
    fun setYPosition(value: Float) = updateState { copy(yPosition = value.coerceIn(0f, 100f)) }
    fun clearMessage() = updateState { copy(errorMessage = null, successMessage = null) }

    private fun showError(message: String) = updateState { copy(errorMessage = message, successMessage = null) }

    private fun updateState(block: CertificateManagementUiState.() -> CertificateManagementUiState) {
        _uiState.value = _uiState.value.block()
    }

    private fun hasEventFinished(endDatetime: String): Boolean {
        val normalized = endDatetime.replace('T', ' ').take(19)
        val formats = listOf("yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm")
        return formats.firstNotNullOfOrNull { pattern ->
            runCatching {
                SimpleDateFormat(pattern, Locale.US).apply { isLenient = false }.parse(normalized)
            }.getOrNull()
        }?.before(Date()) == true
    }

    companion object {
        private const val MAX_TEMPLATE_SIZE = 5L * 1024L * 1024L
        private val ACCEPTED_MIME_TYPES = setOf("image/jpeg", "image/jpg", "image/png")
        private val ACCEPTED_EXTENSIONS = setOf("jpg", "jpeg", "png")
        private val HEX_COLOR = Regex("^#[0-9A-F]{6}$")
    }
}
