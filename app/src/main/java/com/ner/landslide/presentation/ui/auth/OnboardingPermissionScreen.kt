package com.ner.landslide.presentation.ui.auth

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.ner.landslide.R
import com.ner.landslide.presentation.ui.components.GlassCard
import com.ner.landslide.presentation.ui.theme.*
import com.ner.landslide.service.BackgroundRiskWorker

@Composable
fun OnboardingPermissionScreen(
    onContinueToDashboard: () -> Unit
) {
    val context = LocalContext.current
    var isChecking by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        isChecking = false
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (fineGranted) {
            try {
                BackgroundRiskWorker.triggerImmediateRiskCheck(context)
            } catch (_: Exception) {}
        }
        // Proceed to dashboard regardless of grant/deny (graceful degradation)
        onContinueToDashboard()
    }

    LaunchedEffect(Unit) {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (fineGranted) {
            try {
                BackgroundRiskWorker.triggerImmediateRiskCheck(context)
            } catch (_: Exception) {}
            onContinueToDashboard()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBase)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        listOf(BrandIndigo.copy(alpha = 0.2f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(16.dp))

            // Logo
            Box(contentAlignment = Alignment.Center) {
                Image(
                    painter = painterResource(id = R.drawable.bhoochetak_logo),
                    contentDescription = "Bhoochetak",
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.5.dp, Primary80.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "ACTIVATE HAZARD MONITORING",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp,
                color = OnBackgroundDark,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "To protect you from flash floods, slope failures, and rockfalls, Bhoochetak needs your regional location.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(28.dp))

            // Information Card
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = SurfaceDark.copy(alpha = 0.95f),
                borderColor = Color.White.copy(alpha = 0.1f)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    PermissionFeatureRow(
                        icon = Icons.Default.GpsFixed,
                        title = "Precise Slope Corridor Telemetry",
                        description = "Matches your GNSS coordinates against 30m Copernicus DEM elevation, slope, and lithology data."
                    )

                    HorizontalDivider(color = BorderSubtle)

                    PermissionFeatureRow(
                        icon = Icons.Default.CloudQueue,
                        title = "Precipitation & Saturation Risk",
                        description = "Pulls IMD radar and satellite rainfall accumulation (1-day, 3-day, 7-day) for your coordinates."
                    )

                    HorizontalDivider(color = BorderSubtle)

                    PermissionFeatureRow(
                        icon = Icons.Default.Emergency,
                        title = "Emergency SDRF SOS Dispatch",
                        description = "Enables one-tap SMS/GNSS emergency dispatch to state rescue teams during critical disasters."
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // Grant Button
            Button(
                onClick = {
                    isChecking = true
                    val perms = buildList {
                        add(Manifest.permission.ACCESS_FINE_LOCATION)
                        add(Manifest.permission.ACCESS_COARSE_LOCATION)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            add(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                    permissionLauncher.launch(perms.toTypedArray())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary80),
                enabled = !isChecking
            ) {
                if (isChecking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = ObsidianBase,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(Modifier.width(10.dp))
                    Text("INITIALIZING SENSORS...", fontWeight = FontWeight.Bold, color = ObsidianBase)
                } else {
                    Icon(Icons.Default.Security, contentDescription = null, tint = ObsidianBase)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "GRANT LOCATION & ACTIVATE",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp,
                        color = ObsidianBase
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // Skip / Default Sector Button
            TextButton(
                onClick = onContinueToDashboard,
                enabled = !isChecking
            ) {
                Text(
                    text = "Continue with Default Sector (Gangtok, Sikkim)",
                    color = TextMuted,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PermissionFeatureRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = CircleShape,
            color = Primary80.copy(alpha = 0.15f),
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = Primary80, modifier = Modifier.size(20.dp))
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.5.sp,
                color = OnBackgroundDark
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}
