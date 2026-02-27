package ireader.data.sync.datasource

import ireader.core.log.Log

/**
 * iOS implementation of SocketConfigurator.
 * 
 * On iOS, network binding and VPN bypass are handled differently than Android.
 * iOS doesn't provide direct APIs to bind processes to specific networks.
 * 
 * For local network sync:
 * - iOS automatically routes local network traffic correctly
 * - VPN typically doesn't interfere with local network communication
 * - If issues occur, users need to configure VPN split tunneling
 */
actual class SocketConfigurator {
    
    /**
     * iOS doesn't support process-level network binding like Android.
     * Local network traffic is typically routed correctly by the OS.
     */
    actual suspend fun bindToWiFiNetwork(): Boolean {
        Log.debug { "[SocketConfigurator] iOS: Using system default routing (VPN bypass not available on iOS)" }
        return true
    }
    
    /**
     * No-op on iOS.
     */
    actual fun resetNetworkBinding() {
        // No-op
    }
    
    /**
     * Check if network is available.
     * 
     * TODO: Implement using Network framework (NWPathMonitor) to detect WiFi availability.
     * For now, returns true to allow compilation.
     */
    actual suspend fun isWiFiAvailable(): Boolean {
        Log.debug { "[SocketConfigurator] iOS: Network availability check not yet implemented" }
        return true
    }
}
