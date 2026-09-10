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
import com.ner.landslide.presentation.ui.components.*
import com.ner.landslide.presentation.ui.theme.*
import com.ner.landslide.presentation.viewmodel.PredictionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionScreen(
    onNavigateBack: () -> Unit,
    viewModel: PredictionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = BhurakshakTheme.colors
    var selectedScenario by remember { mutableStateOf<String?>("Monsoon Cloudburst") }

    Scaffold(
        containerColor = colors.bgBase,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "AI Hazard Inference",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            "Geotechnical XGBoost & ML Pipeline",
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
            // Scenario Quick Presets
            SectionHeader(
                title = "Geotechnical Scenarios",
                subtitle = "Select a rapid simulation preset or input field telemetry below"
            )

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScenarioChip(
                    title = "Monsoon Cloudburst",
                    accent = colors.critical,
                    isSelected = selectedScenario == "Monsoon Cloudburst",
                    onClick = {
                        selectedScenario = "Monsoon Cloudburst"
                        viewModel.onRainfallChange("185")
                        viewModel.onSlopeChange("42")
                        viewModel.onSoilMoistureChange("92")
                        viewModel.onAntecedentRainChange("320")
                    }
                )
                ScenarioChip(
                    title = "Moderate Hill Shower",
                    accent = colors.warning,
                    isSelected = selectedScenario == "Moderate Hill Shower",
                    onClick = {
                        selectedScenario = "Moderate Hill Shower"
                        viewModel.onRainfallChange("55")
                        viewModel.onSlopeChange("28")
                        viewModel.onSoilMoistureChange("62")
                        viewModel.onAntecedentRainChange("95")
                    }
                )
                ScenarioChip(
                    title = "Dry Hill Slope",
                    accent = colors.accent,
                    isSelected = selectedScenario == "Dry Hill Slope",
                    onClick = {
                        selectedScenario = "Dry Hill Slope"
                        viewModel.onRainfallChange("5")
                        viewModel.onSlopeChange("18")
                        viewModel.onSoilMoistureChange("22")
                        viewModel.onAntecedentRainChange("12")
                    }
                )
            }

            // Input Telemetry Section
            FieldCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        "SLOPE & METEOROLOGICAL TELEMETRY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        color = colors.accent
                    )

                    PredictionInputField(
                        label = "Current 24h Precipitation",
                        unit = "mm",
                        icon = Icons.Default.WaterDrop,
                        iconColor = colors.accent,
                        value = uiState.rainfallMm,
                        onValueChange = {
                            selectedScenario = null
                            viewModel.onRainfallChange(it)
                        },
                        placeholder = "e.g. 140"
                    )

                    PredictionInputField(
                        label = "Slope Angle Inclination",
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

                    PredictionInputField(
                        label = "Volumetric Soil Moisture",
                        unit = "% saturation",
                        icon = Icons.Default.Grass,
                        iconColor = colors.accent,
                        value = uiState.soilMoisturePct,
                        onValueChange = {
                            selectedScenario = null
                            viewModel.onSoilMoistureChange(it)
                        },
                        placeholder = "e.g. 82"
                    )

                    PredictionInputField(
                        label = "3-Day Antecedent Rainfall Index",
                        unit = "mm accumulated",
                        icon = Icons.Default.CloudSync,
                        iconColor = colors.accent,
                        value = uiState.antecedentRain3d,
                        onValueChange = {
                            selectedScenario = null
                            viewModel.onAntecedentRainChange(it)
                        },
                        placeholder = "e.g. 260"
                    )
                }
            }

            // Compute Action Button (Unified Primary Action Button)
            PrimaryActionButton(
                text = if (uiState.isLoading) "RUNNING INFERENCE PIPELINE..." else "RUN GEOTECHNICAL RISK ASSESSMENT",
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

@Composable
private fun HeroRiskAssessmentDisplay(result: PredictionResult) {
    val colors = BhurakshakTheme.colors
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
                        "GEOTECHNICAL HAZARD ASSESSMENT",
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
                        text = if (result.isMock) "SIMULATION" else "LIVE XGBOOST",
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
                        text = "${"%.0f".format(animatedProgress.value * 100)}%",
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
                        text = "Confidence: ${"%.0f".format(result.confidence * 100)}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = colors.textSecondary
                    )
                }
            }

            // Geotechnical Factor Breakdown (Explainable AI)
            if (result.factors.isNotEmpty()) {
                HorizontalDivider(color = colors.borderDefault, thickness = 0.8.dp)

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "CONTRIBUTING GEOTECHNICAL DRIVERS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp,
                        color = colors.textSecondary
                    )

                    result.factors.forEach { (factor, value) ->
                        val factorPercent = (value * 100).toInt()
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = factor.replace("_", " ").replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 12.sp,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "$factorPercent%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (factorPercent > 60) riskColor else colors.textSecondary
                                )
                            }
                            LinearProgressIndicator(
                                progress = { value.toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (factorPercent > 60) riskColor else colors.accent,
                                trackColor = colors.borderDefault
                            )
                        }
                    }
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
