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
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionScreen(
    onNavigateBack: () -> Unit,
    viewModel: PredictionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "AI Hazard Inference",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = OnBackgroundDark
                        )
                        Text(
                            "Geotechnical XGBoost & ML Pipeline",
                            style = MaterialTheme.typography.labelSmall,
                            color = Primary80
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = OnBackgroundDark)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ObsidianBase
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
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
                    accent = SeverityCritical,
                    onClick = {
                        viewModel.onRainfallChange("185")
                        viewModel.onSlopeChange("42")
                        viewModel.onSoilMoistureChange("92")
                        viewModel.onAntecedentRainChange("320")
                    }
                )
                ScenarioChip(
                    title = "Moderate Hill Shower",
                    accent = SeverityModerate,
                    onClick = {
                        viewModel.onRainfallChange("55")
                        viewModel.onSlopeChange("28")
                        viewModel.onSoilMoistureChange("62")
                        viewModel.onAntecedentRainChange("95")
                    }
                )
                ScenarioChip(
                    title = "Dry Hill Slope",
                    accent = SeverityLow,
                    onClick = {
                        viewModel.onRainfallChange("5")
                        viewModel.onSlopeChange("18")
                        viewModel.onSoilMoistureChange("22")
                        viewModel.onAntecedentRainChange("12")
                    }
                )
            }

            // Input Telemetry Section
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceDark.copy(alpha = 0.85f)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        "SLOPE & METEOROLOGICAL TELEMETRY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        color = Primary80
                    )

                    PredictionInputField(
                        label = "Current 24h Precipitation",
                        unit = "mm",
                        icon = Icons.Default.WaterDrop,
                        iconColor = CyberCyan,
                        value = uiState.rainfallMm,
                        onValueChange = viewModel::onRainfallChange,
                        placeholder = "e.g. 140"
                    )

                    PredictionInputField(
                        label = "Slope Angle Inclination",
                        unit = "degrees (°)",
                        icon = Icons.Default.Terrain,
                        iconColor = Secondary80,
                        value = uiState.slopeDeg,
                        onValueChange = viewModel::onSlopeChange,
                        placeholder = "e.g. 38"
                    )

                    PredictionInputField(
                        label = "Volumetric Soil Moisture",
                        unit = "% saturation",
                        icon = Icons.Default.Grass,
                        iconColor = Primary80,
                        value = uiState.soilMoisturePct,
                        onValueChange = viewModel::onSoilMoistureChange,
                        placeholder = "e.g. 82"
                    )

                    PredictionInputField(
                        label = "3-Day Antecedent Rainfall Index",
                        unit = "mm accumulated",
                        icon = Icons.Default.CloudSync,
                        iconColor = BrandIndigo,
                        value = uiState.antecedentRain3d,
                        onValueChange = viewModel::onAntecedentRainChange,
                        placeholder = "e.g. 260"
                    )
                }
            }

            // Compute Action Button
            Button(
                onClick = { viewModel.predict() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary80),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = ObsidianBase,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "RUNNING INFERENCE PIPELINE...",
                        fontWeight = FontWeight.ExtraBold,
                        color = ObsidianBase,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )
                } else {
                    Icon(Icons.Default.Bolt, null, tint = ObsidianBase, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "RUN GEOTECHNICAL RISK ASSESSMENT",
                        fontWeight = FontWeight.ExtraBold,
                        color = ObsidianBase,
                        fontSize = 14.sp,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            // Error Message
            uiState.error?.let {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SeverityCritical.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, SeverityCritical.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.ErrorOutline, null, tint = SeverityCritical)
                        Text(it, color = SeverityCritical, style = MaterialTheme.typography.bodySmall)
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
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = SurfaceVariantDark,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(accent)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = OnBackgroundDark
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
                color = OnBackgroundDark
            )
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                color = TextSubtle
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            leadingIcon = { Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp)) },
            placeholder = { Text(placeholder, color = TextSubtle) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary80,
                unfocusedBorderColor = BorderSubtle,
                focusedContainerColor = SurfaceVariantDark.copy(alpha = 0.5f),
                unfocusedContainerColor = SurfaceVariantDark.copy(alpha = 0.3f),
                focusedTextColor = OnBackgroundDark,
                unfocusedTextColor = OnBackgroundDark
            )
        )
    }
}

@Composable
private fun HeroRiskAssessmentDisplay(result: PredictionResult) {
    val riskColor = result.riskLevel.toAlertSeverityColor()
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(result.probability) {
        animatedProgress.snapTo(0f)
        animatedProgress.animateTo(
            targetValue = result.probability.toFloat().coerceIn(0f, 1f),
            animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing)
        )
    }

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        glowAccent = riskColor,
        backgroundColor = SurfaceDark.copy(alpha = 0.95f)
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
                    color = SurfaceElevated
                ) {
                    Text(
                        text = if (result.isMock) "SIMULATION" else "LIVE XGBOOST",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
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
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 14.dp.toPx()
                    val arcSize = Size(size.width - strokeWidth, size.height - strokeWidth)
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                    // Track arc (240 degrees sweep from 150 to 390)
                    drawArc(
                        color = Color.White.copy(alpha = 0.08f),
                        startAngle = 150f,
                        sweepAngle = 240f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Active risk gradient arc
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(SeverityLow, SeverityModerate, SeverityHigh, SeverityCritical)
                        ),
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
                        color = OnBackgroundDark
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
                        color = TextSubtle
                    )
                }
            }

            // Geotechnical Factor Breakdown (Explainable AI)
            if (result.factors.isNotEmpty()) {
                Divider(color = Color.White.copy(alpha = 0.08f))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "CONTRIBUTING GEOTECHNICAL DRIVERS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp,
                        color = TextMuted
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
                                    color = OnBackgroundDark
                                )
                                Text(
                                    text = "$factorPercent%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (factorPercent > 60) riskColor else TextMuted
                                )
                            }
                            LinearProgressIndicator(
                                progress = { value.toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (factorPercent > 60) riskColor else Primary80,
                                trackColor = SurfaceElevated
                            )
                        }
                    }
                }
            }

            // Official NDMA / GSI Directive
            if (result.recommendation.isNotBlank()) {
                Divider(color = Color.White.copy(alpha = 0.08f))

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
                                color = OnBackgroundDark,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

