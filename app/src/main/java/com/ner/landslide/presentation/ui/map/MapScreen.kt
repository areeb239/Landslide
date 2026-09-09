package com.ner.landslide.presentation.ui.map

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.ner.landslide.domain.model.*
import com.ner.landslide.presentation.ui.components.LoadingContent
import com.ner.landslide.presentation.ui.theme.*
import com.ner.landslide.presentation.viewmodel.MapViewModel

// Default centre of North Eastern Region (approx Guwahati, Assam)
private val NER_CENTER = LatLng(26.14, 91.74)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(viewModel: MapViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(NER_CENTER, 7f)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            LoadingContent()
        } else {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(mapType = MapType.TERRAIN, isMyLocationEnabled = false),
                uiSettings = MapUiSettings(zoomControlsEnabled = true, myLocationButtonEnabled = false)
            ) {
                // Risk Zone Polygons
                if (uiState.showRiskLayer) {
                    uiState.riskZones.forEach { zone ->
                        val fillColor = zone.severity.toMapColor().copy(alpha = 0.35f)
                        val strokeColor = zone.severity.toMapColor()
                        Polygon(
                            points = zone.polygonPoints.map { LatLng(it.latitude, it.longitude) },
                            fillColor = fillColor,
                            strokeColor = strokeColor,
                            strokeWidth = 3f,
                            clickable = true,
                            onClick = { /* show zone info bottom sheet */ }
                        )
                    }
                }

                // Road Segments
                if (uiState.showRoadLayer) {
                    uiState.roadSegments.forEach { segment ->
                        Polyline(
                            points = segment.points.map { LatLng(it.latitude, it.longitude) },
                            color = segment.status.toColor(),
                            width = 8f
                        )
                    }
                }
            }

            // Layer Toggle Controls (top right)
            Card(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Layers",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                    )
                    LayerToggleRow(
                        label = "Risk Zones",
                        icon = Icons.Default.Warning,
                        color = SeverityCritical,
                        checked = uiState.showRiskLayer,
                        onToggle = { viewModel.toggleRiskLayer() }
                    )
                    LayerToggleRow(
                        label = "Roads",
                        icon = Icons.Default.Directions,
                        color = Primary80,
                        checked = uiState.showRoadLayer,
                        onToggle = { viewModel.toggleRoadLayer() }
                    )
                }
            }

            // Legend (bottom left)
            Card(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Risk Level", style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
                    LegendItem(color = SeverityLow, label = "Low")
                    LegendItem(color = SeverityModerate, label = "Moderate")
                    LegendItem(color = SeverityHigh, label = "High")
                    LegendItem(color = SeverityCritical, label = "Critical")
                    Divider(color = MaterialTheme.colorScheme.onSurface.copy(0.1f))
                    LegendItem(color = SeverityLow, label = "Road: Open", isRoad = true)
                    LegendItem(color = SeverityCritical, label = "Road: Blocked", isRoad = true)
                }
            }
        }
    }
}

@Composable
private fun LayerToggleRow(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.width(72.dp))
        Switch(checked = checked, onCheckedChange = { onToggle() },
            modifier = Modifier.size(32.dp, 18.dp))
    }
}

@Composable
private fun LegendItem(color: Color, label: String, isRoad: Boolean = false) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (isRoad) {
            Surface(modifier = Modifier.size(24.dp, 4.dp), color = color) {}
        } else {
            Surface(modifier = Modifier.size(14.dp),
                shape = RoundedCornerShape(3.dp),
                color = color.copy(0.3f),
                border = androidx.compose.foundation.BorderStroke(1.dp, color)) {}
        }
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(0.8f))
    }
}

// Color mappings for map overlays
fun AlertSeverity.toMapColor(): Color = when (this) {
    AlertSeverity.LOW -> SeverityLow
    AlertSeverity.MODERATE -> SeverityModerate
    AlertSeverity.HIGH -> SeverityHigh
    AlertSeverity.CRITICAL -> SeverityCritical
}

fun RoadStatus.toColor(): Color = when (this) {
    RoadStatus.OPEN -> SeverityLow
    RoadStatus.BLOCKED -> SeverityCritical
    RoadStatus.PARTIALLY_BLOCKED -> SeverityHigh
    RoadStatus.UNKNOWN -> Color.Gray
}
