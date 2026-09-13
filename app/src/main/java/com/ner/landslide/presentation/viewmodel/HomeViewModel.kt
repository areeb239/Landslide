package com.ner.landslide.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ner.landslide.domain.model.*
import com.ner.landslide.domain.usecase.*
import com.ner.landslide.presentation.ui.weather.WeatherCondition
import com.ner.landslide.presentation.ui.weather.resolveCondition
import com.ner.landslide.service.SyncWorker
import com.ner.landslide.util.AppLocationManager
import com.ner.landslide.util.EmergencySmsHelper
import com.ner.landslide.util.LocationHelper
import com.ner.landslide.util.LocationSearchResult
import com.ner.landslide.util.NetworkMonitor
import com.ner.landslide.util.SelectedLocation
import com.ner.landslide.util.SmsDispatchResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

data class HomeUiState(
    val alerts: List<Alert> = emptyList(),
    val currentUser: User? = null,
    val isLoading: Boolean = true,
    val isOnline: Boolean = true,
    val lastSyncedTime: Long = System.currentTimeMillis(),
    val sosLocationLat: Double = 27.1765,
    val sosLocationLon: Double = 88.5321,
    val sosLocationName: String = "Northeast India Sector",
    val sosState: SOSState = SOSState.IDLE,
    val error: String? = null,

    // Integrated Weather & Location Selection
    val selectedLocation: SelectedLocation = SelectedLocation("Gangtok, Sikkim", 27.3389, 88.6065, false),
    val currentWeather: HourlyWeather? = null,
    val weatherCondition: WeatherCondition = WeatherCondition.PARTLY_CLOUDY,
    val isWeatherLoading: Boolean = false,
    val currentRiskLevel: String = "LOW",
    val currentRiskProbability: Double = 0.01,
    val isRiskLoading: Boolean = false,
    val locationSearchResults: List<LocationSearchResult> = emptyList(),
    val isSearchingLocation: Boolean = false
)

