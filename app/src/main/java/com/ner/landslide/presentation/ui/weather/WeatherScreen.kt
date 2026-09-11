package com.ner.landslide.presentation.ui.weather

import androidx.compose.animation.core.*
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ner.landslide.domain.model.HourlyWeather
import com.ner.landslide.presentation.ui.components.*
import com.ner.landslide.presentation.ui.theme.*
import com.ner.landslide.presentation.viewmodel.WeatherViewModel
import com.ner.landslide.util.toRelativeTimeString
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

// ─── THEME PALETTE FOR MODERN DARK WEATHER DASHBOARD ─────────────────────────
private val DeepNavyBg = Color(0xFF090D1A)
private val CardNavySurface = Color(0xFF12192E)
private val CardNavyElevated = Color(0xFF17203A)
private val BorderNavySubtle = Color(0xFF223055)

private val AccentTealGlow = Color(0xFF00E5FF)
private val AccentSkyBlue = Color(0xFF38BDF8)
private val AccentPurpleUV = Color(0xFFC084FC)
private val AccentSunYellow = Color(0xFFFFD600)
private val AccentLightning = Color(0xFFFFB300)
private val AccentCriticalRed = Color(0xFFEF4444)
private val AccentWarningAmber = Color(0xFFFB923C)
private val AccentSafeGreen = Color(0xFF10B981)

private val TextPrimaryWhite = Color(0xFFFFFFFF)
private val TextSecondarySlate = Color(0xFF94A3B8)
private val TextMutedSlate = Color(0xFF64748B)

// ─── WEATHER CLASSIFICATION & ICONS ──────────────────────────────────────────
enum class WeatherCondition(val label: String) {
    SUNNY("Sunny"),
    PARTLY_CLOUDY("Partly Cloudy"),
    CLOUDY("Cloudy"),
    RAIN("Rain"),
    THUNDERSTORM("Thunderstorm"),
    HEAVY_STORM("Heavy Storm / Thunder")
}

fun resolveCondition(
    rainfallMm: Double,
    rainProb: Int = 0,
    windSpeedKmh: Double = 0.0,
    humidity: Double = 0.0,
    weatherCode: Int = 0
): WeatherCondition {
    return when {
        rainfallMm >= 15.0 || (rainfallMm >= 5.0 && windSpeedKmh > 35.0) || weatherCode in listOf(95, 96, 99) ->
            WeatherCondition.HEAVY_STORM
        rainfallMm >= 4.0 || weatherCode in listOf(91, 92) ->
            WeatherCondition.THUNDERSTORM
        rainfallMm > 0.0 || rainProb >= 50 || weatherCode in listOf(51, 53, 55, 61, 63, 65, 80, 81, 82) ->
            WeatherCondition.RAIN
        humidity > 70.0 || weatherCode in listOf(3, 45, 48) ->
            WeatherCondition.CLOUDY
        humidity > 45.0 || weatherCode in listOf(1, 2) ->
            WeatherCondition.PARTLY_CLOUDY
        else ->
            WeatherCondition.SUNNY
    }
}

