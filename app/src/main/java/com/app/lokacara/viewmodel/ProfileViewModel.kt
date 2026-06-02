package com.app.lokacara.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.lokacara.R
import com.app.lokacara.data.FileStorageManager
import com.app.lokacara.data.UserSessionManager
import com.app.lokacara.model.CertificateData
import com.app.lokacara.model.Event
import com.app.lokacara.model.MyEventData
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
    private val fileStorageManager: FileStorageManager,
) : AndroidViewModel(application) {

    private val _userProfile = MutableStateFlow(UserProfile(name = "", email = "", phone = "", location = ""))
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private val _myEvents = MutableStateFlow<List<MyEventData>>(emptyList())
    val myEvents: StateFlow<List<MyEventData>> = _myEvents.asStateFlow()

    private val _savedEvents = MutableStateFlow<List<Event>>(emptyList())
    val savedEvents: StateFlow<List<Event>> = _savedEvents.asStateFlow()

    private val _certificates = MutableStateFlow<List<CertificateData>>(emptyList())
    val certificates: StateFlow<List<CertificateData>> = _certificates.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadUserProfile()
        loadMyEvents()
        loadSavedEvents()
        loadCertificates()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            val session = userSessionManager.userSession.first()
            val profileImageRes = if (session.profileImagePath.isNotEmpty()) {
                null
            } else {
                R.drawable.profileicon
            }
            _userProfile.value = UserProfile(
                name = session.name.ifEmpty { "Daffa Arrivo" },
                email = session.email.ifEmpty { "daffarrivo@studenet.uns.ac.id" },
                phone = session.phone.ifEmpty { "+628788133233145" },
                location = session.location.ifEmpty { "Surakarta, Jawa Tengah" },
                profileImageRes = profileImageRes
            )
        }
    }

    fun updateProfileField(label: String, newValue: String) {
        val currentProfile = _userProfile.value
        _userProfile.value = when (label) {
            "Nama Lengkap" -> currentProfile.copy(name = newValue)
            "Email" -> currentProfile.copy(email = newValue)
            "Nomor" -> currentProfile.copy(phone = newValue)
            "Lokasi" -> currentProfile.copy(location = newValue)
            else -> currentProfile
        }
        viewModelScope.launch {
            userSessionManager.updateField(label, newValue)
        }
    }

    fun saveProfilePhoto(uri: Uri) {
        viewModelScope.launch {
            val path = fileStorageManager.saveProfilePhoto(uri)
            if (path != null) {
                userSessionManager.updateProfileImagePath(path)
                _userProfile.value = _userProfile.value.copy(profileImageRes = null)
            }
        }
    }

    fun downloadCertificate(cert: CertificateData) {
        viewModelScope.launch {
            val fileName = "certificate_${cert.id}_${cert.title.take(10).replace(" ", "_")}.png"
            val path = fileStorageManager.saveCertificate(cert.imageRes, fileName)
            if (path != null) {
                _certificates.value = _certificates.value.map {
                    if (it.id == cert.id) it.copy(filePath = path)
                    else it
                }
            }
        }
    }

    fun loadMyEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getMyEvents().collect { events ->
                _myEvents.value = events
            }
            _isLoading.value = false
        }
    }

    fun loadSavedEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getSavedEvents().collect { events ->
                _savedEvents.value = events
            }
            _isLoading.value = false
        }
    }

    fun loadCertificates() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getCertificates().collect { certs ->
                _certificates.value = certs
            }
            _isLoading.value = false
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            userSessionManager.logout()
            onComplete()
        }
    }
}