enum class SOSState {
    IDLE,
    COUNTDOWN,
    SENDING,
    SENT,
    SENT_OFFLINE_SMS,
    SENT_OFFLINE_INTENT,
    ERROR
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAlerts: GetActiveAlertsUseCase,
    private val triggerSOS: TriggerSOSUseCase,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val getWeather: GetWeatherForecastUseCase,
    private val predictRisk: PredictRiskUseCase,
    private val appLocationManager: AppLocationManager,
    private val locationHelper: LocationHelper,
    private val networkMonitor: NetworkMonitor,
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadUser()
        observeAlerts()
        observeNetwork()
        observeSelectedLocation()
    }

    private fun observeSelectedLocation() {
        viewModelScope.launch {
            appLocationManager.selectedLocation.collect { loc ->
                _uiState.update {
                    it.copy(
                        selectedLocation = loc,
                        sosLocationLat = loc.latitude,
                        sosLocationLon = loc.longitude,
                        sosLocationName = loc.name
                    )
                }
                refreshWeather(loc.latitude, loc.longitude)
                refreshRisk(loc.latitude, loc.longitude)
            }
        }
    }

    fun refreshWeather(lat: Double = _uiState.value.selectedLocation.latitude, lon: Double = _uiState.value.selectedLocation.longitude) {
        _uiState.update { it.copy(isWeatherLoading = true) }
        viewModelScope.launch {
            getWeather(lat, lon)
                .onSuccess { forecast ->
                    // Use real-time 'currentWeather' from Open-Meteo
                    val current = forecast.currentWeather ?: forecast.hourlyData.firstOrNull()
                    val cond = if (current != null) {
                        resolveCondition(
                            rainfallMm = current.rainfallMm,
                            rainProb = current.rainProbability,
                            windSpeedKmh = current.windSpeedKmh,
                            humidity = current.humidity,
                            weatherCode = current.weatherCode
                        )
                    } else WeatherCondition.PARTLY_CLOUDY

                    android.util.Log.i(
                        "BhoochetakWeather",
                        "HomeViewModel UI State updated -> lat=$lat, lon=$lon, temp=${current?.temperature}°C (Apparent=${current?.apparentTemperature}°C), humidity=${current?.humidity}%, wind=${current?.windSpeedKmh}km/h, rainProb=${current?.rainProbability}%, condition=${cond.label}, time=${current?.time}"
                    )

                    _uiState.update {
                        it.copy(
                            currentWeather = current,
                            weatherCondition = cond,
                            isWeatherLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    android.util.Log.e("BhoochetakWeather", "Failed to refresh weather in HomeViewModel", error)
                    _uiState.update { it.copy(isWeatherLoading = false) }
                }
        }
    }

    fun refreshRisk(lat: Double = _uiState.value.selectedLocation.latitude, lon: Double = _uiState.value.selectedLocation.longitude) {
        _uiState.update { it.copy(isRiskLoading = true) }
        viewModelScope.launch {
            predictRisk(PredictionRequest(latitude = lat, longitude = lon))
                .onSuccess { result ->
                    _uiState.update {
                        it.copy(
                            currentRiskLevel = result.riskLevel,
                            currentRiskProbability = result.probability,
                            isRiskLoading = false
                        )
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isRiskLoading = false) }
                }
        }
    }

    fun switchToGpsLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isWeatherLoading = true, isRiskLoading = true) }
            val success = appLocationManager.switchToCurrentGpsLocation()
            if (!success) {
                // If GPS is disabled or denied, retry reverse lookup on last known or notify
                val loc = locationHelper.getCurrentLocation()
                if (loc != null) {
                    val resolved = locationHelper.reverseGeocode(loc.latitude, loc.longitude)
                    appLocationManager.setCustomLocation(
                        name = resolved.formattedHeadline,
                        latitude = loc.latitude,
                        longitude = loc.longitude,
                        district = resolved.district,
                        state = resolved.state
                    )
                }
            }
        }
    }

    fun selectCustomLocation(result: LocationSearchResult) {
        val cleanName = if (result.state.isNotBlank() && !result.name.contains(result.state, ignoreCase = true)) {
            "${result.name}, ${result.state}"
        } else {
            result.name
        }
        appLocationManager.setCustomLocation(
            name = cleanName,
            latitude = result.latitude,
            longitude = result.longitude,
            district = result.name,
            state = result.state
        )
    }

    fun searchLocations(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSearchingLocation = true) }
            val results = appLocationManager.searchLocations(query)
            _uiState.update { it.copy(locationSearchResults = results, isSearchingLocation = false) }
        }
    }

    private fun loadUser() {
        viewModelScope.launch {
            val user = getCurrentUser()
            _uiState.update { it.copy(currentUser = user) }
        }
    }

    private fun observeNetwork() {
        viewModelScope.launch {
            networkMonitor.isOnline.collect { online ->
                _uiState.update { it.copy(isOnline = online) }
                if (online) {
                    observeAlerts()
                    SyncWorker.triggerImmediateSync(context)
                    refreshWeather()
                    refreshRisk()
                }
            }
        }
    }

    private fun observeAlerts() {
        viewModelScope.launch {
            getAlerts().collect { alerts ->
                _uiState.update {
                    it.copy(
                        alerts = alerts,
                        isLoading = false,
                        lastSyncedTime = System.currentTimeMillis()
                    )
                }
            }
        }
    }

    fun onSOSTrigger() {
        _uiState.update { it.copy(sosState = SOSState.SENDING) }
        viewModelScope.launch {
            val location = locationHelper.getCurrentLocation()
            val lat = location?.latitude ?: _uiState.value.sosLocationLat
            val lon = location?.longitude ?: _uiState.value.sosLocationLon
            val resolved = locationHelper.reverseGeocode(lat, lon)
            val isNER = com.ner.landslide.util.LocationHelper.isWithinNER(lat, lon)
            val baseSector = resolved.formattedHeadline.ifBlank { _uiState.value.sosLocationName }
            val sectorName = if (isNER) baseSector else "$baseSector [Outside NER Corridor]"

            val user = _uiState.value.currentUser
            val uid = auth.currentUser?.uid ?: user?.uid ?: "offline_citizen"
            val userName = user?.name?.ifBlank { "Citizen" } ?: "Citizen"
            val userPhone = user?.phone

            val sos = SOSAlert(
                uid = uid,
                name = userName,
                latitude = lat,
                longitude = lon,
                message = "SOS — Immediate Landslide Rescue Needed! Sector: $sectorName",
                triggeredAt = System.currentTimeMillis()
            )

            val isOnline = networkMonitor.isCurrentlyOnline()

            if (isOnline) {
                // Attempt cloud Firestore dispatch with a 4-second timeout
                val cloudResult = withTimeoutOrNull(4000L) {
                    triggerSOS(sos)
                }
                if (cloudResult != null && cloudResult.isSuccess) {
                    _uiState.update {
                        it.copy(
                            sosState = SOSState.SENT,
                            sosLocationLat = lat,
                            sosLocationLon = lon,
                            sosLocationName = sectorName
                        )
                    }
                    return@launch
                }
            }

            // Fallback for offline mode or timed-out cloud sync
            triggerSOS.savePending(sos, sectorName = sectorName, smsDispatched = true)

            val smsResult = EmergencySmsHelper.dispatchEmergencySms(
                context = context,
                userName = userName,
                userPhone = userPhone,
                latitude = lat,
                longitude = lon,
                sectorName = sectorName
            )

            // Queue background sync so Firestore receives record as soon as mobile data connects
            SyncWorker.triggerImmediateSync(context)

            when (smsResult) {
                is SmsDispatchResult.DirectSmsSent -> {
                    _uiState.update {
                        it.copy(
                            sosState = SOSState.SENT_OFFLINE_SMS,
                            sosLocationLat = lat,
                            sosLocationLon = lon,
                            sosLocationName = sectorName
                        )
                    }
                }
                is SmsDispatchResult.IntentOpened -> {
                    _uiState.update {
                        it.copy(
                            sosState = SOSState.SENT_OFFLINE_INTENT,
                            sosLocationLat = lat,
                            sosLocationLon = lon,
                            sosLocationName = sectorName
                        )
                    }
                }
                is SmsDispatchResult.Failed -> {
                    _uiState.update {
                        it.copy(
                            sosState = SOSState.ERROR,
                            error = "SOS saved offline. SMS launch failed: ${smsResult.reason}"
                        )
                    }
                }
            }
        }
    }

    fun resetSOSState() {
        _uiState.update { it.copy(sosState = SOSState.IDLE) }
    }
}
