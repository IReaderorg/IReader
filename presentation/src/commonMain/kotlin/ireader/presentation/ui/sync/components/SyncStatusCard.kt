package ireader.presentation.ui.sync.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.models.sync.SyncStatus

/**
 * Composable that displays the current sync status.
 * 
 * Shows different UI based on the sync status (connecting, syncing, completed, failed).
 * Follows Material Design 3 guidelines with proper status indication.
 * 
 * @param syncStatus Current sync status
 * @param modifier Optional modifier for the card
 */
@Composable
fun SyncStatusCard(
    syncStatus: SyncStatus,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (syncStatus) {
                is SyncStatus.Failed -> MaterialTheme.colorScheme.errorContainer
                is SyncStatus.Completed -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            when (syncStatus) {
                is SyncStatus.Idle -> {
                    // Should not be displayed
                }
                
                is SyncStatus.Discovering -> {
                    SyncStatusDiscovering()
                }
                
                is SyncStatus.Connecting -> {
                    SyncStatusConnecting(syncStatus)
                }
                
                is SyncStatus.Syncing -> {
                    SyncStatusSyncing(syncStatus)
                }
                
                is SyncStatus.Completed -> {
                    SyncStatusCompleted(syncStatus)
                }
                
                is SyncStatus.Failed -> {
                    SyncStatusFailed(syncStatus)
                }
            }
        }
    }
}

@Composable
private fun SyncStatusDiscovering() {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = "Discovering devices...",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SyncStatusConnecting(status: SyncStatus.Connecting) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(
                text = "Connecting...",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = status.deviceName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SyncStatusSyncing(status: SyncStatus.Syncing) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Syncing with ${status.deviceName}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "${(status.progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LinearProgressIndicator(
            progress = { status.progress },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = status.currentItem,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SyncStatusCompleted(status: SyncStatus.Completed) {
    Column {
        Text(
            text = "Sync completed",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Synced ${status.syncedItems} items with ${status.deviceName} in ${formatDuration(status.duration)}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun SyncStatusFailed(status: SyncStatus.Failed) {
    Column {
        Text(
            text = "Sync failed",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = if (status.deviceName != null) {
                "Failed to sync with ${status.deviceName}: ${status.error.message}"
            } else {
                "Sync failed: ${status.error.message}"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

/**
 * Formats duration in milliseconds to a human-readable string.
 */
private fun formatDuration(durationMs: Long): String {
    val seconds = durationMs / 1000
    return when {
        seconds < 60 -> "${seconds}s"
        seconds < 3600 -> "${seconds / 60}m ${seconds % 60}s"
        else -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
    }
}
