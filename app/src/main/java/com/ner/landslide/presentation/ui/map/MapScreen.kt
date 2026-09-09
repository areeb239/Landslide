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

// Key Eastern Himalayas Hazard Corridors
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
                            fillColor = zoneColor.copy(alpha = if (zone.severity == AlertSeverity.CRITICAL) 0.45f else 0.30f),
                            strokeColor = zoneColor,
                            strokeWidth = 3f,
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
                            width = 8f
                        )
                    }
                }
            }

            // Integrated Top Control Bar (Zero Floating Capsule Soup)
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .background(ObsidianBase)
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Top header line with title, beacon, and controls
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PulsingStatusDot(color = Primary80, size = 6.dp)
                        Text(
                            "GIS TACTICAL SURVEILLANCE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp,
                            color = OnBackgroundDark
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Terrain / Hybrid satellite switch
                        Surface(
                            onClick = {
                                mapType = if (mapType == MapType.TERRAIN) MapType.HYBRID else MapType.TERRAIN
                            },
                            shape = RoundedCornerShape(4.dp),
                            color = SurfaceVariantDark,
                            border = BorderStroke(1.dp, BorderSubtle)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (mapType == MapType.TERRAIN) Icons.Default.SatelliteAlt else Icons.Default.Landscape,
                                    contentDescription = "Map Style",
                                    tint = Primary80,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    if (mapType == MapType.TERRAIN) "TERRAIN" else "SATELLITE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnBackgroundDark
                                )
                            }
                        }
                    }
                }

                // Layer Toggles & Hotspot Filter Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Risk Polygons toggle
                    Surface(
                        onClick = { viewModel.toggleRiskLayer() },
                        shape = RoundedCornerShape(4.dp),
                        color = if (uiState.showRiskLayer) SurfaceVariantDark else SurfaceDark,
                        border = BorderStroke(1.dp, if (uiState.showRiskLayer) SeverityCritical.copy(alpha = 0.6f) else BorderSubtle)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (uiState.showRiskLayer) SeverityCritical else TextSubtle)
                            )
                            Text(
                                "Hazard Polygons",
                                fontSize = 10.sp,
                                fontWeight = if (uiState.showRiskLayer) FontWeight.Bold else FontWeight.Medium,
                                color = if (uiState.showRiskLayer) OnBackgroundDark else TextMuted
                            )
                        }
                    }

                    // Key Road Segments toggle
                    Surface(
                        onClick = { viewModel.toggleRoadLayer() },
                        shape = RoundedCornerShape(4.dp),
                        color = if (uiState.showRoadLayer) SurfaceVariantDark else SurfaceDark,
                        border = BorderStroke(1.dp, if (uiState.showRoadLayer) Primary80.copy(alpha = 0.6f) else BorderSubtle)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(if (uiState.showRoadLayer) Primary80 else TextSubtle)
                            )
                            Text(
                                "Corridors",
                                fontSize = 10.sp,
                                fontWeight = if (uiState.showRoadLayer) FontWeight.Bold else FontWeight.Medium,
                                color = if (uiState.showRoadLayer) OnBackgroundDark else TextMuted
                            )
                        }
                    }
                }

                // Hotspot Jump Chips
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
                            shape = RoundedCornerShape(4.dp),
                            color = SurfaceDark,
                            border = BorderStroke(1.dp, BorderSubtle)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Place, null, tint = TextMuted, modifier = Modifier.size(11.dp))
                                Text(name, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = TextMuted)
                            }
                        }
                    }
                }
            }

            // Bottom Minimal Status & Legend Dock (Restrained, Sits cleanly above Bottom Nav)
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                shape = RoundedCornerShape(8.dp),
                color = SurfaceDark.copy(alpha = 0.95f),
                border = BorderStroke(1.dp, BorderSubtle)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Primary80)
                        )
                        Text("8 SECTORS LIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Primary80)
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MapLegendPill(color = SeverityLow, label = "Nominal")
                        MapLegendPill(color = SeverityModerate, label = "Advisory")
                        MapLegendPill(color = SeverityCritical, label = "Hazard")
                    }
                }
            }
        }
    }
}

@Composable
private fun MapLegendPill(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(color)
        )
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