@Composable
fun WeatherConditionIcon(
    condition: WeatherCondition,
    modifier: Modifier = Modifier,
    iconSize: Dp = 24.dp
) {
    when (condition) {
        WeatherCondition.SUNNY -> {
            Icon(
                imageVector = Icons.Default.WbSunny,
                contentDescription = condition.label,
                tint = AccentSunYellow,
                modifier = modifier.size(iconSize)
            )
        }
        WeatherCondition.PARTLY_CLOUDY -> {
            Box(modifier = modifier.size(iconSize), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = null,
                    tint = AccentSunYellow,
                    modifier = Modifier
                        .size(iconSize * 0.7f)
                        .align(Alignment.TopEnd)
                )
                Icon(
                    imageVector = Icons.Default.Cloud,
                    contentDescription = condition.label,
                    tint = Color(0xFFE2E8F0),
                    modifier = Modifier
                        .size(iconSize * 0.85f)
                        .align(Alignment.BottomStart)
                )
            }
        }
        WeatherCondition.CLOUDY -> {
            Icon(
                imageVector = Icons.Default.Cloud,
                contentDescription = condition.label,
                tint = Color(0xFFE2E8F0),
                modifier = modifier.size(iconSize)
            )
        }
        WeatherCondition.RAIN -> {
            Icon(
                imageVector = Icons.Default.WaterDrop,
                contentDescription = condition.label,
                tint = AccentSkyBlue,
                modifier = modifier.size(iconSize)
            )
        }
        WeatherCondition.THUNDERSTORM -> {
            Icon(
                imageVector = Icons.Default.Thunderstorm,
                contentDescription = condition.label,
                tint = AccentLightning,
                modifier = modifier.size(iconSize)
            )
        }
        WeatherCondition.HEAVY_STORM -> {
            Box(modifier = modifier.size(iconSize), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Thunderstorm,
                    contentDescription = condition.label,
                    tint = AccentWarningAmber,
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}

// ─── DATE HELPERS ─────────────────────────────────────────────────────────────
data class DayAggregate(
    val dateKey: String,
    val dateFormatted: String, // e.g. "21 Sep"
    val dayName: String,       // e.g. "Thursday"
    val isToday: Boolean,
    val isTomorrow: Boolean,
    val condition: WeatherCondition,
    val maxTemp: Double,
    val minTemp: Double,
    val rainProbability: Int,
    val totalRain: Double,
    val maxPerHour: Double,
    val hourlyData: List<HourlyWeather>
)

fun parseDateKey(dateStr: String): Calendar {
    val raw = dateStr.substringBefore('T')
    val parts = raw.split("-")
    return Calendar.getInstance().apply {
        if (parts.size == 3) {
            set(Calendar.YEAR, parts[0].toInt())
            set(Calendar.MONTH, parts[1].toInt() - 1)
            set(Calendar.DAY_OF_MONTH, parts[2].toInt())
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
        }
    }
}

// ─── MAIN WEATHER SCREEN COMPOSABLE ──────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    onNavigateBack: () -> Unit,
    viewModel: WeatherViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val strings = LocalAppStrings.current

    Scaffold(
        containerColor = DeepNavyBg,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "WEATHER RADAR & HAZARDS",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            letterSpacing = 1.sp,
                            color = AccentTealGlow
                        )
                        Text(
                            text = "Numerical Atmospheric Simulation",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.5.sp,
                            color = TextSecondarySlate
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimaryWhite
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.useCurrentLocation() }) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Update GPS Location",
                            tint = AccentTealGlow,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepNavyBg)
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DeepNavyBg)
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CircularProgressIndicator(color = AccentTealGlow, strokeWidth = 2.5.dp)
                        Text("Retrieving High-Resolution Weather Model...", color = TextSecondarySlate, fontSize = 12.sp)
                    }
                }
            }
            uiState.error != null -> {
                ErrorContent(
                    message = uiState.error ?: "Weather telemetry unavailable",
                    onRetry = { viewModel.useCurrentLocation() },
                    modifier = Modifier.padding(padding)
                )
            }
            else -> {
                val forecast = uiState.forecast ?: return@Scaffold

                // Aggregate 7-day data
                val dailyList = remember(forecast) {
                    val grouped = forecast.hourlyData.groupBy { it.time.substringBefore('T') }
                    val entries = grouped.entries.toList().take(7)
                    val cal = Calendar.getInstance()
                    val todayYearDay = cal.get(Calendar.DAY_OF_YEAR)

                    entries.mapIndexed { index, entry ->
                        val dateKey = entry.key
                        val hours = entry.value
                        val itemCal = parseDateKey(dateKey)
                        val itemYearDay = itemCal.get(Calendar.DAY_OF_YEAR)

                        val isToday = index == 0 || itemYearDay == todayYearDay
                        val isTomorrow = index == 1 || itemYearDay == todayYearDay + 1

                        val dateFormatted = SimpleDateFormat("d MMM", Locale.ENGLISH).format(itemCal.time)
                        val dayName = SimpleDateFormat("EEEE", Locale.ENGLISH).format(itemCal.time)

                        val maxRain = hours.maxOfOrNull { it.rainfallMm } ?: 0.0
                        val totalRain = hours.sumOf { it.rainfallMm }
                        val maxWind = hours.maxOfOrNull { it.windSpeedKmh } ?: 0.0
                        val avgHum = if (hours.isNotEmpty()) hours.map { it.humidity }.average() else 50.0
                        val maxProb = hours.maxOfOrNull { it.rainProbability } ?: if (totalRain > 0) 65 else 10
                        val code = hours.firstOrNull()?.weatherCode ?: 0

                        val domCondition = resolveCondition(maxRain, maxProb, maxWind, avgHum, code)

                        DayAggregate(
                            dateKey = dateKey,
                            dateFormatted = dateFormatted,
                            dayName = dayName,
                            isToday = isToday,
                            isTomorrow = isTomorrow,
                            condition = domCondition,
                            maxTemp = hours.maxOfOrNull { it.temperature } ?: 26.0,
                            minTemp = hours.minOfOrNull { it.temperature } ?: 18.0,
                            rainProbability = maxProb,
                            totalRain = totalRain,
                            maxPerHour = maxRain,
                            hourlyData = hours
                        )
                    }
                }

                val currentHour = remember(forecast) {
                    forecast.hourlyData.firstOrNull() ?: HourlyWeather(temperature = 24.0)
                }
                val currentCondition = remember(currentHour) {
                    resolveCondition(
                        currentHour.rainfallMm,
                        currentHour.rainProbability,
                        currentHour.windSpeedKmh,
                        currentHour.humidity,
                        currentHour.weatherCode
                    )
                }

                var selectedRadarDayIndex by remember { mutableIntStateOf(0) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(DeepNavyBg)
                        .verticalScroll(rememberScrollState())
                        .padding(padding)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {

                    // ─── 1. LOCATION HEADER ──────────────────────────────────────
                    LocationHeaderCard(
                        locationName = uiState.locationName,
                        lastUpdatedTime = uiState.lastUpdatedTime,
                        onGpsClick = { viewModel.useCurrentLocation() }
                    )

                    // ─── 2. MAIN CIRCULAR TEMPERATURE HERO INDICATOR ─────────────
                    MainTemperatureHero(
                        currentTemp = currentHour.temperature,
                        feelsLike = currentHour.temperature + if (currentHour.humidity > 70) 2.0 else -1.0,
                        highTemp = dailyList.firstOrNull()?.maxTemp ?: 28.0,
                        lowTemp = dailyList.firstOrNull()?.minTemp ?: 19.0,
                        condition = currentCondition,
                        humidity = currentHour.humidity,
                        windSpeed = currentHour.windSpeedKmh,
                        windDirection = currentHour.windDirectionDeg,
                        uvIndex = currentHour.uvIndex,
                        rainRate = currentHour.rainfallMm
                    )

                    // ─── 3. TEMPERATURE TREND LINE & HOURLY FORECAST ─────────────
                    val hourlySlice = remember(forecast) {
                        forecast.hourlyData.take(24)
                    }
                    HourlyTrendSection(hourlyList = hourlySlice)

                    // ─── 4. CLEAN VERTICAL 7-DAY FORECAST ────────────────────────
                    SevenDayForecastSection(
                        days = dailyList,
                        overallMin = dailyList.minOfOrNull { it.minTemp } ?: 15.0,
                        overallMax = dailyList.maxOfOrNull { it.maxTemp } ?: 32.0
                    )

                    // ─── 5. AI RISK PREDICTION SECTION ───────────────────────────
                    AIRiskPredictionCard(
                        forecast = forecast,
                        days = dailyList
                    )

                    // ─── 6. DEDICATED RAIN RADAR VISUALIZATION ───────────────────
                    RainRadarSection(
                        days = dailyList,
                        selectedIndex = selectedRadarDayIndex,
                        onSelectDay = { selectedRadarDayIndex = it }
                    )

                    Spacer(Modifier.height(30.dp))
                }
            }
        }
    }
}

