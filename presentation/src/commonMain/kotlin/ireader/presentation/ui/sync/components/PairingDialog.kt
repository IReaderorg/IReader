package ireader.presentation.ui.sync.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.models.sync.DiscoveredDevice
import ireader.presentation.ui.component.components.IAlertDialog

/**
 * Dialog for confirming device pairing before sync.
 * 
 * Displays device information and allows user to confirm or cancel pairing.
 * Follows Material Design 3 dialog patterns with proper accessibility.
 * 
 * @param device The device to pair with
 * @param onPair Callback when user confirms pairing
 * @param onDismiss Callback when user dismisses the dialog
 * @param modifier Optional modifier for the dialog
 */
@Composable
fun PairingDialog(
    device: DiscoveredDevice,
    onPair: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    IAlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Devices,
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text("Pair with device?")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                DeviceInfoRow(
                    label = "Device Name",
                    value = device.deviceInfo.deviceName
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                DeviceInfoRow(
                    label = "Device Type",
                    value = device.deviceInfo.deviceType.name
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                DeviceInfoRow(
                    label = "IP Address",
                    value = "${device.deviceInfo.ipAddress}:${device.deviceInfo.port}"
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                DeviceInfoRow(
                    label = "App Version",
                    value = device.deviceInfo.appVersion
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "This will sync your reading progress, bookmarks, and library with this device.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onPair) {
                Text("Pair")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        modifier = modifier
    )
}

/**
 * Displays a label-value pair for device information.
 */
@Composable
private fun DeviceInfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
