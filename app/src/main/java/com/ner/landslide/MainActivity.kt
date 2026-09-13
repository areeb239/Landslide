package com.ner.landslide

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.ner.landslide.navigation.AppNavGraph
import com.ner.landslide.presentation.ui.theme.LocaleController
import com.ner.landslide.presentation.ui.theme.NERLandslideTheme
import com.ner.landslide.presentation.ui.theme.ProvideAppLocale
import com.ner.landslide.service.BackgroundRiskWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val navRouteFlow = MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        intent?.getStringExtra(BackgroundRiskWorker.EXTRA_NAVIGATE_TO)?.let {
            navRouteFlow.value = it
        }

        setContent {
            CompositionLocalProvider(
                LocalActivityResultRegistryOwner provides this@MainActivity
            ) {
                val prefs = remember { getSharedPreferences("bhurakshak_locale_prefs", Context.MODE_PRIVATE) }
                val localeController = remember { LocaleController(this@MainActivity, prefs) }

                ProvideAppLocale(localeController) {
                    NERLandslideTheme {
                        val pendingRoute by navRouteFlow.collectAsState()
                        var showBackgroundRationale by remember { mutableStateOf(false) }

                        // Step 2 Launcher: Background Location (API 29+)
                        val backgroundLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.RequestPermission()
                        ) { isGranted ->
                            if (isGranted) {
                                BackgroundRiskWorker.triggerImmediateRiskCheck(this@MainActivity)
                            }
                        }

                        // Step 1 Launcher: Foreground Location, Notifications, and SMS
                        val foregroundLauncher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.RequestMultiplePermissions()
                        ) { result ->
                            val fineGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                                    result[Manifest.permission.ACCESS_COARSE_LOCATION] == true

                            if (fineGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                val bgGranted = ContextCompat.checkSelfPermission(
                                    this@MainActivity,
                                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED

                                if (!bgGranted) {
                                    showBackgroundRationale = true
                                }
                            }
                        }

                        LaunchedEffect(Unit) {
                            val hasActiveSession = com.ner.landslide.data.repository.UserRepositoryImpl.isSessionActive(this@MainActivity)
                            if (hasActiveSession) {
                                val fineGranted = ContextCompat.checkSelfPermission(
                                    this@MainActivity,
                                    Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED
                                if (fineGranted) {
                                    BackgroundRiskWorker.triggerImmediateRiskCheck(this@MainActivity)
                                }
                            }
                        }

                        // Step 2 Rationale Dialog: Clear explanation before requesting background location
                        if (showBackgroundRationale) {
                            AlertDialog(
                                onDismissRequest = { showBackgroundRationale = false },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                },
                                title = {
                                    Text(
                                        text = "24/7 Background Landslide Safety",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                },
                                text = {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(
                                            text = "Step 2 of 2: Background Protection",
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "To warn you of sudden cloudbursts, mudslides, and rockfalls even when the app is closed or your screen is off, Bhoochetak monitors regional hazard telemetry in the background.\n\nIn the next system screen, please select 'Allow all the time'.",
                                            fontSize = 13.5.sp,
                                            lineHeight = 18.sp
                                        )
                                    }
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            showBackgroundRationale = false
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                                backgroundLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                                            }
                                        }
                                    ) {
                                        Text("Continue to Allow All the Time")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showBackgroundRationale = false }) {
                                        Text("Maybe Later")
                                    }
                                }
                            )
                        }

                        Surface(modifier = Modifier.fillMaxSize()) {
                            AppNavGraph(initialNavigateRoute = pendingRoute)
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.getStringExtra(BackgroundRiskWorker.EXTRA_NAVIGATE_TO)?.let {
            navRouteFlow.value = it
        }
    }
}

