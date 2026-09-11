package com.ner.landslide.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ner.landslide.domain.model.*
import com.ner.landslide.domain.usecase.*
import com.ner.landslide.util.LocationHelper
import com.ner.landslide.util.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PredictionUiState(
    val latitude: String = "27.33",
    val longitude: String = "88.61",
    val date: String = "",
    val locationName: String? = "Gangtok (Sikkim)",
    val selectedPreset: String? = "gangtok",
    val elevation: String = "1562",
    val slopeDeg: String = "28.5",
    val rainfall1d: String = "24.5",
    val rainfall3d: String = "88.0",
    val rainfall7d: String = "185.5",
    val soilMoisturePct: String = "82",
    val lithologyGroup: String = "Metamorphic rocks",
    val landCover: String = "Tree cover",
    val isExtractingFeatures: Boolean = false,
    val telemetryMessage: String? = null,
    val result: PredictionResult? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    // Backward-compatibility aliases
    val rainfallMm: String get() = rainfall1d
    val antecedentRain3d: String get() = rainfall3d
}

@HiltViewModel
class PredictionViewModel @Inject constructor(
    private val predictRisk: PredictRiskUseCase,
    private val extractFeatures: ExtractFeaturesUseCase,
    private val locationHelper: LocationHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(PredictionUiState())
    val uiState: StateFlow<PredictionUiState> = _uiState.asStateFlow()

    fun onLatitudeChange(v: String) = _uiState.update { it.copy(latitude = v, selectedPreset = null) }
    fun onLongitudeChange(v: String) = _uiState.update { it.copy(longitude = v, selectedPreset = null) }
    fun onDateChange(v: String) = _uiState.update { it.copy(date = v) }

    fun useCurrentLocation() {
        viewModelScope.launch {
            locationHelper.getCurrentLocation()?.let { loc ->
                val lat = String.format(java.util.Locale.US, "%.4f", loc.latitude)
                val lon = String.format(java.util.Locale.US, "%.4f", loc.longitude)
                val resolved = locationHelper.reverseGeocode(loc.latitude, loc.longitude)
                _uiState.update {
                    it.copy(
                        latitude = lat,
                        longitude = lon,
                        locationName = resolved.formattedHeadline,
                        selectedPreset = null
                    )
                }
                fetchTelemetry(loc.latitude, loc.longitude)
            }
        }
    }

    fun onElevationChange(v: String) = _uiState.update { it.copy(elevation = v) }
    fun onSlopeChange(v: String) = _uiState.update { it.copy(slopeDeg = v) }
    fun onRainfall1dChange(v: String) = _uiState.update { it.copy(rainfall1d = v) }
    fun onRainfall3dChange(v: String) = _uiState.update { it.copy(rainfall3d = v) }
    fun onRainfall7dChange(v: String) = _uiState.update { it.copy(rainfall7d = v) }
    fun onSoilMoistureChange(v: String) = _uiState.update { it.copy(soilMoisturePct = v) }
    fun onLithologyChange(v: String) = _uiState.update { it.copy(lithologyGroup = v) }
    fun onLandCoverChange(v: String) = _uiState.update { it.copy(landCover = v) }

    // Backward-compatible setters
    fun onRainfallChange(v: String) = onRainfall1dChange(v)
    fun onAntecedentRainChange(v: String) = onRainfall3dChange(v)

    fun selectPreset(presetId: String, lat: Double, lon: Double, name: String) {
        _uiState.update {
            it.copy(
                selectedPreset = presetId,
                latitude = lat.toString(),
                longitude = lon.toString(),
                locationName = name
            )
        }
        fetchTelemetry(lat, lon)
    }

    fun fetchTelemetry(forcedLat: Double? = null, forcedLon: Double? = null) {
        val lat = forcedLat ?: _uiState.value.latitude.toDoubleOrNull() ?: 27.33
        val lon = forcedLon ?: _uiState.value.longitude.toDoubleOrNull() ?: 88.61
        val date = _uiState.value.date.ifBlank { null }

        _uiState.update { it.copy(isExtractingFeatures = true, error = null, telemetryMessage = null) }

        viewModelScope.launch {
            val resolved = locationHelper.reverseGeocode(lat, lon)
            extractFeatures(lat, lon, date)
                .onSuccess { feat ->
                    val resolvedPlace = if (!feat.locationName.isNullOrBlank() && !feat.locationName.contains("°")) {
                        feat.locationName
                    } else {
                        resolved.formattedHeadline
                    }
                    _uiState.update {
                        it.copy(
                            isExtractingFeatures = false,
                            elevation = feat.elevation.toInt().toString(),
                            slopeDeg = String.format(java.util.Locale.US, "%.1f", feat.slope),
                            rainfall1d = String.format(java.util.Locale.US, "%.1f", feat.rainfallPrevious1d),
                            rainfall3d = String.format(java.util.Locale.US, "%.1f", feat.rainfallPrevious3d),
                            rainfall7d = String.format(java.util.Locale.US, "%.1f", feat.rainfallPrevious7d),
                            lithologyGroup = feat.lithologyGroup,
                            landCover = feat.landCover,
                            locationName = resolvedPlace,
                            telemetryMessage = "Synced from ${feat.source["elevation"] ?: "SRTM DEM / GLiM"}"
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isExtractingFeatures = false,
                            locationName = resolved.formattedHeadline,
                            error = "Telemetry lookup failed: ${err.localizedMessage}"
                        )
                    }
                }
        }
    }

    fun predict() {
        val state = _uiState.value
        val rain1 = state.rainfall1d.toDoubleOrNull() ?: 45.0
        val slope = state.slopeDeg.toDoubleOrNull() ?: 35.0
        val rain3 = state.rainfall3d.toDoubleOrNull() ?: (rain1 * 2.2)
        val rain7 = state.rainfall7d.toDoubleOrNull() ?: (rain3 * 1.7)
        val elev = state.elevation.toDoubleOrNull() ?: 1450.0
        val moist = state.soilMoisturePct.toDoubleOrNull() ?: 75.0
        val lat = state.latitude.toDoubleOrNull() ?: 0.0
        val lon = state.longitude.toDoubleOrNull() ?: 0.0

        val request = PredictionRequest(
            rainfallMm = rain1,
            slopeDeg = slope,
            soilMoisturePct = moist,
            antecedentRain3d = rain3,
            elevation = elev,
            slope = slope,
            rainfallPrevious1d = rain1,
            rainfallPrevious3d = rain3,
            rainfallPrevious7d = rain7,
            lithologyGroup = state.lithologyGroup,
            landCover = state.landCover,
            latitude = lat,
            longitude = lon
        )

        _uiState.update { it.copy(isLoading = true, error = null, result = null) }
        viewModelScope.launch {
            predictRisk(request)
                .onSuccess { result -> _uiState.update { it.copy(isLoading = false, result = result) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) } }
        }
    }
}

