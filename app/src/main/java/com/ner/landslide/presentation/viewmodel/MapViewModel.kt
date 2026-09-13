package com.ner.landslide.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ner.landslide.domain.model.*
import com.ner.landslide.domain.usecase.*
import com.ner.landslide.util.AppLocationManager
import com.ner.landslide.util.LocationSearchResult
import com.ner.landslide.util.SelectedLocation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MapUiState(
    val riskZones: List<RiskZone> = emptyList(),
    val roadSegments: List<RoadSegment> = emptyList(),
    val selectedLocation: SelectedLocation? = null,
    val selectedZone: RiskZone? = null,
    val selectedRoad: RoadSegment? = null,
    val isLoading: Boolean = true,
    val showRiskLayer: Boolean = true,
    val showRoadLayer: Boolean = true,
    val isSearching: Boolean = false,
    val searchResults: List<LocationSearchResult> = emptyList()
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getRiskZones: GetRiskZonesUseCase,
    private val getRoadSegments: GetRoadSegmentsUseCase,
    private val appLocationManager: AppLocationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState(selectedLocation = appLocationManager.selectedLocation.value))
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        // Observe app-wide single source of truth for location
        viewModelScope.launch {
            appLocationManager.selectedLocation.collect { loc ->
                _uiState.update { it.copy(selectedLocation = loc) }
            }
        }
        viewModelScope.launch {
            combine(
                getRiskZones(),
                getRoadSegments()
            ) { zones, roads ->
                _uiState.update { it.copy(riskZones = zones, roadSegments = roads, isLoading = false) }
            }.collect()
        }
    }

    fun selectZone(zone: RiskZone?) {
        _uiState.update { it.copy(selectedZone = zone, selectedRoad = null) }
    }

    fun selectRoad(road: RoadSegment?) {
        _uiState.update { it.copy(selectedRoad = road, selectedZone = null) }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedZone = null, selectedRoad = null) }
    }

    fun searchLocations(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            val results = appLocationManager.searchLocations(query)
            _uiState.update { it.copy(searchResults = results, isSearching = false) }
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
        _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
    }

    fun switchToCurrentGpsLocation() {
        viewModelScope.launch {
            appLocationManager.switchToCurrentGpsLocation()
        }
    }

    fun toggleRiskLayer() = _uiState.update { it.copy(showRiskLayer = !it.showRiskLayer) }
    fun toggleRoadLayer() = _uiState.update { it.copy(showRoadLayer = !it.showRoadLayer) }
}

