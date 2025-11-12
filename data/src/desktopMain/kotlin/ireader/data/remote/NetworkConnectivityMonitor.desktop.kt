package ireader.data.remote

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Desktop implementation of network connectivity monitoring
 * Uses periodic polling to check connectivity
 */
actual class NetworkConnectivityMonitor {
    
    private var monitoringJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    
    private val _isConnected = MutableStateFlow(true)
    actual val isConnected: StateFlow<Boolean> = _isConnected
    
    actual fun startMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = scope.launch {
            while (isActive) {
                _isConnected.value = checkConnectivity()
                delay(5000) // Check every 5 seconds
            }
        }
    }
    
    actual fun stopMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = null
    }
    
    private fun checkConnectivity(): Boolean {
        return try {
            // Try to connect to Google DNS
            Socket().use { socket ->
                socket.connect(InetSocketAddress("8.8.8.8", 53), 2000)
                true
            }
        } catch (e: Exception) {
            false
        }
    }
}
