package ireader.presentation.ui.sync

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ireader.domain.models.sync.ConflictResolutionStrategy
import ireader.domain.models.sync.DeviceInfo
import ireader.domain.models.sync.DeviceType

/**
 * Settings screen for configuring sync preferences.
 * 
 * Allows users to:
 * - Configure conflict resolution strategy
 * - Manage trusted devices
 * - Toggle sync on charger only
 * - View privacy policy
 * 
 * Follows Material Design 3 guidelines and Compose best practices.
 * 
 * @param conflictResolutionStrategy Current conflict resolution strategy
 * @param trustedDevices List of trusted devices
 * @param syncOnChargerOnly Whether sync should only occur when charging
 * @param onConflictStrategyChange Callback when conflict strategy changes
 * @param onRemoveDevice Callback when user removes a trusted device
 * @param onSyncOnChargerOnlyChange Callback when sync on charger toggle changes
 * @param onNavigateBack Callback when user navigates back
 * @param modifier Optional modifier for the screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncSettingsScreen(
    conflictResolutionStrategy: ConflictResolutionStrategy,
    trustedDevices: List<DeviceInfo>,
    syncOnChargerOnly: Boolean,
    onConflictStrategyChange: (ConflictResolutionStrategy) -> Unit,
    onRemoveDevice: (String) -> Unit,
    onSyncOnChargerOnlyChange: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showStrategyDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sync Settings") },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.semantics {
                            contentDescription = "Navigate back"
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate back"
                        )
                    }
                }
            )
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Conflict Resolution Section
            item {
                ConflictResolutionSection(
                    currentStrategy = conflictResolutionStrategy,
                    onClick = { showStrategyDialog = true }
                )
            }
            
            // Trusted Devices Section
            item {
                Text(
                    text = "Trusted Devices",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            if (trustedDevices.isEmpty()) {
                item {
                    EmptyTrustedDevices()
                }
            } else {
                items(
                    items = trustedDevices,
                    key = { device -> device.deviceId }
                ) { device ->
                    TrustedDeviceItem(
                        device = device,
                        onRemove = { onRemoveDevice(device.deviceId) }
                    )
                }
            }
            
            // Sync on Charger Only Toggle
            item {
                SyncOnChargerToggle(
                    enabled = syncOnChargerOnly,
                    onToggle = onSyncOnChargerOnlyChange
                )
            }
            
            // Privacy Policy Link
            item {
                PrivacyPolicyLink()
            }
        }
    }
    
    // Conflict Strategy Selection Dialog
    if (showStrategyDialog) {
        ConflictStrategyDialog(
            currentStrategy = conflictResolutionStrategy,
            onStrategySelected = { strategy ->
                onConflictStrategyChange(strategy)
                showStrategyDialog = false
            },
            onDismiss = { showStrategyDialog = false }
        )
    }
}

/**
 * Section displaying conflict resolution strategy setting.
 */
@Composable
private fun ConflictResolutionSection(
    currentStrategy: ConflictResolutionStrategy,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Conflict Resolution",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = getStrategyDisplayName(currentStrategy),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = getStrategyDescription(currentStrategy),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Empty state for trusted devices list.
 */
@Composable
private fun EmptyTrustedDevices(
    modifier: Modifier = Modifier
) {
    Text(
        text = "No trusted devices",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp)
    )
}

/**
 * Item displaying a trusted device with remove button.
 */
@Composable
private fun TrustedDeviceItem(
    device: DeviceInfo,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.deviceName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "${device.deviceType.name} • ${device.ipAddress}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            IconButton(
                onClick = onRemove,
                modifier = Modifier.semantics {
                    contentDescription = "Remove ${device.deviceName}"
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove ${device.deviceName}",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * Toggle for sync on charger only setting.
 */
@Composable
private fun SyncOnChargerToggle(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Sync on Charger Only",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Only sync when device is charging to save battery",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                modifier = Modifier.testTag("sync_on_charger_toggle")
            )
        }
    }
}

/**
 * Link to privacy policy.
 */
@Composable
private fun PrivacyPolicyLink(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Privacy Policy",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "All sync operations occur entirely on your local network. " +
                    "No data is sent to external servers or cloud services. " +
                    "Your reading data remains private and under your control.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Dialog for selecting conflict resolution strategy.
 */
@Composable
private fun ConflictStrategyDialog(
    currentStrategy: ConflictResolutionStrategy,
    onStrategySelected: (ConflictResolutionStrategy) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Select Conflict Resolution Strategy")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ConflictResolutionStrategy.values().forEach { strategy ->
                    StrategyOption(
                        strategy = strategy,
                        isSelected = strategy == currentStrategy,
                        onClick = { onStrategySelected(strategy) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        modifier = modifier
    )
}

/**
 * Single strategy option in the selection dialog.
 */
@Composable
private fun StrategyOption(
    strategy: ConflictResolutionStrategy,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = getStrategyDisplayName(strategy),
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = getStrategyDescription(strategy),
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

/**
 * Get display name for conflict resolution strategy.
 */
private fun getStrategyDisplayName(strategy: ConflictResolutionStrategy): String {
    return when (strategy) {
        ConflictResolutionStrategy.LATEST_TIMESTAMP -> "Use Latest Timestamp"
        ConflictResolutionStrategy.LOCAL_WINS -> "Always Use Local"
        ConflictResolutionStrategy.REMOTE_WINS -> "Always Use Remote"
        ConflictResolutionStrategy.MERGE -> "Merge Changes"
        ConflictResolutionStrategy.MANUAL -> "Ask Every Time"
    }
}

/**
 * Get description for conflict resolution strategy.
 */
private fun getStrategyDescription(strategy: ConflictResolutionStrategy): String {
    return when (strategy) {
        ConflictResolutionStrategy.LATEST_TIMESTAMP -> 
            "Use data with the most recent modification time"
        ConflictResolutionStrategy.LOCAL_WINS -> 
            "Always prefer data from this device"
        ConflictResolutionStrategy.REMOTE_WINS -> 
            "Always prefer data from the other device"
        ConflictResolutionStrategy.MERGE -> 
            "Attempt to merge compatible changes automatically"
        ConflictResolutionStrategy.MANUAL -> 
            "Prompt for manual resolution when conflicts occur"
    }
}
