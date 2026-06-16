package com.app.lokacara.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.lokacara.data.FileStorageManager
import com.app.lokacara.data.PushTokenManager
import com.app.lokacara.data.SettingsManager
import com.app.lokacara.data.UserSessionManager
import com.app.lokacara.data.remote.ApiResult
import com.app.lokacara.data.remote.ImageUrlProvider
import com.app.lokacara.data.remote.toEvent
import com.app.lokacara.model.CertificateData
import com.app.lokacara.model.Event
import com.app.lokacara.model.UserProfile
import com.app.lokacara.repository.ProfileRepository
import com.app.lokacara.ui.components.SnackbarManager
import coil.ImageLoader
import coil.request.ImageRequest
import coil.size.Precision
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    application: Application,
    private val repository: ProfileRepository,
    private val userSessionManager: UserSessionManager,
    private val settingsManager: SettingsManager,
    private val imageUrlProvider: ImageUrlProvider,
    private val fileStorageManager: FileStorageManager,
    private val pushTokenManager: PushTokenManager,
    private val imageLoader: ImageLoader
) : AndroidViewModel(application) {

    private val prefetchedImageUrls = mutableSetOf<String>()

    private val _userProfile = MutableStateFlow(UserProfile(name = "", email = "", phone = "", location = ""))
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _myEvents = MutableStateFlow<List<Event>>(emptyList())
    val myEvents: StateFlow<List<Event>> = _myEvents.asStateFlow()

    private val _savedEvents = MutableStateFlow<List<Event>>(emptyList())
    val savedEvents: StateFlow<List<Event>> = _savedEvents.asStateFlow()

    private val _certificates = MutableStateFlow<List<CertificateData>>(emptyList())
    val certificates: StateFlow<List<CertificateData>> = _certificates.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadUserProfile()
        loadDashboard()
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
            profileImageUrl = localProfileImage.ifBlank { null }
        )
    }

    fun refresh() {
        loadUserProfile()
        loadDashboard()
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
        val remote = remoteAvatar
            ?.takeIf { it.isNotBlank() }
            ?.let { imageUrlProvider.avatarUrl(it) }
            ?.withAvatarCacheBuster(updatedAt)
        return remote ?: local
    }

    private fun String.withAvatarCacheBuster(version: String?): String {
        val safeVersion = version?.takeIf { it.isNotBlank() } ?: System.currentTimeMillis().toString()
        val separator = if (contains("?")) "&" else "?"
        return "$this${separator}v=$safeVersion"
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                when (val result = repository.getDashboard()) {
                    is ApiResult.Success -> {
                        val dashboard = result.data
                        val events = dashboard.hosted_events.map { it.toEvent(imageUrlProvider) }
                        val certificates = dashboard.certificates.map { cert ->
                            val eventTitle = cert.event_registration?.event?.title ?: "Sertifikat"
                            CertificateData(
                                id = cert.id.toString(),
                                title = eventTitle,
                                date = cert.issued_at?.take(10) ?: "",
                                time = "",
                                location = "",
                                category = "",
                                imageUrl = imageUrlProvider.certificateUrl(cert.file_url)
                            )
                        }
                        _myEvents.value = events
                        _certificates.value = certificates
                        prefetchProfileImages(events, certificates)
                    }
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

        if (urls.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            urls.forEach { imageUrl ->
                val shouldPrefetch = synchronized(prefetchedImageUrls) {
                    prefetchedImageUrls.add(imageUrl)
                }
                if (!shouldPrefetch) return@forEach

                imageLoader.enqueue(
                    ImageRequest.Builder(context)
                        .data(imageUrl)
                        .size(700)
                        .precision(Precision.INEXACT)
                        .crossfade(false)
                        .build()
                )
            }
        }
    }

    fun updateProfileField(field: UserSessionManager.Field, newValue: String) {
        val current = _userProfile.value
        val trimmedValue = newValue.trim()
        if (field == UserSessionManager.Field.NAME && trimmedValue.isBlank()) {
            _errorMessage.value = "Nama tidak boleh kosong"
            SnackbarManager.showError("Nama tidak boleh kosong")
            return
        }
        if (field == UserSessionManager.Field.EMAIL && !isValidEmail(trimmedValue)) {
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
            updateProfile(updatedProfile, field)
        }
    }

    private suspend fun updateProfile(profile: UserProfile, updatedField: UserSessionManager.Field) {
        try {
            val session = userSessionManager.userSession.first()
            val resolvedEmail = profile.email.ifBlank { session.email }.trim()
            if (resolvedEmail.isBlank() || !isValidEmail(resolvedEmail)) {
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

    private fun isValidEmail(value: String): Boolean {
        return "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$".toRegex().matches(value)
    }

    private fun String.isSyntheticEmail(): Boolean {
        return trim().endsWith("@placeholder.local", ignoreCase = true)
    }

    private fun String.isDisplayableEmail(): Boolean {
        val value = trim()
        return value.isNotBlank() && !value.isSyntheticEmail()
    }

    private fun String.toDisplayEmail(): String {
        return if (isDisplayableEmail()) trim() else ""
    }

    fun saveProfilePhoto(uri: Uri) {
        viewModelScope.launch {
            _errorMessage.value = null
            val context = getApplication<Application>()
            when (val result = repository.uploadAvatar(context, uri)) {
                is ApiResult.Success -> {
                    val localPath = fileStorageManager.saveProfilePhoto(uri)
                    localPath?.let { path ->
                        userSessionManager.updateProfileImagePath(path)
                    }
                    val user = result.data.user
                    val displayImage = localPath ?: resolveProfileImageUrl(
                        remoteAvatar = user?.avatar_url,
                        updatedAt = user?.updated_at ?: System.currentTimeMillis().toString()
                    )
                    _userProfile.value = _userProfile.value.copy(
                        profileImageUrl = displayImage
                    )
                    SnackbarManager.show("Foto profil diperbarui")
                }
                is ApiResult.Error -> {
                    _errorMessage.value = result.message
                    SnackbarManager.showError(result.message)
                }
            }
        }
    }

    fun downloadCertificate(cert: CertificateData) {
        viewModelScope.launch {
            _certificates.value = _certificates.value.map {
                if (it.id == cert.id) it.copy(filePath = cert.imageUrl)
                else it
            }
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            pushTokenManager.unregisterLastSyncedToken()
            userSessionManager.logout()
            onComplete()
        }
    }
}
