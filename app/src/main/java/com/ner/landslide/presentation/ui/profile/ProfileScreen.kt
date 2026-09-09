package com.ner.landslide.presentation.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ner.landslide.presentation.ui.components.GlassCard
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
            containerColor = SurfaceDark,
            title = { Text("Sign Out of BhuRakshak?", fontWeight = FontWeight.Bold, color = OnBackgroundDark) },
            text = { Text("You will need to sign in again or use Guest Access to receive localized hazard telemetry.", color = TextMuted) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.signOut()
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SeverityCritical)
                ) { Text("Sign Out", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel", color = TextMuted) }
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
        Spacer(Modifier.height(20.dp))

        // Profile Avatar with Neon Glow Ring
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(BrandIndigo, CyberCyan))
                    )
                    .border(2.dp, Primary80, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = uiState.user?.name?.take(1)?.uppercase() ?: "U",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }

        uiState.user?.let { user ->
            Text(user.name, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = OnBackgroundDark)
            Text(user.email, style = MaterialTheme.typography.bodyMedium, color = TextMuted)

            // Role Badge
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Primary80.copy(0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Primary80.copy(0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
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
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Menu Cards
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceDark.copy(alpha = 0.9f)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    if (user.role.name == "ADMIN") {
                        ProfileMenuItem(
                            icon = Icons.Default.AdminPanelSettings,
                            label = "Disaster Admin Command Hub",
                            tint = BrandIndigo,
                            onClick = onNavigateToAdmin
                        )
                        Divider(color = Color.White.copy(0.06f))
                    }

                    ProfileMenuItem(
                        icon = Icons.Default.Security,
                        label = "Regional Clearance: Eastern Himalayas",
                        tint = CyberCyan,
                        onClick = {}
                    )

                    Divider(color = Color.White.copy(0.06f))

                    ProfileMenuItem(
                        icon = Icons.Default.Info,
                        label = "BhuRakshak v2.4 — Production Edition",
                        tint = TextMuted,
                        onClick = {}
                    )

                    Divider(color = Color.White.copy(0.06f))

                    ProfileMenuItem(
                        icon = Icons.Default.Logout,
                        label = "Sign Out",
                        tint = SeverityCritical,
                        onClick = { showLogoutDialog = true }
                    )
                }
            }
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
    tint: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (label == "Sign Out") tint else OnBackgroundDark
            )
            Spacer(Modifier.weight(1f))
            if (label != "Sign Out" && label.contains("v2.4").not() && label.contains("Clearance").not()) {
                Icon(Icons.Default.ChevronRight, null, tint = TextSubtle)
            }
        }
    }
}

