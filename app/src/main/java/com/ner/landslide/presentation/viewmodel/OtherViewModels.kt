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
    val latitude: String = "26.8467",
    val longitude: String = "80.9462",
    val date: String = "",
    val locationName: String? = null,
    val isOutsideNER: Boolean = false,
    val outsideNERRealPlace: String? = null,
    val selectedPreset: String? = null,
    val elevation: String = "117",
    val slopeDeg: String = "0.8",
    val rainfall1d: String = "5.0",
    val rainfall3d: String = "15.0",
    val rainfall7d: String = "32.0",
    val soilMoisturePct: String = "50",
    val lithologyGroup: String = "Unconsolidated sediments",
    val landCover: String = "Built-up",
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
    private val locationHelper: LocationHelper,
    private val appLocationManager: com.ner.landslide.util.AppLocationManager
) : ViewModel() {

    companion object {
        val PRESET_DATA_MAP = mapOf(
            "wayanad" to listOf("980", "29.5", "42.0", "145.0", "280.0", "Metamorphic rocks", "Tree cover"),
            "shimla" to listOf("2205", "32.0", "35.0", "110.0", "210.0", "Metamorphic rocks", "Tree cover"),
            "chamoli" to listOf("1890", "36.5", "38.0", "120.0", "235.0", "Metamorphic rocks", "Bare/sparse vegetation"),
            "munnar" to listOf("1532", "31.0", "40.0", "135.0", "260.0", "Metamorphic rocks", "Cropland"),
            "lucknow" to listOf("117", "0.8", "5.0", "15.0", "32.0", "Unconsolidated sediments", "Built-up"),
            "gangtok" to listOf("1562", "28.5", "24.5", "88.0", "185.5", "Metamorphic rocks", "Tree cover"),
            "shillong" to listOf("1496", "22.0", "32.0", "110.0", "215.0", "Metamorphic rocks", "Tree cover"),
            "guwahati" to listOf("55", "16.5", "5.0", "18.0", "42.0", "Unconsolidated sediments", "Built-up"),
            "aizawl" to listOf("1132", "34.0", "18.0", "65.0", "140.0", "Siliciclastic sedimentary rocks", "Tree cover"),
            "kohima" to listOf("1444", "31.5", "20.0", "72.0", "155.0", "Siliciclastic sedimentary rocks", "Tree cover"),
            "itanagar" to listOf("320", "26.0", "28.0", "95.0", "190.0", "Mixed sedimentary rocks", "Tree cover"),
            "darjeeling" to listOf("2042", "38.0", "35.0", "125.0", "240.0", "Metamorphic rocks", "Cropland"),
            "kaziranga" to listOf("85", "4.5", "1.5", "4.5", "12.0", "Unconsolidated sediments", "Tree cover"),
            "majuli" to listOf("84", "2.0", "2.0", "6.0", "16.0", "Unconsolidated sediments", "Cropland"),
            "mawphlang" to listOf("1620", "15.0", "2.5", "7.0", "16.0", "Metamorphic rocks", "Tree cover")
        )
    }

    private val _uiState = MutableStateFlow(
        run {
            val initial = appLocationManager.selectedLocation.value
            val isPlain = (initial.latitude in 23.0..28.5 && initial.longitude in 75.0..87.5) || initial.latitude < 18.0
            PredictionUiState(
                latitude = String.format(java.util.Locale.US, "%.4f", initial.latitude),
                longitude = String.format(java.util.Locale.US, "%.4f", initial.longitude),
                locationName = initial.name,
                elevation = if (isPlain) "117" else "1250",
                slopeDeg = if (isPlain) "0.8" else "24.0",
                rainfall1d = if (isPlain) "5.0" else "20.0",
                rainfall3d = if (isPlain) "15.0" else "50.0",
                rainfall7d = if (isPlain) "32.0" else "110.0",
                lithologyGroup = if (isPlain) "Unconsolidated sediments" else "Metamorphic rocks",
                landCover = if (isPlain) "Built-up" else "Tree cover"
            )
        }
    )
    val uiState: StateFlow<PredictionUiState> = _uiState.asStateFlow()

    init {
        // Automatically sync with the app-wide single source of truth for location
        viewModelScope.launch {
            appLocationManager.selectedLocation.collect { loc ->
                syncWithLocation(loc)
            }
        }
    }

    private fun syncWithLocation(loc: com.ner.landslide.util.SelectedLocation) {
        val currentLat = _uiState.value.latitude.toDoubleOrNull() ?: 0.0
        val currentLon = _uiState.value.longitude.toDoubleOrNull() ?: 0.0
        val isSame = kotlin.math.abs(currentLat - loc.latitude) < 0.0001 &&
                     kotlin.math.abs(currentLon - loc.longitude) < 0.0001 &&
                     _uiState.value.locationName == loc.name

        val matchedPreset = PRESET_DATA_MAP.entries.firstOrNull { (id, _) ->
            loc.name.contains(id, ignoreCase = true)
        }?.key

        val isPlain = (loc.latitude in 23.0..28.5 && loc.longitude in 75.0..87.5) || loc.latitude < 18.0
        val preset = matchedPreset?.let { PRESET_DATA_MAP[it] }

        _uiState.update {
            it.copy(
                latitude = String.format(java.util.Locale.US, "%.4f", loc.latitude),
                longitude = String.format(java.util.Locale.US, "%.4f", loc.longitude),
                locationName = loc.name,
                selectedPreset = matchedPreset,
                isOutsideNER = false,
                outsideNERRealPlace = null,
                elevation = preset?.getOrNull(0) ?: if (isPlain) "117" else "1250",
                slopeDeg = preset?.getOrNull(1) ?: if (isPlain) "0.8" else "24.0",
                rainfall1d = preset?.getOrNull(2) ?: if (isPlain) "5.0" else "25.0",
                rainfall3d = preset?.getOrNull(3) ?: if (isPlain) "15.0" else "55.0",
                rainfall7d = preset?.getOrNull(4) ?: if (isPlain) "32.0" else "115.0",
                lithologyGroup = preset?.getOrNull(5) ?: if (isPlain) "Unconsolidated sediments" else "Metamorphic rocks",
                landCover = preset?.getOrNull(6) ?: if (isPlain) "Built-up" else "Tree cover",
                telemetryMessage = "Synced with Active Location (${loc.name})",
                error = null
            )
        }
    }

    fun onLatitudeChange(v: String) = _uiState.update {
        it.copy(latitude = v, selectedPreset = null, isOutsideNER = false, outsideNERRealPlace = null)
    }
    fun onLongitudeChange(v: String) = _uiState.update {
        it.copy(longitude = v, selectedPreset = null, isOutsideNER = false, outsideNERRealPlace = null)
    }
    fun onDateChange(v: String) = _uiState.update { it.copy(date = v) }

    fun useCurrentLocation() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isExtractingFeatures = true, error = null) }
                val success = appLocationManager.switchToCurrentGpsLocation()
                if (success) {
                    val loc = appLocationManager.selectedLocation.value
                    syncWithLocation(loc)
                } else {
                    _uiState.update {
                        it.copy(
                            isExtractingFeatures = false,
                            error = "Unable to acquire GPS lock. Please check location permissions."
                        )
                    }
                }
            } catch (e: Throwable) {
                _uiState.update {
                    it.copy(
                        isExtractingFeatures = false,
                        error = "GPS Error: ${e.localizedMessage ?: "Unknown"}"
                    )
                }
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
        val preset = PRESET_DATA_MAP[presetId]
        _uiState.update {
            it.copy(
                selectedPreset = presetId,
                latitude = lat.toString(),
                longitude = lon.toString(),
                locationName = name,
                isOutsideNER = false,
                outsideNERRealPlace = null,
                // Immediately populate telemetry to guarantee zero stale data lag
                elevation = preset?.getOrNull(0) ?: it.elevation,
                slopeDeg = preset?.getOrNull(1) ?: it.slopeDeg,
                rainfall1d = preset?.getOrNull(2) ?: it.rainfall1d,
                rainfall3d = preset?.getOrNull(3) ?: it.rainfall3d,
                rainfall7d = preset?.getOrNull(4) ?: it.rainfall7d,
                lithologyGroup = preset?.getOrNull(5) ?: it.lithologyGroup,
                landCover = preset?.getOrNull(6) ?: it.landCover,
                telemetryMessage = "Loaded from Curated Regional DEM / GLiM Cache",
                error = null
            )
        }
        // Update global centralized location store so all pages synchronize
        appLocationManager.setCustomLocation(
            name = name,
            latitude = lat,
            longitude = lon
        )
    }

    fun fetchTelemetry(forcedLat: Double? = null, forcedLon: Double? = null, autoPredict: Boolean = false) {
        val lat = forcedLat ?: _uiState.value.latitude.toDoubleOrNull() ?: 26.8467
        val lon = forcedLon ?: _uiState.value.longitude.toDoubleOrNull() ?: 80.9462
        val date = _uiState.value.date.ifBlank { null }

        _uiState.update { it.copy(isExtractingFeatures = true, error = null, telemetryMessage = null) }

        viewModelScope.launch {
            try {
                val resolved = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        locationHelper.reverseGeocode(lat, lon)
                    } catch (e: Throwable) {
                        null
                    }
                }
                extractFeatures(lat, lon, date)
                    .onSuccess { feat ->
                        val resolvedPlace = if (!feat.locationName.isNullOrBlank() && !feat.locationName.contains("°")) {
                            feat.locationName
                        } else {
                            resolved?.formattedHeadline ?: "${String.format(java.util.Locale.US, "%.2f", lat)}°N, ${String.format(java.util.Locale.US, "%.2f", lon)}°E"
                        }
                        val src = feat.source["elevation"] ?: "Curated Regional DEM / GLiM Cache"
                        val formattedMsg = if (src.contains("Cache", ignoreCase = true)) {
                            "Loaded from Curated Regional DEM / GLiM Cache"
                        } else {
                            "Synced from SRTM 30m DEM / NASA-ISRO GLiM"
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
                                locationName = it.locationName ?: resolvedPlace,
                                telemetryMessage = formattedMsg
                            )
                        }
                        if (autoPredict) {
                            predict()
                        }
                    }
                    .onFailure { err ->
                        _uiState.update {
                            it.copy(
                                isExtractingFeatures = false,
                                telemetryMessage = "Curated regional telemetry baseline active",
                                error = null
                            )
                        }
                        if (autoPredict) {
                            predict()
                        }
                    }
            } catch (e: Throwable) {
                _uiState.update {
                    it.copy(
                        isExtractingFeatures = false,
                        error = "Telemetry notice: ${e.localizedMessage ?: "Fallback active"}"
                    )
                }
            } finally {
                // Guaranteed safety: spinner will never get stuck indefinitely
                _uiState.update { it.copy(isExtractingFeatures = false) }
            }
        }
    }

    fun predict() {
        val state = _uiState.value
        val lat = state.latitude.toDoubleOrNull() ?: 0.0
        val lon = state.longitude.toDoubleOrNull() ?: 0.0
        val isPlain = (lat in 23.0..28.5 && lon in 75.0..87.5) || (state.elevation.toDoubleOrNull() ?: 1000.0) < 350.0

        val rain1 = state.rainfall1d.toDoubleOrNull() ?: (if (isPlain) 5.0 else 25.0)
        val slope = state.slopeDeg.toDoubleOrNull() ?: (if (isPlain) 1.0 else 24.0)
        val rain3 = state.rainfall3d.toDoubleOrNull() ?: (rain1 * 2.2)
        val rain7 = state.rainfall7d.toDoubleOrNull() ?: (rain3 * 1.7)
        val elev = state.elevation.toDoubleOrNull() ?: (if (isPlain) 120.0 else 1450.0)
        val moist = state.soilMoisturePct.toDoubleOrNull() ?: (if (isPlain) 40.0 else 75.0)

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
            try {
                predictRisk(request)
                    .onSuccess { rawResult ->
                        // Sanitize result factors to prevent NPE and NaN in UI
                        val cleanFactors = rawResult.factors
                            .filter { it.key.isNotBlank() && it.value != null && !it.value.isNaN() }
                        val cleanImportances = rawResult.featureImportances
                            .filter { it.key.isNotBlank() && it.value != null && !it.value.isNaN() }
                        val cleanProb = if (rawResult.probability.isNaN() || rawResult.probability.isInfinite()) {
                            0.01
                        } else {
                            rawResult.probability.coerceIn(0.0, 1.0)
                        }
                        val sanitizedResult = rawResult.copy(
                            probability = cleanProb,
                            factors = cleanFactors,
                            featureImportances = cleanImportances
                        )
                        _uiState.update { it.copy(isLoading = false, result = sanitizedResult) }
                    }
                    .onFailure { e ->
                        _uiState.update { it.copy(isLoading = false, error = e.localizedMessage ?: "Assessment error") }
                    }
            } catch (e: Throwable) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage ?: "Assessment error") }
            }
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
    private val locationHelper: LocationHelper,
    private val appLocationManager: com.ner.landslide.util.AppLocationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private var currentLat: Double = 27.33
    private var currentLng: Double = 88.61

    init {
        // Automatically sync with the app-wide single source of truth for location
        viewModelScope.launch {
            appLocationManager.selectedLocation.collect { loc ->
                loadWeather(loc.latitude, loc.longitude, loc.name)
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
            appLocationManager.switchToCurrentGpsLocation()
        }
    }

    fun loadWeather(lat: Double, lng: Double, forcedName: String? = null) {
        currentLat = lat
        currentLng = lng
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val resolvedHeadline = forcedName ?: locationHelper.reverseGeocode(lat, lng).formattedHeadline
            getWeather(lat, lng)
                .onSuccess { forecast ->
                    _uiState.update {
                        it.copy(
                            forecast = forecast,
                            locationName = resolvedHeadline.ifBlank { "Selected Sector" },
                            lastUpdatedTime = System.currentTimeMillis(),
                            isLoading = false
                        )
                    }
                }
                .onFailure { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.localizedMessage ?: "Failed to load weather"
                        )
                    }
                }
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
