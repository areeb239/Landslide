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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import android.webkit.WebView
import com.ner.landslide.domain.model.*
import com.ner.landslide.presentation.ui.components.*
import com.ner.landslide.presentation.ui.theme.*
import com.ner.landslide.presentation.viewmodel.MapViewModel
import java.util.Locale

// 8 Verified Regional Himalayan Monitoring Hotspots
private val REGION_HOTSPOTS = listOf(
    Pair("Mangan North", LatLng(27.52, 88.54)),
    Pair("Dzongu Valley", LatLng(27.48, 88.48)),
    Pair("Sevoke (NH-10)", LatLng(26.89, 88.47)),
    Pair("Gangtok Ridge", LatLng(27.33, 88.61)),
    Pair("Haflong Slopes", LatLng(25.17, 93.02)),
    Pair("Kohima Bypass", LatLng(25.66, 94.10)),
    Pair("Guwahati Hills", LatLng(26.14, 91.73)),
    Pair("Tawang Pass", LatLng(27.58, 91.87))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(viewModel: MapViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = BhurakshakTheme.colors

    var mapMode by remember { mutableStateOf("terrain") }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            LoadingContent()
        } else {
            TacticalGisMapView(
                modifier = Modifier.fillMaxSize(),
                selectedLocation = uiState.selectedLocation,
                riskZones = uiState.riskZones,
                roadSegments = uiState.roadSegments,
                showRiskLayer = uiState.showRiskLayer,
                showRoadLayer = uiState.showRoadLayer,
                mapMode = mapMode,
                onZoneClick = { zone -> viewModel.selectZone(zone) },
                onRoadClick = { road -> viewModel.selectRoad(road) },
                onMapClick = {
                    viewModel.clearSelection()
                    isSearchExpanded = false
                },
                onWebViewReady = { wv ->
                    webViewRef = wv
                }
            )

            // Top Tactical Control Bar & Search
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(colors.bgBase)
                    .statusBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header line: Title, 3-Way Map Mode, Center Active Sector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PulsingStatusDot(color = colors.accent, size = 6.dp)
                        Text(
                            "GIS TACTICAL SURVEILLANCE",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp,
                            color = colors.textPrimary
                        )
                    }

                    // 3-Way Map Mode Switcher: TERRAIN, STANDARD, SATELLITE
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = colors.bgSurface,
                        border = BorderStroke(1.dp, colors.borderDefault)
                    ) {
                        Row(
                            modifier = Modifier.padding(2.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MapModeButton(
                                label = "Terrain",
                                isSelected = mapMode == "terrain",
                                onClick = { mapMode = "terrain" }
                            )
                            MapModeButton(
                                label = "Standard",
                                isSelected = mapMode == "standard",
                                onClick = { mapMode = "standard" }
                            )
                            MapModeButton(
                                label = "Satellite",
                                isSelected = mapMode == "satellite",
                                onClick = { mapMode = "satellite" }
                            )
                        }
                    }
                }

                // Layer Toggles & Location Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Hazard Polygons Layer Toggle
                        Surface(
                            onClick = { viewModel.toggleRiskLayer() },
                            shape = RoundedCornerShape(6.dp),
                            color = if (uiState.showRiskLayer) colors.accent else colors.bgSurface,
                            border = BorderStroke(1.dp, if (uiState.showRiskLayer) colors.accent else colors.borderDefault)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (uiState.showRiskLayer) Color.White else colors.textSecondary)
                                )
                                Text(
                                    "Hazard Zones",
                                    fontSize = 10.sp,
                                    fontWeight = if (uiState.showRiskLayer) FontWeight.Bold else FontWeight.Medium,
                                    color = if (uiState.showRiskLayer) Color.White else colors.textSecondary
                                )
                            }
                        }

                        // Evacuation Corridors Layer Toggle (Clear Labeling!)
                        Surface(
                            onClick = { viewModel.toggleRoadLayer() },
                            shape = RoundedCornerShape(6.dp),
                            color = if (uiState.showRoadLayer) colors.accent else colors.bgSurface,
                            border = BorderStroke(1.dp, if (uiState.showRoadLayer) colors.accent else colors.borderDefault)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (uiState.showRoadLayer) Color.White else colors.textSecondary)
                                )
                                Text(
                                    "Evac Corridors",
                                    fontSize = 10.sp,
                                    fontWeight = if (uiState.showRoadLayer) FontWeight.Bold else FontWeight.Medium,
                                    color = if (uiState.showRoadLayer) Color.White else colors.textSecondary
                                )
                            }
                        }

                        // Toggle Search Bar Button
                        Surface(
                            onClick = { isSearchExpanded = !isSearchExpanded },
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSearchExpanded) colors.accent.copy(alpha = 0.15f) else colors.bgSurface,
                            border = BorderStroke(1.dp, if (isSearchExpanded) colors.accent else colors.borderDefault)
                        ) {
                            Box(modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Search Location",
                                    tint = if (isSearchExpanded) colors.accent else colors.textSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    // Active Sector Chip (Clean formatting without slicing mid-word)
                    uiState.selectedLocation?.let { activeLoc ->
                        Surface(
                            onClick = {
                                webViewRef?.evaluateJavascript(
                                    "setCenter(${activeLoc.latitude}, ${activeLoc.longitude}, 12);",
                                    null
                                )
                            },
                            shape = RoundedCornerShape(6.dp),
                            color = colors.accent.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, colors.accent.copy(alpha = 0.5f)),
                            modifier = Modifier.widthIn(max = 140.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = null,
                                    tint = colors.accent,
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = activeLoc.name,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.accent,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                // Search Bar Input Dropdown
                AnimatedVisibility(
                    visible = isSearchExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                viewModel.searchLocations(it)
                            },
                            placeholder = { Text("Jump to city, town or landmark...", fontSize = 11.sp) },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(16.dp)) },
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Clear, null, modifier = Modifier.size(14.dp))
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp)
                        )

                        if (uiState.searchResults.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = colors.bgSurface,
                                border = BorderStroke(1.dp, colors.borderDefault),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(4.dp)) {
                                    uiState.searchResults.take(4).forEach { res ->
                                        Surface(
                                            onClick = {
                                                viewModel.selectCustomLocation(res)
                                                webViewRef?.evaluateJavascript(
                                                    "setCenter(${res.latitude}, ${res.longitude}, 12);",
                                                    null
                                                )
                                                isSearchExpanded = false
                                                searchQuery = ""
                                            },
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color.Transparent,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(Icons.Default.Place, null, tint = colors.accent, modifier = Modifier.size(13.dp))
                                                Text(
                                                    text = "${res.name}, ${res.state}",
                                                    fontSize = 11.sp,
                                                    color = colors.textPrimary,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Sector Jump Chips Row with Edge Gradient Fade & Indicators
                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        REGION_HOTSPOTS.forEach { (name, coords) ->
                            val matchingZone = uiState.riskZones.find {
                                val zLat = it.polygonPoints.firstOrNull()?.latitude ?: 0.0
                                val zLon = it.polygonPoints.firstOrNull()?.longitude ?: 0.0
                                kotlin.math.hypot(zLat - coords.latitude, zLon - coords.longitude) < 0.25
                            }

                            val badgeColor = when (matchingZone?.severity) {
                                AlertSeverity.CRITICAL -> colors.critical
                                AlertSeverity.HIGH -> Color(0xFFF97316)
                                AlertSeverity.MODERATE -> colors.warning
                                AlertSeverity.LOW -> colors.success
                                null -> colors.textSecondary
                            }

                            val isSelected = uiState.selectedZone?.id == matchingZone?.id && matchingZone != null

                            Surface(
                                onClick = {
                                    webViewRef?.evaluateJavascript(
                                        "setCenter(${coords.latitude}, ${coords.longitude}, 12);",
                                        null
                                    )
                                    if (matchingZone != null) {
                                        viewModel.selectZone(matchingZone)
                                    }
                                },
                                shape = RoundedCornerShape(5.dp),
                                color = if (isSelected) badgeColor.copy(alpha = 0.2f) else colors.bgSurface,
                                border = BorderStroke(1.dp, if (isSelected) badgeColor else colors.borderDefault),
                                shadowElevation = if (colors.isDark) 0.dp else 1.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(badgeColor)
                                    )
                                    Text(
                                        text = name,
                                        fontSize = 10.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) colors.textPrimary else colors.textSecondary
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                    }

                    // Trailing Edge Fade & Arrow Hint
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .width(28.dp)
                            .matchParentSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color.Transparent, colors.bgBase)
                                )
                            ),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "Scroll Sectors",
                            tint = colors.textSecondary.copy(alpha = 0.6f),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            // Interactive Floating Map Controls: Recenter GPS & Zoom +/-
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
                    .width(IntrinsicSize.Min),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Recenter on GPS FAB
                Surface(
                    onClick = {
                        viewModel.switchToCurrentGpsLocation()
                        uiState.selectedLocation?.let { activeLoc ->
                            webViewRef?.evaluateJavascript(
                                "setCenter(${activeLoc.latitude}, ${activeLoc.longitude}, 12);",
                                null
                            )
                        }
                    },
                    shape = CircleShape,
                    color = colors.bgSurface,
                    border = BorderStroke(1.2.dp, colors.accent),
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.GpsFixed,
                            contentDescription = "Recenter GPS",
                            tint = colors.accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(Modifier.height(2.dp))

                // Zoom Controls (+ / -)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = colors.bgSurface,
                    border = BorderStroke(1.dp, colors.borderDefault),
                    shadowElevation = 3.dp,
                    modifier = Modifier.width(36.dp)
                ) {
                    Column(
                        modifier = Modifier.width(36.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        IconButton(
                            onClick = {
                                webViewRef?.evaluateJavascript("zoomIn();", null)
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Add, "Zoom In", tint = colors.textPrimary, modifier = Modifier.size(18.dp))
                        }
                        HorizontalDivider(color = colors.borderDefault, thickness = 0.8.dp)
                        IconButton(
                            onClick = {
                                webViewRef?.evaluateJavascript("zoomOut();", null)
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.Remove, "Zoom Out", tint = colors.textPrimary, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            // Interactive Zone Detail Card (Slides up on polygon tap)
            AnimatedVisibility(
                visible = uiState.selectedZone != null,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 14.dp)
                    .padding(bottom = 58.dp)
            ) {
                uiState.selectedZone?.let { zone ->
                    val zoneColor = when (zone.severity) {
                        AlertSeverity.CRITICAL -> colors.critical
                        AlertSeverity.HIGH -> Color(0xFFF97316)
                        AlertSeverity.MODERATE -> colors.warning
                        AlertSeverity.LOW -> colors.success
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.bgSurface,
                        border = BorderStroke(1.2.dp, zoneColor),
                        shadowElevation = 6.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = zoneColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${zone.severity.name} HAZARD ZONE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = zoneColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Text(
                                    text = String.format(Locale.US, "%.0f%% Risk Probability", zone.riskProbability * 100),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = zoneColor
                                )

                                IconButton(
                                    onClick = { viewModel.clearSelection() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp), tint = colors.textSecondary)
                                }
                            }

                            Text(
                                text = zone.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Sector: ${zone.district}",
                                    fontSize = 10.5.sp,
                                    color = colors.textSecondary
                                )
                                Text(
                                    text = "Hazard Area: ${zone.affectedAreaKm2} km²",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                            }

                            if (zone.recommendation.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = colors.bgBase,
                                    border = BorderStroke(0.8.dp, colors.borderDefault),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Directive: ${zone.recommendation}",
                                        fontSize = 10.5.sp,
                                        color = colors.textPrimary,
                                        lineHeight = 14.sp,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    zone.polygonPoints.firstOrNull()?.let { pt ->
                                        webViewRef?.evaluateJavascript(
                                            "setCenter(${pt.latitude}, ${pt.longitude}, 13);",
                                            null
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = zoneColor),
                                modifier = Modifier.fillMaxWidth().height(34.dp)
                            ) {
                                Text("Focus Sector Zoom", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }

            // Interactive Corridor Detail Card (Slides up on road tap)
            AnimatedVisibility(
                visible = uiState.selectedRoad != null,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 14.dp)
                    .padding(bottom = 58.dp)
            ) {
                uiState.selectedRoad?.let { road ->
                    val roadColor = when (road.status) {
                        RoadStatus.OPEN -> colors.success
                        RoadStatus.BLOCKED -> colors.critical
                        RoadStatus.PARTIALLY_BLOCKED -> Color(0xFFF59E0B)
                        RoadStatus.UNKNOWN -> colors.textSecondary
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.bgSurface,
                        border = BorderStroke(1.2.dp, roadColor),
                        shadowElevation = 6.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = roadColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "${road.routeCode.ifBlank { "CORRIDOR" }} — ${road.status.name}",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = roadColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.clearSelection() },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp), tint = colors.textSecondary)
                                }
                            }

                            Text(
                                text = road.name,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )

                            Text(
                                text = "Status: ${road.blockageReason}",
                                fontSize = 11.sp,
                                color = colors.textPrimary
                            )

                            if (road.alternateRoute.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = colors.bgBase,
                                    border = BorderStroke(0.8.dp, colors.borderDefault),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Detour: ${road.alternateRoute}",
                                        fontSize = 10.5.sp,
                                        color = colors.accent,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Minimal Status & Dynamic Legend Dock
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                shape = RoundedCornerShape(8.dp),
                color = colors.bgSurface.copy(alpha = 0.96f),
                border = BorderStroke(1.dp, colors.borderDefault),
                shadowElevation = if (colors.isDark) 0.dp else 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val totalCount = uiState.riskZones.size
                    val hazardCount = uiState.riskZones.count { it.severity == AlertSeverity.CRITICAL || it.severity == AlertSeverity.HIGH }
                    val advisoryCount = uiState.riskZones.count { it.severity == AlertSeverity.MODERATE }
                    val nominalCount = uiState.riskZones.count { it.severity == AlertSeverity.LOW }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(colors.accent)
                        )
                        Text(
                            text = "$totalCount SECTORS MONITORED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MapLegendPill(color = colors.success, label = "Nominal ($nominalCount)", textColor = colors.textSecondary)
                        MapLegendPill(color = colors.warning, label = "Advisory ($advisoryCount)", textColor = colors.textSecondary)
                        MapLegendPill(color = colors.critical, label = "Hazard ($hazardCount)", textColor = colors.textSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun MapModeButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = BhurakshakTheme.colors
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(4.dp),
        color = if (isSelected) colors.accent else Color.Transparent
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else colors.textSecondary,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun MapLegendPill(color: Color, label: String, textColor: Color) {
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
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
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
