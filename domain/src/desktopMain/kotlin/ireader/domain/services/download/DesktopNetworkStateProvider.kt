package ireader.domain.services.download

import ireader.core.log.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.InetSocketAddress
import java.net.Socket

/**
 * Desktop implementation of NetworkStateProvider.
 * Uses periodic connectivity checks since desktop doesn't have native network callbacks.
 * Assumes WiFi when connected (desktop typically uses wired/WiFi, not cellular).
 */
class DesktopNetworkStateProvider : NetworkStateProvider {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private val _networkState = MutableStateFlow(checkConnectivity())
    override val networkState: StateFlow<NetworkType> = _networkState.asStateFlow()
    
    init {
        startPeriodicCheck()
    }
    
    private fun startPeriodicCheck() {
        scope.launch {
            while (isActive) {
                val newState = checkConnectivity()
                if (_networkState.value != newState) {
                    _networkState.value = newState
                    Log.debug { "NetworkStateProvider: State changed to $newState" }
                }
                delay(CHECK_INTERVAL_MS)
            }
        }
    }
    
    private fun checkConnectivity(): NetworkType {
        return try {
            // Try to connect to a reliable host to check internet connectivity
            Socket().use { socket ->
                socket.connect(InetSocketAddress(CONNECTIVITY_CHECK_HOST, CONNECTIVITY_CHECK_PORT), TIMEOUT_MS)
                // Desktop is typically WiFi or Ethernet, treat as WiFi for download purposes
                NetworkType.WIFI
            }
        } catch (e: Exception) {
            // Try alternative host
            try {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress(CONNECTIVITY_CHECK_HOST_ALT, CONNECTIVITY_CHECK_PORT), TIMEOUT_MS)
                    NetworkType.WIFI
                }
            } catch (e2: Exception) {
                Log.debug { "NetworkStateProvider: No connectivity detected" }
                NetworkType.NONE
            }
        }
    }
    
    companion object {
        private const val CHECK_INTERVAL_MS = 30_000L // Check every 30 seconds
        private const val TIMEOUT_MS = 3_000 // 3 second timeout
        private const val CONNECTIVITY_CHECK_HOST = "8.8.8.8" // Google DNS
        private const val CONNECTIVITY_CHECK_HOST_ALT = "1.1.1.1" // Cloudflare DNS
        private const val CONNECTIVITY_CHECK_PORT = 53 // DNS port
    }
}
