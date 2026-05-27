package ireader.presentation.ui.sync

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import ireader.domain.models.sync.DeviceType
import ireader.presentation.ui.core.theme.LocalLocalizeHelper

/**
 * Dialog for configuring device settings for WiFi sync.
 * Allows users to:
 * - Set custom device name
 * - Select device type (Phone, Tablet, Desktop)
 * - Configure transfer options
 */
@Composable
fun DeviceSettingsDialog(
    currentDeviceName: String,
    currentDeviceType: DeviceType,
    syncLibrary: Boolean,
    syncReadingProgress: Boolean,
    syncDownloadedChapters: Boolean,
    syncSettings: Boolean,
    onDeviceNameChange: (String) -> Unit,
    onDeviceTypeChange: (DeviceType) -> Unit,
    onSyncLibraryChange: (Boolean) -> Unit,
    onSyncReadingProgressChange: (Boolean) -> Unit,
    onSyncDownloadedChaptersChange: (Boolean) -> Unit,
    onSyncSettingsChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Device Settings")
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Device name input
                OutlinedTextField(
                    value = currentDeviceName,
                    onValueChange = onDeviceNameChange,
                    label = { Text("Device Name") },
                    placeholder = { Text("My Device") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Device type selection
                Text(
                    text = "Device Type",
                    style = MaterialTheme.typography.labelLarge
                )
                
                Column(modifier = Modifier.selectableGroup()) {
                    DeviceType.values().forEach { deviceType ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = currentDeviceType == deviceType,
                                    onClick = { onDeviceTypeChange(deviceType) },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentDeviceType == deviceType,
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (deviceType) {
                                    DeviceType.ANDROID -> "Android"
                                    DeviceType.DESKTOP -> "Desktop"
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                HorizontalDivider()
                
                // Transfer options
                Text(
                    text = "Transfer Options",
                    style = MaterialTheme.typography.labelLarge
                )
                
                // Sync library
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = syncLibrary,
                        onCheckedChange = onSyncLibraryChange
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Library", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Sync books and categories",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Sync reading progress
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = syncReadingProgress,
                        onCheckedChange = onSyncReadingProgressChange
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Reading Progress", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Sync where you left off reading",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Sync downloaded chapters
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = syncDownloadedChapters,
                        onCheckedChange = onSyncDownloadedChaptersChange
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Downloaded Chapters", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Sync downloaded chapter content",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Sync settings
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = syncSettings,
                        onCheckedChange = onSyncSettingsChange
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Settings", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Sync app preferences",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