// ─── 1. LOCATION HEADER COMPOSABLE ───────────────────────────────────────────
@Composable
private fun LocationHeaderCard(
    locationName: String,
    lastUpdatedTime: Long,
    onGpsClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = CardNavySurface,
        border = BorderStroke(1.dp, BorderNavySubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AccentTealGlow.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = AccentTealGlow,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = locationName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextPrimaryWhite,
                        maxLines = 1
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PulsingStatusDot(color = AccentSafeGreen, size = 6.dp)
                        Text(
                            text = "GPS Live Monitored • Synced ${lastUpdatedTime.toRelativeTimeString()}",
                            fontSize = 11.sp,
                            color = TextSecondarySlate
                        )
                    }
                }
            }

            Surface(
                onClick = onGpsClick,
                shape = RoundedCornerShape(10.dp),
                color = AccentTealGlow.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, AccentTealGlow.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = null,
                        tint = AccentTealGlow,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "GPS Sync",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentTealGlow
                    )
                }
            }
        }
    }
}

// ─── 2. MAIN TEMPERATURE HERO INDICATOR ──────────────────────────────────────
@Composable
private fun MainTemperatureHero(
    currentTemp: Double,
    feelsLike: Double,
    highTemp: Double,
    lowTemp: Double,
    condition: WeatherCondition,
    humidity: Double,
    windSpeed: Double,
    windDirection: Double,
    uvIndex: Double,
    rainRate: Double
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = CardNavySurface,
        border = BorderStroke(1.dp, BorderNavySubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Glowing Circular Ring Indicator
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                CardNavyElevated,
                                Color(0xFF0D1426)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Outer subtle gradient ring
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        brush = Brush.sweepGradient(
                            listOf(
                                AccentTealGlow.copy(alpha = 0.8f),
                                AccentSkyBlue.copy(alpha = 0.5f),
                                AccentPurpleUV.copy(alpha = 0.7f),
                                AccentTealGlow.copy(alpha = 0.8f)
                            )
                        ),
                        radius = size.minDimension / 2f - 4.dp.toPx(),
                        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    WeatherConditionIcon(condition = condition, iconSize = 28.dp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${currentTemp.toInt()}°",
                        fontSize = 46.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimaryWhite,
                        lineHeight = 48.sp
                    )
                    Text(
                        text = condition.label,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AccentSkyBlue
                    )
                }
            }

            // High / Low & Feels Like Sub-pill
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CardNavyElevated,
                border = BorderStroke(1.dp, BorderNavySubtle)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Feels like ${feelsLike.toInt()}°C",
                        fontSize = 11.5.sp,
                        color = TextSecondarySlate,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "•",
                        fontSize = 11.sp,
                        color = TextMutedSlate
                    )
                    Text(
                        text = "H: ${highTemp.toInt()}°  L: ${lowTemp.toInt()}°",
                        fontSize = 11.5.sp,
                        color = TextPrimaryWhite,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Quick Atmospheric Telemetry Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricPill(
                    icon = Icons.Default.WaterDrop,
                    iconTint = AccentSkyBlue,
                    label = "Rain Rate",
                    value = "%.1f mm/h".format(rainRate)
                )
                MetricPill(
                    icon = Icons.Default.Air,
                    iconTint = Color(0xFF67E8F9),
                    label = "Wind",
                    value = "${windSpeed.toInt()} km/h"
                )
                MetricPill(
                    icon = Icons.Default.Opacity,
                    iconTint = Color(0xFF38BDF8),
                    label = "Humidity",
                    value = "${humidity.toInt()}%"
                )
                MetricPill(
                    icon = Icons.Default.WbSunny,
                    iconTint = AccentPurpleUV,
                    label = "UV Index",
                    value = "UV %.0f".format(uvIndex)
                )
            }
        }
    }
}

