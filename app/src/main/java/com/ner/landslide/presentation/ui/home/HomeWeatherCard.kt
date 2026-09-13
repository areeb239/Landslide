package com.ner.landslide.presentation.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ner.landslide.domain.model.HourlyWeather
import com.ner.landslide.presentation.ui.components.FieldCard
import com.ner.landslide.presentation.ui.components.PulsingStatusDot
import com.ner.landslide.presentation.ui.theme.BhurakshakTheme
import com.ner.landslide.presentation.ui.weather.WeatherCondition
import com.ner.landslide.presentation.ui.weather.WeatherConditionIcon
import com.ner.landslide.util.SelectedLocation
import java.util.Locale

@Composable
fun HomeWeatherCard(
    weather: HourlyWeather?,
    location: SelectedLocation,
    isLoading: Boolean,
    condition: WeatherCondition,
    onOpenWeather: () -> Unit,
    onChangeLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = BhurakshakTheme.colors

    FieldCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onOpenWeather)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Section label + Location switch tactile chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(colors.accent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudQueue,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = "LIVE WEATHER",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.accent,
                        letterSpacing = 0.8.sp
                    )
                    PulsingStatusDot(color = colors.accent, size = 5.dp)
                }

                // Interactive "Change Location" Chip
                Surface(
                    onClick = onChangeLocation,
                    shape = RoundedCornerShape(12.dp),
                    color = colors.bgBase,
                    border = BorderStroke(1.dp, if (location.isGpsLocation) colors.accent.copy(alpha = 0.5f) else colors.borderDefault)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (location.isGpsLocation) Icons.Default.GpsFixed else Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = if (location.isGpsLocation) colors.accent else colors.textSecondary,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = if (location.isGpsLocation) "GPS" else "Custom",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (location.isGpsLocation) colors.accent else colors.textSecondary
                        )
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "Change Location",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            // Location Name Display (Clearly formatted, NEVER showing raw lat/lon)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = location.name.ifBlank { "Selected Sector" },
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    maxLines = 1
                )
            }

            if (isLoading && weather == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = colors.accent,
                        strokeWidth = 2.dp
                    )
                }
            } else {
                val temp = weather?.temperature ?: 27.0
                val rainProb = weather?.rainProbability ?: 10
                val humidity = weather?.humidity ?: 65.0
                val windSpeed = weather?.windSpeedKmh ?: 12.0

                // Middle: Big Temperature + Condition Icon & Label
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                text = String.format(Locale.US, "%.0f", temp),
                                fontSize = 34.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = colors.textPrimary,
                                letterSpacing = (-1).sp
                            )
                            Text(
                                text = "°C",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.accent,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        Text(
                            text = condition.label,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textSecondary
                        )
                    }

                    // Weather condition icon illustration
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(colors.accent.copy(alpha = 0.18f), Color.Transparent)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        WeatherConditionIcon(condition = condition, iconSize = 32.dp)
                    }
                }

                // Micro-Telemetry metrics row: Rain, Humidity, Wind
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    WeatherMetricTile(
                        icon = Icons.Default.WaterDrop,
                        label = "Rain",
                        value = "$rainProb%",
                        accentColor = colors.accent,
                        modifier = Modifier.weight(1f)
                    )
                    WeatherMetricTile(
                        icon = Icons.Default.Opacity,
                        label = "Humidity",
                        value = String.format(Locale.US, "%.0f%%", humidity),
                        accentColor = Color(0xFF38BDF8),
                        modifier = Modifier.weight(1f)
                    )
                    WeatherMetricTile(
                        icon = Icons.Default.Air,
                        label = "Wind",
                        value = String.format(Locale.US, "%.0f km/h", windSpeed),
                        accentColor = Color(0xFFC084FC),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherMetricTile(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val colors = BhurakshakTheme.colors

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = colors.bgBase,
        border = BorderStroke(0.5.dp, colors.borderDefault)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(13.dp)
            )
            Column {
                Text(
                    text = label,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary
                )
                Text(
                    text = value,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    maxLines = 1
                )
            }
        }
    }
}
