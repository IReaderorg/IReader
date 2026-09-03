package ireader.presentation.ui.sync.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.domain.models.sync.DiscoveredDevice
import ireader.domain.models.sync.SyncTransferScope
import ireader.domain.models.sync.TransferPreset
import ireader.presentation.ui.component.components.IAlertDialog

/**
 * Quick Share / ShareMe style modal sheet for choosing exactly what to transfer
 * to the target device (e.g. Everything vs Progress vs Downloaded Chapters).
 */
@Composable
fun QuickShareTransferSheet(
    device: DiscoveredDevice,
    initialScope: SyncTransferScope,
    isDeviceSaved: Boolean,
    onToggleSaveDevice: () -> Unit,
    onStartTransfer: (SyncTransferScope) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var scope by remember(device) { mutableStateOf(initialScope) }

    IAlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier.fillMaxWidth(),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Device Avatar Icon
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = getDeviceIcon(device.deviceInfo.deviceType),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = device.deviceInfo.deviceName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${device.deviceInfo.deviceType.displayName} • ${device.deviceInfo.ipAddress}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onToggleSaveDevice) {
                    Icon(
                        imageVector = if (isDeviceSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = if (isDeviceSaved) "Remove from Your Devices" else "Save to Your Devices",
                        tint = if (isDeviceSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Choose What to Transfer",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Transfer Presets Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = scope.preset == TransferPreset.EVERYTHING,
                        onClick = {
                            scope = SyncTransferScope.Everything
                        },
                        label = { Text("Everything") },
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = scope.preset == TransferPreset.LIBRARY_AND_PROGRESS,
                        onClick = {
                            scope = SyncTransferScope.LibraryAndProgress
                        },
                        label = { Text("Library") },
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        selected = scope.preset == TransferPreset.PROGRESS_ONLY,
                        onClick = {
                            scope = SyncTransferScope.ProgressOnly
                        },
                        label = { Text("Progress") },
                        modifier = Modifier.weight(1f)
                    )
                }

                HorizontalDivider()

                // Detailed Granular Toggles
                TransferOptionItem(
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    title = "Library Books",
                    subtitle = "Books, covers, authors & categories",
                    checked = scope.transferLibrary,
                    onCheckedChange = {
                        scope = scope.copy(transferLibrary = it, preset = TransferPreset.CUSTOM)
                    }
                )

                TransferOptionItem(
                    icon = Icons.Default.DownloadDone,
                    title = "Downloaded Chapters",
                    subtitle = "Offline readable chapter content (text & images)",
                    badge = "Offline Content",
                    checked = scope.transferDownloadedChapters,
                    onCheckedChange = {
                        scope = scope.copy(transferDownloadedChapters = it, preset = TransferPreset.CUSTOM)
                    }
                )

                TransferOptionItem(
                    icon = Icons.Default.Timeline,
                    title = "Reading Progress & History",
                    subtitle = "Bookmarks, current read position & history",
                    checked = scope.transferReadingProgress,
                    onCheckedChange = {
                        scope = scope.copy(transferReadingProgress = it, preset = TransferPreset.CUSTOM)
                    }
                )

                TransferOptionItem(
                    icon = Icons.Default.Settings,
                    title = "App & Reader Settings",
                    subtitle = "Theme, fonts, reader layout & preferences",
                    checked = scope.transferSettings,
                    onCheckedChange = {
                        scope = scope.copy(transferSettings = it, preset = TransferPreset.CUSTOM)
                    }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onStartTransfer(scope) },
                enabled = scope.transferLibrary || scope.transferReadingProgress || scope.transferDownloadedChapters || scope.transferSettings
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Send via Quick Share")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun TransferOptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    badge: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                if (badge != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.padding(vertical = 1.dp)
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
