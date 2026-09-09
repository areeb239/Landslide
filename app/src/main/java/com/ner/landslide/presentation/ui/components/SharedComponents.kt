package com.ner.landslide.presentation.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ner.landslide.domain.model.*
import com.ner.landslide.presentation.ui.theme.*
import com.ner.landslide.util.toRelativeTimeString

// ─── Semantic Severity Color & Label Mappings ────────────────────────────────

fun AlertSeverity.toColor(): Color = when (this) {
    AlertSeverity.LOW -> SeverityLow
    AlertSeverity.MODERATE -> SeverityModerate
    AlertSeverity.HIGH -> SeverityHigh
    AlertSeverity.CRITICAL -> SeverityCritical
}

fun AlertSeverity.toLabel(): String = when (this) {
    AlertSeverity.LOW -> "Low Risk"
    AlertSeverity.MODERATE -> "Moderate"
    AlertSeverity.HIGH -> "High Risk"
    AlertSeverity.CRITICAL -> "Critical Hazard"
}

fun AlertSeverity.toActionGuideline(): String = when (this) {
    AlertSeverity.LOW -> "Normal vigilance along hill slopes"
    AlertSeverity.MODERATE -> "Avoid steep drainage paths; monitor rain"
    AlertSeverity.HIGH -> "Caution on highways; prepare grab-bags"
    AlertSeverity.CRITICAL -> "Immediate evacuation recommended"
}

fun String.toAlertSeverityColor(): Color = when (this.uppercase()) {
    "LOW" -> SeverityLow
    "MODERATE" -> SeverityModerate
    "HIGH" -> SeverityHigh
    "CRITICAL" -> SeverityCritical
    else -> SeverityLow
}

// ─── Restrained Field Status Beacon ──────────────────────────────────────────

@Composable
fun PulsingStatusDot(
    color: Color = Primary80,
    size: Dp = 8.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_beacon")
    val waveScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_scale"
    )
    val waveAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_alpha"
    )

    Box(contentAlignment = Alignment.Center, modifier = modifier.size(size * 2f)) {
        Box(
            modifier = Modifier
                .size(size)
                .scale(waveScale)
                .clip(CircleShape)
                .background(color.copy(alpha = waveAlpha))
        )
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(color)
        )
    }
}

// ─── Purpose-Built Field Card Container (Zero Neon Glow) ────────────────────

@Composable
fun FieldCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    backgroundColor: Color = SurfaceDark,
    borderColor: Color = BorderSubtle,
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val baseModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else modifier

    Surface(
        modifier = baseModifier,
        shape = shape,
        color = backgroundColor,
        border = BorderStroke(borderWidth, borderColor),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(content = content)
    }
}

// Backward-compatible alias for existing references
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    backgroundColor: Color = SurfaceDark,
    borderColor: Color = BorderSubtle,
    borderWidth: Dp = 1.dp,
    glowAccent: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    FieldCard(
        modifier = modifier,
        shape = shape,
        backgroundColor = backgroundColor,
        borderColor = if (glowAccent != null) glowAccent.copy(alpha = 0.4f) else borderColor,
        borderWidth = borderWidth,
        onClick = onClick,
        content = content
    )
}

// ─── Severity-Weighted Left Accent Card ──────────────────────────────────────

@Composable
fun SeverityAccentCard(
    severity: AlertSeverity,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val accentColor = severity.toColor()
    val isCritical = severity == AlertSeverity.CRITICAL

    val barWidth = when (severity) {
        AlertSeverity.CRITICAL -> 5.dp
        AlertSeverity.HIGH -> 4.dp
        AlertSeverity.MODERATE -> 3.5.dp
        AlertSeverity.LOW -> 3.dp
    }

    val baseModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else modifier

    Surface(
        modifier = baseModifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = SurfaceDark,
        border = BorderStroke(
            1.dp,
            if (isCritical) SeverityCritical.copy(alpha = 0.6f) else BorderSubtle
        )
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Graduated Solid Left Semantic Accent Bar
            Box(
                modifier = Modifier
                    .width(barWidth)
                    .fillMaxHeight()
                    .defaultMinSize(minHeight = 88.dp)
                    .background(accentColor)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                content = content
            )
        }
    }
}

// ─── Severity Badges ─────────────────────────────────────────────────────────

