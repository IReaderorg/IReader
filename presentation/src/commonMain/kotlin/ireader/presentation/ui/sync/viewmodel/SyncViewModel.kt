package ireader.presentation.ui.sync.viewmodel

import ireader.domain.models.sync.*
import ireader.domain.usecases.sync.*
import ireader.presentation.core.viewmodel.IReaderStateScreenModel
import ireader.presentation.ui.sync.SyncErrorMapper
import ireader.presentation.ui.sync.SyncServiceController
import kotlinx.coroutines.delay
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
    private val syncPreferences: ireader.domain.preferences.prefs.SyncPreferences,
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
        val conflicts: List<DataConflict> = emptyList(),
        val manualIp: String = "",
        val serverMode: String = "server" // "server" or "client", default to server for desktop
    )

    init {
        // Set up cancel callback for notification action
        serviceController?.setCancelCallback {
            cancelSync()
        }
        
        // Load server mode preference
        updateState { it.copy(serverMode = syncPreferences.actAsServer().get()) }
        
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
                    is SyncStatus.Connecting -> {
                        // Start foreground service when connecting (important for server mode)
                        // Server needs to stay alive while waiting for client connection
                        serviceController?.startService(status.deviceName)
                    }
                    is SyncStatus.Syncing -> {
                        // Update foreground service with progress
                        serviceController?.let {
                            val progressPercent = (status.progress * 100).toInt()
                            it.updateProgress(
                                progressPercent, 
                                status.currentItem,
                                status.currentIndex,
                                status.totalItems
                            )
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
        updateState { it.copy(selectedDevice = device, showPairingDialog = true) }
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
    
    // ========== Manual Control Methods ==========
    
    /**
     * Refresh device discovery.
     * Useful when network changes or devices don't appear.
     */
    fun refreshDiscovery() {
        screenModelScope.launch {
            try {
                logInfo("Refreshing device discovery")
                
                // Stop current discovery
                stopDiscovery()
                
                // Wait a moment for cleanup
                delay(500)
                
                // Restart discovery
                startDiscovery()
                
                logInfo("Discovery refreshed successfully")
            } catch (e: Exception) {
                logError("Failed to refresh discovery", e)
                updateState { it.copy(error = formatError(e)) }
            }
        }
    }
    
    /**
     * Update manual IP address input.
     */
    fun updateManualIp(ip: String) {
        updateState { it.copy(manualIp = ip) }
    }
    
    /**
     * Connect to a device using manual IP address.
     * Creates a temporary DeviceInfo and initiates sync.
     */
    fun connectToManualIp(ip: String, port: Int = 8963) {
        screenModelScope.launch {
            try {
                logInfo("Connecting to manual IP: $ip:$port")
                
                // Validate IP address
                if (!isValidIp(ip)) {
                    updateState { it.copy(error = "Invalid IP address format") }
                    return@launch
                }
                
                // Create temporary DeviceInfo for manual connection
                val deviceInfo = DeviceInfo(
                    deviceId = "manual-$ip",
                    deviceName = "Manual Device ($ip)",
                    deviceType = DeviceType.DESKTOP, // Default to DESKTOP for manual connections
                    appVersion = "unknown",
                    ipAddress = ip,
                    port = port,
                    lastSeen = System.currentTimeMillis()
                )
                
                // Create DiscoveredDevice
                val discoveredDevice = DiscoveredDevice(
                    deviceInfo = deviceInfo,
                    isReachable = true,
                    discoveredAt = System.currentTimeMillis()
                )
                
                // Select and sync with device
                selectDevice(discoveredDevice)
                syncWithDevice(deviceInfo.deviceId)
                
                logInfo("Manual connection initiated")
            } catch (e: Exception) {
                logError("Failed to connect to manual IP", e)
                updateState { it.copy(error = formatError(e)) }
            }
        }
    }
    
    /**
     * Validate IP address format.
     */
    private fun isValidIp(ip: String): Boolean {
        val parts = ip.split(".")
        if (parts.size != 4) return false
        return parts.all { part ->
            part.toIntOrNull()?.let { num -> num in 0..255 } ?: false
        }
    }
    
    /**
     * Set server/client mode preference.
     * 
     * @param mode "auto" for automatic, "server" to force server, "client" to force client
     */
    fun setServerMode(mode: String) {
        screenModelScope.launch {
            try {
                logInfo("Setting server mode to: $mode")
                
                // Validate mode
                if (mode !in listOf("auto", "server", "client")) {
                    logError("Invalid server mode: $mode", null)
                    return@launch
                }
                
                // Update preference
                syncPreferences.actAsServer().set(mode)
                
                // Update state
                updateState { it.copy(serverMode = mode) }
                
                logInfo("Server mode updated successfully")
            } catch (e: Exception) {
                logError("Failed to set server mode", e)
                updateState { it.copy(error = formatError(e)) }
            }
        }
    }
}
