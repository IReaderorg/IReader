package ireader.presentation.ui.plugins.marketplace.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

/**
 * Horizontal scrolling section displaying featured/popular plugins
 * Requirements: 2.1, 2.2
 */
@Composable
fun FeaturedPluginsSection(
    plugins: List<PluginInfo>,
    onPluginClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onInstall: ((String) -> Unit)? = null
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    if (plugins.isEmpty()) return
    
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = localizeHelper.localize(Res.string.featured_plugins),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(plugins, key = { it.id }) { plugin ->
                FeaturedPluginCard(
                    plugin = plugin,
                    onClick = { onPluginClick(plugin.id) },
                    onInstall = onInstall
                )
            }
        }
    }
}

/**
 * Card for featured plugin in horizontal scroll
 */
@Composable
internal fun FeaturedPluginCard(
    plugin: PluginInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onInstall: ((String) -> Unit)? = null
) {
    Card(
        modifier = modifier
            .width(280.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Plugin icon/banner
            AsyncImage(
                model = plugin.manifest.iconUrl,
                contentDescription = plugin.manifest.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            )
            
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = plugin.manifest.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
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
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = plugin.manifest.description,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rating
                    plugin.rating?.let { rating ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = ireader.presentation.ui.core.utils.toDecimalString(rating.toDouble(), 1),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    // Downloads
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatDownloadCount(plugin.downloadCount),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Install button
                FeaturedInstallButton(
                    plugin = plugin,
                    onInstall = onInstall
                )
            }
        }
    }
}

/**
 * Install button for featured plugin card
 */
@Composable
private fun FeaturedInstallButton(
    plugin: PluginInfo,
    onInstall: ((String) -> Unit)?
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    when (plugin.status) {
        PluginStatus.NOT_INSTALLED -> {
            if (onInstall != null) {
                Button(
                    onClick = { onInstall(plugin.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(localizeHelper.localize(Res.string.install))
                }
            }
        }
        PluginStatus.UPDATING -> {
            OutlinedButton(
                onClick = {},
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(localizeHelper.localize(Res.string.installing_1))
            }
        }
        PluginStatus.ENABLED, PluginStatus.DISABLED -> {
            OutlinedButton(
                onClick = {},
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(localizeHelper.localize(Res.string.installed))
            }
        }
        PluginStatus.ERROR -> {
            if (onInstall != null) {
                Button(
                    onClick = { onInstall(plugin.id) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(localizeHelper.localize(Res.string.retry))
                }
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
