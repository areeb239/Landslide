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
    val themeController = LocalThemeController.current
    val localeController = LocalLocaleController.current
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showSOSDialog by remember { mutableStateOf(false) }
    var selectedSeverityFilter by remember { mutableStateOf<AlertSeverity?>(null) }

    LaunchedEffect(uiState.sosState) {
        if (uiState.sosState == SOSState.SENT) {
            kotlinx.coroutines.delay(3500)
            viewModel.resetSOSState()
        }
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(onDismissRequest = { showLanguageDialog = false })
    }

    if (showSOSDialog) {
        AlertDialog(
            onDismissRequest = { showSOSDialog = false },
            containerColor = colors.bgSurface,
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(colors.critical.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Emergency, null, tint = colors.critical, modifier = Modifier.size(28.dp))
                }
            },
            title = {
                Text(
                    "TRIGGER EMERGENCY SOS?",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = colors.textPrimary,
                    letterSpacing = 0.5.sp
                )
            },
            text = {
                Text(
                    "This broadcasts your verified GPS coordinates directly to the State Disaster Management Authority (SDMA) and District Incident Response Teams.\n\nUse solely in case of immediate slope failure, structural collapse, or life hazard.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    lineHeight = 19.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSOSDialog = false
                        viewModel.onSOSTrigger()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.critical),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("TRANSMIT SOS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSOSDialog = false }) {
                    Text("Cancel", color = colors.textSecondary, fontSize = 12.sp)
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
                            text = "TELEMETRY RADAR: ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.accent,
                            letterSpacing = 0.8.sp
                        )
                    }

                    // Global utility controls: Admin, Language Selector, Theme Switcher
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
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
                                text = "BHURAKSHAK",
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
                            text = "Himalayan Landslide Early Warning & Response",
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

            // Real-time Regional Telemetry Instrumentation (Inline Micro-Gauges)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TelemetryInstrumentTile(
                        title = "Rainfall",
                        value = "48.2",
                        unit = "mm",
                        progressFraction = 0.48f,
                        indicatorColor = colors.accent,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryInstrumentTile(
                        title = "Soil Sat",
                        value = "64%",
                        unit = "Sat",
                        progressFraction = 0.64f,
                        indicatorColor = colors.warning,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryInstrumentTile(
                        title = "Sectors",
                        value = "8",
                        unit = "Live",
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
                EmergencySOSBar(onTriggerSOS = { showSOSDialog = true })
            }

            // Secondary Utility: AI Hazard Neural Core
            item {
                HeroAiFeatureCard(
                    onRunPredict = onNavigateToPrediction,
                    onOpenDoppler = onNavigateToWeather
                )
            }

            // Section Header: Regional Hazard Feed with Severity Filters
            item {
                Spacer(Modifier.height(4.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader(
                        title = "Regional Hazard Feed",
                        subtitle = "${uiState.alerts.size} active alert(s) across Eastern Himalayas",
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
                                    Text("LIVE FEED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = colors.accent)
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
                                text = "All (${uiState.alerts.size})",
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
                                text = "Critical ($critCount)",
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
                                text = "Warning ($highCount)",
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
                                text = "Advisory ($modCount)",
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
                                "SECTORS NOMINAL",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                letterSpacing = 0.5.sp,
                                color = colors.textPrimary
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                "No active slope failures matching this filter across monitored corridors.",
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
    onOpenDoppler: () -> Unit
) {
    val colors = BhurakshakTheme.colors
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
                        text = "AI HAZARD NEURAL CORE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp,
                        color = colors.textPrimary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = colors.accent.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, colors.accent.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "ONLINE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent,
                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "Evaluate geotechnical slope failure probability using live 24h rainfall saturation, antecedent precipitation index, and digital elevation models.",
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
                    Text("Calculate Risk", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
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
                    Text("Doppler", fontWeight = FontWeight.SemiBold, color = colors.textPrimary, fontSize = 12.sp)
                }
            }
        }
    }
}