@Composable
fun HazardBadge(
    severity: AlertSeverity,
    modifier: Modifier = Modifier
) {
    val color = severity.toColor()
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = severity.toLabel().uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
                color = color
            )
        }
    }
}

@Composable
fun SeverityBadge(severity: AlertSeverity, modifier: Modifier = Modifier) {
    HazardBadge(severity = severity, modifier = modifier)
}

// ─── Live Telemetry Instrumentation Tile (Mini Gauge Indicator) ─────────────

@Composable
fun TelemetryInstrumentTile(
    title: String,
    value: String,
    unit: String,
    progressFraction: Float, // 0.0f to 1.0f relative to danger threshold
    indicatorColor: Color = Primary80,
    modifier: Modifier = Modifier
) {
    FieldCard(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        backgroundColor = SurfaceDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Label
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = TextSubtle,
                letterSpacing = 0.4.sp,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )

            // Numeric Value Readout
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.padding(vertical = 1.dp)
            ) {
                Text(
                    text = value,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = OnBackgroundDark,
                    maxLines = 1,
                    softWrap = false
                )
                if (unit.isNotBlank()) {
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = unit,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMuted,
                        modifier = Modifier.padding(bottom = 1.dp),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            // Precision Inline Micro-Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(BorderSubtle)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressFraction.coerceIn(0.05f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(indicatorColor)
                )
            }
        }
    }
}

// Backward-compatible alias for existing references
@Composable
fun TelemetryMetricItem(
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    iconColor: Color = Primary80,
    modifier: Modifier = Modifier
) {
    val fraction = when {
        title.contains("RAIN", ignoreCase = true) -> 0.48f
        title.contains("SOIL", ignoreCase = true) -> 0.64f
        else -> 0.80f
    }
    TelemetryInstrumentTile(
        title = title,
        value = value,
        unit = unit,
        progressFraction = fraction,
        indicatorColor = iconColor,
        modifier = modifier
    )
}

// ─── Production Grade Alert Card (Severity Weighted) ─────────────────────────

@Composable
fun AlertCard(
    alert: Alert,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val severityColor = alert.severity.toColor()
    val isCritical = alert.severity == AlertSeverity.CRITICAL

    SeverityAccentCard(
        severity = alert.severity,
        modifier = modifier,
        onClick = onClick
    ) {
        // Top Row: Severity Badge + Timestamp
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            HazardBadge(severity = alert.severity)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = TextSubtle
                )
                Text(
                    text = alert.issuedAt.toRelativeTimeString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    color = TextSubtle
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Alert Headline
        Text(
            text = alert.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = OnBackgroundDark,
            lineHeight = 20.sp
        )

        // Description
        if (alert.description.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = alert.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                lineHeight = 17.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bottom Row: Location Tag & Action Directive
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (alert.affectedDistrict.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = SurfaceVariantDark,
                    border = BorderStroke(1.dp, BorderSubtle)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(11.dp),
                            tint = TextMuted
                        )
                        Text(
                            text = alert.affectedDistrict,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = OnSurfaceDark
                        )
                    }
                }
            }

            // Directive guidance banner
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (isCritical) SeverityCritical.copy(alpha = 0.15f) else Color.Transparent
            ) {
                Text(
                    text = alert.severity.toActionGuideline(),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    fontWeight = if (isCritical) FontWeight.Bold else FontWeight.Medium,
                    color = if (isCritical) SeverityCritical else TextMuted,
                    modifier = Modifier.padding(horizontal = if (isCritical) 6.dp else 0.dp, vertical = 2.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ─── Graduated Severity Matrix Control (Scaling Weight & Urgency) ────────────

@Composable
fun GraduatedSeveritySelector(
    selectedSeverity: AlertSeverity,
    onSeveritySelected: (AlertSeverity) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        AlertSeverity.values().forEach { severity ->
            val isSelected = selectedSeverity == severity
            val color = severity.toColor()
            val height = when (severity) {
                AlertSeverity.LOW -> 62.dp
                AlertSeverity.MODERATE -> 70.dp
                AlertSeverity.HIGH -> 80.dp
                AlertSeverity.CRITICAL -> 92.dp
            }

            Surface(
                onClick = { onSeveritySelected(severity) },
                modifier = Modifier
                    .weight(1f)
                    .height(height),
                shape = RoundedCornerShape(8.dp),
                color = when {
                    isSelected && severity == AlertSeverity.CRITICAL -> SeverityCritical.copy(alpha = 0.25f)
                    isSelected -> color.copy(alpha = 0.2f)
                    else -> SurfaceDark
                },
                border = BorderStroke(
                    if (isSelected) 1.5.dp else 1.dp,
                    if (isSelected) color else BorderSubtle
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Indicator beacon
                    Box(
                        modifier = Modifier
                            .size(if (severity == AlertSeverity.CRITICAL) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) color else color.copy(alpha = 0.4f))
                    )

                    // Severity Label
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = severity.name,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                            fontSize = if (severity == AlertSeverity.CRITICAL) 11.sp else 10.sp,
                            color = if (isSelected) color else TextMuted,
                            letterSpacing = 0.4.sp,
                            maxLines = 1
                        )
                        if (severity == AlertSeverity.CRITICAL) {
                            Text(
                                text = "EVACUATE",
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.sp,
                                color = SeverityCritical,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── 3-Tier Button System ───────────────────────────────────────────────────

// Tier 1: Primary Action Button (Single dominant CTA per screen)
@Composable
fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    containerColor: Color = Primary80
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = Color.White,
            disabledContainerColor = containerColor.copy(alpha = 0.4f),
            disabledContentColor = Color.White.copy(alpha = 0.5f)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
            }
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 0.6.sp
            )
        }
    }
}

// Tier 2: Secondary / Ghost Button (Subtle hairline outline)
@Composable
fun GhostSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, BorderSubtle),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = OnSurfaceDark
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = TextMuted)
            }
            Text(
                text = text,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
        }
    }
}

