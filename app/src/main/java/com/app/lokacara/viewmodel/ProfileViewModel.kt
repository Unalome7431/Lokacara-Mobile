package com.app.lokacara.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.lokacara.model.CertificateData
import com.app.lokacara.model.Event
import com.app.lokacara.model.MyEventData
import com.app.lokacara.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: ProfileRepository = ProfileRepository()
) : ViewModel() {

    private val _myEvents = MutableStateFlow<List<MyEventData>>(emptyList())
    val myEvents: StateFlow<List<MyEventData>> = _myEvents.asStateFlow()

    private val _savedEvents = MutableStateFlow<List<Event>>(emptyList())
    val savedEvents: StateFlow<List<Event>> = _savedEvents.asStateFlow()

    private val _certificates = MutableStateFlow<List<CertificateData>>(emptyList())
    val certificates: StateFlow<List<CertificateData>> = _certificates.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadMyEvents()
        loadSavedEvents()
        loadCertificates()
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
}