@Composable
private fun MetricPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    label: String,
    value: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CardNavyElevated,
        border = BorderStroke(1.dp, BorderNavySubtle)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryWhite
            )
            Text(
                text = label,
                fontSize = 9.sp,
                color = TextMutedSlate
            )
        }
    }
}

// ─── 3. TEMPERATURE TREND LINE & HOURLY FORECAST ─────────────────────────────
@Composable
private fun HourlyTrendSection(hourlyList: List<HourlyWeather>) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = CardNavySurface,
        border = BorderStroke(1.dp, BorderNavySubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "24-HOUR HOURLY SIMULATION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp,
                    color = AccentTealGlow
                )
                Text(
                    text = "Temperature & Precipitation Curve",
                    fontSize = 10.sp,
                    color = TextMutedSlate
                )
            }

            val cardWidth = 86.dp
            val totalWidth = cardWidth * hourlyList.size
            val minT = hourlyList.minOfOrNull { it.temperature } ?: 18.0
            val maxT = hourlyList.maxOfOrNull { it.temperature } ?: 30.0
            val tempRange = (maxT - minT).coerceAtLeast(2.0)

            // Horizontal Scroll Container holding the Bezier curve and the cards
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                Column(modifier = Modifier.width(totalWidth)) {
                    // Smooth Temperature Canvas Bezier Curve
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                    ) {
                        val widthPx = size.width
                        val heightPx = size.height
                        val topPad = 12.dp.toPx()
                        val botPad = 10.dp.toPx()
                        val usableH = heightPx - topPad - botPad

                        val points = hourlyList.mapIndexed { i, h ->
                            val cx = (i + 0.5f) * cardWidth.toPx()
                            val norm = ((h.temperature - minT) / tempRange).toFloat()
                            val cy = heightPx - botPad - (norm * usableH)
                            Offset(cx, cy)
                        }

                        if (points.size >= 2) {
                            val curvePath = Path().apply {
                                moveTo(points[0].x, points[0].y)
                                for (i in 0 until points.size - 1) {
                                    val p0 = points[i]
                                    val p1 = points[i + 1]
                                    val controlX = (p0.x + p1.x) / 2f
                                    cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                                }
                            }

                            val fillPath = Path().apply {
                                addPath(curvePath)
                                lineTo(points.last().x, heightPx)
                                lineTo(points.first().x, heightPx)
                                close()
                            }

                            // Gradient Fill under curve
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        AccentTealGlow.copy(alpha = 0.25f),
                                        AccentSkyBlue.copy(alpha = 0.05f),
                                        Color.Transparent
                                    ),
                                    startY = 0f,
                                    endY = heightPx
                                )
                            )

                            // Glowing Curve Line
                            drawPath(
                                path = curvePath,
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        AccentTealGlow,
                                        AccentSkyBlue,
                                        AccentPurpleUV
                                    )
                                ),
                                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                            )

                            // Point Nodes
                            points.forEach { pt ->
                                drawCircle(
                                    color = DeepNavyBg,
                                    radius = 4.dp.toPx(),
                                    center = pt
                                )
                                drawCircle(
                                    color = AccentTealGlow,
                                    radius = 2.5.dp.toPx(),
                                    center = pt
                                )
                            }
                        }
                    }

                    // Hourly Weather Cards underneath
                    Row(modifier = Modifier.fillMaxWidth()) {
                        hourlyList.forEach { hour ->
                            HourlyWeatherCard(
                                hour = hour,
                                modifier = Modifier.width(cardWidth)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HourlyWeatherCard(
    hour: HourlyWeather,
    modifier: Modifier = Modifier
) {
    val cond = resolveCondition(
        hour.rainfallMm,
        hour.rainProbability,
        hour.windSpeedKmh,
        hour.humidity,
        hour.weatherCode
    )

    Column(
        modifier = modifier
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(CardNavyElevated)
            .border(BorderStroke(1.dp, BorderNavySubtle), RoundedCornerShape(14.dp))
            .padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Time
        Text(
            text = hour.time.takeLast(5),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondarySlate
        )

        // Weather Icon
        WeatherConditionIcon(condition = cond, iconSize = 22.dp)

        // Temperature
        Text(
            text = "${hour.temperature.toInt()}°",
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimaryWhite
        )

        // Rain Probability (Blue accent)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.WaterDrop,
                contentDescription = null,
                tint = AccentSkyBlue,
                modifier = Modifier.size(10.dp)
            )
            Text(
                text = "${hour.rainProbability}%",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = AccentSkyBlue
            )
        }

        // Weather Condition Label
        Text(
            text = cond.label,
            fontSize = 9.sp,
            color = TextMutedSlate,
            maxLines = 1
        )

        HorizontalDivider(color = BorderNavySubtle, thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 4.dp))

        // Wind Speed & UV level (Purple accent for UV)
        Text(
            text = "${hour.windSpeedKmh.toInt()} km/h",
            fontSize = 9.5.sp,
            color = TextSecondarySlate,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "UV %.0f".format(hour.uvIndex),
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            color = AccentPurpleUV
        )
    }
}

