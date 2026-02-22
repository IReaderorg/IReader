package ireader.presentation.ui.sync

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ireader.domain.models.sync.ConflictResolutionStrategy
import ireader.domain.models.sync.DiscoveredDevice
import ireader.domain.models.sync.SyncStatus
import ireader.presentation.ui.sync.components.ConflictResolutionDialog
import ireader.presentation.ui.sync.components.DeviceListItem
import ireader.presentation.ui.sync.components.EmptyDeviceList
import ireader.presentation.ui.sync.components.PairingDialog
import ireader.presentation.ui.sync.components.SyncStatusCard
import ireader.presentation.ui.sync.viewmodel.SyncViewModel
import org.koin.compose.koinInject

/**
 * Main screen for WiFi sync functionality.
 * 
 * Displays discovered devices, sync status, and controls for managing sync operations.
 * Follows Material Design 3 guidelines and Compose best practices.
 * 
 * @param state Current sync state
 * @param onStartDiscovery Callback when user starts device discovery
 * @param onStopDiscovery Callback when user stops device discovery
 * @param onDeviceClick Callback when user clicks on a device
 * @param onNavigateBack Callback when user navigates back
 * @param onPairDevice Callback when user confirms device pairing
 * @param onDismissPairing Callback when user dismisses pairing dialog
 * @param onResolveConflicts Callback when user resolves conflicts
 * @param onDismissConflicts Callback when user dismisses conflict dialog
 * @param onCancelSync Callback when user cancels ongoing sync
 * @param modifier Optional modifier for the screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncScreen(
    state: SyncViewModel.State,
    onStartDiscovery: () -> Unit,
    onStopDiscovery: () -> Unit,
    onDeviceClick: (DiscoveredDevice) -> Unit,
    onNavigateBack: () -> Unit,
    onPairDevice: () -> Unit,
    onDismissPairing: () -> Unit,
    onResolveConflicts: (ConflictResolutionStrategy) -> Unit,
    onDismissConflicts: () -> Unit,
    onCancelSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Show snackbar when error occurs
    LaunchedEffect(state.error) {
        state.error?.let { errorMessage ->
            snackbarHostState.showSnackbar(
                message = errorMessage,
                duration = SnackbarDuration.Long
            )
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("WiFi Sync (Experimental)") },
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
                },
                actions = {
                    // Sync status indicator in TopAppBar
                    SyncStatusIndicator(
                        syncStatus = state.syncStatus,
                        isDiscovering = state.isDiscovering
                    )
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Discovery control button
            Button(
                onClick = if (state.isDiscovering) onStopDiscovery else onStartDiscovery,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.isDiscovering) "Stop Discovery" else "Start Discovery")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Sync status card - always show to provide feedback
            SyncStatusCard(
                syncStatus = state.syncStatus,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Cancel sync button (only shown when sync is in progress)
            if (state.syncStatus is SyncStatus.Syncing) {
                Button(
                    onClick = onCancelSync,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Cancel Sync")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Device list
            if (state.discoveredDevices.isEmpty()) {
                EmptyDeviceList(
                    isDiscovering = state.isDiscovering,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = state.discoveredDevices,
                        key = { device -> device.deviceInfo.deviceId }
                    ) { device ->
                        DeviceListItem(
                            device = device,
                            onClick = { onDeviceClick(device) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
    
    // Pairing dialog
    if (state.showPairingDialog && state.selectedDevice != null) {
        PairingDialog(
            device = state.selectedDevice,
            onPair = onPairDevice,
            onDismiss = onDismissPairing
        )
    }
    
    // Conflict resolution dialog
    if (state.showConflictDialog && state.conflicts.isNotEmpty()) {
        ConflictResolutionDialog(
            conflicts = state.conflicts,
            onResolve = onResolveConflicts,
            onDismiss = onDismissConflicts
        )
    }
}

/**
 * Wrapper composable that integrates with SyncViewModel.
 * 
 * This is the entry point for the sync screen that handles ViewModel integration.
 * 
 * @param onNavigateBack Callback when user navigates back
 * @param modifier Optional modifier for the screen
 * @param viewModel SyncViewModel instance (injected via Koin)
 */
@Composable
fun SyncScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SyncViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    
    SyncScreen(
        state = state,
        onStartDiscovery = viewModel::startDiscovery,
        onStopDiscovery = viewModel::stopDiscovery,
        onDeviceClick = viewModel::selectDevice,
        onNavigateBack = onNavigateBack,
        onPairDevice = viewModel::pairDevice,
        onDismissPairing = viewModel::dismissPairingDialog,
        onResolveConflicts = viewModel::resolveConflicts,
        onDismissConflicts = viewModel::dismissConflictDialog,
        onCancelSync = viewModel::cancelSync,
        modifier = modifier
    )
}

/**
 * Sync status indicator for the TopAppBar.
 * Shows a visual indicator of the current sync state.
 * 
 * @param syncStatus Current sync status
 * @param isDiscovering Whether device discovery is active
 * @param modifier Optional modifier
 */
@Composable
private fun SyncStatusIndicator(
    syncStatus: SyncStatus,
    isDiscovering: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        when (syncStatus) {
            is SyncStatus.Idle -> {
                if (isDiscovering) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "Discovering",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Sync ready",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            is SyncStatus.Discovering -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Text(
                    text = "Discovering",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            is SyncStatus.Connecting -> {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Text(
                    text = "Connecting",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            is SyncStatus.Syncing -> {
                CircularProgressIndicator(
                    progress = { syncStatus.progress },
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Text(
                    text = "${(syncStatus.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            is SyncStatus.Completed -> {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Sync completed",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Completed",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            is SyncStatus.Failed -> {
                Icon(
                    imageVector = Icons.Default.SyncProblem,
                    contentDescription = "Sync failed",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "Failed",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

