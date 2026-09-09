package com.ner.landslide.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ner.landslide.domain.model.*
import com.ner.landslide.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MapUiState(
    val riskZones: List<RiskZone> = emptyList(),
    val roadSegments: List<RoadSegment> = emptyList(),
    val isLoading: Boolean = true,
    val showRiskLayer: Boolean = true,
    val showRoadLayer: Boolean = true
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val getRiskZones: GetRiskZonesUseCase,
    private val getRoadSegments: GetRoadSegmentsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                getRiskZones(),
                getRoadSegments()
            ) { zones, roads ->
                _uiState.update { it.copy(riskZones = zones, roadSegments = roads, isLoading = false) }
            }.collect()
        }
    }

    fun toggleRiskLayer() = _uiState.update { it.copy(showRiskLayer = !it.showRiskLayer) }
    fun toggleRoadLayer() = _uiState.update { it.copy(showRoadLayer = !it.showRoadLayer) }
}
