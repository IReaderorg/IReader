package ireader.presentation.ui.component.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.SourceStatus

/**
 * Displays a colored status indicator for a source
 * @param status The current status of the source
 * @param modifier Modifier for the composable
 * @param showLabel Whether to show a text label alongside the indicator
 */
@Composable
fun SourceStatusIndicator(
    status: SourceStatus,
    modifier: Modifier = Modifier,
    showLabel: Boolean = false
) {
    val (color, icon, label) = when (status) {
        is SourceStatus.Online -> Triple(
            Color(0xFF4CAF50), // Green
            Icons.Default.CheckCircle,
            "Online"
        )
        is SourceStatus.Offline -> Triple(
            Color(0xFFF44336), // Red
            Icons.Default.Error,
            "Offline"
        )
        is SourceStatus.LoginRequired -> Triple(
            Color(0xFFFFC107), // Yellow/Amber
            Icons.Default.Lock,
            "Login Required"
        )
        is SourceStatus.Error -> Triple(
            Color(0xFFF44336), // Red
            Icons.Default.Warning,
            "Error"
        )
    }
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(12.dp)
        )
        
        if (showLabel) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Displays a simple colored dot indicator for a source
 * @param status The current status of the source
 * @param modifier Modifier for the composable
 */
@Composable
fun SourceStatusDot(
    status: SourceStatus,
    modifier: Modifier = Modifier
) {
    val color = when (status) {
        is SourceStatus.Online -> Color(0xFF4CAF50) // Green
        is SourceStatus.Offline -> Color(0xFFF44336) // Red
        is SourceStatus.LoginRequired -> Color(0xFFFFC107) // Yellow/Amber
        is SourceStatus.Error -> Color(0xFFF44336) // Red
    }
    
    Box(
        modifier = modifier
            .size(8.dp)
            .background(color, CircleShape)
    )
}
