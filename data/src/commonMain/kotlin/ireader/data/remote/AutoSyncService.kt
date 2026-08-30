package ireader.data.remote

import kotlinx.coroutines.CoroutineScope
import ireader.domain.utils.extensions.ioDispatcher
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
    private val lock = Any()
    private var monitoringJob: Job? = null
    private val scope = CoroutineScope(kotlinx.coroutines.SupervisorJob() + ioDispatcher)
    
    /**
     * Starts monitoring network connectivity and auto-syncing
     */
    fun start() = synchronized(lock) {
        monitoringJob?.cancel()
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
    fun stop() = synchronized(lock) {
        monitoringJob?.cancel()
        monitoringJob = null
        networkMonitor.stopMonitoring()
    }
}
