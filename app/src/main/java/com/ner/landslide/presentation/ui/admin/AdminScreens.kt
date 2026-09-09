package com.ner.landslide.presentation.ui.admin

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.ner.landslide.domain.model.*
import com.ner.landslide.presentation.ui.components.*
import com.ner.landslide.presentation.ui.theme.*
import com.ner.landslide.presentation.viewmodel.AdminViewModel
import com.ner.landslide.util.toRelativeTimeString

// ─── Admin Dashboard ──────────────────────────────────────────────────────────

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
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    Button(
                        onClick = onNavigateToBroadcast,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(Icons.Default.Campaign, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Broadcast", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Tab Row
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Report, null, modifier = Modifier.size(16.dp))
                            Text("Reports (${uiState.reports.size})")
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Emergency, null, modifier = Modifier.size(16.dp))
                            Text("SOS (${uiState.sosAlerts.size})", color = if (uiState.sosAlerts.isNotEmpty()) SeverityCritical else MaterialTheme.colorScheme.onSurface)
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
        ErrorContent(message = "No reports yet", modifier = Modifier.fillMaxSize())
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
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, report.severity.toColor().copy(0.3f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                SeverityBadge(severity = report.severity)
                Text(report.reportedAt.toRelativeTimeString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
            }
            Text(
                report.incidentType.name.replace("_", " "),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall
            )
            if (report.description.isNotBlank()) {
                Text(report.description, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.7f), maxLines = 2)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.Person, null, modifier = Modifier.size(14.dp), tint = Primary80)
                Text(report.reporterName, style = MaterialTheme.typography.labelSmall, color = Primary80)
                Spacer(Modifier.width(8.dp))
                if (report.district.isNotBlank()) {
                    Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(14.dp), tint = Primary80)
                    Text(report.district, style = MaterialTheme.typography.labelSmall, color = Primary80)
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
                Icon(Icons.Default.CheckCircleOutline, null, tint = SeverityLow, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(8.dp))
                Text("No active SOS alerts", color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
            }
        }
    } else {
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(sosAlerts, key = { it.id }) { sos ->
                SOSCard(sos = sos, onResolve = { onResolve(sos.id) })
            }
        }
    }
}

@Composable
private fun SOSCard(sos: SOSAlert, onResolve: () -> Unit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SeverityCritical.copy(0.08f)),
        border = BorderStroke(1.5.dp, SeverityCritical.copy(0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Emergency, null, tint = SeverityCritical, modifier = Modifier.size(20.dp))
                    Text("SOS ALERT", color = SeverityCritical, fontWeight = FontWeight.ExtraBold)
                }
                Text(sos.triggeredAt.toRelativeTimeString(), style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
            }
            Text(sos.name, fontWeight = FontWeight.SemiBold)
            Text("%.5f, %.5f".format(sos.latitude, sos.longitude),
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
            Button(
                onClick = onResolve,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = SeverityLow)
            ) {
                Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Mark Resolved")
            }
        }
    }
}

// ─── Broadcast Alert Screen ───────────────────────────────────────────────────

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
        topBar = {
            TopAppBar(
                title = { Text("Broadcast Alert", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Warning
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SeverityCritical.copy(0.1f)),
                border = BorderStroke(1.dp, SeverityCritical.copy(0.3f))
            ) {
                Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.Warning, null, tint = SeverityCritical, modifier = Modifier.size(20.dp))
                    Text(
                        "This alert will be broadcast to ALL users in the region via push notification.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(0.85f)
                    )
                }
            }

            OutlinedTextField(
                value = uiState.broadcastTitle,
                onValueChange = { viewModel.onBroadcastTitleChange(it) },
                label = { Text("Alert Title") },
                leadingIcon = { Icon(Icons.Default.Title, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = uiState.broadcastDescription,
                onValueChange = { viewModel.onBroadcastDescriptionChange(it) },
                label = { Text("Alert Description") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                shape = RoundedCornerShape(12.dp),
                maxLines = 5
            )

            OutlinedTextField(
                value = uiState.broadcastDistrict,
                onValueChange = { viewModel.onBroadcastDistrictChange(it) },
                label = { Text("Affected District") },
                leadingIcon = { Icon(Icons.Default.LocationCity, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Severity
            SectionHeader(title = "Alert Severity")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AlertSeverity.values().forEach { severity ->
                    FilterChip(
                        selected = uiState.broadcastSeverity == severity,
                        onClick = { viewModel.onBroadcastSeverityChange(severity) },
                        label = { Text(severity.name, color = if (uiState.broadcastSeverity == severity) severity.toColor() else MaterialTheme.colorScheme.onSurface) }
                    )
                }
            }

            Button(
                onClick = { viewModel.sendBroadcast() },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SeverityCritical),
                enabled = uiState.broadcastTitle.isNotBlank() && !uiState.isBroadcasting
            ) {
                if (uiState.isBroadcasting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Campaign, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Send Alert to All Users", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
