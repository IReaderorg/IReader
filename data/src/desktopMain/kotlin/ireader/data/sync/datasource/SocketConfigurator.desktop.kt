package ireader.data.sync.datasource

import ireader.core.log.Log
import java.net.NetworkInterface

/**
 * Desktop implementation of SocketConfigurator.
 * 
 * On desktop platforms, we can't directly bind sockets to specific networks like Android,
 * but we can detect VPN interfaces and warn the user if needed.
 */
actual class SocketConfigurator {
    
    /**
     * Check if VPN is active and log a warning.
     * Desktop doesn't support process-level network binding like Android.
     */
    actual suspend fun bindToWiFiNetwork(): Boolean {
        val vpnActive = isVPNActive()
        if (vpnActive) {
            Log.warn { "[SocketConfigurator] Desktop: VPN detected. If sync fails, try disabling VPN temporarily." }
            Log.info { "[SocketConfigurator] Desktop: Using system default routing (VPN bypass not available on desktop)" }
        } else {
            Log.debug { "[SocketConfigurator] Desktop: No VPN detected, using default routing" }
        }
        return true
    }
    
    /**
     * No-op on desktop.
     */
    actual fun resetNetworkBinding() {
        // No-op
    }
    
    /**
     * Check if network is available (always true on desktop for simplicity).
     */
    actual suspend fun isWiFiAvailable(): Boolean {
        return true
    }
    
    /**
     * Detect if a VPN interface is active.
     * Checks for common VPN interface patterns.
     */
    private fun isVPNActive(): Boolean {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces().toList()
            interfaces.any { iface ->
                if (!iface.isUp) return@any false
                
                val name = iface.name.lowercase()
                val displayName = iface.displayName.lowercase()
                
                // Check for common VPN interface patterns
                name.contains("tun") || 
                name.contains("tap") || 
                name.contains("vpn") || 
                name.contains("v2ray") ||
                name.contains("ppp") ||
                displayName.contains("vpn") ||
                displayName.contains("virtual private")
            }
        } catch (e: Exception) {
            Log.error(e, "[SocketConfigurator] Failed to detect VPN: ${e.message}")
            false
        }
    }
}

