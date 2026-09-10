package com.ner.landslide

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.ner.landslide.navigation.AppNavGraph
import com.ner.landslide.presentation.ui.theme.NERLandslideTheme
import dagger.hilt.android.AndroidEntryPoint

import android.content.Context
import androidx.compose.runtime.remember
import com.ner.landslide.presentation.ui.theme.LocaleController
import com.ner.landslide.presentation.ui.theme.ProvideAppLocale

import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.compose.runtime.CompositionLocalProvider

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CompositionLocalProvider(
                LocalActivityResultRegistryOwner provides this@MainActivity
            ) {
                val prefs = remember { getSharedPreferences("bhurakshak_locale_prefs", Context.MODE_PRIVATE) }
                val localeController = remember { LocaleController(this@MainActivity, prefs) }

                ProvideAppLocale(localeController) {
                    NERLandslideTheme {

                    // Automatically request critical runtime permissions (Location & Notifications) on launch
                    val permissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestMultiplePermissions()
                    ) { /* Permissions granted/denied handled seamlessly by features */ }

                    LaunchedEffect(Unit) {
                        val permissions = buildList {
                            add(Manifest.permission.ACCESS_FINE_LOCATION)
                            add(Manifest.permission.ACCESS_COARSE_LOCATION)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                add(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                        permissionLauncher.launch(permissions.toTypedArray())
                    }

                    Surface(modifier = Modifier.fillMaxSize()) {
                        AppNavGraph()
                    }
                }
            }
        }
    }
}
}

