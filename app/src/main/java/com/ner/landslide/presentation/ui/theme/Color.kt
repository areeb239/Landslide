package com.ner.landslide.presentation.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ─── Luxury Midnight Obsidian Theme Palette ─────────────────────────────────
// Primary brand — Electric Cyber Mint & Emerald
val Primary80 = Color(0xFF10B981)         // Neon Emerald
val Primary40 = Color(0xFF059669)         // Deep Emerald
val PrimaryLight = Color(0xFF34D399)      // Bright Mint
val OnPrimary = Color(0xFFFFFFFF)

// Secondary — Solar Amber / Hazard Gold
val Secondary80 = Color(0xFFFBBF24)       // Vibrant Amber
val Secondary40 = Color(0xFFD97706)       // Dark Amber
val OnSecondary = Color(0xFF0B0F19)

// Cyber Accents
val CyberCyan = Color(0xFF06B6D4)         // Telemetry Cyan
val BrandIndigo = Color(0xFF6366F1)       // Deep AI Indigo
val BrandViolet = Color(0xFF8B5CF6)       // Electric Violet

// Midnight Obsidian Surfaces (Dark-First Mission Critical System)
val ObsidianBase = Color(0xFF070B14)       // Deepest space background
val BackgroundDark = Color(0xFF0A0F1D)     // Primary screen backdrop
val SurfaceDark = Color(0xFF111827)        // Glass card surface
val SurfaceVariantDark = Color(0xFF1A2338) // Elevated card surface
val SurfaceElevated = Color(0xFF222F4C)    // High-elevation interactive surface
val BorderSubtle = Color(0xFF2A3A5E)      // Crisp 1dp frosted border
val BorderHighlight = Color(0xFF3B82F6)   // Active focus border

val OnBackgroundDark = Color(0xFFF1F5F9)   // Crisp near-white
val OnSurfaceDark = Color(0xFFE2E8F0)      // Slate 200 text
val TextMuted = Color(0xFF94A3B8)          // Slate 400 secondary text
val TextSubtle = Color(0xFF64748B)         // Slate 500 metadata text

// Light mode fallback
val BackgroundLight = Color(0xFFF8FAFC)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFF1F5F9)
val OnBackgroundLight = Color(0xFF0F172A)
val OnSurfaceLight = Color(0xFF1E293B)

// Hazard Alert Severity Colours (High Luminance Neon)
val SeverityLow = Color(0xFF10B981)        // Emerald Green (Nominal)
val SeverityModerate = Color(0xFFF59E0B)   // Golden Amber (Advisory)
val SeverityHigh = Color(0xFFF97316)       // Radiant Orange (Warning)
val SeverityCritical = Color(0xFFEF4444)   // Electric Crimson (Evacuate / Emergency)

// Error & States
val ErrorRed = Color(0xFFF87171)
val OnError = Color(0xFFFFFFFF)

// ─── Mission-Critical Gradients ──────────────────────────────────────────────
val GlassGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF1E293B).copy(alpha = 0.85f),
        Color(0xFF0F172A).copy(alpha = 0.95f)
    )
)

val CardGlowGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF1E293B),
        Color(0xFF111827)
    )
)

val HeroRadarGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF0F172A),
        Color(0xFF0A0F1D)
    )
)

val CrimsonEmergencyGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFFDC2626),
        Color(0xFF991B1B)
    )
)

val CyberAiGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF6366F1),
        Color(0xFF06B6D4)
    )
)

