package com.ner.landslide.presentation.ui.weather

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.ner.landslide.domain.model.HourlyWeather
import com.ner.landslide.presentation.ui.components.*
import com.ner.landslide.presentation.ui.theme.*
import com.ner.landslide.presentation.viewmodel.WeatherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    onNavigateBack: () -> Unit,
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Doppler & Weather Radar", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = OnBackgroundDark)
                        Text("Real-Time Precipitation Telemetry", style = MaterialTheme.typography.labelSmall, fontSize = 11.sp, color = Primary80)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = OnBackgroundDark) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ObsidianBase)
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingContent(modifier = Modifier.padding(padding))
            uiState.error != null -> ErrorContent(
                message = uiState.error ?: "Failed to load weather",
                onRetry = { viewModel.loadWeather(26.14, 91.74) },
                modifier = Modifier.padding(padding)
            )
            else -> {
                val forecast = uiState.forecast ?: return@Scaffold
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BackgroundDark)
                        .verticalScroll(rememberScrollState())
                        .padding(padding)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Location Banner Card
                    FieldCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(SurfaceVariantDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.LocationOn, null, tint = Primary80, modifier = Modifier.size(18.dp))
                            }
                            Column {
                                Text(
                                    "EASTERN HIMALAYAS MET STATION",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.6.sp,
                                    color = Primary80
                                )
                                Text(
                                    "Lat: %.2f°N, Lon: %.2f°E".format(forecast.latitude, forecast.longitude),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 12.sp,
                                    color = OnBackgroundDark
                                )
                            }
                        }
                    }

                    // Hourly Forecast Section
                    SectionHeader(
                        title = "72-Hour Precipitation Radar",
                        subtitle = "Continuous Open-Meteo satellite & rain gauge feed"
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(forecast.hourlyData.take(72)) { hour ->
                            HourlyWeatherCard(hour = hour)
                        }
                    }

                    // Peak Rainfall Alert Card
                    val maxRain = forecast.hourlyData.maxOfOrNull { it.rainfallMm } ?: 0.0
                    val isPeakCritical = maxRain > 100
                    val alertColor = when {
                        maxRain > 100 -> SeverityCritical
                        maxRain > 50 -> SeverityHigh
                        maxRain > 20 -> SeverityModerate
                        else -> Primary80
                    }

                    SectionHeader(title = "Peak Precipitation Window")

                    FieldCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = if (isPeakCritical) SeverityCritical.copy(alpha = 0.5f) else BorderSubtle
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(alertColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.WaterDrop,
                                    contentDescription = null,
                                    tint = alertColor,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Column {
                                Text(
                                    "%.1f mm/h".format(maxRain),
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Primary80
                                )
                                Text(
                                    when {
                                        maxRain > 100 -> "Extreme cloudburst — Catastrophic debris flow danger"
                                        maxRain > 50 -> "Heavy monsoon downpour — Elevated landslide risk"
                                        maxRain > 20 -> "Moderate precipitation — Saturated slope caution"
                                        else -> "Nominal rainfall — Hill slopes stable"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = OnBackgroundDark
                                )
                            }
                        }
                    }

                    // Attribution
                    Text(
                        "Telemetry powered by Open-Meteo High-Resolution Numerical Weather Models",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = TextSubtle
                    )

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun HourlyWeatherCard(hour: HourlyWeather) {
    val rainColor = when {
        hour.rainfallMm > 50 -> SeverityCritical
        hour.rainfallMm > 20 -> SeverityModerate
        hour.rainfallMm > 5 -> SeverityModerate
        else -> Primary80
    }

    FieldCard(
        shape = RoundedCornerShape(8.dp),
        borderColor = if (hour.rainfallMm > 50) SeverityCritical.copy(alpha = 0.5f) else BorderSubtle
    ) {
        Column(
            modifier = Modifier.padding(10.dp).width(74.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                hour.time.takeLast(5),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted
            )
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(rainColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (hour.rainfallMm > 0) Icons.Default.Umbrella else Icons.Default.WbSunny,
                    contentDescription = null,
                    tint = rainColor,
                    modifier = Modifier.size(15.dp)
                )
            }
            Text(
                "%.1f mm".format(hour.rainfallMm),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = rainColor
            )
            Text(
                "%.0f°C".format(hour.temperature),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = OnBackgroundDark
            )
        }
    }
}
