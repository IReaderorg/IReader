package ireader.data.remote

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Monitors network connectivity and triggers sync when connection is restored
 * Platform-specific implementations should update the connectivity state
 * 
 * Requirements: 8.2, 10.3
 */
expect class NetworkConnectivityMonitor() {
    /**
     * Starts monitoring network connectivity
     */
    fun startMonitoring()
    
    /**
     * Stops monitoring network connectivity
     */
    fun stopMonitoring()
    
    /**
     * Observable flow of network connectivity status
     */
    val isConnected: StateFlow<Boolean>
}