// ─── 4. CLEAN VERTICAL 7-DAY FORECAST SECTION ────────────────────────────────
@Composable
private fun SevenDayForecastSection(
    days: List<DayAggregate>,
    overallMin: Double,
    overallMax: Double
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = CardNavySurface,
        border = BorderStroke(1.dp, BorderNavySubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "7-DAY WEATHER FORECAST",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp,
                    color = AccentTealGlow
                )
                Text(
                    text = "High / Low Spectrum",
                    fontSize = 10.sp,
                    color = TextMutedSlate
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                days.forEach { day ->
                    DayForecastRow(
                        day = day,
                        overallMin = overallMin,
                        overallMax = overallMax
                    )
                }
            }
        }
    }
}

@Composable
private fun DayForecastRow(
    day: DayAggregate,
    overallMin: Double,
    overallMax: Double
) {
    val totalRange = (overallMax - overallMin).coerceAtLeast(1.0)
    val minRatio = ((day.minTemp - overallMin) / totalRange).toFloat().coerceIn(0f, 1f)
    val maxRatio = ((day.maxTemp - overallMin) / totalRange).toFloat().coerceIn(0f, 1f)

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (day.isToday) CardNavyElevated else Color.Transparent,
        border = BorderStroke(
            1.dp,
            if (day.isToday) AccentTealGlow.copy(alpha = 0.35f) else BorderNavySubtle.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Day Name & Date column
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.width(110.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(
                            text = if (day.isToday) "Today" else if (day.isTomorrow) "Tomorrow" else day.dayName.take(3),
                            fontWeight = if (day.isToday || day.isTomorrow) FontWeight.ExtraBold else FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (day.isToday) AccentTealGlow else TextPrimaryWhite
                        )
                        if (day.isToday) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = AccentTealGlow.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "NOW",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = AccentTealGlow,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = day.dateFormatted,
                        fontSize = 10.5.sp,
                        color = TextMutedSlate
                    )
                }
            }

            // Weather Icon & Rain Probability
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.width(68.dp)
            ) {
                WeatherConditionIcon(condition = day.condition, iconSize = 22.dp)
                if (day.rainProbability > 20) {
                    Text(
                        text = "${day.rainProbability}%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentSkyBlue
                    )
                } else {
                    Spacer(Modifier.width(10.dp))
                }
            }

            // Min Temp, Horizontal Temperature Spectrum Bar, Max Temp
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "${day.minTemp.toInt()}°",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondarySlate,
                    modifier = Modifier.width(24.dp)
                )

                // Visual Temperature Spectrum Bar
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF0F172A))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction = (maxRatio - minRatio).coerceAtLeast(0.1f))
                            .offset(x = (minRatio * 70).dp) // approximate visual offset
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        AccentSkyBlue,
                                        AccentTealGlow,
                                        AccentSunYellow
                                    )
                                )
                            )
                    )
                }

                Text(
                    text = "${day.maxTemp.toInt()}°",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryWhite,
                    modifier = Modifier.width(24.dp)
                )
            }
        }
    }
}

