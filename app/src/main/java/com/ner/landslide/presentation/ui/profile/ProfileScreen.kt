package com.ner.landslide.presentation.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ner.landslide.presentation.ui.components.FieldCard
import com.ner.landslide.presentation.ui.theme.*
import com.ner.landslide.presentation.viewmodel.ProfileViewModel

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
            containerColor = SurfaceDark,
            icon = {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SeverityCritical.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, null, tint = SeverityCritical, modifier = Modifier.size(22.dp))
                }
            },
            title = {
                Text(
                    "Sign Out of Incident Network?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = OnBackgroundDark
                )
            },
            text = {
                Text(
                    "You will stop receiving prioritized SDRF push alerts and direct GNSS SOS dispatch relays until you sign back in.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.signOut()
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SeverityCritical),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Sign Out", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = TextMuted, fontSize = 12.sp)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // Authoritative Profile Avatar (Zero Neon Glow)
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(SurfaceVariantDark)
                    .border(1.5.dp, BorderSubtle, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.user?.name?.take(1)?.uppercase() ?: "U",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = OnBackgroundDark
                )
            }
        }

        uiState.user?.let { user ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = user.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = OnBackgroundDark
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            }

            // Role Badge
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = SurfaceVariantDark,
                border = BorderStroke(1.dp, BorderSubtle)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
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
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = user.role.name.replace("_", " "),
                        color = OnSurfaceDark,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Operational Details Card
            FieldCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(4.dp)) {
                    if (user.role.name == "ADMIN") {
                        ProfileMenuItem(
                            icon = Icons.Default.AdminPanelSettings,
                            label = "Disaster Admin Command Hub",
                            tint = Primary80,
                            onClick = onNavigateToAdmin
                        )
                        HorizontalDivider(color = BorderSubtle, thickness = 0.8.dp)
                    }

                    ProfileMenuItem(
                        icon = Icons.Default.Security,
                        label = "Operational Sector: Eastern Himalayas",
                        tint = TextMuted,
                        onClick = {}
                    )

                    HorizontalDivider(color = BorderSubtle, thickness = 0.8.dp)

                    ProfileMenuItem(
                        icon = Icons.Default.Info,
                        label = "BhuRakshak v2.4 • Field Edition",
                        tint = TextSubtle,
                        onClick = {}
                    )

                    HorizontalDivider(color = BorderSubtle, thickness = 0.8.dp)

                    ProfileMenuItem(
                        icon = Icons.AutoMirrored.Filled.Logout,
                        label = "Sign Out",
                        tint = SeverityCritical,
                        onClick = { showLogoutDialog = true }
                    )
                }
            }
        }

        if (uiState.isLoading) {
            CircularProgressIndicator(color = Primary80, strokeWidth = 2.5.dp, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (label == "Sign Out") FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp,
                color = if (label == "Sign Out") tint else OnBackgroundDark
            )
            Spacer(Modifier.weight(1f))
            if (label != "Sign Out" && !label.contains("v2.4") && !label.contains("Operational")) {
                Icon(Icons.Default.ChevronRight, null, tint = TextSubtle, modifier = Modifier.size(16.dp))
            }
        }
    }
}
