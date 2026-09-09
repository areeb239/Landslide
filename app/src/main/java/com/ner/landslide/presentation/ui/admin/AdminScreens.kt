package com.ner.landslide.presentation.ui.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.ner.landslide.presentation.viewmodel.AdminViewModel
import com.ner.landslide.util.toRelativeTimeString

// ─── Admin Command Hub ──────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onNavigateBack: () -> Unit,
    onNavigateToBroadcast: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(SurfaceDark)
                        ) {
                            Icon(Icons.Default.ArrowBack, "Back", tint = OnBackgroundDark, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "COMMAND HUB",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    color = OnBackgroundDark
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = BrandIndigo.copy(alpha = 0.2f),
                                    border = BorderStroke(0.8.dp, BrandIndigo.copy(alpha = 0.6f))
                                ) {
                                    Text(
                                        text = "ADMIN",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = BrandIndigo,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                "Disaster Management & Civic Telemetry",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    // Quick Broadcast Button
                    Button(
                        onClick = onNavigateToBroadcast,
                        colors = ButtonDefaults.buttonColors(containerColor = SeverityCritical),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Campaign, null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text("BROADCAST", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, letterSpacing = 0.5.sp)
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // High-Tech Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SurfaceDark,
                contentColor = Primary80,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = if (selectedTab == 1 && uiState.sosAlerts.isNotEmpty()) SeverityCritical else Primary80,
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Assignment, null, modifier = Modifier.size(16.dp))
                            Text(
                                "Field Reports (${uiState.reports.size})",
                                fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == 0) Primary80 else TextMuted
                            )
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Emergency, null, modifier = Modifier.size(16.dp))
                            Text(
                                "SOS Alerts (${uiState.sosAlerts.size})",
                                fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                                color = if (uiState.sosAlerts.isNotEmpty()) SeverityCritical else if (selectedTab == 1) Primary80 else TextMuted
                            )
                        }
                    }
                )
            }

            when (selectedTab) {
                0 -> ReportsList(reports = uiState.reports, isLoading = uiState.isLoading)
                1 -> SOSAlertsList(
                    sosAlerts = uiState.sosAlerts,
                    onResolve = { viewModel.resolveSOSAlert(it) }
                )
            }
        }
    }
}

@Composable
private fun ReportsList(reports: List<IncidentReport>, isLoading: Boolean) {
    if (isLoading) {
        LoadingContent()
    } else if (reports.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Inbox, null, tint = TextMuted, modifier = Modifier.size(44.dp))
                Spacer(Modifier.height(8.dp))
                Text("No citizen incident reports filed yet", color = TextMuted, fontSize = 13.sp)
            }
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(reports, key = { it.id }) { report ->
                ReportCard(report = report)
            }
        }
    }
}

