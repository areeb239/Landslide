package com.ner.landslide.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ner.landslide.domain.model.*
import com.ner.landslide.domain.usecase.*
import com.ner.landslide.service.SyncWorker
import com.ner.landslide.util.EmergencySmsHelper
import com.ner.landslide.util.LocationHelper
import com.ner.landslide.util.NetworkMonitor
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
    val error: String? = null
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
        prefetchSOSLocation()
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
                }
            }
        }
    }

    fun prefetchSOSLocation() {
        viewModelScope.launch {
            val loc = locationHelper.getCurrentLocation()
            if (loc != null) {
                val resolved = locationHelper.reverseGeocode(loc.latitude, loc.longitude)
                _uiState.update {
                    it.copy(
                        sosLocationLat = loc.latitude,
                        sosLocationLon = loc.longitude,
                        sosLocationName = resolved.formattedHeadline
                    )
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
            val sectorName = resolved.formattedHeadline.ifBlank { _uiState.value.sosLocationName }

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
