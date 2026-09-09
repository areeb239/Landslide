package com.ner.landslide.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ner.landslide.domain.model.*
import com.ner.landslide.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PredictionUiState(
    val rainfallMm: String = "",
    val slopeDeg: String = "",
    val soilMoisturePct: String = "",
    val antecedentRain3d: String = "",
    val result: PredictionResult? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PredictionViewModel @Inject constructor(
    private val predictRisk: PredictRiskUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PredictionUiState())
    val uiState: StateFlow<PredictionUiState> = _uiState.asStateFlow()

    fun onRainfallChange(v: String) = _uiState.update { it.copy(rainfallMm = v) }
    fun onSlopeChange(v: String) = _uiState.update { it.copy(slopeDeg = v) }
    fun onSoilMoistureChange(v: String) = _uiState.update { it.copy(soilMoisturePct = v) }
    fun onAntecedentRainChange(v: String) = _uiState.update { it.copy(antecedentRain3d = v) }

    fun predict() {
        val state = _uiState.value
        val request = PredictionRequest(
            rainfallMm = state.rainfallMm.toDoubleOrNull() ?: return,
            slopeDeg = state.slopeDeg.toDoubleOrNull() ?: return,
            soilMoisturePct = state.soilMoisturePct.toDoubleOrNull() ?: return,
            antecedentRain3d = state.antecedentRain3d.toDoubleOrNull() ?: return
        )
        _uiState.update { it.copy(isLoading = true, error = null, result = null) }
        viewModelScope.launch {
            predictRisk(request)
                .onSuccess { result -> _uiState.update { it.copy(isLoading = false, result = result) } }
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) } }
        }
    }
}

// ─── Weather ──────────────────────────────────────────────────────────────────

data class WeatherUiState(
    val forecast: WeatherForecast? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val getWeather: GetWeatherForecastUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(WeatherUiState())
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    // Default to Guwahati, can be overridden with user GPS
    init { loadWeather(26.14, 91.74) }

    fun loadWeather(lat: Double, lng: Double) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            getWeather(lat, lng)
                .onSuccess { forecast -> _uiState.update { it.copy(forecast = forecast, isLoading = false) } }
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
