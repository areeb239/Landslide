package com.ner.landslide.presentation.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(SeverityCritical.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Emergency, null, tint = SeverityCritical, modifier = Modifier.size(32.dp))
                }
            },
            title = {
                Text(
                    "TRIGGER EMERGENCY SOS?",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = OnBackgroundDark,
                    letterSpacing = 0.5.sp
                )
            },
            text = {
                Text(
                    "This broadcasts your verified GPS coordinates directly to the State Disaster Management Authority (SDMA) and District Incident Response Teams.\n\nUse solely in case of immediate slope failure, structural collapse, or life hazard.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSOSDialog = false
                        viewModel.onSOSTrigger()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SeverityCritical),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("TRANSMIT SOS", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSOSDialog = false }) {
                    Text("Cancel", color = TextMuted)
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PulsingStatusDot(color = Primary80, size = 7.dp)
                        Text(
                            text = "TELEMETRY RADAR: ACTIVE",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary80,
                            letterSpacing = 1.sp
                        )
                    }

                    // Admin panel shortcut if authorized
                    if (uiState.currentUser?.role == UserRole.ADMIN) {
                        Surface(
                            onClick = onNavigateToAdmin,
                            shape = RoundedCornerShape(20.dp),
                            color = BrandIndigo.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BrandIndigo.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.AdminPanelSettings, null, tint = BrandIndigo, modifier = Modifier.size(14.dp))
                                Text("ADMIN", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BrandIndigo)
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
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp,
                                color = OnBackgroundDark
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Primary80.copy(alpha = 0.2f),
                                border = androidx.compose.foundation.BorderStroke(0.8.dp, Primary80.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = "NER 2.0",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Primary80,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Himalayan Landslide Early Warning System",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = onNavigateToWeather,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SurfaceVariantDark)
                        ) {
                            Icon(Icons.Default.Cloud, "Weather Doppler", tint = CyberCyan, modifier = Modifier.size(20.dp))
                        }
                        IconButton(
                            onClick = onNavigateToPrediction,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SurfaceVariantDark)
                        ) {
                            Icon(Icons.Default.Psychology, "AI Predictor", tint = Primary80, modifier = Modifier.size(20.dp))
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
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Citizen Profile & Regional Telemetry Ribbon
            item {
                uiState.currentUser?.let { user ->
                    MissionUserProfileCard(user = user)
                }
            }

            // Real-time Regional Geotechnical Metrics
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TelemetryMetricItem(
                        title = "Rainfall",
                        value = "48.2",
                        unit = "mm",
                        icon = Icons.Default.WaterDrop,
                        iconColor = CyberCyan,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryMetricItem(
                        title = "Soil Sat",
                        value = "64%",
                        unit = "Sat",
                        icon = Icons.Default.Grass,
                        iconColor = Secondary80,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryMetricItem(
                        title = "Sectors",
                        value = "8",
                        unit = "Live",
                        icon = Icons.Default.Terrain,
                        iconColor = Primary80,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // SOS Confirmation Banner
            item {
                AnimatedVisibility(
                    visible = uiState.sosState == SOSState.SENT,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    GlassCard(
                        backgroundColor = SeverityCritical.copy(alpha = 0.18f),
                        borderColor = SeverityCritical.copy(alpha = 0.6f)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(SeverityCritical),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(24.dp))
                            }
                            Column {
                                Text(
                                    "EMERGENCY SOS DISPATCHED",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = SeverityCritical,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    "Your GPS telemetry has been relayed to SDRF / NDMA quick-response teams.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnBackgroundDark.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }

            // Emergency SOS Dispatch Action Card (Embedded, Never Overlaps Content)
            item {
                Surface(
                    onClick = { showSOSDialog = true },
                    shape = RoundedCornerShape(14.dp),
                    color = SeverityCritical.copy(alpha = 0.14f),
                    border = BorderStroke(1.2.dp, SeverityCritical.copy(alpha = 0.55f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SeverityCritical),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Emergency,
                                    contentDescription = "SOS",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "EMERGENCY SOS DISPATCH",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 13.sp,
                                    letterSpacing = 0.6.sp,
                                    color = SeverityCritical
                                )
                                Text(
                                    text = "One-tap GNSS broadcast to SDRF / NDMA",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = SeverityCritical
                        ) {
                            Text(
                                text = "TRANSMIT",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp,
                                letterSpacing = 0.8.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            // Hero AI Prediction Quick Launcher Card
            item {
                HeroAiFeatureCard(
                    onRunPredict = onNavigateToPrediction,
                    onOpenDoppler = onNavigateToWeather
                )
            }

            // Section Header: Live Disaster Alerts
            item {
                Spacer(Modifier.height(4.dp))
                SectionHeader(
                    title = "Regional Hazard Feed",
                    subtitle = "${uiState.alerts.size} active alert(s) across Eastern Himalayas",
                    trailingContent = {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Sensors, null, tint = CyberCyan, modifier = Modifier.size(13.dp))
                                Text("LIVE FEED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
                            }
                        }
                    }
                )
            }

            // Alerts Feed / Empty State
            if (uiState.isLoading) {
                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Primary80, strokeWidth = 3.dp)
                    }
                }
            } else if (uiState.alerts.isEmpty()) {
                item {
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = SurfaceDark.copy(alpha = 0.7f)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(32.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            PulsingStatusDot(color = Primary80, size = 12.dp)
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "ALL CORRIDORS NOMINAL",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp,
                                color = OnBackgroundDark
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "No critical slope deformations detected in Gangtok, Sevoke, or Mangan sectors.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            } else {
                items(uiState.alerts, key = { it.id }) { alert ->
                    AlertCard(alert = alert)
                }
            }

            // Bottom clearance padding
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun MissionUserProfileCard(user: User) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = SurfaceDark.copy(alpha = 0.9f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(BrandIndigo, CyberCyan))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.name.take(1).uppercase(),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Primary80)
                        .border(2.dp, SurfaceDark, CircleShape)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = OnBackgroundDark
                    )
                }
                Text(
                    text = "SECTOR: Eastern Himalayas (Sikkim / Assam Corridor)",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Primary80.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Primary80.copy(alpha = 0.4f))
            ) {
                Text(
                    text = user.role.name.replace("_", " "),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary80,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
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
    Surface(
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Brush.horizontalGradient(listOf(BrandIndigo.copy(alpha = 0.6f), CyberCyan.copy(alpha = 0.3f)))),
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF162038),
                            Color(0xFF0F172A)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
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
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(BrandIndigo.copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Psychology, null, tint = BrandIndigo, modifier = Modifier.size(20.dp))
                        }
                        Text(
                            text = "AI HAZARD NEURAL CORE",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            letterSpacing = 1.sp,
                            color = OnBackgroundDark
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.08f)
                    ) {
                        Text(
                            text = "v2.4 ONLINE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }

                Text(
                    text = "Evaluate geotechnical slope failure probability using live 24h rainfall saturation, antecedent precipitation index, and digital elevation models.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    lineHeight = 18.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onRunPredict,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary80),
                        modifier = Modifier.weight(1.3f).height(44.dp)
                    ) {
                        Icon(Icons.Default.Bolt, null, modifier = Modifier.size(16.dp), tint = ObsidianBase)
                        Spacer(Modifier.width(6.dp))
                        Text("Calculate Risk", fontWeight = FontWeight.ExtraBold, color = ObsidianBase, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = onOpenDoppler,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Icon(Icons.Default.Radar, null, modifier = Modifier.size(16.dp), tint = CyberCyan)
                        Spacer(Modifier.width(6.dp))
                        Text("Doppler", fontWeight = FontWeight.Bold, color = OnBackgroundDark, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

