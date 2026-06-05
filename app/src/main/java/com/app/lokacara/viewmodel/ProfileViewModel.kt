package com.app.lokacara.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.lokacara.data.SettingsManager
import com.app.lokacara.data.UserSessionManager
import com.app.lokacara.data.remote.ApiResult
import com.app.lokacara.data.remote.ImageUrlProvider
import com.app.lokacara.data.remote.toEvent
import com.app.lokacara.model.CertificateData
import com.app.lokacara.model.Event
import com.app.lokacara.model.UserProfile
import com.app.lokacara.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val imageUrlProvider: ImageUrlProvider
) : AndroidViewModel(application) {

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

    init {
        loadUserProfile()
        loadDashboard()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            when (val result = repository.getProfile()) {
                is ApiResult.Success -> {
                    val user = result.data.user
                    _userProfile.value = UserProfile(
                        name = user.name,
                        email = user.email,
                        phone = "",
                        location = "",
                        profileImageUrl = user.avatar_url
                    )
                }
                is ApiResult.Error -> {
                    val session = userSessionManager.userSession.first()
                    _userProfile.value = UserProfile(
                        name = session.name.ifEmpty { "Pengguna" },
                        email = session.email,
                        phone = session.phone,
                        location = session.location,
                        profileImageUrl = null
                    )
                }
            }
        }
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            _isLoading.value = true

            when (val result = repository.getDashboard()) {
                is ApiResult.Success -> {
                    val dashboard = result.data

                    _myEvents.value = dashboard.hosted_events.map { it.toEvent(imageUrlProvider) }

                    _certificates.value = dashboard.certificates.map { cert ->
                        val eventTitle = cert.event_registration
                            ?.event
                            ?.title
                            ?: "Sertifikat"
                        CertificateData(
                            id = cert.id.toString(),
                            title = eventTitle,
                            date = cert.issued_at?.take(10) ?: "",
                            time = "",
                            location = "",
                            category = "",
                            imageUrl = cert.file_url
                        )
                    }
                }
                is ApiResult.Error -> {
                    // use cached data silently
                }
            }

            _isLoading.value = false
        }
    }

    fun updateProfileField(label: String, newValue: String) {
        val current = _userProfile.value
        _userProfile.value = when (label) {
            "Nama Lengkap" -> current.copy(name = newValue)
            "Email" -> current.copy(email = newValue)
            else -> current
        }
        viewModelScope.launch {
            userSessionManager.updateField(label, newValue)
        }
    }

    fun updateProfile(name: String?, email: String?) {
        viewModelScope.launch {
            val body = mutableMapOf<String, String>()
            name?.let { body["name"] = it }
            email?.let { body["email"] = it }
            if (body.isEmpty()) return@launch

            when (val result = repository.getProfile()) {
                is ApiResult.Success -> {
                    loadUserProfile()
                }
                is ApiResult.Error -> { }
            }
        }
    }

    fun saveProfilePhoto(uri: Uri) {
        // TODO: upload via apiService.uploadAvatar after adding multipart in ProfileRepository
    }

    fun downloadCertificate(cert: CertificateData) {
        // Certificate download is handled by the API endpoint /events/{event}/certificate
        viewModelScope.launch {
            _certificates.value = _certificates.value.map {
                if (it.id == cert.id) it.copy(filePath = cert.imageUrl)
                else it
            }
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            userSessionManager.logout()
            settingsManager.clearAuthSession()
            onComplete()
        }
    }
}