// Tier 3: Dedicated Emergency SOS Dispatch Bar (Tactile Crimson Treatment)
@Composable
fun EmergencySOSBar(
    onTriggerSOS: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = SurfaceDark,
        border = BorderStroke(1.dp, SeverityCritical.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(SeverityCritical.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Emergency,
                        contentDescription = "SOS",
                        tint = SeverityCritical,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "EMERGENCY SOS DISPATCH",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp,
                        color = SeverityCritical
                    )
                    Text(
                        text = "One-tap GNSS broadcast to SDRF / NDMA",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            Button(
                onClick = onTriggerSOS,
                colors = ButtonDefaults.buttonColors(containerColor = SeverityCritical),
                shape = RoundedCornerShape(6.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    "TRANSMIT",
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    letterSpacing = 0.8.sp,
                    color = Color.White
                )
            }
        }
    }
}

// ─── Section Header ──────────────────────────────────────────────────────────

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = OnBackgroundDark
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = TextSubtle
                )
            }
        }
        if (trailingContent != null) {
            trailingContent()
        }
    }
}

// ─── Offline Synchronization Banner ──────────────────────────────────────────

@Composable
fun OfflineBanner(pendingCount: Int, modifier: Modifier = Modifier) {
    AnimatedVisibility(
        visible = pendingCount > 0,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SeverityModerate.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, SeverityModerate.copy(alpha = 0.35f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WifiOff,
                    contentDescription = null,
                    tint = SeverityModerate,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "$pendingCount report(s) cached offline • Will transmit upon reconnection",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = SeverityModerate,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ─── Loading & Error States ──────────────────────────────────────────────────

@Composable
fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = Primary80,
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(36.dp)
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = "SYNCHRONIZING TELEMETRY...",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = TextSubtle
            )
        }
    }
}

@Composable
fun ErrorContent(message: String, onRetry: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(SeverityCritical.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.WarningAmber,
                contentDescription = null,
                tint = SeverityCritical,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(Modifier.height(14.dp))
        Text(
            text = "Telemetry Disconnect",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = OnBackgroundDark
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        if (onRetry != null) {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = Primary80),
                shape = RoundedCornerShape(6.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Reconnect Telemetry", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

// ─── Clean Field Canvas Container ───────────────────────────────────────────

@Composable
fun GradientBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark),
        content = content
    )
}

// Backward-compatible alias for existing SOSFab references
@Composable
fun SOSFab(onClick: () -> Unit) {
    // Deprecated floating button replaced by embedded EmergencySOSBar
    EmergencySOSBar(onTriggerSOS = onClick)
}
