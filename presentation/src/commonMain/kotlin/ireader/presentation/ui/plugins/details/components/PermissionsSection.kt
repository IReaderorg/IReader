package ireader.presentation.ui.plugins.details.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.domain.plugins.PluginPermission
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

/**
 * Permissions section listing required permissions with explanations
 * Requirements: 7.4, 10.1, 10.2, 10.3
 */
@Composable
fun PermissionsSection(
    permissions: List<PluginPermission>,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = localizeHelper.localize(Res.string.permissions),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            permissions.forEach { permission ->
                PermissionItem(permission = permission)
            }
        }
    }
}

/**
 * Individual permission item with icon and explanation
 */
@Composable
private fun PermissionItem(
    permission: PluginPermission,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = getPermissionIcon(permission),
            contentDescription = permission.name,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = getPermissionTitle(permission),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = getPermissionDescription(permission),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Get icon for permission type
 */
private fun getPermissionIcon(permission: PluginPermission): ImageVector {
    return when (permission) {
        PluginPermission.NETWORK -> Icons.Default.Wifi
        PluginPermission.STORAGE -> Icons.Default.Storage
        PluginPermission.READER_CONTEXT -> Icons.Default.MenuBook
        PluginPermission.LIBRARY_ACCESS -> Icons.Default.LibraryBooks
        PluginPermission.PREFERENCES -> Icons.Default.Settings
        PluginPermission.NOTIFICATIONS -> Icons.Default.Notifications
    }
}

/**
 * Get human-readable title for permission
 */
private fun getPermissionTitle(permission: PluginPermission): String {
    return when (permission) {
        PluginPermission.NETWORK -> "Network Access"
        PluginPermission.STORAGE -> "Storage Access"
        PluginPermission.READER_CONTEXT -> "Reader Context"
        PluginPermission.LIBRARY_ACCESS -> "Library Access"
        PluginPermission.PREFERENCES -> "Preferences Access"
        PluginPermission.NOTIFICATIONS -> "Notifications"
    }
}

/**
 * Get description for permission
 */
private fun getPermissionDescription(permission: PluginPermission): String {
    return when (permission) {
        PluginPermission.NETWORK -> "Access the internet to fetch data or communicate with external services"
        PluginPermission.STORAGE -> "Read and write files to local storage"
        PluginPermission.READER_CONTEXT -> "Access information about the current reading session"
        PluginPermission.LIBRARY_ACCESS -> "View and modify your library contents"
        PluginPermission.PREFERENCES -> "Access and modify app preferences"
        PluginPermission.NOTIFICATIONS -> "Show notifications to keep you informed"
    }
}
