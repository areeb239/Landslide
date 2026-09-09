package com.ner.landslide.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.ner.landslide.domain.model.*
import com.ner.landslide.domain.usecase.*
import com.ner.landslide.util.LocationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val alerts: List<Alert> = emptyList(),
    val currentUser: User? = null,
    val isLoading: Boolean = true,
    val sosState: SOSState = SOSState.IDLE,
    val error: String? = null
)

enum class SOSState { IDLE, SENDING, SENT, ERROR }

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAlerts: GetActiveAlertsUseCase,
    private val triggerSOS: TriggerSOSUseCase,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val locationHelper: LocationHelper,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadUser()
        observeAlerts()
    }

    private fun loadUser() {
        viewModelScope.launch {
            val user = getCurrentUser()
            _uiState.update { it.copy(currentUser = user) }
        }
    }

    private fun observeAlerts() {
        viewModelScope.launch {
            getAlerts().collect { alerts ->
                _uiState.update { it.copy(alerts = alerts, isLoading = false) }
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
                name = user?.name ?: "Unknown",
                latitude = location?.latitude ?: 0.0,
                longitude = location?.longitude ?: 0.0
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
