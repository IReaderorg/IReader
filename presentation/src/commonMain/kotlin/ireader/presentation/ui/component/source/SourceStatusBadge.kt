package ireader.presentation.ui.component.source

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.SourceStatus

/**
 * Badge component to display source status.
 * Shows different colors and icons based on the source's operational status.
 */
@Composable
fun SourceStatusBadge(
    status: SourceStatus,
    modifier: Modifier = Modifier,
    showLabel: Boolean = false,
    compact: Boolean = true
) {
    val (icon, color, label) = remember(status) {
        getStatusConfig(status)
    }
    
    val animatedColor by animateColorAsState(
        targetValue = color,
        animationSpec = tween(300),
        label = "status_color"
    )
    
    if (compact) {
        CompactBadge(
            icon = icon,
            color = animatedColor,
            label = if (showLabel) label else null,
            modifier = modifier
        )
    } else {
        ExpandedBadge(
            icon = icon,
            color = animatedColor,
            label = label,
            modifier = modifier
        )
    }
}

@Composable
private fun CompactBadge(
    icon: ImageVector,
    color: Color,
    label: String?,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(12.dp),
                tint = color
            )
            if (label != null) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ExpandedBadge(
    icon: ImageVector,
    color: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Dot indicator for source status (minimal version).
 */
@Composable
fun SourceStatusDot(
    status: SourceStatus,
    modifier: Modifier = Modifier,
    size: Int = 8
) {
    val (_, color, _) = remember(status) {
        getStatusConfig(status)
    }
    
    // Pulsing animation for certain statuses
    val shouldPulse = status is SourceStatus.Offline || status is SourceStatus.LoginRequired
    val alpha by animateFloatAsState(
        targetValue = if (shouldPulse) 0.5f else 1f,
        animationSpec = if (shouldPulse) {
            tween(durationMillis = 1000)
        } else {
            tween(durationMillis = 0)
        },
        label = "pulse"
    )
    
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = alpha))
    )
}

/**
 * Get configuration for a status.
 */
private fun getStatusConfig(status: SourceStatus): Triple<ImageVector, Color, String> {
    return when (status) {
        is SourceStatus.Working -> Triple(
            Icons.Default.CheckCircle,
            Color(0xFF4CAF50), // Green
            "Working"
        )
        is SourceStatus.Online -> Triple(
            Icons.Default.Cloud,
            Color(0xFF4CAF50), // Green
            "Online"
        )
        is SourceStatus.Offline -> Triple(
            Icons.Default.CloudOff,
            Color(0xFFFF9800), // Orange
            "Offline"
        )
        is SourceStatus.LoginRequired -> Triple(
            Icons.Default.Lock,
            Color(0xFFFF9800), // Orange
            "Login Required"
        )
        is SourceStatus.Outdated -> Triple(
            Icons.Default.Update,
            Color(0xFFFF9800), // Orange
            "Outdated"
        )
        is SourceStatus.LoadFailed -> Triple(
            Icons.Default.Error,
            Color(0xFFF44336), // Red
            "Failed"
        )
        is SourceStatus.RequiresPlugin -> Triple(
            Icons.Default.Extension,
            Color(0xFF9C27B0), // Purple
            "Plugin Required"
        )
        is SourceStatus.Incompatible -> Triple(
            Icons.Default.Block,
            Color(0xFFF44336), // Red
            "Incompatible"
        )
        is SourceStatus.Deprecated -> Triple(
            Icons.Default.Warning,
            Color(0xFF795548), // Brown
            "Deprecated"
        )
        is SourceStatus.Error -> Triple(
            Icons.Default.Error,
            Color(0xFFF44336), // Red
            "Error"
        )
        is SourceStatus.Unknown -> Triple(
            Icons.Default.HelpOutline,
            Color(0xFF9E9E9E), // Gray
            "Unknown"
        )
    }
}

/**
 * Get color for a status.
 */
fun getStatusColor(status: SourceStatus): Color {
    return getStatusConfig(status).second
}

/**
 * Get label for a status.
 */
fun getStatusLabel(status: SourceStatus): String {
    return getStatusConfig(status).third
}

/**
 * Get icon for a status.
 */
fun getStatusIcon(status: SourceStatus): ImageVector {
    return getStatusConfig(status).first
}
