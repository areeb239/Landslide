package com.ner.landslide.presentation.ui.weather

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        topBar = {
            TopAppBar(
                title = { Text("Weather Forecast", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null) }
                }
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
                        .verticalScroll(rememberScrollState())
                        .padding(padding)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header: location
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.LocationOn, null, tint = Primary80)
                        Text(
                            "North Eastern Region (%.2f°N, %.2f°E)".format(forecast.latitude, forecast.longitude),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                        )
                    }

                    // Hourly Cards
                    SectionHeader(title = "Hourly Forecast", subtitle = "Next 72 hours")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(forecast.hourlyData.take(72)) { hour ->
                            HourlyWeatherCard(hour = hour)
                        }
                    }

                    // Rainfall Risk Indicator
                    val maxRain = forecast.hourlyData.maxOfOrNull { it.rainfallMm } ?: 0.0
                    SectionHeader(title = "Peak Rainfall Alert")
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                maxRain > 100 -> SeverityCritical.copy(0.12f)
                                maxRain > 50 -> SeverityHigh.copy(0.12f)
                                maxRain > 20 -> SeverityModerate.copy(0.12f)
                                else -> SeverityLow.copy(0.12f)
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(Icons.Default.WaterDrop, null,
                                tint = if (maxRain > 50) SeverityCritical else SeverityLow,
                                modifier = Modifier.size(32.dp))
                            Column {
                                Text(
                                    "%.1f mm".format(maxRain),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (maxRain > 50) SeverityCritical else SeverityLow
                                )
                                Text(
                                    when {
                                        maxRain > 100 -> "Extreme rainfall — Very High landslide risk"
                                        maxRain > 50 -> "Heavy rainfall — High landslide risk"
                                        maxRain > 20 -> "Moderate rainfall — Watch for alerts"
                                        else -> "Normal rainfall — Low risk"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
                                )
                            }
                        }
                    }

                    // IMD attribution
                    Text(
                        "Data source: Open-Meteo (open-meteo.com) — free & no API key required",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.35f)
                    )

                    Spacer(Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun HourlyWeatherCard(hour: HourlyWeather) {
    val rainColor = when {
        hour.rainfallMm > 50 -> SeverityCritical
        hour.rainfallMm > 20 -> SeverityHigh
        hour.rainfallMm > 5 -> SeverityModerate
        else -> Primary80
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).width(80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                hour.time.takeLast(5), // HH:mm
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
            )
            Icon(
                imageVector = if (hour.rainfallMm > 0) Icons.Default.Umbrella else Icons.Default.WbSunny,
                contentDescription = null,
                tint = rainColor,
                modifier = Modifier.size(22.dp)
            )
            Text(
                "%.1f mm".format(hour.rainfallMm),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = rainColor
            )
            Text(
                "%.0f°C".format(hour.temperature),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(0.7f)
            )
        }
    }
}
