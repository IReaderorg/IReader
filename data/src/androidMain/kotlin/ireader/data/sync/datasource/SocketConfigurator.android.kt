package ireader.data.sync.datasource

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import ireader.core.log.Log

/**
 * Android implementation of SocketConfigurator.
 * 
 * Configures process network binding to bypass VPN and use the underlying WiFi network directly.
 * This ensures WiFi sync works even when a VPN is active.
 */
actual class SocketConfigurator(private val context: Context) {
    
    private val connectivityManager: ConnectivityManager by lazy {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    
    private var boundNetwork: Network? = null
    
    /**
     * Bind process to WiFi network, bypassing VPN.
     * 
     * Uses ConnectivityManager.bindProcessToNetwork() to route all sockets
     * created by this process through the WiFi network, bypassing VPN.
     */
    actual suspend fun bindToWiFiNetwork(): Boolean {
        return try {
            val wifiNetwork = findWiFiNetwork()
            
            if (wifiNetwork == null) {
                Log.warn { "[SocketConfigurator] No WiFi network found, using default routing" }
                return false
            }
            
            // Bind process to WiFi network (bypasses VPN)
            val success = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                connectivityManager.bindProcessToNetwork(wifiNetwork)
            } else {
                @Suppress("DEPRECATION")
                ConnectivityManager.setProcessDefaultNetwork(wifiNetwork)
            }
            
            if (success) {
                boundNetwork = wifiNetwork
                Log.info { "[SocketConfigurator] Process bound to WiFi network (VPN bypassed)" }
            } else {
                Log.warn { "[SocketConfigurator] Failed to bind process to WiFi network" }
            }
            
            success
        } catch (e: Exception) {
            Log.error(e, "[SocketConfigurator] Failed to bind to WiFi network: ${e.message}")
            false
        }
    }
    
    /**
     * Reset network binding to default routing.
     * Call this when sync is complete to restore normal routing.
     */
    actual fun resetNetworkBinding() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                connectivityManager.bindProcessToNetwork(null)
            } else {
                @Suppress("DEPRECATION")
                ConnectivityManager.setProcessDefaultNetwork(null)
            }
            boundNetwork = null
            Log.debug { "[SocketConfigurator] Network binding reset to default" }
        } catch (e: Exception) {
            Log.error(e, "[SocketConfigurator] Failed to reset network binding: ${e.message}")
        }
    }
    
    /**
     * Check if WiFi network is available.
     */
    actual suspend fun isWiFiAvailable(): Boolean {
        return findWiFiNetwork() != null
    }
    
    /**
     * Find the active WiFi network.
     * 
     * Searches through all available networks to find one with WiFi transport.
     * Prefers networks without VPN transport (underlying WiFi).
     */
    private fun findWiFiNetwork(): Network? {
        return try {
            // Get all networks
            val allNetworks = connectivityManager.allNetworks
            
            // Find WiFi network (prefer non-VPN)
            var wifiNetwork: Network? = null
            var wifiWithVpn: Network? = null
            
            for (network in allNetworks) {
                val capabilities = connectivityManager.getNetworkCapabilities(network) ?: continue
                
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                        // WiFi with VPN overlay - save as fallback
                        wifiWithVpn = network
                    } else {
                        // Pure WiFi network (preferred) - use this
                        wifiNetwork = network
                        break
                    }
                }
            }
            
            // Prefer pure WiFi, fallback to WiFi with VPN
            val selectedNetwork = wifiNetwork ?: wifiWithVpn
            
            if (selectedNetwork != null) {
                Log.debug { "[SocketConfigurator] Found WiFi network: ${selectedNetwork}" }
            } else {
                Log.warn { "[SocketConfigurator] No WiFi network found" }
            }
            
            selectedNetwork
        } catch (e: Exception) {
            Log.error(e, "[SocketConfigurator] Failed to find WiFi network: ${e.message}")
            null
        }
    }
}