@Composable
private fun ReportCard(report: IncidentReport) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = SurfaceDark.copy(alpha = 0.9f),
        borderColor = report.severity.toColor().copy(alpha = 0.35f)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                HazardBadge(severity = report.severity)
                Text(
                    report.reportedAt.toRelativeTimeString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
            Text(
                report.incidentType.name.replace("_", " "),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = OnBackgroundDark
            )
            if (report.description.isNotBlank()) {
                Text(
                    report.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnBackgroundDark.copy(alpha = 0.8f),
                    maxLines = 3
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Icon(Icons.Default.Person, null, modifier = Modifier.size(14.dp), tint = Primary80)
                Text(report.reporterName, style = MaterialTheme.typography.labelSmall, color = Primary80, fontWeight = FontWeight.Medium)
                if (report.district.isNotBlank()) {
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(14.dp), tint = CyberCyan)
                    Text(report.district, style = MaterialTheme.typography.labelSmall, color = CyberCyan, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun SOSAlertsList(sosAlerts: List<SOSAlert>, onResolve: (String) -> Unit) {
    if (sosAlerts.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.CheckCircleOutline, null, tint = Primary80, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(8.dp))
                Text("All clear — no active SOS distress signals", color = TextMuted, fontSize = 13.sp)
            }
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(sosAlerts, key = { it.id }) { sos ->
                SOSCard(sos = sos, onResolve = { onResolve(sos.id) })
            }
        }
    }
}

@Composable
private fun SOSCard(sos: SOSAlert, onResolve: () -> Unit) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = SurfaceDark.copy(alpha = 0.95f),
        borderColor = SeverityCritical.copy(alpha = 0.6f)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PulsingStatusDot(color = SeverityCritical, size = 8.dp)
                    Text("EMERGENCY DISTRESS BEACON", color = SeverityCritical, fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 0.8.sp)
                }
                Text(
                    sos.triggeredAt.toRelativeTimeString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
            Text(sos.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = OnBackgroundDark)

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = SurfaceElevated,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.GpsFixed, null, tint = Primary80, modifier = Modifier.size(14.dp))
                    Text(
                        "%.5f° N, %.5f° E".format(sos.latitude, sos.longitude),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = OnBackgroundDark
                    )
                }
            }

            Button(
                onClick = onResolve,
                modifier = Modifier.fillMaxWidth().height(42.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary80),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp), tint = ObsidianBase)
                Spacer(Modifier.width(6.dp))
                Text("MARK RESOLVED", color = ObsidianBase, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

// ─── Regional Hazard Broadcast Screen ──────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BroadcastAlertScreen(
    onNavigateBack: () -> Unit,
    onAlertSent: () -> Unit,
    viewModel: AdminViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.broadcastSuccess) {
        if (uiState.broadcastSuccess) onAlertSent()
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SurfaceDark)
                    ) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = OnBackgroundDark, modifier = Modifier.size(20.dp))
                    }
                    Column {
                        Text(
                            text = "BROADCAST ALERT",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = OnBackgroundDark
                        )
                        Text(
                            "Immediate Emergency Alert Broadcast to All Units",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Warning Glass Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SeverityCritical.copy(alpha = 0.1f),
                borderColor = SeverityCritical.copy(alpha = 0.4f)
            ) {
                Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, null, tint = SeverityCritical, modifier = Modifier.size(22.dp))
                    Text(
                        "Transmitting this alert will send an instant push notification and update live hazard telemetry across all connected civilian and response devices.",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnBackgroundDark.copy(alpha = 0.9f),
                        lineHeight = 18.sp
                    )
                }
            }

            // Broadcast Form Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceDark.copy(alpha = 0.95f),
                borderColor = Color.White.copy(alpha = 0.1f)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        "ALERT SPECIFICATIONS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        color = Primary80
                    )

                    OutlinedTextField(
                        value = uiState.broadcastTitle,
                        onValueChange = { viewModel.onBroadcastTitleChange(it) },
                        label = { Text("Alert Headline") },
                        leadingIcon = { Icon(Icons.Default.Title, null, tint = Primary80, modifier = Modifier.size(20.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary80,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = OnBackgroundDark,
                            unfocusedTextColor = OnBackgroundDark,
                            focusedLabelColor = Primary80,
                            unfocusedLabelColor = TextMuted
                        )
                    )

                    OutlinedTextField(
                        value = uiState.broadcastDescription,
                        onValueChange = { viewModel.onBroadcastDescriptionChange(it) },
                        label = { Text("Incident Advisory Details") },
                        leadingIcon = { Icon(Icons.Default.Description, null, tint = Primary80, modifier = Modifier.size(20.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 3,
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary80,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = OnBackgroundDark,
                            unfocusedTextColor = OnBackgroundDark,
                            focusedLabelColor = Primary80,
                            unfocusedLabelColor = TextMuted
                        )
                    )

                    // Severity Selector
                    Text(
                        "HAZARD SEVERITY TIER",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = TextSubtle
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AlertSeverity.entries.forEach { severity ->
                            val isSelected = uiState.broadcastSeverity == severity
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { viewModel.onBroadcastSeverityChange(severity) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) severity.toColor().copy(alpha = 0.2f) else SurfaceElevated,
                                border = BorderStroke(1.2.dp, if (isSelected) severity.toColor() else BorderSubtle)
                            ) {
                                Text(
                                    text = severity.name,
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) severity.toColor() else TextMuted
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = uiState.broadcastDistrict,
                        onValueChange = { viewModel.onBroadcastDistrictChange(it) },
                        label = { Text("Target District (e.g. Kalimpong)") },
                        leadingIcon = { Icon(Icons.Default.LocationCity, null, tint = Primary80, modifier = Modifier.size(20.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary80,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = OnBackgroundDark,
                            unfocusedTextColor = OnBackgroundDark,
                            focusedLabelColor = Primary80,
                            unfocusedLabelColor = TextMuted
                        )
                    )

                    Spacer(Modifier.height(6.dp))

                    Button(
                        onClick = { viewModel.sendBroadcast() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SeverityCritical),
                        shape = RoundedCornerShape(12.dp),
                        enabled = uiState.broadcastTitle.isNotBlank() && !uiState.isBroadcasting
                    ) {
                        if (uiState.isBroadcasting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Send, null, modifier = Modifier.size(18.dp), tint = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("DISPATCH EMERGENCY BROADCAST", fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 0.5.sp)
                        }
                    }
                }
            }
        }
    }
}
