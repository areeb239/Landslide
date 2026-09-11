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

@Composable
fun AlertSeverity.toThemeColor(): Color {
    val colors = BhurakshakTheme.colors
    return when (this) {
        AlertSeverity.LOW -> colors.success
        AlertSeverity.MODERATE -> colors.warning
        AlertSeverity.HIGH -> colors.warning
        AlertSeverity.CRITICAL -> colors.critical
    }
}

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
    color: Color? = null,
    size: Dp = 8.dp,
    modifier: Modifier = Modifier
) {
    val effectiveColor = color ?: BhurakshakTheme.colors.accent
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
                .background(effectiveColor.copy(alpha = waveAlpha))
        )
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(effectiveColor)
        )
    }
}

// ─── Purpose-Built Field Card Container (Zero Neon Glow, Dynamic Token Set) ─

@Composable
fun FieldCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    borderWidth: Dp = 1.dp,
    elevation: Dp? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = BhurakshakTheme.colors
    val effectiveBg = backgroundColor ?: colors.bgSurface
    val effectiveBorder = borderColor ?: colors.borderDefault
    val effectiveElevation = elevation ?: if (colors.isDark) 0.dp else 1.dp

    val baseModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else modifier

    Surface(
        modifier = baseModifier,
        shape = shape,
        color = effectiveBg,
        border = BorderStroke(borderWidth, effectiveBorder),
        tonalElevation = 0.dp,
        shadowElevation = effectiveElevation
    ) {
        Column(content = content)
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    borderWidth: Dp = 1.dp,
    glowAccent: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = BhurakshakTheme.colors
    val effectiveBorder = if (glowAccent != null) glowAccent.copy(alpha = 0.4f) else (borderColor ?: colors.borderDefault)
    FieldCard(
        modifier = modifier,
        shape = shape,
        backgroundColor = backgroundColor ?: colors.bgSurface,
        borderColor = effectiveBorder,
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
    val colors = BhurakshakTheme.colors
    val accentColor = severity.toThemeColor()
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
        color = colors.bgSurface,
        border = BorderStroke(
            1.dp,
            if (isCritical) colors.critical.copy(alpha = 0.6f) else colors.borderDefault
        ),
        shadowElevation = if (colors.isDark) 0.dp else 1.dp
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
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
    val color = severity.toThemeColor()
    val strings = LocalAppStrings.current
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
                text = severity.toLocalizedLabel(strings).uppercase(),
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
    progressFraction: Float,
    indicatorColor: Color? = null,
    modifier: Modifier = Modifier
) {
    val colors = BhurakshakTheme.colors
    val effectiveColor = indicatorColor ?: colors.accent

    FieldCard(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        backgroundColor = colors.bgSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textSecondary,
                letterSpacing = 0.4.sp,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.padding(vertical = 1.dp)
            ) {
                Text(
                    text = value,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = colors.textPrimary,
                    maxLines = 1,
                    softWrap = false
                )
                if (unit.isNotBlank()) {
                    Spacer(Modifier.width(3.dp))
                    Text(
                        text = unit,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(bottom = 1.dp),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(colors.borderDefault)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progressFraction.coerceIn(0.05f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(effectiveColor)
                )
            }
        }
    }
}

@Composable
fun TelemetryMetricItem(
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    iconColor: Color? = null,
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
    val colors = BhurakshakTheme.colors
    val strings = LocalAppStrings.current
    val isCritical = alert.severity == AlertSeverity.CRITICAL

    SeverityAccentCard(
        severity = alert.severity,
        modifier = modifier,
        onClick = onClick
    ) {
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
                    tint = colors.textSecondary
                )
                Text(
                    text = alert.issuedAt.toRelativeTimeString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = alert.getLocalizedTitle(strings),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = colors.textPrimary,
            lineHeight = 20.sp
        )

        val localizedDesc = alert.getLocalizedDescription(strings)
        if (localizedDesc.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = localizedDesc,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary,
                lineHeight = 17.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (alert.affectedDistrict.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = colors.bgSurface,
                    border = BorderStroke(1.dp, colors.borderDefault)
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
                            tint = colors.textSecondary
                        )
                        Text(
                            text = alert.affectedDistrict,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textPrimary
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (isCritical) colors.critical.copy(alpha = 0.15f) else Color.Transparent
            ) {
                Text(
                    text = alert.severity.toLocalizedDirective(strings),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    fontWeight = if (isCritical) FontWeight.Bold else FontWeight.Medium,
                    color = if (isCritical) colors.critical else colors.textSecondary,
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
    val colors = BhurakshakTheme.colors
    val strings = LocalAppStrings.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        AlertSeverity.values().forEach { severity ->
            val isSelected = selectedSeverity == severity
            val color = severity.toThemeColor()
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
                    isSelected && severity == AlertSeverity.CRITICAL -> colors.critical.copy(alpha = 0.25f)
                    isSelected -> color.copy(alpha = 0.2f)
                    else -> colors.bgSurface
                },
                border = BorderStroke(
                    if (isSelected) 1.5.dp else 1.dp,
                    if (isSelected) color else colors.borderDefault
                ),
                shadowElevation = if (colors.isDark) 0.dp else 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 2.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (severity == AlertSeverity.CRITICAL) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) color else color.copy(alpha = 0.4f))
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = severity.toLocalizedLabel(strings).uppercase(),
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                            fontSize = if (severity == AlertSeverity.CRITICAL) 10.sp else 9.sp,
                            color = if (isSelected) color else colors.textSecondary,
                            letterSpacing = 0.2.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (severity == AlertSeverity.CRITICAL) {
                            Text(
                                text = strings.directiveEvacuate.uppercase(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 7.5.sp,
                                color = colors.critical,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── 3-Tier Button System ───────────────────────────────────────────────────

@Composable
fun PrimaryActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    containerColor: Color? = null
) {
    val effectiveColor = containerColor ?: BhurakshakTheme.colors.accent
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = effectiveColor,
            contentColor = Color.White,
            disabledContainerColor = effectiveColor.copy(alpha = 0.4f),
            disabledContentColor = Color.White.copy(alpha = 0.5f)
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            }
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp,
                letterSpacing = 0.4.sp,
                lineHeight = 15.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

@Composable
fun GhostSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    val colors = BhurakshakTheme.colors
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
        border = BorderStroke(1.dp, colors.borderDefault),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = colors.textPrimary
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = colors.textSecondary)
            }
            Text(
                text = text,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                letterSpacing = 0.4.sp
            )
        }
    }
}

