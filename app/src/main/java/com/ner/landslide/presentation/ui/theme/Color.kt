package com.ner.landslide.presentation.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ─── Control-Room Blue Master Palette (Bhurakshak Early Warning System) ──────
// Calm, legible, purpose-built for a safety-critical disaster monitoring tool.
// Base Surface Hierarchy (Deep blue-slate — dark, but not black)
val ObsidianBase = Color(0xFF0F1720)       // Deep blue-slate root canvas (#0F1720)
val BackgroundDark = Color(0xFF0F1720)     // Screen background
val SurfaceDark = Color(0xFF141E29)        // Card / surface panels (#141E29)
val SurfaceVariantDark = Color(0xFF1C2836) // Slightly elevated interactive chips (#1C2836)
val SurfaceElevated = Color(0xFF26313D)    // Modal / popover elevation
val BorderSubtle = Color(0xFF26313D)      // Low-contrast flat 1px hairline (#26313D)
val BorderHighlight = Color(0xFF2D9CDB)   // Active / focused hairline (#2D9CDB)

// Text & Content Hierarchy
val OnBackgroundDark = Color(0xFFEAF0F5)   // Off-white primary text (#EAF0F5)
val OnSurfaceDark = Color(0xFFEAF0F5)      // Primary card text
val TextMuted = Color(0xFF8A99A8)          // Muted blue-gray metadata & labels (#8A99A8)
val TextSubtle = Color(0xFF6B7A8A)         // Lower-priority technical specs & footnotes

// Accent & System Active State (Calm Blue — normal monitoring, GNSS locked, active telemetry)
val Primary80 = Color(0xFF2D9CDB)         // Calm Blue Accent (#2D9CDB)
val Primary40 = Color(0xFF1B6C9B)         // Deep Slate Blue
val PrimaryLight = Color(0xFF56B4E9)      // Active Indicator Blue
val OnPrimary = Color(0xFFFFFFFF)

// Secondary & Advisory Palette
val Secondary80 = Color(0xFFF2994A)       // Amber / Advisory (#F2994A)
val Secondary40 = Color(0xFFD97706)       // Dark Amber
val OnSecondary = Color(0xFF0F1720)

// Telemetry & Utility Aliases (Restrained)
val CyberCyan = Color(0xFF2D9CDB)         // Tactical GNSS Blue (#2D9CDB)
val BrandIndigo = Color(0xFF2D9CDB)       // Calm Blue Core
val BrandViolet = Color(0xFF8A99A8)       // Neutral Slate

// Strict Semantic Severity Tokens
// NOTE: Red (#EB5757) is reserved ONLY for Critical severity, SOS/Emergency actions, and Hazard markers!
val SeverityLow = Color(0xFF27AE60)        // Muted Green (Nominal / Safe / Completed) (#27AE60)
val SeverityModerate = Color(0xFFF2994A)   // Amber (Moderate / Advisory) (#F2994A)
val SeverityHigh = Color(0xFFF2994A)       // Amber (Warning) (#F2994A)
val SeverityCritical = Color(0xFFEB5757)   // Saturated Muted Red (Critical Emergency) (#EB5757)

// Error & States
val ErrorRed = Color(0xFFEB5757)
val OnError = Color(0xFFFFFFFF)

// Light mode fallback (Standard high-legibility slate)
val BackgroundLight = Color(0xFFF8FAFC)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFF1F5F9)
val OnBackgroundLight = Color(0xFF0F1720)
val OnSurfaceLight = Color(0xFF141E29)

// ─── Flat Field Gradients (Zero Neon Glow, Pure Slate Depth) ─────────────────
val GlassGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF141E29),
        Color(0xFF0F1720)
    )
)

val CardGlowGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF141E29),
        Color(0xFF141E29)
    )
)

val HeroRadarGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF141E29),
        Color(0xFF0F1720)
    )
)

// Single-purpose Emergency SOS Gradient (Used ONLY for SOS dispatch actions)
val CrimsonEmergencyGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFFEB5757),
        Color(0xFFC53030)
    )
)

val CyberAiGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF141E29),
        Color(0xFF1C2836)
    )
)
