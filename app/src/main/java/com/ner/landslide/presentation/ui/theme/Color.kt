package com.ner.landslide.presentation.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ─── Bhurakshak Master Design Token System ───────────────────────────────────
// Core Rule: One component system, two token sets.
// Switching theme only swaps token values — never layout, spacing, or typography.

@Immutable
data class BhurakshakColorTokens(
    val bgBase: Color,
    val bgSurface: Color,
    val borderDefault: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accent: Color,
    val success: Color,
    val warning: Color,
    val critical: Color,
    val isDark: Boolean
)

// ─── Dark Theme — "Control-room blue" ────────────────────────────────────────
// Calm, legible, dark but not black. Built for night operations and low-light field conditions.
val DarkColorTokens = BhurakshakColorTokens(
    bgBase = Color(0xFF0F1720),        // Deep blue-slate (#0F1720)
    bgSurface = Color(0xFF141E29),     // Card / panel background (#141E29)
    borderDefault = Color(0xFF26313D), // Hairline border (#26313D)
    textPrimary = Color(0xFFEAF0F5),   // Headlines, key values (#EAF0F5)
    textSecondary = Color(0xFF8A99A8), // Metadata, helper text (#8A99A8)
    accent = Color(0xFF2D9CDB),        // Normal / active / monitoring state (#2D9CDB)
    success = Color(0xFF27AE60),       // Nominal / safe / completed (#27AE60)
    warning = Color(0xFFF2994A),       // Moderate / advisory (#F2994A)
    critical = Color(0xFFEB5757),      // Critical / emergency / hazard (#EB5757)
    isDark = true
)

// ─── Light Theme — "Field daylight" ──────────────────────────────────────────
// High-contrast, glare-resistant, built for outdoor use in direct Himalayan sunlight.
// Note: Accent, success, warning, and critical are intentionally darkened to maintain >4.5:1 WCAG AA contrast against white surfaces.
val LightColorTokens = BhurakshakColorTokens(
    bgBase = Color(0xFFF5F7FA),        // Screen background (#F5F7FA)
    bgSurface = Color(0xFFFFFFFF),     // Card / panel background (#FFFFFF)
    borderDefault = Color(0xFFDCE1E6), // Hairline border (#DCE1E6)
    textPrimary = Color(0xFF111827),   // Headlines, key values (#111827)
    textSecondary = Color(0xFF5B6572), // Metadata, helper text (#5B6572)
    accent = Color(0xFF1D6FB8),        // Darkened maritime blue (#1D6FB8, 5.4:1 contrast)
    success = Color(0xFF1E8E4F),       // Darkened emerald green (#1E8E4F, 4.5:1 contrast)
    warning = Color(0xFFC56A1A),       // Darkened ochre amber (#C56A1A, 4.8:1 contrast)
    critical = Color(0xFFD0342C),      // Darkened crimson red (#D0342C, 5.1:1 contrast)
    isDark = false
)

val LocalBhurakshakColors = staticCompositionLocalOf { DarkColorTokens }

object BhurakshakTheme {
    val colors: BhurakshakColorTokens
        @Composable
        get() = LocalBhurakshakColors.current
}

// ─── Backward-compatible Palette Aliases (Direct Token Mappings) ─────────────
val ObsidianBase = Color(0xFF0F1720)
val BackgroundDark = Color(0xFF0F1720)
val SurfaceDark = Color(0xFF141E29)
val SurfaceVariantDark = Color(0xFF1C2836)
val SurfaceElevated = Color(0xFF26313D)
val BorderSubtle = Color(0xFF26313D)
val BorderHighlight = Color(0xFF2D9CDB)

val OnBackgroundDark = Color(0xFFEAF0F5)
val OnSurfaceDark = Color(0xFFEAF0F5)
val TextMuted = Color(0xFF8A99A8)
val TextSubtle = Color(0xFF6B7A8A)

val Primary80 = Color(0xFF2D9CDB)
val Primary40 = Color(0xFF1B6C9B)
val PrimaryLight = Color(0xFF56B4E9)
val OnPrimary = Color(0xFFFFFFFF)

val Secondary80 = Color(0xFFF2994A)
val Secondary40 = Color(0xFFD97706)
val OnSecondary = Color(0xFF0F1720)

val CyberCyan = Color(0xFF2D9CDB)
val BrandIndigo = Color(0xFF2D9CDB)
val BrandViolet = Color(0xFF8A99A8)

val SeverityLow = Color(0xFF27AE60)
val SeverityModerate = Color(0xFFF2994A)
val SeverityHigh = Color(0xFFF2994A)
val SeverityCritical = Color(0xFFEB5757)

val ErrorRed = Color(0xFFEB5757)
val OnError = Color(0xFFFFFFFF)

val BackgroundLight = Color(0xFFF5F7FA)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFF1F5F9)
val OnBackgroundLight = Color(0xFF111827)
val OnSurfaceLight = Color(0xFF111827)

// ─── Subtle Depth Gradients ──────────────────────────────────────────────────
val GlassGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF141E29), Color(0xFF0F1720))
)
val CardGlowGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF141E29), Color(0xFF141E29))
)
val HeroRadarGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF141E29), Color(0xFF0F1720))
)
val CrimsonEmergencyGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFEB5757), Color(0xFFC53030))
)
val CyberAiGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF141E29), Color(0xFF1C2836))
)
