package ireader.data.remote

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

/**
 * Service that automatically syncs queued data when network connectivity is restored
 * 
 * Requirements: 8.2, 10.3
 */
class AutoSyncService(
    private val networkMonitor: NetworkConnectivityMonitor,
    private val remoteRepository: SupabaseRemoteRepository
) {
    private var monitoringJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    
    /**
     * Starts monitoring network connectivity and auto-syncing
     */
    fun start() {
        networkMonitor.startMonitoring()
        
        var wasConnected = false
        
        monitoringJob = scope.launch {
            networkMonitor.isConnected
                .collect { isConnected ->
                    // Only react when transitioning from disconnected to connected
                    if (isConnected && !wasConnected) {
                        // Network is connected, process sync queue
                        try {
                            remoteRepository.processSyncQueue()
                        } catch (_: Exception) {
                            // Silently ignore sync errors
                        }
                    }
                    wasConnected = isConnected
                }
        }
    }
    
    /**
     * Stops monitoring and auto-syncing
     */
    fun stop() {
        monitoringJob?.cancel()
        monitoringJob = null
        networkMonitor.stopMonitoring()
    }
}
