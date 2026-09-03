package ireader.presentation.ui.sync.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ireader.domain.models.sync.DiscoveredDevice
import ireader.domain.models.sync.SavedDevice
import ireader.presentation.ui.component.components.IAlertDialog

/**
 * Section displaying the user's saved/trusted devices ("Your Devices")
 * for instant 1-tap connection.
 */
@Composable
fun SavedDevicesCard(
    savedDevices: List<SavedDevice>,
    discoveredDevices: List<DiscoveredDevice>,
    onConnect: (SavedDevice) -> Unit,
    onRemove: (String) -> Unit,
    onUpdateAlias: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var deviceToEdit by remember { mutableStateOf<SavedDevice?>(null) }

    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Column {
                    Text(
                        text = "Your Devices",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Saved & trusted devices for 1-tap quick connection",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (savedDevices.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No saved devices yet. Tap the bookmark icon on any nearby device to save it here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                savedDevices.forEachIndexed { index, savedDevice ->
                    val isOnline = discoveredDevices.any { it.deviceInfo.deviceId == savedDevice.deviceId }

                    if (index > 0) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Device Icon with Online Badge
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Icon(
                                imageVector = getDeviceIcon(savedDevice.deviceType),
                                contentDescription = savedDevice.deviceType.name,
                                modifier = Modifier.size(36.dp),
                                tint = if (isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isOnline) Color(0xFF4CAF50) else Color(0xFF9E9E9E))
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Device Details
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = savedDevice.displayName,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isOnline) "Online" else "Offline",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isOnline) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline
                                )
                            }
                            Text(
                                text = "${savedDevice.deviceType.displayName} • ${savedDevice.lastKnownIp}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Actions
                        IconButton(
                            onClick = { deviceToEdit = savedDevice },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit alias",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }

                        IconButton(
                            onClick = { onRemove(savedDevice.deviceId) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Remove device",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        FilledTonalButton(
                            onClick = { onConnect(savedDevice) },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Connect", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }

    // Rename Alias Dialog
    deviceToEdit?.let { device ->
        var newAlias by remember(device) { mutableStateOf(device.customAlias ?: "") }

        IAlertDialog(
            onDismissRequest = { deviceToEdit = null },
            title = { Text("Device Nickname") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Give \"${device.deviceName}\" a custom name for quick identification:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = newAlias,
                        onValueChange = { newAlias = it },
                        placeholder = { Text(device.deviceName) },
                        label = { Text("Custom Nickname") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onUpdateAlias(device.deviceId, newAlias)
                        deviceToEdit = null
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { deviceToEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
