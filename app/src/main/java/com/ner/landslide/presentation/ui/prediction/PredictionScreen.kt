package com.ner.landslide.presentation.ui.prediction

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ner.landslide.domain.model.PredictionResult
import com.ner.landslide.domain.model.ModelConstants
import com.ner.landslide.presentation.ui.components.*
import com.ner.landslide.presentation.ui.theme.*
import com.ner.landslide.presentation.viewmodel.PredictionViewModel

data class CityPreset(
    val id: String,
    val name: String,
    val state: String,
    val lat: Double,
    val lon: Double
)

private val HIMALAYAN_CITIES = listOf(
    CityPreset("gangtok", "Gangtok (Urban)", "Sikkim", 27.33, 88.61),
    CityPreset("shillong", "Shillong (Urban)", "Meghalaya", 25.57, 91.89),
    CityPreset("guwahati", "Guwahati (Built-up)", "Assam", 26.14, 91.74),
    CityPreset("aizawl", "Aizawl (Urban)", "Mizoram", 23.73, 92.71),
    CityPreset("kohima", "Kohima (Urban)", "Nagaland", 25.67, 94.11),
    CityPreset("itanagar", "Itanagar (Urban)", "Arunachal", 27.08, 93.60),
    CityPreset("darjeeling", "Darjeeling (Tea Hills)", "WB/Sikkim", 27.04, 88.26),
    CityPreset("kaziranga", "Kaziranga Buffer", "Assam Forest • 0.3% Low", 26.58, 93.17),
    CityPreset("majuli", "Majuli Plain", "Assam Farmland • 0.4% Low", 26.95, 94.22),
    CityPreset("mawphlang", "Mawphlang Forest", "Meghalaya • 31% Mod", 25.45, 91.75)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionScreen(
    onNavigateBack: () -> Unit,
    viewModel: PredictionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = BhurakshakTheme.colors
    val strings = LocalAppStrings.current
    var selectedScenario by remember { mutableStateOf<String?>("Monsoon Cloudburst") }

    Scaffold(
        containerColor = colors.bgBase,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = strings.predictionScreenTitle,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = strings.predictionScreenSub,
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.accent
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.textPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.bgBase
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.bgBase)
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Location & Satellite Telemetry Intelligence Card
            FieldCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = colors.accent, modifier = Modifier.size(20.dp))
                            Column {
                                Text(
                                    text = "LOCATION & SATELLITE TELEMETRY",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp,
                                    color = colors.accent
                                )
                                uiState.locationName?.let { loc ->
                                    Text(
                                        text = loc,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary
                                    )
                                }
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = colors.accent.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, colors.accent.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "AUTO-EXTRACTION",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.accent,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // City Presets horizontal scroll
                    Text(
                        text = "Regional Presets (Instant Cached Load):",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HIMALAYAN_CITIES.forEach { city ->
                            val isSelected = uiState.selectedPreset == city.id
                            Surface(
                                onClick = {
                                    selectedScenario = null
                                    viewModel.selectPreset(city.id, city.lat, city.lon, "${city.name} (${city.state})")
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) colors.accent else colors.bgSurface,
                                border = BorderStroke(1.dp, if (isSelected) colors.accent else colors.borderDefault)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Text(
                                        text = city.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else colors.textPrimary
                                    )
                                    Text(
                                        text = city.state,
                                        fontSize = 9.sp,
                                        color = if (isSelected) Color.White.copy(alpha = 0.8f) else colors.textSecondary
                                    )
                                }
                            }
                        }
                    }

                    // Coordinates Header & GPS Action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GEOGRAPHIC COORDINATES",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.sp,
                            letterSpacing = 0.5.sp,
                            color = colors.textSecondary
                        )
                        TextButton(
                            onClick = { viewModel.useCurrentLocation() },
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(13.dp), tint = colors.accent)
                            Spacer(Modifier.width(4.dp))
                            Text("Use Current GPS", fontSize = 11.sp, color = colors.accent, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Coordinates Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            PredictionInputField(
                                label = "Latitude (°N)",
                                unit = "decimal",
                                icon = Icons.Default.Place,
                                iconColor = colors.accent,
                                value = uiState.latitude,
                                onValueChange = {
                                    selectedScenario = null
                                    viewModel.onLatitudeChange(it)
                                },
                                placeholder = "27.33"
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            PredictionInputField(
                                label = "Longitude (°E)",
                                unit = "decimal",
                                icon = Icons.Default.Explore,
                                iconColor = colors.accent,
                                value = uiState.longitude,
                                onValueChange = {
                                    selectedScenario = null
                                    viewModel.onLongitudeChange(it)
                                },
                                placeholder = "88.61"
                            )
                        }
                    }

                    // Fetch Telemetry Button
                    Button(
                        onClick = { viewModel.fetchTelemetry() },
                        enabled = !uiState.isExtractingFeatures,
                        modifier = Modifier.fillMaxWidth().height(42.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.bgSurface,
                            contentColor = colors.accent
                        ),
                        border = BorderStroke(1.dp, colors.accent.copy(alpha = 0.5f))
                    ) {
                        if (uiState.isExtractingFeatures) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = colors.accent
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Extracting GIS Telemetry...", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        } else {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Fetch Satellite Telemetry (SRTM / GLiM)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    uiState.telemetryMessage?.let { msg ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = colors.success.copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, colors.success.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = colors.success, modifier = Modifier.size(14.dp))
                                Text(text = msg, fontSize = 11.sp, color = colors.success, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            // Scenario Quick Presets
            SectionHeader(
                title = strings.geotechnicalScenarios,
                subtitle = strings.scenarioSub
            )

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScenarioChip(
                    title = strings.scenarioCloudburst,
                    accent = colors.critical,
                    isSelected = selectedScenario == "Monsoon Cloudburst",
                    onClick = {
                        selectedScenario = "Monsoon Cloudburst"
                        viewModel.onRainfall1dChange("185")
                        viewModel.onSlopeChange("42")
                        viewModel.onRainfall3dChange("320")
                        viewModel.onRainfall7dChange("480")
                        viewModel.onElevationChange("1850")
                        viewModel.onLithologyChange("Metamorphic rocks")
                        viewModel.onLandCoverChange("Tree cover")
                    }
                )
                ScenarioChip(
                    title = strings.scenarioModerate,
                    accent = colors.warning,
                    isSelected = selectedScenario == "Moderate Hill Shower",
                    onClick = {
                        selectedScenario = "Moderate Hill Shower"
                        viewModel.onRainfall1dChange("55")
                        viewModel.onSlopeChange("28")
                        viewModel.onRainfall3dChange("95")
                        viewModel.onRainfall7dChange("160")
                        viewModel.onElevationChange("1400")
                        viewModel.onLithologyChange("Siliciclastic sedimentary rocks")
                        viewModel.onLandCoverChange("Grassland")
                    }
                )
                ScenarioChip(
                    title = strings.scenarioDry,
                    accent = colors.accent,
                    isSelected = selectedScenario == "Dry Hill Slope",
                    onClick = {
                        selectedScenario = "Dry Hill Slope"
                        viewModel.onRainfall1dChange("5")
                        viewModel.onSlopeChange("18")
                        viewModel.onRainfall3dChange("12")
                        viewModel.onRainfall7dChange("25")
                        viewModel.onElevationChange("950")
                        viewModel.onLithologyChange("Carbonate sedimentary rocks")
                        viewModel.onLandCoverChange("Cropland")
                    }
                )
            }

            // Input Telemetry Section (All 7 Geotechnical & GIS Features)
            FieldCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = strings.slopeTelemetryHeader,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        color = colors.accent
                    )

                    // 1. Elevation (MSL)
                    PredictionInputField(
                        label = strings.elevation,
                        unit = "m MSL",
                        icon = Icons.Default.Landscape,
                        iconColor = colors.accent,
                        value = uiState.elevation,
                        onValueChange = {
                            selectedScenario = null
                            viewModel.onElevationChange(it)
                        },
                        placeholder = "e.g. 1650"
                    )

                    // 2. Slope Angle (degrees)
                    PredictionInputField(
                        label = strings.slopeAngle,
                        unit = "degrees (°)",
                        icon = Icons.Default.Terrain,
                        iconColor = colors.warning,
                        value = uiState.slopeDeg,
                        onValueChange = {
                            selectedScenario = null
                            viewModel.onSlopeChange(it)
                        },
                        placeholder = "e.g. 38"
                    )

                    // 3. Current 24h Precipitation (rainfall_previous_1d)
                    PredictionInputField(
                        label = strings.rainfall1d,
                        unit = "mm (24h)",
                        icon = Icons.Default.WaterDrop,
                        iconColor = colors.accent,
                        value = uiState.rainfall1d,
                        onValueChange = {
                            selectedScenario = null
                            viewModel.onRainfall1dChange(it)
                        },
                        placeholder = "e.g. 85"
                    )

                    // 4. 3-Day Antecedent Rainfall Index (rainfall_previous_3d)
                    PredictionInputField(
                        label = strings.rainfall3d,
                        unit = "mm (3-day)",
                        icon = Icons.Default.CloudSync,
                        iconColor = colors.accent,
                        value = uiState.rainfall3d,
                        onValueChange = {
                            selectedScenario = null
                            viewModel.onRainfall3dChange(it)
                        },
                        placeholder = "e.g. 190"
                    )

                    // 5. 7-Day Cumulative Precipitation (rainfall_previous_7d)
                    PredictionInputField(
                        label = strings.rainfall7d,
                        unit = "mm (7-day)",
                        icon = Icons.Default.Thunderstorm,
                        iconColor = colors.critical,
                        value = uiState.rainfall7d,
                        onValueChange = {
                            selectedScenario = null
                            viewModel.onRainfall7dChange(it)
                        },
                        placeholder = "e.g. 320"
                    )

                    // 6. Lithology Group (GLiM Categorical Feature)
                    PredictionDropdownField(
                        label = strings.lithology,
                        selectedValue = uiState.lithologyGroup,
                        options = ModelConstants.LITHOLOGY_GROUPS,
                        onValueChange = {
                            selectedScenario = null
                            viewModel.onLithologyChange(it)
                        },
                        icon = Icons.Default.Layers,
                        iconColor = colors.warning
                    )

                    // 8. Land Cover (ESA WorldCover Categorical Feature)
                    PredictionDropdownField(
                        label = strings.landCover,
                        selectedValue = uiState.landCover,
                        options = ModelConstants.LAND_COVER_CLASSES,
                        onValueChange = {
                            selectedScenario = null
                            viewModel.onLandCoverChange(it)
                        },
                        icon = Icons.Default.Forest,
                        iconColor = colors.accent
                    )
                }
            }

            // Compute Action Button (Unified Primary Action Button)
            PrimaryActionButton(
                text = if (uiState.isLoading) strings.runningInference else strings.runAssessment,
                onClick = { viewModel.predict() },
                enabled = !uiState.isLoading,
                icon = Icons.Default.Bolt,
                containerColor = colors.accent
            )

            // Error Message
            uiState.error?.let {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.critical.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, colors.critical.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.ErrorOutline, null, tint = colors.critical)
                        Text(it, color = colors.critical, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Animated Assessment Result
            uiState.result?.let { result ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically { it / 3 }
                ) {
                    HeroRiskAssessmentDisplay(result = result)
                }
            }

            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
