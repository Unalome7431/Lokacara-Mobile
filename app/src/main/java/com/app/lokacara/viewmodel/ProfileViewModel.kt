package com.app.lokacara.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.lokacara.data.FileStorageManager
import com.app.lokacara.data.PushTokenManager
import com.app.lokacara.data.SettingsManager
import com.app.lokacara.data.UserSessionManager
import com.app.lokacara.data.mapDashboardCertificates
import com.app.lokacara.data.remote.ApiResult
import com.app.lokacara.data.remote.BoundedImagePrefetcher
import com.app.lokacara.data.validation.Validators
import com.app.lokacara.data.validation.isValidEmail
import com.app.lokacara.data.validation.isDisplayableEmail
import com.app.lokacara.data.validation.toDisplayEmail
import com.app.lokacara.data.remote.ImageUrlProvider
import com.app.lokacara.data.remote.toEvent
import com.app.lokacara.model.CertificateData
import com.app.lokacara.model.Event
import com.app.lokacara.model.UserProfile
import com.app.lokacara.repository.ProfileRepository
import com.app.lokacara.repository.CertificateRepository
import com.app.lokacara.ui.components.SnackbarManager
import coil.ImageLoader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    application: Application,
    private val repository: ProfileRepository,
    private val certificateRepository: CertificateRepository,
    private val userSessionManager: UserSessionManager,
    private val settingsManager: SettingsManager,
    private val imageUrlProvider: ImageUrlProvider,
    private val fileStorageManager: FileStorageManager,
    private val pushTokenManager: PushTokenManager,
    private val imageLoader: ImageLoader
) : AndroidViewModel(application) {

    private val imagePrefetcher = BoundedImagePrefetcher(
        context = application,
        imageLoader = imageLoader,
        maxRequests = 6
    )

    private val _userProfile = MutableStateFlow(UserProfile(name = "", email = "", phone = "", location = ""))
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private var profileVersion = 0L

    private val _myEvents = MutableStateFlow<List<Event>>(emptyList())
    val myEvents: StateFlow<List<Event>> = _myEvents.asStateFlow()

    private val _savedEvents = MutableStateFlow<List<Event>>(emptyList())
    val savedEvents: StateFlow<List<Event>> = _savedEvents.asStateFlow()

    private val _certificates = MutableStateFlow<List<CertificateData>>(emptyList())
    val certificates: StateFlow<List<CertificateData>> = _certificates.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _cancellingEventId = MutableStateFlow<Long?>(null)
    val cancellingEventId: StateFlow<Long?> = _cancellingEventId.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        observeProfileImagePath()
        observeDashboard()
        loadUserProfile()
        loadDashboard()
    }

    private fun observeProfileImagePath() {
        viewModelScope.launch {
            userSessionManager.userSession.collect { session ->
                val path = session.profileImagePath.takeIf { it.isNotBlank() } ?: return@collect
                profileVersion = System.currentTimeMillis()
                _userProfile.value = _userProfile.value.copy(
                    profileImageUrl = withLocalAvatarVersion(path)
                )
            }
        }
    }

    private fun observeDashboard() {
        viewModelScope.launch {
            repository.dashboard.collect { dashboard ->
                if (dashboard != null) applyDashboard(dashboard)
            }
        }
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                when (val result = repository.getProfile()) {
                    is ApiResult.Success -> {
                        val user = result.data.user
                        if (user != null) {
                            val session = userSessionManager.userSession.first()
                            val email = when {
                                user.email.isDisplayableEmail() -> user.email.trim()
                                session.email.isDisplayableEmail() -> session.email.trim()
                                else -> ""
                            }
                            _userProfile.value = UserProfile(
                                name = user.name,
                                email = email,
                                phone = user.phone ?: "",
                                location = user.location ?: "",
                                profileImageUrl = resolveProfileImageUrl(
                                    remoteAvatar = user.avatar_url,
                                    updatedAt = user.updated_at,
                                    localFallback = resolveLocalProfileImagePath()
                                )
                            )
                        } else {
                            loadFallbackProfile()
                        }
                    }
                    is ApiResult.Error -> {
                        loadFallbackProfile()
                    }
                }
            } catch (e: Exception) {
                loadFallbackProfile()
            }
        }
    }

    private suspend fun loadFallbackProfile() {
        val session = userSessionManager.userSession.first()
        val localProfileImage = session.profileImagePath.ifBlank { resolveLocalProfileImagePath().orEmpty() }
        _userProfile.value = UserProfile(
            name = session.name.ifEmpty { "Pengguna" },
            email = session.email.toDisplayEmail(),
            phone = session.phone,
            location = session.location,
            profileImageUrl = localProfileImage
                .takeIf { it.isNotBlank() }
                ?.let(::withLocalAvatarVersion)
        )
    }

    fun refresh() {
        profileVersion = System.currentTimeMillis()
        loadUserProfile()
        loadDashboard(forceRefresh = true)
    }

    fun refreshDashboard() = loadDashboard(forceRefresh = true)

    fun cancelMyEvent(eventId: Long) {
        if (_cancellingEventId.value != null) return

        viewModelScope.launch {
            _cancellingEventId.value = eventId
            _errorMessage.value = null

            when (val result = repository.cancelEvent(eventId)) {
                is ApiResult.Success -> {
                    SnackbarManager.show(result.data.message.ifBlank { "Event berhasil dibatalkan" })
                    loadDashboard(forceRefresh = true)
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                    SnackbarManager.showError(result.message)
                }
            }

            _cancellingEventId.value = null
        }
    }

    private fun resolveLocalProfileImagePath(): String? {
        return fileStorageManager.getProfilePhoto()?.absolutePath
    }

    private fun resolveProfileImageUrl(
        remoteAvatar: String?,
        updatedAt: String? = null,
        localFallback: String? = null
    ): String? {
        val local = localFallback?.takeIf { it.isNotBlank() }
        val localVersioned = local?.let(::withLocalAvatarVersion)
        val remote = remoteAvatar
            ?.takeIf { it.isNotBlank() }
            ?.let { imageUrlProvider.avatarUrl(it) }
            ?.withAvatarCacheBuster(updatedAt)
            ?.withAvatarCacheBuster(profileVersion.toString())
        return localVersioned ?: remote
    }

    private fun withLocalAvatarVersion(path: String): String {
        val file = File(path)
        val version = maxOf(file.lastModified(), profileVersion).takeIf { it > 0L }
            ?: System.currentTimeMillis()
        return "$path?v=$version"
    }

    private fun String.withAvatarCacheBuster(version: String?): String {
        val safeVersion = version?.takeIf { it.isNotBlank() } ?: System.currentTimeMillis().toString()
        val separator = if (contains("?")) "&" else "?"
        return "$this${separator}v=$safeVersion"
    }

    private fun loadDashboard(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                when (val result = repository.getDashboard(forceRefresh)) {
                    is ApiResult.Success -> Unit
                    is ApiResult.Error -> { }
                }
            } catch (_: Exception) { }
            _isLoading.value = false
        }
    }

    private fun prefetchProfileImages(events: List<Event>, certificates: List<CertificateData>) {
        val urls = buildList {
            addAll(events.mapNotNull { it.imageUrl?.takeIf(String::isNotBlank) })
            addAll(certificates.mapNotNull { it.imageUrl?.takeIf(String::isNotBlank) })
        }
            .distinct()
            .take(8)

        imagePrefetcher.replace(urls, sizePx = 700)
    }

    fun updateProfileField(field: UserSessionManager.Field, newValue: String) {
        if (_isLoading.value) return
        val current = _userProfile.value
        val trimmedValue = newValue.trim()
        if (field == UserSessionManager.Field.NAME && trimmedValue.isBlank()) {
            _errorMessage.value = "Nama tidak boleh kosong"
            SnackbarManager.showError("Nama tidak boleh kosong")
            return
        }
        if (field == UserSessionManager.Field.EMAIL && !trimmedValue.isValidEmail()) {
            _errorMessage.value = "Format email tidak valid"
            SnackbarManager.showError("Format email tidak valid")
            return
        }

        val updatedProfile = when (field) {
            UserSessionManager.Field.NAME -> current.copy(name = trimmedValue)
            UserSessionManager.Field.EMAIL -> current.copy(email = trimmedValue)
            UserSessionManager.Field.PHONE -> current.copy(phone = trimmedValue)
            UserSessionManager.Field.LOCATION -> current.copy(location = trimmedValue)
        }
        viewModelScope.launch {
            _isLoading.value = true
            updateProfile(updatedProfile, field)
            _isLoading.value = false
        }
    }

    private suspend fun updateProfile(profile: UserProfile, updatedField: UserSessionManager.Field) {
        try {
            val session = userSessionManager.userSession.first()
            val resolvedEmail = profile.email.ifBlank { session.email }.trim()
            if (resolvedEmail.isBlank() || !resolvedEmail.isValidEmail()) {
                _errorMessage.value = "Email tidak valid"
                SnackbarManager.showError("Email tidak valid")
                return
            }
            val body = mutableMapOf(
                "name" to profile.name,
                "email" to resolvedEmail
            )
            if (profile.phone.isNotBlank()) body["phone"] = profile.phone
            if (profile.location.isNotBlank()) body["location"] = profile.location

            when (val result = repository.updateProfile(body)) {
                is ApiResult.Success -> {
                    userSessionManager.updateField(
                        field = updatedField,
                        value = when (updatedField) {
                            UserSessionManager.Field.NAME -> profile.name
                            UserSessionManager.Field.EMAIL -> resolvedEmail
                            UserSessionManager.Field.PHONE -> profile.phone
                            UserSessionManager.Field.LOCATION -> profile.location
                        }
                    )
                    _userProfile.value = profile.copy(email = resolvedEmail.toDisplayEmail())
                    loadUserProfile()
                    SnackbarManager.show("Profil berhasil diperbarui")
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                    SnackbarManager.showError(result.message)
                }
            }
        } catch (_: Exception) {
            _errorMessage.value = "Gagal memperbarui profil"
            SnackbarManager.showError("Gagal memperbarui profil")
        }
    }

    fun saveProfilePhoto(uri: Uri) {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            val context = getApplication<Application>()
            when (val result = repository.uploadAvatar(context, uri)) {
                is ApiResult.Success -> {
                    profileVersion = System.currentTimeMillis()
                    val localPath = fileStorageManager.saveProfilePhoto(uri)
                    localPath?.let { path ->
                        userSessionManager.updateProfileImagePath(path)
                    }
                    val user = result.data.user
                    val displayImage = localPath?.let(::withLocalAvatarVersion) ?: resolveProfileImageUrl(
                        remoteAvatar = user?.avatar_url,
                        updatedAt = user?.updated_at ?: System.currentTimeMillis().toString()
                    )
                    _userProfile.value = _userProfile.value.copy(
                        profileImageUrl = displayImage
                    )
                    SnackbarManager.show("Foto profil diperbarui")
                }
                is ApiResult.Error -> {
                    val message = if (result.code == 413) {
                        "Ukuran foto terlalu besar. Coba pilih foto yang lebih kecil."
                    } else {
                        result.message
                    }
                    _errorMessage.value = message
                    SnackbarManager.showError(message)
                }
            }
            _isLoading.value = false
        }
    }

    fun downloadCertificate(cert: CertificateData) {
        if (cert.eventId == 0L || cert.isDownloading) return
        viewModelScope.launch {
            updateCertificate(cert.id) { copy(isDownloading = true, errorMessage = null) }
            when (val result = certificateRepository.saveParticipantCertificate(
                cert.eventId,
                cert.id,
                cert.title
            )) {
                is ApiResult.Success -> {
                    updateCertificate(cert.id) { copy(isDownloading = false, filePath = result.data) }
                    SnackbarManager.show("Sertifikat berhasil diunduh")
                }
                is ApiResult.Error -> {
                    updateCertificate(cert.id) { copy(isDownloading = false, errorMessage = result.message) }
                    SnackbarManager.showError(result.message)
                }
            }
        }
    }

    private suspend fun applyDashboard(dashboard: com.app.lokacara.data.remote.dto.DashboardResponse) {
        val (events, certificates) = withContext(Dispatchers.Default) {
            dashboard.hosted_events.map { it.toEvent(imageUrlProvider) } to mapDashboardCertificates(dashboard)
        }
        _myEvents.value = events
        _certificates.value = certificates
        certificates.forEach(::loadCertificatePreview)
        prefetchProfileImages(events, certificates)
    }

    fun loadCertificatePreview(cert: CertificateData, forceRefresh: Boolean = false) {
        if (cert.eventId == 0L || cert.isPreviewLoading || (!forceRefresh && cert.imageUrl != null)) return
        viewModelScope.launch {
            updateCertificate(cert.id) { copy(isPreviewLoading = true, errorMessage = null) }
            when (val result = certificateRepository.cacheParticipantCertificate(cert.eventId, cert.id, forceRefresh)) {
                is ApiResult.Success -> updateCertificate(cert.id) {
                    copy(imageUrl = result.data, isPreviewLoading = false, errorMessage = null)
                }
                is ApiResult.Error -> updateCertificate(cert.id) {
                    SnackbarManager.showError(result.message)
                    copy(isPreviewLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    private fun updateCertificate(id: String, transform: CertificateData.() -> CertificateData) {
        _certificates.value = _certificates.value.map { if (it.id == id) it.transform() else it }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            pushTokenManager.unregisterLastSyncedToken()
            userSessionManager.logout()
            onComplete()
        }
    }

    override fun onCleared() {
        imagePrefetcher.clear()
        super.onCleared()
    }
}
