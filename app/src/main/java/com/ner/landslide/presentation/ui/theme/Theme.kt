package com.ner.landslide.presentation.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

class ThemeController(
    val isDarkState: MutableState<Boolean>
) {
    val isDark: Boolean get() = isDarkState.value
    fun toggleTheme() {
        isDarkState.value = !isDarkState.value
    }
    fun setDark(dark: Boolean) {
        isDarkState.value = dark
    }
}

val LocalThemeController = staticCompositionLocalOf<ThemeController> {
    error("No ThemeController provided")
}

@Composable
fun NERLandslideTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val isDarkState = remember { mutableStateOf(darkTheme) }
    val themeController = remember { ThemeController(isDarkState) }
    val isDark = isDarkState.value
    val tokens = if (isDark) DarkColorTokens else LightColorTokens

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = tokens.accent,
            onPrimary = Color.White,
            primaryContainer = tokens.bgSurface,
            onPrimaryContainer = tokens.accent,
            secondary = tokens.warning,
            onSecondary = tokens.bgBase,
            secondaryContainer = tokens.bgSurface,
            onSecondaryContainer = tokens.warning,
            background = tokens.bgBase,
            surface = tokens.bgSurface,
            surfaceVariant = tokens.bgSurface,
            onBackground = tokens.textPrimary,
            onSurface = tokens.textPrimary,
            onSurfaceVariant = tokens.textSecondary,
            outline = tokens.borderDefault,
            error = tokens.critical,
            onError = Color.White
        )
    } else {
        lightColorScheme(
            primary = tokens.accent,
            onPrimary = Color.White,
            primaryContainer = tokens.bgSurface,
            onPrimaryContainer = tokens.accent,
            secondary = tokens.warning,
            onSecondary = Color.White,
            secondaryContainer = tokens.bgSurface,
            onSecondaryContainer = tokens.warning,
            background = tokens.bgBase,
            surface = tokens.bgSurface,
            surfaceVariant = tokens.bgBase,
            onBackground = tokens.textPrimary,
            onSurface = tokens.textPrimary,
            onSurfaceVariant = tokens.textSecondary,
            outline = tokens.borderDefault,
            error = tokens.critical,
            onError = Color.White
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = tokens.bgBase.toArgb()
            window.navigationBarColor = tokens.bgBase.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isDark
        }
    }

    CompositionLocalProvider(
        LocalBhurakshakColors provides tokens,
        LocalThemeController provides themeController
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
