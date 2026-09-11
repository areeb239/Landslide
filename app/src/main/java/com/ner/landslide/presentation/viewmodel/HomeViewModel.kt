package com.ner.landslide.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ner.landslide.domain.model.*
import com.ner.landslide.domain.usecase.*
import com.ner.landslide.util.LocationHelper
import com.ner.landslide.util.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val alerts: List<Alert> = emptyList(),
    val currentUser: User? = null,
    val isLoading: Boolean = true,
    val isOnline: Boolean = true,
    val lastSyncedTime: Long = System.currentTimeMillis(),
    val sosLocationLat: Double = 27.1765,
    val sosLocationLon: Double = 88.5321,
    val sosState: SOSState = SOSState.IDLE,
    val error: String? = null
)

enum class SOSState { IDLE, COUNTDOWN, SENDING, SENT, ERROR }

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAlerts: GetActiveAlertsUseCase,
    private val triggerSOS: TriggerSOSUseCase,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val locationHelper: LocationHelper,
    private val networkMonitor: NetworkMonitor,
    private val auth: FirebaseAuth
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
                }
            }
        }
    }

    fun prefetchSOSLocation() {
        viewModelScope.launch {
            val loc = locationHelper.getCurrentLocation()
            if (loc != null) {
                _uiState.update {
                    it.copy(
                        sosLocationLat = loc.latitude,
                        sosLocationLon = loc.longitude
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
            val user = _uiState.value.currentUser
            val sos = SOSAlert(
                uid = auth.currentUser?.uid ?: return@launch,
                name = user?.name ?: "Citizen",
                latitude = location?.latitude ?: _uiState.value.sosLocationLat,
                longitude = location?.longitude ?: _uiState.value.sosLocationLon
            )
            triggerSOS(sos)
                .onSuccess { _uiState.update { it.copy(sosState = SOSState.SENT) } }
                .onFailure { _uiState.update { it.copy(sosState = SOSState.ERROR) } }
        }
    }

    fun resetSOSState() {
        _uiState.update { it.copy(sosState = SOSState.IDLE) }
    }
}
