package ireader.presentation.ui.plugins.details.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.domain.plugins.PluginResourceUsage
import ireader.domain.plugins.ResourceUsagePercentages
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

/**
 * Section displaying real-time resource usage for a plugin
 * Requirements: 4.8, 4.9, 4.10
 */
@Composable
fun ResourceUsageSection(
    usage: PluginResourceUsage,
    percentages: ResourceUsagePercentages,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = localizeHelper.localize(Res.string.resource_usage),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // CPU Usage
            ResourceUsageItem(
                icon = Icons.Default.Speed,
                label = localizeHelper.localize(Res.string.cpu),
                value = "${usage.cpuUsagePercent.format(1)}%",
                percentage = percentages.cpuPercent.toFloat() / 100f,
                limit = "50%",
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Memory Usage
            ResourceUsageItem(
                icon = Icons.Default.Memory,
                label = localizeHelper.localize(Res.string.memory),
                value = "${usage.memoryUsageMB.format(1)} MB",
                percentage = percentages.memoryPercent.toFloat() / 100f,
                limit = "64 MB",
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Network Usage
            ResourceUsageItem(
                icon = Icons.Default.NetworkCheck,
                label = localizeHelper.localize(Res.string.network),
                value = "${(usage.networkUsageBytes / (1024.0 * 1024.0)).format(1)} MB/min",
                percentage = percentages.networkPercent.toFloat() / 100f,
                limit = "10 MB/min",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Individual resource usage item with progress indicator
 */
@Composable
private fun ResourceUsageItem(
    icon: ImageVector,
    label: String,
    value: String,
    percentage: Float,
    limit: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = "$value / $limit",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LinearProgressIndicator(
            progress = percentage.coerceIn(0f, 1f),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = getProgressColor(percentage),
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

/**
 * Get progress bar color based on usage percentage
 */
@Composable
private fun getProgressColor(percentage: Float): androidx.compose.ui.graphics.Color {
    return when {
        percentage >= 1.0f -> MaterialTheme.colorScheme.error
        percentage >= 0.8f -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }
}

/**
 * Extension function to format doubles
 */
private fun Double.format(decimals: Int): String {
    return ireader.presentation.ui.core.utils.formatDecimal(this, decimals)
}

/**
 * Resource usage history graph
 * Requirements: 4.8
 */
@Composable
fun ResourceUsageHistoryGraph(
    history: List<PluginResourceUsage>,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = localizeHelper.localize(Res.string.usage_history_last_hour),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (history.isEmpty()) {
                Text(
                    text = localizeHelper.localize(Res.string.no_usage_data_available_yet),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                // Simple text-based history for now
                // In a full implementation, this would be a proper graph
                Column {
                    Text(
                        text = "Average CPU: ${history.map { it.cpuUsagePercent }.average().format(1)}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Average Memory: ${history.map { it.memoryUsageMB }.average().format(1)} MB",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Peak CPU: ${history.maxOfOrNull { it.cpuUsagePercent }?.format(1) ?: "0.0"}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Peak Memory: ${history.maxOfOrNull { it.memoryUsageMB }?.format(1) ?: "0.0"} MB",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
