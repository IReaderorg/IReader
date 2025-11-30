package ireader.presentation.ui.plugins.management.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import ireader.domain.plugins.PluginInfo
import ireader.domain.plugins.PluginResourceUsage
import ireader.domain.plugins.PluginStatus
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

/**
 * Item displaying an installed plugin with controls
 * Requirements: 14.1, 14.2, 14.3, 14.4, 14.5
 */
@Composable
fun InstalledPluginItem(
    plugin: PluginInfo,
    hasUpdate: Boolean,
    newVersion: String?,
    resourceUsage: PluginResourceUsage?,
    onEnableToggle: (Boolean) -> Unit,
    onConfigure: () -> Unit,
    onUninstall: () -> Unit,
    onShowErrorDetails: () -> Unit,
    onUpdate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Plugin icon
                AsyncImage(
                    model = plugin.manifest.iconUrl,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Plugin info
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = plugin.manifest.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        // Update badge
                        if (hasUpdate) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary
                            ) {
                                Text("Update Available", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    
                    Text(
                        text = "v${plugin.manifest.version}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Status with color coding
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val (statusText, statusColor) = when (plugin.status) {
                            PluginStatus.ENABLED -> "Enabled" to Color(0xFF4CAF50) // Green
                            PluginStatus.DISABLED -> "Disabled" to Color.Gray
                            PluginStatus.ERROR -> "Error" to Color(0xFFF44336) // Red
                            PluginStatus.UPDATING -> "Updating..." to Color(0xFF2196F3) // Blue
                        }
                        
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = statusColor.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = statusText,
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        
                        // Error icon for error status
                        if (plugin.status == PluginStatus.ERROR) {
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = onShowErrorDetails,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = localizeHelper.localize(Res.string.show_error_details),
                                    tint = statusColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
                
                // Enable/Disable switch
                Switch(
                    checked = plugin.status == PluginStatus.ENABLED,
                    onCheckedChange = onEnableToggle,
                    enabled = plugin.status != PluginStatus.UPDATING
                )
            }
            
            // Resource usage (if available)
            resourceUsage?.let { usage ->
                Spacer(modifier = Modifier.height(8.dp))
                ResourceUsageIndicator(usage = usage)
            }
            
            // Action buttons
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Update button
                if (hasUpdate && newVersion != null) {
                    TextButton(
                        onClick = onUpdate,
                        enabled = plugin.status != PluginStatus.UPDATING
                    ) {
                        Icon(
                            imageVector = Icons.Default.Update,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Update to $newVersion")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                // Configure button
                IconButton(
                    onClick = onConfigure,
                    enabled = plugin.status == PluginStatus.ENABLED
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = localizeHelper.localize(Res.string.configure)
                    )
                }
                
                // Uninstall button
                IconButton(
                    onClick = onUninstall,
                    enabled = plugin.status != PluginStatus.UPDATING
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = localizeHelper.localize(Res.string.uninstall),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * Resource usage indicator showing memory and CPU usage
 */
@Composable
private fun ResourceUsageIndicator(
    usage: PluginResourceUsage,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Memory usage
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = localizeHelper.localize(Res.string.memory),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LinearProgressIndicator(
                progress = { (usage.memoryUsageMB.toDouble() / usage.memoryLimitMB.toDouble()).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = if (usage.memoryUsageMB > usage.memoryLimitMB * 0.8f) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
            Text(
                text = "${usage.memoryUsageMB.toInt()} / ${usage.memoryLimitMB.toInt()} MB",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // CPU usage
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = localizeHelper.localize(Res.string.cpu),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LinearProgressIndicator(
                progress = { (usage.cpuUsagePercent.toDouble() / 100.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = if (usage.cpuUsagePercent > 80f) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
            Text(
                text = "${usage.cpuUsagePercent.toInt()}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
