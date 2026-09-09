package com.ner.landslide.presentation.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ner.landslide.domain.model.*
import com.ner.landslide.presentation.ui.theme.*
import com.ner.landslide.util.toRelativeTimeString

// ─── Severity color mapping ────────────────────────────────────────────────────

fun AlertSeverity.toColor(): Color = when (this) {
    AlertSeverity.LOW -> SeverityLow
    AlertSeverity.MODERATE -> SeverityModerate
    AlertSeverity.HIGH -> SeverityHigh
    AlertSeverity.CRITICAL -> SeverityCritical
}

fun AlertSeverity.toLabel(): String = when (this) {
    AlertSeverity.LOW -> "Low"
    AlertSeverity.MODERATE -> "Moderate"
    AlertSeverity.HIGH -> "High"
    AlertSeverity.CRITICAL -> "Critical"
}

fun String.toAlertSeverityColor(): Color = when (this.uppercase()) {
    "LOW" -> SeverityLow
    "MODERATE" -> SeverityModerate
    "HIGH" -> SeverityHigh
    "CRITICAL" -> SeverityCritical
    else -> SeverityLow
}

// ─── SeverityBadge ────────────────────────────────────────────────────────────

@Composable
fun SeverityBadge(severity: AlertSeverity, modifier: Modifier = Modifier) {
    val color = severity.toColor()
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
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
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

// ─── Alert Card ───────────────────────────────────────────────────────────────

@Composable
fun AlertCard(
    alert: Alert,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val borderColor = alert.severity.toColor()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(1.5.dp, borderColor.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SeverityBadge(severity = alert.severity)
                Text(
                    text = alert.issuedAt.toRelativeTimeString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = alert.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (alert.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = alert.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 2
                )
            }
            if (alert.affectedDistrict.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = alert.affectedDistrict,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// ─── Offline Banner ───────────────────────────────────────────────────────────

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
            color = SeverityHigh.copy(alpha = 0.15f),
            border = BorderStroke(1.dp, SeverityHigh.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WifiOff,
                    contentDescription = null,
                    tint = SeverityHigh,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "$pendingCount report(s) queued offline — will sync when connected",
                    style = MaterialTheme.typography.bodySmall,
                    color = SeverityHigh,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ─── SOS Floating Action Button ───────────────────────────────────────────────

@Composable
fun SOSFab(onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "sos_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    ExtendedFloatingActionButton(
        onClick = onClick,
        containerColor = SeverityCritical,
        contentColor = Color.White,
        modifier = Modifier.scale(scale)
    ) {
        Icon(Icons.Default.Emergency, contentDescription = "SOS")
        Spacer(Modifier.width(8.dp))
        Text(
            text = "SOS",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 16.sp
        )
    }
}

// ─── Loading / Error states ───────────────────────────────────────────────────

@Composable
fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun ErrorContent(message: String, onRetry: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        if (onRetry != null) {
            Spacer(Modifier.height(16.dp))
            OutlinedButton(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

// ─── Section Header ───────────────────────────────────────────────────────────

@Composable
fun SectionHeader(title: String, subtitle: String? = null, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

// ─── Gradient background ──────────────────────────────────────────────────────

@Composable
fun GradientBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val isDark = isSystemInDarkTheme()
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = if (isDark) listOf(
                        BackgroundDark,
                        SurfaceDark
                    ) else listOf(
                        BackgroundLight,
                        SurfaceLight
                    )
                )
            ),
        content = content
    )
}
