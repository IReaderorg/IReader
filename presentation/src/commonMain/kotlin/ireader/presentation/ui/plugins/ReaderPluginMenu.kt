package ireader.presentation.ui.plugins

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ireader.plugin.api.PluginMenuItem
import ireader.plugin.api.PluginScreen
import ireader.plugin.api.PluginScreenContext

/**
 * Bottom sheet showing available plugin menu items in the reader.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderPluginMenuSheet(
    menuItems: List<PluginMenuItem>,
    onMenuItemClick: (PluginMenuItem) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState()
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Plugins",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            HorizontalDivider()
            
            if (menuItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Extension,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "No plugins available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn {
                    items(menuItems) { item ->
                        PluginMenuItemRow(
                            item = item,
                            onClick = { onMenuItemClick(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PluginMenuItemRow(
    item: PluginMenuItem,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(item.label) },
        leadingContent = {
            Icon(
                imageVector = getIconForName(item.icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

/**
 * Map icon names to Material Icons
 */
private fun getIconForName(iconName: String?): ImageVector {
    return when (iconName) {
        "note_add", "notes" -> Icons.Default.Notes
        "person_add", "people" -> Icons.Default.People
        "timeline" -> Icons.Default.Timeline
        "auto_awesome" -> Icons.Default.AutoAwesome
        "list" -> Icons.Default.List
        "history" -> Icons.Default.History
        "settings" -> Icons.Default.Settings
        "share" -> Icons.Default.Share
        "bookmark" -> Icons.Default.Bookmark
        "highlight" -> Icons.Default.Highlight
        else -> Icons.Default.Extension
    }
}

/**
 * Floating action button for quick access to plugin features.
 */
@Composable
fun PluginQuickAccessFab(
    hasPlugins: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (hasPlugins) {
        FloatingActionButton(
            onClick = onClick,
            modifier = modifier,
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Icon(
                Icons.Default.Extension,
                contentDescription = "Plugins",
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
