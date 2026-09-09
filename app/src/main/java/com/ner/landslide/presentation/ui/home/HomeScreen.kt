package com.ner.landslide.presentation.ui.home

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

    // SOS dialog
    var showSOSDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.sosState) {
        if (uiState.sosState == SOSState.SENT) {
            kotlinx.coroutines.delay(3000)
            viewModel.resetSOSState()
        }
    }

    if (showSOSDialog) {
        AlertDialog(
            onDismissRequest = { showSOSDialog = false },
            icon = { Icon(Icons.Default.Warning, null, tint = SeverityCritical) },
            title = { Text("Send SOS Alert?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "This will send your current GPS location to district emergency responders. " +
                            "Only use in a genuine emergency."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSOSDialog = false
                        viewModel.onSOSTrigger()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SeverityCritical)
                ) {
                    Text("Send SOS", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSOSDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "NER Landslide Watch",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "North Eastern Region",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                        )
                    }
                },
                actions = {
                    // Weather shortcut
                    IconButton(onClick = onNavigateToWeather) {
                        Icon(Icons.Default.Cloud, "Weather Forecast")
                    }
                    // AI Prediction shortcut
                    IconButton(onClick = onNavigateToPrediction) {
                        Icon(Icons.Default.Psychology, "AI Prediction")
                    }
                    // Admin panel (only visible to admins)
                    if (uiState.currentUser?.role == UserRole.ADMIN) {
                        IconButton(onClick = onNavigateToAdmin) {
                            Icon(Icons.Default.AdminPanelSettings, "Admin Dashboard")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            SOSFab(onClick = { showSOSDialog = true })
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Welcome Banner
            item {
                uiState.currentUser?.let { user ->
                    WelcomeBanner(user = user)
                }
            }

            // SOS State Feedback
            item {
                AnimatedVisibility(uiState.sosState == SOSState.SENT) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SeverityLow.copy(0.15f)),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, null, tint = SeverityLow)
                            Text(
                                "SOS alert sent to district emergency response team.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SeverityLow
                            )
                        }
                    }
                }
            }

            // Quick Action Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        label = "AI Prediction",
                        icon = Icons.Default.Psychology,
                        color = Primary80,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToPrediction
                    )
                    QuickActionCard(
                        label = "Weather",
                        icon = Icons.Default.Thunderstorm,
                        color = Secondary80,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToWeather
                    )
                }
            }

            // Section: Active Alerts
            item {
                Spacer(Modifier.height(4.dp))
                SectionHeader(
                    title = "Active Alerts",
                    subtitle = "${uiState.alerts.size} alert(s) in North Eastern Region"
                )
            }

            if (uiState.isLoading) {
                item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary80)
                } }
            } else if (uiState.alerts.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CheckCircleOutline, null,
                                tint = SeverityLow, modifier = Modifier.size(40.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("No active alerts", style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.6f))
                        }
                    }
                }
            } else {
                items(uiState.alerts, key = { it.id }) { alert ->
                    AlertCard(alert = alert)
                }
            }

            item { Spacer(Modifier.height(80.dp)) } // FAB clearance
        }
    }
}

@Composable
private fun WelcomeBanner(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Primary40.copy(alpha = 0.15f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(Icons.Default.AccountCircle, null,
                tint = Primary80, modifier = Modifier.size(44.dp))
            Column {
                Text(
                    text = "Hello, ${user.name.split(" ").firstOrNull() ?: "User"}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = user.role.name.replace("_", " "),
                    style = MaterialTheme.typography.labelSmall,
                    color = Primary80
                )
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(0.12f)),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(6.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = color,
                fontWeight = FontWeight.SemiBold)
        }
    }
}