private fun ScenarioChip(
    title: String,
    accent: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val colors = BhurakshakTheme.colors
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) accent else colors.bgSurface,
        border = BorderStroke(1.dp, if (isSelected) accent else colors.borderDefault),
        shadowElevation = if (colors.isDark) 0.dp else 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color.White else accent)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else colors.textSecondary
            )
        }
    }
}

@Composable
private fun PredictionInputField(
    label: String,
    unit: String,
    icon: ImageVector,
    iconColor: Color,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    val colors = BhurakshakTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = colors.textPrimary
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                color = colors.textSecondary
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            leadingIcon = { Icon(icon, null, tint = iconColor, modifier = Modifier.size(18.dp)) },
            placeholder = { Text(placeholder, color = colors.textSecondary, fontSize = 13.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.accent,
                unfocusedBorderColor = colors.borderDefault,
                focusedContainerColor = colors.bgSurface,
                unfocusedContainerColor = colors.bgSurface,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary,
                cursorColor = colors.accent
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PredictionDropdownField(
    label: String,
    selectedValue: String,
    options: List<String>,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    iconColor: Color
) {
    val colors = BhurakshakTheme.colors
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = colors.textPrimary
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedValue,
                onValueChange = {},
                readOnly = true,
                leadingIcon = { Icon(icon, null, tint = iconColor, modifier = Modifier.size(18.dp)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.accent,
                    unfocusedBorderColor = colors.borderDefault,
                    focusedContainerColor = colors.bgSurface,
                    unfocusedContainerColor = colors.bgSurface,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(colors.bgSurface)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                color = if (option == selectedValue) colors.accent else colors.textPrimary,
                                fontWeight = if (option == selectedValue) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            onValueChange(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroRiskAssessmentDisplay(result: PredictionResult) {
    val colors = BhurakshakTheme.colors
    val strings = LocalAppStrings.current
    val riskColor = when (result.riskLevel.uppercase()) {
        "CRITICAL" -> colors.critical
        "HIGH", "MODERATE" -> colors.warning
        else -> colors.success
    }
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(result.probability) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = result.probability.toFloat().coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    FieldCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (result.riskLevel.equals("CRITICAL", ignoreCase = true)) colors.critical.copy(alpha = 0.5f) else colors.borderDefault
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PulsingStatusDot(color = riskColor, size = 8.dp)
                    Text(
                        strings.failureProbability.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        color = riskColor
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = colors.bgSurface,
                    border = BorderStroke(1.dp, colors.borderDefault)
                ) {
                    Text(
                        text = if (result.isMock) "OFFLINE MODE" else "LIVE XGBOOST",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Circular Arc Risk Gauge
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(190.dp)
                    .padding(8.dp)
            ) {
                val trackColor = if (colors.isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f)
                val gradientColors = listOf(colors.success, colors.warning, colors.warning, colors.critical)

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 14.dp.toPx()
                    val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                    // Track arc (240 degrees sweep from 150 to 390)
                    drawArc(
                        color = trackColor,
                        startAngle = 150f,
                        sweepAngle = 240f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Active risk gradient arc
                    drawArc(
                        brush = Brush.sweepGradient(gradientColors),
                        startAngle = 150f,
                        sweepAngle = 240f * animatedProgress.value,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${String.format(java.util.Locale.US, "%.0f", animatedProgress.value * 100)}%",
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.textPrimary
                    )
                    Text(
                        text = result.riskLevel.uppercase(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        color = riskColor
                    )
                    Text(
                        text = if (result.isMock) "RULE-BASED ESTIMATE (OFFLINE)" else "AI PREDICT_PROBA (7-FEATURE XGBOOST)",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        color = if (result.isMock) colors.warning else colors.accent
                    )
                }
            }

            // Geotechnical Factor Breakdown (Explainable AI / Feature Importances)
            val factorItems = if (result.featureImportances.isNotEmpty()) {
                result.featureImportances.entries.sortedByDescending { it.value }
            } else {
                result.factors.entries.sortedByDescending { it.value }
            }

            if (factorItems.isNotEmpty()) {
                HorizontalDivider(color = colors.borderDefault, thickness = 0.8.dp)

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            strings.contributingFactors.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp,
                            color = colors.textSecondary
                        )
                        Text(
                            text = "Feature Importance Weights",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent
                        )
                    }

                    factorItems.take(8).forEach { (factor, value) ->
                        val factorPercent = (value * 100).toInt()
                        val factorLabel = when (factor) {
                            "rainfall_previous_7d" -> "7-Day Cumulative Rainfall"
                            "rainfall_previous_3d" -> "3-Day Antecedent Rainfall"
                            "rainfall_previous_1d" -> "24h Precipitation"
                            "elevation" -> "Elevation (MSL)"
                            "slope" -> "Terrain Slope Gradient"
                            else -> factor.replace("_", " ").replaceFirstChar { it.uppercase() }
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = factorLabel,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 12.sp,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "${String.format(java.util.Locale.US, "%.1f", value * 100)}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (factorPercent >= 10) colors.critical else colors.textSecondary
                                )
                            }
                            LinearProgressIndicator(
                                progress = { (value / 0.16).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (factorPercent >= 10) colors.critical else colors.accent,
                                trackColor = colors.borderDefault
                            )
                        }
                    }

                    Text(
                        text = "Note: Weights reflect statistical predictive gain across training data. Built-up land prominence represents plausible anthropogenic slope modifications and/or reporting bias in historical disaster inventories.",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = colors.textSecondary.copy(alpha = 0.75f),
                        lineHeight = 13.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Official NDMA / GSI Directive
            if (result.recommendation.isNotBlank()) {
                HorizontalDivider(color = colors.borderDefault, thickness = 0.8.dp)

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = riskColor.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, riskColor.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = riskColor,
                            modifier = Modifier.size(20.dp).padding(top = 2.dp)
                        )
                        Column {
                            Text(
                                "CIVIC MITIGATION DIRECTIVE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.8.sp,
                                color = riskColor
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = result.recommendation,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textPrimary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
