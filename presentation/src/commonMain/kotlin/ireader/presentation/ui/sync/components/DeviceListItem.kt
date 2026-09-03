package ireader.presentation.ui.sync.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.TabletAndroid
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ireader.domain.models.sync.DeviceType
import ireader.domain.models.sync.DiscoveredDevice

/**
 * Composable that displays a discovered device in a list.
 * 
 * Shows device name, type icon, IP address, and reachability status.
 * Follows Material Design 3 guidelines with proper accessibility support.
 * 
 * @param device The discovered device to display
 * @param onClick Callback when the device is clicked
 * @param onToggleSave Optional callback when user bookmarks/saves this device
 * @param isSaved Whether this device is already saved in Your Devices
 * @param modifier Optional modifier for the item
 */
@Composable
fun DeviceListItem(
    device: DiscoveredDevice,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onToggleSave: (() -> Unit)? = null,
    isSaved: Boolean = false
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription = "Device: ${device.deviceInfo.deviceName}, " +
                    "IP: ${device.deviceInfo.ipAddress}:${device.deviceInfo.port}, " +
                    "${if (device.isReachable) "reachable" else "unreachable"}"
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Device type icon
            Icon(
                imageVector = getDeviceIcon(device.deviceInfo.deviceType),
                contentDescription = device.deviceInfo.deviceType.name,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Device info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = device.deviceInfo.deviceName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${device.deviceInfo.deviceType.displayName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "${device.deviceInfo.ipAddress}:${device.deviceInfo.port}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (onToggleSave != null) {
                IconButton(
                    onClick = onToggleSave,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = if (isSaved) "Remove from Your Devices" else "Save to Your Devices",
                        tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
            
            // Reachability indicator
            ReachabilityIndicator(
                isReachable = device.isReachable,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

/**
 * Returns the appropriate icon for the device type.
 */
@Composable
fun getDeviceIcon(deviceType: DeviceType): ImageVector {
    return when (deviceType) {
        DeviceType.PHONE, DeviceType.ANDROID -> Icons.Default.PhoneAndroid
        DeviceType.TABLET -> Icons.Default.TabletAndroid
        DeviceType.DESKTOP -> Icons.Default.Computer
        DeviceType.TV -> Icons.Default.Tv
        DeviceType.UNKNOWN -> Icons.Default.Devices
    }
}

/**
 * Displays a colored dot indicating device reachability.
 * 
 * @param isReachable Whether the device is reachable
 * @param modifier Optional modifier for the indicator
 */
@Composable
private fun ReachabilityIndicator(
    isReachable: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.semantics {
            contentDescription = if (isReachable) "Device is reachable" else "Device is unreachable"
        },
        shape = MaterialTheme.shapes.small,
        color = if (isReachable) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.error
        }
    ) {
        // Empty surface acts as colored dot
    }
}
