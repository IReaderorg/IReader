package ireader.data.remote

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import platform.Network.*
import platform.darwin.dispatch_get_main_queue
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * iOS implementation of network connectivity monitoring
 * 
 * Uses NWPathMonitor from Network framework for real-time connectivity updates
 */
@OptIn(ExperimentalForeignApi::class)
actual class NetworkConnectivityMonitor {
    
    private var pathMonitor: nw_path_monitor_t? = null
    private val scope = CoroutineScope(Dispatchers.Default)
    
    private val _isConnected = MutableStateFlow(true)
    actual val isConnected: StateFlow<Boolean> = _isConnected
    
    private val _connectionType = MutableStateFlow(ConnectionType.UNKNOWN)
    val connectionType: StateFlow<ConnectionType> = _connectionType
    
    /**
     * Start monitoring network connectivity
     */
    actual fun startMonitoring() {
        stopMonitoring()
        
        // Create path monitor
        pathMonitor = nw_path_monitor_create()
        
        pathMonitor?.let { monitor ->
            // Set update handler
            nw_path_monitor_set_update_handler(monitor) { path ->
                val status = nw_path_get_status(path)
                
                // Update connection state
                val connected = status == nw_path_status_satisfied || 
                               status == nw_path_status_satisfiable
                _isConnected.value = connected
                
                // Determine connection type
                _connectionType.value = when {
                    !connected -> ConnectionType.NONE
                    nw_path_uses_interface_type(path, nw_interface_type_wifi) -> ConnectionType.WIFI
                    nw_path_uses_interface_type(path, nw_interface_type_cellular) -> ConnectionType.CELLULAR
                    nw_path_uses_interface_type(path, nw_interface_type_wired) -> ConnectionType.ETHERNET
                    else -> ConnectionType.UNKNOWN
                }
            }
            
            // Set queue and start
            nw_path_monitor_set_queue(monitor, dispatch_get_main_queue())
            nw_path_monitor_start(monitor)
        }
    }
    
    /**
     * Stop monitoring network connectivity
     */
    actual fun stopMonitoring() {
        pathMonitor?.let { monitor ->
            nw_path_monitor_cancel(monitor)
        }
        pathMonitor = null
    }
    
    /**
     * Check if currently connected to WiFi
     */
    fun isWifiConnected(): Boolean {
        return _isConnected.value && _connectionType.value == ConnectionType.WIFI
    }
    
    /**
     * Check if currently connected to cellular
     */
    fun isCellularConnected(): Boolean {
        return _isConnected.value && _connectionType.value == ConnectionType.CELLULAR
    }
    
    /**
     * Check if connection is expensive (cellular)
     */
    fun isConnectionExpensive(): Boolean {
        return _connectionType.value == ConnectionType.CELLULAR
    }
}

/**
 * Connection type enum
 */
enum class ConnectionType {
    NONE,
    WIFI,
    CELLULAR,
    ETHERNET,
    UNKNOWN
}
