package ireader.presentation.ui.sync.viewmodel

import ireader.domain.models.sync.*
import ireader.domain.usecases.sync.*
import ireader.presentation.core.viewmodel.IReaderStateScreenModel
import ireader.presentation.ui.sync.SyncErrorMapper
import ireader.presentation.ui.sync.SyncServiceController
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * ViewModel for the Local WiFi Book Sync feature.
 * Manages device discovery, sync operations, and conflict resolution.
 * 
 * Follows the IReaderStateScreenModel pattern for state management.
 * 
 * @param serviceController Optional controller for platform-specific service management (e.g., Android foreground service)
 */
class SyncViewModel(
    private val startSyncUseCase: StartSyncUseCase,
    private val stopSyncUseCase: StopSyncUseCase,
    private val syncWithDeviceUseCase: SyncWithDeviceUseCase,
    private val getDiscoveredDevicesUseCase: GetDiscoveredDevicesUseCase,
    private val getSyncStatusUseCase: GetSyncStatusUseCase,
    private val cancelSyncUseCase: CancelSyncUseCase,
    private val resolveConflictsUseCase: ResolveConflictsUseCase,
    private val serviceController: SyncServiceController? = null
) : IReaderStateScreenModel<SyncViewModel.State>(State()) {

    /**
     * State for the sync screen.
     */
    data class State(
        val discoveredDevices: List<DiscoveredDevice> = emptyList(),
        val syncStatus: SyncStatus = SyncStatus.Idle,
        val selectedDevice: DiscoveredDevice? = null,
        val isDiscovering: Boolean = false,
        val error: String? = null,
        val showPairingDialog: Boolean = false,
        val showConflictDialog: Boolean = false,
        val conflicts: List<DataConflict> = emptyList()
    )

    init {
        // Set up cancel callback for notification action
        serviceController?.setCancelCallback {
            cancelSync()
        }
        
        observeDiscoveredDevices()
        observeSyncStatus()
    }

    /**
     * Observe discovered devices from the repository.
     */
    private fun observeDiscoveredDevices() {
        getDiscoveredDevicesUseCase()
            .catch { e ->
                logError("Error observing discovered devices", e)
                updateState { it.copy(error = formatError(e)) }
            }
            .onEach { devices ->
                updateState { it.copy(discoveredDevices = devices) }
            }
            .launchIn(screenModelScope)
    }

    /**
     * Observe sync status from the repository.
     * Also manages the foreground service lifecycle based on sync status.
     */
    private fun observeSyncStatus() {
        getSyncStatusUseCase()
            .catch { e ->
                logError("Error observing sync status", e)
                updateState { it.copy(error = formatError(e)) }
            }
            .onEach { status ->
                updateState { it.copy(syncStatus = status) }
                
                // Manage service lifecycle based on sync status
                when (status) {
                    is SyncStatus.Syncing -> {
                        // Start or update foreground service
                        serviceController?.let {
                            if (status.progress == 0f) {
                                it.startService(status.deviceName)
                            } else {
                                val progressPercent = (status.progress * 100).toInt()
                                it.updateProgress(
                                    progressPercent, 
                                    status.currentItem,
                                    status.currentIndex,
                                    status.totalItems
                                )
                            }
                        }
                    }
                    is SyncStatus.Completed -> {
                        // Show completion notification
                        serviceController?.showCompletionNotification(
                            status.deviceName,
                            status.syncedItems,
                            status.duration
                        )
                        // Stop foreground service
                        serviceController?.stopService()
                    }
                    is SyncStatus.Failed -> {
                        // Show error notification with mapped error message
                        val errorInfo = mapSyncError(status.error)
                        serviceController?.showErrorNotification(
                            status.deviceName,
                            errorInfo.message,
                            errorInfo.suggestion
                        )
                        // Stop foreground service
                        serviceController?.stopService()
                        // Update state with error
                        updateState { it.copy(error = errorInfo.message) }
                    }
                    else -> {
                        // No service management needed for other states
                    }
                }
            }
            .launchIn(screenModelScope)
    }

    /**
     * Start device discovery on the local network.
     */
    fun startDiscovery() {
        screenModelScope.launch {
            try {
                logInfo("Starting device discovery")
                updateState { it.copy(isDiscovering = true, error = null) }
                
                startSyncUseCase()
                    .onSuccess {
                        logInfo("Device discovery started successfully")
                    }
                    .onFailure { e ->
                        logError("Failed to start discovery", e)
                        updateState { 
                            it.copy(
                                isDiscovering = false,
                                error = formatError(e)
                            ) 
                        }
                    }
            } catch (e: Exception) {
                logError("Exception starting discovery", e)
                updateState { 
                    it.copy(
                        isDiscovering = false,
                        error = formatError(e)
                    ) 
                }
            }
        }
    }

    /**
     * Stop device discovery.
     */
    fun stopDiscovery() {
        screenModelScope.launch {
            try {
                logInfo("Stopping device discovery")
                
                stopSyncUseCase()
                    .onSuccess {
                        logInfo("Device discovery stopped successfully")
                        updateState { it.copy(isDiscovering = false) }
                    }
                    .onFailure { e ->
                        logError("Failed to stop discovery", e)
                        updateState { 
                            it.copy(
                                isDiscovering = false,
                                error = formatError(e)
                            ) 
                        }
                    }
            } catch (e: Exception) {
                logError("Exception stopping discovery", e)
                updateState { 
                    it.copy(
                        isDiscovering = false,
                        error = formatError(e)
                    ) 
                }
            }
        }
    }

    /**
     * Select a device for syncing.
     */
    fun selectDevice(device: DiscoveredDevice) {
        logInfo("Selected device: ${device.deviceInfo.deviceName}")
        updateState { it.copy(selectedDevice = device) }
    }

    /**
     * Initiate sync with the specified device.
     */
    fun syncWithDevice(deviceId: String, conflictStrategy: ConflictResolutionStrategy = ConflictResolutionStrategy.LATEST_TIMESTAMP) {
        screenModelScope.launch {
            try {
                logInfo("Starting sync with device: $deviceId with strategy: $conflictStrategy")
                updateState { it.copy(error = null) }
                
                syncWithDeviceUseCase(deviceId, conflictStrategy)
                    .onSuccess {
                        logInfo("Sync initiated successfully with device: $deviceId")
                    }
                    .onFailure { e ->
                        logError("Failed to sync with device: $deviceId", e)
                        updateState { it.copy(error = formatError(e)) }
                    }
            } catch (e: Exception) {
                logError("Exception syncing with device: $deviceId", e)
                updateState { it.copy(error = formatError(e)) }
            }
        }
    }

    /**
     * Cancel the ongoing sync operation.
     */
    fun cancelSync() {
        screenModelScope.launch {
            try {
                logInfo("Cancelling sync operation")
                
                // Stop the foreground service immediately
                serviceController?.cancelSync()
                
                cancelSyncUseCase()
                    .onSuccess {
                        logInfo("Sync cancelled successfully")
                    }
                    .onFailure { e ->
                        logError("Failed to cancel sync", e)
                        updateState { it.copy(error = formatError(e)) }
                    }
            } catch (e: Exception) {
                logError("Exception cancelling sync", e)
                updateState { it.copy(error = formatError(e)) }
            }
        }
    }

    /**
     * Dismiss the error message.
     */
    fun dismissError() {
        updateState { it.copy(error = null) }
    }

    /**
     * Show the pairing dialog.
     */
    fun showPairingDialog() {
        updateState { it.copy(showPairingDialog = true) }
    }

    /**
     * Dismiss the pairing dialog.
     */
    fun dismissPairingDialog() {
        updateState { it.copy(showPairingDialog = false) }
    }

    /**
     * Show the conflict resolution dialog with the given conflicts.
     */
    fun showConflictDialog(conflicts: List<DataConflict>) {
        updateState { 
            it.copy(
                showConflictDialog = true,
                conflicts = conflicts
            ) 
        }
    }

    /**
     * Dismiss the conflict resolution dialog.
     */
    fun dismissConflictDialog() {
        updateState { 
            it.copy(
                showConflictDialog = false,
                conflicts = emptyList()
            ) 
        }
    }
    
    /**
     * Pair with the selected device.
     */
    fun pairDevice() {
        val device = state.value.selectedDevice ?: return
        
        screenModelScope.launch {
            try {
                logInfo("Pairing with device: ${device.deviceInfo.deviceName}")
                
                // Initiate sync with the device (which includes pairing)
                syncWithDevice(device.deviceInfo.deviceId)
                
                // Dismiss the pairing dialog
                dismissPairingDialog()
            } catch (e: Exception) {
                logError("Exception pairing with device", e)
                updateState { it.copy(error = formatError(e)) }
            }
        }
    }
    
    /**
     * Resolve all conflicts using the specified strategy.
     */
    fun resolveConflicts(strategy: ConflictResolutionStrategy) {
        val conflicts = state.value.conflicts
        if (conflicts.isEmpty()) return
        
        screenModelScope.launch {
            try {
                logInfo("Resolving ${conflicts.size} conflicts with strategy: $strategy")
                
                val result = resolveConflictsUseCase(conflicts, strategy)
                result.onSuccess {
                    logInfo("All conflicts resolved successfully")
                    dismissConflictDialog()
                }
                .onFailure { e ->
                    logError("Failed to resolve conflicts", e)
                    updateState { it.copy(error = formatError(e)) }
                }
            } catch (e: Exception) {
                logError("Exception resolving conflicts", e)
                updateState { it.copy(error = formatError(e)) }
            }
        }
    }

    /**
     * Resolve a data conflict using the specified strategy.
     */
    fun resolveConflict(conflict: DataConflict, strategy: ConflictResolutionStrategy) {
        screenModelScope.launch {
            try {
                logInfo("Resolving conflict: ${conflict.conflictField} with strategy: $strategy")
                
                val result = resolveConflictsUseCase(listOf(conflict), strategy)
                result.onSuccess {
                    logInfo("Conflict resolved successfully")
                    // Remove the resolved conflict from the list
                    val remainingConflicts = state.value.conflicts.filter { it != conflict }
                    updateState { 
                        it.copy(
                            conflicts = remainingConflicts,
                            showConflictDialog = remainingConflicts.isNotEmpty()
                        ) 
                    }
                }
                .onFailure { e ->
                    logError("Failed to resolve conflict", e)
                    updateState { it.copy(error = formatError(e)) }
                }
            } catch (e: Exception) {
                logError("Exception resolving conflict", e)
                updateState { it.copy(error = formatError(e)) }
            }
        }
    }

    /**
     * Map a SyncError to user-friendly error message and suggestion.
     */
    private fun mapSyncError(error: SyncError): SyncErrorMapper.ErrorInfo {
        return SyncErrorMapper.mapError(error)
    }

    /**
     * Format an error for display to the user.
     * Converts technical errors into user-friendly messages.
     */
    private fun formatError(error: Throwable): String {
        return when {
            error.message?.contains("network", ignoreCase = true) == true ||
            error.message?.contains("wifi", ignoreCase = true) == true ->
                "WiFi connection lost. Please check your network and try again."
            
            error.message?.contains("connection", ignoreCase = true) == true ->
                "Failed to connect to device. Please ensure both devices are on the same network."
            
            error.message?.contains("authentication", ignoreCase = true) == true ||
            error.message?.contains("pairing", ignoreCase = true) == true ->
                "Device authentication failed. Please try pairing again."
            
            error.message?.contains("version", ignoreCase = true) == true ||
            error.message?.contains("incompatible", ignoreCase = true) == true ->
                "App versions are incompatible. Please update the app on both devices."
            
            error.message?.contains("storage", ignoreCase = true) == true ||
            error.message?.contains("space", ignoreCase = true) == true ->
                "Insufficient storage space. Please free up some space and try again."
            
            error.message?.contains("transfer", ignoreCase = true) == true ->
                "Data transfer failed. Please try again."
            
            error.message?.contains("conflict", ignoreCase = true) == true ->
                "Failed to resolve data conflicts. Please try manual resolution."
            
            error.message?.contains("device not found", ignoreCase = true) == true ->
                "Device not found. Please ensure the device is still on the network."
            
            error.message?.contains("cancelled", ignoreCase = true) == true ->
                "Sync operation was cancelled."
            
            else -> error.message ?: "An unexpected error occurred. Please try again."
        }
    }

    override fun handleError(error: Throwable) {
        super.handleError(error)
        updateState { it.copy(error = formatError(error)) }
    }
}
