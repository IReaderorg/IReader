package ireader.presentation.ui.sync.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.PhoneAndroid
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
 * @param modifier Optional modifier for the item
 */
@Composable
fun DeviceListItem(
    device: DiscoveredDevice,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
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
                Text(
                    text = device.deviceInfo.deviceName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "${device.deviceInfo.ipAddress}:${device.deviceInfo.port}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
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
private fun getDeviceIcon(deviceType: DeviceType): ImageVector {
    return when (deviceType) {
        DeviceType.ANDROID -> Icons.Default.PhoneAndroid
        DeviceType.DESKTOP -> Icons.Default.Computer
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
