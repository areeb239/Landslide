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
    val resolvedLocationName: String = "",
    val isGpsEnabled: Boolean = true,
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
    private val getCurrentUser: GetCurrentUserUseCase,
    private val appLocationManager: com.ner.landslide.util.AppLocationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        run {
            val initialLoc = appLocationManager.selectedLocation.value
            ReportUiState(
                latitude = initialLoc.latitude,
                longitude = initialLoc.longitude,
                resolvedLocationName = initialLoc.name,
                district = initialLoc.district
            )
        }
    )
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    private var currentUser: User? = null

    init {
        viewModelScope.launch {
            currentUser = getCurrentUser()
            networkMonitor.isOnline.collect { online ->
                _uiState.update { it.copy(isOnline = online) }
            }
        }
        // Keep in sync with app-wide single source of truth for location
        viewModelScope.launch {
            appLocationManager.selectedLocation.collect { loc ->
                _uiState.update {
                    it.copy(
                        latitude = loc.latitude,
                        longitude = loc.longitude,
                        resolvedLocationName = loc.name,
                        district = if (it.district.isBlank()) loc.district else it.district
                    )
                }
            }
        }
        val currentLoc = appLocationManager.selectedLocation.value
        if (currentLoc.isGpsLocation || (currentLoc.latitude == 0.0 && currentLoc.longitude == 0.0)) {
            autoDetectLocation()
        }
    }

    fun checkGpsEnabled(): Boolean = locationHelper.isGpsEnabled()

    fun autoDetectLocation() {
        val gpsOn = locationHelper.isGpsEnabled()
        _uiState.update { it.copy(isFetchingLocation = true, isGpsEnabled = gpsOn) }
        viewModelScope.launch {
            val success = appLocationManager.switchToCurrentGpsLocation()
            if (success) {
                val loc = appLocationManager.selectedLocation.value
                val isNER = com.ner.landslide.util.LocationHelper.isWithinNER(loc.latitude, loc.longitude)
                val headline = if (isNER) loc.name else "${loc.name} [Outside NER Corridor]"
                _uiState.update {
                    it.copy(
                        latitude = loc.latitude,
                        longitude = loc.longitude,
                        resolvedLocationName = headline,
                        district = if (it.district.isBlank() && loc.district.isNotBlank()) loc.district else it.district,
                        isFetchingLocation = false,
                        isGpsEnabled = true
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isFetchingLocation = false,
                        isGpsEnabled = locationHelper.isGpsEnabled()
                    )
                }
            }
        }
    }

    fun fetchLocation() = autoDetectLocation()

    fun onIncidentTypeChange(type: IncidentType) = _uiState.update { it.copy(incidentType = type) }
    fun onSeverityChange(s: AlertSeverity) = _uiState.update { it.copy(severity = s) }
    fun onDescriptionChange(d: String) = _uiState.update { it.copy(description = d) }
    fun onDistrictChange(d: String) = _uiState.update { it.copy(district = d) }
    fun onVillageChange(v: String) = _uiState.update { it.copy(village = v) }
    fun onPhotosSelected(uris: List<Uri>) = _uiState.update { it.copy(photoUris = uris) }

    fun clearError() = _uiState.update { it.copy(error = null) }

    fun isStep1Valid(): Boolean {
        val state = _uiState.value
        val hasDesc = state.description.trim().length >= 5
        val hasLoc = (state.latitude != 0.0 && state.longitude != 0.0) || state.district.trim().isNotBlank()
        return hasDesc && hasLoc
    }

    fun resetState() {
        _uiState.update { current ->
            ReportUiState(
                latitude = current.latitude,
                longitude = current.longitude,
                resolvedLocationName = current.resolvedLocationName,
                isGpsEnabled = current.isGpsEnabled,
                isOnline = current.isOnline,
                incidentType = IncidentType.LANDSLIDE,
                severity = AlertSeverity.MODERATE,
                description = "",
                district = "",
                village = "",
                photoUris = emptyList(),
                isSubmitting = false,
                isSuccess = false,
                isFetchingLocation = false,
                error = null
            )
        }
    }

    fun submitReport() {
        val state = _uiState.value
        if (state.description.trim().length < 5) {
            _uiState.update { it.copy(error = "Please describe what happened (at least 5 characters).") }
            return
        }
        val hasLocation = (state.latitude != 0.0 && state.longitude != 0.0) || state.district.trim().isNotBlank()
        if (!hasLocation) {
            _uiState.update { it.copy(error = "Please provide an incident location using GPS or entering your town/landmark.") }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, error = null) }
        viewModelScope.launch {
            try {
                if (currentUser == null) {
                    try {
                        currentUser = getCurrentUser()
                    } catch (e: Exception) { /* ignore */ }
                }

                var finalLat = state.latitude
                var finalLon = state.longitude
                if (finalLat == 0.0 && finalLon == 0.0 && state.district.isNotBlank()) {
                    val resolved = locationHelper.forwardGeocode(state.district)
                    if (resolved != null) {
                        finalLat = resolved.first
                        finalLon = resolved.second
                    }
                }

                // Upload photos if online
                val uploadedUrls = if (state.isOnline) {
                    state.photoUris.mapNotNull { uri ->
                        try {
                            val uid = auth.currentUser?.uid ?: currentUser?.uid ?: "anon"
                            val ref = storage.reference
                                .child("reports/$uid/${System.currentTimeMillis()}_${uri.lastPathSegment}")
                            ref.putFile(uri).await()
                            ref.downloadUrl.await().toString()
                        } catch (e: Exception) { null }
                    }
                } else emptyList()

                // Preserve local photo URIs if offline or remote upload was unavailable
                val finalPhotos = if (uploadedUrls.isNotEmpty()) {
                    uploadedUrls
                } else {
                    state.photoUris.map { it.toString() }
                }

                val resolvedUid = auth.currentUser?.uid?.ifBlank { null }
                    ?: currentUser?.uid?.ifBlank { null }
                    ?: "citizen_${System.currentTimeMillis()}"

                val resolvedName = currentUser?.name?.ifBlank { null }
                    ?: auth.currentUser?.displayName?.ifBlank { null }
                    ?: "Citizen"

                val report = IncidentReport(
                    reporterUid = resolvedUid,
                    reporterName = resolvedName,
                    incidentType = state.incidentType,
                    severity = state.severity,
                    description = state.description.trim(),
                    latitude = finalLat,
                    longitude = finalLon,
                    photoUrls = finalPhotos,
                    district = state.district.trim(),
                    village = state.village.trim(),
                    reportedAt = System.currentTimeMillis()
                )
                submitReport(report)
                    .onSuccess { _uiState.update { it.copy(isSubmitting = false, isSuccess = true) } }
                    .onFailure { e ->
                        _uiState.update { it.copy(isSubmitting = false, error = e.localizedMessage ?: "Failed to submit report. Please retry.") }
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSubmitting = false, error = e.localizedMessage ?: "Unexpected submission error.") }
            }
        }
    }
}
