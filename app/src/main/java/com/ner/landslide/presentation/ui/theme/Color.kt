package com.ner.landslide.presentation.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ─── Authoritative Mission-Critical Charcoal Slate Palette ───────────────────
// Base Surface Hierarchy (Deep Charcoal Slate — avoids pitch-black eye strain)
val ObsidianBase = Color(0xFF0F141C)       // Deep slate root canvas
val BackgroundDark = Color(0xFF0F141C)     // Primary screen backdrop
val SurfaceDark = Color(0xFF161D27)        // Card surface
val SurfaceVariantDark = Color(0xFF1E2634) // Elevated interactive surface
val SurfaceElevated = Color(0xFF263242)    // Popover / dialog elevation
val BorderSubtle = Color(0xFF263242)      // Low-contrast 1dp hairline
val BorderHighlight = Color(0xFF38495F)   // Focused active hairline

// Text & Content Hierarchy
val OnBackgroundDark = Color(0xFFF1F5F9)   // Crisp high-contrast off-white
val OnSurfaceDark = Color(0xFFE2E8F0)      // Slate 200 high-readability text
val TextMuted = Color(0xFF94A3B8)          // Slate 400 secondary metadata
val TextSubtle = Color(0xFF64748B)         // Slate 500 technical tags & timestamps

// Primary Monitoring & Telemetry Brand (Muted Field Teal — non-neon)
val Primary80 = Color(0xFF0D9488)         // Operational Monitoring Teal
val Primary40 = Color(0xFF0F766E)         // Deep Slate Teal
val PrimaryLight = Color(0xFF14B8A6)      // Active Beacon Teal
val OnPrimary = Color(0xFFFFFFFF)

// Secondary & Advisory Palette
val Secondary80 = Color(0xFFF59E0B)       // Advisory Amber
val Secondary40 = Color(0xFFD97706)       // Dark Amber
val OnSecondary = Color(0xFF0F141C)

// Telemetry & Utility Colors (Restrained)
val CyberCyan = Color(0xFF0284C7)         // Tactical GNSS Blue
val BrandIndigo = Color(0xFF475569)       // Muted Slate Core
val BrandViolet = Color(0xFF64748B)       // Neutral Slate

// Strict Semantic Severity Tokens
// NOTE: Saturated Red is strictly reserved for Critical/Emergency states only!
val SeverityLow = Color(0xFF0D9488)        // Monitoring Teal (Nominal)
val SeverityModerate = Color(0xFFF59E0B)   // Amber / Gold (Advisory)
val SeverityHigh = Color(0xFFF97316)       // Warm Amber-Orange (Warning)
val SeverityCritical = Color(0xFFEF4444)   // Saturated Crimson (Critical Emergency)

// Error & States
val ErrorRed = Color(0xFFEF4444)
val OnError = Color(0xFFFFFFFF)

// Light mode fallback (Standard high-legibility slate)
val BackgroundLight = Color(0xFFF8FAFC)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFF1F5F9)
val OnBackgroundLight = Color(0xFF0F172A)
val OnSurfaceLight = Color(0xFF1E293B)

// ─── Restrained Field Gradients (Zero Neon Glow) ─────────────────────────────
val GlassGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF1E2634),
        Color(0xFF161D27)
    )
)

val CardGlowGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF1E2634),
        Color(0xFF161D27)
    )
)

val HeroRadarGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF161D27),
        Color(0xFF0F141C)
    )
)

// Single-purpose Emergency SOS Gradient (Used ONLY for SOS dispatch actions)
val CrimsonEmergencyGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFFDC2626),
        Color(0xFF991B1B)
    )
)

val CyberAiGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF1E293B),
        Color(0xFF0F766E)
    )
)
