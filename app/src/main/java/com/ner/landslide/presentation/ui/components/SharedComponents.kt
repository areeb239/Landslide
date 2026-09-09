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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ner.landslide.domain.model.*
import com.ner.landslide.presentation.ui.theme.*
import com.ner.landslide.util.toRelativeTimeString

// ─── Severity Color & Label Mappings ──────────────────────────────────────────

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
    AlertSeverity.CRITICAL -> "Evacuate high-risk slope zones immediately"
}

fun String.toAlertSeverityColor(): Color = when (this.uppercase()) {
    "LOW" -> SeverityLow
    "MODERATE" -> SeverityModerate
    "HIGH" -> SeverityHigh
    "CRITICAL" -> SeverityCritical
    else -> SeverityLow
}

// ─── Pulsing Status Beacon ───────────────────────────────────────────────────

@Composable
fun PulsingStatusDot(
    color: Color = Primary80,
    size: Dp = 8.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_beacon")
    val waveScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_scale"
    )
    val waveAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_alpha"
    )

    Box(contentAlignment = Alignment.Center, modifier = modifier.size(size * 2.4f)) {
        // Expanding radar wave
        Box(
            modifier = Modifier
                .size(size)
                .scale(waveScale)
                .clip(CircleShape)
                .background(color.copy(alpha = waveAlpha))
        )
        // Core glowing dot
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(color)
        )
    }
}

// ─── Frosted GlassCard Container ─────────────────────────────────────────────

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color = SurfaceVariantDark.copy(alpha = 0.75f),
    borderColor: Color = Color.White.copy(alpha = 0.08f),
    borderWidth: Dp = 1.dp,
    glowAccent: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val borderStroke = if (glowAccent != null) {
        BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(glowAccent.copy(alpha = 0.7f), borderColor)))
    } else {
        BorderStroke(borderWidth, borderColor)
    }

    val baseModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else modifier

    Surface(
        modifier = baseModifier,
        shape = shape,
        color = backgroundColor,
        border = borderStroke,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Column(content = content)
    }
}

// ─── Severity & Hazard Badges ────────────────────────────────────────────────

@Composable
fun HazardBadge(
    severity: AlertSeverity,
    modifier: Modifier = Modifier
) {
    val color = severity.toColor()
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PulsingStatusDot(color = color, size = 6.dp)
            Text(
                text = severity.toLabel().uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = color
            )
        }
    }
}

@Composable
fun SeverityBadge(severity: AlertSeverity, modifier: Modifier = Modifier) {
    HazardBadge(severity = severity, modifier = modifier)
}

// ─── Telemetry Metric Item ───────────────────────────────────────────────────

@Composable
fun TelemetryMetricItem(
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    iconColor: Color = Primary80,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        backgroundColor = SurfaceDark.copy(alpha = 0.88f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(15.dp))
            }
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 0.3.sp,
                maxLines = 1,
                softWrap = false,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = value,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = OnBackgroundDark,
                    maxLines = 1,
                    softWrap = false
                )
                if (unit.isNotBlank()) {
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = unit,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSubtle,
                        modifier = Modifier.padding(bottom = 1.dp),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

// ─── Production Grade Alert Card ─────────────────────────────────────────────

@Composable
fun AlertCard(
    alert: Alert,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val severityColor = alert.severity.toColor()

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        glowAccent = if (alert.severity == AlertSeverity.CRITICAL || alert.severity == AlertSeverity.HIGH) severityColor else null,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Severity Badge + Pulsing Indicator + Time
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
                        modifier = Modifier.size(13.dp),
                        tint = TextSubtle
                    )
                    Text(
                        text = alert.issuedAt.toRelativeTimeString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSubtle
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Alert Title
            Text(
                text = alert.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = OnBackgroundDark,
                lineHeight = 22.sp
            )

            // Alert Description
            if (alert.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = alert.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    lineHeight = 18.sp,
                    maxLines = 3
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bottom Row: Location Chip + Directive Guidance
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (alert.affectedDistrict.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SurfaceElevated.copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = CyberCyan
                            )
                            Text(
                                text = alert.affectedDistrict,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = CyberCyan
                            )
                        }
                    }
                }

                // Advisory Pill
                Text(
                    text = alert.severity.toActionGuideline(),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = severityColor.copy(alpha = 0.9f),
                    maxLines = 1
                )
            }
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
            color = SeverityModerate.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, SeverityModerate.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WifiOff,
                    contentDescription = null,
                    tint = SeverityModerate,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "$pendingCount incident report(s) saved offline • Will sync automatically upon reconnection",
                    style = MaterialTheme.typography.bodySmall,
                    color = SeverityModerate,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ─── High Impact SOS Floating Action Button ──────────────────────────────────

@Composable
fun SOSFab(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "sos_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(28.dp),
        shadowElevation = 10.dp,
        border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.3f)),
        modifier = Modifier.scale(scale)
    ) {
        Box(
            modifier = Modifier
                .background(CrimsonEmergencyGradient)
                .padding(horizontal = 22.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Emergency,
                    contentDescription = "SOS",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "SOS DISPATCH",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    letterSpacing = 1.sp,
                    color = Color.White
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
                strokeWidth = 3.dp,
                modifier = Modifier.size(44.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "SYNCHRONIZING TELEMETRY...",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = TextMuted
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
                .size(64.dp)
                .clip(CircleShape)
                .background(SeverityCritical.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.WarningAmber,
                contentDescription = null,
                tint = SeverityCritical,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Telemetry Disconnect",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = OnBackgroundDark
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            lineHeight = 20.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        if (onRetry != null) {
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = Primary80)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Reconnect Telemetry", fontWeight = FontWeight.Bold)
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
                fontSize = 18.sp,
                color = OnBackgroundDark
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    color = TextSubtle
                )
            }
        }
        if (trailingContent != null) {
            trailingContent()
        }
    }
}

// ─── Gradient Background Container ───────────────────────────────────────────

@Composable
fun GradientBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        ObsidianBase,
                        BackgroundDark,
                        Color(0xFF0F172A)
                    )
                )
            ),
        content = content
    )
}

