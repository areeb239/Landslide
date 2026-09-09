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
    var showSOSDialog by remember { mutableStateOf(false) }
    var selectedSeverityFilter by remember { mutableStateOf<AlertSeverity?>(null) }

    LaunchedEffect(uiState.sosState) {
        if (uiState.sosState == SOSState.SENT) {
            kotlinx.coroutines.delay(3500)
            viewModel.resetSOSState()
        }
    }

    if (showSOSDialog) {
        AlertDialog(
            onDismissRequest = { showSOSDialog = false },
            containerColor = SurfaceDark,
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(SeverityCritical.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Emergency, null, tint = SeverityCritical, modifier = Modifier.size(28.dp))
                }
            },
            title = {
                Text(
                    "TRIGGER EMERGENCY SOS?",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = OnBackgroundDark,
                    letterSpacing = 0.5.sp
                )
            },
            text = {
                Text(
                    "This broadcasts your verified GPS coordinates directly to the State Disaster Management Authority (SDMA) and District Incident Response Teams.\n\nUse solely in case of immediate slope failure, structural collapse, or life hazard.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    lineHeight = 19.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSOSDialog = false
                        viewModel.onSOSTrigger()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SeverityCritical),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("TRANSMIT SOS", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSOSDialog = false }) {
                    Text("Cancel", color = TextMuted, fontSize = 12.sp)
                }
            }
        )
    }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ObsidianBase)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Top telemetry status line
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PulsingStatusDot(color = Primary80, size = 6.dp)
                        Text(
                            text = "TELEMETRY RADAR: ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary80,
                            letterSpacing = 0.8.sp
                        )
                    }

                    // Admin panel shortcut if authorized
                    if (uiState.currentUser?.role == UserRole.ADMIN) {
                        Surface(
                            onClick = onNavigateToAdmin,
                            shape = RoundedCornerShape(14.dp),
                            color = SurfaceVariantDark,
                            border = BorderStroke(1.dp, BorderSubtle)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.AdminPanelSettings, null, tint = TextMuted, modifier = Modifier.size(13.dp))
                                Text("ADMIN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = OnSurfaceDark)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // Main App Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "BHURAKSHAK",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = OnBackgroundDark
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Primary80.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, Primary80.copy(alpha = 0.35f))
                            ) {
                                Text(
                                    text = "NER 2.0",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary80,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Himalayan Landslide Early Warning & Response",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = onNavigateToWeather,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SurfaceVariantDark)
                                .border(1.dp, BorderSubtle, CircleShape)
                        ) {
                            Icon(Icons.Default.Cloud, "Weather Radar", tint = CyberCyan, modifier = Modifier.size(18.dp))
                        }
                        IconButton(
                            onClick = onNavigateToPrediction,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SurfaceVariantDark)
                                .border(1.dp, BorderSubtle, CircleShape)
                        ) {
                            Icon(Icons.Default.Psychology, "AI Predictor", tint = Primary80, modifier = Modifier.size(18.dp))
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
                        indicatorColor = CyberCyan,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryInstrumentTile(
                        title = "Soil Sat",
                        value = "64%",
                        unit = "Sat",
                        progressFraction = 0.64f,
                        indicatorColor = Secondary80,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryInstrumentTile(
                        title = "Sectors",
                        value = "8",
                        unit = "Live",
                        progressFraction = 0.80f,
                        indicatorColor = Primary80,
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
                        color = SeverityCritical.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, SeverityCritical.copy(alpha = 0.5f))
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
                                    .background(SeverityCritical),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Column {
                                Text(
                                    "EMERGENCY SOS DISPATCHED",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = SeverityCritical,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    "Your GPS telemetry has been relayed to SDRF / NDMA quick-response teams.",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = OnBackgroundDark
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
                                color = SurfaceVariantDark,
                                border = BorderStroke(1.dp, BorderSubtle)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Sensors, null, tint = Primary80, modifier = Modifier.size(11.dp))
                                    Text("LIVE FEED", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Primary80)
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
                        Surface(
                            onClick = { selectedSeverityFilter = null },
                            shape = RoundedCornerShape(6.dp),
                            color = if (selectedSeverityFilter == null) Primary80.copy(alpha = 0.18f) else SurfaceDark,
                            border = BorderStroke(1.dp, if (selectedSeverityFilter == null) Primary80 else BorderSubtle)
                        ) {
                            Text(
                                text = "All (${uiState.alerts.size})",
                                fontSize = 11.sp,
                                fontWeight = if (selectedSeverityFilter == null) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedSeverityFilter == null) Primary80 else TextMuted,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // Critical Filter
                        val critCount = uiState.alerts.count { it.severity == AlertSeverity.CRITICAL }
                        Surface(
                            onClick = { selectedSeverityFilter = AlertSeverity.CRITICAL },
                            shape = RoundedCornerShape(6.dp),
                            color = if (selectedSeverityFilter == AlertSeverity.CRITICAL) SeverityCritical.copy(alpha = 0.18f) else SurfaceDark,
                            border = BorderStroke(1.dp, if (selectedSeverityFilter == AlertSeverity.CRITICAL) SeverityCritical else BorderSubtle)
                        ) {
                            Text(
                                text = "Critical ($critCount)",
                                fontSize = 11.sp,
                                fontWeight = if (selectedSeverityFilter == AlertSeverity.CRITICAL) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedSeverityFilter == AlertSeverity.CRITICAL) SeverityCritical else TextMuted,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // High Filter
                        val highCount = uiState.alerts.count { it.severity == AlertSeverity.HIGH }
                        Surface(
                            onClick = { selectedSeverityFilter = AlertSeverity.HIGH },
                            shape = RoundedCornerShape(6.dp),
                            color = if (selectedSeverityFilter == AlertSeverity.HIGH) SeverityHigh.copy(alpha = 0.18f) else SurfaceDark,
                            border = BorderStroke(1.dp, if (selectedSeverityFilter == AlertSeverity.HIGH) SeverityHigh else BorderSubtle)
                        ) {
                            Text(
                                text = "Warning ($highCount)",
                                fontSize = 11.sp,
                                fontWeight = if (selectedSeverityFilter == AlertSeverity.HIGH) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedSeverityFilter == AlertSeverity.HIGH) SeverityHigh else TextMuted,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // Moderate Filter
                        val modCount = uiState.alerts.count { it.severity == AlertSeverity.MODERATE }
                        Surface(
                            onClick = { selectedSeverityFilter = AlertSeverity.MODERATE },
                            shape = RoundedCornerShape(6.dp),
                            color = if (selectedSeverityFilter == AlertSeverity.MODERATE) SeverityModerate.copy(alpha = 0.18f) else SurfaceDark,
                            border = BorderStroke(1.dp, if (selectedSeverityFilter == AlertSeverity.MODERATE) SeverityModerate else BorderSubtle)
                        ) {
                            Text(
                                text = "Advisory ($modCount)",
                                fontSize = 11.sp,
                                fontWeight = if (selectedSeverityFilter == AlertSeverity.MODERATE) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedSeverityFilter == AlertSeverity.MODERATE) SeverityModerate else TextMuted,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
                        CircularProgressIndicator(color = Primary80, strokeWidth = 2.5.dp, modifier = Modifier.size(32.dp))
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
                                    .background(Primary80)
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                "SECTORS NOMINAL",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                letterSpacing = 0.5.sp,
                                color = OnBackgroundDark
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                "No active slope failures matching this filter across monitored corridors.",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = TextMuted,
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
                        .background(SurfaceVariantDark)
                        .border(1.dp, BorderSubtle, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.name.take(1).uppercase(),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = OnBackgroundDark
                    )
                }
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Primary80)
                        .border(1.5.dp, SurfaceDark, CircleShape)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = OnBackgroundDark
                )
                Text(
                    text = "SECTOR: Eastern Himalayas (Sikkim / Assam Corridor)",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }

            Surface(
                shape = RoundedCornerShape(4.dp),
                color = SurfaceVariantDark,
                border = BorderStroke(1.dp, BorderSubtle)
            ) {
                Text(
                    text = user.role.name,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
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
                            .background(SurfaceVariantDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Psychology, null, tint = Primary80, modifier = Modifier.size(17.dp))
                    }
                    Text(
                        text = "AI HAZARD NEURAL CORE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp,
                        color = OnBackgroundDark
                    )
                }

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = SurfaceVariantDark,
                    border = BorderStroke(1.dp, BorderSubtle)
                ) {
                    Text(
                        text = "ONLINE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary80,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = "Evaluate geotechnical slope failure probability using live 24h rainfall saturation, antecedent precipitation index, and digital elevation models.",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = TextMuted,
                lineHeight = 16.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onRunPredict,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary80),
                    modifier = Modifier.weight(1.2f).height(40.dp)
                ) {
                    Icon(Icons.Default.Bolt, null, modifier = Modifier.size(15.dp), tint = Color.White)
                    Spacer(Modifier.width(6.dp))
                    Text("Calculate Risk", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onOpenDoppler,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.weight(1f).height(40.dp)
                ) {
                    Icon(Icons.Default.Radar, null, modifier = Modifier.size(15.dp), tint = CyberCyan)
                    Spacer(Modifier.width(6.dp))
                    Text("Doppler", fontWeight = FontWeight.SemiBold, color = OnBackgroundDark, fontSize = 12.sp)
                }
            }
        }
    }
}
