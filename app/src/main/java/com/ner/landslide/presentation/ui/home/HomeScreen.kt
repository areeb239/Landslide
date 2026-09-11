package com.ner.landslide.presentation.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ner.landslide.domain.model.*
import com.ner.landslide.presentation.ui.components.*
import com.ner.landslide.presentation.ui.theme.*
import com.ner.landslide.presentation.viewmodel.*
import com.ner.landslide.util.toRelativeTimeString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToPrediction: () -> Unit,
    onNavigateToWeather: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = BhurakshakTheme.colors
    val strings = LocalAppStrings.current
    val themeController = LocalThemeController.current
    val localeController = LocalLocaleController.current
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showSOSDialog by remember { mutableStateOf(false) }
    var selectedSeverityFilter by remember { mutableStateOf<AlertSeverity?>(null) }

    var isCitizenMode by rememberSaveable { mutableStateOf(true) }
    var countdownSeconds by remember { mutableIntStateOf(5) }
    var isCountingDown by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        com.ner.landslide.util.NetworkMonitor.getInstance(context).refresh()
    }

    LaunchedEffect(uiState.sosState) {
        if (uiState.sosState == SOSState.SENT) {
            kotlinx.coroutines.delay(3500)
            viewModel.resetSOSState()
        }
    }

    LaunchedEffect(showSOSDialog) {
        if (showSOSDialog) {
            countdownSeconds = 5
            isCountingDown = true
        } else {
            isCountingDown = false
        }
    }

    LaunchedEffect(showSOSDialog, countdownSeconds, isCountingDown) {
        if (showSOSDialog && isCountingDown && countdownSeconds > 0) {
            kotlinx.coroutines.delay(1000)
            if (showSOSDialog && isCountingDown) {
                countdownSeconds -= 1
                if (countdownSeconds == 0) {
                    showSOSDialog = false
                    isCountingDown = false
                    viewModel.onSOSTrigger()
                }
            }
        }
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(onDismissRequest = { showLanguageDialog = false })
    }

    if (showSOSDialog) {
        AlertDialog(
            onDismissRequest = {
                showSOSDialog = false
                isCountingDown = false
            },
            containerColor = colors.bgSurface,
            icon = {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(colors.critical.copy(alpha = 0.2f))
                        .border(2.dp, colors.critical, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$countdownSeconds",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.critical
                    )
                }
            },
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = strings.emergencySosTitle,
                        fontWeight = FontWeight.Black,
                        fontSize = 17.sp,
                        color = colors.critical,
                        letterSpacing = 0.5.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Text(
                        text = "Broadcasting to SDRF / NDMA in $countdownSeconds seconds...",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        color = colors.textSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = colors.bgBase,
                        border = BorderStroke(1.dp, colors.borderDefault),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "LOCKED GPS RESCUE TELEMETRY",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textSecondary
                            )
                            Text(
                                text = String.format(java.util.Locale.US, "%.4f° N, %.4f° E", uiState.sosLocationLat, uiState.sosLocationLon),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                        }
                    }

                    Text(
                        text = strings.sosDescription,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.5.sp,
                        color = colors.textSecondary,
                        lineHeight = 16.sp
                    )

                    LinearProgressIndicator(
                        progress = { (countdownSeconds / 5f).coerceIn(0f, 1f) },
                        color = colors.critical,
                        trackColor = colors.critical.copy(alpha = 0.2f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSOSDialog = false
                        isCountingDown = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.critical),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = strings.cancel.uppercase(),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showSOSDialog = false
                        isCountingDown = false
                        viewModel.onSOSTrigger()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${strings.transmit} →",
                        color = colors.textSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        )
    }

    Scaffold(
        containerColor = colors.bgBase,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.bgBase)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Top telemetry status & global utility controls line
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PulsingStatusDot(color = colors.accent, size = 6.dp)
                        Text(
                            text = if (isCitizenMode) "SAFETY MONITOR: ACTIVE" else strings.telemetryRadarActive,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent,
                            letterSpacing = 0.8.sp
                        )
                    }

                    // Global utility controls: Citizen/Official Mode, Admin, Language Selector, Theme Switcher
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Plain-Language Citizen Mode vs Official Mode Switch
                        Surface(
                            onClick = { isCitizenMode = !isCitizenMode },
                            shape = RoundedCornerShape(16.dp),
                            color = colors.bgSurface,
                            border = BorderStroke(1.dp, if (isCitizenMode) colors.accent else colors.warning)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isCitizenMode) Icons.Default.Person else Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = if (isCitizenMode) colors.accent else colors.warning,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = if (isCitizenMode) "Citizen" else "Official",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCitizenMode) colors.accent else colors.warning
                                )
                            }
                        }

                        if (uiState.currentUser?.role == UserRole.ADMIN) {
                            Surface(
                                onClick = onNavigateToAdmin,
                                shape = RoundedCornerShape(12.dp),
                                color = colors.bgSurface,
                                border = BorderStroke(1.dp, colors.borderDefault)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.AdminPanelSettings, null, tint = colors.textSecondary, modifier = Modifier.size(12.dp))
                                    Text("ADMIN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                }
                            }
                        }

                        // Compact Language Selector Pill
                        Surface(
                            onClick = { showLanguageDialog = true },
                            shape = RoundedCornerShape(16.dp),
                            color = colors.bgSurface,
                            border = BorderStroke(1.dp, colors.borderDefault)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Language, null, tint = colors.accent, modifier = Modifier.size(13.dp))
                                Text(
                                    text = localeController.currentLanguage.value.badgeCode,
                                    color = colors.accent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        // Compact Theme Switcher Button
                        Surface(
                            onClick = { themeController.toggleTheme() },
                            shape = CircleShape,
                            color = colors.bgSurface,
                            border = BorderStroke(1.dp, colors.borderDefault),
                            modifier = Modifier.size(30.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (colors.isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = if (colors.isDark) "Switch to Field Daylight Theme" else "Switch to Control-Room Blue Theme",
                                    tint = colors.accent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Main App Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = strings.appTitle,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = colors.textPrimary
                            )
                            
                                Text(
                                    text = "NER 2.0",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.accent,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                        }
                        Text(
                            text = strings.appSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }

                    // Tactical Action Shortcuts: Weather Radar & AI Predictor
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = onNavigateToWeather,
                            shape = RoundedCornerShape(10.dp),
                            color = colors.bgSurface,
                            border = BorderStroke(1.dp, colors.borderDefault),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Cloud, "Weather Radar", tint = colors.accent, modifier = Modifier.size(18.dp))
                            }
                        }
                        Surface(
                            onClick = onNavigateToPrediction,
                            shape = RoundedCornerShape(10.dp),
                            color = colors.bgSurface,
                            border = BorderStroke(1.dp, colors.borderDefault),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Psychology, "AI Predictor", tint = colors.accent, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Citizen Profile Ribbon
            item {
                uiState.currentUser?.let { user ->
                    MissionUserProfileCard(user = user)
                }
            }

            // Real Offline State Banner (Emergency Buffer Active)
            if (!uiState.isOnline) {
                item {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = colors.warning.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, colors.warning.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = colors.warning,
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = "OFFLINE MODE — CACHED RISK DATA",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp,
                                    color = colors.warning,
                                    letterSpacing = 0.4.sp
                                )
                                Text(
                                    text = "Offline — showing last known risk from ${uiState.lastSyncedTime.toRelativeTimeString()}. Emergency SOS and disaster reports are queued locally for transmission.",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 10.5.sp,
                                    color = colors.textPrimary,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }

            // Real-time Regional Telemetry Instrumentation (Inline Micro-Gauges)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TelemetryInstrumentTile(
                        title = strings.rainfall,
                        value = "48.2",
                        unit = "mm",
                        progressFraction = 0.48f,
                        indicatorColor = colors.accent,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryInstrumentTile(
                        title = strings.soilSat,
                        value = "64%",
                        unit = "Sat",
                        progressFraction = 0.64f,
                        indicatorColor = colors.warning,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryInstrumentTile(
                        title = strings.sectors,
                        value = "8",
                        unit = strings.live,
                        progressFraction = 0.80f,
                        indicatorColor = colors.accent,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // SOS Confirmation Feedback Banner
            item {
                AnimatedVisibility(
                    visible = uiState.sosState == SOSState.SENT,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = colors.critical.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, colors.critical.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(colors.critical),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Text(
                                    "EMERGENCY SOS DISPATCHED",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = colors.critical,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    "Your GPS telemetry has been relayed to SDRF / NDMA quick-response teams.",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = colors.textPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Embedded Emergency SOS Dispatch Action Bar (Tactile Crimson Treatment)
            item {
                EmergencySOSBar(
                    onTriggerSOS = { showSOSDialog = true },
                    isCitizenMode = isCitizenMode
                )
            }

            // Secondary Utility: AI Hazard Prediction Card
            item {
                HeroAiFeatureCard(
                    onRunPredict = onNavigateToPrediction,
                    onOpenDoppler = onNavigateToWeather,
                    isCitizenMode = isCitizenMode,
                    isOnline = uiState.isOnline
                )
            }

            // Section Header: Regional Hazard Feed with Severity Filters
            item {
                Spacer(Modifier.height(4.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader(
                        title = strings.regionalHazardFeed,
                        subtitle = "${uiState.alerts.size} ${strings.activeAlertsSuffix}",
                        trailingContent = {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = colors.bgSurface,
                                border = BorderStroke(1.dp, colors.borderDefault)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Sensors, null, tint = colors.accent, modifier = Modifier.size(11.dp))
                                    Text(strings.liveFeedBadge, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = colors.accent)
                                }
                            }
                        }
                    )

                    // Severity Filter Tabs Row
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        // All Filter
                        val isAllSelected = selectedSeverityFilter == null
                        Surface(
                            onClick = { selectedSeverityFilter = null },
                            shape = RoundedCornerShape(6.dp),
                            color = if (isAllSelected) colors.accent else colors.bgSurface,
                            border = BorderStroke(1.dp, if (isAllSelected) colors.accent else colors.borderDefault),
                            shadowElevation = if (colors.isDark) 0.dp else 1.dp
                        ) {
                            Text(
                                text = "${strings.filterAll} (${uiState.alerts.size})",
                                fontSize = 11.sp,
                                fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isAllSelected) Color.White else colors.textSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }

                        // Critical Filter
                        val isCritSelected = selectedSeverityFilter == AlertSeverity.CRITICAL
                        val critCount = uiState.alerts.count { it.severity == AlertSeverity.CRITICAL }
                        Surface(
                            onClick = { selectedSeverityFilter = AlertSeverity.CRITICAL },
                            shape = RoundedCornerShape(6.dp),
                            color = if (isCritSelected) colors.critical else colors.bgSurface,
                            border = BorderStroke(1.dp, if (isCritSelected) colors.critical else colors.borderDefault),
                            shadowElevation = if (colors.isDark) 0.dp else 1.dp
                        ) {
                            Text(
                                text = "${strings.filterCritical} ($critCount)",
                                fontSize = 11.sp,
                                fontWeight = if (isCritSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCritSelected) Color.White else colors.textSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }

                        // High Filter
                        val isHighSelected = selectedSeverityFilter == AlertSeverity.HIGH
                        val highCount = uiState.alerts.count { it.severity == AlertSeverity.HIGH }
                        Surface(
                            onClick = { selectedSeverityFilter = AlertSeverity.HIGH },
                            shape = RoundedCornerShape(6.dp),
                            color = if (isHighSelected) colors.warning else colors.bgSurface,
                            border = BorderStroke(1.dp, if (isHighSelected) colors.warning else colors.borderDefault),
                            shadowElevation = if (colors.isDark) 0.dp else 1.dp
                        ) {
                            Text(
                                text = "${strings.filterWarning} ($highCount)",
                                fontSize = 11.sp,
                                fontWeight = if (isHighSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isHighSelected) Color.White else colors.textSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }

                        // Moderate Filter
                        val isModSelected = selectedSeverityFilter == AlertSeverity.MODERATE
                        val modCount = uiState.alerts.count { it.severity == AlertSeverity.MODERATE }
                        Surface(
                            onClick = { selectedSeverityFilter = AlertSeverity.MODERATE },
                            shape = RoundedCornerShape(6.dp),
                            color = if (isModSelected) colors.warning else colors.bgSurface,
                            border = BorderStroke(1.dp, if (isModSelected) colors.warning else colors.borderDefault),
                            shadowElevation = if (colors.isDark) 0.dp else 1.dp
                        ) {
                            Text(
                                text = "${strings.filterAdvisory} ($modCount)",
                                fontSize = 11.sp,
                                fontWeight = if (isModSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isModSelected) Color.White else colors.textSecondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            // Filtered Alerts List
            val displayedAlerts = if (selectedSeverityFilter != null) {
                uiState.alerts.filter { it.severity == selectedSeverityFilter }
            } else {
                uiState.alerts
            }

            if (uiState.isLoading) {
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = colors.accent, strokeWidth = 2.5.dp, modifier = Modifier.size(32.dp))
                    }
                }
            } else if (displayedAlerts.isEmpty()) {
                item {
                    FieldCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(colors.accent)
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = if (isCitizenMode) "ALL CLEAR" else "SECTORS NOMINAL",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                letterSpacing = 0.5.sp,
                                color = colors.textPrimary
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = if (isCitizenMode) "No active landslide warnings reported in your area right now." else "No active slope failures matching this filter across monitored corridors.",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = colors.textSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(displayedAlerts, key = { it.id }) { alert ->
                    AlertCard(alert = alert)
                }
            }

            // Bottom clearance padding above navigation dock
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun MissionUserProfileCard(user: User) {
    val colors = BhurakshakTheme.colors
    FieldCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(colors.bgSurface)
                        .border(1.dp, colors.borderDefault, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.name.take(1).uppercase(),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = colors.textPrimary
                    )
                }
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(colors.accent)
                        .border(1.5.dp, colors.bgSurface, CircleShape)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = colors.textPrimary
                )
                Text(
                    text = "SECTOR: Eastern Himalayas (Sikkim / Assam Corridor)",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = colors.textSecondary
                )
            }

            Surface(
                shape = RoundedCornerShape(4.dp),
                color = colors.bgSurface,
                border = BorderStroke(1.dp, colors.borderDefault)
            ) {
                Text(
                    text = user.role.name,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textSecondary,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun HeroAiFeatureCard(
    onRunPredict: () -> Unit,
    onOpenDoppler: () -> Unit,
    isCitizenMode: Boolean = true,
    isOnline: Boolean = true
) {
    val colors = BhurakshakTheme.colors
    val strings = LocalAppStrings.current
    FieldCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            .size(28.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(colors.accent.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Psychology, null, tint = colors.accent, modifier = Modifier.size(17.dp))
                    }
                    Text(
                        text = if (isCitizenMode) "CHECK LANDSLIDE RISK" else strings.aiHazardNeuralCore,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp,
                        color = colors.textPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isOnline) colors.success.copy(alpha = 0.12f) else colors.warning.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, if (isOnline) colors.success.copy(alpha = 0.3f) else colors.warning.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = if (isOnline) strings.online else strings.offline,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isOnline) colors.success else colors.warning,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = if (isCitizenMode) "Check if your village, slope, or travel route is at risk of landslide based on recent rain and steep terrain." else strings.aiHazardDescription,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = colors.textSecondary,
                lineHeight = 16.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onRunPredict,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
                    modifier = Modifier.weight(1.2f).height(40.dp)
                ) {
                    Icon(Icons.Default.Bolt, null, modifier = Modifier.size(15.dp), tint = Color.White)
                    Spacer(Modifier.width(6.dp))
                    Text(if (isCitizenMode) "Check My Risk" else strings.calculateRisk, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onOpenDoppler,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, colors.borderDefault),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textPrimary),
                    modifier = Modifier.weight(1f).height(40.dp)
                ) {
                    Icon(Icons.Default.Radar, null, modifier = Modifier.size(15.dp), tint = colors.accent)
                    Spacer(Modifier.width(6.dp))
                    Text(if (isCitizenMode) "Rain Radar" else strings.doppler, fontWeight = FontWeight.SemiBold, color = colors.textPrimary, fontSize = 12.sp)
                }
            }
        }
    }
}
