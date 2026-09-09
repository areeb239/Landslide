package com.ner.landslide.presentation.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.ner.landslide.presentation.ui.theme.*
import com.ner.landslide.presentation.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Sign Out?") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.signOut()
                    showLogoutDialog = false
                    onLogout()
                }) { Text("Sign Out") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(24.dp))

        // Avatar
        Surface(
            shape = CircleShape,
            color = Primary40.copy(0.2f),
            modifier = Modifier.size(90.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.AccountCircle, null,
                    tint = Primary80, modifier = Modifier.size(72.dp))
            }
        }

        uiState.user?.let { user ->
            Text(user.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text(user.email, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(0.6f))

            // Role badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Primary80.copy(0.15f),
                modifier = Modifier
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        when (user.role.name) {
                            "ADMIN" -> Icons.Default.AdminPanelSettings
                            "FIELD_OFFICER" -> Icons.Default.Badge
                            else -> Icons.Default.Person
                        },
                        null,
                        tint = Primary80,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        user.role.name.replace("_", " "),
                        color = Primary80,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(0.1f))

            // Admin shortcut
            if (user.role.name == "ADMIN") {
                ProfileMenuItem(
                    icon = Icons.Default.AdminPanelSettings,
                    label = "Admin Dashboard",
                    tint = Primary80,
                    onClick = onNavigateToAdmin
                )
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(0.06f))
            }

            ProfileMenuItem(
                icon = Icons.Default.Info,
                label = "App Version 1.0 — SIH 2026",
                tint = MaterialTheme.colorScheme.onSurface.copy(0.4f),
                onClick = {}
            )
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(0.06f))

            ProfileMenuItem(
                icon = Icons.Default.Logout,
                label = "Sign Out",
                tint = MaterialTheme.colorScheme.error,
                onClick = { showLogoutDialog = true }
            )
        }

        if (uiState.isLoading) {
            CircularProgressIndicator(color = Primary80)
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(22.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium,
                color = if (label == "Sign Out") tint else MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.weight(1f))
            if (label != "Sign Out" && label.contains("Version").not()) {
                Icon(Icons.Default.ChevronRight, null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(0.3f))
            }
        }
    }
}
