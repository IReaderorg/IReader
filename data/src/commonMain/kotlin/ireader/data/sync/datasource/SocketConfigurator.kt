package ireader.data.sync.datasource

/**
 * Platform-specific network configuration for WiFi sync.
 * 
 * Provides platform-specific network configuration to ensure WiFi sync
 * bypasses VPN and proxy settings for direct local network communication.
 */
expect class SocketConfigurator {
    /**
     * Bind the current process to the WiFi network, bypassing VPN.
     * 
     * On Android: Binds process to the WiFi network using ConnectivityManager
     * On Desktop: No-op (VPN bypass not needed)
     * 
     * Call this before creating sockets for WiFi sync.
     * Must call resetNetworkBinding() when done.
     * 
     * @return true if binding was successful, false otherwise
     */
    suspend fun bindToWiFiNetwork(): Boolean
    
    /**
     * Reset network binding to default routing.
     * 
     * Call this after WiFi sync is complete to restore normal network routing.
     */
    fun resetNetworkBinding()
    
    /**
     * Check if WiFi network is available.
     * 
     * @return true if WiFi is available, false otherwise
     */
    suspend fun isWiFiAvailable(): Boolean
}
