package ireader.presentation.ui.home.sources.extension

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.ExtensionStatistics
import java.text.SimpleDateFormat
import java.util.*
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*

/**
 * Dialog showing extension usage statistics
 */
@Composable
fun ExtensionStatisticsDialog(
    statistics: ExtensionStatistics,
    extensionName: String,
    onDismiss: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Analytics,
                    contentDescription = null
                )
                Text("Statistics: $extensionName")
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatisticItem(
                    icon = Icons.Default.CalendarToday,
                    label = "Installed",
                    value = formatDate(statistics.installDate)
                )
                
                StatisticItem(
                    icon = Icons.Default.AccessTime,
                    label = "Last Used",
                    value = if (statistics.lastUsed > 0) {
                        formatDate(statistics.lastUsed)
                    } else {
                        "Never"
                    }
                )
                
                StatisticItem(
                    icon = Icons.Default.TouchApp,
                    label = "Usage Count",
                    value = statistics.usageCount.toString()
                )
                
                StatisticItem(
                    icon = Icons.Default.Error,
                    label = "Errors",
                    value = statistics.errorCount.toString()
                )
                
                StatisticItem(
                    icon = Icons.Default.Speed,
                    label = "Avg Response Time",
                    value = "${statistics.averageResponseTime}ms"
                )
                
                StatisticItem(
                    icon = Icons.Default.DataUsage,
                    label = "Data Transferred",
                    value = formatBytes(statistics.totalDataTransferred)
                )
                
                if (statistics.crashCount > 0) {
                    StatisticItem(
                        icon = Icons.Default.BugReport,
                        label = "Crashes",
                        value = statistics.crashCount.toString(),
                        isError = true
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(localizeHelper.localize(Res.string.close))
            }
        }
    )
}

@Composable
private fun StatisticItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    isError: Boolean = false
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatBytes(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "${bytes / (1024 * 1024 * 1024)} GB"
    }
}