// Tier 3: Dedicated Emergency SOS Dispatch Bar (Visually Dominant Hero Treatment)
@Composable
fun EmergencySOSBar(
    onTriggerSOS: () -> Unit,
    modifier: Modifier = Modifier,
    isCitizenMode: Boolean = true
) {
    val colors = BhurakshakTheme.colors
    val strings = LocalAppStrings.current

    // Pulsing halo animation for the emergency trigger
    val infiniteTransition = rememberInfiniteTransition(label = "sos_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sos_scale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sos_glow"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer pulsing glow/halo
        Box(
            modifier = Modifier
                .matchParentSize()
                .scale(pulseScale)
                .clip(RoundedCornerShape(14.dp))
                .background(colors.critical.copy(alpha = glowAlpha * 0.25f))
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onTriggerSOS),
            shape = RoundedCornerShape(12.dp),
            color = colors.critical,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 56.dp)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Emergency,
                        contentDescription = "SOS",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = strings.sosButtonLabel,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        letterSpacing = 0.5.sp,
                        color = Color.White
                    )
                    Text(
                        text = strings.sosButtonSub,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.88f)
                    )
                }

                Icon(
                    Icons.Default.ArrowForwardIos,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
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
    val colors = BhurakshakTheme.colors
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
                color = colors.textPrimary
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = colors.textSecondary
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
    val colors = BhurakshakTheme.colors
    AnimatedVisibility(
        visible = pendingCount > 0,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = colors.warning.copy(alpha = 0.12f),
            border = BorderStroke(1.dp, colors.warning.copy(alpha = 0.35f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WifiOff,
                    contentDescription = null,
                    tint = colors.warning,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "$pendingCount report(s) cached offline • Will transmit upon reconnection",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = colors.warning,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ─── Loading & Error States ──────────────────────────────────────────────────

@Composable
fun LoadingContent(modifier: Modifier = Modifier) {
    val colors = BhurakshakTheme.colors
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                color = colors.accent,
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
                color = colors.textSecondary
            )
        }
    }
}

@Composable
fun ErrorContent(message: String, onRetry: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    val colors = BhurakshakTheme.colors
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(colors.critical.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.WarningAmber,
                contentDescription = null,
                tint = colors.critical,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(Modifier.height(14.dp))
        Text(
            text = "Telemetry Disconnect",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        if (onRetry != null) {
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = colors.accent),
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
    val colors = BhurakshakTheme.colors
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.bgBase),
        content = content
    )
}

@Composable
fun SOSFab(onClick: () -> Unit) {
    EmergencySOSBar(onTriggerSOS = onClick)
}