// ─── 5. AI RISK PREDICTION CARD ──────────────────────────────────────────────
@Composable
private fun AIRiskPredictionCard(
    forecast: com.ner.landslide.domain.model.WeatherForecast,
    days: List<DayAggregate>
) {
    val total24hRain = days.firstOrNull()?.totalRain ?: 0.0
    val maxPerHour = days.firstOrNull()?.maxPerHour ?: 0.0
    val antecedent3d = days.take(3).sumOf { it.totalRain }

    // Determine Risk Level
    val (riskTitle, riskColor, riskExplanation) = when {
        total24hRain > 80.0 || maxPerHour > 40.0 -> Triple(
            "CRITICAL HAZARD",
            AccentCriticalRed,
            "Torrential cloudburst exceeds geological drainage thresholds. Extreme pore-water pressure creates catastrophic debris flow hazard across steep cuttings."
        )
        total24hRain > 40.0 || maxPerHour > 18.0 -> Triple(
            "HIGH RISK",
            AccentWarningAmber,
            "Heavy monsoon downpour causing rapid slope saturation. Structural cohesion in weathered regolith compromised. Evacuate vulnerable landslide toes."
        )
        total24hRain > 15.0 || maxPerHour > 6.0 -> Triple(
            "MODERATE CAUTION",
            Color(0xFFFBBF24),
            "Moderate precipitation softening surface soil strata. Hill roads susceptible to shallow slumps and rockfalls along highway corridors."
        )
        else -> Triple(
            "LOW HAZARD",
            AccentSafeGreen,
            "Atmospheric and precipitation indicators nominal. Slope shear strength stable with standard drainage clearance."
        )
    }

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = CardNavySurface,
        border = BorderStroke(1.dp, riskColor.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(riskColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = riskColor,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "AI HAZARD & LANDSLIDE RISK",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp,
                            color = TextPrimaryWhite
                        )
                        Text(
                            text = "Geotechnical ML Inference",
                            fontSize = 9.5.sp,
                            color = TextMutedSlate
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = riskColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, riskColor.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        PulsingStatusDot(color = riskColor, size = 6.dp)
                        Text(
                            text = riskTitle,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.sp,
                            color = riskColor
                        )
                    }
                }
            }

            Text(
                text = riskExplanation,
                fontSize = 12.sp,
                color = TextSecondarySlate,
                lineHeight = 17.sp
            )

            HorizontalDivider(color = BorderNavySubtle, thickness = 0.8.dp)

            // Contributing Geotechnical Factors Grid
            Text(
                text = "Key Contributing Factors",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondarySlate
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RiskFactorBox(
                    title = "24h Rain",
                    value = "%.1f mm".format(total24hRain),
                    highlightColor = if (total24hRain > 30.0) riskColor else AccentTealGlow,
                    modifier = Modifier.weight(1f)
                )
                RiskFactorBox(
                    title = "Peak Rate",
                    value = "%.1f mm/h".format(maxPerHour),
                    highlightColor = if (maxPerHour > 15.0) riskColor else AccentSkyBlue,
                    modifier = Modifier.weight(1f)
                )
                RiskFactorBox(
                    title = "3-Day Saturation",
                    value = "%.0f mm".format(antecedent3d),
                    highlightColor = if (antecedent3d > 60.0) riskColor else AccentPurpleUV,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun RiskFactorBox(
    title: String,
    value: String,
    highlightColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CardNavyElevated,
        border = BorderStroke(1.dp, BorderNavySubtle),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = title,
                fontSize = 9.sp,
                color = TextMutedSlate
            )
            Text(
                text = value,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = highlightColor
            )
        }
    }
}