data class WeatherUiState(
    val forecast: WeatherForecast? = null,
    val locationName: String = "Guwahati, Assam",
    val lastUpdatedTime: Long = System.currentTimeMillis(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val getWeather: GetWeatherForecastUseCase,
    private val networkMonitor: NetworkMonitor,
    private val locationHelper: LocationHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private var currentLat: Double = 26.14
    private var currentLng: Double = 91.74

    // Auto-detect GPS location if available, otherwise default to regional capital
    init {
        viewModelScope.launch {
            val loc = locationHelper.getCurrentLocation()
            if (loc != null) {
                loadWeather(loc.latitude, loc.longitude)
            } else {
                loadWeather(26.14, 91.74)
            }
        }
        observeNetworkReconnection()
    }

    private fun observeNetworkReconnection() {
        viewModelScope.launch {
            networkMonitor.isOnline.collect { online ->
                if (online && _uiState.value.error != null) {
                    loadWeather(currentLat, currentLng)
                }
            }
        }
    }

    fun useCurrentLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val loc = locationHelper.getCurrentLocation()
            if (loc != null) {
                loadWeather(loc.latitude, loc.longitude)
            } else {
                loadWeather(currentLat, currentLng)
            }
        }
    }

    fun loadWeather(lat: Double, lng: Double) {
        currentLat = lat
        currentLng = lng
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val resolved = locationHelper.reverseGeocode(lat, lng)
            getWeather(lat, lng)
                .onSuccess { forecast ->
                    _uiState.update {
                        it.copy(
                            forecast = forecast,
                            locationName = resolved.formattedHeadline,
                            lastUpdatedTime = System.currentTimeMillis(),
                            isLoading = false
                        )
                    }
                }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) } }
        }
    }
}

// ─── Admin ────────────────────────────────────────────────────────────────────

data class AdminUiState(
    val reports: List<IncidentReport> = emptyList(),
    val sosAlerts: List<SOSAlert> = emptyList(),
    val isLoading: Boolean = true,
    val broadcastTitle: String = "",
    val broadcastDescription: String = "",
    val broadcastSeverity: AlertSeverity = AlertSeverity.HIGH,
    val broadcastDistrict: String = "",
    val isBroadcasting: Boolean = false,
    val broadcastSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val getAllReports: GetAllReportsUseCase,
    private val getAllSOSAlerts: GetAllSOSAlertsUseCase,
    private val resolveSOS: ResolveSOSUseCase,
    private val broadcastAlert: BroadcastAlertUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getAllReports().collect { reports ->
                _uiState.update { it.copy(reports = reports, isLoading = false) }
            }
        }
        viewModelScope.launch {
            getAllSOSAlerts().collect { sos ->
                _uiState.update { it.copy(sosAlerts = sos) }
            }
        }
    }

    fun onBroadcastTitleChange(v: String) = _uiState.update { it.copy(broadcastTitle = v) }
    fun onBroadcastDescriptionChange(v: String) = _uiState.update { it.copy(broadcastDescription = v) }
    fun onBroadcastSeverityChange(s: AlertSeverity) = _uiState.update { it.copy(broadcastSeverity = s) }
    fun onBroadcastDistrictChange(v: String) = _uiState.update { it.copy(broadcastDistrict = v) }

    fun sendBroadcast() {
        val state = _uiState.value
        if (state.broadcastTitle.isBlank()) return
        _uiState.update { it.copy(isBroadcasting = true) }
        viewModelScope.launch {
            val alert = Alert(
                title = state.broadcastTitle,
                description = state.broadcastDescription,
                severity = state.broadcastSeverity,
                affectedDistrict = state.broadcastDistrict
            )
            broadcastAlert(alert)
                .onSuccess { _uiState.update { it.copy(isBroadcasting = false, broadcastSuccess = true) } }
                .onFailure { e -> _uiState.update { it.copy(isBroadcasting = false, error = e.localizedMessage) } }
        }
    }

    fun resolveSOSAlert(sosId: String) {
        viewModelScope.launch { resolveSOS(sosId) }
    }
}

// ─── Profile ──────────────────────────────────────────────────────────────────

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentUser: GetCurrentUserUseCase,
    private val userRepository: com.ner.landslide.domain.repository.UserRepository,
    private val auth: com.google.firebase.auth.FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val user = getCurrentUser()
            _uiState.update { it.copy(user = user, isLoading = false) }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            userRepository.signOut()
        }
    }
}
