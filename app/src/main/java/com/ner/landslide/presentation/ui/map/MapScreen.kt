package com.ner.landslide.presentation.ui.map

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.ner.landslide.domain.model.*
import com.ner.landslide.presentation.ui.components.*
import com.ner.landslide.presentation.ui.theme.*
import com.ner.landslide.presentation.viewmodel.MapViewModel
import kotlinx.coroutines.launch

// Key Eastern Himalayas Hazard Sectors
private val REGION_HOTSPOTS = listOf(
    Pair("Sevoke (NH-10)", LatLng(26.89, 88.46)),
    Pair("Gangtok Ridge", LatLng(27.33, 88.61)),
    Pair("Mangan (North)", LatLng(27.50, 88.53)),
    Pair("Guwahati (Assam)", LatLng(26.14, 91.74)),
    Pair("Dzongu Sector", LatLng(27.48, 88.49))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(viewModel: MapViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    var mapType by remember { mutableStateOf(MapType.TERRAIN) }
    var selectedHotspot by remember { mutableStateOf<String?>(null) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(26.95, 88.85), 7.8f)
    }

    Box(modifier = Modifier.fillMaxSize().background(ObsidianBase)) {
        if (uiState.isLoading) {
            LoadingContent()
        } else {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    mapType = mapType,
                    isMyLocationEnabled = false
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    myLocationButtonEnabled = false,
                    compassEnabled = true
                )
            ) {
                // Risk Zone Polygons
                if (uiState.showRiskLayer) {
                    uiState.riskZones.forEach { zone ->
                        val zoneColor = zone.severity.toMapColor()
                        Polygon(
                            points = zone.polygonPoints.map { LatLng(it.latitude, it.longitude) },
                            fillColor = zoneColor.copy(alpha = 0.38f),
                            strokeColor = zoneColor,
                            strokeWidth = 4f,
                            clickable = true,
                            onClick = {
                                selectedHotspot = zone.district
                            }
                        )
                    }
                }

                // Road Segments
                if (uiState.showRoadLayer) {
                    uiState.roadSegments.forEach { segment ->
                        Polyline(
                            points = segment.points.map { LatLng(it.latitude, it.longitude) },
                            color = segment.status.toColor(),
                            width = 10f
                        )
                    }
                }
            }

            // Top Floating Controls Bar
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Frosted Glass HUD Bar
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = SurfaceDark.copy(alpha = 0.88f),
                    borderColor = Color.White.copy(alpha = 0.12f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Telemetry Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PulsingStatusDot(color = Primary80, size = 6.dp)
                            Text(
                                "HIMALAYAN GIS HUD",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.8.sp,
                                color = OnBackgroundDark
                            )
                        }

                        // Layer Toggles
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Risk Zones toggle
                            FilterChip(
                                selected = uiState.showRiskLayer,
                                onClick = { viewModel.toggleRiskLayer() },
                                label = { Text("Risks", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Warning,
                                        null,
                                        modifier = Modifier.size(12.dp),
                                        tint = if (uiState.showRiskLayer) SeverityCritical else TextMuted
                                    )
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SeverityCritical.copy(alpha = 0.2f),
                                    selectedLabelColor = SeverityCritical
                                )
                            )

                            // Roads toggle
                            FilterChip(
                                selected = uiState.showRoadLayer,
                                onClick = { viewModel.toggleRoadLayer() },
                                label = { Text("Roads", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Route,
                                        null,
                                        modifier = Modifier.size(12.dp),
                                        tint = if (uiState.showRoadLayer) Primary80 else TextMuted
                                    )
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Primary80.copy(alpha = 0.2f),
                                    selectedLabelColor = Primary80
                                )
                            )

                            // Map Style Toggle
                            IconButton(
                                onClick = {
                                    mapType = if (mapType == MapType.TERRAIN) MapType.HYBRID else MapType.TERRAIN
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceElevated)
                            ) {
                                Icon(
                                    imageVector = if (mapType == MapType.TERRAIN) Icons.Default.SatelliteAlt else Icons.Default.Landscape,
                                    contentDescription = "Map Style",
                                    tint = CyberCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Horizontal Hotspot Fly-to Chips
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    REGION_HOTSPOTS.forEach { (name, coords) ->
                        Surface(
                            onClick = {
                                coroutineScope.launch {
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngZoom(coords, 11.5f),
                                        1200
                                    )
                                }
                            },
                            shape = RoundedCornerShape(20.dp),
                            color = SurfaceDark.copy(alpha = 0.85f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.MyLocation, null, tint = Primary80, modifier = Modifier.size(12.dp))
                                Text(name, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = OnBackgroundDark)
                            }
                        }
                    }
                }
            }

            // Bottom Corridor Status & Legend Card
            GlassCard(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 14.dp, vertical = 16.dp)
                    .fillMaxWidth(),
                backgroundColor = SurfaceDark.copy(alpha = 0.92f),
                borderColor = Color.White.copy(alpha = 0.1f)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "LIVE CORRIDOR TELEMETRY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp,
                            color = TextMuted
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            PulsingStatusDot(color = Primary80, size = 5.dp)
                            Text("8 SECTORS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Primary80)
                        }
                    }

                    // Compact Hazard Legend Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MapLegendPill(color = SeverityLow, label = "Nominal")
                        MapLegendPill(color = SeverityModerate, label = "Advisory")
                        MapLegendPill(color = SeverityHigh, label = "Warning")
                        MapLegendPill(color = SeverityCritical, label = "Evacuate")
                        MapLegendPill(color = SeverityCritical, label = "Blocked Road", isRoad = true)
                    }
                }
            }
        }
    }
}

@Composable
private fun MapLegendPill(color: Color, label: String, isRoad: Boolean = false) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (isRoad) {
            Box(
                modifier = Modifier
                    .size(16.dp, 3.dp)
                    .background(color, RoundedCornerShape(2.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = TextMuted
        )
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