// ─── 6. DEDICATED RAIN RADAR VISUALIZATION ───────────────────────────────────
@Composable
private fun RainRadarSection(
    days: List<DayAggregate>,
    selectedIndex: Int,
    onSelectDay: (Int) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = CardNavySurface,
        border = BorderStroke(1.dp, BorderNavySubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "LIVE DOPPLER PRECIPITATION RADAR",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.8.sp,
                        color = AccentTealGlow
                    )
                    Text(
                        text = "Real-Time Echo Reflectivity Simulation",
                        fontSize = 10.sp,
                        color = TextMutedSlate
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = AccentSkyBlue.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, AccentSkyBlue.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = "RADAR ACTIVE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentSkyBlue,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Interactive Radar Map Canvas with Rotating Beam
            val infiniteTransition = rememberInfiniteTransition(label = "radar_sweep")
            val sweepAngle by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 4000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "angle"
            )

            val activeDay = days.getOrNull(selectedIndex) ?: days.firstOrNull()
            val rainIntensity = (activeDay?.totalRain ?: 0.0).coerceIn(0.0, 100.0)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF070B16)),
                contentAlignment = Alignment.Center
            ) {
                // Radar concentric rings & sweep Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val center = Offset(w / 2f, h / 2f)
                    val maxR = min(w, h) / 2f - 8.dp.toPx()

                    // Range Rings
                    listOf(0.33f, 0.66f, 1.0f).forEach { frac ->
                        drawCircle(
                            color = Color(0xFF1E293B),
                            radius = maxR * frac,
                            center = center,
                            style = Stroke(width = 1.dp.toPx())
                        )
                    }

                    // Crosshairs
                    drawLine(
                        color = Color(0xFF1E293B),
                        start = Offset(center.x, center.y - maxR),
                        end = Offset(center.x, center.y + maxR),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = Color(0xFF1E293B),
                        start = Offset(center.x - maxR, center.y),
                        end = Offset(center.x + maxR, center.y),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Simulated localized rainfall clusters
                    if (rainIntensity > 5.0) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    AccentCriticalRed.copy(alpha = 0.45f),
                                    AccentWarningAmber.copy(alpha = 0.3f),
                                    Color.Transparent
                                ),
                                center = Offset(center.x - 35.dp.toPx(), center.y - 20.dp.toPx()),
                                radius = 45.dp.toPx()
                            ),
                            radius = 45.dp.toPx(),
                            center = Offset(center.x - 35.dp.toPx(), center.y - 20.dp.toPx())
                        )
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    AccentSkyBlue.copy(alpha = 0.5f),
                                    AccentTealGlow.copy(alpha = 0.25f),
                                    Color.Transparent
                                ),
                                center = Offset(center.x + 40.dp.toPx(), center.y + 25.dp.toPx()),
                                radius = 55.dp.toPx()
                            ),
                            radius = 55.dp.toPx(),
                            center = Offset(center.x + 40.dp.toPx(), center.y + 25.dp.toPx())
                        )
                    } else {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    AccentTealGlow.copy(alpha = 0.2f),
                                    Color.Transparent
                                ),
                                center = center,
                                radius = 40.dp.toPx()
                            ),
                            radius = 40.dp.toPx(),
                            center = center
                        )
                    }

                    // Rotating Radar Sweep Beam
                    val rad = Math.toRadians(sweepAngle.toDouble())
                    val endX = center.x + (maxR * cos(rad)).toFloat()
                    val endY = center.y + (maxR * sin(rad)).toFloat()
                    drawLine(
                        brush = Brush.linearGradient(
                            listOf(
                                AccentTealGlow.copy(alpha = 0.8f),
                                AccentTealGlow.copy(alpha = 0.1f)
                            ),
                            start = center,
                            end = Offset(endX, endY)
                        ),
                        start = center,
                        end = Offset(endX, endY),
                        strokeWidth = 2.dp.toPx()
                    )
                }

                // Weather symbols positioned across radar sectors
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WeatherConditionIcon(condition = activeDay?.condition ?: WeatherCondition.RAIN, iconSize = 24.dp)
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = null,
                        tint = AccentTealGlow,
                        modifier = Modifier.size(18.dp)
                    )
                    WeatherConditionIcon(condition = activeDay?.condition ?: WeatherCondition.CLOUDY, iconSize = 24.dp)
                }
            }

            // Date-Based Timeline Scrubber
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                days.forEachIndexed { idx, d ->
                    val isSelected = idx == selectedIndex
                    Surface(
                        onClick = { onSelectDay(idx) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) AccentTealGlow else CardNavyElevated,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) AccentTealGlow else BorderNavySubtle
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (d.isToday) "Today" else if (d.isTomorrow) "Tomorrow" else d.dayName.take(3),
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                                color = if (isSelected) DeepNavyBg else TextPrimaryWhite
                            )
                            Text(
                                text = "• %.0fmm".format(d.totalRain),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) DeepNavyBg.copy(alpha = 0.8f) else AccentSkyBlue
                            )
                        }
                    }
                }
            }
        }
    }
}
