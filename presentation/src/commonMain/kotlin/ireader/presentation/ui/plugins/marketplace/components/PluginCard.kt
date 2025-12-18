package ireader.presentation.ui.plugins.marketplace.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import ireader.domain.plugins.PluginInfo
import ireader.domain.plugins.PluginStatus
import ireader.plugin.api.PluginMonetization
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.featurestore.DownloadProgress
import ireader.presentation.ui.featurestore.DownloadStatus
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

/**
 * Card displaying plugin information in the marketplace
 * Requirements: 2.1, 2.2, 2.3
 */
@Composable
fun PluginCard(
    plugin: PluginInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onInstall: ((String) -> Unit)? = null,
    onUninstall: ((String) -> Unit)? = null,
    onCancelDownload: ((String) -> Unit)? = null,
    downloadProgress: DownloadProgress? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Plugin icon
            AsyncImage(
                model = plugin.manifest.iconUrl,
                contentDescription = plugin.manifest.name,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Plugin info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = plugin.manifest.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = plugin.manifest.author.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = plugin.manifest.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Rating and downloads
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    plugin.rating?.let { rating ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = ireader.presentation.ui.core.utils.toDecimalString(rating.toDouble(), 1),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatDownloadCount(plugin.downloadCount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Install button or status with download progress
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (downloadProgress != null && downloadProgress.status != DownloadStatus.COMPLETED) {
                    DownloadProgressIndicator(
                        progress = downloadProgress,
                        onCancel = { onCancelDownload?.invoke(plugin.id) }
                    )
                } else {
                    InstallStatusButton(
                        plugin = plugin,
                        onInstall = onInstall,
                        onUninstall = onUninstall
                    )
                }
                
                // Price badge below button
                PriceBadge(monetization = plugin.manifest.monetization)
            }
        }
    }
}

/**
 * Download progress indicator with cancel button
 */
@Composable
private fun DownloadProgressIndicator(
    progress: DownloadProgress,
    onCancel: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.widthIn(min = 80.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { if (progress.progress > 0f) progress.progress else 0f },
                modifier = Modifier.size(40.dp),
                strokeWidth = 3.dp,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            // Cancel button in center
            IconButton(
                onClick = onCancel,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = "Cancel download",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Status text
        Text(
            text = when (progress.status) {
                DownloadStatus.PENDING -> "Queued"
                DownloadStatus.DOWNLOADING -> "${(progress.progress * 100).toInt()}%"
                DownloadStatus.INSTALLING -> "Installing..."
                DownloadStatus.COMPLETED -> "Done"
                DownloadStatus.FAILED -> "Failed"
            },
            style = MaterialTheme.typography.labelSmall,
            color = if (progress.status == DownloadStatus.FAILED) 
                MaterialTheme.colorScheme.error 
            else 
                MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        // Size info if available
        if (progress.totalBytes > 0 && progress.status == DownloadStatus.DOWNLOADING) {
            Text(
                text = formatBytes(progress.bytesDownloaded, progress.totalBytes),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Format bytes for display
 */
private fun formatBytes(downloaded: Long, total: Long): String {
    val downloadedMB = downloaded / (1024.0 * 1024.0)
    val totalMB = total / (1024.0 * 1024.0)
    return "${ireader.presentation.ui.core.utils.toDecimalString(downloadedMB, 1)}/${ireader.presentation.ui.core.utils.toDecimalString(totalMB, 1)} MB"
}

/**
 * Install button showing different states
 */
@Composable
private fun InstallStatusButton(
    plugin: PluginInfo,
    onInstall: ((String) -> Unit)?,
    onUninstall: ((String) -> Unit)? = null
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    when (plugin.status) {
        PluginStatus.NOT_INSTALLED -> {
            if (onInstall != null) {
                FilledTonalButton(
                    onClick = { onInstall(plugin.id) },
                    modifier = Modifier.widthIn(min = 80.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = localizeHelper.localize(Res.string.install),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
        PluginStatus.UPDATING -> {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
        }
        PluginStatus.ENABLED, PluginStatus.DISABLED -> {
            if (onUninstall != null) {
                // Show uninstall button for installed plugins
                OutlinedButton(
                    onClick = { onUninstall(plugin.id) },
                    modifier = Modifier.widthIn(min = 80.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = localizeHelper.localize(Res.string.uninstall),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            } else {
                // Fallback to showing "Installed" badge if no uninstall handler
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = localizeHelper.localize(Res.string.installed),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
        PluginStatus.ERROR -> {
            if (onInstall != null) {
                FilledTonalButton(
                    onClick = { onInstall(plugin.id) },
                    modifier = Modifier.widthIn(min = 80.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = localizeHelper.localize(Res.string.retry),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

/**
 * Badge showing plugin price or "Free"
 */
@Composable
private fun PriceBadge(
    monetization: PluginMonetization?,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    when (monetization) {
        is PluginMonetization.Premium -> {
            Surface(
                modifier = modifier,
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = formatPrice(monetization.price, monetization.currency),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
        is PluginMonetization.Freemium -> {
            Surface(
                modifier = modifier,
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = localizeHelper.localize(Res.string.freemium),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
        is PluginMonetization.Free, null -> {
            Surface(
                modifier = modifier,
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Text(
                    text = localizeHelper.localize(Res.string.free),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Format download count with K/M suffixes
 */
private fun formatDownloadCount(count: Int): String {
    return when {
        count >= 1_000_000 -> "${ireader.presentation.ui.core.utils.toDecimalString(count / 1_000_000.0, 1)}M"
        count >= 1_000 -> "${ireader.presentation.ui.core.utils.toDecimalString(count / 1_000.0, 1)}K"
        else -> count.toString()
    }
}

/**
 * Format price with currency symbol
 */
private fun formatPrice(price: Double, currency: String): String {
    val symbol = when (currency.uppercase()) {
        "USD" -> "$"
        "EUR" -> "€"
        "GBP" -> "£"
        "JPY" -> "¥"
        else -> currency
    }
    return "$symbol${ireader.presentation.ui.core.utils.toDecimalString(price, 2)}"
}
