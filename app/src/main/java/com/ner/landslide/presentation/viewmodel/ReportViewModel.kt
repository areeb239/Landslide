package com.ner.landslide.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.ner.landslide.domain.model.*
import com.ner.landslide.domain.usecase.*
import com.ner.landslide.util.LocationHelper
import com.ner.landslide.util.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class ReportUiState(
    val incidentType: IncidentType = IncidentType.LANDSLIDE,
    val severity: AlertSeverity = AlertSeverity.MODERATE,
    val description: String = "",
    val district: String = "",
    val village: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val photoUris: List<Uri> = emptyList(),
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val isFetchingLocation: Boolean = false,
    val isOnline: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val submitReport: SubmitReportUseCase,
    private val locationHelper: LocationHelper,
    private val networkMonitor: NetworkMonitor,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth,
    private val getCurrentUser: GetCurrentUserUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    private var currentUser: User? = null

    init {
        viewModelScope.launch {
            currentUser = getCurrentUser()
            networkMonitor.isOnline.collect { online ->
                _uiState.update { it.copy(isOnline = online) }
            }
        }
        fetchLocation()
    }

    fun fetchLocation() {
        _uiState.update { it.copy(isFetchingLocation = true) }
        viewModelScope.launch {
            val loc = locationHelper.getCurrentLocation()
            _uiState.update {
                it.copy(
                    latitude = loc?.latitude ?: 0.0,
                    longitude = loc?.longitude ?: 0.0,
                    isFetchingLocation = false
                )
            }
        }
    }

    fun onIncidentTypeChange(type: IncidentType) = _uiState.update { it.copy(incidentType = type) }
    fun onSeverityChange(s: AlertSeverity) = _uiState.update { it.copy(severity = s) }
    fun onDescriptionChange(d: String) = _uiState.update { it.copy(description = d) }
    fun onDistrictChange(d: String) = _uiState.update { it.copy(district = d) }
    fun onVillageChange(v: String) = _uiState.update { it.copy(village = v) }
    fun onPhotosSelected(uris: List<Uri>) = _uiState.update { it.copy(photoUris = uris) }

    fun submitReport() {
        _uiState.update { it.copy(isSubmitting = true, error = null) }
        viewModelScope.launch {
            try {
                val state = _uiState.value
                // Upload photos if online
                val uploadedUrls = if (state.isOnline) {
                    state.photoUris.mapNotNull { uri ->
                        try {
                            val ref = storage.reference
                                .child("reports/${auth.currentUser?.uid}/${System.currentTimeMillis()}_${uri.lastPathSegment}")
                            ref.putFile(uri).await()
                            ref.downloadUrl.await().toString()
                        } catch (e: Exception) { null }
                    }
                } else emptyList()

                val report = IncidentReport(
                    reporterUid = auth.currentUser?.uid ?: "",
                    reporterName = currentUser?.name ?: "",
                    incidentType = state.incidentType,
                    severity = state.severity,
                    description = state.description,
                    latitude = state.latitude,
                    longitude = state.longitude,
                    photoUrls = uploadedUrls,
                    district = state.district,
                    village = state.village,
                    reportedAt = System.currentTimeMillis()
                )
                submitReport(report)
                    .onSuccess { _uiState.update { it.copy(isSubmitting = false, isSuccess = true) } }
                    .onFailure { e ->
                        _uiState.update { it.copy(isSubmitting = false, error = e.localizedMessage) }
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmitting = false, error = e.localizedMessage) }
            }
        }
    }
}
